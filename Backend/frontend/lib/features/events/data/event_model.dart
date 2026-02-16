class Event {
  final String id;
  final String userId;
  final String name;
  final DateTime date;
  final String location;
  final int guestCount;
  final double budget;
  final String status;

  Event({
    required this.id,
    required this.userId,
    required this.name,
    required this.date,
    required this.location,
    required this.guestCount,
    required this.budget,
    required this.status,
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
      status: json['status'],
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
      'status': status,
    };
  }
}
