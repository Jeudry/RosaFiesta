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
  String? _error;

  List<EventMessage> get messages => _messages;
  bool get isConnected => _isConnected;
  String? get error => _error;

  final _storage = const FlutterSecureStorage();

  void connect(String eventId) async {
    if (_isConnected) return;

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
      notifyListeners();

      _channel!.stream.listen(
        (data) {
          final decoded = jsonDecode(data);
          if (decoded is Map<String, dynamic>) {
            final message = EventMessage.fromJson(decoded);
            _messages.add(message);
            notifyListeners();
          }
        },
        onError: (err) {
          _isConnected = false;
          _error = 'WebSocket Error: $err';
          notifyListeners();
        },
        onDone: () {
          _isConnected = false;
          notifyListeners();
        },
      );
    } catch (e) {
      _isConnected = false;
      _error = 'Connection failed: $e';
      notifyListeners();
    }
  }

  void sendMessage(String content) {
    if (_channel != null && _isConnected) {
      final data = jsonEncode({'content': content});
      _channel!.sink.add(data);
    }
  }

  void disconnect() {
    _channel?.sink.close();
    _isConnected = false;
    _messages.clear();
    notifyListeners();
  }

  @override
  void dispose() {
    disconnect();
    super.dispose();
  }
}
