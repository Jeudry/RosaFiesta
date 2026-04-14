import 'dart:async';
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:web_socket_channel/web_socket_channel.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../data/message_model.dart';
import '../../../core/config/env_config.dart';

class ChatProvider extends ChangeNotifier {
  WebSocketChannel? _channel;
  final List<EventMessage> _messages = [];
  bool _isConnected = false;
  bool _isTyping = false;
  bool _isLoadingOlderMessages = false;
  String? _error;
  Timer? _reconnectTimer;
  int _reconnectAttempts = 0;
  static const int _maxReconnectAttempts = 3;
  String? _currentEventId;

  List<EventMessage> get messages => _messages;
  bool get isConnected => _isConnected;
  bool get isTyping => _isTyping;
  bool get isLoadingOlderMessages => _isLoadingOlderMessages;
  String? get error => _error;

  final _storage = const FlutterSecureStorage();

  void connect(String eventId) async {
    if (_isConnected && _currentEventId == eventId) return;
    _currentEventId = eventId;

    final token = await _storage.read(key: 'access_token');
    if (token == null) {
      _error = 'No authentication token found';
      notifyListeners();
      return;
    }

    final wsBaseUrl = EnvConfig.apiUrl.replaceFirst('http', 'ws');
    final wsUrl = '$wsBaseUrl/events/$eventId/messages/ws?token=$token';

    try {
      _channel = WebSocketChannel.connect(Uri.parse(wsUrl));
      _isConnected = true;
      _error = null;
      _reconnectAttempts = 0;
      notifyListeners();

      _channel!.stream.listen(
        (data) {
          final decoded = jsonDecode(data);
          if (decoded is Map<String, dynamic>) {
            // Check if it's a typing indicator
            if (decoded['type'] == 'typing') {
              _isTyping = decoded['is_typing'] ?? false;
              notifyListeners();
              return;
            }
            final message = EventMessage.fromJson(decoded);
            _messages.add(message);
            notifyListeners();
          }
        },
        onError: (err) {
          _isConnected = false;
          _error = 'WebSocket Error: $err';
          notifyListeners();
          _scheduleReconnect();
        },
        onDone: () {
          _isConnected = false;
          notifyListeners();
          _scheduleReconnect();
        },
      );
    } catch (e) {
      _isConnected = false;
      _error = 'Connection failed: $e';
      notifyListeners();
      _scheduleReconnect();
    }
  }

  void _scheduleReconnect() {
    if (_reconnectAttempts >= _maxReconnectAttempts) {
      _error = 'Conexion perdida. Toca para reintentar.';
      _reconnectTimer?.cancel();
      _reconnectTimer = null;
      notifyListeners();
      return;
    }
    _reconnectTimer?.cancel();
    _reconnectTimer = Timer(const Duration(seconds: 5), () {
      _reconnectAttempts++;
      if (_currentEventId != null) {
        connect(_currentEventId!);
      }
    });
  }

  void sendMessage(String content) {
    if (_channel != null && _isConnected) {
      final data = jsonEncode({'content': content});
      _channel!.sink.add(data);
    }
  }

  /// Loads older messages (mock/pagination placeholder).
  /// In a real implementation this would call a REST endpoint.
  Future<void> loadOlderMessages() async {
    if (_messages.isEmpty) return;
    _isLoadingOlderMessages = true;
    notifyListeners();

    // Simulate network delay for pagination
    await Future.delayed(const Duration(milliseconds: 800));

    // TODO: Replace with actual REST pagination call:
    //   GET /events/$eventId/messages?before=${_messages.first.id}
    // For now, just simulate a few older placeholder messages
    _isLoadingOlderMessages = false;
    notifyListeners();
  }

  void disconnect() {
    _reconnectTimer?.cancel();
    _reconnectTimer = null;
    _reconnectAttempts = 0;
    _channel?.sink.close();
    _channel = null;
    _isConnected = false;
    _isTyping = false;
    _currentEventId = null;
    // Don't clear messages so the UI can show the last state
    notifyListeners();
  }

  /// Manually trigger a reconnect attempt
  void reconnect() {
    _reconnectAttempts = 0;
    if (_currentEventId != null) {
      connect(_currentEventId!);
    }
  }

  @override
  void dispose() {
    disconnect();
    super.dispose();
  }
}