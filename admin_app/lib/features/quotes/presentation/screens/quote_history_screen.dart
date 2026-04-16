import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import '../../../../core/api_client/api_client.dart';
import '../../../../core/design_system.dart';
import '../../../../core/router/app_router.dart';

class QuoteHistoryScreen extends StatefulWidget {
  final String? eventId;

  const QuoteHistoryScreen({super.key, this.eventId});

  @override
  State<QuoteHistoryScreen> createState() => _QuoteHistoryScreenState();
}

class _QuoteHistoryScreenState extends State<QuoteHistoryScreen> {
  List<dynamic> _history = [];
  bool _loading = true;
  String? _selectedEventId;
  final _apiClient = ApiClient();

  @override
  void initState() {
    super.initState();
    _selectedEventId = widget.eventId;
    _loadHistory();
  }

  Future<void> _loadHistory() async {
    setState(() => _loading = true);
    try {
      final resp = await _apiClient.getQuoteHistory(eventId: _selectedEventId);
      final data = resp.data['data'] as List? ?? [];
      if (mounted) setState(() { _history = data; _loading = false; });
    } catch (e) {
      if (mounted) setState(() => _loading = false);
    }
  }

  Color _actionColor(String action) {
    switch (action) {
      case 'quote_approved': return Colors.green;
      case 'quote_rejected': return AppColors.error;
      case 'quote_adjusted': return AppColors.amber;
      default: return Colors.grey;
    }
  }

  String _actionLabel(String action) {
    switch (action) {
      case 'quote_approved': return 'Aprobada';
      case 'quote_rejected': return 'Rechazada';
      case 'quote_adjusted': return 'Ajustada';
      default: return action;
    }
  }

  @override
  Widget build(BuildContext context) {
    return AdminScaffold(
      title: 'Historial de Cotizaciones',
      showBack: true,
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _history.isEmpty
              ? Center(child: Text('Sin historial de cotizaciones', style: GoogleFonts.dmSans(color: Colors.grey)))
              : ListView.builder(
                  padding: const EdgeInsets.all(16),
                  itemCount: _history.length,
                  itemBuilder: (ctx, i) {
                    final item = _history[i];
                    return Card(
                      margin: const EdgeInsets.only(bottom: 12),
                      child: ListTile(
                        leading: CircleAvatar(
                          backgroundColor: _actionColor(item['action'] ?? '').withOpacity(0.15),
                          child: Icon(
                            item['action'] == 'quote_approved'
                                ? Icons.check
                                : item['action'] == 'quote_rejected'
                                    ? Icons.close
                                    : Icons.edit,
                            color: _actionColor(item['action'] ?? ''),
                          ),
                        ),
                        title: Text(_actionLabel(item['action'] ?? ''), style: GoogleFonts.outfit(fontWeight: FontWeight.w600)),
                        subtitle: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            if (item['admin_name'] != null)
                              Text('Por: ${item['admin_name']}', style: GoogleFonts.dmSans(fontSize: 12)),
                            if (item['old_value'] != null && item['old_value'].toString().isNotEmpty)
                              Text('Antes: ${item['old_value']}', style: GoogleFonts.dmSans(fontSize: 12, color: Colors.grey)),
                            if (item['new_value'] != null && item['new_value'].toString().isNotEmpty)
                              Text('Después: ${item['new_value']}', style: GoogleFonts.dmSans(fontSize: 12, color: Colors.green)),
                          ],
                        ),
                        trailing: item['created_at'] != null
                            ? Text(
                                _formatDate(item['created_at']),
                                style: GoogleFonts.dmSans(fontSize: 11, color: Colors.grey),
                              )
                            : null,
                      ),
                    );
                  },
                ),
    );
  }

  String _formatDate(String dateStr) {
    try {
      final dt = DateTime.parse(dateStr);
      return '${dt.day}/${dt.month}/${dt.year}';
    } catch (_) {
      return dateStr;
    }
  }
}
