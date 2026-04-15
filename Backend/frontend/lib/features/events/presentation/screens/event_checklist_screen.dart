import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';
import 'package:frontend/l10n/generated/app_localizations.dart';
import '../events_provider.dart';
import '../../data/event_model.dart';
import '../../../guests/presentation/screens/guest_list_screen.dart';
import 'package:hive/hive.dart';

class EventChecklistScreen extends StatefulWidget {
  final String eventId;

  const EventChecklistScreen({super.key, required this.eventId});

  static void open(BuildContext context, String eventId) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => EventChecklistScreen(eventId: eventId),
      ),
    );
  }

  @override
  State<EventChecklistScreen> createState() => _EventChecklistScreenState();
}

class _EventChecklistScreenState extends State<EventChecklistScreen> {
  final Map<String, bool> _checklistState = {};
  late Box _hiveBox;
  bool _notesExpanded = false;
  final _notesController = TextEditingController();
  Event? _event;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _hiveBox = Hive.box('checklist');
    _loadSavedState();
    _fetchEvent();
  }

  Future<void> _fetchEvent() async {
    try {
      final provider = context.read<EventsProvider>();
      final event = await provider.fetchEventDetails(widget.eventId);
      if (mounted) {
        setState(() {
          _event = event;
          _isLoading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() => _isLoading = false);
      }
    }
  }

  void _loadSavedState() {
    final saved = _hiveBox.get(widget.eventId, defaultValue: <String, bool>{});
    if (saved is Map) {
      _checklistState.addAll(Map<String, bool>.from(saved));
    }
  }

  void _saveState() {
    _hiveBox.put(widget.eventId, _checklistState);
  }

  void _toggleItem(String key, bool value) {
    setState(() {
      _checklistState[key] = value;
    });
    _saveState();
  }

  int get _completedCount =>
      _checklistState.values.where((v) => v).length;

  @override
  Widget build(BuildContext context) {
    final l = AppLocalizations.of(context)!;
    final isDark = context.watch<ThemeProvider>().isDark;
    final t = isDark ? RfTheme.dark : RfTheme.light;

    return Scaffold(
      backgroundColor: t.base,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: Icon(Icons.arrow_back_ios, color: t.textPrimary),
          onPressed: () => Navigator.pop(context),
        ),
        title: ShaderMask(
          shaderCallback: (b) => AppColors.titleGradient.createShader(b),
          child: Text(
            l.checklist,
            style: GoogleFonts.outfit(
              fontSize: 22,
              fontWeight: FontWeight.w800,
              color: Colors.white,
            ),
          ),
        ),
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _event == null
              ? Center(child: Text('Event not found', style: GoogleFonts.dmSans(color: t.textPrimary)))
              : _buildContent(l, t),
    );
  }

  Widget _buildContent(AppLocalizations l, RfTheme t) {
    final event = _event!;
    final totalItems = 5;
    final completed = _completedCount;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildProgressHeader(l, t, completed, totalItems),
          const SizedBox(height: 24),
          _buildEventInfoCard(event, l, t),
          const SizedBox(height: 24),
          _buildChecklistItem(
            key: 'rsvp',
            icon: Icons.people_outline,
            title: l.confirmGuests,
            onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => GuestListScreen(eventId: widget.eventId))),
            t: t,
            l: l,
          ),
          const SizedBox(height: 12),
          _buildChecklistItem(
            key: 'items',
            icon: Icons.inventory_2_outlined,
            title: l.reviewItems,
            onTap: () {
              Navigator.pop(context);
            },
            t: t,
            l: l,
          ),
          const SizedBox(height: 12),
          _buildChecklistItem(
            key: 'address',
            icon: Icons.location_on_outlined,
            title: l.verifyAddress,
            subtitle: event.location.isNotEmpty ? event.location : null,
            onTap: () {},
            t: t,
            l: l,
          ),
          const SizedBox(height: 12),
          _buildChecklistItem(
            key: 'space',
            icon: Icons.construction_outlined,
            title: l.prepareSpace,
            onTap: () {
              setState(() => _notesExpanded = !_notesExpanded);
            },
            t: t,
            l: l,
            expandable: true,
            notesExpanded: _notesExpanded,
            notesController: _notesController,
            onToggleExpand: () => setState(() => _notesExpanded = !_notesExpanded),
          ),
          const SizedBox(height: 12),
          if (event.remainingAmount > 0)
            _buildPaymentItem(event, l, t),
          const SizedBox(height: 32),
          if (completed == totalItems)
            _buildCelebration(t),
        ],
      ),
    );
  }

  Widget _buildProgressHeader(AppLocalizations l, RfTheme t, int completed, int total) {
    final progress = completed / total;
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            AppColors.hotPink.withValues(alpha: 0.15),
            AppColors.violet.withValues(alpha: 0.15),
          ],
        ),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: AppColors.hotPink.withValues(alpha: 0.2)),
      ),
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                l.completedItems(completed, total),
                style: GoogleFonts.dmSans(
                  fontSize: 16,
                  fontWeight: FontWeight.w600,
                  color: t.textPrimary,
                ),
              ),
              Text(
                '${(progress * 100).toInt()}%',
                style: GoogleFonts.outfit(
                  fontSize: 20,
                  fontWeight: FontWeight.w800,
                  color: AppColors.hotPink,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          ClipRRect(
            borderRadius: BorderRadius.circular(8),
            child: LinearProgressIndicator(
              value: progress,
              minHeight: 8,
              backgroundColor: t.borderFaint,
              valueColor: const AlwaysStoppedAnimation(AppColors.hotPink),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildEventInfoCard(Event event, AppLocalizations l, RfTheme t) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: t.card,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: t.borderFaint),
      ),
      child: Row(
        children: [
          Container(
            width: 56,
            height: 56,
            decoration: BoxDecoration(
              gradient: const LinearGradient(
                colors: [AppColors.hotPink, AppColors.violet],
              ),
              borderRadius: BorderRadius.circular(14),
            ),
            child: const Icon(Icons.event, color: Colors.white, size: 28),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  event.name,
                  style: GoogleFonts.outfit(
                    fontSize: 18,
                    fontWeight: FontWeight.w700,
                    color: t.textPrimary,
                  ),
                ),
                const SizedBox(height: 4),
                if (event.date != null)
                  Text(
                    '${event.date!.day}/${event.date!.month}/${event.date!.year}',
                    style: GoogleFonts.dmSans(
                      fontSize: 14,
                      color: AppColors.hotPink,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildChecklistItem({
    required String key,
    required IconData icon,
    required String title,
    String? subtitle,
    required VoidCallback onTap,
    required RfTheme t,
    required AppLocalizations l,
    bool expandable = false,
    bool notesExpanded = false,
    TextEditingController? notesController,
    VoidCallback? onToggleExpand,
  }) {
    final isChecked = _checklistState[key] ?? false;

    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: isChecked
              ? AppColors.teal.withValues(alpha: 0.1)
              : t.card,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(
            color: isChecked
                ? AppColors.teal.withValues(alpha: 0.3)
                : t.borderFaint,
          ),
        ),
        child: Column(
          children: [
            Row(
              children: [
                GestureDetector(
                  onTap: () => _toggleItem(key, !isChecked),
                  child: Container(
                    width: 28,
                    height: 28,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      color: isChecked ? AppColors.teal : Colors.transparent,
                      border: Border.all(
                        color: isChecked ? AppColors.teal : t.textDim,
                        width: 2,
                      ),
                    ),
                    child: isChecked
                        ? const Icon(Icons.check, size: 18, color: Colors.white)
                        : null,
                  ),
                ),
                const SizedBox(width: 14),
                Container(
                  width: 44,
                  height: 44,
                  decoration: BoxDecoration(
                    color: (isChecked ? AppColors.teal : AppColors.hotPink)
                        .withValues(alpha: 0.12),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Icon(
                    icon,
                    color: isChecked ? AppColors.teal : AppColors.hotPink,
                    size: 22,
                  ),
                ),
                const SizedBox(width: 14),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        title,
                        style: GoogleFonts.dmSans(
                          fontSize: 15,
                          fontWeight: FontWeight.w600,
                          color: t.textPrimary,
                          decoration:
                              isChecked ? TextDecoration.lineThrough : null,
                        ),
                      ),
                      if (subtitle != null)
                        Text(
                          subtitle,
                          style: GoogleFonts.dmSans(
                            fontSize: 13,
                            color: t.textMuted,
                          ),
                        ),
                    ],
                  ),
                ),
                if (!expandable)
                  Icon(
                    Icons.chevron_right_rounded,
                    color: t.textDim,
                    size: 24,
                  ),
                if (expandable)
                  GestureDetector(
                    onTap: onToggleExpand,
                    child: Icon(
                      notesExpanded
                          ? Icons.keyboard_arrow_up
                          : Icons.keyboard_arrow_down,
                      color: t.textDim,
                      size: 24,
                    ),
                  ),
              ],
            ),
            if (expandable && notesExpanded) ...[
              const SizedBox(height: 12),
              TextField(
                controller: notesController,
                maxLines: 3,
                decoration: InputDecoration(
                  hintText: l.notes,
                  hintStyle: GoogleFonts.dmSans(color: t.textDim),
                  filled: true,
                  fillColor: t.base,
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: BorderSide(color: t.borderFaint),
                  ),
                  enabledBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: BorderSide(color: t.borderFaint),
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildPaymentItem(Event event, AppLocalizations l, RfTheme t) {
    return GestureDetector(
      onTap: () {
        // Navigate to payment/checkout
      },
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: AppColors.amber.withValues(alpha: 0.1),
          borderRadius: BorderRadius.circular(16),
          border: Border.all(
            color: AppColors.amber.withValues(alpha: 0.3),
          ),
        ),
        child: Row(
          children: [
            Container(
              width: 28,
              height: 28,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: Colors.transparent,
                border: Border.all(color: t.textDim, width: 2),
              ),
            ),
            const SizedBox(width: 14),
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                color: AppColors.amber.withValues(alpha: 0.15),
                borderRadius: BorderRadius.circular(12),
              ),
              child: const Icon(
                Icons.payment_outlined,
                color: AppColors.amber,
                size: 22,
              ),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    l.paymentPending,
                    style: GoogleFonts.dmSans(
                      fontSize: 15,
                      fontWeight: FontWeight.w600,
                      color: t.textPrimary,
                    ),
                  ),
                  Text(
                    'RD\$${event.remainingAmount}',
                    style: GoogleFonts.outfit(
                      fontSize: 18,
                      fontWeight: FontWeight.w800,
                      color: AppColors.amber,
                    ),
                  ),
                ],
              ),
            ),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                  colors: [AppColors.amber, Color(0xFFFF8C00)],
                ),
                borderRadius: BorderRadius.circular(20),
              ),
              child: Text(
                l.payNow,
                style: GoogleFonts.dmSans(
                  fontSize: 13,
                  fontWeight: FontWeight.w700,
                  color: Colors.white,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildCelebration(RfTheme t) {
    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            AppColors.teal.withValues(alpha: 0.2),
            AppColors.hotPink.withValues(alpha: 0.1),
          ],
        ),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(Icons.celebration, color: AppColors.teal, size: 28),
          const SizedBox(width: 12),
          Text(
            '¡Todo listo!',
            style: GoogleFonts.outfit(
              fontSize: 20,
              fontWeight: FontWeight.w800,
              color: AppColors.teal,
            ),
          ),
        ],
      ),
    );
  }

  @override
  void dispose() {
    _notesController.dispose();
    super.dispose();
  }
}