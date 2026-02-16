import '../../products/data/product_models.dart';

class Cart {
  final String id;
  final String userId;
  final List<CartItem> items;
  final DateTime createdAt;
  final DateTime updatedAt;

  Cart({
    required this.id,
    required this.userId,
    required this.items,
    required this.createdAt,
    required this.updatedAt,
  });

  factory Cart.fromJson(Map<String, dynamic> json) {
    return Cart(
      id: json['id'],
      userId: json['user_id'],
      items: (json['items'] as List<dynamic>?)
              ?.map((e) => CartItem.fromJson(e))
              .toList() ??
          [],
      createdAt: DateTime.parse(json['created_at']),
      updatedAt: DateTime.parse(json['updated_at']),
    );
  }

  double get total {
    return items.fold(0, (sum, item) => sum + item.totalPrice);
  }
}

class CartItem {
  final String id;
  final String cartId;
  final String articleId;
  final Product article;
  final String? variantId;
  final ProductVariant? variant;
  final int quantity;

  CartItem({
    required this.id,
    required this.cartId,
    required this.articleId,
    required this.article,
    this.variantId,
    this.variant,
    required this.quantity,
  });

  factory CartItem.fromJson(Map<String, dynamic> json) {
    return CartItem(
      id: json['id'],
      cartId: json['cart_id'],
      articleId: json['article_id'],
      article: Product.fromJson(json['article']),
      variantId: json['variant_id'],
      variant: json['variant'] != null
          ? ProductVariant.fromJson(json['variant'])
          : null,
      quantity: json['quantity'],
    );
  }

  double get unitPrice {
    if (variant != null) {
      return variant!.rentalPrice;
    }
    // Fallback if no variant is selected (though usually one is required)
    // or if we use base article logic.
    // For now assuming first variant if variants exist, or 0.
    if (article.variants.isNotEmpty) {
        return article.variants.first.rentalPrice;
    }
    return 0.0;
  }

  double get totalPrice => unitPrice * quantity;
}
