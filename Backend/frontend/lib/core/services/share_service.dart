import 'package:flutter/foundation.dart';
import 'package:share_plus/share_plus.dart';

class ShareService {
  static final ShareService _instance = ShareService._internal();
  factory ShareService() => _instance;
  ShareService._internal();

  /// Share event as a text + link summary via WhatsApp, SMS, or any installed app.
  Future<void> shareEvent({
    required String eventName,
    required String eventDate,
    required String location,
    required int itemCount,
    required double? totalEstimate,
    String? eventId,
  }) async {
    final buffer = StringBuffer();
    buffer.writeln('🎉 ¡Mi evento: $eventName!');
    buffer.writeln('📅 $eventDate');
    if (location.isNotEmpty) {
      buffer.writeln('📍 $location');
    }
    buffer.writeln('🎯 $itemCount artículo(s) seleccionado(s)');

    if (totalEstimate != null) {
      buffer.writeln('💰 Estimado: RD\$${totalEstimate.toStringAsFixed(0)}');
    }

    buffer.writeln();
    buffer.writeln('¿Te gustaría ayudarme a hacerlo realidad?');
    if (eventId != null) {
      buffer.writeln('https://rosafiesta.com/event/$eventId');
    } else {
      buffer.writeln('https://rosafiesta.com');
    }

    await Share.share(
      buffer.toString(),
      subject: '🎉 Invitación: $eventName — RosaFiesta',
    );
  }

  /// Share the current cart/event as a generic referral/interest link.
  Future<void> shareCatalogLink({String? productName}) async {
    final text = productName != null
        ? '¡Mira este producto de RosaFiesta: $productName! https://rosafiesta.com/catalog'
        : '¡Echa un vistazo al catálogo de RosaFiesta! https://rosafiesta.com/catalog';
    await Share.share(text, subject: 'RosaFiesta — Decoración para eventos');
  }
}
