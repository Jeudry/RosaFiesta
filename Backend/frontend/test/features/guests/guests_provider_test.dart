import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:frontend/features/guests/presentation/guests_provider.dart';
import 'package:frontend/features/guests/data/guests_repository.dart';
import 'package:frontend/features/guests/data/guest_model.dart';

class MockGuestsRepository extends Mock implements GuestsRepository {}

class GuestFake extends Fake implements Guest {}

void main() {
  late GuestsProvider provider;
  late MockGuestsRepository mockRepository;

  setUpAll(() {
    registerFallbackValue(GuestFake());
  });

  setUp(() {
    mockRepository = MockGuestsRepository();
    provider = GuestsProvider(mockRepository);
  });

  group('GuestsProvider', () {
    final guest = Guest(
      id: '1',
      eventId: 'event-1',
      name: 'Test Guest',
      rsvpStatus: 'pending',
      plusOne: false,
      createdAt: DateTime.now(),
      updatedAt: DateTime.now(),
    );

    test('fetchGuests success', () async {
      when(() => mockRepository.getGuests('event-1')).thenAnswer((_) async => [guest]);

      await provider.fetchGuests('event-1');

      expect(provider.guests, [guest]);
      expect(provider.isLoading, false);
      verify(() => mockRepository.getGuests('event-1')).called(1);
    });

    test('addGuest success', () async {
      when(() => mockRepository.addGuest(any(), any())).thenAnswer((_) async => guest);
      when(() => mockRepository.getGuests(any())).thenAnswer((_) async => [guest]);

      final success = await provider.addGuest('event-1', guest);

      expect(success, true);
      verify(() => mockRepository.addGuest('event-1', any())).called(1);
    });

    test('updateRSVP success', () async {
      when(() => mockRepository.updateGuest(any(), any())).thenAnswer((_) async => guest);
      when(() => mockRepository.getGuests(any())).thenAnswer((_) async => [guest]);

      final success = await provider.updateRSVP('1', 'event-1', 'confirmed');

      expect(success, true);
      verify(() => mockRepository.updateGuest('1', {'rsvp_status': 'confirmed'})).called(1);
    });

    test('deleteGuest success', () async {
      when(() => mockRepository.deleteGuest(any())).thenAnswer((_) async => {});
      when(() => mockRepository.getGuests(any())).thenAnswer((_) async => []);

      final success = await provider.deleteGuest('1', 'event-1');

      expect(success, true);
      verify(() => mockRepository.deleteGuest('1')).called(1);
    });
  });
}
