import 'package:flutter/foundation.dart';
import 'package:speech_to_text/speech_to_text.dart';

class VoiceSearchService {
  static final VoiceSearchService _instance = VoiceSearchService._internal();
  factory VoiceSearchService() => _instance;
  VoiceSearchService._internal();

  final SpeechToText _stt = SpeechToText();

  bool _isAvailable = false;
  bool _isListening = false;
  String _lastResult = '';
  String? _error;

  bool get isAvailable => _isAvailable;
  bool get isListening => _isListening;
  String get lastResult => _lastResult;
  String? get error => _error;

  Function(String text)? onResult;
  Function()? onDone;

  Future<void> initialize() async {
    try {
      _isAvailable = await _stt.initialize(
        onStatus: _onStatus,
        onError: _onError,
      );
      if (kDebugMode) {
        print('VoiceSearch available: $_isAvailable');
      }
    } catch (e) {
      _isAvailable = false;
      if (kDebugMode) {
        print('VoiceSearch init error: $e');
      }
    }
  }

  void _onStatus(String status) {
    if (kDebugMode) {
      print('VoiceSearch status: $status');
    }
    if (status == 'done' || status == 'notListening') {
      _isListening = false;
    }
  }

  void _onError(dynamic error) {
    _error = error.toString();
    _isListening = false;
    if (kDebugMode) {
      print('VoiceSearch error: $_error');
    }
  }

  Future<void> startListening({
    String localeId = 'es_ES',
    Duration listenFor = const Duration(seconds: 30),
  }) async {
    if (!_isAvailable) {
      await initialize();
      if (!_isAvailable) return;
    }
    if (_isListening) return;

    _error = null;
    _lastResult = '';
    _isListening = true;

    await _stt.listen(
      onResult: _handleResult,
      listenFor: listenFor,
      localeId: localeId,
      partialResults: true,
    );
  }

  Future<void> stopListening() async {
    if (!_isListening) return;
    await _stt.stop();
    _isListening = false;
    onDone?.call();
  }

  void _handleResult(result) {
    _lastResult = result.recognizedWords;
    if (kDebugMode) {
      print('VoiceSearch result: $_lastResult');
    }
    if (result.finalResult) {
      _isListening = false;
      onResult?.call(_lastResult);
      onDone?.call();
    }
  }

  Future<List<LocaleName>> getLocales() async {
    if (!_isAvailable) return [];
    return await _stt.locales();
  }
}
