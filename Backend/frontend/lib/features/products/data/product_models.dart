class Product {
  final String id;
  final String nameTemplate;
  final String? descriptionTemplate;
  final bool isActive;
  final String type; // 'Rental' or 'Sale'
  final String? categoryId;
  final List<ProductVariant> variants;

  Product({
    required this.id,
    required this.nameTemplate,
    this.descriptionTemplate,
    required this.isActive,
    required this.type,
    this.categoryId,
    this.variants = const [],
  });

  factory Product.fromJson(Map<String, dynamic> json) {
    return Product(
      id: json['id'],
      nameTemplate: json['name_template'],
      descriptionTemplate: json['description_template'],
      isActive: json['is_active'],
      type: json['type'],
      categoryId: json['category_id'],
      variants: (json['variants'] as List<dynamic>?)
              ?.map((e) => ProductVariant.fromJson(e))
              .toList() ??
          [],
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
