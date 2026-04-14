import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import '../../../../core/design_system.dart';
import '../../../../core/app_colors.dart';
import '../../data/event_model.dart';
import '../../data/message_model.dart';
import '../chat_provider.dart';
import '../../../auth/presentation/auth_provider.dart';

class EventChatScreen extends StatefulWidget {
  final Event event;

  const EventChatScreen({super.key, required this.event});

  @override
  State<EventChatScreen> createState() => _EventChatScreenState();
}

class _EventChatScreenState extends State<EventChatScreen> {
  final TextEditingController _messageController = TextEditingController();
  final ScrollController _scrollController = ScrollController();
  final FocusNode _inputFocusNode = FocusNode();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ChatProvider>().connect(widget.event.id);
    });
  }

  @override
  void dispose() {
    _messageController.dispose();
    _scrollController.dispose();
    _inputFocusNode.dispose();
    super.dispose();
  }

  void _scrollToBottom({bool animated = true}) {
    if (!_scrollController.hasClients) return;
    final maxScroll = _scrollController.position.maxScrollExtent;
    if (maxScroll <= 0) return;
    if (animated) {
      _scrollController.animateTo(
        maxScroll + 100,
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeOut,
      );
    } else {
      _scrollController.jumpTo(maxScroll + 100);
    }
  }

  Future<void> _loadOlderMessages() async {
    final provider = context.read<ChatProvider>();
    if (provider.isLoadingOlderMessages) return;
    await provider.loadOlderMessages();
    // Scroll back to where we were (don't jump to bottom after loading older)
    if (_scrollController.hasClients) {
      _scrollController.jumpTo(_scrollController.position.pixels);
    }
  }

  void _sendMessage() {
    final content = _messageController.text.trim();
    if (content.isEmpty) return;
    context.read<ChatProvider>().sendMessage(content);
    _messageController.clear();
    _inputFocusNode.requestFocus();
    WidgetsBinding.instance.addPostFrameCallback((_) => _scrollToBottom());
  }

  // ── Date separator logic ───────────────────────────────────────────────────

  String _dateSeparatorLabel(DateTime date) {
    final now = DateTime.now();
    final today = DateTime(now.year, now.month, now.day);
    final yesterday = today.subtract(const Duration(days: 1));
    final msgDate = DateTime(date.year, date.month, date.day);
    if (msgDate == today) return 'Hoy';
    if (msgDate == yesterday) return 'Ayer';
    return DateFormat('d MMM yyyy').format(date);
  }

  bool _needsDateSeparator(int index, List<EventMessage> messages) {
    if (index == 0) return true;
    final prev = messages[index - 1].createdAt;
    final curr = messages[index].createdAt;
    return DateTime(prev.year, prev.month, prev.day) !=
        DateTime(curr.year, curr.month, curr.day);
  }

  // ── Avatar helper ─────────────────────────────────────────────────────────

  String _initials(String? name) {
    if (name == null || name.isEmpty) return '?';
    final parts = name.trim().split(' ');
    if (parts.length >= 2) {
      return '${parts[0][0]}${parts[1][0]}'.toUpperCase();
    }
    return name[0].toUpperCase();
  }

  Color _avatarColor(String? name, RfTheme t) {
    final colors = [
      AppColors.hotPink,
      AppColors.violet,
      AppColors.teal,
      AppColors.amber,
      AppColors.coral,
      AppColors.sky,
    ];
    final idx = (name ?? '').isEmpty ? 0 : name!.codeUnitAt(0) % colors.length;
    return colors[idx];
  }

  // ── Build ─────────────────────────────────────────────────────────────────

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);
    final isDark = context.watch<ThemeProvider>().isDark;
    final authProvider = context.watch<AuthProvider>();
    final currentUserId = authProvider.user?.id;

    return Scaffold(
      backgroundColor: t.base,
      body: Stack(
        children: [
          // Subtle ambient gradient orbs
          Positioned(
            top: -100,
            right: -80,
            child: Container(
              width: 260,
              height: 260,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                gradient: RadialGradient(colors: [
                  AppColors.violet.withValues(alpha: isDark ? 0.15 : 0.08),
                  Colors.transparent,
                ]),
              ),
            ),
          ),
          Positioned(
            bottom: -60,
            left: -60,
            child: Container(
              width: 200,
              height: 200,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                gradient: RadialGradient(colors: [
                  AppColors.hotPink.withValues(alpha: isDark ? 0.12 : 0.06),
                  Colors.transparent,
                ]),
              ),
            ),
          ),
          SafeArea(
            child: Column(
              children: [
                _buildHeader(t),
                // Reconnecting banner
                Consumer<ChatProvider>(
                  builder: (context, chat, _) {
                    if (!chat.isConnected &&
                        chat.messages.isNotEmpty &&
                        chat.error == null) {
                      return _buildReconnectingBanner(t);
                    }
                    return const SizedBox.shrink();
                  },
                ),
                // Message list
                Expanded(
                  child: Consumer<ChatProvider>(
                    builder: (context, chat, _) {
                      if (chat.error != null && chat.messages.isEmpty) {
                        return Center(
                          child: Column(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Icon(Icons.error_outline_rounded,
                                  color: AppColors.coral, size: 48),
                              const SizedBox(height: 12),
                              Text(chat.error!,
                                  style: GoogleFonts.dmSans(
                                      color: t.textMuted, fontSize: 14)),
                            ],
                          ),
                        );
                      }

                      if (chat.messages.isEmpty && !chat.isConnected) {
                        return Center(
                          child: Column(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Icon(Icons.chat_bubble_outline_rounded,
                                  color: t.textDim, size: 48),
                              const SizedBox(height: 12),
                              Text('Conectando...',
                                  style: GoogleFonts.dmSans(
                                      color: t.textMuted, fontSize: 14)),
                            ],
                          ),
                        );
                      }

                      WidgetsBinding.instance
                          .addPostFrameCallback((_) => _scrollToBottom());

                      return NotificationListener<ScrollNotification>(
                        onNotification: (notification) {
                          if (notification is ScrollEndNotification &&
                              _scrollController.position.pixels <= 0) {
                            _loadOlderMessages();
                          }
                          return false;
                        },
                        child: ListView.builder(
                          controller: _scrollController,
                          padding: const EdgeInsets.symmetric(
                              horizontal: 16, vertical: 8),
                          itemCount: chat.messages.length,
                          itemBuilder: (context, index) {
                            final message = chat.messages[index];
                            final isMe = message.senderId == currentUserId;

                            // Date separator
                            final showDateSep =
                                _needsDateSeparator(index, chat.messages);

                            return Column(
                              children: [
                                if (showDateSep)
                                  _buildDateSeparator(message.createdAt, t),
                                _buildMessageBubble(
                                    message, isMe, chat, index, t),
                              ],
                            );
                          },
                        ),
                      );
                    },
                  ),
                ),
                // Typing indicator
                Consumer<ChatProvider>(
                  builder: (context, chat, _) {
                    if (chat.isTyping) return _buildTypingIndicator(t);
                    return const SizedBox.shrink();
                  },
                ),
                _buildMessageInput(t),
              ],
            ),
          ),
        ],
      ),
    );
  }

  // ── Header ────────────────────────────────────────────────────────────────

  Widget _buildHeader(RfTheme t) {
    return Container(
      padding: const EdgeInsets.fromLTRB(8, 8, 8, 8),
      decoration: BoxDecoration(
        color: t.card,
        border: Border(bottom: BorderSide(color: t.borderFaint)),
      ),
      child: Row(
        children: [
          GestureDetector(
            onTap: () => Navigator.pop(context),
            child: Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                color: t.isDark ? t.base : Colors.white,
                borderRadius: BorderRadius.circular(14),
                border: Border.all(color: t.borderFaint),
              ),
              child: Icon(Icons.arrow_back_rounded,
                  color: t.textPrimary, size: 22),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  widget.event.name,
                  style: GoogleFonts.outfit(
                    fontSize: 16,
                    fontWeight: FontWeight.w700,
                    color: t.textPrimary,
                  ),
                ),
                Consumer<ChatProvider>(
                  builder: (context, chat, _) {
                    return Row(
                      children: [
                        Container(
                          width: 8,
                          height: 8,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            color:
                                chat.isConnected ? AppColors.teal : AppColors.coral,
                          ),
                        ),
                        const SizedBox(width: 6),
                        Text(
                          chat.isConnected ? 'En linea' : 'Desconectado',
                          style: GoogleFonts.dmSans(
                            fontSize: 12,
                            color: t.textMuted,
                          ),
                        ),
                      ],
                    );
                  },
                ),
              ],
            ),
          ),
          Consumer<ChatProvider>(
            builder: (context, chat, _) {
              return Container(
                width: 44,
                height: 44,
                decoration: BoxDecoration(
                  color: t.isDark ? t.base : Colors.white,
                  borderRadius: BorderRadius.circular(14),
                  border: Border.all(color: t.borderFaint),
                ),
                child: Icon(
                  Icons.more_vert_rounded,
                  color: t.textPrimary,
                  size: 22,
                ),
              );
            },
          ),
        ],
      ),
    );
  }

  // ── Reconnecting banner ──────────────────────────────────────────────────

  Widget _buildReconnectingBanner(RfTheme t) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(vertical: 10, horizontal: 16),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            AppColors.amber.withValues(alpha: 0.15),
            AppColors.coral.withValues(alpha: 0.1),
          ],
        ),
        border: Border(bottom: BorderSide(color: t.borderFaint)),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          SizedBox(
            width: 16,
            height: 16,
            child: CircularProgressIndicator(
              strokeWidth: 2,
              color: AppColors.amber,
            ),
          ),
          const SizedBox(width: 10),
          Text(
            'Reconectando...',
            style: GoogleFonts.dmSans(
              fontSize: 13,
              fontWeight: FontWeight.w600,
              color: AppColors.amber,
            ),
          ),
        ],
      ),
    );
  }

  // ── Date separator ───────────────────────────────────────────────────────

  Widget _buildDateSeparator(DateTime date, RfTheme t) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 16),
      child: Row(
        children: [
          Expanded(child: Divider(color: t.borderFaint)),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Text(
              _dateSeparatorLabel(date),
              style: GoogleFonts.dmSans(
                fontSize: 12,
                fontWeight: FontWeight.w600,
                color: t.textDim,
              ),
            ),
          ),
          Expanded(child: Divider(color: t.borderFaint)),
        ],
      ),
    );
  }

  // ── Message bubble ───────────────────────────────────────────────────────

  Widget _buildMessageBubble(
    EventMessage message,
    bool isMe,
    ChatProvider chat,
    int index,
    RfTheme t,
  ) {
    // Check if we should show sender avatar (first in a group of messages from same sender)
    final messages = chat.messages;
    final showAvatar = !isMe &&
        (index == 0 ||
            messages[index - 1].senderId != message.senderId ||
            _needsDateSeparator(index, messages));

    // Check if we should show sender name (only when avatar is shown)
    final showSenderName = showAvatar && message.senderName != null;

    // Read status for own messages
    final isRead = isMe && index == messages.length - 1 && chat.isConnected;

    return Padding(
      padding: EdgeInsets.only(bottom: showAvatar ? 12 : 2),
      child: Row(
        mainAxisAlignment:
            isMe ? MainAxisAlignment.end : MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.end,
        children: [
          if (!isMe) ...[
            // Avatar
            if (showAvatar)
              _buildAvatar(message.senderName, t)
            else
              const SizedBox(width: 36),
            const SizedBox(width: 8),
          ],
          Flexible(
            child: Column(
              crossAxisAlignment:
                  isMe ? CrossAxisAlignment.end : CrossAxisAlignment.start,
              children: [
                if (showSenderName == true)
                  Padding(
                    padding: const EdgeInsets.only(left: 4, bottom: 4),
                    child: Text(
                      message.senderName!,
                      style: GoogleFonts.dmSans(
                        fontSize: 12,
                        fontWeight: FontWeight.w700,
                        color: _avatarColor(message.senderName, t),
                      ),
                    ),
                  ),
                Container(
                  constraints: BoxConstraints(
                      maxWidth: MediaQuery.of(context).size.width * 0.68),
                  padding:
                      const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                  decoration: BoxDecoration(
                    gradient: isMe
                        ? const LinearGradient(
                            colors: [AppColors.hotPink, AppColors.violet],
                            begin: Alignment.topLeft,
                            end: Alignment.bottomRight,
                          )
                        : null,
                    color: isMe
                        ? null
                        : (t.isDark
                            ? t.card
                            : Colors.white),
                    borderRadius: BorderRadius.only(
                      topLeft: const Radius.circular(20),
                      topRight: const Radius.circular(20),
                      bottomLeft: Radius.circular(isMe ? 20 : 4),
                      bottomRight: Radius.circular(isMe ? 4 : 20),
                    ),
                    border: isMe
                        ? null
                        : Border.all(color: t.borderFaint),
                    boxShadow: [
                      BoxShadow(
                        color: Colors.black.withValues(alpha: isMe ? 0.15 : 0.05),
                        blurRadius: 8,
                        offset: const Offset(0, 2),
                      ),
                    ],
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      Text(
                        message.content,
                        style: GoogleFonts.dmSans(
                          fontSize: 15,
                          fontWeight: FontWeight.w500,
                          color: isMe ? Colors.white : t.textPrimary,
                          height: 1.35,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(
                            DateFormat('HH:mm').format(message.createdAt),
                            style: GoogleFonts.dmSans(
                              fontSize: 11,
                              color: isMe
                                  ? Colors.white.withValues(alpha: 0.65)
                                  : t.textDim,
                            ),
                          ),
                          if (isMe) ...[
                            const SizedBox(width: 4),
                            Icon(
                              isRead
                                  ? Icons.done_all_rounded
                                  : Icons.done_rounded,
                              size: 14,
                              color: isRead
                                  ? AppColors.teal
                                  : Colors.white.withValues(alpha: 0.55),
                            ),
                          ],
                        ],
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAvatar(String? senderName, RfTheme t) {
    return Container(
      width: 36,
      height: 36,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        gradient: LinearGradient(
          colors: [
            _avatarColor(senderName, t),
            _avatarColor(senderName, t).withValues(alpha: 0.7),
          ],
        ),
      ),
      alignment: Alignment.center,
      child: Text(
        _initials(senderName),
        style: GoogleFonts.dmSans(
          fontSize: 13,
          fontWeight: FontWeight.w800,
          color: Colors.white,
        ),
      ),
    );
  }

  // ── Typing indicator ─────────────────────────────────────────────────────

  Widget _buildTypingIndicator(RfTheme t) {
    return Padding(
      padding: const EdgeInsets.only(left: 16, bottom: 8),
      child: Row(
        children: [
          Container(
            width: 36,
            height: 36,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: t.card,
              border: Border.all(color: t.borderFaint),
            ),
            alignment: Alignment.center,
            child: Icon(Icons.chat_bubble_rounded,
                color: t.textDim, size: 16),
          ),
          const SizedBox(width: 10),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
            decoration: BoxDecoration(
              color: t.card,
              borderRadius:
                  const BorderRadius.only(topLeft: Radius.circular(20), topRight: Radius.circular(20), bottomLeft: Radius.circular(4), bottomRight: Radius.circular(20)),
              border: Border.all(color: t.borderFaint),
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                _typingDot(0),
                const SizedBox(width: 4),
                _typingDot(1),
                const SizedBox(width: 4),
                _typingDot(2),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _typingDot(int index) {
    return TweenAnimationBuilder<double>(
      tween: Tween(begin: 0.3, end: 1.0),
      duration: Duration(milliseconds: 600 + index * 200),
      builder: (context, value, child) {
        return Container(
          width: 6,
          height: 6,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: AppColors.hotPink.withValues(alpha: value),
          ),
        );
      },
    );
  }

  // ── Message input ────────────────────────────────────────────────────────

  Widget _buildMessageInput(RfTheme t) {
    return Container(
      padding: EdgeInsets.fromLTRB(
        12,
        10,
        12,
        MediaQuery.of(context).padding.bottom + 10,
      ),
      decoration: BoxDecoration(
        color: t.card,
        border: Border(top: BorderSide(color: t.borderFaint)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.04),
            blurRadius: 8,
            offset: const Offset(0, -2),
          ),
        ],
      ),
      child: Row(
        children: [
          // Emoji / attachment button (decorative)
          Container(
            width: 42,
            height: 42,
            decoration: BoxDecoration(
              color: t.isDark
                  ? Colors.white.withValues(alpha: 0.04)
                  : const Color(0xFFF3F4F6),
              borderRadius: BorderRadius.circular(14),
            ),
            child: Icon(Icons.sentiment_satisfied_alt_rounded,
                color: t.textDim, size: 22),
          ),
          const SizedBox(width: 8),
          // Text input
          Expanded(
            child: Container(
              height: 44,
              decoration: BoxDecoration(
                color: t.isDark
                    ? Colors.white.withValues(alpha: 0.04)
                    : const Color(0xFFF8F6FF),
                borderRadius: BorderRadius.circular(22),
                border: Border.all(color: t.borderFaint),
              ),
              child: TextField(
                controller: _messageController,
                focusNode: _inputFocusNode,
                style: GoogleFonts.dmSans(
                  fontSize: 15,
                  color: t.textPrimary,
                ),
                decoration: InputDecoration(
                  hintText: 'Escribe un mensaje...',
                  hintStyle: GoogleFonts.dmSans(
                    fontSize: 14,
                    color: t.textDim,
                  ),
                  border: InputBorder.none,
                  contentPadding: const EdgeInsets.symmetric(
                    horizontal: 18,
                    vertical: 12,
                  ),
                ),
                onSubmitted: (_) => _sendMessage(),
                textInputAction: TextInputAction.send,
              ),
            ),
          ),
          const SizedBox(width: 8),
          // Send button
          GestureDetector(
            onTap: _sendMessage,
            child: Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                  colors: [AppColors.hotPink, AppColors.violet],
                ),
                borderRadius: BorderRadius.circular(14),
                boxShadow: [
                  BoxShadow(
                    color: AppColors.hotPink.withValues(alpha: 0.35),
                    blurRadius: 12,
                    offset: const Offset(0, 4),
                  ),
                ],
              ),
              child: const Icon(Icons.send_rounded, color: Colors.white, size: 20),
            ),
          ),
        ],
      ),
    );
  }
}