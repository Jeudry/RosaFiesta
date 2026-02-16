import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/app_theme.dart';
import '../../auth/presentation/auth_provider.dart';
import '../../home/presentation/screens/welcome_onboarding_screen.dart';
import '../profile_provider.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final authProvider = context.read<AuthProvider>();
      if (authProvider.user != null) {
        context.read<ProfileProvider>().fetchProfile(authProvider.user!.id);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Mi Perfil'),
        backgroundColor: Colors.transparent,
        elevation: 0,
      ),
      body: Consumer<ProfileProvider>(
        builder: (context, provider, child) {
          if (provider.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (provider.error != null) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text('Error: ${provider.error}', style: const TextStyle(color: Colors.red)),
                  ElevatedButton(
                    onPressed: () {
                      final authProvider = context.read<AuthProvider>();
                      if (authProvider.user != null) {
                        provider.fetchProfile(authProvider.user!.id);
                      }
                    },
                    child: const Text('Reintentar'),
                  ),
                ],
              ),
            );
          }

          final user = provider.userProfile;
          if (user == null) {
            return const Center(child: Text('No se pudo cargar el perfil'));
          }

          return SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: Column(
              children: [
                // Avatar
                Container(
                  width: 120,
                  height: 120,
                  decoration: BoxDecoration(
                    color: AppColors.teal.withOpacity(0.1),
                    shape: BoxShape.circle,
                    border: Border.all(color: AppColors.teal, width: 2),
                    image: user.avatar != null && user.avatar!.isNotEmpty
                        ? DecorationImage(
                            image: NetworkImage(user.avatar!), fit: BoxFit.cover)
                        : null,
                  ),
                  child: user.avatar == null || user.avatar!.isEmpty
                      ? const Icon(Icons.person, size: 64, color: AppColors.teal)
                      : null,
                ),
                const SizedBox(height: 24),
                
                // Name
                Text(
                  user.fullName,
                  style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                        fontWeight: FontWeight.bold,
                        color: AppColors.purple,
                      ),
                ),
                const SizedBox(height: 8),
                Text(
                  user.email,
                  style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                        color: Colors.grey[600],
                      ),
                ),
                
                const SizedBox(height: 48),

                // Details Card
                Container(
                  padding: const EdgeInsets.all(24),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(24),
                    boxShadow: [
                      BoxShadow(
                        color: Colors.black.withOpacity(0.05),
                        blurRadius: 20,
                        offset: const Offset(0, 10),
                      ),
                    ],
                  ),
                  child: Column(
                    children: [
                       _buildInfoRow(Icons.phone, 'Phone', user.phoneNumber),
                       const Divider(height: 32),
                       _buildInfoRow(Icons.cake, 'Birthday', user.bornDate ?? 'Not set'),
                       const Divider(height: 32),
                       _buildInfoRow(Icons.badge, 'Username', user.userName),
                    ],
                  ),
                ),

                const SizedBox(height: 48),

                // Logout Button
                SizedBox(
                  width: double.infinity,
                  height: 56,
                  child: OutlinedButton.icon(
                    onPressed: () {
                      context.read<AuthProvider>().logout();
                      context.read<ProfileProvider>().clearProfile();
                      
                      Navigator.of(context).pushAndRemoveUntil(
                        MaterialPageRoute(
                          builder: (_) => const WelcomeOnboardingScreen(),
                        ),
                        (route) => false,
                      );
                    },
                    icon: const Icon(Icons.logout, color: Colors.red),
                    label: const Text(
                      'Cerrar Sesión',
                      style: TextStyle(color: Colors.red, fontSize: 16),
                    ),
                    style: OutlinedButton.styleFrom(
                      side: const BorderSide(color: Colors.red),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(16),
                      ),
                    ),
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _buildInfoRow(IconData icon, String label, String value) {
    return Row(
      children: [
        Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: AppColors.lime.withOpacity(0.2),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Icon(icon, color: AppColors.teal, size: 20),
        ),
        const SizedBox(width: 16),
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              label,
              style: TextStyle(
                color: Colors.grey[500],
                fontSize: 12,
                fontWeight: FontWeight.w500,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              value.isEmpty ? 'Not set' : value,
              style: const TextStyle(
                color: AppColors.purple,
                fontSize: 16,
                fontWeight: FontWeight.w600,
              ),
            ),
          ],
        ),
      ],
    );
  }
}
