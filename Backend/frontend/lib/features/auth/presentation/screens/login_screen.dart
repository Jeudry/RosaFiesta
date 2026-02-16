import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:frontend/l10n/generated/app_localizations.dart';
import 'package:frontend/core/widgets/brand_background.dart';
import 'package:frontend/core/widgets/glass_card.dart';
import 'package:frontend/core/app_theme.dart';
import '../auth_provider.dart';
import '../../../home/presentation/screens/home_screen.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  final _formKey = GlobalKey<FormState>();

  @override
  Widget build(BuildContext context) {
    final authProvider = context.watch<AuthProvider>();
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      extendBodyBehindAppBar: true,
      appBar: AppBar(
        title: Text(l10n.loginButton, style: const TextStyle(color: Colors.white)),
        backgroundColor: Colors.transparent,
        elevation: 0,
        iconTheme: const IconThemeData(color: Colors.white),
      ),
      body: BrandBackground(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24.0),
            child: GlassCard(
              opacity: 0.15,
              child: Padding(
                padding: const EdgeInsets.all(32.0),
                child: Form(
                  key: _formKey,
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      Text(
                        l10n.loginHeadline,
                        style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                              color: Colors.white,
                              fontWeight: FontWeight.bold,
                            ),
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 32),
                      TextFormField(
                        controller: _emailController,
                        style: const TextStyle(color: Colors.white),
                        decoration: InputDecoration(
                          labelText: l10n.emailLabel,
                          labelStyle: const TextStyle(color: Colors.white70),
                          prefixIcon: const Icon(Icons.email_outlined, color: Colors.white70),
                          fillColor: Colors.white.withOpacity(0.1),
                        ),
                        validator: (value) =>
                            (value == null || !value.contains('@')) ? l10n.emailError : null,
                      ),
                      const SizedBox(height: 16),
                      TextFormField(
                        controller: _passwordController,
                        obscureText: true,
                        style: const TextStyle(color: Colors.white),
                        decoration: InputDecoration(
                          labelText: l10n.passwordLabel,
                          labelStyle: const TextStyle(color: Colors.white70),
                          prefixIcon: const Icon(Icons.lock_outline, color: Colors.white70),
                          fillColor: Colors.white.withOpacity(0.1),
                        ),
                        validator: (value) =>
                            (value == null || value.length < 3) ? l10n.passwordError : null,
                      ),
                      const SizedBox(height: 24),
                      if (authProvider.error != null)
                        Padding(
                          padding: const EdgeInsets.only(bottom: 16),
                          child: Text(
                            authProvider.error!,
                            style: const TextStyle(color: AppColors.yellow),
                            textAlign: TextAlign.center,
                          ),
                        ),
                      ElevatedButton(
                        onPressed: authProvider.isLoading
                            ? null
                            : () async {
                                if (_formKey.currentState!.validate()) {
                                  final navigator = Navigator.of(context);
                                  final messenger = ScaffoldMessenger.of(context);
                                  final successMessage = l10n.loginSuccess;
                                  
                                  await authProvider.login(
                                    _emailController.text,
                                    _passwordController.text,
                                  );
                                  
                                  if (!mounted) return;
                                  if (authProvider.isAuthenticated) {
                                    messenger.showSnackBar(
                                      SnackBar(content: Text(successMessage)),
                                    );
                                    
                                    // Navigate to HomeScreen and remove all previous routes
                                    navigator.pushAndRemoveUntil(
                                      MaterialPageRoute(builder: (_) => const HomeScreen()),
                                      (route) => false,
                                    );
                                  }
                                }
                              },
                        style: ElevatedButton.styleFrom(
                          padding: const EdgeInsets.symmetric(vertical: 16),
                        ),
                        child: authProvider.isLoading
                            ? const CircularProgressIndicator(color: AppColors.purple)
                            : Text(l10n.loginButton),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
