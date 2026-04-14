class Supplier {
  final String id;
  final String userId;
  final String name;
  final String contactName;
  final String email;
  final String phone;
  final String website;
  final String notes;
  final DateTime createdAt;
  final DateTime updatedAt;

  Supplier({
    required this.id,
    required this.userId,
    required this.name,
    this.contactName = '',
    this.email = '',
    this.phone = '',
    this.website = '',
    this.notes = '',
    required this.createdAt,
    required this.updatedAt,
  });

  factory Supplier.fromJson(Map<String, dynamic> json) {
    return Supplier(
      id: json['id'],
      userId: json['user_id'],
      name: json['name'] ?? '',
      contactName: json['contact_name'] ?? '',
      email: json['email'] ?? '',
      phone: json['phone'] ?? '',
      website: json['website'] ?? '',
      notes: json['notes'] ?? '',
      createdAt: DateTime.parse(json['created_at']),
      updatedAt: DateTime.parse(json['updated_at']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'user_id': userId,
      'name': name,
      'contact_name': contactName,
      'email': email,
      'phone': phone,
      'website': website,
      'notes': notes,
      'created_at': createdAt.toIso8601String(),
      'updated_at': updatedAt.toIso8601String(),
    };
  }

  Supplier copyWith({
    String? id,
    String? userId,
    String? name,
    String? contactName,
    String? email,
    String? phone,
    String? website,
    String? notes,
    DateTime? createdAt,
    DateTime? updatedAt,
  }) {
    return Supplier(
      id: id ?? this.id,
      userId: userId ?? this.userId,
      name: name ?? this.name,
      contactName: contactName ?? this.contactName,
      email: email ?? this.email,
      phone: phone ?? this.phone,
      website: website ?? this.website,
      notes: notes ?? this.notes,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }
}
