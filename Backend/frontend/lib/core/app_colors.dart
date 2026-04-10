import 'package:flutter/material.dart';

/// Rosa Fiesta Design System — Color Palette
/// These are the authoritative color tokens for the entire app.
/// All screens MUST use these constants — do NOT define local color constants.
class AppColors {
  // ── Accent spectrum (primary brand colors) ───────────────────────────
  static const hotPink = Color(0xFFFF3CAC); // Primary brand pink — CTAs, titles
  static const coral   = Color(0xFFFF6B6B); // Secondary coral — errors, accents
  static const amber   = Color(0xFFFFB800); // Gold/amber — highlights
  static const teal    = Color(0xFF00D4AA); // Fresh teal — success, accents
  static const violet  = Color(0xFF8B5CF6); // Deep violet — orbs, backgrounds
  static const sky     = Color(0xFF4FC3F7); // Sky blue — light accents

  // ── Dark theme surfaces ──────────────────────────────────────────────
  static const darkBase    = Color(0xFF0A0A14);
  static const darkSurface = Color(0xFF12121E);
  static const darkCard    = Color(0xFF1A1A2E);

  // ── Light theme surfaces ─────────────────────────────────────────────
  static const lightBase    = Color(0xFFFAFAFC);
  static const lightSurface = Colors.white;
  static const lightCard    = Color(0xFFF4F4F8);

  // ── Typography ───────────────────────────────────────────────────────
  static const darkTextPrimary  = Color(0xFFF8F8FF);
  static const darkTextMuted    = Color(0xFF9B9BC0);
  static const darkTextDim      = Color(0xFF6B6B8D);
  static const lightTextPrimary = Color(0xFF0A0A1E);
  static const lightTextMuted   = Color(0xFF5A5A80);
  static const lightTextDim     = Color(0xFF9090B0);

  // ── Borders ──────────────────────────────────────────────────────────
  static const darkBorder  = Color(0x1AFFFFFF);
  static const lightBorder = Color(0x1A000000);

  // ── Gradients ────────────────────────────────────────────────────────
  /// Used for gradient titles and ShaderMask
  static const titleGradient = LinearGradient(
    colors: [hotPink, amber, teal],
    begin: Alignment.centerLeft,
    end: Alignment.centerRight,
  );

  /// Used for primary action buttons
  static const buttonGradient = LinearGradient(
    colors: [hotPink, violet],
    begin: Alignment.centerLeft,
    end: Alignment.centerRight,
  );

  // ── Legacy aliases — kept for backward compatibility only ─────────────
  // Do NOT use these for new code. Use the tokens above instead.
  static const primary        = Color(0xFF6D2D91);
  static const secondary      = Color(0xFFD10A7C);
  static const accent         = Color(0xFF00A99D);
  static const highlight      = Color(0xFFBCD72E);
  static const surface        = Colors.white;
  static const background     = Color(0xFFFBFBFC);
  static const cardBackground = Colors.white;
  static const textPrimary    = Color(0xFF1A1A1A);
  static const textSecondary  = Color(0xFF666666);
  static const purple         = primary;
  static const pink           = secondary;
  static const lime           = highlight;
  static const yellow         = Color(0xFFFFD700);
  static const orange         = Colors.orange;
  static const backgroundLight = background;
  static const primaryGradient = LinearGradient(
    colors: [hotPink, amber, teal],
    begin: Alignment.centerLeft,
    end: Alignment.centerRight,
  );
}
