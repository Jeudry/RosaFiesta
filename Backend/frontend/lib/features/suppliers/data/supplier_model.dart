class Supplier {
  final String id;
  final String userId;
  final String name;
  final String? contactName;
  final String? email;
  final String? phone;
  final String? website;
  final String? notes;
  final DateTime createdAt;
  final DateTime updatedAt;

  Supplier({
    required this.id,
    required this.userId,
    required this.name,
    this.contactName,
    this.email,
    this.phone,
    this.website,
    this.notes,
    required this.createdAt,
    required this.updatedAt,
  });

  factory Supplier.fromJson(Map<String, dynamic> json) {
    return Supplier(
      id: json['id'],
      userId: json['user_id'],
      name: json['name'],
      contactName: json['contact_name'],
      email: json['email'],
      phone: json['phone'],
      website: json['website'],
      notes: json['notes'],
      createdAt: DateTime.parse(json['created_at']),
      updatedAt: DateTime.parse(json['updated_at']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (contactName != null) 'contact_name': contactName,
      if (email != null) 'email': email,
      if (phone != null) 'phone': phone,
      if (website != null) 'website': website,
      if (notes != null) 'notes': notes,
      'name': name,
    };
  }
}
