import 'package:frontend/features/products/data/product_models.dart';

class Bundle {
  final String id;
  final String name;
  final String description;
  final double discountPercent;
  final String imageUrl;
  final bool isActive;
  final double minPrice;
  final List<BundleItem> items;

  Bundle({
    required this.id,
    required this.name,
    required this.description,
    required this.discountPercent,
    required this.imageUrl,
    required this.isActive,
    required this.minPrice,
    required this.items,
  });

  factory Bundle.fromJson(Map<String, dynamic> json) {
    return Bundle(
      id: json['id'],
      name: json['name'],
      description: json['description'] ?? '',
      discountPercent: (json['discount_percent'] as num?)?.toDouble() ?? 0.0,
      imageUrl: json['image_url'] ?? '',
      isActive: json['is_active'] ?? true,
      minPrice: (json['min_price'] as num?)?.toDouble() ?? 0.0,
      items: (json['items'] as List<dynamic>?)
              ?.map((e) => BundleItem.fromJson(e))
              .toList() ??
          [],
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'name': name,
        'description': description,
        'discount_percent': discountPercent,
        'image_url': imageUrl,
        'is_active': isActive,
        'min_price': minPrice,
        'items': items.map((e) => e.toJson()).toList(),
      };
}

class BundleItem {
  final String id;
  final String bundleId;
  final String articleId;
  final int quantity;
  final bool isOptional;
  final Product? article;

  BundleItem({
    required this.id,
    required this.bundleId,
    required this.articleId,
    required this.quantity,
    required this.isOptional,
    this.article,
  });

  factory BundleItem.fromJson(Map<String, dynamic> json) {
    return BundleItem(
      id: json['id'],
      bundleId: json['bundle_id'],
      articleId: json['article_id'],
      quantity: json['quantity'] ?? 1,
      isOptional: json['is_optional'] ?? false,
      article: json['article'] != null ? Product.fromJson(json['article']) : null,
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'bundle_id': bundleId,
        'article_id': articleId,
        'quantity': quantity,
        'is_optional': isOptional,
      };
}
