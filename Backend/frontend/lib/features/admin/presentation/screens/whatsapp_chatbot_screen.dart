import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:frontend/core/app_colors.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/api_client.dart';

class WhatsAppChatbotScreen extends StatefulWidget {
  const WhatsAppChatbotScreen({super.key});

  @override
  State<WhatsAppChatbotScreen> createState() => _WhatsAppChatbotScreenState();
}

class _WhatsAppChatbotScreenState extends State<WhatsAppChatbotScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  List<dynamic> _faqs = [];
  List<dynamic> _conversations = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 3, vsync: this);
    _loadData();
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  Future<void> _loadData() async {
    setState(() => _isLoading = true);
    try {
      final faqsData = await ApiClient.get('/chatbot/faqs');
      final convData = await ApiClient.get('/chatbot/conversations');
      setState(() {
        _faqs = faqsData as List<dynamic>? ?? [];
        _conversations = convData as List<dynamic>? ?? [];
      });
    } catch (e) {
      debugPrint('Error loading data: $e');
    } finally {
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);

    return Scaffold(
      backgroundColor: t.base,
      appBar: AppBar(
        backgroundColor: t.card,
        title: ShaderMask(
          shaderCallback: (bounds) =>
              AppColors.titleGradient.createShader(bounds),
          child: Text(
            'WhatsApp Chatbot',
            style: GoogleFonts.outfit(
              color: Colors.white,
              fontSize: 20,
              fontWeight: FontWeight.w700,
            ),
          ),
        ),
        leading: IconButton(
          icon: Icon(Icons.arrow_back, color: t.textPrimary),
          onPressed: () => Navigator.pop(context),
        ),
        bottom: TabBar(
          controller: _tabController,
          indicatorColor: AppColors.hotPink,
          labelColor: AppColors.hotPink,
          unselectedLabelColor: t.textMuted,
          tabs: const [
            Tab(text: 'FAQs'),
            Tab(text: 'Keywords'),
            Tab(text: 'Conversations'),
          ],
        ),
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : TabBarView(
              controller: _tabController,
              children: [
                _buildFAQsTab(t),
                _buildKeywordsTab(t),
                _buildConversationsTab(t),
              ],
            ),
      floatingActionButton: FloatingActionButton(
        backgroundColor: AppColors.hotPink,
        onPressed: () => _showAddFAQDialog(t),
        child: const Icon(Icons.add, color: Colors.white),
      ),
    );
  }

  Widget _buildFAQsTab(RfTheme t) {
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: _faqs.length,
      itemBuilder: (context, index) {
        final faq = _faqs[index];
        return Container(
          margin: const EdgeInsets.only(bottom: 12),
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: t.card,
            borderRadius: BorderRadius.circular(12),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 8,
                      vertical: 4,
                    ),
                    decoration: BoxDecoration(
                      color: AppColors.violet.withValues(alpha: 0.2),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(
                      'Keyword: ${faq['keyword']}',
                      style: TextStyle(
                        color: AppColors.violet,
                        fontSize: 12,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                  const Spacer(),
                  IconButton(
                    icon: Icon(Icons.edit, color: t.textMuted, size: 20),
                    onPressed: () => _showEditFAQDialog(faq, t),
                  ),
                  IconButton(
                    icon: Icon(
                      Icons.delete_outline,
                      color: AppColors.coral,
                      size: 20,
                    ),
                    onPressed: () => _deleteFAQ(faq['id']),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              Text(
                'Q: ${faq['question']}',
                style: GoogleFonts.outfit(
                  color: t.textPrimary,
                  fontWeight: FontWeight.w600,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                'A: ${faq['answer']}',
                style: GoogleFonts.dmSans(color: t.textMuted),
              ),
              if (faq['response_type'] != null) ...[
                const SizedBox(height: 8),
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 8,
                    vertical: 4,
                  ),
                  decoration: BoxDecoration(
                    color: t.borderFaint,
                    borderRadius: BorderRadius.circular(4),
                  ),
                  child: Text(
                    faq['response_type'],
                    style: TextStyle(color: t.textMuted, fontSize: 11),
                  ),
                ),
              ],
            ],
          ),
        );
      },
    );
  }

  Widget _buildKeywordsTab(RfTheme t) {
    final keywords = <String, List<Map<String, dynamic>>>{};
    for (final faq in _faqs) {
      final kw = faq['keyword']?.toString().toLowerCase() ?? '';
      if (!keywords.containsKey(kw)) keywords[kw] = [];
      keywords[kw]!.add(faq);
    }
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: keywords.length,
      itemBuilder: (context, index) {
        final keyword = keywords.keys.elementAt(index);
        final items = keywords[keyword]!;
        return Container(
          margin: const EdgeInsets.only(bottom: 12),
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: t.card,
            borderRadius: BorderRadius.circular(12),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 12,
                      vertical: 6,
                    ),
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        colors: [AppColors.hotPink, AppColors.violet],
                      ),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Text(
                      keyword,
                      style: GoogleFonts.outfit(
                        color: Colors.white,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Text(
                    '${items.length} responses',
                    style: TextStyle(color: t.textMuted, fontSize: 12),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              ...items.map(
                (item) => Padding(
                  padding: const EdgeInsets.only(top: 4),
                  child: Text(
                    '→ ${item['question']}',
                    style: TextStyle(color: t.textMuted, fontSize: 13),
                  ),
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildConversationsTab(RfTheme t) {
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: _conversations.length,
      itemBuilder: (context, index) {
        final conv = _conversations[index];
        return Container(
          margin: const EdgeInsets.only(bottom: 12),
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: t.card,
            borderRadius: BorderRadius.circular(12),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  CircleAvatar(
                    radius: 20,
                    backgroundColor: AppColors.teal.withValues(alpha: 0.2),
                    child: Icon(Icons.person, color: AppColors.teal),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          conv['customer_name'] ?? 'Unknown',
                          style: GoogleFonts.outfit(
                            color: t.textPrimary,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        Text(
                          conv['customer_phone'] ?? '',
                          style: TextStyle(color: t.textMuted, fontSize: 12),
                        ),
                      ],
                    ),
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 8,
                      vertical: 4,
                    ),
                    decoration: BoxDecoration(
                      color: conv['status'] == 'resolved'
                          ? Colors.green.withValues(alpha: 0.2)
                          : Colors.amber.withValues(alpha: 0.2),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(
                      conv['status'] ?? 'pending',
                      style: TextStyle(
                        color: conv['status'] == 'resolved'
                            ? Colors.green
                            : Colors.amber,
                        fontSize: 11,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              Text(
                conv['last_message'] ?? '',
                style: GoogleFonts.dmSans(color: t.textMuted),
              ),
              const SizedBox(height: 8),
              Text(
                'Started: ${_formatDate(conv['created_at'])}',
                style: TextStyle(color: t.textDim, fontSize: 11),
              ),
            ],
          ),
        );
      },
    );
  }

  void _showAddFAQDialog(RfTheme t) {
    final keywordController = TextEditingController();
    final questionController = TextEditingController();
    final answerController = TextEditingController();
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: t.card,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => Padding(
        padding: EdgeInsets.only(
          bottom: MediaQuery.of(context).viewInsets.bottom,
          left: 20,
          right: 20,
          top: 20,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Add FAQ',
              style: GoogleFonts.outfit(
                color: t.textPrimary,
                fontSize: 20,
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 16),
            RfFormField(
              label: 'Keyword',
              icon: Icons.tag,
              controller: keywordController,
              t: t,
            ),
            const SizedBox(height: 12),
            RfFormField(
              label: 'Question',
              icon: Icons.help_outline,
              controller: questionController,
              t: t,
            ),
            const SizedBox(height: 12),
            RfFormField(
              label: 'Answer',
              icon: Icons.question_answer,
              controller: answerController,
              t: t,
            ),
            const SizedBox(height: 20),
            SizedBox(
              width: double.infinity,
              child: RfLuxeButton(
                label: 'Save FAQ',
                onTap: () async {
                  await ApiClient.post('/chatbot/faqs', {
                    'keyword': keywordController.text,
                    'question': questionController.text,
                    'answer': answerController.text,
                    'response_type': 'text',
                  });
                  if (mounted) Navigator.pop(context);
                  _loadData();
                },
                t: t,
              ),
            ),
            const SizedBox(height: 20),
          ],
        ),
      ),
    );
  }

  void _showEditFAQDialog(Map<String, dynamic> faq, RfTheme t) {
    final keywordController = TextEditingController(text: faq['keyword']);
    final questionController = TextEditingController(text: faq['question']);
    final answerController = TextEditingController(text: faq['answer']);
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: t.card,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => Padding(
        padding: EdgeInsets.only(
          bottom: MediaQuery.of(context).viewInsets.bottom,
          left: 20,
          right: 20,
          top: 20,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Edit FAQ',
              style: GoogleFonts.outfit(
                color: t.textPrimary,
                fontSize: 20,
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 16),
            RfFormField(
              label: 'Keyword',
              icon: Icons.tag,
              controller: keywordController,
              t: t,
            ),
            const SizedBox(height: 12),
            RfFormField(
              label: 'Question',
              icon: Icons.help_outline,
              controller: questionController,
              t: t,
            ),
            const SizedBox(height: 12),
            RfFormField(
              label: 'Answer',
              icon: Icons.question_answer,
              controller: answerController,
              t: t,
            ),
            const SizedBox(height: 20),
            SizedBox(
              width: double.infinity,
              child: RfLuxeButton(
                label: 'Update FAQ',
                onTap: () async {
                  await ApiClient.put('/chatbot/faqs/${faq['id']}', {
                    'keyword': keywordController.text,
                    'question': questionController.text,
                    'answer': answerController.text,
                  });
                  if (mounted) Navigator.pop(context);
                  _loadData();
                },
                t: t,
              ),
            ),
            const SizedBox(height: 20),
          ],
        ),
      ),
    );
  }

  Future<void> _deleteFAQ(int id) async {
    await ApiClient.delete('/chatbot/faqs/$id');
    _loadData();
  }

  String _formatDate(String? dateStr) {
    if (dateStr == null) return '';
    try {
      final date = DateTime.parse(dateStr);
      return '${date.day}/${date.month}/${date.year} ${date.hour}:${date.minute.toString().padLeft(2, '0')}';
    } catch (_) {
      return dateStr;
    }
  }
}
