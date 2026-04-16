import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import '../../../../core/design_system.dart';
import '../../../../core/router/app_router.dart';
import '../../../../core/router/app_router.dart';
import '../providers/quotes_provider.dart';

class QuotesScreen extends StatefulWidget {
  const QuotesScreen({super.key});

  @override
  State<QuotesScreen> createState() => _QuotesScreenState();
}

class _QuotesScreenState extends State<QuotesScreen> with SingleTickerProviderStateMixin {
  late TabController _tabController;

  final _statusTabs = [
    {'value': null, 'label': 'Todas'},
    {'value': 'pending_quote', 'label': 'Pendientes'},
    {'value': 'adjusted', 'label': 'Ajustadas'},
    {'value': 'paid', 'label': 'Pagadas'},
    {'value': 'rejected', 'label': 'Rechazadas'},
  ];

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: _statusTabs.length, vsync: this);
    _tabController.addListener(_onTabChanged);
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<QuotesProvider>().loadQuotes(refresh: true);
    });
  }

  void _onTabChanged() {
    if (!_tabController.indexIsChanging) {
      final status = _statusTabs[_tabController.index]['value'] as String?;
      context.read<QuotesProvider>().setStatusFilter(status);
    }
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<QuotesProvider>();

    return AdminScaffold(
      title: 'Cotizaciones',
      body: Column(
        children: [
          Container(
            color: Theme.of(context).cardTheme.color,
            child: TabBar(
              controller: _tabController,
              isScrollable: true,
              tabs: _statusTabs.map((t) => Tab(text: t['label'] as String)).toList(),
              labelColor: AppColors.primary,
              unselectedLabelColor: AppColors.textMuted,
              indicatorColor: AppColors.primary,
            ),
          ),
          Expanded(
            child: provider.quotes.isEmpty && !provider.loading
                ? EmptyState(
                    icon: Icons.request_quote_outlined,
                    title: 'No hay cotizaciones',
                    subtitle: 'Crea una nueva cotización para un cliente',
                    action: AdminButton(
                      label: 'Crear Cotización',
                      onTap: () => Navigator.pushNamed(context, '/quotes/create'),
                    ),
                  )
                : RefreshIndicator(
                    onRefresh: () => provider.loadQuotes(refresh: true),
                    child: ListView.builder(
                      padding: const EdgeInsets.all(16),
                      itemCount: provider.quotes.length,
                      itemBuilder: (ctx, i) => _QuoteCard(quote: provider.quotes[i]),
                    ),
                  ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => Navigator.pushNamed(context, '/quotes/create'),
        backgroundColor: AppColors.primary,
        child: const Icon(Icons.add),
      ),
    );
  }
}

class _QuoteCard extends StatelessWidget {
  final Map<String, dynamic> quote;

  const _QuoteCard({required this.quote});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: () => Navigator.pushNamed(context, '/events/${quote['id']}'),
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          quote['client_name'] ?? 'Cliente sin nombre',
                          style: GoogleFonts.dmSans(fontSize: 15, fontWeight: FontWeight.w600),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          quote['event_type'] ?? '',
                          style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textMuted),
                        ),
                      ],
                    ),
                  ),
                  _buildStatusBadge(quote['status']),
                ],
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Icon(Icons.calendar_today, size: 14, color: AppColors.textMuted),
                  const SizedBox(width: 6),
                  Text(quote['date'] ?? '', style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textSecondary)),
                  const Spacer(),
                  Text(
                    'RD\$${quote['total'] ?? 0}',
                    style: GoogleFonts.outfit(fontSize: 16, fontWeight: FontWeight.w700, color: AppColors.primary),
                  ),
                ],
              ),
              if (quote['status'] == 'pending_quote' || quote['status'] == 'adjusted') ...[
                const SizedBox(height: 12),
                Row(
                  children: [
                    Expanded(
                      child: OutlinedButton(
                        onPressed: () => _showAdjustDialog(context, quote['id']),
                        child: const Text('Ajustar'),
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: ElevatedButton(
                        onPressed: () async {
                          final confirmed = await showDialog<bool>(
                            context: context,
                            builder: (ctx) => AlertDialog(
                              title: const Text('Enviar Cotización'),
                              content: const Text('¿Enviar esta cotización al cliente por WhatsApp?'),
                              actions: [
                                TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Cancelar')),
                                ElevatedButton(onPressed: () => Navigator.pop(ctx, true), child: const Text('Enviar')),
                              ],
                            ),
                          );
                          if (confirmed == true) {
                            await context.read<QuotesProvider>().sendQuote(quote['id']);
                          }
                        },
                        child: const Text('Enviar'),
                      ),
                    ),
                  ],
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildStatusBadge(String? status) {
    switch (status) {
      case 'pending_quote':
        return StatusBadge(label: 'Pendiente', color: AppColors.warning);
      case 'adjusted':
        return StatusBadge(label: 'Ajustada', color: AppColors.violet);
      case 'paid':
        return StatusBadge.paid();
      case 'rejected':
        return StatusBadge.rejected();
      default:
        return StatusBadge.draft();
    }
  }

  void _showAdjustDialog(BuildContext context, String quoteId) {
    final amountController = TextEditingController();
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Ajustar Cotización'),
        content: AdminTextField(
          label: 'Nuevo monto total',
          controller: amountController,
          keyboardType: TextInputType.number,
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancelar')),
          ElevatedButton(
            onPressed: () async {
              final amount = int.tryParse(amountController.text);
              if (amount != null) {
                await context.read<QuotesProvider>().adjustQuote(quoteId, {'total': amount});
                if (context.mounted) Navigator.pop(ctx);
              }
            },
            child: const Text('Guardar'),
          ),
        ],
      ),
    );
  }
}
