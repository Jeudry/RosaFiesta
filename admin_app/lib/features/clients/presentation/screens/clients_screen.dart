import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import '../../../../core/design_system.dart';
import '../../../../core/router/app_router.dart';
import '../../../../core/router/app_router.dart';
import '../providers/clients_provider.dart';

class ClientsScreen extends StatefulWidget {
  const ClientsScreen({super.key});

  @override
  State<ClientsScreen> createState() => _ClientsScreenState();
}

class _ClientsScreenState extends State<ClientsScreen> with SingleTickerProviderStateMixin {
  late TabController _tabController;
  final _searchController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    _tabController.addListener(_onTabChanged);
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ClientsProvider>().loadClients(refresh: true);
    });
  }

  void _onTabChanged() {
    context.read<ClientsProvider>().loadClients(refresh: true);
  }

  @override
  void dispose() {
    _tabController.dispose();
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<ClientsProvider>();
    final isRegisteredTab = _tabController.index == 0;

    // Filter clients by registered/lead status
    final clients = provider.clients.where((c) {
      if (isRegisteredTab) {
        return c['is_lead'] != true;
      } else {
        return c['is_lead'] == true;
      }
    }).toList();

    return AdminScaffold(
      title: 'Clientes',
      body: Column(
        children: [
          Container(
            padding: const EdgeInsets.all(16),
            child: AdminSearchField(
              controller: _searchController,
              hint: 'Buscar por nombre, email o teléfono...',
              onChanged: (q) => provider.search(q),
            ),
          ),
          Container(
            color: Theme.of(context).cardTheme.color,
            child: TabBar(
              controller: _tabController,
              tabs: const [
                Tab(text: 'Registrados'),
                Tab(text: 'Leads'),
              ],
              labelColor: AppColors.primary,
              unselectedLabelColor: AppColors.textMuted,
              indicatorColor: AppColors.primary,
            ),
          ),
          Expanded(
            child: clients.isEmpty && !provider.loading
                ? EmptyState(
                    icon: Icons.people_outline,
                    title: isRegisteredTab ? 'No hay clientes registrados' : 'No hay leads',
                    action: AdminButton(
                      label: 'Crear Lead',
                      onTap: () => _showCreateLeadDialog(context),
                    ),
                  )
                : RefreshIndicator(
                    onRefresh: () => provider.loadClients(refresh: true),
                    child: ListView.builder(
                      padding: const EdgeInsets.all(16),
                      itemCount: clients.length,
                      itemBuilder: (ctx, i) => _ClientCard(client: clients[i]),
                    ),
                  ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => _showCreateLeadDialog(context),
        backgroundColor: AppColors.primary,
        child: const Icon(Icons.person_add),
      ),
    );
  }

  void _showCreateLeadDialog(BuildContext context) {
    final nameController = TextEditingController();
    final phoneController = TextEditingController();

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Crear Lead'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            AdminTextField(label: 'Nombre', controller: nameController),
            const SizedBox(height: 12),
            AdminTextField(label: 'Teléfono', controller: phoneController, keyboardType: TextInputType.phone),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancelar')),
          ElevatedButton(
            onPressed: () async {
              if (nameController.text.isNotEmpty) {
                await context.read<ClientsProvider>().createLead({
                  'name': nameController.text,
                  'phone': phoneController.text,
                });
                if (context.mounted) Navigator.pop(ctx);
              }
            },
            child: const Text('Crear'),
          ),
        ],
      ),
    );
  }
}

class _ClientCard extends StatelessWidget {
  final Map<String, dynamic> client;

  const _ClientCard({required this.client});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: () => Navigator.pushNamed(context, '/clients/${client['id']}'),
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              Container(
                width: 48,
                height: 48,
                decoration: BoxDecoration(
                  color: client['is_lead'] == true
                      ? AppColors.warning.withValues(alpha: 0.1)
                      : AppColors.primary.withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(24),
                ),
                child: Icon(
                  client['is_lead'] == true ? Icons.person_outline : Icons.person,
                  color: client['is_lead'] == true ? AppColors.warning : AppColors.primary,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Flexible(
                          child: Text(
                            client['name'] ?? '',
                            style: GoogleFonts.dmSans(fontSize: 15, fontWeight: FontWeight.w600),
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                        if (client['is_lead'] == true) ...[
                          const SizedBox(width: 8),
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                            decoration: BoxDecoration(
                              color: AppColors.warning.withValues(alpha: 0.1),
                              borderRadius: BorderRadius.circular(4),
                            ),
                            child: Text('Lead', style: GoogleFonts.dmSans(fontSize: 10, color: AppColors.warning)),
                          ),
                        ],
                      ],
                    ),
                    const SizedBox(height: 4),
                    Text(
                      client['email'] ?? 'Sin email',
                      style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textMuted),
                    ),
                    Text(
                      client['phone'] ?? '',
                      style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textMuted),
                    ),
                  ],
                ),
              ),
              Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  if (client['events_count'] != null)
                    Text(
                      '${client['events_count']} eventos',
                      style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textSecondary),
                    ),
                  if (client['total_spent'] != null)
                    Text(
                      'RD\$${client['total_spent']}',
                      style: GoogleFonts.outfit(fontSize: 14, fontWeight: FontWeight.w600, color: AppColors.primary),
                    ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
