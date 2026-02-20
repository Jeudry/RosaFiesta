import 'package:hive/hive.dart';

part 'task_model.g.dart';

@HiveType(typeId: 0)
class EventTask {
  @HiveField(0)
  final String id;
  @HiveField(1)
  final String eventId;
  @HiveField(2)
  final String title;
  @HiveField(3)
  final String? description;
  @HiveField(4)
  final bool isCompleted;
  @HiveField(5)
  final DateTime? dueDate;
  @HiveField(6)
  final DateTime createdAt;
  @HiveField(7)
  final DateTime updatedAt;

  EventTask({
    required this.id,
    required this.eventId,
    required this.title,
    this.description,
    required this.isCompleted,
    this.dueDate,
    required this.createdAt,
    required this.updatedAt,
  });

  factory EventTask.fromJson(Map<String, dynamic> json) {
    return EventTask(
      id: json['id'],
      eventId: json['event_id'],
      title: json['title'],
      description: json['description'],
      isCompleted: json['is_completed'] ?? false,
      dueDate: json['due_date'] != null ? DateTime.parse(json['due_date']) : null,
      createdAt: DateTime.parse(json['created_at']),
      updatedAt: DateTime.parse(json['updated_at']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'title': title,
      'description': description,
      'is_completed': isCompleted,
      'due_date': dueDate?.toIso8601String(),
    };
  }
}
