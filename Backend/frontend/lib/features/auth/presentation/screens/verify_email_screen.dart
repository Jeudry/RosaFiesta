import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';
import 'package:frontend/core/api_client.dart';
import 'login_screen.dart';

class VerifyEmailScreen extends StatefulWidget {
  final String token;

  const VerifyEmailScreen({super.key, required this.token});

  @override
  State<VerifyEmailScreen> createState() => _VerifyEmailScreenState();
}

class _VerifyEmailScreenState extends State<VerifyEmailScreen>
    with TickerProviderStateMixin {
  late final AnimationController _floatCtrl;
  late final AnimationController _decoCtrl;
  late final AnimationController _pulseCtrl;
  late final AnimationController _gradCtrl;
  late final AnimationController _checkCtrl;

  bool _isLoading = true;
  bool _isSuccess = false;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _floatCtrl = AnimationController(vsync: this, duration: const Duration(milliseconds: 5500))..repeat();
    _decoCtrl = AnimationController(vsync: this, duration: const Duration(seconds: 20))..repeat();
    _pulseCtrl = AnimationController(vsync: this, duration: const Duration(seconds: 3))..repeat(reverse: true);
    _gradCtrl = AnimationController(vsync: this, duration: const Duration(seconds: 12))..repeat();
    _checkCtrl = AnimationController(vsync: this, duration: const Duration(milliseconds: 800));
    _verifyEmail();
  }

  @override
  void dispose() {
    _floatCtrl.dispose();
    _decoCtrl.dispose();
    _pulseCtrl.dispose();
    _gradCtrl.dispose();
    _checkCtrl.dispose();
    super.dispose();
  }

  Future<void> _verifyEmail() async {
    try {
      // GET /v1/users/active/{token} - activate account via token
      await ApiClient.get('/users/active/${widget.token}');
      _checkCtrl.forward();
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
          _isSuccess = false;
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
                  RfThemeToggle(t: t),
                ],
              ),
            ),
            // Content
            Expanded(
              child: Center(
                child: SingleChildScrollView(
                  physics: const BouncingScrollPhysics(),
                  padding: const EdgeInsets.fromLTRB(28, 32, 28, 40),
                  child: _buildContent(t),
                ),
              ),
            ),
          ]),
        ),
      ]),
    );
  }

  Widget _buildContent(RfTheme t) {
    if (_isLoading) {
      return _buildLoadingState(t);
    }
    if (_isSuccess) {
      return _buildSuccessState(t);
    }
    return _buildErrorState(t);
  }

  Widget _buildLoadingState(RfTheme t) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        TweenAnimationBuilder<double>(
          tween: Tween(begin: 0.0, end: 1.0),
          duration: const Duration(milliseconds: 800),
          builder: (context, value, child) {
            return Container(
              width: 100,
              height: 100,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                gradient: LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: [
                    AppColors.hotPink.withOpacity(value * 0.6),
                    AppColors.violet.withOpacity(value * 0.6),
                  ],
                ),
              ),
              child: Center(
                child: SizedBox(
                  width: 40,
                  height: 40,
                  child: CircularProgressIndicator(
                    color: Colors.white.withOpacity(value),
                    strokeWidth: 3,
                  ),
                ),
              ),
            );
          },
        ),
        const SizedBox(height: 32),
        ShaderMask(
          shaderCallback: (b) => AppColors.titleGradient.createShader(b),
          child: Text(
            'Verificando...',
            style: GoogleFonts.outfit(
              fontSize: 28,
              fontWeight: FontWeight.w700,
              color: Colors.white,
            ),
          ),
        ),
        const SizedBox(height: 12),
        Text(
          'Estamos verificando tu correo electrónico.',
          style: GoogleFonts.dmSans(
            fontSize: 14,
            color: t.textDim,
          ),
        ),
      ],
    );
  }

  Widget _buildSuccessState(RfTheme t) {
    return Column(
      children: [
        TweenAnimationBuilder<double>(
          tween: Tween(begin: 0.0, end: 1.0),
          duration: const Duration(milliseconds: 800),
          builder: (context, value, child) {
            return Container(
              width: 120,
              height: 120,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                gradient: LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: [
                    AppColors.teal.withOpacity(value),
                    AppColors.sky.withOpacity(value),
                  ],
                ),
                boxShadow: [
                  BoxShadow(
                    color: AppColors.teal.withOpacity(0.4 * value),
                    blurRadius: 30,
                    spreadRadius: 5,
                  ),
                ],
              ),
              child: Center(
                child: Icon(
                  Icons.check_rounded,
                  color: Colors.white.withOpacity(value),
                  size: 60 * value,
                ),
              ),
            );
          },
        ),
        const SizedBox(height: 32),
        ShaderMask(
          shaderCallback: (b) => AppColors.titleGradient.createShader(b),
          child: Text(
            '¡Correo verificado!',
            style: GoogleFonts.outfit(
              fontSize: 28,
              fontWeight: FontWeight.w700,
              color: Colors.white,
            ),
          ),
        ),
        const SizedBox(height: 12),
        Text(
          'Tu cuenta ha sido activada correctamente.\nAhora puedes iniciar sesión.',
          textAlign: TextAlign.center,
          style: GoogleFonts.dmSans(
            fontSize: 14,
            color: t.textDim,
          ),
        ),
        const SizedBox(height: 32),
        RfLuxeButton(
          label: 'Iniciar sesión',
          onTap: () => _navigateToLogin(),
        ),
      ],
    );
  }

  Widget _buildErrorState(RfTheme t) {
    return Column(
      children: [
        Container(
          width: 100,
          height: 100,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: AppColors.coral.withOpacity(0.15),
          ),
          child: const Icon(Icons.error_outline_rounded, color: AppColors.coral, size: 50),
        ),
        const SizedBox(height: 32),
        Text(
          'Error de verificación',
          style: GoogleFonts.outfit(
            fontSize: 24,
            fontWeight: FontWeight.w700,
            color: t.textPrimary,
          ),
        ),
        const SizedBox(height: 12),
        Text(
          _errorMessage ?? 'El enlace ha expirado o es inválido.',
          textAlign: TextAlign.center,
          style: GoogleFonts.dmSans(
            fontSize: 14,
            color: t.textDim,
          ),
        ),
        const SizedBox(height: 32),
        RfLuxeButton(
          label: 'Volver',
          onTap: () => _navigateToLogin(),
        ),
      ],
    );
  }

  void _navigateToLogin() {
    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (_) => const LoginScreen()),
      (route) => false,
    );
  }
}