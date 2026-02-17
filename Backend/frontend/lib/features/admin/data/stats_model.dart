class AdminStats {
  final double totalRevenue;
  final int totalEvents;
  final Map<String, double> revenueByMonth;
  final Map<String, int> eventsByStatus;

  AdminStats({
    required this.totalRevenue,
    required this.totalEvents,
    required this.revenueByMonth,
    required this.eventsByStatus,
  });

  factory AdminStats.fromJson(Map<String, dynamic> json) {
    return AdminStats(
      totalRevenue: (json['total_revenue'] as num).toDouble(),
      totalEvents: json['total_events'],
      revenueByMonth: Map<String, double>.from(json['revenue_by_month'] ?? {}),
      eventsByStatus: Map<String, int>.from(json['events_by_status'] ?? {}),
    );
  }
}
