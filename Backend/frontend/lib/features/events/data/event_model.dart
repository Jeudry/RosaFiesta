class Event {
  final String id;
  final String userId;
  final String name;
  // Nullable because a draft event (the user's "active event" while they
  // are browsing the catalog) may not have a date picked yet.
  final DateTime? date;
  final String location;
  final int guestCount;
  final double budget;
  final double additionalCosts;
  final String? adminNotes;
  final String status;
  final String paymentStatus;
  final String? paymentMethod;
  final DateTime? paidAt;
  final bool depositPaid;
  final int depositAmount;
  final DateTime? depositPaidAt;
  final int remainingAmount;
  final DateTime? installmentDueDate;
  final int totalQuote;

  Event({
    required this.id,
    required this.userId,
    required this.name,
    this.date,
    required this.location,
    required this.guestCount,
    required this.budget,
    this.additionalCosts = 0.0,
    this.adminNotes,
    required this.status,
    this.paymentStatus = 'pending',
    this.paymentMethod,
    this.paidAt,
    this.depositPaid = false,
    this.depositAmount = 0,
    this.depositPaidAt,
    this.remainingAmount = 0,
    this.installmentDueDate,
    this.totalQuote = 0,
  });

  /// True when this event is the user's draft "active event" — their
  /// working basket before they commit to a date / name.
  bool get isDraft => status == 'draft';

  /// True when deposit has been paid but full payment is not complete
  bool get isDepositPending => depositPaid && remainingAmount > 0;

  /// True when the remaining payment is overdue
  bool get isInstallmentOverdue =>
      installmentDueDate != null &&
      installmentDueDate!.isBefore(DateTime.now()) &&
      remainingAmount > 0;

  factory Event.fromJson(Map<String, dynamic> json) {
    return Event(
      id: json['id'],
      userId: json['user_id'],
      name: json['name'] ?? '',
      date: json['date'] != null ? DateTime.parse(json['date']) : null,
      location: json['location'] ?? '',
      guestCount: json['guest_count'] ?? 0,
      budget: (json['budget'] as num?)?.toDouble() ?? 0.0,
      additionalCosts: (json['additional_costs'] as num?)?.toDouble() ?? 0.0,
      adminNotes: json['admin_notes'],
      status: json['status'],
      paymentStatus: json['payment_status'] ?? 'pending',
      paymentMethod: json['payment_method'],
      paidAt: json['paid_at'] != null ? DateTime.parse(json['paid_at']) : null,
      depositPaid: json['depositPaid'] ?? false,
      depositAmount: json['depositAmount'] ?? 0,
      depositPaidAt: json['depositPaidAt'] != null
          ? DateTime.parse(json['depositPaidAt'])
          : null,
      remainingAmount: json['remainingAmount'] ?? 0,
      installmentDueDate: json['installmentDueDate'] != null
          ? DateTime.parse(json['installmentDueDate'])
          : null,
      totalQuote: json['totalQuote'] ?? 0,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'user_id': userId,
      'name': name,
      'date': date?.toIso8601String(),
      'location': location,
      'guest_count': guestCount,
      'budget': budget,
      'additional_costs': additionalCosts,
      'admin_notes': adminNotes,
      'status': status,
      'payment_status': paymentStatus,
      'payment_method': paymentMethod,
      'paid_at': paidAt?.toIso8601String(),
      'depositPaid': depositPaid,
      'depositAmount': depositAmount,
      'depositPaidAt': depositPaidAt?.toIso8601String(),
      'remainingAmount': remainingAmount,
      'installmentDueDate': installmentDueDate?.toIso8601String(),
      'totalQuote': totalQuote,
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

/// Lightweight variant info that comes joined in event_items responses.
class EventItemVariant {
  final String id;
  final String sku;
  final String name;
  final String? imageUrl;
  final double rentalPrice;

  EventItemVariant({
    required this.id,
    required this.sku,
    required this.name,
    this.imageUrl,
    required this.rentalPrice,
  });

  factory EventItemVariant.fromJson(Map<String, dynamic> json) {
    return EventItemVariant(
      id: json['id'],
      sku: json['sku'] ?? '',
      name: json['name'] ?? '',
      imageUrl: json['image_url'],
      rentalPrice: (json['rental_price'] as num?)?.toDouble() ?? 0.0,
    );
  }
}

class EventItem {
  final String id;
  final String eventId;
  final String articleId;
  final String? variantId;
  final int quantity;
  final double? priceSnapshot;
  final DateTime createdAt;
  final ArticleLite? article;
  final EventItemVariant? variant;
  final double? price;

  EventItem({
    required this.id,
    required this.eventId,
    required this.articleId,
    this.variantId,
    required this.quantity,
    this.priceSnapshot,
    required this.createdAt,
    this.article,
    this.variant,
    this.price,
  });

  /// Effective price per unit — falls back through snapshot → variant
  /// rental → the joined `price` column computed by the backend.
  double get unitPrice {
    if (priceSnapshot != null) return priceSnapshot!;
    if (variant != null) return variant!.rentalPrice;
    if (price != null) return price!;
    return 0.0;
  }

  double get lineTotal => unitPrice * quantity;

  factory EventItem.fromJson(Map<String, dynamic> json) {
    return EventItem(
      id: json['id'],
      eventId: json['event_id'],
      articleId: json['article_id'],
      variantId: json['variant_id'],
      quantity: json['quantity'],
      priceSnapshot: (json['price_snapshot'] as num?)?.toDouble(),
      createdAt: DateTime.parse(json['created_at']),
      article:
          json['article'] != null ? ArticleLite.fromJson(json['article']) : null,
      variant: json['variant'] != null
          ? EventItemVariant.fromJson(json['variant'])
          : null,
      price: json['price'] != null ? (json['price'] as num).toDouble() : null,
    );
  }
}
