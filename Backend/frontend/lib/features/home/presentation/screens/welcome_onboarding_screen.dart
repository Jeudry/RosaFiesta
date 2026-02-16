import 'package:flutter/material.dart';
import 'package:frontend/core/app_theme.dart';
import 'package:frontend/features/auth/presentation/screens/login_screen.dart';
import 'package:frontend/features/auth/presentation/screens/register_screen.dart';

/// Welcome/Onboarding screen matching the HTML design
/// Features animated background with confetti pattern and brand colors
class WelcomeOnboardingScreen extends StatelessWidget {
  const WelcomeOnboardingScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Stack(
        children: [
          // Animated background with colorful blobs
          _buildAnimatedBackground(),
          
          // Main content
          SafeArea(
            child: SingleChildScrollView(
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 24),
                child: Column(
                  children: [
                    // Top menu button
                    Align(
                      alignment: Alignment.topRight,
                      child: IconButton(
                        onPressed: () {},
                        icon: const Icon(Icons.more_horiz, size: 28),
                        style: IconButton.styleFrom(
                          backgroundColor: Colors.white.withOpacity(0.2),
                        ),
                      ),
                    ),
                    
                    const SizedBox(height: 40),
                    
                    // Logo and brand section
                    _buildLogoSection(),
                    
                    const SizedBox(height: 48),
                    
                    // Welcome text
                    _buildWelcomeText(),
                    
                    const SizedBox(height: 60),
                    
                    // Action buttons
                    _buildActionButtons(context),
                    
                    const SizedBox(height: 24),
                    
                    // iOS home indicator
                    Container(
                      width: 128,
                      height: 6,
                      decoration: BoxDecoration(
                        color: Colors.grey.shade300,
                        borderRadius: BorderRadius.circular(3),
                      ),
                    ),
                    
                    const SizedBox(height: 16),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAnimatedBackground() {
    return Stack(
      children: [
        // Lime blob
        Positioned(
          top: -80,
          left: -80,
          child: Container(
            width: 256,
            height: 256,
            decoration: BoxDecoration(
              color: AppColors.lime.withOpacity(0.2),
              shape: BoxShape.circle,
            ),
          ),
        ),
        
        // Purple blob
        Positioned(
          top: 200,
          right: -80,
          child: Container(
            width: 320,
            height: 320,
            decoration: BoxDecoration(
              color: AppColors.purple.withOpacity(0.15),
              shape: BoxShape.circle,
            ),
          ),
        ),
        
        // Teal blob
        Positioned(
          bottom: 200,
          left: -40,
          child: Container(
            width: 240,
            height: 240,
            decoration: BoxDecoration(
              color: AppColors.teal.withOpacity(0.2),
              shape: BoxShape.circle,
            ),
          ),
        ),
        
        // Yellow blob
        Positioned(
          bottom: 0,
          right: 0,
          child: Container(
            width: 288,
            height: 288,
            decoration: BoxDecoration(
              color: AppColors.yellow.withOpacity(0.1),
              shape: BoxShape.circle,
            ),
          ),
        ),
        
        // Confetti pattern overlay
        Positioned.fill(
          child: CustomPaint(
            painter: _ConfettiPainter(),
          ),
        ),
      ],
    );
  }

  Widget _buildLogoSection() {
    return SizedBox(
      width: 288,
      height: 288,
      child: Stack(
        alignment: Alignment.center,
        children: [
          // Main lime circle
          Container(
            width: 224,
            height: 224,
            decoration: const BoxDecoration(
              color: AppColors.lime,
              shape: BoxShape.circle,
            ),
          ),
          
          // RF text
          const Text(
            'RF',
            style: TextStyle(
              fontSize: 90,
              fontWeight: FontWeight.w900,
              color: Colors.white,
              fontStyle: FontStyle.italic,
              letterSpacing: -4,
            ),
          ),
          
          // Decorative stars
          Positioned(
            top: 32,
            left: 32,
            child: Icon(
              Icons.star,
              color: AppColors.yellow,
              size: 32,
            ),
          ),
          
          Positioned(
            bottom: 48,
            right: 24,
            child: Icon(
              Icons.auto_awesome,
              color: AppColors.pink,
              size: 36,
            ),
          ),
          
          // Decorative lines
          Positioned(
            top: 16,
            right: 64,
            child: Transform.rotate(
              angle: 0.785,
              child: Container(
                width: 12,
                height: 32,
                decoration: BoxDecoration(
                  color: AppColors.teal,
                  borderRadius: BorderRadius.circular(6),
                ),
              ),
            ),
          ),
          
          Positioned(
            bottom: 16,
            left: 80,
            child: Transform.rotate(
              angle: -0.785,
              child: Container(
                width: 12,
                height: 32,
                decoration: BoxDecoration(
                  color: AppColors.purple,
                  borderRadius: BorderRadius.circular(6),
                ),
              ),
            ),
          ),
          
          // Small dots
          Positioned(
            top: 100,
            left: -16,
            child: Container(
              width: 24,
              height: 8,
              decoration: BoxDecoration(
                color: AppColors.pink,
                borderRadius: BorderRadius.circular(4),
              ),
            ),
          ),
          
          Positioned(
            top: 72,
            right: 0,
            child: Container(
              width: 16,
              height: 16,
              decoration: const BoxDecoration(
                color: AppColors.purple,
                shape: BoxShape.circle,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildWelcomeText() {
    return Column(
      children: [
        RichText(
          textAlign: TextAlign.center,
          text: const TextSpan(
            style: TextStyle(
              fontSize: 36,
              fontWeight: FontWeight.w800,
              height: 1.2,
              letterSpacing: -0.5,
            ),
            children: [
              TextSpan(
                text: 'Welcome to\n',
                style: TextStyle(color: Colors.black87),
              ),
              TextSpan(
                text: 'Rosa ',
                style: TextStyle(color: AppColors.purple),
              ),
              TextSpan(
                text: 'Fiesta',
                style: TextStyle(color: AppColors.teal),
              ),
            ],
          ),
        ),
        const SizedBox(height: 16),
        Text(
          'Turning your celebrations into vibrant masterpieces.',
          textAlign: TextAlign.center,
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.w500,
            color: Colors.black.withOpacity(0.7),
            height: 1.5,
          ),
        ),
      ],
    );
  }

  Widget _buildActionButtons(BuildContext context) {
    return Column(
      children: [
        SizedBox(
          width: double.infinity,
          height: 64,
          child: ElevatedButton(
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const RegisterScreen()),
              );
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.purple,
              shadowColor: AppColors.purple.withOpacity(0.3),
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: const [
                Text(
                  'Get Started',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                    letterSpacing: 0.5,
                  ),
                ),
                SizedBox(width: 8),
                Icon(Icons.arrow_forward, size: 24),
              ],
            ),
          ),
        ),
        const SizedBox(height: 12),
        TextButton(
          onPressed: () {
            Navigator.push(
              context,
              MaterialPageRoute(builder: (_) => const LoginScreen()),
            );
          },
          style: TextButton.styleFrom(
            minimumSize: const Size(double.infinity, 48),
          ),
          child: const Text(
            'Log In',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w600,
              color: Colors.black87,
            ),
          ),
        ),
      ],
    );
  }
}

/// Custom painter for confetti pattern
class _ConfettiPainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()..style = PaintingStyle.fill;
    
    // Draw confetti dots in a pattern
    for (double x = 0; x < size.width; x += 40) {
      for (double y = 0; y < size.height; y += 40) {
        // Lime dots
        paint.color = AppColors.lime.withOpacity(0.15);
        canvas.drawCircle(Offset(x, y), 1.5, paint);
        
        // Purple dots (offset)
        paint.color = AppColors.purple.withOpacity(0.15);
        canvas.drawCircle(Offset(x + 20, y + 20), 1.5, paint);
        
        // Teal dots (offset)
        paint.color = AppColors.teal.withOpacity(0.15);
        canvas.drawCircle(Offset(x + 10, y + 30), 1.5, paint);
      }
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}
