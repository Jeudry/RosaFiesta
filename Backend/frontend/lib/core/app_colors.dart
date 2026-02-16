import 'package:flutter/material.dart';

class AppColors {
  static const Color primaryPink = Color(0xFFFFC0CB);
  static const Color secondaryPurple = Color(0xFF800080);
  static const Color accentGreen = Color(0xFF4CAF50);
  static const Color actionYellow = Color(0xFFFFD700);

  static const Color background = Color(0xFFF8F9FA);
  static const Color surface = Colors.white;
  static const Color error = Color(0xFFD32F2F);

  // Gradient examples
  static const LinearGradient primaryGradient = LinearGradient(
    colors: [primaryPink, secondaryPurple],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );
}
