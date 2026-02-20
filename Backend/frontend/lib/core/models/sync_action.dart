import 'package:hive/hive.dart';

part 'sync_action.g.dart';

@HiveType(typeId: 2)
enum SyncOperation {
  @HiveField(0)
  create,
  @HiveField(1)
  update,
  @HiveField(2)
  delete,
}

@HiveType(typeId: 3)
class SyncAction extends HiveObject {
  @HiveField(0)
  final String id;
  @HiveField(1)
  final String entityType; // 'task' or 'timeline'
  @HiveField(2)
  final String entityId;
  @HiveField(3)
  final SyncOperation operation;
  @HiveField(4)
  final Map<String, dynamic> payload;
  @HiveField(5)
  final DateTime createdAt;

  SyncAction({
    required this.id,
    required this.entityType,
    required this.entityId,
    required this.operation,
    required this.payload,
    required this.createdAt,
  });
}
