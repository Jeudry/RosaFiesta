class EventColor {
  final String id;
  final String eventId;
  final String colorHex;
  final int sortOrder;

  EventColor({
    required this.id,
    required this.eventId,
    required this.colorHex,
    this.sortOrder = 0,
  });

  factory EventColor.fromJson(Map<String, dynamic> json) {
    return EventColor(
      id: json['id'],
      eventId: json['event_id'],
      colorHex: json['color_hex'],
      sortOrder: json['sort_order'] ?? 0,
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'event_id': eventId,
        'color_hex': colorHex,
        'sort_order': sortOrder,
      };
}