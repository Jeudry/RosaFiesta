import 'package:flutter/material.dart';
import '../data/event_model.dart';
import '../data/events_repository.dart';
import '../../../core/utils/error_translator.dart';

class EventsProvider extends ChangeNotifier {
  final EventsRepository _repository;

  EventsProvider({EventsRepository? repository})
      : _repository = repository ?? EventsRepository();

  List<Event> _events = [];
  List<Event> get events => _events;

  bool _isLoading = false;
  bool get isLoading => _isLoading;

  String? _error;
  String? get error => _error;

  Future<void> fetchEvents() async {
    _setLoading(true);
    _error = null;
    try {
      _events = await _repository.getEvents();
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
    } finally {
      _setLoading(false);
    }
  }

  Future<bool> createEvent(String name, DateTime date, String location, double budget, int guestCount) async {
    _setLoading(true);
    _error = null;
    try {
      final eventData = {
        'name': name,
        'date': date.toIso8601String(),
        'location': location,
        'budget': budget,
        'guest_count': guestCount,
      };
      await _repository.createEvent(eventData);
      await fetchEvents(); // Refresh list
      return true;
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
      return false;
    } finally {
      _setLoading(false);
    }
  }
  
  // Method to delete an event
  Future<bool> deleteEvent(String id) async {
      _setLoading(true);
      _error = null;
      try {
        await _repository.deleteEvent(id);
        await fetchEvents(); // Refresh list
        return true;
      } catch (e) {
        _error = ErrorTranslator.translate(e.toString());
        return false;
      } finally {
        _setLoading(false);
      }
  }

  void _setLoading(bool value) {
    _isLoading = value;
    notifyListeners();
  }
}
