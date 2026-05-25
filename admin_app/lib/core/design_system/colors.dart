import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

/// Admin app uses slightly darker colors than the customer app
class AppColors {
  // Primary (darker than customer app's #FF3CAC)
  static const Color primary = Color(0xFFD42A8F);
  static const Color primaryLight = Color(0xFFE85BA8);
  static const Color primaryDark = Color(0xFFB01878);

  // Accent (deeper than customer app's #FF6B6B)
  static const Color accent = Color(0xFFE05555);
  static const Color accentLight = Color(0xFFFF7B7B);

  // Other colors (shared)
  static const Color hotPink = Color(0xFFFF3CAC);
  static const Color coral = Color(0xFFFF6B6B);
  static const Color amber = Color(0xFFFFB800);
  static const Color teal = Color(0xFF00D4AA);
  static const Color violet = Color(0xFF8B5CF6);
  static const Color sky = Color(0xFF4FC3F7);

  // Neutrals
  static const Color white = Color(0xFFFFFFFF);
  static const Color black = Color(0xFF000000);
  static const Color surface = Color(0xFFF8F9FA);
  static const Color surfaceDark = Color(0xFF1A1A2E);
  static const Color cardDark = Color(0xFF252542);
  static const Color borderDark = Color(0xFF3A3A5C);

  // Status
  static const Color success = Color(0xFF00D4AA);
  static const Color error = Color(0xFFE05555);
  static const Color warning = Color(0xFFFFB800);
  static const Color info = Color(0xFF4FC3F7);

  // Text
  static const Color textPrimary = Color(0xFF1A1A2E);
  static const Color textSecondary = Color(0xFF6B7280);
  static const Color textMuted = Color(0xFF9CA3AF);
  static const Color textOnDark = Color(0xFFF8F9FA);
}

/// Admin theme - darker surfaces, more defined borders
class AdminTheme {
  static ThemeData get dark {
    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,
      scaffoldBackgroundColor: AppColors.surfaceDark,
      colorScheme: const ColorScheme.dark(
        primary: AppColors.primary,
        secondary: AppColors.accent,
        surface: AppColors.surfaceDark,
        error: AppColors.error,
      ),
      cardTheme: CardThemeData(
        color: AppColors.cardDark.withOpacity(0.75), // Glassmorphic translucent cards!
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
          side: BorderSide(color: AppColors.borderDark.withOpacity(0.5), width: 1),
        ),
        elevation: 0,
      ),
      appBarTheme: AppBarTheme(
        backgroundColor: Colors.transparent,
        elevation: 0,
        centerTitle: false,
        titleTextStyle: GoogleFonts.outfit(
          fontSize: 20,
          fontWeight: FontWeight.w600,
          color: AppColors.textOnDark,
        ),
        iconTheme: const IconThemeData(color: AppColors.textOnDark),
      ),
      drawerTheme: const DrawerThemeData(
        backgroundColor: AppColors.surfaceDark,
      ),
      listTileTheme: const ListTileThemeData(
        iconColor: AppColors.textMuted,
        textColor: AppColors.textOnDark,
      ),
      dividerTheme: const DividerThemeData(
        color: AppColors.borderDark,
        thickness: 1,
      ),
      textTheme: _buildTextTheme(AppColors.textOnDark),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: AppColors.cardDark.withOpacity(0.6),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(color: AppColors.borderDark.withOpacity(0.5)),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(color: AppColors.borderDark.withOpacity(0.5)),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: AppColors.primary, width: 2),
        ),
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.primary,
          foregroundColor: AppColors.white,
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          foregroundColor: AppColors.primary,
          side: const BorderSide(color: AppColors.primary),
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        ),
      ),
      chipTheme: ChipThemeData(
        backgroundColor: AppColors.cardDark.withOpacity(0.6),
        labelStyle: GoogleFonts.dmSans(color: AppColors.textOnDark),
        side: BorderSide(color: AppColors.borderDark.withOpacity(0.5)),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      ),
    );
  }

  static ThemeData get light {
    const textPurple = Color(0xFF2C1A4D);
    const borderPink = Color(0x2BD42A8F);
    
    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.light,
      scaffoldBackgroundColor: const Color(0xFFFFF0F5),
      colorScheme: const ColorScheme.light(
        primary: AppColors.primary,
        secondary: AppColors.accent,
        surface: Color(0xFFFFF5F8),
        error: AppColors.error,
      ),
      cardTheme: CardThemeData(
        color: AppColors.white.withOpacity(0.68), // Ultra-premium glassmorphism!
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
          side: BorderSide(color: AppColors.primary.withOpacity(0.16), width: 1.5), // Custom pinkish border
        ),
        elevation: 0,
      ),
      appBarTheme: AppBarTheme(
        backgroundColor: Colors.transparent,
        elevation: 0,
        centerTitle: false,
        titleTextStyle: GoogleFonts.outfit(
          fontSize: 20,
          fontWeight: FontWeight.w600,
          color: textPurple,
        ),
        iconTheme: const IconThemeData(color: textPurple),
      ),
      listTileTheme: const ListTileThemeData(
        iconColor: AppColors.primary,
        textColor: textPurple,
      ),
      dividerTheme: DividerThemeData(
        color: AppColors.primary.withOpacity(0.12),
        thickness: 1,
      ),
      textTheme: _buildTextTheme(textPurple),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: AppColors.white.withOpacity(0.55),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(color: AppColors.primary.withOpacity(0.16), width: 1),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(color: AppColors.primary.withOpacity(0.16), width: 1),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: AppColors.primary, width: 2),
        ),
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.primary,
          foregroundColor: AppColors.white,
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        ),
      ),
    );
  }

  static TextTheme _buildTextTheme(Color color) {
    return TextTheme(
      displayLarge: GoogleFonts.outfit(fontSize: 42, fontWeight: FontWeight.w800, color: color),
      displayMedium: GoogleFonts.outfit(fontSize: 32, fontWeight: FontWeight.w700, color: color),
      displaySmall: GoogleFonts.outfit(fontSize: 24, fontWeight: FontWeight.w600, color: color),
      headlineMedium: GoogleFonts.outfit(fontSize: 20, fontWeight: FontWeight.w600, color: color),
      titleLarge: GoogleFonts.outfit(fontSize: 18, fontWeight: FontWeight.w600, color: color),
      titleMedium: GoogleFonts.outfit(fontSize: 16, fontWeight: FontWeight.w500, color: color),
      bodyLarge: GoogleFonts.dmSans(fontSize: 16, color: color),
      bodyMedium: GoogleFonts.dmSans(fontSize: 14, color: color),
      bodySmall: GoogleFonts.dmSans(fontSize: 12, color: color),
      labelLarge: GoogleFonts.dmSans(fontSize: 14, fontWeight: FontWeight.w500, color: color),
    );
  }
}
