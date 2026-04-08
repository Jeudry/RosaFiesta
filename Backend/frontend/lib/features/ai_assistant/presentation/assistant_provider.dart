import 'package:flutter/foundation.dart';

import '../../products/data/product_models.dart';
import '../../products/data/products_repository.dart';

/// Assistant mode — how the user is talking to the assistant right now.
enum AssistantMode { chat, voice }

/// High-level conversation step the assistant is currently on.
///
/// This is a tiny hand-rolled state machine that mimics a guided planner flow
/// until we wire up a real LLM in Phase 3.
enum ConversationStep {
  greeting,        // Step 1: pick event type
  inputTutorial,   // Step 1.5: explain mic/chat
  askHelp,         // Step 2: want help picking articles?
  suggestArticles, // Step 3: show product suggestions
  categories,      // Step 4: categories that might be missing
  eventDetails,    // Step 5: date, location, guests
  summary,         // Step 6: full order summary
  done,            // Step 7: confirmation sent
}

/// Message types rendered in the chat stream.
enum MessageKind {
  assistantText,
  userText,
  chips,
  productSuggestions,
}

class AssistantMessage {
  final MessageKind kind;
  final String? text;
  final List<String> chips;
  final List<Product> products;
  final String? sectionTitle;
  final DateTime timestamp;

  AssistantMessage._({
    required this.kind,
    this.text,
    this.chips = const [],
    this.products = const [],
    this.sectionTitle,
  }) : timestamp = DateTime.now();

  factory AssistantMessage.assistant(String text) =>
      AssistantMessage._(kind: MessageKind.assistantText, text: text);

  factory AssistantMessage.user(String text) =>
      AssistantMessage._(kind: MessageKind.userText, text: text);

  factory AssistantMessage.chips(List<String> chips) =>
      AssistantMessage._(kind: MessageKind.chips, chips: chips);

  factory AssistantMessage.products(List<Product> products,
          {String? sectionTitle}) =>
      AssistantMessage._(
          kind: MessageKind.productSuggestions,
          products: products,
          sectionTitle: sectionTitle);
}

/// Manages the assistant conversation state and mocks an LLM-guided flow.
///
/// Phase 1 is intentionally mock: canned responses + basic state machine.
/// Phase 3 will swap the `_respond*` methods for real LLM calls.
class AssistantProvider extends ChangeNotifier {
  final ProductsRepository _productsRepository;

  AssistantProvider({ProductsRepository? productsRepository})
      : _productsRepository = productsRepository ?? ProductsRepository();

  final List<AssistantMessage> _messages = [];
  List<AssistantMessage> get messages => List.unmodifiable(_messages);

  AssistantMode _mode = AssistantMode.chat;
  AssistantMode get mode => _mode;

  ConversationStep _step = ConversationStep.greeting;
  ConversationStep get step => _step;

  bool _isThinking = false;
  bool get isThinking => _isThinking;

  bool _shouldMinimize = false;
  bool get shouldMinimize => _shouldMinimize;
  void clearMinimize() => _shouldMinimize = false;

  String? _eventType;
  String? get eventType => _eventType;

  String? _guestCount;
  String? get guestCount => _guestCount;

  List<Product> _catalog = [];

  /// Begin the conversation. Idempotent — if there are already messages,
  /// nothing happens.
  Future<void> start() async {
    if (_messages.isNotEmpty) return;
    // Kick off product loading in the background so suggestions are instant.
    _productsRepository.getProducts(limit: 50, offset: 0).then((p) {
      _catalog = p;
    }).catchError((_) {
      _catalog = <Product>[];
    });

    _messages.add(AssistantMessage.assistant(
        '¿Qué evento quieres celebrar?'));
    _messages.add(AssistantMessage.chips(const [
      'Boda',
      'Cumpleaños',
      'Baby Shower',
      'Gender Reveal',
      'Corporativo',
      'Otro',
    ]));
    notifyListeners();
  }

  void setMode(AssistantMode mode) {
    if (_mode == mode) return;
    _mode = mode;
    notifyListeners();
  }

  /// Called when the user taps a chip suggestion from the stream.
  Future<void> handleChipTap(String value) async {
    _messages.add(AssistantMessage.user(value));
    notifyListeners();
    await _thinkFor(const Duration(milliseconds: 700));

    switch (_step) {
      // Step 1: User picked event type → show input tutorial
      case ConversationStep.greeting:
        _eventType = value;
        _step = ConversationStep.inputTutorial;
        _messages.add(AssistantMessage.assistant(
            '¡$value! Me encanta ✨\n\nAntes de empezar, recuerda que si en cualquier momento quieres darme más detalles, puedes usar los botones de **Hablar** o **Chatear** de abajo.'));
        _messages.add(AssistantMessage.chips(const [
          'Entendido',
        ]));
        break;

      // Step 1.5: Tutorial acknowledged → ask if want help
      case ConversationStep.inputTutorial:
        _step = ConversationStep.askHelp;
        _messages.add(AssistantMessage.assistant(
            '¿Quieres que te ayude a elegir los artículos de alquiler desde aquí?'));
        _messages.add(AssistantMessage.chips(const [
          'No, por ahora',
          'Sí, busquemos',
        ]));
        break;

      // Step 2: Help picking articles?
      case ConversationStep.askHelp:
        if (value == 'No, por ahora') {
          _shouldMinimize = true;
          _messages.add(AssistantMessage.assistant(
              'Sin problema. Estaré aquí cuando me necesites. Explora el catálogo y vuelve cuando quieras.'));
          _step = ConversationStep.suggestArticles;
          notifyListeners();
          return;
        }
        _step = ConversationStep.suggestArticles;
        _messages.add(AssistantMessage.assistant(
            'Basándome en lo que necesitas para tu $_eventType, te recomiendo estos artículos:'));
        final picks = _pickSuggestions();
        if (picks.isNotEmpty) {
          _messages.add(AssistantMessage.products(picks,
              sectionTitle: 'Recomendado para tu $_eventType'));
        }
        final more = _pickSuggestions(skipFirst: 3);
        if (more.isNotEmpty) {
          _messages.add(AssistantMessage.products(more,
              sectionTitle: 'También te puede interesar'));
        }
        _messages.add(AssistantMessage.assistant(
            'Recuerda que puedes opinar por micrófono en cualquier momento 🎙️'));
        _messages.add(AssistantMessage.chips(const [
          'Ver otras sugerencias',
          'Continuar',
        ]));
        break;

      // Step 3: Article suggestions
      case ConversationStep.suggestArticles:
        if (value == 'Continuar') {
          _step = ConversationStep.categories;
          _messages.add(AssistantMessage.assistant(
              '¡Genial! Revisemos si te falta algo. Estas son categorías que podrían complementar tu $_eventType:'));
          _messages.add(AssistantMessage.chips(const [
            'Más sugerencias',
            'Siguiente: detalles',
          ]));
        } else {
          final batch = _pickSuggestions(skipFirst: 6);
          if (batch.isNotEmpty) {
            _messages.add(AssistantMessage.products(batch,
                sectionTitle: 'Más opciones'));
          } else {
            _messages.add(AssistantMessage.assistant(
                'Ya te mostré todo lo disponible por ahora.'));
          }
          _messages.add(AssistantMessage.chips(const [
            'Ver otras sugerencias',
            'Continuar',
          ]));
        }
        break;

      // Step 4: Categories review
      case ConversationStep.categories:
        if (value == 'Siguiente: detalles') {
          _step = ConversationStep.eventDetails;
          _messages.add(AssistantMessage.assistant(
              'Ahora completemos los detalles de tu evento:'));
          _messages.add(AssistantMessage.chips(const [
            'Más sugerencias',
            'Resumen del evento',
          ]));
        } else {
          _step = ConversationStep.suggestArticles;
          _messages.add(AssistantMessage.assistant(
              'Aquí tienes más sugerencias:'));
          final batch = _pickSuggestions();
          if (batch.isNotEmpty) {
            _messages.add(AssistantMessage.products(batch,
                sectionTitle: 'Sugerencias adicionales'));
          }
          _messages.add(AssistantMessage.chips(const [
            'Ver otras sugerencias',
            'Continuar',
          ]));
        }
        break;

      // Step 5: Event details
      case ConversationStep.eventDetails:
        if (value == 'Resumen del evento') {
          _step = ConversationStep.summary;
          _messages.add(AssistantMessage.assistant(
              'Aquí tienes el resumen de todo lo que has seleccionado. Revisa que todo esté correcto:'));
          _messages.add(AssistantMessage.chips(const [
            'Agregar algo más',
            'Solicitar cotización',
          ]));
        } else {
          _step = ConversationStep.suggestArticles;
          _messages.add(AssistantMessage.assistant(
              'Claro, veamos más opciones:'));
          final batch = _pickSuggestions();
          if (batch.isNotEmpty) {
            _messages.add(AssistantMessage.products(batch,
                sectionTitle: 'Más para tu $_eventType'));
          }
          _messages.add(AssistantMessage.chips(const [
            'Ver otras sugerencias',
            'Continuar',
          ]));
        }
        break;

      // Step 6: Summary
      case ConversationStep.summary:
        if (value == 'Solicitar cotización') {
          _step = ConversationStep.done;
          _messages.add(AssistantMessage.assistant(
              '¡Tu solicitud ha sido enviada! 🎉\n\nEl equipo de RosaFiesta revisará tu evento y te enviará una cotización por correo y WhatsApp.\n\nGracias por confiar en nosotros.'));
        } else {
          // Agregar algo más
          _step = ConversationStep.suggestArticles;
          _messages.add(AssistantMessage.assistant(
              '¿Qué más te gustaría agregar?'));
          final batch = _pickSuggestions();
          if (batch.isNotEmpty) {
            _messages.add(AssistantMessage.products(batch,
                sectionTitle: 'Explorar más'));
          }
          _messages.add(AssistantMessage.chips(const [
            'Ver otras sugerencias',
            'Continuar',
          ]));
        }
        break;

      case ConversationStep.done:
        _messages.add(AssistantMessage.assistant(
            '¡Tu solicitud ya fue enviada! Si necesitas algo más, no dudes en volver.'));
        break;
    }
    notifyListeners();
  }

  /// Called when the user sends a free-form text message (chat mode).
  Future<void> sendText(String text) async {
    final trimmed = text.trim();
    if (trimmed.isEmpty) return;
    _messages.add(AssistantMessage.user(trimmed));
    notifyListeners();
    await _thinkFor(const Duration(milliseconds: 900));
    // Mock: for Phase 1 we just echo back a generic continuation.
    _messages.add(AssistantMessage.assistant(
        'Anotado. Mientras tanto, puedes seleccionar una opción de las sugerencias para seguir avanzando.'));
    notifyListeners();
  }

  /// Reset the conversation (e.g. when closing and reopening the assistant).
  void reset() {
    _messages.clear();
    _step = ConversationStep.greeting;
    _eventType = null;
    _guestCount = null;
    _mode = AssistantMode.chat;
    notifyListeners();
  }

  Future<void> _thinkFor(Duration d) async {
    _isThinking = true;
    notifyListeners();
    await Future<void>.delayed(d);
    _isThinking = false;
  }

  List<Product> _pickSuggestions({int skipFirst = 0}) {
    if (_catalog.isEmpty) return const [];
    final start = skipFirst.clamp(0, _catalog.length);
    final end = (start + 3).clamp(0, _catalog.length);
    return _catalog.sublist(start, end);
  }
}
