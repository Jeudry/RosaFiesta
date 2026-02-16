import 'package:flutter/foundation.dart';
import '../data/guest_model.dart';
import '../data/guests_repository.dart';
import '../../../core/utils/error_translator.dart';

class GuestsProvider with ChangeNotifier {
  final GuestsRepository _repository;
  List<Guest> _guests = [];
  bool _isLoading = false;
  String? _error;

  GuestsProvider(this._repository);

  List<Guest> get guests => _guests;
  bool get isLoading => _isLoading;
  String? get error => _error;

  Future<void> fetchGuests(String eventId) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _guests = await _repository.getGuests(eventId);
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> addGuest(String eventId, Guest guest) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      await _repository.addGuest(eventId, guest);
      await fetchGuests(eventId); // Refresh list
      return true;
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> updateRSVP(String guestId, String eventId, String status) async {
    try {
      await _repository.updateGuest(guestId, {'rsvp_status': status});
      await fetchGuests(eventId); // Refresh list
      return true;
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
      notifyListeners();
      return false;
    }
  }

  Future<bool> deleteGuest(String guestId, String eventId) async {
    try {
      await _repository.deleteGuest(guestId);
      await fetchGuests(eventId); // Refresh list
      return true;
    } catch (e) {
      _error = ErrorTranslator.translate(e.toString());
      notifyListeners();
      return false;
    }
  }
}
