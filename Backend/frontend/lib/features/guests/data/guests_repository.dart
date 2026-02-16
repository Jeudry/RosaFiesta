import '../../../core/api_client.dart';
import 'guest_model.dart';

class GuestsRepository {
  Future<List<Guest>> getGuests(String eventId) async {
    final response = await ApiClient.get('/v1/events/$eventId/guests');
    final List<dynamic> data = response;
    return data.map((json) => Guest.fromJson(json)).toList();
  }

  Future<Guest> addGuest(String eventId, Guest guest) async {
    final response = await ApiClient.post('/v1/events/$eventId/guests', guest.toJson());
    return Guest.fromJson(response);
  }

  Future<Guest> updateGuest(String guestId, Map<String, dynamic> updates) async {
    final response = await ApiClient.put('/v1/guests/$guestId', updates);
    return Guest.fromJson(response);
  }

  Future<void> deleteGuest(String guestId) async {
    await ApiClient.delete('/v1/guests/$guestId');
  }
}
