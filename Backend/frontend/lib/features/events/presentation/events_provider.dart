import 'package:flutter/material.dart';
import '../data/event_model.dart';
import '../data/events_repository.dart';
import '../../../core/utils/error_translator.dart';
import '../../../core/services/notification_service.dart';

class EventsProvider extends ChangeNotifier {
  final EventsRepository _repository;
  final NotificationService _notificationService = NotificationService();

  EventsProvider({EventsRepository? repository})
      : _repository = repository ?? EventsRepository();

  List<Event> _events = [];
  List<Event> get events => _events;

  bool _isLoading = false;
  bool get isLoading => _isLoading;

  String? _error;
  String? get error => _error;

  double get totalSpent => _currentEventItems.fold(0.0, (sum, item) => sum + ((item.price ?? 0) * item.quantity));

  Map<String, double> getCategorySpending(List<dynamic> allCategories) {
    final Map<String, double> spending = {};
    
    for (var item in _currentEventItems) {
      final categoryId = item.article?.categoryId;
      final categoryName = allCategories
          .firstWhere((c) => c.id == categoryId, orElse: () => null)
          ?.name ?? 'Otros';
      
      final total = (item.price ?? 0) * item.quantity;
      spending[categoryName] = (spending[categoryName] ?? 0) + total;
    }
    
    return spending;
  }

  Future<void> fetchEvents() async {
    _setLoading(true);
    _error = null;
    try {
      _events = await _repository.getEvents();
      _syncEventNotifications();
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


  List<EventItem> _currentEventItems = [];
  List<EventItem> get currentEventItems => _currentEventItems;

  Future<void> fetchEventItems(String eventId) async {
    _setLoading(true);
    _error = null;
    try {
      _currentEventItems = await _repository.getItems(eventId);
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
    } finally {
      _setLoading(false);
    }
  }

  Future<bool> addItemToEvent(String eventId, String articleId, int quantity) async {
    _setLoading(true);
    _error = null;
    try {
      await _repository.addItem(eventId, articleId, quantity);
      await fetchEventItems(eventId); // Refresh items
      return true;
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
      return false;
    } finally {
      _setLoading(false);
    }
  }

  Future<bool> removeItemFromEvent(String eventId, String itemId) async {
    _setLoading(true);
    _error = null;
    try {
      await _repository.removeItem(eventId, itemId);
      await fetchEventItems(eventId); // Refresh items
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

  void _syncEventNotifications() {
    for (var event in _events) {
      if (event.date.isAfter(DateTime.now())) {
        // Schedule reminder 24 hours before the event
        final reminderDate = event.date.subtract(const Duration(hours: 24));
        if (reminderDate.isAfter(DateTime.now())) {
          _notificationService.scheduleNotification(
            id: event.id.hashCode,
            title: '¡Tu evento se acerca!',
            body: 'Faltan 24 horas para "${event.name}". ¿Está todo listo?',
            scheduledDate: reminderDate,
          );
        }
      }
    }
  }
}
