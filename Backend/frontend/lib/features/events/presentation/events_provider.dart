import 'package:flutter/material.dart';
import '../data/event_model.dart';
import '../data/message_model.dart';
import '../data/events_repository.dart';
import '../../../core/utils/error_translator.dart';
import '../../../core/services/notification_service.dart';

class EventsProvider extends ChangeNotifier {
  // ... (previous fields)
  List<EventMessage> _messages = [];
  List<EventMessage> get messages => _messages;

  // ... (existing methods fetchEvents, createEvent, etc.)

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
