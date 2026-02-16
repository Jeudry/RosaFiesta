class Category {
  final String id;
  final String name;
  final String? description;
  final String? imageUrl;
  final String? parentId;

  Category({
    required this.id,
    required this.name,
    this.description,
    this.imageUrl,
    this.parentId,
  });

  factory Category.fromJson(Map<String, dynamic> json) {
    return Category(
      id: json['id'],
      name: json['name'],
      description: json['description'],
      imageUrl: json['image_url'],
      parentId: json['parent_id'],
    );
  }
}
