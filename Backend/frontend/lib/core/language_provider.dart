import 'package:flutter/material.dart';
import 'services/hive_service.dart';

class LanguageProvider extends ChangeNotifier {
  static const String _languageKey = 'language_code';

  Locale _locale = const Locale('es');

  Locale get locale => _locale;

  LanguageProvider() {
    _loadSavedLanguage();
  }

  void _loadSavedLanguage() {
    final box = HiveService.languageBox;
    final saved = box.get(_languageKey, defaultValue: 'es') as String;
    _locale = Locale(saved);
  }

  Future<void> setLanguage(String languageCode) async {
    if (_locale.languageCode == languageCode) return;
    _locale = Locale(languageCode);
    await HiveService.languageBox.put(_languageKey, languageCode);
    notifyListeners();
  }

  bool get isSpanish => _locale.languageCode == 'es';
  bool get isEnglish => _locale.languageCode == 'en';

  String get currentLanguageCode => _locale.languageCode;
}
