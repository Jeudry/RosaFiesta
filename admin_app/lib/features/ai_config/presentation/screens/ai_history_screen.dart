import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import '../../../../core/design_system.dart';
import '../../../../core/router/app_router.dart';
import '../../../../core/api_client/api_client.dart';

class AIHistoryScreen extends StatefulWidget {
  const AIHistoryScreen({super.key});

  @override
  State<AIHistoryScreen> createState() => _AIHistoryScreenState();
}

class _AIHistoryScreenState extends State<AIHistoryScreen> {
  List<dynamic> _history = [];
  bool _loading = true;
  int _page = 1;
  bool _hasMore = true;

  @override
  void initState() {
    super.initState();
    _loadHistory();
  }

  Future<void> _loadHistory() async {
    if (_loading) return;
    setState(() => _loading = true);

    try {
      final response = await apiClient.getAIHistory(page: _page);
      final data = response.data['data'] ?? [];
      if (_page == 1) {
        _history = data;
      } else {
        _history.addAll(data);
      }
      _hasMore = data.length >= 20;
      _page++;
    } catch (e) {
      _history = [];
    }
    setState(() => _loading = false);
  }

  @override
  Widget build(BuildContext context) {
    return AdminScaffold(
      title: 'Historial IA',
      showBack: true,
      body: _history.isEmpty && !_loading
          ? const EmptyState(
              icon: Icons.smart_toy_outlined,
              title: 'No hay historial',
              subtitle: 'Las conversaciones con Rosa IA aparecerán aquí',
            )
          : RefreshIndicator(
              onRefresh: () async {
                _page = 1;
                await _loadHistory();
              },
              child: ListView.builder(
                padding: const EdgeInsets.all(16),
                itemCount: _history.length + (_hasMore ? 1 : 0),
                itemBuilder: (ctx, i) {
                  if (i == _history.length) {
                    return Center(
                      child: TextButton(
                        onPressed: _loadHistory,
                        child: _loading ? const CircularProgressIndicator() : const Text('Cargar más'),
                      ),
                    );
                  }
                  final item = _history[i];
                  return _ConversationCard(conversation: item);
                },
              ),
            ),
    );
  }
}

class _ConversationCard extends StatelessWidget {
  final Map<String, dynamic> conversation;

  const _ConversationCard({required this.conversation});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: () {},
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Container(
                    width: 40,
                    height: 40,
                    decoration: BoxDecoration(
                      color: AppColors.violet.withValues(alpha: 0.1),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: const Icon(Icons.smart_toy, color: AppColors.violet, size: 20),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          conversation['user_name'] ?? 'Usuario anónimo',
                          style: GoogleFonts.dmSans(fontWeight: FontWeight.w600),
                        ),
                        Text(
                          conversation['created_at']?.toString().split('T').first ?? '',
                          style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textMuted),
                        ),
                      ],
                    ),
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: conversation['status'] == 'completed'
                          ? AppColors.success.withValues(alpha: 0.1)
                          : AppColors.warning.withValues(alpha: 0.1),
                      borderRadius: BorderRadius.circular(4),
                    ),
                    child: Text(
                      conversation['status'] == 'completed' ? 'Completado' : 'En proceso',
                      style: GoogleFonts.dmSans(fontSize: 11, color: conversation['status'] == 'completed' ? AppColors.success : AppColors.warning),
                    ),
                  ),
                ],
              ),
              if (conversation['preview'] != null) ...[
                const SizedBox(height: 12),
                Text(
                  conversation['preview'],
                  style: GoogleFonts.dmSans(fontSize: 13, color: AppColors.textSecondary),
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
              const SizedBox(height: 8),
              Text(
                '${conversation['message_count'] ?? 0} mensajes',
                style: GoogleFonts.dmSans(fontSize: 11, color: AppColors.textMuted),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
