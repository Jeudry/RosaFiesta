import 'dart:convert';
import 'package:hive_flutter/hive_flutter.dart';
import '../../features/products/data/product_models.dart';

class ProductCacheService {
  static const String _productsBoxName = 'products_cache';
  static const String _categoriesBoxName = 'categories_cache';
  static const String _cacheMetaKey = 'cache_meta';

  static Future<void> init() async {
    if (!Hive.isBoxOpen(_productsBoxName)) {
      await Hive.openBox<String>(_productsBoxName);
    }
    if (!Hive.isBoxOpen(_categoriesBoxName)) {
      await Hive.openBox<String>(_categoriesBoxName);
    }
  }

  static Box<String> get _productsBox =>
      Hive.box<String>(_productsBoxName);
  static Box<String> get _categoriesBox =>
      Hive.box<String>(_categoriesBoxName);

  // ── Products ───────────────────────────────────────────────────────────────

  static Future<List<Product>?> getCachedProducts() async {
    await init();
    final json = _productsBox.get('all_products');
    if (json == null) return null;
    try {
      final list = jsonDecode(json) as List;
      return list.map((e) => Product.fromJson(e as Map<String, dynamic>)).toList();
    } catch (_) {
      return null;
    }
  }

  static Future<void> cacheProducts(List<Product> products) async {
    await init();
    final json = jsonEncode(products.map((p) => p.toJson()).toList());
    await _productsBox.put('all_products', json);
    await _updateMeta('all_products');
  }

  static Future<Product?> getCachedProduct(String id) async {
    await init();
    final json = _productsBox.get('product_$id');
    if (json == null) return null;
    try {
      return Product.fromJson(jsonDecode(json) as Map<String, dynamic>);
    } catch (_) {
      return null;
    }
  }

  static Future<void> cacheProduct(Product product) async {
    await init();
    await _productsBox.put('product_${product.id}', jsonEncode(product.toJson()));
    await _updateMeta('product_${product.id}');
  }

  // ── Categories ───────────────────────────────────────────────────────────────

  static Future<List<Map<String, dynamic>>?> getCachedCategories() async {
    await init();
    final json = _categoriesBox.get('all');
    if (json == null) return null;
    try {
      final list = jsonDecode(json) as List;
      return list.cast<Map<String, dynamic>>();
    } catch (_) {
      return null;
    }
  }

  static Future<void> cacheCategories(List<Map<String, dynamic>> categories) async {
    await init();
    await _categoriesBox.put('all', jsonEncode(categories));
    await _updateMeta('categories');
  }

  // ── Cache metadata ─────────────────────────────────────────────────────────

  static Future<void> _updateMeta(String key) async {
    final meta = await _getMeta();
    meta[key] = DateTime.now().toIso8601String();
    await _productsBox.put(_cacheMetaKey, jsonEncode(meta));
  }

  static Future<Map<String, String>> _getMeta() async {
    final raw = _productsBox.get(_cacheMetaKey);
    if (raw == null) return {};
    try {
      return Map<String, String>.from(jsonDecode(raw) as Map);
    } catch (_) {
      return {};
    }
  }

  static Future<bool> isCacheStale({Duration maxAge = const Duration(hours: 24)}) async {
    final meta = await _getMeta();
    final lastUpdate = meta['all_products'];
    if (lastUpdate == null) return true;
    final age = DateTime.now().difference(DateTime.parse(lastUpdate));
    return age > maxAge;
  }

  static Future<void> clearAll() async {
    await init();
    await _productsBox.clear();
    await _categoriesBox.clear();
  }
}
