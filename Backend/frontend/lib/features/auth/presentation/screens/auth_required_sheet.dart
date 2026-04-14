import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/app_colors.dart';
import 'package:frontend/core/design_system.dart';

import '../auth_provider.dart';
import '../../../home/presentation/screens/welcome_onboarding_screen.dart';

/// Bottom sheet shown when an unauthenticated user tries to access
/// a protected action (checkout, reviews, favorites, creating events).
///
/// Shows "Inicia sesión para continuar" with two CTAs:
/// - "Iniciar sesión" → navigates to WelcomeOnboardingScreen
/// - "Cerrar" → dismisses the sheet
class AuthRequiredSheet extends StatelessWidget {
  const AuthRequiredSheet({super.key});

  /// Show the sheet. Call this directly from auth-gated actions.
  static Future<void> show(BuildContext context) {
    return showModalBottomSheet<void>(
      context: context,
      backgroundColor: Colors.transparent,
      barrierColor: Colors.black.withValues(alpha: 0.55),
      builder: (_) => const AuthRequiredSheet(),
    );
  }

  /// Convenience: checks auth first, shows sheet if not authenticated,
  /// returns true if auth was missing (caller should abort the action).
  /// Returns false if authenticated (caller may proceed).
  static bool checkAndShow(BuildContext context) {
    final auth = context.read<AuthProvider>();
    if (!auth.isAuthenticated) {
      show(context);
      return true;
    }
    return false;
  }

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);

    return Container(
      decoration: BoxDecoration(
        color: t.isDark ? t.card : Colors.white,
        borderRadius: const BorderRadius.vertical(top: Radius.circular(28)),
        boxShadow: [
          BoxShadow(
            color: AppColors.hotPink.withValues(alpha: 0.1),
            blurRadius: 32,
            offset: const Offset(0, -8),
          ),
        ],
      ),
      child: SafeArea(
        top: false,
        child: Padding(
          padding: const EdgeInsets.fromLTRB(24, 12, 24, 32),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              // Drag handle
              Center(
                child: Container(
                  width: 44, height: 4,
                  decoration: BoxDecoration(
                    color: t.textDim.withValues(alpha: 0.3),
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
              ),
              const SizedBox(height: 24),
              // Icon
              Container(
                width: 64, height: 64,
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                    colors: [AppColors.violet, AppColors.hotPink],
                  ),
                  shape: BoxShape.circle,
                ),
                child: const Icon(Icons.lock_outline_rounded,
                    color: Colors.white, size: 30),
              ),
              const SizedBox(height: 20),
              // Title
              ShaderMask(
                shaderCallback: (b) => AppColors.titleGradient.createShader(b),
                child: Text(
                  'Inicia sesión para continuar',
                  style: GoogleFonts.outfit(
                    fontSize: 20,
                    fontWeight: FontWeight.w800,
                    color: Colors.white,
                  ),
                ),
              ),
              const SizedBox(height: 10),
              Text(
                'Crea una cuenta o inicia sesión para acceder a esta función.',
                textAlign: TextAlign.center,
                style: GoogleFonts.dmSans(
                  fontSize: 14,
                  color: t.textMuted,
                  height: 1.4,
                ),
              ),
              const SizedBox(height: 28),
              // Primary CTA — Iniciar sesión
              GestureDetector(
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                        builder: (_) => const WelcomeOnboardingScreen()),
                  );
                },
                child: Container(
                  height: 56,
                  decoration: BoxDecoration(
                    gradient: AppColors.buttonGradient,
                    borderRadius: BorderRadius.circular(18),
                    boxShadow: [
                      BoxShadow(
                        color: AppColors.hotPink.withValues(alpha: 0.3),
                        blurRadius: 16,
                        offset: const Offset(0, 6),
                      ),
                    ],
                  ),
                  child: Center(
                    child: Text(
                      'Iniciar sesión',
                      style: GoogleFonts.dmSans(
                        fontSize: 16,
                        fontWeight: FontWeight.w800,
                        color: Colors.white,
                      ),
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 12),
              // Secondary — Regístrate (link to register tab on WelcomeOnboardingScreen)
              GestureDetector(
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                        builder: (_) => const WelcomeOnboardingScreen()),
                  );
                },
                child: Container(
                  height: 48,
                  decoration: BoxDecoration(
                    color: t.isDark
                        ? Colors.white.withValues(alpha: 0.06)
                        : const Color(0xFFF3F4F6),
                    borderRadius: BorderRadius.circular(18),
                  ),
                  child: Center(
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(
                          '¿No tienes cuenta?',
                          style: GoogleFonts.dmSans(
                            fontSize: 14,
                            fontWeight: FontWeight.w500,
                            color: t.textMuted,
                          ),
                        ),
                        const SizedBox(width: 4),
                        Text(
                          'Regístrate',
                          style: GoogleFonts.dmSans(
                            fontSize: 14,
                            fontWeight: FontWeight.w700,
                            color: AppColors.hotPink,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 8),
              // Close button
              TextButton(
                onPressed: () => Navigator.pop(context),
                child: Text(
                  'Cerrar',
                  style: GoogleFonts.dmSans(
                    fontSize: 13,
                    fontWeight: FontWeight.w500,
                    color: t.textDim,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}