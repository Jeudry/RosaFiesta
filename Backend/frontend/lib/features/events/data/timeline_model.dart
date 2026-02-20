import 'package:hive/hive.dart';

part 'timeline_model.g.dart';

@HiveType(typeId: 1)
class TimelineItem {
  @HiveField(0)
  final String id;
  @HiveField(1)
  final String eventId;
  @HiveField(2)
  final String title;
  @HiveField(3)
  final String description;
  @HiveField(4)
  final DateTime startTime;
  @HiveField(5)
  final DateTime endTime;
  @HiveField(6)
  final bool isCompleted;
  @HiveField(7)
  final bool isCritical;
  @HiveField(8)
  final DateTime createdAt;
  @HiveField(9)
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
