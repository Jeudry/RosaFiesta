class Event {
  final String id;
  final String userId;
  final String name;
  final DateTime date;
  final String location;
  final int guestCount;
  final double budget;
  final double additionalCosts;
  final String? adminNotes;
  final String status;
  final String paymentStatus;
  final String? paymentMethod;
  final DateTime? paidAt;

  Event({
    required this.id,
    required this.userId,
    required this.name,
    required this.date,
    required this.location,
    required this.guestCount,
    required this.budget,
    this.additionalCosts = 0.0,
    this.adminNotes,
    required this.status,
    this.paymentStatus = 'pending',
    this.paymentMethod,
    this.paidAt,
  });

  factory Event.fromJson(Map<String, dynamic> json) {
    return Event(
      id: json['id'],
      userId: json['user_id'],
      name: json['name'],
      date: DateTime.parse(json['date']),
      location: json['location'],
      guestCount: json['guest_count'],
      budget: (json['budget'] as num).toDouble(),
      additionalCosts: (json['additional_costs'] as num?)?.toDouble() ?? 0.0,
      adminNotes: json['admin_notes'],
      status: json['status'],
      paymentStatus: json['payment_status'] ?? 'pending',
      paymentMethod: json['payment_method'],
      paidAt: json['paid_at'] != null ? DateTime.parse(json['paid_at']) : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'user_id': userId,
      'name': name,
      'date': date.toIso8601String(),
      'location': location,
      'guest_count': guestCount,
      'budget': budget,
      'additional_costs': additionalCosts,
      'admin_notes': adminNotes,
      'status': status,
      'payment_status': paymentStatus,
      'payment_method': paymentMethod,
      'paid_at': paidAt?.toIso8601String(),
    };
  }
}

class ArticleLite {
  final String id;
  final String nameTemplate;
  final String? descriptionTemplate;
  final String? categoryId;
  final bool isActive;
  final String type;
  final int stockQuantity;

  ArticleLite({
    required this.id,
    required this.nameTemplate,
    this.descriptionTemplate,
    this.categoryId,
    required this.isActive,
    required this.type,
    this.stockQuantity = 0,
  });

  factory ArticleLite.fromJson(Map<String, dynamic> json) {
    return ArticleLite(
      id: json['id'],
      nameTemplate: json['name_template'],
      descriptionTemplate: json['description_template'],
      categoryId: json['category_id'],
      isActive: json['is_active'] ?? false,
      type: json['type'] ?? '',
      stockQuantity: json['stock_quantity'] ?? 0,
    );
  }
}

class EventItem {
  final String id;
  final String eventId;
  final String articleId;
  final int quantity;
  final DateTime createdAt;
  final ArticleLite? article;
  final double? price;

  EventItem({
    required this.id,
    required this.eventId,
    required this.articleId,
    required this.quantity,
    required this.createdAt,
    this.article,
    this.price,
  });

  factory EventItem.fromJson(Map<String, dynamic> json) {
    return EventItem(
      id: json['id'],
      eventId: json['event_id'],
      articleId: json['article_id'],
      quantity: json['quantity'],
      createdAt: DateTime.parse(json['created_at']),
      article: json['article'] != null ? ArticleLite.fromJson(json['article']) : null,
      price: json['price'] != null ? (json['price'] as num).toDouble() : null,
    );
  }
}
