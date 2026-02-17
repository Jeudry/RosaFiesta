import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../data/message_model.dart';
import '../chat_provider.dart';
import '../../../../features/auth/presentation/auth_provider.dart';
import 'package:intl/intl.dart';

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
      context.read<ChatProvider>().connect(widget.eventId);
    });
  }

  void _scrollToBottom() {
    if (_scrollController.hasClients) {
      _scrollController.animateTo(
        _scrollController.position.maxScrollExtent + 100,
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeOut,
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final authProvider = context.watch<AuthProvider>();
    final currentUserId = authProvider.user?.id;

    return Column(
      children: [
        Expanded(
          child: Consumer<ChatProvider>(
            builder: (context, chat, child) {
              if (chat.messages.isEmpty && !chat.isConnected) {
                return const Center(child: CircularProgressIndicator());
              }

              if (chat.messages.isEmpty) {
                return const Center(child: Text('No hay mensajes aún. ¡Inicia la conversación!'));
              }

              WidgetsBinding.instance.addPostFrameCallback((_) => _scrollToBottom());

              return ListView.builder(
                controller: _scrollController,
                padding: const EdgeInsets.all(16),
                itemCount: chat.messages.length,
                itemBuilder: (context, index) {
                  final msg = chat.messages[index];
                  final isMe = msg.senderId == currentUserId;

                  return Align(
                    alignment: isMe ? Alignment.centerRight : Alignment.centerLeft,
                    child: Container(
                      margin: const EdgeInsets.symmetric(vertical: 4),
                      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      decoration: BoxDecoration(
                        color: isMe ? Colors.pink.shade100 : Colors.grey[200],
                        borderRadius: BorderRadius.circular(12).copyWith(
                          bottomRight: isMe ? Radius.zero : null,
                          bottomLeft: !isMe ? Radius.zero : null,
                        ),
                      ),
                      child: Column(
                        crossAxisAlignment: isMe ? CrossAxisAlignment.end : CrossAxisAlignment.start,
                        children: [
                          if (!isMe)
                            Text(
                              msg.senderName ?? 'Rosa Fiesta',
                              style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 10, color: Colors.black54),
                            ),
                          Text(
                            msg.content,
                            style: const TextStyle(color: Colors.black87),
                          ),
                          Text(
                            DateFormat('HH:mm').format(msg.createdAt),
                            style: const TextStyle(fontSize: 8, color: Colors.black54),
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
        _buildInput(context),
      ],
    );
  }

  Widget _buildInput(BuildContext context) {
    final chat = context.read<ChatProvider>();
    return Container(
      padding: const EdgeInsets.all(8.0),
      decoration: BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.2),
            spreadRadius: 1,
            blurRadius: 3,
            offset: const Offset(0, -1),
          ),
        ],
      ),
      child: Row(
        children: [
          Expanded(
            child: TextField(
              controller: _controller,
              decoration: const InputDecoration(
                hintText: 'Escribe un mensaje...',
                border: InputBorder.none,
              ),
              onSubmitted: (_) => _send(chat),
            ),
          ),
          IconButton(
            icon: const Icon(Icons.send, color: Colors.pink),
            onPressed: () => _send(chat),
          ),
        ],
      ),
    );
  }

  void _send(ChatProvider chat) {
    if (_controller.text.trim().isEmpty) return;
    chat.sendMessage(_controller.text);
    _controller.clear();
  }
}
