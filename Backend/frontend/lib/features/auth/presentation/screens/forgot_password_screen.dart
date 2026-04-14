import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';
import 'package:frontend/core/api_client.dart';
import 'login_screen.dart';

class ForgotPasswordScreen extends StatefulWidget {
  const ForgotPasswordScreen({super.key});

  @override
  State<ForgotPasswordScreen> createState() => _ForgotPasswordScreenState();
}

class _ForgotPasswordScreenState extends State<ForgotPasswordScreen>
    with TickerProviderStateMixin {
  final _emailController = TextEditingController();
  final _formKey = GlobalKey<FormState>();

  late final AnimationController _floatCtrl;
  late final AnimationController _decoCtrl;
  late final AnimationController _pulseCtrl;
  late final AnimationController _gradCtrl;

  bool _isLoading = false;
  bool _isSuccess = false;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _floatCtrl = AnimationController(vsync: this, duration: const Duration(milliseconds: 5500))..repeat();
    _decoCtrl = AnimationController(vsync: this, duration: const Duration(seconds: 20))..repeat();
    _pulseCtrl = AnimationController(vsync: this, duration: const Duration(seconds: 3))..repeat(reverse: true);
    _gradCtrl = AnimationController(vsync: this, duration: const Duration(seconds: 12))..repeat();
  }

  @override
  void dispose() {
    _emailController.dispose();
    _floatCtrl.dispose();
    _decoCtrl.dispose();
    _pulseCtrl.dispose();
    _gradCtrl.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      // POST /v1/authentication/forgot-password
      await ApiClient.post('/authentication/forgot-password', {
        'email': _emailController.text.trim(),
      });
      if (mounted) {
        setState(() {
          _isLoading = false;
          _isSuccess = true;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _isLoading = false;
          _errorMessage = e.toString();
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);

    return Scaffold(
      backgroundColor: t.base,
      body: Stack(children: [
        RfGradientOrbs(
          controller: _gradCtrl,
          color1: AppColors.hotPink,
          color2: AppColors.violet,
          isDark: t.isDark,
        ),
        RfDecoLayer(
          floatController: _floatCtrl,
          decoController: _decoCtrl,
          pulseController: _pulseCtrl,
          baseOpacity: t.isDark ? 1.0 : 1.8,
        ),
        Positioned.fill(child: IgnorePointer(child: CustomPaint(
          painter: RfGridPainter(
            color: (t.isDark ? Colors.white : Colors.black).withOpacity(0.015),
          ),
        ))),
        SafeArea(
          child: Column(children: [
            // Top bar
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 16, 20, 0),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  _backButton(t),
                  RfThemeToggle(t: t),
                ],
              ),
            ),
            // Content
            Expanded(
              child: SingleChildScrollView(
                physics: const BouncingScrollPhysics(),
                padding: const EdgeInsets.fromLTRB(28, 32, 28, 40),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    ShaderMask(
                      shaderCallback: (b) => AppColors.titleGradient.createShader(b),
                      child: Text(
                        'Recuperar contraseña',
                        style: GoogleFonts.outfit(
                          fontSize: 36,
                          fontWeight: FontWeight.w800,
                          color: Colors.white,
                          height: 1.15,
                          letterSpacing: -0.5,
                        ),
                      ),
                    ),
                    const SizedBox(height: 12),
                    Text(
                      'Ingresa tu correo y te enviaremos un enlace para restablecer tu contraseña.',
                      style: GoogleFonts.dmSans(
                        fontSize: 14,
                        color: t.textDim,
                      ),
                    ),
                    const SizedBox(height: 32),
                    if (_isSuccess)
                      _buildSuccessCard(t)
                    else
                      _buildForm(t),
                  ],
                ),
              ),
            ),
          ]),
        ),
      ]),
    );
  }

  Widget _backButton(RfTheme t) {
    return GestureDetector(
      onTap: () => Navigator.pop(context),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(20),
          color: t.isDark ? Colors.white.withOpacity(0.06) : Colors.black.withOpacity(0.04),
          border: Border.all(color: t.borderFaint),
        ),
        child: Row(mainAxisSize: MainAxisSize.min, children: [
          Icon(Icons.arrow_back_ios_new_rounded, color: t.textMuted, size: 13),
          const SizedBox(width: 6),
          Text('Volver', style: GoogleFonts.dmSans(color: t.textMuted, fontSize: 12, fontWeight: FontWeight.w500, letterSpacing: 1.5)),
        ]),
      ),
    );
  }

  Widget _buildForm(RfTheme t) {
    return Form(
      key: _formKey,
      child: Column(children: [
        Container(
          padding: const EdgeInsets.all(24),
          decoration: BoxDecoration(
            color: t.isDark ? Colors.white.withOpacity(0.06) : Colors.white.withOpacity(0.93),
            borderRadius: BorderRadius.circular(24),
            border: Border.all(color: t.borderFaint),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(t.isDark ? 0.0 : 0.07),
                blurRadius: 28,
                offset: const Offset(0, 10),
              ),
            ],
          ),
          child: Column(children: [
            RfFormField(
              label: 'Correo electrónico',
              icon: Icons.email_outlined,
              controller: _emailController,
              t: t,
              validator: (v) {
                if (v == null || v.isEmpty) return 'Campo requerido';
                if (!v.contains('@')) return 'Correo inválido';
                return null;
              },
            ),
            if (_errorMessage != null) ...[
              const SizedBox(height: 16),
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: AppColors.coral.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: AppColors.coral.withOpacity(0.3)),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.error_outline_rounded, color: AppColors.coral, size: 18),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        _errorMessage!,
                        style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.coral),
                      ),
                    ),
                  ],
                ),
              ),
            ],
            const SizedBox(height: 24),
            RfLuxeButton(
              label: 'Enviar enlace',
              onTap: _isLoading ? () {} : _submit,
              loading: _isLoading,
            ),
          ]),
        ),
      ]),
    );
  }

  Widget _buildSuccessCard(RfTheme t) {
    return Container(
      padding: const EdgeInsets.all(32),
      decoration: BoxDecoration(
        color: t.isDark ? Colors.white.withOpacity(0.06) : Colors.white.withOpacity(0.93),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: t.borderFaint),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(t.isDark ? 0.0 : 0.07),
            blurRadius: 28,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: Column(
        children: [
          Container(
            width: 80,
            height: 80,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: [AppColors.teal.withOpacity(0.8), AppColors.sky.withOpacity(0.8)],
              ),
              boxShadow: [
                BoxShadow(
                  color: AppColors.teal.withOpacity(0.3),
                  blurRadius: 20,
                  spreadRadius: 2,
                ),
              ],
            ),
            child: const Icon(Icons.mark_email_read_rounded, color: Colors.white, size: 40),
          ),
          const SizedBox(height: 24),
          ShaderMask(
            shaderCallback: (b) => AppColors.titleGradient.createShader(b),
            child: Text(
              '¡Enlace enviado!',
              style: GoogleFonts.outfit(
                fontSize: 22,
                fontWeight: FontWeight.w700,
                color: Colors.white,
              ),
            ),
          ),
          const SizedBox(height: 12),
          Text(
            'Revisa tu correo, te enviamos un enlace para restablecer tu contraseña.',
            textAlign: TextAlign.center,
            style: GoogleFonts.dmSans(fontSize: 14, color: t.textDim),
          ),
          const SizedBox(height: 28),
          RfLuxeButton(
            label: 'Volver al inicio de sesión',
            onTap: () => _navigateToLogin(),
          ),
        ],
      ),
    );
  }

  void _navigateToLogin() {
    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (_) => const LoginScreen()),
      (route) => false,
    );
  }
}