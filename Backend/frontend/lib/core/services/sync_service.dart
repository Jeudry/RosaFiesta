import 'dart:async';
import 'package:connectivity_plus/connectivity_plus.dart';
import '../models/sync_action.dart';
import 'hive_service.dart';
import '../../features/tasks/data/task_model.dart';
import '../../features/events/data/event_model.dart';
import '../../features/tasks/data/tasks_repository.dart';
import '../../features/events/data/timeline_repository.dart';
import 'package:uuid/uuid.dart';

class SyncService {
  static final SyncService _instance = SyncService._internal();
  factory SyncService() => _instance;
  SyncService._internal();

  final _tasksRepo = EventTasksRepository();
  final _timelineRepo = TimelineRepository();
  final _connectivity = Connectivity();
  StreamSubscription? _connectivitySubscription;

  void init() {
    _connectivitySubscription = _connectivity.onConnectivityChanged.listen((results) {
      if (results.isNotEmpty && !results.contains(ConnectivityResult.none)) {
        sync();
      }
    });
  }

  void dispose() {
    _connectivitySubscription?.cancel();
  }

  Future<void> addAction({
    required String entityType,
    required String entityId,
    required SyncOperation operation,
    required Map<String, dynamic> payload,
  }) async {
    final action = SyncAction(
      id: const Uuid().v4(),
      entityType: entityType,
      entityId: entityId,
      operation: operation,
      payload: payload,
      createdAt: DateTime.now(),
    );

    await HiveService.syncBox.add(action);
    sync(); // Attempt sync immediately
  }

  Future<void> sync() async {
    final results = await _connectivity.checkConnectivity();
    if (results.isEmpty || results.contains(ConnectivityResult.none)) return;

    final actions = HiveService.syncBox.values.toList();
    actions.sort((a, b) => a.createdAt.compareTo(b.createdAt));

    for (var action in actions) {
      try {
        bool success = false;
        if (action.entityType == 'task') {
          success = await _syncTask(action);
        } else if (action.entityType == 'timeline') {
          success = await _syncTimeline(action);
        }

        if (success) {
          await action.delete();
        }
      } catch (e) {
        print("Sync failed for action ${action.id}: $e");
        // Keep in queue for next attempt
      }
    }
  }

  Future<bool> _syncTask(SyncAction action) async {
    try {
      switch (action.operation) {
        case SyncOperation.create:
          // We need the eventId from payload
          final eventId = HiveService.tasksBox.get(action.entityId)?.eventId;
          if (eventId == null) return true; // Orphan action
          
          final task = EventTask.fromJson(action.payload);
          await _tasksRepo.addTask(eventId, task);
          return true;
        case SyncOperation.update:
          await _tasksRepo.updateTask(action.entityId, action.payload);
          return true;
        case SyncOperation.delete:
          await _tasksRepo.deleteTask(action.entityId);
          return true;
      }
    } catch (e) {
      print("Sync error for task ${action.entityId}: $e");
      return false;
    }
  }

  Future<bool> _syncTimeline(SyncAction action) async {
    try {
      switch (action.operation) {
        case SyncOperation.create:
          final item = HiveService.timelineBox.get(action.entityId);
          if (item == null) return true;
          
          await _timelineRepo.createItem(item.eventId, action.payload);
          return true;
        case SyncOperation.update:
          await _timelineRepo.updateItem(action.entityId, action.payload);
          return true;
        case SyncOperation.delete:
          await _timelineRepo.deleteItem(action.entityId);
          return true;
      }
    } catch (e) {
      print("Sync error for timeline ${action.entityId}: $e");
      return false;
    }
  }
}
