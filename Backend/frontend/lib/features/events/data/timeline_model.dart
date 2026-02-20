class TimelineItem {
  final String id;
  final String eventId;
  final String title;
  final String description;
  final DateTime startTime;
  final DateTime endTime;
  final bool isCompleted;
  final bool isCritical;
  final DateTime createdAt;
  final DateTime updatedAt;

  TimelineItem({
    required this.id,
    required this.eventId,
    required this.title,
    required this.description,
    required this.startTime,
    required this.endTime,
    this.isCompleted = false,
    this.isCritical = false,
    required this.createdAt,
    required this.updatedAt,
  });

  factory TimelineItem.fromJson(Map<String, dynamic> json) {
    return TimelineItem(
      id: json['id'],
      eventId: json['event_id'],
      title: json['title'],
      description: json['description'] ?? '',
      startTime: DateTime.parse(json['start_time']),
      endTime: DateTime.parse(json['end_time']),
      isCompleted: json['is_completed'] ?? false,
      isCritical: json['is_critical'] ?? false,
      createdAt: DateTime.parse(json['created_at']),
      updatedAt: DateTime.parse(json['updated_at']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'event_id': eventId,
      'title': title,
      'description': description,
      'start_time': startTime.toIso8601String(),
      'end_time': endTime.toIso8601String(),
      'is_completed': isCompleted,
      'is_critical': isCritical,
    };
  }
}
