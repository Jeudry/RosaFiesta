class Guest {
  final String id;
  final String eventId;
  final String name;
  final String? email;
  final String? phone;
  final String rsvpStatus; // pending, confirmed, declined
  final DateTime? confirmedAt;
  final DateTime? declinedAt;
  final bool plusOne;
  final String? dietaryRestrictions;
  final DateTime createdAt;
  final DateTime updatedAt;

  Guest({
    required this.id,
    required this.eventId,
    required this.name,
    this.email,
    this.phone,
    required this.rsvpStatus,
    this.confirmedAt,
    this.declinedAt,
    required this.plusOne,
    this.dietaryRestrictions,
    required this.createdAt,
    required this.updatedAt,
  });

  factory Guest.fromJson(Map<String, dynamic> json) {
    return Guest(
      id: json['id'],
      eventId: json['event_id'],
      name: json['name'],
      email: json['email'],
      phone: json['phone'],
      rsvpStatus: json['rsvp_status'],
      confirmedAt: json['confirmed_at'] != null ? DateTime.parse(json['confirmed_at']) : null,
      declinedAt: json['declined_at'] != null ? DateTime.parse(json['declined_at']) : null,
      plusOne: json['plus_one'] ?? false,
      dietaryRestrictions: json['dietary_restrictions'],
      createdAt: DateTime.parse(json['created_at']),
      updatedAt: DateTime.parse(json['updated_at']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'email': email,
      'phone': phone,
      'rsvp_status': rsvpStatus,
      'plus_one': plusOne,
      'dietary_restrictions': dietaryRestrictions,
    };
  }
}
