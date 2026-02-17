class EventMessage {
  final String id;
  final String eventId;
  final String senderId;
  final String content;
  final DateTime createdAt;
  final String? senderName;

  EventMessage({
    required this.id,
    required this.eventId,
    required this.senderId,
    required this.content,
    required this.createdAt,
    this.senderName,
  });

  factory EventMessage.fromJson(Map<String, dynamic> json) {
    return EventMessage(
      id: json['id'],
      eventId: json['event_id'],
      senderId: json['sender_id'],
      content: json['content'],
      createdAt: DateTime.parse(json['created_at']),
      senderName: json['sender_name'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'event_id': eventId,
      'sender_id': senderId,
      'content': content,
      'created_at': createdAt.toIso8601String(),
      'sender_name': senderName,
    };
  }
}
