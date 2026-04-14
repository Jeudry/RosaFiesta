class ReviewPhoto {
  final String id;
  final String reviewId;
  final String photoUrl;
  final String? caption;
  final int sortOrder;

  ReviewPhoto({
    required this.id,
    required this.reviewId,
    required this.photoUrl,
    this.caption,
    required this.sortOrder,
  });

  factory ReviewPhoto.fromJson(Map<String, dynamic> json) {
    return ReviewPhoto(
      id: json['id'],
      reviewId: json['review_id'],
      photoUrl: json['photo_url'],
      caption: json['caption'],
      sortOrder: json['sort_order'] ?? 0,
    );
  }
}

class EventReview {
  final String id;
  final String userId;
  final String eventId;
  final int rating;
  final String comment;
  final DateTime createdAt;
  final String? userName;
  final String? avatar;
  final List<ReviewPhoto> photos;

  EventReview({
    required this.id,
    required this.userId,
    required this.eventId,
    required this.rating,
    required this.comment,
    required this.createdAt,
    this.userName,
    this.avatar,
    this.photos = const [],
  });

  factory EventReview.fromJson(Map<String, dynamic> json) {
    final photosList = json['photos'] as List<dynamic>? ?? [];
    return EventReview(
      id: json['id'],
      userId: json['user_id'],
      eventId: json['event_id'],
      rating: json['rating'],
      comment: json['comment'],
      createdAt: DateTime.parse(json['created'] ?? json['created_at']),
      userName: json['user']?['user_name'],
      avatar: json['user']?['avatar'],
      photos: photosList.map((p) => ReviewPhoto.fromJson(p)).toList(),
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
