import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:frontend/core/app_colors.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/api_client.dart';

class InventoryCalendarScreen extends StatefulWidget {
  const InventoryCalendarScreen({super.key});

  @override
  State<InventoryCalendarScreen> createState() =>
      _InventoryCalendarScreenState();
}

class _InventoryCalendarScreenState extends State<InventoryCalendarScreen> {
  DateTime _selectedMonth = DateTime.now();
  Map<String, dynamic>? _availability;
  List<dynamic> _articles = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    setState(() => _isLoading = true);
    try {
      final articlesData = await ApiClient.get('/articles?limit=100&offset=0');
      setState(() => _articles = articlesData as List<dynamic>? ?? []);
      await _loadAvailability();
    } catch (e) {
      debugPrint('Error loading data: $e');
    } finally {
      setState(() => _isLoading = false);
    }
  }

  Future<void> _loadAvailability() async {
    try {
      final start = DateTime(_selectedMonth.year, _selectedMonth.month, 1);
      final end = DateTime(_selectedMonth.year, _selectedMonth.month + 1, 0);
      final startStr = start.toIso8601String().split('T')[0];
      final endStr = end.toIso8601String().split('T')[0];
      final data = await ApiClient.get(
        '/availability?start=$startStr&end=$endStr',
      );
      setState(() => _availability = data);
    } catch (e) {
      debugPrint('Error loading availability: $e');
    }
  }

  void _previousMonth() {
    setState(() {
      _selectedMonth = DateTime(_selectedMonth.year, _selectedMonth.month - 1);
    });
    _loadAvailability();
  }

  void _nextMonth() {
    setState(() {
      _selectedMonth = DateTime(_selectedMonth.year, _selectedMonth.month + 1);
    });
    _loadAvailability();
  }

  Color _getAvailabilityColor(int available, int total) {
    if (available == 0) return Colors.red;
    if (available < total * 0.3) return Colors.orange;
    if (available < total * 0.7) return Colors.amber;
    return Colors.green;
  }

  String _getMonthName(int month) {
    const months = [
      'January',
      'February',
      'March',
      'April',
      'May',
      'June',
      'July',
      'August',
      'September',
      'October',
      'November',
      'December',
    ];
    return months[month - 1];
  }

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);
    final now = DateTime.now();
    final daysInMonth = DateTime(
      _selectedMonth.year,
      _selectedMonth.month + 1,
      0,
    ).day;

    return Scaffold(
      backgroundColor: t.base,
      appBar: AppBar(
        backgroundColor: t.card,
        title: Text(
          'Inventory Calendar',
          style: GoogleFonts.outfit(
            color: t.textPrimary,
            fontSize: 20,
            fontWeight: FontWeight.w600,
          ),
        ),
        leading: IconButton(
          icon: Icon(Icons.arrow_back, color: t.textPrimary),
          onPressed: () => Navigator.pop(context),
        ),
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : Column(
              children: [
                Container(
                  padding: const EdgeInsets.all(16),
                  color: t.card,
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      IconButton(
                        icon: Icon(Icons.chevron_left, color: t.textPrimary),
                        onPressed: _previousMonth,
                      ),
                      Text(
                        '${_getMonthName(_selectedMonth.month)} ${_selectedMonth.year}',
                        style: GoogleFonts.outfit(
                          fontSize: 18,
                          fontWeight: FontWeight.w600,
                          color: t.textPrimary,
                        ),
                      ),
                      IconButton(
                        icon: Icon(Icons.chevron_right, color: t.textPrimary),
                        onPressed: _nextMonth,
                      ),
                    ],
                  ),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(vertical: 12),
                  color: t.card.withValues(alpha: 0.5),
                  child: Row(
                    children: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
                        .map(
                          (d) => Expanded(
                            child: Center(
                              child: Text(
                                d,
                                style: TextStyle(
                                  color: t.textMuted,
                                  fontSize: 12,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                            ),
                          ),
                        )
                        .toList(),
                  ),
                ),
                Expanded(
                  child: GridView.builder(
                    padding: const EdgeInsets.all(8),
                    gridDelegate:
                        const SliverGridDelegateWithFixedCrossAxisCount(
                          crossAxisCount: 7,
                          childAspectRatio: 1,
                        ),
                    itemCount: daysInMonth,
                    itemBuilder: (context, index) {
                      final day = index + 1;
                      final dateKey =
                          '${_selectedMonth.year}-${_selectedMonth.month.toString().padLeft(2, '0')}-${day.toString().padLeft(2, '0')}';
                      final dayAvail =
                          _availability?[dateKey] as Map<String, dynamic>?;
                      final available = dayAvail?['available'] ?? 0;
                      final total = dayAvail?['total'] ?? 0;
                      final isToday =
                          day == now.day &&
                          _selectedMonth.month == now.month &&
                          _selectedMonth.year == now.year;

                      return GestureDetector(
                        onTap: () => _showDayDetail(dateKey, available, total),
                        child: Container(
                          margin: const EdgeInsets.all(2),
                          decoration: BoxDecoration(
                            color: t.card,
                            borderRadius: BorderRadius.circular(8),
                            border: isToday
                                ? Border.all(color: AppColors.hotPink, width: 2)
                                : null,
                          ),
                          child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Text(
                                '$day',
                                style: TextStyle(
                                  color: t.textPrimary,
                                  fontWeight: FontWeight.w500,
                                ),
                              ),
                              if (total > 0)
                                Container(
                                  width: 24,
                                  height: 24,
                                  margin: const EdgeInsets.only(top: 4),
                                  decoration: BoxDecoration(
                                    color: _getAvailabilityColor(
                                      available,
                                      total,
                                    ),
                                    shape: BoxShape.circle,
                                  ),
                                  child: Center(
                                    child: Text(
                                      '$available',
                                      style: const TextStyle(
                                        color: Colors.white,
                                        fontSize: 10,
                                        fontWeight: FontWeight.bold,
                                      ),
                                    ),
                                  ),
                                ),
                            ],
                          ),
                        ),
                      );
                    },
                  ),
                ),
                Container(
                  padding: const EdgeInsets.all(16),
                  color: t.card,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Legend',
                        style: GoogleFonts.outfit(
                          color: t.textPrimary,
                          fontSize: 16,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                      const SizedBox(height: 8),
                      Row(
                        children: [
                          _legendItem(Colors.green, 'High', t),
                          _legendItem(Colors.amber, 'Medium', t),
                          _legendItem(Colors.orange, 'Low', t),
                          _legendItem(Colors.red, 'None', t),
                        ],
                      ),
                    ],
                  ),
                ),
              ],
            ),
    );
  }

  Widget _legendItem(Color color, String label, RfTheme t) {
    return Padding(
      padding: const EdgeInsets.only(right: 16),
      child: Row(
        children: [
          Container(
            width: 16,
            height: 16,
            decoration: BoxDecoration(color: color, shape: BoxShape.circle),
          ),
          const SizedBox(width: 4),
          Text(label, style: TextStyle(color: t.textMuted, fontSize: 12)),
        ],
      ),
    );
  }

  void _showDayDetail(String date, int available, int total) {
    final t = RfTheme.of(context);
    showModalBottomSheet(
      context: context,
      backgroundColor: t.card,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Availability: $date',
              style: GoogleFonts.outfit(
                color: t.textPrimary,
                fontSize: 18,
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 16),
            Text(
              'Available: $available / $total items',
              style: GoogleFonts.dmSans(color: t.textMuted, fontSize: 16),
            ),
            const SizedBox(height: 8),
            LinearProgressIndicator(
              value: total > 0 ? available / total : 0,
              backgroundColor: t.borderFaint,
              valueColor: AlwaysStoppedAnimation(
                _getAvailabilityColor(available, total),
              ),
            ),
            const SizedBox(height: 20),
            SizedBox(
              width: double.infinity,
              child: RfLuxeButton(
                label: 'View Articles',
                onTap: () => Navigator.pop(context),
                t: t,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
