import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import '../../../../core/design_system.dart';
import '../../../../core/router/app_router.dart';
import '../../../../core/api_client/api_client.dart';

class ActivityLogScreen extends StatefulWidget {
  const ActivityLogScreen({super.key});

  @override
  State<ActivityLogScreen> createState() => _ActivityLogScreenState();
}

class _ActivityLogScreenState extends State<ActivityLogScreen> {
  List<dynamic> _logs = [];
  bool _loading = true;
  int _page = 1;
  bool _hasMore = true;
  String? _adminFilter;
  String? _actionFilter;

  final _actionTypes = [
    {'value': null, 'label': 'Todas'},
    {'value': 'event_create', 'label': 'Crear evento'},
    {'value': 'event_update', 'label': 'Actualizar evento'},
    {'value': 'quote_adjust', 'label': 'Ajustar cotización'},
    {'value': 'payment', 'label': 'Pago'},
    {'value': 'user_block', 'label': 'Bloquear usuario'},
  ];

  @override
  void initState() {
    super.initState();
    _loadLogs();
  }

  Future<void> _loadLogs({bool refresh = false}) async {
    if (_loading && !refresh) return;
    if (refresh) {
      _page = 1;
      _logs = [];
      _hasMore = true;
    }
    if (!_hasMore) return;

    setState(() => _loading = true);

    try {
      final response = await apiClient.getAuditLogs(
        page: _page,
        adminId: _adminFilter,
        actionType: _actionFilter,
      );
      final data = response.data['data'] ?? [];
      if (refresh) {
        _logs = data;
      } else {
        _logs.addAll(data);
      }
      _hasMore = data.length >= 20;
      _page++;
    } catch (e) {
      _logs = [];
    }

    setState(() => _loading = false);
  }

  String _formatAction(String action) {
    switch (action) {
      case 'event_create':
        return 'creó un evento';
      case 'event_update':
        return 'actualizó un evento';
      case 'quote_adjust':
        return 'ajustó cotización';
      case 'payment':
        return 'registró pago';
      case 'user_block':
        return 'bloqueó usuario';
      default:
        return action;
    }
  }

  Color _getActionColor(String action) {
    switch (action) {
      case 'event_create':
        return AppColors.success;
      case 'event_update':
        return AppColors.primary;
      case 'quote_adjust':
        return AppColors.violet;
      case 'payment':
        return AppColors.teal;
      case 'user_block':
        return AppColors.error;
      default:
        return AppColors.textMuted;
    }
  }

  @override
  Widget build(BuildContext context) {
    return AdminScaffold(
      title: 'Log de Actividad',
      showBack: true,
      body: Column(
        children: [
          // Filters
          Container(
            padding: const EdgeInsets.all(16),
            child: Column(
              children: [
                SizedBox(
                  height: 40,
                  child: ListView.separated(
                    scrollDirection: Axis.horizontal,
                    itemCount: _actionTypes.length,
                    separatorBuilder: (_, __) => const SizedBox(width: 8),
                    itemBuilder: (ctx, i) {
                      final type = _actionTypes[i];
                      final isSelected = _actionFilter == type['value'];
                      return FilterChip(
                        label: Text(type['label'] as String),
                        selected: isSelected,
                        onSelected: (_) {
                          setState(() => _actionFilter = type['value'] as String?);
                          _loadLogs(refresh: true);
                        },
                        selectedColor: AppColors.primary.withValues(alpha: 0.2),
                      );
                    },
                  ),
                ),
              ],
            ),
          ),

          // Log list
          Expanded(
            child: _logs.isEmpty && !_loading
                ? const EmptyState(
                    icon: Icons.history,
                    title: 'No hay actividad',
                    subtitle: 'Las acciones de admins aparecerán aquí',
                  )
                : RefreshIndicator(
                    onRefresh: () => _loadLogs(refresh: true),
                    child: ListView.builder(
                      padding: const EdgeInsets.symmetric(horizontal: 16),
                      itemCount: _logs.length + (_hasMore ? 1 : 0),
                      itemBuilder: (ctx, i) {
                        if (i == _logs.length) {
                          return Center(
                            child: TextButton(
                              onPressed: _loading ? null : _loadLogs,
                              child: _loading ? const CircularProgressIndicator() : const Text('Cargar más'),
                            ),
                          );
                        }

                        final log = _logs[i];
                        final action = log['action'] ?? '';
                        final color = _getActionColor(action);

                        return Card(
                          margin: const EdgeInsets.only(bottom: 8),
                          child: Padding(
                            padding: const EdgeInsets.all(12),
                            child: Row(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Container(
                                  width: 36,
                                  height: 36,
                                  decoration: BoxDecoration(
                                    color: color.withValues(alpha: 0.1),
                                    borderRadius: BorderRadius.circular(18),
                                  ),
                                  child: Icon(
                                    Icons.edit,
                                    color: color,
                                    size: 18,
                                  ),
                                ),
                                const SizedBox(width: 12),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      RichText(
                                        text: TextSpan(
                                          style: GoogleFonts.dmSans(fontSize: 13),
                                          children: [
                                            TextSpan(
                                              text: log['admin_name'] ?? 'Admin',
                                              style: const TextStyle(fontWeight: FontWeight.w600),
                                            ),
                                            TextSpan(
                                              text: ' ${_formatAction(action)}',
                                              style: TextStyle(color: color),
                                            ),
                                          ],
                                        ),
                                      ),
                                      if (log['details'] != null) ...[
                                        const SizedBox(height: 4),
                                        Text(
                                          log['details'],
                                          style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textMuted),
                                        ),
                                      ],
                                      const SizedBox(height: 4),
                                      Text(
                                        _formatTimeAgo(log['created_at']),
                                        style: GoogleFonts.dmSans(fontSize: 11, color: AppColors.textMuted),
                                      ),
                                    ],
                                  ),
                                ),
                              ],
                            ),
                          ),
                        );
                      },
                    ),
                  ),
          ),
        ],
      ),
    );
  }

  String _formatTimeAgo(String? dateStr) {
    if (dateStr == null) return '';
    try {
      final date = DateTime.parse(dateStr);
      final diff = DateTime.now().difference(date);
      if (diff.inMinutes < 60) {
        return 'Hace ${diff.inMinutes} minutos';
      } else if (diff.inHours < 24) {
        return 'Hace ${diff.inHours} horas';
      } else if (diff.inDays < 7) {
        return 'Hace ${diff.inDays} días';
      } else {
        return dateStr.split('T').first;
      }
    } catch (e) {
      return dateStr;
    }
  }
}
