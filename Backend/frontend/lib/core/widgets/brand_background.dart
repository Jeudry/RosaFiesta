import 'dart:math';
import 'package:flutter/material.dart';
import '../app_theme.dart';

class BrandBackground extends StatefulWidget {
  final Widget child;
  const BrandBackground({super.key, required this.child});

  @override
  State<BrandBackground> createState() => _BrandBackgroundState();
}

class _BrandBackgroundState extends State<BrandBackground> with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  final List<Star> _stars = List.generate(15, (index) => Star());

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 20),
    )..repeat();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: [
        // Mesh Gradient Background
        AnimatedBuilder(
          animation: _controller,
          builder: (context, _) {
            return Container(
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: [
                    AppColors.purple,
                    AppColors.pink,
                    AppColors.purple.withOpacity(0.8),
                  ],
                  stops: [
                    0.0,
                    0.5 + 0.1 * sin(_controller.value * 2 * pi),
                    1.0,
                  ],
                ),
              ),
            );
          },
        ),
        // Flying Stars
        ..._stars.map((star) => MovingStar(star: star, animation: _controller)),
        // Content
        widget.child,
      ],
    );
  }
}

class Star {
  final double size = Random().nextDouble() * 30 + 10;
  final double speed = Random().nextDouble() * 0.2 + 0.1;
  final Color color = [AppColors.yellow, AppColors.lime, AppColors.teal, AppColors.pink][Random().nextInt(4)];
  double x = Random().nextDouble();
  double y = Random().nextDouble();
  double angle = Random().nextDouble() * pi * 2;
}

class MovingStar extends StatelessWidget {
  final Star star;
  final Animation<double> animation;
  const MovingStar({super.key, required this.star, required this.animation});

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: animation,
      builder: (context, _) {
        final offset = animation.value * 2 * pi * star.speed;
        return Positioned(
          left: (star.x * MediaQuery.of(context).size.width + 50 * sin(offset)) % MediaQuery.of(context).size.width,
          top: (star.y * MediaQuery.of(context).size.height + 50 * cos(offset)) % MediaQuery.of(context).size.height,
          child: Transform.rotate(
            angle: offset + star.angle,
            child: Icon(
              Icons.star,
              color: star.color.withOpacity(0.3),
              size: star.size,
            ),
          ),
        );
      },
    );
  }
}
