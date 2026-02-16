import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

/// Rosa Fiesta brand colors extracted from logo
class AppColors {
  // Primary brand colors from logo
  static const lime = Color(0xFFBCD72E);        // Logo Lime Green
  static const purple = Color(0xFF6D2D91);      // Logo Purple
  static const pink = Color(0xFFD10A7C);        // Logo Pink
  static const teal = Color(0xFF00A99D);        // Logo Teal
  static const yellow = Color(0xFFFDB913);      // Logo Yellow
  
  // Background colors
  static const backgroundLight = Color(0xFFF9FAF5);  // Slightly lime tinted
  static const backgroundDark = Color(0xFF1A1A1A);
  
  // Gradient combinations
  static const backgroundGradient = [
    Color(0xFF6D2D91), // Purple
    Color(0xFFD10A7C), // Pink
  ];
}

class AppTheme {
  static ThemeData get lightTheme {
    return ThemeData(
      useMaterial3: true,
      scaffoldBackgroundColor: AppColors.backgroundLight,
      colorScheme: ColorScheme.fromSeed(
        seedColor: AppColors.purple,
        primary: AppColors.purple,
        secondary: AppColors.teal,
        tertiary: AppColors.lime,
        surface: Colors.white,
        error: Colors.redAccent,
        brightness: Brightness.light,
      ),
      // Use Plus Jakarta Sans to match HTML design
      textTheme: GoogleFonts.plusJakartaSansTextTheme(),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.purple,
          foregroundColor: Colors.white,
          textStyle: const TextStyle(
            fontWeight: FontWeight.bold,
            fontSize: 16,
            letterSpacing: 0.5,
          ),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(999), // Full rounded
          ),
          elevation: 8,
          shadowColor: AppColors.purple.withOpacity(0.3),
          padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 16),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: const Color(0xFFF1F4E8), // Light lime tint
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(999), // Full rounded
          borderSide: BorderSide.none,
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(999),
          borderSide: BorderSide.none,
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(999),
          borderSide: const BorderSide(color: AppColors.lime, width: 2),
        ),
        prefixIconColor: AppColors.teal,
        contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
      ),
      appBarTheme: AppBarTheme(
        backgroundColor: Colors.white.withOpacity(0.9),
        elevation: 0,
        centerTitle: true,
        titleTextStyle: GoogleFonts.plusJakartaSans(
          color: AppColors.purple,
          fontSize: 20,
          fontWeight: FontWeight.w800,
          letterSpacing: -0.3,
        ),
        iconTheme: const IconThemeData(color: AppColors.teal),
      ),
    );
  }
}
