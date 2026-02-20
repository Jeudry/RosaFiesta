import 'package:flutter/material.dart';
import '../data/events_repository.dart';
import '../data/event_debrief_model.dart';

class DebriefProvider extends ChangeNotifier {
  final EventsRepository _repository = EventsRepository();
  
  EventDebrief? _debrief;
  bool _isLoading = false;
  String? _error;

  EventDebrief? get debrief => _debrief;
  bool get isLoading => _isLoading;
  String? get error => _error;

  Future<void> fetchDebrief(String eventId) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _debrief = await _repository.getEventDebrief(eventId);
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }
}
