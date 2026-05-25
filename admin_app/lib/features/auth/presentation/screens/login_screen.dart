import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import '../../../../core/design_system.dart';
import '../providers/auth_provider.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> with TickerProviderStateMixin {
  final _emailController = TextEditingController(text: 'admin@rosafiesta.com');
  final _passwordController = TextEditingController(text: 'Password123!');
  bool _obscure = true;

  late AnimationController _floatController;
  late AnimationController _decoController;
  late AnimationController _pulseController;

  @override
  void initState() {
    super.initState();
    _floatController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 20),
    )..repeat();
    _decoController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 40),
    )..repeat();
    _pulseController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 5),
    )..repeat(reverse: true);
  }

  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    _floatController.dispose();
    _decoController.dispose();
    _pulseController.dispose();
    super.dispose();
  }

  Future<void> _login() async {
    final auth = context.read<AuthProvider>();
    auth.clearError();
    final success = await auth.login(_emailController.text.trim(), _passwordController.text);
    if (success && mounted) {
      Navigator.of(context).pushReplacementNamed('/dashboard');
    }
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthProvider>();
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final size = MediaQuery.of(context).size;

    // Colores autoritativos del tema "Candy Pop" (de la app del cliente)
    const hotPink = Color(0xFFFF3CAC);
    const coral = Color(0xFFFF6B6B);
    const amber = Color(0xFFFFB800);
    const teal = Color(0xFF00D4AA);
    const violet = Color(0xFF8B5CF6);

    return Scaffold(
      body: Stack(
        children: [
          // 1. Fondo base Candy Pop
          Container(
            color: isDark ? const Color(0xFF0A0A14) : const Color(0xFFFAFAFC),
          ),

          // 2. Orbes de Gradiente Animados Candy Pop
          RfGradientOrbs(
            controller: _floatController,
            color1: hotPink,
            color2: violet,
            isDark: isDark,
          ),

          // Filtro de desenfoque masivo para fundir los orbes de caramelo
          Positioned.fill(
            child: BackdropFilter(
              filter: ImageFilter.blur(sigmaX: 85, sigmaY: 85),
              child: Container(color: Colors.transparent),
            ),
          ),

          // 3. Patrón sutil de cuadrícula de diseño Candy Pop
          Positioned.fill(
            child: CustomPaint(
              painter: _RfGridPainter(
                color: isDark ? Colors.white.withOpacity(0.015) : Colors.black.withOpacity(0.015),
              ),
            ),
          ),

          // 4. Capa de Decoración Flotante y Animada (Pétalos, Anillos, Destellos)
          // Atenuado para máxima legibilidad de los textos frontales
          RfDecoLayer(
            floatController: _floatController,
            decoController: _decoController,
            pulseController: _pulseController,
            baseOpacity: isDark ? 0.6 : 0.35,
          ),

          // 4. Contenido principal
          SafeArea(
            child: Center(
              child: SingleChildScrollView(
                padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
                child: ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 400),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      // Encabezado Candy Pop
                      Column(
                        children: [
                          // Contenedor de Logo Oficial Circular
                          Container(
                            width: 110,
                            height: 110,
                            decoration: BoxDecoration(
                              shape: BoxShape.circle,
                              color: Colors.white,
                              boxShadow: [
                                BoxShadow(
                                  color: coral.withOpacity(0.2),
                                  blurRadius: 30,
                                  spreadRadius: 5,
                                ),
                              ],
                            ),
                            child: ClipOval(
                              child: Image.asset(
                                'assets/images/logo_rosafiesta.png',
                                width: 110,
                                height: 110,
                                fit: BoxFit.cover,
                                errorBuilder: (_, __, ___) => ShaderMask(
                                  shaderCallback: (b) => const LinearGradient(
                                    colors: [coral, amber],
                                  ).createShader(b),
                                  child: const Icon(
                                    Icons.local_florist,
                                    size: 55,
                                    color: Colors.white,
                                  ),
                                ),
                              ),
                            ),
                          ),
                          const SizedBox(height: 24),
                          // Texto de Título con Gradiente Candy Pop (Pink-Amber-Teal)
                          ShaderMask(
                            shaderCallback: (bounds) => const LinearGradient(
                              colors: [hotPink, amber, teal],
                              begin: Alignment.centerLeft,
                              end: Alignment.centerRight,
                            ).createShader(bounds),
                            child: Text(
                              'RosaFiesta',
                              style: GoogleFonts.outfit(
                                fontSize: 42,
                                fontWeight: FontWeight.w900,
                                color: Colors.white,
                                letterSpacing: -1.5,
                              ),
                            ),
                          ),
                          const SizedBox(height: 6),
                          Text(
                            'PORTAL DE ADMINISTRACIÓN',
                            style: GoogleFonts.outfit(
                              fontSize: 11,
                              fontWeight: FontWeight.w800,
                              color: isDark ? const Color(0xFF9B9BC0) : const Color(0xFF5A5A80),
                              letterSpacing: 4.5,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 36),

                      // Tarjeta Glassmorphic Candy Pop
                      ClipRRect(
                        borderRadius: BorderRadius.circular(30),
                        child: BackdropFilter(
                          filter: ImageFilter.blur(sigmaX: 30, sigmaY: 30),
                          child: Container(
                            decoration: BoxDecoration(
                              color: isDark
                                  ? Colors.white.withOpacity(0.04)
                                  : Colors.white.withOpacity(0.68), // Frosted glass premium igual que cliente
                              borderRadius: BorderRadius.circular(30),
                              border: Border.all(
                                color: isDark
                                    ? Colors.white.withOpacity(0.08)
                                    : hotPink.withOpacity(0.12), // Rosa suave sutil y elegante
                                width: 1.5,
                              ),
                              boxShadow: [
                                BoxShadow(
                                  color: hotPink.withOpacity(isDark ? 0.2 : 0.05),
                                  blurRadius: 40,
                                  offset: const Offset(0, 20),
                                ),
                              ],
                            ),
                            child: Padding(
                              padding: const EdgeInsets.all(30),
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.stretch,
                                children: [
                                  Text(
                                    'Iniciar Sesión',
                                    style: GoogleFonts.outfit(
                                      fontSize: 24,
                                      fontWeight: FontWeight.w800,
                                      color: isDark ? Colors.white : const Color(0xFF0A0A1E),
                                    ),
                                    textAlign: TextAlign.center,
                                  ),
                                  const SizedBox(height: 28),

                                  // Campo de Email
                                  _buildCustomTextField(
                                    label: 'Correo Electrónico',
                                    controller: _emailController,
                                    hint: 'correo@ejemplo.com', // Placeholder indicativo
                                    icon: Icons.mail_outline_rounded,
                                    keyboardType: TextInputType.emailAddress,
                                    isDark: isDark,
                                    activeColor: hotPink,
                                  ),
                                  const SizedBox(height: 20),

                                  // Campo de Contraseña
                                  _buildCustomTextField(
                                    label: 'Contraseña',
                                    controller: _passwordController,
                                    hint: 'Mínimo 8 caracteres', // Placeholder indicativo
                                    icon: Icons.lock_outline_rounded,
                                    obscure: _obscure,
                                    isDark: isDark,
                                    activeColor: hotPink,
                                    suffix: IconButton(
                                      icon: Icon(
                                        _obscure
                                            ? Icons.visibility_off_outlined
                                            : Icons.visibility_outlined,
                                        size: 20,
                                        color: isDark ? Colors.white54 : Colors.black45,
                                      ),
                                      onPressed: () => setState(() => _obscure = !_obscure),
                                    ),
                                  ),

                                  // Manejo de Error
                                  if (auth.error != null) ...[
                                    const SizedBox(height: 20),
                                    Container(
                                      padding: const EdgeInsets.all(12),
                                      decoration: BoxDecoration(
                                        color: coral.withOpacity(0.08),
                                        borderRadius: BorderRadius.circular(14),
                                        border: Border.all(
                                          color: coral.withOpacity(0.4),
                                          width: 1,
                                        ),
                                      ),
                                      child: Row(
                                        children: [
                                          const Icon(Icons.error_outline_rounded,
                                              color: coral, size: 20),
                                          const SizedBox(width: 10),
                                          Expanded(
                                            child: Text(
                                              auth.error!,
                                              style: GoogleFonts.dmSans(
                                                fontSize: 13,
                                                color: coral,
                                                fontWeight: FontWeight.w600,
                                              ),
                                            ),
                                          ),
                                        ],
                                      ),
                                    ),
                                  ],
                                  const SizedBox(height: 32),

                                  // Botón Candy Pop con Gradiente (Hot Pink a Violet) - Píldora premium
                                  GestureDetector(
                                    onTap: auth.loading ? null : _login,
                                    child: AnimatedContainer(
                                      duration: const Duration(milliseconds: 200),
                                      padding: const EdgeInsets.symmetric(vertical: 16),
                                      decoration: BoxDecoration(
                                        gradient: const LinearGradient(
                                          colors: [hotPink, violet],
                                          begin: Alignment.centerLeft,
                                          end: Alignment.centerRight,
                                        ),
                                        borderRadius: BorderRadius.circular(24), // Hiper redondeado premium
                                        boxShadow: [
                                          BoxShadow(
                                            color: hotPink.withOpacity(isDark ? 0.35 : 0.25),
                                            blurRadius: 20,
                                            offset: const Offset(0, 8),
                                          ),
                                        ],
                                      ),
                                      child: Center(
                                        child: auth.loading
                                            ? const SizedBox(
                                                width: 22,
                                                height: 22,
                                                child: CircularProgressIndicator(
                                                  strokeWidth: 2.5,
                                                  valueColor:
                                                      AlwaysStoppedAnimation<Color>(Colors.white),
                                                ),
                                              )
                                            : Text(
                                                'Entrar al Portal',
                                                style: GoogleFonts.outfit(
                                                  fontSize: 16,
                                                  fontWeight: FontWeight.w700,
                                                  color: Colors.white,
                                                  letterSpacing: 0.5,
                                                ),
                                              ),
                                      ),
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCustomTextField({
    required String label,
    required TextEditingController controller,
    required String hint,
    required IconData icon,
    bool obscure = false,
    TextInputType? keyboardType,
    Widget? suffix,
    required bool isDark,
    required Color activeColor,
  }) {
    final enabledBorderColor = isDark
        ? Colors.white.withOpacity(0.08)
        : Colors.transparent; // Borde invisible para diseño súper limpio
    final fillColor = isDark
        ? Colors.white.withOpacity(0.04)
        : const Color(0xFFF5F5FA); // Fondo suave igual a app cliente

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.only(left: 6, bottom: 6),
          child: Text(
            label,
            style: GoogleFonts.outfit( // Outfit para consistencia de marca
              fontSize: 13,
              fontWeight: FontWeight.w600,
              color: isDark ? const Color(0xFF9B9BC0) : const Color(0xFF5A5A80),
            ),
          ),
        ),
        Container(
          decoration: BoxDecoration(
            color: fillColor,
            borderRadius: BorderRadius.circular(20), // Hiper redondeado premium
            border: Border.all(
              color: enabledBorderColor,
              width: 1,
            ),
          ),
          child: TextFormField(
            controller: controller,
            obscureText: obscure,
            keyboardType: keyboardType,
            style: GoogleFonts.outfit(
              color: isDark ? Colors.white : const Color(0xFF0A0A1E),
              fontSize: 15,
            ),
            decoration: InputDecoration(
              hintText: hint,
              hintStyle: GoogleFonts.outfit(
                color: isDark ? Colors.white30 : Colors.black26,
                fontSize: 15,
              ),
              prefixIcon: Icon(icon, color: isDark ? Colors.white30 : Colors.black26, size: 20),
              suffixIcon: suffix,
              border: InputBorder.none,
              enabledBorder: InputBorder.none,
              focusedBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(20),
                borderSide: BorderSide(color: activeColor, width: 1.5),
              ),
              contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
            ),
          ),
        ),
      ],
    );
  }
}

// Dibujador de cuadrícula sutil alineado con el tema Candy Pop
class _RfGridPainter extends CustomPainter {
  final Color color;
  const _RfGridPainter({required this.color});

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..strokeWidth = 0.5;
    const step = 48.0;
    for (double x = 0; x < size.width; x += step) {
      canvas.drawLine(Offset(x, 0), Offset(x, size.height), paint);
    }
    for (double y = 0; y < size.height; y += step) {
      canvas.drawLine(Offset(0, y), Offset(size.width, y), paint);
    }
  }

  @override
  bool shouldRepaint(_RfGridPainter old) => old.color != color;
}


