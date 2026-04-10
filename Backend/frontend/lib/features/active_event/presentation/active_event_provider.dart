import 'package:flutter/material.dart';

import '../../../core/utils/error_translator.dart';
import '../../events/data/event_model.dart';
import '../../products/data/product_models.dart';
import '../data/active_event_repository.dart';

/// Holds the user's "active event" — a draft event that acts as their
/// working basket while they browse the catalog. Replaces CartProvider.
///
/// The provider is intentionally lazy: it only hits the backend on the
/// first read or after an explicit `addItem` / `updateQuantity` / `clear`
/// call, so the home screen does not pay a roundtrip if the user never
/// touches a product.
class ActiveEventProvider extends ChangeNotifier {
  final ActiveEventRepository _repository;

  ActiveEventProvider({ActiveEventRepository? repository})
      : _repository = repository ?? ActiveEventRepository();

  Event? _event;
  Event? get event => _event;

  List<EventItem> _items = const [];
  List<EventItem> get items => _items;

  bool _isLoading = false;
  bool get isLoading => _isLoading;

  String? _error;
  String? get error => _error;

  /// Total number of *units* in the draft (sum of all line quantities).
  /// Used by the top-bar badge in the catalog.
  int get itemCount =>
      _items.fold<int>(0, (sum, item) => sum + item.quantity);

  /// Number of distinct lines (one per article × variant tuple). Used in
  /// the active event screen as a secondary stat.
  int get lineCount => _items.length;

  /// Estimated subtotal (no taxes, no additional costs).
  double get subtotal =>
      _items.fold<double>(0, (sum, item) => sum + item.lineTotal);

  /// Hydrates the provider from the backend. Safe to call multiple times.
  Future<void> fetch({bool force = false}) async {
    if (_isLoading) return;
    if (_event != null && !force) return;

    _setLoading(true);
    _error = null;
    try {
      final response = await _repository.getActive();
      _event = response.event;
      _items = response.items;
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
    } finally {
      _setLoading(false);
    }
  }

  /// Add a product to the draft event. Ensures the draft exists first
  /// (single roundtrip via `getActive`) and then upserts the line.
  ///
  /// The optimistic path is intentionally simple: we don't pre-add the
  /// line locally because the backend returns the canonical row (with
  /// the merged quantity if it already existed) — we just refresh.
  Future<void> addItem(
    Product product, {
    ProductVariant? variant,
    int quantity = 1,
  }) async {
    if (quantity <= 0) return;

    // Make sure we have a draft to write into.
    if (_event == null) {
      await fetch();
      if (_event == null) return; // fetch failed; error already set
    }

    final variantToUse = variant ??
        (product.variants.isNotEmpty ? product.variants.first : null);
    final price = variantToUse?.rentalPrice;

    _error = null;
    try {
      await _repository.addItem(
        eventId: _event!.id,
        articleId: product.id,
        variantId: variantToUse?.id,
        quantity: quantity,
        priceSnapshot: price,
      );
      // Re-pull so the local list reflects the canonical merged state
      // (and joined article/variant fields the backend computed).
      await _refreshItems();
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
      notifyListeners();
    }
  }

  /// Sets the absolute quantity of a line. Quantity 0 removes it.
  Future<void> updateQuantity(String itemId, int quantity) async {
    if (_event == null) return;

    // Optimistic local update so the UI reacts instantly.
    final previous = _items;
    if (quantity <= 0) {
      _items = _items.where((i) => i.id != itemId).toList();
    } else {
      _items = _items
          .map((i) => i.id == itemId ? _withQuantity(i, quantity) : i)
          .toList();
    }
    notifyListeners();

    try {
      await _repository.updateItemQuantity(
        itemId: itemId,
        quantity: quantity,
      );
    } catch (e) {
      // Roll back on failure.
      _items = previous;
      _error = ErrorTranslator.translate(e.toString());
      notifyListeners();
    }
  }

  /// Removes a line from the draft. Convenience wrapper around
  /// `updateQuantity(0)` so callers can be more explicit at the call site.
  Future<void> removeItem(String itemId) => updateQuantity(itemId, 0);

  /// Clears the local state — used after logout. Does not touch the
  /// backend (the draft survives across sessions on purpose).
  void clearLocal() {
    _event = null;
    _items = const [];
    _error = null;
    notifyListeners();
  }

  // ─── internals ─────────────────────────────────────────────────────────

  Future<void> _refreshItems() async {
    try {
      final response = await _repository.getActive();
      _event = response.event;
      _items = response.items;
      notifyListeners();
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
      notifyListeners();
    }
  }

  EventItem _withQuantity(EventItem item, int quantity) {
    return EventItem(
      id: item.id,
      eventId: item.eventId,
      articleId: item.articleId,
      variantId: item.variantId,
      quantity: quantity,
      priceSnapshot: item.priceSnapshot,
      createdAt: item.createdAt,
      article: item.article,
      variant: item.variant,
      price: item.price,
    );
  }

  void _setLoading(bool value) {
    _isLoading = value;
    notifyListeners();
  }
}
