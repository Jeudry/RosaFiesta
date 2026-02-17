class Product {
  final String id;
  final String nameTemplate;
  final String? descriptionTemplate;
  final bool isActive;
  final String type; // 'Rental' or 'Sale'
  final int stockQuantity;
  final String? categoryId;
  final List<ProductVariant> variants;
  final double averageRating;
  final int reviewCount;

  Product({
    required this.id,
    required this.nameTemplate,
    this.descriptionTemplate,
    required this.isActive,
    required this.type,
    this.stockQuantity = 0,
    this.categoryId,
    this.variants = const [],
    this.averageRating = 0.0,
    this.reviewCount = 0,
  });

  factory Product.fromJson(Map<String, dynamic> json) {
    return Product(
      id: json['id'],
      nameTemplate: json['name_template'],
      descriptionTemplate: json['description_template'],
      isActive: json['is_active'],
      type: json['type'],
      stockQuantity: json['stock_quantity'] ?? 0,
      categoryId: json['category_id'],
      variants: (json['variants'] as List<dynamic>?)
              ?.map((e) => ProductVariant.fromJson(e))
              .toList() ??
          [],
      averageRating: (json['average_rating'] as num?)?.toDouble() ?? 0.0,
      reviewCount: json['review_count'] ?? 0,
    );
  }
}

class Review {
  final String id;
  final String userId;
  final String articleId;
  final int rating;
  final String comment;
  final DateTime created;
  final ReviewUser? user;

  Review({
    required this.id,
    required this.userId,
    required this.articleId,
    required this.rating,
    required this.comment,
    required this.created,
    this.user,
  });

  factory Review.fromJson(Map<String, dynamic> json) {
    return Review(
      id: json['id'],
      userId: json['user_id'],
      articleId: json['article_id'],
      rating: json['rating'],
      comment: json['comment'],
      created: DateTime.parse(json['created']),
      user: json['user'] != null ? ReviewUser.fromJson(json['user']) : null,
    );
  }
}

class ReviewUser {
  final String userName;
  final String? avatar;

  ReviewUser({
    required this.userName,
    this.avatar,
  });

  factory ReviewUser.fromJson(Map<String, dynamic> json) {
    return ReviewUser(
      userName: json['user_name'],
      avatar: json['avatar'],
    );
  }
}

class ProductVariant {
  final String id;
  final String articleId;
  final String sku;
  final String name;
  final String? description;
  final String? imageUrl;
  final bool isActive;
  final int stock;
  final double rentalPrice;
  final double? salePrice;
  final double? replacementCost;
  final Map<String, dynamic> attributes;
  final List<ProductDimension> dimensions;

  ProductVariant({
    required this.id,
    required this.articleId,
    required this.sku,
    required this.name,
    this.description,
    this.imageUrl,
    required this.isActive,
    required this.stock,
    required this.rentalPrice,
    this.salePrice,
    this.replacementCost,
    this.attributes = const {},
    this.dimensions = const [],
  });

  factory ProductVariant.fromJson(Map<String, dynamic> json) {
    return ProductVariant(
      id: json['id'],
      articleId: json['article_id'],
      sku: json['sku'],
      name: json['name'],
      description: json['description'],
      imageUrl: json['image_url'],
      isActive: json['is_active'],
      stock: json['stock'],
      rentalPrice: (json['rental_price'] as num).toDouble(),
      salePrice: (json['sale_price'] as num?)?.toDouble(),
      replacementCost: (json['replacement_cost'] as num?)?.toDouble(),
      attributes: json['attributes'] ?? {},
      dimensions: (json['dimensions'] as List<dynamic>?)
              ?.map((e) => ProductDimension.fromJson(e))
              .toList() ??
          [],
    );
  }
}

class ProductDimension {
  final String id;
  final String variantId;
  final double? height;
  final double? width;
  final double? depth;
  final double? weight;

  ProductDimension({
    required this.id,
    required this.variantId,
    this.height,
    this.width,
    this.depth,
    this.weight,
  });

  factory ProductDimension.fromJson(Map<String, dynamic> json) {
    return ProductDimension(
      id: json['id'],
      variantId: json['variant_id'],
      height: (json['height'] as num?)?.toDouble(),
      width: (json['width'] as num?)?.toDouble(),
      depth: (json['depth'] as num?)?.toDouble(),
      weight: (json['weight'] as num?)?.toDouble(),
    );
  }
}
