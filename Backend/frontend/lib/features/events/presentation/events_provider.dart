import 'package:flutter/material.dart';
import '../data/event_model.dart';
import '../data/message_model.dart';
import '../data/events_repository.dart';
import '../../../core/utils/error_translator.dart';
import '../../../core/services/notification_service.dart';

class EventsProvider extends ChangeNotifier {
  final EventsRepository _repository = EventsRepository();
  final NotificationService _notificationService = NotificationService();

  List<Event> _events = [];
  List<Event> get events => _events;

  List<EventItem> _currentEventItems = [];
  List<EventItem> get currentEventItems => _currentEventItems;

  List<EventMessage> _messages = [];
  List<EventMessage> get messages => _messages;

  bool _isLoading = false;
  bool get isLoading => _isLoading;

  String? _error;
  String? get error => _error;

  double get realBudget {
    double total = 0;
    for (var item in _currentEventItems) {
      if (item.price != null) {
        total += item.price! * item.quantity;
      }
    }
    return total;
  }

  Map<String, double> getCategorySpending(List<dynamic> categories) {
    final Map<String, double> spending = {};
    for (var item in _currentEventItems) {
      if (item.article?.categoryId != null && item.price != null) {
        final categoryId = item.article!.categoryId!;
        final amount = item.price! * item.quantity;
        
        // Find category name if possible, otherwise use ID
        String key = categoryId;
        try {
          final category = categories.firstWhere((c) => c.id == categoryId, orElse: () => null);
          if (category != null) {
            key = category.name;
          }
        } catch (_) {
          // Ignore, keep using ID
        }
        
        spending[key] = (spending[key] ?? 0) + amount;
      }
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

  Future<bool> createEvent(Map<String, dynamic> eventData) async {
    _setLoading(true);
    _error = null;
    try {
      final newEvent = await _repository.createEvent(eventData);
      _events.add(newEvent);
      _syncEventNotifications();
      return true;
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
      return false;
    } finally {
      _setLoading(false);
    }
  }

  Future<void> fetchEventItems(String eventId) async {
    _setLoading(true);
    _error = null;
    try {
      _currentEventItems = await _repository.getEventItems(eventId);
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
    } finally {
      _setLoading(false);
    }
  }

  Future<bool> addItemToEvent(String eventId, String articleId, int quantity) async {
    try {
      final newItem = await _repository.addEventItem(eventId, {
        'article_id': articleId,
        'quantity': quantity,
      });
      _currentEventItems.add(newItem);
      notifyListeners();
      return true;
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
      notifyListeners();
      return false;
    }
  }

  Future<void> removeItemFromEvent(String eventId, String itemId) async {
    try {
      await _repository.removeEventItem(eventId, itemId);
      _currentEventItems.removeWhere((item) => item.id == itemId);
      notifyListeners();
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
      notifyListeners();
    }
  }

  Future<bool> requestQuote(String eventId) async {
    _setLoading(true);
    try {
      final updatedEvent = await _repository.requestQuote(eventId);
      _updateEventInList(updatedEvent);
      return true;
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
      return false;
    } finally {
      _setLoading(false);
    }
  }

  Future<bool> confirmQuote(String eventId) async {
    _setLoading(true);
    try {
      final updatedEvent = await _repository.confirmQuote(eventId);
      _updateEventInList(updatedEvent);
      return true;
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
      return false;
    } finally {
      _setLoading(false);
    }
  }

  double get totalSpent => realBudget;

  Future<bool> payEvent(String eventId, String method) async {
    _setLoading(true);
    try {
      final updatedEvent = await _repository.payEvent(eventId, method);
      _updateEventInList(updatedEvent);
      return true;
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
      return false;
    } finally {
      _setLoading(false);
    }
  }

  Future<void> fetchMessages(String eventId) async {
    _setLoading(true);
    _error = null;
    try {
      final List<dynamic> response = await _repository.getMessages(eventId);
      _messages = response.map((json) => EventMessage.fromJson(json)).toList();
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
    } finally {
      _setLoading(false);
    }
  }

  Future<bool> sendMessage(String eventId, String content) async {
    _error = null;
    try {
      final response = await _repository.sendMessage(eventId, content);
      final newMessage = EventMessage.fromJson(response);
      _messages.add(newMessage);
      notifyListeners();
      return true;
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
      return false;
    }
  }

  void _updateEventInList(Event updatedEvent) {
    final index = _events.indexWhere((e) => e.id == updatedEvent.id);
    if (index != -1) {
      _events[index] = updatedEvent;
      notifyListeners();
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
