import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/l10n/generated/app_localizations.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import '../auth_provider.dart';
import '../../../shell/main_shell.dart';
import 'register_screen.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});
  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen>
    with TickerProviderStateMixin {
  final _emailController    = TextEditingController();
  final _passwordController = TextEditingController();
  final _formKey = GlobalKey<FormState>();

  late final AnimationController _floatCtrl;
  late final AnimationController _decoCtrl;
  late final AnimationController _pulseCtrl;
  late final AnimationController _gradCtrl;

  @override
  void initState() {
    super.initState();
    _floatCtrl = AnimationController(vsync: this,
        duration: const Duration(milliseconds: 5500))..repeat();
    _decoCtrl  = AnimationController(vsync: this,
        duration: const Duration(seconds: 20))..repeat();
    _pulseCtrl = AnimationController(vsync: this,
        duration: const Duration(seconds: 3))..repeat(reverse: true);
    _gradCtrl  = AnimationController(vsync: this,
        duration: const Duration(seconds: 12))..repeat();
  }

  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    _floatCtrl.dispose();
    _decoCtrl.dispose();
    _pulseCtrl.dispose();
    _gradCtrl.dispose();
    super.dispose();
  }

  Future<void> _submit(AuthProvider auth, AppLocalizations l10n) async {
    if (!_formKey.currentState!.validate()) return;
    final navigator  = Navigator.of(context);
    final messenger  = ScaffoldMessenger.of(context);
    final successMsg = l10n.loginSuccess;
    await auth.login(_emailController.text, _passwordController.text);
    if (!mounted) return;
    if (auth.isAuthenticated) {
      messenger.showSnackBar(SnackBar(
        content: Row(
          children: [
            const Icon(Icons.check_circle_rounded, color: Colors.white, size: 20),
            const SizedBox(width: 10),
            Text(successMsg, style: const TextStyle(
              color: Colors.white, fontWeight: FontWeight.w600)),
          ],
        ),
        backgroundColor: const Color(0xFF00C853),
        behavior: SnackBarBehavior.floating,
        margin: const EdgeInsets.fromLTRB(20, 0, 20, 0),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        duration: const Duration(seconds: 2),
      ));
      navigator.pushAndRemoveUntil(
        MaterialPageRoute(builder: (_) => const MainShell()),
        (route) => false,
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthProvider>();
    final l10n = AppLocalizations.of(context)!;
    final t    = RfTheme.of(context);

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
          decoController:  _decoCtrl,
          pulseController: _pulseCtrl,
          baseOpacity: t.isDark ? 1.0 : 1.8,
        ),
        Positioned.fill(child: IgnorePointer(child: CustomPaint(
          painter: RfGridPainter(
            color: (t.isDark ? Colors.white : Colors.black)
                .withOpacity(0.015)),
        ))),
        SafeArea(
          child: Column(children: [
            // ── Top bar ──
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
            // ── Content ──
            Expanded(
              child: SingleChildScrollView(
                physics: const BouncingScrollPhysics(),
                padding: const EdgeInsets.fromLTRB(28, 32, 28, 40),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    ShaderMask(
                      shaderCallback: (b) =>
                          AppColors.titleGradient.createShader(b),
                      child: Text('Bienvenido',
                        style: GoogleFonts.outfit(
                          fontSize: 42,
                          fontWeight: FontWeight.w800,
                          color: Colors.white,
                          height: 1.15,
                          letterSpacing: -0.5,
                        ),
                      ),
                    ),
                    const SizedBox(height: 28),
                    _socialCard(t),
                    const SizedBox(height: 20),
                    t.isDark
                        ? ClipRRect(
                            borderRadius: BorderRadius.circular(24),
                            child: BackdropFilter(
                              filter: ImageFilter.blur(
                                  sigmaX: 16, sigmaY: 16),
                              child: _card(auth, l10n, t),
                            ),
                          )
                        : _card(auth, l10n, t),
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
        padding:
            const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(20),
          color: t.isDark
              ? Colors.white.withOpacity(0.06)
              : Colors.black.withOpacity(0.04),
          border: Border.all(color: t.borderFaint),
        ),
        child: Row(mainAxisSize: MainAxisSize.min, children: [
          Icon(Icons.arrow_back_ios_new_rounded,
              color: t.isDark
                  ? Colors.white54
                  : const Color(0xFF5A5A80),
              size: 13),
          const SizedBox(width: 6),
          Text('Volver',
              style: GoogleFonts.dmSans(
                color: t.textMuted,
                fontSize: 12,
                fontWeight: FontWeight.w500,
                letterSpacing: 1.5,
              )),
        ]),
      ),
    );
  }

  Widget _socialCard(RfTheme t) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: t.isDark
            ? Colors.white.withOpacity(0.05)
            : Colors.white.withOpacity(0.85),
        borderRadius: BorderRadius.circular(22),
        border: Border.all(color: t.borderFaint),
      ),
      child: Column(
        children: [
          Text('Continuar con',
              style: GoogleFonts.dmSans(
                fontSize: 13,
                fontWeight: FontWeight.w600,
                color: t.textMuted,
                letterSpacing: 0.3,
              )),
          const SizedBox(height: 14),
          Row(
            children: [
              _socialIcon(
                const FaIcon(FontAwesomeIcons.google,
                    color: Color(0xFF4285F4), size: 20),
                t,
                onTap: () {}, // TODO: Google sign-in
              ),
              const SizedBox(width: 12),
              _socialIcon(
                Icon(Icons.apple_rounded,
                    color: t.isDark ? Colors.white : Colors.black,
                    size: 24),
                t,
                onTap: () {}, // TODO: Apple sign-in
              ),
              const SizedBox(width: 12),
              _socialIcon(
                const FaIcon(FontAwesomeIcons.instagram,
                    color: Color(0xFFE1306C), size: 20),
                t,
                onTap: () {}, // TODO: Instagram sign-in
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _socialIcon(
    Widget iconWidget,
    RfTheme t, {
    VoidCallback? onTap,
  }) {
    return Expanded(
      child: GestureDetector(
        onTap: onTap,
        child: Container(
          height: 50,
          decoration: BoxDecoration(
            color: t.isDark
                ? Colors.white.withOpacity(0.06)
                : const Color(0xFFF5F6FA),
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: t.borderFaint),
          ),
          child: Center(child: iconWidget),
        ),
      ),
    );
  }

  Widget _card(AuthProvider auth, AppLocalizations l10n, RfTheme t) {
    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: t.isDark
            ? Colors.white.withOpacity(0.06)
            : Colors.white.withOpacity(0.93),
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
      child: Form(
        key: _formKey,
        child: Column(children: [
          RfFormField(
            label: 'Usuario o correo electrónico',
            icon:  Icons.person_outline_rounded,
            controller: _emailController,
            t: t,
            validator: (v) => (v == null || v.trim().isEmpty)
                ? 'Campo requerido'
                : null,
          ),
          const SizedBox(height: 14),
          RfFormField(
            label: l10n.passwordLabel,
            icon:  Icons.lock_outline_rounded,
            controller: _passwordController,
            t: t,
            obscure: true,
            validator: (v) =>
                (v == null || v.length < 3) ? l10n.passwordError : null,
          ),
          const SizedBox(height: 24),
          if (auth.error != null)
            Padding(
              padding: const EdgeInsets.only(bottom: 16),
              child: Text(auth.error!,
                style: GoogleFonts.dmSans(
                    color: AppColors.coral, fontSize: 13),
                textAlign: TextAlign.center,
              ),
            ),
          RfLuxeButton(
            label: l10n.loginButton,
            onTap: auth.isLoading ? () {} : () => _submit(auth, l10n),
            loading: auth.isLoading,
          ),
          const SizedBox(height: 16),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text('¿No tienes cuenta? ',
                style: GoogleFonts.dmSans(
                  color: t.textDim,
                  fontSize: 12,
                  letterSpacing: 0.3,
                )),
              GestureDetector(
                onTap: () {
                  Navigator.of(context).pushReplacement(
                    MaterialPageRoute(
                        builder: (_) => const RegisterScreen()),
                  );
                },
                child: Text('Registrarse',
                    style: GoogleFonts.dmSans(
                      color: AppColors.hotPink,
                      fontSize: 12,
                      fontWeight: FontWeight.w800,
                      decoration: TextDecoration.underline,
                      decorationColor: AppColors.hotPink,
                    )),
              ),
            ],
          ),
        ]),
      ),
    );
  }
}
