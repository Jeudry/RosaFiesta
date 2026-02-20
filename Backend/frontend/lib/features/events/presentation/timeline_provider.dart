import 'package:flutter/material.dart';
import '../data/timeline_model.dart';
import '../data/timeline_repository.dart';

class TimelineProvider with ChangeNotifier {
  final TimelineRepository repository;
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
    notifyListeners();

    try {
      _items = await repository.getTimeline(eventId);
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> addItem(String eventId, String title, String description, DateTime start, DateTime end, {bool isCritical = false}) async {
    _isLoading = true;
    notifyListeners();

    try {
      final newItem = await repository.createItem(eventId, {
        'title': title,
        'description': description,
        'start_time': start.toIso8601String(),
        'end_time': end.toIso8601String(),
        'is_critical': isCritical,
      });
      _items.add(newItem);
      _items.sort((a, b) => a.startTime.compareTo(b.startTime));
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> updateItem(String itemId, String title, String description, DateTime start, DateTime end, {bool? isCritical}) async {
    _isLoading = true;
    notifyListeners();

    try {
      final updated = await repository.updateItem(itemId, {
        'title': title,
        'description': description,
        'start_time': start.toIso8601String(),
        'end_time': end.toIso8601String(),
        'is_completed': _items.firstWhere((i) => i.id == itemId).isCompleted,
        'is_critical': isCritical ?? _items.firstWhere((i) => i.id == itemId).isCritical,
      });
      final index = _items.indexWhere((i) => i.id == itemId);
      if (index != -1) {
        _items[index] = updated;
        _items.sort((a, b) => a.startTime.compareTo(b.startTime));
      }
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> toggleTimelineCompletion(TimelineItem item, bool isCompleted) async {
    try {
      final updated = await repository.updateItem(item.id, {
        ...item.toJson(),
        'is_completed': isCompleted,
      });
      final index = _items.indexWhere((i) => i.id == item.id);
      if (index != -1) {
        _items[index] = updated;
        notifyListeners();
      }
    } catch (e) {
      _error = e.toString();
      notifyListeners();
    }
  }

  Future<void> toggleTimelineCritical(TimelineItem item, bool isCritical) async {
    try {
      final updated = await repository.updateItem(item.id, {
        ...item.toJson(),
        'is_critical': isCritical,
      });
      final index = _items.indexWhere((i) => i.id == item.id);
      if (index != -1) {
        _items[index] = updated;
        notifyListeners();
      }
    } catch (e) {
      _error = e.toString();
      notifyListeners();
    }
  }

  Future<void> deleteItem(String itemId) async {
    _isLoading = true;
    notifyListeners();

    try {
      await repository.deleteItem(itemId);
      _items.removeWhere((i) => i.id == itemId);
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }
}
