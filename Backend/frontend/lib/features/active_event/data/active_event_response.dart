import '../../events/data/event_model.dart';

/// Response shape of `GET /v1/events/active`: the backend returns the draft
/// event alongside its hydrated items in a single payload.
class ActiveEventResponse {
  final Event event;
  final List<EventItem> items;

  ActiveEventResponse({required this.event, required this.items});

  factory ActiveEventResponse.fromJson(Map<String, dynamic> json) {
    final eventJson = json['event'] as Map<String, dynamic>;
    final itemsJson = (json['items'] as List<dynamic>?) ?? const [];
    return ActiveEventResponse(
      event: Event.fromJson(eventJson),
      items: itemsJson
          .map((e) => EventItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
  }
}
