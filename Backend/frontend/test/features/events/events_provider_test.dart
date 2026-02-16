import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:frontend/features/events/presentation/events_provider.dart';
import 'package:frontend/features/events/data/events_repository.dart';
import 'package:frontend/features/events/data/event_model.dart';

class MockEventsRepository extends Mock implements EventsRepository {}

class EventFake extends Fake implements Event {}

void main() {
  late EventsProvider provider;
  late MockEventsRepository mockRepository;

  setUpAll(() {
    registerFallbackValue(EventFake());
  });

  setUp(() {
    mockRepository = MockEventsRepository();
    provider = EventsProvider(repository: mockRepository);
  });

  group('EventsProvider', () {
    test('initial state is correct', () {
      expect(provider.events, []);
      expect(provider.isLoading, false);
      expect(provider.error, null);
    });

    test('fetchEvents success', () async {
      final events = [
        Event(
          id: '1',
          userId: 'user1',
          name: 'Event 1',
          date: DateTime.now(),
          location: 'Loc 1',
          guestCount: 10,
          budget: 100.0,
          status: 'planning',
        )
      ];
      when(() => mockRepository.getEvents()).thenAnswer((_) async => events);

      await provider.fetchEvents();

      expect(provider.events, events);
      expect(provider.isLoading, false);
      expect(provider.error, null);
    });

    test('fetchEvents failure handles error', () async {
      when(() => mockRepository.getEvents()).thenThrow(Exception('Failed to fetch'));

      await provider.fetchEvents();

      expect(provider.events, []);
      expect(provider.isLoading, false);
      expect(provider.error, isNotNull);
    });

    test('createEvent success', () async {
      final date = DateTime.now();
      when(() => mockRepository.createEvent(any())).thenAnswer((_) async => Event(
            id: '2',
            userId: 'user1',
            name: 'New Event',
            date: date,
            location: 'Loc 2',
            guestCount: 20,
            budget: 200.0,
            status: 'planning',
          ));
        
      when(() => mockRepository.getEvents()).thenAnswer((_) async => []);

      final success = await provider.createEvent('New Event', date, 'Loc 2', 200.0, 20);

      expect(success, true);
      expect(provider.isLoading, false);
      expect(provider.error, null);
      verify(() => mockRepository.getEvents()).called(1);
    });

    test('addItemToEvent success', () async {
      when(() => mockRepository.addItem(any(), any(), any())).thenAnswer((_) async => true);
      
      final success = await provider.addItemToEvent('1', 'art-1', 2);
      
      expect(success, true);
      verify(() => mockRepository.addItem('1', 'art-1', 2)).called(1);
    });

    test('getEventItems success', () async {
      when(() => mockRepository.getItems(any())).thenAnswer((_) async => []);
      
      final items = await provider.getEventItems('1');
      
      expect(items, []);
      verify(() => mockRepository.getItems('1')).called(1);
    });
  });
}
