import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../core/utils/error_translator.dart';
import '../../active_event/presentation/active_event_provider.dart';
import '../data/bundle_model.dart';
import '../data/bundles_repository.dart';

class BundlesProvider extends ChangeNotifier {
  final BundlesRepository _repository;

  BundlesProvider({BundlesRepository? repository})
      : _repository = repository ?? BundlesRepository();

  List<Bundle> _bundles = [];
  List<Bundle> get bundles => _bundles;

  Bundle? _selectedBundle;
  Bundle? get selectedBundle => _selectedBundle;

  bool _isLoading = false;
  bool get isLoading => _isLoading;

  String? _error;
  String? get error => _error;

  /// Fetches all active bundles.
  Future<void> fetchBundles() async {
    if (_isLoading) return;

    _setLoading(true);
    _error = null;
    try {
      _bundles = await _repository.getBundles();
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
    } finally {
      _setLoading(false);
    }
  }

  /// Fetches a single bundle by ID with its items and article details.
  Future<void> fetchBundle(String id) async {
    if (_isLoading) return;

    _setLoading(true);
    _error = null;
    try {
      _selectedBundle = await _repository.getBundle(id);
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
    } finally {
      _setLoading(false);
    }
  }

  /// Adds bundle items to the user's active event.
  /// [adjustedItems] - list of bundle items after client removes optional items they don't want.
  Future<void> addBundleToEvent(
    BuildContext context,
    String bundleId,
    List<BundleItem> adjustedItems,
  ) async {
    final activeEvent = context.read<ActiveEventProvider>();

    for (final item in adjustedItems) {
      if (item.article != null) {
        await activeEvent.addItem(
          item.article!,
          variant: item.article!.variants.isNotEmpty
              ? item.article!.variants.first
              : null,
          quantity: item.quantity,
        );
      }
    }

    if (!context.mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        duration: const Duration(seconds: 2),
        content: Text('Paquete agregado a tu evento'),
      ),
    );
  }

  void _setLoading(bool value) {
    _isLoading = value;
    notifyListeners();
  }
}
