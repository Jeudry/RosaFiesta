import 'package:flutter/material.dart';
import '../data/timeline_model.dart';
import '../data/timeline_repository.dart';
import '../../../core/services/hive_service.dart';
import '../../../core/services/sync_service.dart';
import '../../../core/models/sync_action.dart';

class TimelineProvider with ChangeNotifier {
  final TimelineRepository repository;
  final SyncService _syncService = SyncService();
  
  List<TimelineItem> _items = [];
  bool _isLoading = false;
  String? _error;

  TimelineProvider(this.repository);

  List<TimelineItem> get items => _items;
  bool get isLoading => _isLoading;
  String? get error => _error;

  Future<void> fetchTimeline(String eventId) async {
    _isLoading = true;
    _error = null;
    
    // Optimistic/Offline load
    _loadFromHive(eventId);
    notifyListeners();

    try {
      final fetchedItems = await repository.getTimeline(eventId);
      _items = fetchedItems;
      await _saveToHive(eventId, fetchedItems);
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  void _loadFromHive(String eventId) {
    _items = HiveService.timelineBox.values
        .where((i) => i.eventId == eventId)
        .toList();
    _items.sort((a, b) => a.startTime.compareTo(b.startTime));
  }

  Future<void> _saveToHive(String eventId, List<TimelineItem> items) async {
    final keysToDelete = HiveService.timelineBox.keys
        .where((key) => HiveService.timelineBox.get(key)?.eventId == eventId);
    await HiveService.timelineBox.deleteAll(keysToDelete);
    
    for (var item in items) {
      await HiveService.timelineBox.put(item.id, item);
    }
  }

  Future<void> addItem(String eventId, String title, String description, DateTime start, DateTime end, {bool isCritical = false}) async {
    _isLoading = true;
    notifyListeners();

    try {
      // For now, we need to create a dummy item to show locally
      // In a real app, we'd generate a temporary ID
      final tempId = 'temp_${DateTime.now().millisecondsSinceEpoch}';
      final newItem = TimelineItem(
        id: tempId,
        eventId: eventId,
        title: title,
        description: description,
        startTime: start,
        endTime: end,
        isCritical: isCritical,
        createdAt: DateTime.now(),
        updatedAt: DateTime.now(),
      );

      _items.add(newItem);
      _items.sort((a, b) => a.startTime.compareTo(b.startTime));
      await HiveService.timelineBox.put(newItem.id, newItem);

      await _syncService.addAction(
        entityType: 'timeline',
        entityId: tempId,
        operation: SyncOperation.create,
        payload: {
          'title': title,
          'description': description,
          'start_time': start.toIso8601String(),
          'end_time': end.toIso8601String(),
          'is_critical': isCritical,
        },
      );
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> toggleTimelineCompletion(TimelineItem item, bool isCompleted) async {
    try {
      // Optimistic update
      final index = _items.indexWhere((i) => i.id == item.id);
      if (index != -1) {
        final updated = TimelineItem(
          id: item.id,
          eventId: item.eventId,
          title: item.title,
          description: item.description,
          startTime: item.startTime,
          endTime: item.endTime,
          isCompleted: isCompleted,
          isCritical: item.isCritical,
          createdAt: item.createdAt,
          updatedAt: DateTime.now(),
        );
        _items[index] = updated;
        await HiveService.timelineBox.put(updated.id, updated);
        notifyListeners();
      }

      await _syncService.addAction(
        entityType: 'timeline',
        entityId: item.id,
        operation: SyncOperation.update,
        payload: {'is_completed': isCompleted},
      );
    } catch (e) {
      _error = e.toString();
      notifyListeners();
    }
  }

  Future<void> deleteItem(String itemId) async {
    try {
      _items.removeWhere((i) => i.id == itemId);
      await HiveService.timelineBox.delete(itemId);
      notifyListeners();

      await _syncService.addAction(
        entityType: 'timeline',
        entityId: itemId,
        operation: SyncOperation.delete,
        payload: {},
      );
    } catch (e) {
      _error = e.toString();
      notifyListeners();
    }
  }
  
  Future<void> updateItem(String itemId, String title, String description, DateTime start, DateTime end, {bool? isCritical}) async {
    try {
      final index = _items.indexWhere((i) => i.id == itemId);
      if (index != -1) {
        final item = _items[index];
        final updated = TimelineItem(
          id: item.id,
          eventId: item.eventId,
          title: title,
          description: description,
          startTime: start,
          endTime: end,
          isCompleted: item.isCompleted,
          isCritical: isCritical ?? item.isCritical,
          createdAt: item.createdAt,
          updatedAt: DateTime.now(),
        );
        _items[index] = updated;
        _items.sort((a, b) => a.startTime.compareTo(b.startTime));
        await HiveService.timelineBox.put(updated.id, updated);
        notifyListeners();
      }

      await _syncService.addAction(
        entityType: 'timeline',
        entityId: itemId,
        operation: SyncOperation.update,
        payload: {
          'title': title,
          'description': description,
          'start_time': start.toIso8601String(),
          'end_time': end.toIso8601String(),
          'is_critical': isCritical ?? _items.firstWhere((i) => i.id == itemId).isCritical,
        },
      );
    } catch (e) {
      _error = e.toString();
      notifyListeners();
    }
  }

  Future<void> toggleTimelineCritical(TimelineItem item, bool isCritical) async {
    try {
      final index = _items.indexWhere((i) => i.id == item.id);
      if (index != -1) {
        final updated = TimelineItem(
          id: item.id,
          eventId: item.eventId,
          title: item.title,
          description: item.description,
          startTime: item.startTime,
          endTime: item.endTime,
          isCompleted: item.isCompleted,
          isCritical: isCritical,
          createdAt: item.createdAt,
          updatedAt: DateTime.now(),
        );
        _items[index] = updated;
        await HiveService.timelineBox.put(updated.id, updated);
        notifyListeners();
      }

      await _syncService.addAction(
        entityType: 'timeline',
        entityId: item.id,
        operation: SyncOperation.update,
        payload: {'is_critical': isCritical},
      );
    } catch (e) {
      _error = e.toString();
      notifyListeners();
    }
  }
}
