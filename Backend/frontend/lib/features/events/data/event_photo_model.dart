class EventPhoto {
  final String id;
  final String eventId;
  final String url;
  final String? caption;
  final DateTime uploadedAt;

  EventPhoto({
    required this.id,
    required this.eventId,
    required this.url,
    this.caption,
    required this.uploadedAt,
  });

  factory EventPhoto.fromJson(Map<String, dynamic> json) {
    return EventPhoto(
      id: json['id'],
      eventId: json['event_id'],
      url: json['url'],
      caption: json['caption'],
      uploadedAt: DateTime.parse(json['uploaded_at']),
    );
  }
}