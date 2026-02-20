class EventReview {
  final String id;
  final String userId;
  final String eventId;
  final int rating;
  final String comment;
  final DateTime createdAt;
  final String? userName;
  final String? avatar;

  EventReview({
    required this.id,
    required this.userId,
    required this.eventId,
    required this.rating,
    required this.comment,
    required this.createdAt,
    this.userName,
    this.avatar,
  });

  factory EventReview.fromJson(Map<String, dynamic> json) {
    return EventReview(
      id: json['id'],
      userId: json['user_id'],
      eventId: json['event_id'],
      rating: json['rating'],
      comment: json['comment'],
      createdAt: DateTime.parse(json['created'] ?? json['created_at']),
      userName: json['user']?['user_name'],
      avatar: json['user']?['avatar'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'user_id': userId,
      'event_id': eventId,
      'rating': rating,
      'comment': comment,
      'created_at': createdAt.toIso8601String(),
    };
  }
}
