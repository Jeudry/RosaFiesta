import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'app_colors.dart';

export 'app_colors.dart';

/// Rosa Fiesta Design System — Theme
/// Fonts: Outfit (headings/display) + DM Sans (body/UI)
class AppTheme {
  static ThemeData get lightTheme {
    return ThemeData(
      useMaterial3: true,
      scaffoldBackgroundColor: AppColors.lightBase,
      colorScheme: ColorScheme.fromSeed(
        seedColor: AppColors.hotPink,
        primary: AppColors.hotPink,
        secondary: AppColors.violet,
        tertiary: AppColors.teal,
        surface: AppColors.lightSurface,
        brightness: Brightness.light,
      ),
      textTheme: GoogleFonts.dmSansTextTheme().copyWith(
        displayLarge: GoogleFonts.outfit(
          fontWeight: FontWeight.w800,
          color: AppColors.lightTextPrimary,
          letterSpacing: -1,
        ),
        displayMedium: GoogleFonts.outfit(
          fontWeight: FontWeight.w700,
          color: AppColors.lightTextPrimary,
          letterSpacing: -0.5,
        ),
        titleLarge: GoogleFonts.outfit(
          fontWeight: FontWeight.w700,
          color: AppColors.lightTextPrimary,
        ),
        bodyLarge: GoogleFonts.dmSans(
          color: AppColors.lightTextPrimary,
        ),
        bodyMedium: GoogleFonts.dmSans(
          color: AppColors.lightTextMuted,
        ),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.hotPink,
          foregroundColor: Colors.white,
          minimumSize: const Size(double.infinity, 54),
          shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(16)),
          elevation: 0,
          textStyle: GoogleFonts.outfit(
            fontSize: 15,
            fontWeight: FontWeight.w700,
            letterSpacing: 0.5,
          ),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: AppColors.lightSurface,
        contentPadding:
            const EdgeInsets.symmetric(horizontal: 20, vertical: 18),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: const BorderSide(color: Color(0xFFE8E8F0)),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: const BorderSide(color: Color(0xFFE8E8F0)),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide:
              const BorderSide(color: AppColors.hotPink, width: 1.5),
        ),
        labelStyle: GoogleFonts.dmSans(
            color: AppColors.lightTextDim, fontSize: 14),
      ),
      appBarTheme: const AppBarTheme(
        backgroundColor: Colors.transparent,
        elevation: 0,
        centerTitle: true,
        scrolledUnderElevation: 0,
        iconTheme: IconThemeData(color: AppColors.lightTextPrimary),
      ),
    );
  }
}

/// Legacy alias — kept for backward compatibility
class AppDecorations {
  static BoxDecoration glass(
      {double opacity = 0.1, double blur = 10.0}) {
    return BoxDecoration(
      color: Colors.white.withOpacity(opacity),
      borderRadius: BorderRadius.circular(24),
      border: Border.all(color: Colors.white.withOpacity(0.2)),
    );
  }

  static List<BoxShadow> softShadow = [
    BoxShadow(
      color: AppColors.hotPink.withOpacity(0.08),
      blurRadius: 24,
      offset: const Offset(0, 8),
    ),
  ];
}
