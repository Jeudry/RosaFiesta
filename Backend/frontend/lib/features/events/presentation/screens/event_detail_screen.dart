import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../data/event_model.dart';
import '../events_provider.dart';
import '../inspiration_provider.dart';
import '../../../tasks/presentation/screens/event_task_list_screen.dart';
import '../../presentation/timeline_provider.dart';
import '../../../guests/presentation/guests_provider.dart';
import '../../../tasks/presentation/tasks_provider.dart';
import 'package:frontend/core/services/pdf_export_service.dart';
import 'checkout_screen.dart';
import '../widgets/quotation_chat_widget.dart';
import 'package:frontend/core/app_theme.dart';
import '../debrief_provider.dart';
import 'event_timeline_screen.dart';
import 'event_execution_screen.dart';
import 'event_debrief_screen.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:frontend/core/config/env_config.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:share_plus/share_plus.dart' as share_plus;
import '../widgets/event_countdown_widget.dart';


class EventDetailScreen extends StatefulWidget {
  final String eventId;

  const EventDetailScreen({super.key, required this.eventId});

  @override
  State<EventDetailScreen> createState() => _EventDetailScreenState();
}

class _EventDetailScreenState extends State<EventDetailScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      Provider.of<EventsProvider>(context, listen: false).fetchEventItems(widget.eventId);
    });
  }

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 2,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Detalle de Reserva'),
          bottom: const TabBar(
            tabs: [
              Tab(text: 'Detalles', icon: Icon(Icons.description)),
              Tab(text: 'Chat', icon: Icon(Icons.chat)),
            ],
          ),
          actions: [
            IconButton(
              icon: const Icon(Icons.picture_as_pdf),
              onPressed: () => _exportToPdf(context),
              tooltip: 'Exportar a PDF',
            ),
            IconButton(
              icon: const Icon(Icons.share),
              onPressed: () => _showShareSheet(context),
              tooltip: 'Compartir',
            ),
          ],
        ),
        body: Consumer<EventsProvider>(
          builder: (context, provider, child) {
            final event = provider.events.firstWhere((e) => e.id == widget.eventId);

            return TabBarView(
              children: [
                // Tab 1: Details
                SingleChildScrollView(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    children: [
                      // Prominent Execution Mode Banner
                      InkWell(
                        onTap: () => Navigator.push(
                          context,
                          MaterialPageRoute(builder: (context) => EventExecutionScreen(eventId: widget.eventId)),
                        ),
                        borderRadius: BorderRadius.circular(16),
                        child: Container(
                          width: double.infinity,
                          padding: const EdgeInsets.symmetric(vertical: 20, horizontal: 16),
                          decoration: BoxDecoration(
                            gradient: const LinearGradient(
                              colors: [AppColors.primary, AppColors.accent],
                              begin: Alignment.topLeft,
                              end: Alignment.bottomRight,
                            ),
                            borderRadius: BorderRadius.circular(16),
                            boxShadow: [
                              BoxShadow(color: AppColors.primary.withValues(alpha: 0.4), blurRadius: 8, offset: const Offset(0, 4)),
                            ],
                          ),
                          child: const Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text('MODO EJECUCIÓN', style: TextStyle(color: Colors.white, fontWeight: FontWeight.w900, letterSpacing: 1.2, fontSize: 18)),
                                  SizedBox(height: 4),
                                  Text('Lista de verificación para hoy', style: TextStyle(color: Colors.white70, fontSize: 14)),
                                ],
                              ),
                              Icon(Icons.play_circle_fill, color: Colors.white, size: 40),
                            ],
                          ),
                        ),
                      ),
                      const SizedBox(height: 20),

                      // Event Countdown Widget (only show for upcoming confirmed/paid/adjusted events)
                      if (event.date != null &&
                          (event.status == 'confirmed' ||
                              event.status == 'paid' ||
                              event.status == 'adjusted'))
                        EventCountdownWidget(
                          eventDate: event.date!,
                          eventName: event.name,
                        ),

                      const SizedBox(height: 20),

                      _buildControlGroup([
                        _buildNavAction(
                          icon: Icons.check_circle_outline,
                          label: 'Tareas',
                          subtitle: 'Ver checklist',
                          onTap: () => Navigator.push(
                            context,
                            MaterialPageRoute(builder: (context) => EventTaskListScreen(eventId: widget.eventId)),
                          ),
                        ),
                        const Divider(height: 1),
                        _buildNavAction(
                          icon: Icons.timer_outlined,
                          label: 'Cronograma',
                          subtitle: 'Ver planificación por horas',
                          onTap: () => Navigator.push(
                            context,
                            MaterialPageRoute(builder: (context) => EventTimelineScreen(eventId: widget.eventId)),
                          ),
                        ),
                        const Divider(height: 1),
                        _buildNavAction(
                          icon: Icons.calendar_month,
                          label: 'Sincronizar Calendario',
                          subtitle: 'Exportar a Google/Apple Calendar',
                          onTap: () => _syncCalendar(context, widget.eventId),
                        ),
                      ]),
                      
                      const SizedBox(height: 20),

                      // Budget Comparison Group
                      _buildControlGroup([
                        _buildBudgetView(event, provider),
                      ]),

                      const SizedBox(height: 20),

                      // Mood Board / Inspiration Section
                      _MoodBoardSection(eventId: widget.eventId),

                      const SizedBox(height: 20),

                      _buildControlGroup([
                        if (event.adminNotes != null && event.adminNotes!.isNotEmpty)
                          _buildDetailRow(Icons.note, 'Notas de Admin', event.adminNotes!, color: Colors.orange),
                        _buildDetailRow(Icons.info, 'Estado', _getStatusLabel(event.status)),
                        if (event.status == 'paid')
                           _buildDetailRow(Icons.verified, 'Pago', 'Completado via ${event.paymentMethod ?? "N/A"}', color: Colors.green),
                      ]),

                      const SizedBox(height: 24),
                      
                      // Action Buttons based on status
                      _buildActionButtons(context, provider, event),

                      const SizedBox(height: 32),
                      
                      const Align(
                        alignment: Alignment.centerLeft,
                        child: Text(
                          'Mobiliario y Decoración',
                          style: TextStyle(fontSize: 20, fontWeight: FontWeight.w800, letterSpacing: -0.5),
                        ),
                      ),
                      const SizedBox(height: 16),
                      provider.isLoading
                          ? const Center(child: CircularProgressIndicator())
                          : provider.currentEventItems.isEmpty
                              ? const Center(child: Text('No hay productos agregados'))
                              : ListView.builder(
                                  shrinkWrap: true,
                                  physics: const NeverScrollableScrollPhysics(),
                                  itemCount: provider.currentEventItems.length,
                                  itemBuilder: (context, index) {
                                    final item = provider.currentEventItems[index];
                                    return Container(
                                      margin: const EdgeInsets.only(bottom: 12),
                                      decoration: BoxDecoration(
                                        color: Colors.white,
                                        borderRadius: BorderRadius.circular(16),
                                        boxShadow: AppDecorations.softShadow,
                                      ),
                                      child: ListTile(
                                        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                                        leading: Container(
                                          padding: const EdgeInsets.all(8),
                                          decoration: BoxDecoration(
                                            color: AppColors.primary.withValues(alpha: 0.1),
                                            borderRadius: BorderRadius.circular(8),
                                          ),
                                          child: const Icon(Icons.chair, color: AppColors.primary),
                                        ),
                                        title: Text(
                                          item.article?.nameTemplate ?? 'Producto desconocido',
                                          style: const TextStyle(fontWeight: FontWeight.bold),
                                        ),
                                        subtitle: Text('${item.quantity} x \$${item.price?.toStringAsFixed(2) ?? "0.00"}'),
                                        trailing: IconButton(
                                          icon: const Icon(Icons.delete_outline, color: Colors.redAccent),
                                          onPressed: () => provider.removeItemFromEvent(event.id, item.id),
                                        ),
                                      ),
                                    );
                                  },
                                ),
                    ],
                  ),
                ),
                
                // Tab 2: Chat (Real-time)
                QuotationChatWidget(eventId: widget.eventId),
              ],
            );
          },
        ),
      ),
    );
  }

  Widget _buildControlGroup(List<Widget> children) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        boxShadow: AppDecorations.softShadow,
      ),
      child: Column(children: children),
    );
  }

  Widget _buildNavAction({required IconData icon, required String label, required String subtitle, required VoidCallback onTap}) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(24),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(color: AppColors.accent.withValues(alpha: 0.1), borderRadius: BorderRadius.circular(12)),
              child: Icon(icon, color: AppColors.accent),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(label, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                  Text(subtitle, style: const TextStyle(color: Colors.grey, fontSize: 12)),
                ],
              ),
            ),
            const Icon(Icons.chevron_right, color: Colors.grey),
          ],
        ),
      ),
    );
  }

  Widget _buildBudgetView(Event event, EventsProvider provider) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        boxShadow: AppDecorations.softShadow,
      ),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('Análisis de Presupuesto', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 16)),
                const Icon(Icons.analytics_outlined, color: AppColors.primary),
              ],
            ),
            const SizedBox(height: 20),
            Row(
              children: [
                Expanded(child: _buildBudgetTile('Estimado', '\$${event.budget.toStringAsFixed(2)}', Colors.grey)),
                Container(width: 1, height: 40, color: Colors.grey.shade100, margin: const EdgeInsets.symmetric(horizontal: 16)),
                Expanded(child: _buildBudgetTile('Real', '\$${provider.realBudget.toStringAsFixed(2)}', provider.realBudget > event.budget ? Colors.red : Colors.green)),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildBudgetTile(String label, String value, Color color) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: const TextStyle(fontSize: 10, color: Colors.grey, fontWeight: FontWeight.bold)),
        Text(value, style: TextStyle(fontSize: 18, color: color, fontWeight: FontWeight.w900)),
      ],
    );
  }

  Future<void> _exportToPdf(BuildContext context) async {
    final eventsProvider = context.read<EventsProvider>();
    final guestsProvider = context.read<GuestsProvider>();
    final tasksProvider = context.read<EventTasksProvider>();
    final timelineProvider = context.read<TimelineProvider>();
    final debriefProvider = context.read<DebriefProvider>();

    // Show loading dialog
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => const Center(child: CircularProgressIndicator()),
    );

    try {
      final event = eventsProvider.events.firstWhere((e) => e.id == widget.eventId);

      // List of tasks to load
      List<Future> loadTasks = [
        guestsProvider.fetchGuests(widget.eventId),
        tasksProvider.fetchTasks(widget.eventId),
        timelineProvider.fetchTimeline(widget.eventId),
      ];

      // Add debrief fetch if event is completed
      if (event.status == 'completed' || event.status == 'paid') {
        loadTasks.add(debriefProvider.fetchDebrief(widget.eventId));
      }

      await Future.wait(loadTasks);

      if (context.mounted) {
        Navigator.pop(context); // Close loading dialog
        
        await PdfExportService.generateEventReport(
          event: event,
          products: eventsProvider.currentEventItems,
          guests: guestsProvider.guests,
          tasks: tasksProvider.tasks,
          timeline: timelineProvider.items,
          debrief: debriefProvider.debrief,
        );
      }
    } catch (e) {
      if (context.mounted) {
        Navigator.pop(context);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error al exportar: $e')),
        );
      }
    }
  }

  String _getStatusLabel(String status) {
    switch (status) {
      case 'planning': return 'Borrador (Planeando)';
      case 'requested': return 'En Revisión (Cotización Solicitada)';
      case 'adjusted': return 'Cotización Lista (Revisar Ajustes)';
      case 'confirmed': return 'Confirmado (Pendiente de Pago)';
      case 'paid': return 'Pagado y Reservado';
      case 'completed': return 'Evento Finalizado';
      default: return status;
    }
  }

  Widget _buildActionButtons(BuildContext context, EventsProvider provider, Event event) {
    if (event.status == 'planning') {
      return SizedBox(
        width: double.infinity,
        child: ElevatedButton.icon(
          icon: const Icon(Icons.send),
          label: const Text('Solicitar Cotización a Rosa Fiesta'),
          onPressed: () => _requestQuotation(context, provider),
          style: ElevatedButton.styleFrom(backgroundColor: Colors.pink, foregroundColor: Colors.white),
        ),
      );
    }
    
    if (event.status == 'adjusted') {
      return SizedBox(
        width: double.infinity,
        child: ElevatedButton.icon(
          icon: const Icon(Icons.check_circle),
          label: const Text('Aceptar Cotización'),
          onPressed: () => _confirmQuotation(context, provider),
          style: ElevatedButton.styleFrom(backgroundColor: Colors.green, foregroundColor: Colors.white),
        ),
      );
    }

    if (event.status == 'confirmed') {
      return SizedBox(
        width: double.infinity,
        child: ElevatedButton.icon(
          icon: const Icon(Icons.payment),
          label: const Text('Pagar para Reservar'),
          onPressed: () => _navigateToCheckout(context, provider, event),
          style: ElevatedButton.styleFrom(backgroundColor: Colors.blue, foregroundColor: Colors.white),
        ),
      );
    }

    if (event.status == 'paid') {
      return SizedBox(
        width: double.infinity,
        child: OutlinedButton.icon(
          icon: const Icon(Icons.description),
          label: const Text('Descargar Contrato'),
          onPressed: () => _downloadContract(context, event.id),
          style: OutlinedButton.styleFrom(
            foregroundColor: AppColors.primary,
            side: const BorderSide(color: AppColors.primary),
            padding: const EdgeInsets.symmetric(vertical: 12),
          ),
        ),
      );
    }

    if (event.status == 'completed') {
      return SizedBox(
        width: double.infinity,
        child: ElevatedButton.icon(
          icon: const Icon(Icons.analytics),
          label: const Text('Ver Análisis Post-Evento'),
          onPressed: () => Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => EventDebriefScreen(eventId: widget.eventId)),
          ),
          style: ElevatedButton.styleFrom(backgroundColor: AppColors.primary, foregroundColor: Colors.white),
        ),
      );
    }

    return const SizedBox.shrink();
  }

  Future<void> _downloadContract(BuildContext context, String eventId) async {
    const storage = FlutterSecureStorage();
    final token = await storage.read(key: 'access_token');
    if (token == null) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Error de autenticación')),
        );
      }
      return;
    }

    // Build URL with auth token - backend will redirect to PDF
    final url = Uri.parse('${EnvConfig.apiUrl}/events/$eventId/contract?token=$token');

    try {
      await launchUrl(url, mode: LaunchMode.externalApplication);
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error al descargar contrato: $e')),
        );
      }
    }
  }

  void _navigateToCheckout(BuildContext context, EventsProvider provider, Event event) {
    double realBudget = 0;
    for (var item in provider.currentEventItems) {
      if (item.price != null) {
        realBudget += item.price! * item.quantity;
      }
    }
    final total = realBudget + event.additionalCosts;

    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => CheckoutScreen(eventId: event.id, eventName: event.name.isEmpty ? 'Mi Evento' : event.name, totalAmount: total),
      ),
    );
  }

  Future<void> _requestQuotation(BuildContext context, EventsProvider provider) async {
    final success = await provider.requestQuote(widget.eventId);
    if (success && context.mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Cotización solicitada. Rosa Fiesta revisará tu evento.')),
      );
      Navigator.pop(context); // Go back to refresh list
    }
  }

  Future<void> _confirmQuotation(BuildContext context, EventsProvider provider) async {
    final success = await provider.confirmQuote(widget.eventId);
    if (success && context.mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('¡Evento confirmado y reservado!')),
      );
      Navigator.pop(context);
    }
  }

  Widget _buildDetailRow(IconData icon, String label, String value, {Color? color}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: Row(
        children: [
          Icon(icon, color: Colors.grey),
          const SizedBox(width: 16),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
              Text(value, style: TextStyle(fontSize: 16, color: color, fontWeight: color != null ? FontWeight.bold : FontWeight.normal)),
            ],
          ),
        ],
      ),
    );
  }

  Future<void> _syncCalendar(BuildContext context, String eventId) async {
    const storage = FlutterSecureStorage();
    final token = await storage.read(key: 'access_token');
    if (token == null) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Error de autenticación')),
        );
      }
      return;
    }

    final url = Uri.parse('${EnvConfig.apiUrl}/events/$eventId/calendar.ics?token=$token');
    
    if (await canLaunchUrl(url)) {
      // mode: LaunchMode.externalApplication is necessary for .ics files on mobile
      await launchUrl(url, mode: LaunchMode.externalApplication);
    } else {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('No se pudo abrir el calendario')),
        );
      }
    }
  }

  void _showShareSheet(BuildContext context) {
    final provider = context.read<EventsProvider>();
    final Event event;
    try {
      event = provider.events.firstWhere((e) => e.id == widget.eventId);
    } catch (_) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('No se pudo cargar la informacion del evento')),
      );
      return;
    }

    final eventDate = event.date != null
        ? '${event.date!.day}/${event.date!.month}/${event.date!.year}'
        : 'Fecha por confirmar';
    final shareUrl = '${EnvConfig.apiUrl}/events/${event.id}/share-card';

    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (ctx) => Padding(
        padding: const EdgeInsets.fromLTRB(20, 16, 20, 32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 40,
              height: 4,
              decoration: BoxDecoration(
                color: Colors.grey.shade300,
                borderRadius: BorderRadius.circular(2),
              ),
            ),
            const SizedBox(height: 20),
            ListTile(
              leading: Container(
                width: 42,
                height: 42,
                decoration: BoxDecoration(
                  color: AppColors.teal.withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: const Icon(Icons.text_fields, color: AppColors.teal),
              ),
              title: const Text('Compartir como texto'),
              subtitle: const Text('Copiar invitacion al portapapeles'),
              onTap: () {
                final text = '🎉 ¡Mi evento "$event.name" ($eventDate) está confirmado! '
                    'Organizado con RosaFiesta 🌸\n\n'
                    'Visita rosafiesta.com/mi-evento/${event.id}';
                share_plus.Share.share(text, subject: 'Mi evento RosaFiesta');
                Navigator.pop(ctx);
              },
            ),
            ListTile(
              leading: Container(
                width: 42,
                height: 42,
                decoration: BoxDecoration(
                  color: AppColors.hotPink.withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: const Icon(Icons.image, color: AppColors.hotPink),
              ),
              title: const Text('Generar tarjeta para WhatsApp'),
              subtitle: const Text('Abrir tarjeta compartida en el navegador'),
              onTap: () {
                Navigator.pop(ctx);
                launchUrl(Uri.parse(shareUrl), mode: LaunchMode.externalApplication);
              },
            ),
          ],
        ),
      ),
    );
  }
}

// Mood Board / Inspiration Section Widget
class _MoodBoardSection extends StatefulWidget {
  final String eventId;

  const _MoodBoardSection({required this.eventId});

  @override
  State<_MoodBoardSection> createState() => _MoodBoardSectionState();
}

class _MoodBoardSectionState extends State<_MoodBoardSection> {
  bool _isExpanded = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<InspirationProvider>().fetchInspiration(widget.eventId);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: const Color(0xFFFAF8F5),
        borderRadius: BorderRadius.circular(24),
        boxShadow: AppDecorations.softShadow,
        border: Border.all(color: const Color(0xFFE8E0D5), width: 1),
      ),
      child: Column(
        children: [
          InkWell(
            onTap: () => setState(() => _isExpanded = !_isExpanded),
            borderRadius: BorderRadius.circular(24),
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(
                      color: AppColors.amber.withValues(alpha: 0.15),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: const Icon(Icons.photo_library, color: AppColors.amber, size: 24),
                  ),
                  const SizedBox(width: 16),
                  const Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Tablero de Inspiración',
                          style: TextStyle(fontWeight: FontWeight.w800, fontSize: 16),
                        ),
                        SizedBox(height: 2),
                        Text(
                          'Fotos de referencia para tu evento',
                          style: TextStyle(color: Colors.grey, fontSize: 12),
                        ),
                      ],
                    ),
                  ),
                  Consumer<InspirationProvider>(
                    builder: (context, provider, _) {
                      final count = provider.photos.length;
                      if (count > 0) {
                        return Container(
                          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                          decoration: BoxDecoration(
                            color: AppColors.hotPink.withValues(alpha: 0.1),
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: Text(
                            '$count',
                            style: const TextStyle(
                              color: AppColors.hotPink,
                              fontWeight: FontWeight.bold,
                              fontSize: 12,
                            ),
                          ),
                        );
                      }
                      return const SizedBox.shrink();
                    },
                  ),
                  const SizedBox(width: 8),
                  AnimatedRotation(
                    turns: _isExpanded ? 0.5 : 0,
                    duration: const Duration(milliseconds: 300),
                    child: const Icon(Icons.keyboard_arrow_down, color: Colors.grey),
                  ),
                ],
              ),
            ),
          ),
          if (_isExpanded) ...[
            const Divider(height: 1),
            Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Consumer<InspirationProvider>(
                    builder: (context, provider, _) {
                      if (provider.isLoading) {
                        return const Center(
                          child: Padding(
                            padding: EdgeInsets.all(20),
                            child: CircularProgressIndicator(),
                          ),
                        );
                      }

                      if (provider.photos.isEmpty) {
                        return _buildEmptyState(context);
                      }

                      return _buildPhotoGrid(context, provider);
                    },
                  ),
                ],
              ),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildEmptyState(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(vertical: 32, horizontal: 20),
      decoration: BoxDecoration(
        color: Colors.white.withValues(alpha: 0.6),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: const Color(0xFFE8E0D5)),
      ),
      child: Column(
        children: [
          Icon(Icons.add_photo_alternate_outlined, size: 48, color: Colors.grey.shade400),
          const SizedBox(height: 12),
          const Text(
            '¿Quieres agregar fotos de inspiración?',
            style: TextStyle(fontWeight: FontWeight.w600, fontSize: 15),
          ),
          const SizedBox(height: 4),
          Text(
            'Sube fotos de Pinterest, Instagram o tu galería',
            style: TextStyle(color: Colors.grey.shade600, fontSize: 13),
          ),
          const SizedBox(height: 16),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              OutlinedButton(
                onPressed: () => setState(() => _isExpanded = false),
                style: OutlinedButton.styleFrom(
                  foregroundColor: Colors.grey,
                  side: const BorderSide(color: Colors.grey),
                  padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
                ),
                child: const Text('Ahora no'),
              ),
              const SizedBox(width: 12),
              ElevatedButton.icon(
                onPressed: () => _addPhoto(context),
                icon: const Icon(Icons.add, size: 18),
                label: const Text('Sí, agregar'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.hotPink,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildPhotoGrid(BuildContext context, InspirationProvider provider) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SizedBox(
          height: 200,
          child: ListView.builder(
            scrollDirection: Axis.horizontal,
            itemCount: provider.photos.length > 10 ? 10 : provider.photos.length,
            itemBuilder: (context, index) {
              final photo = provider.photos[index];
              return _PolaroidFrame(
                photoUrl: photo.photoUrl,
                caption: photo.caption,
                onDelete: () => _confirmDelete(context, provider, photo.id),
              );
            },
          ),
        ),
        const SizedBox(height: 16),
        if (provider.photos.length < 10)
          Center(
            child: TextButton.icon(
              onPressed: () => _addPhoto(context),
              icon: const Icon(Icons.add_photo_alternate, size: 20),
              label: const Text('Agregar foto'),
              style: TextButton.styleFrom(foregroundColor: AppColors.hotPink),
            ),
          ),
      ],
    );
  }

  Future<void> _addPhoto(BuildContext context) async {
    final provider = context.read<InspirationProvider>();
    final imageFile = await provider.pickImage(context);

    if (imageFile != null) {
      final success = await provider.uploadInspiration(context, widget.eventId, imageFile);
      if (success && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Foto agregada al tablero')),
        );
      }
    }
  }

  void _confirmDelete(BuildContext context, InspirationProvider provider, String photoId) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Eliminar foto'),
        content: const Text('¿Estás segura de eliminar esta foto de inspiración?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancelar'),
          ),
          TextButton(
            onPressed: () async {
              Navigator.pop(ctx);
              await provider.deleteInspiration(widget.eventId, photoId);
            },
            style: TextButton.styleFrom(foregroundColor: Colors.red),
            child: const Text('Eliminar'),
          ),
        ],
      ),
    );
  }
}

class _PolaroidFrame extends StatelessWidget {
  final String photoUrl;
  final String? caption;
  final VoidCallback onDelete;

  const _PolaroidFrame({
    required this.photoUrl,
    this.caption,
    required this.onDelete,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 150,
      margin: const EdgeInsets.only(right: 12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(4),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.15),
            blurRadius: 8,
            offset: const Offset(0, 3),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          ClipRRect(
            borderRadius: const BorderRadius.vertical(top: Radius.circular(4)),
            child: Image.network(
              photoUrl,
              width: 150,
              height: 120,
              fit: BoxFit.cover,
              errorBuilder: (_, __, ___) => Container(
                width: 150,
                height: 120,
                color: Colors.grey.shade200,
                child: const Icon(Icons.broken_image, color: Colors.grey),
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(8),
            child: Row(
              children: [
                if (caption != null && caption!.isNotEmpty)
                  Expanded(
                    child: Text(
                      caption!,
                      style: const TextStyle(fontSize: 10, color: Colors.black87),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                GestureDetector(
                  onTap: onDelete,
                  child: Icon(Icons.close, size: 16, color: Colors.grey.shade600),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
