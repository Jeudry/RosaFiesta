import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../data/message_model.dart';
import '../events_provider.dart';
import '../../../../features/auth/presentation/auth_provider.dart';

class QuotationChatWidget extends StatefulWidget {
  final String eventId;

  const QuotationChatWidget({super.key, required this.eventId});

  @override
  State<QuotationChatWidget> createState() => _QuotationChatWidgetState();
}

class _QuotationChatWidgetState extends State<QuotationChatWidget> {
  final TextEditingController _controller = TextEditingController();
  final ScrollController _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<EventsProvider>().fetchMessages(widget.eventId);
    });
  }

  void _scrollToBottom() {
    if (_scrollController.hasClients) {
      _scrollController.animateTo(
        _scrollController.position.maxScrollExtent,
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeOut,
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final currentUserId = context.read<AuthProvider>().currentUser?.id;

    return Column(
      children: [
        Expanded(
          child: Consumer<EventsProvider>(
            builder: (context, provider, child) {
              if (provider.isLoading && provider.messages.isEmpty) {
                return const Center(child: CircularProgressIndicator());
              }

              if (provider.messages.isEmpty) {
                return const Center(child: Text('No hay mensajes aún. ¡Inicia la conversación!'));
              }

              WidgetsBinding.instance.addPostFrameCallback((_) => _scrollToBottom());

              return ListView.builder(
                controller: _scrollController,
                padding: const EdgeInsets.all(16),
                itemCount: provider.messages.length,
                itemBuilder: (context, index) {
                  final msg = provider.messages[index];
                  final isMe = msg.senderId == currentUserId;

                  return Align(
                    alignment: isMe ? Alignment.centerRight : Alignment.centerLeft,
                    child: Container(
                      margin: const EdgeInsets.symmetric(vertical: 4),
                      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      decoration: BoxDecoration(
                        color: isMe ? Theme.of(context).primaryColor : Colors.grey[200],
                        borderRadius: BorderRadius.circular(12).copyWith(
                          bottomRight: isMe ? Radius.zero : null,
                          bottomLeft: !isMe ? Radius.zero : null,
                        ),
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          if (!isMe)
                            Text(
                              msg.senderName ?? 'Usuario',
                              style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 12, color: Colors.black54),
                            ),
                          Text(
                            msg.content,
                            style: TextStyle(color: isMe ? Colors.white : Colors.black87),
                          ),
                          Text(
                            timeago(msg.createdAt),
                            style: TextStyle(fontSize: 10, color: isMe ? Colors.white70 : Colors.black54),
                          ),
                        ],
                      ),
                    ),
                  );
                },
              );
            },
          ),
        ),
        Padding(
          padding: const EdgeInsets.all(8.0),
          child: Row(
            children: [
              Expanded(
                child: TextField(
                  controller: _controller,
                  decoration: const InputDecoration(
                    hintText: 'Escribe un mensaje...',
                    border: OutlineInputBorder(),
                  ),
                ),
              ),
              IconButton(
                icon: const Icon(Icons.send),
                onPressed: () async {
                  if (_controller.text.trim().isEmpty) return;
                  final content = _controller.text;
                  _controller.clear();
                  await context.read<EventsProvider>().sendMessage(widget.eventId, content);
                },
              ),
            ],
          ),
        ),
      ],
    );
  }

  String timeago(DateTime date) {
    final diff = DateTime.now().difference(date);
    if (diff.inMinutes < 1) return 'ahora';
    if (diff.inMinutes < 60) return 'hace ${diff.inMinutes}m';
    if (diff.inHours < 24) return 'hace ${diff.inHours}h';
    return '${date.day}/${date.month}';
  }
}
