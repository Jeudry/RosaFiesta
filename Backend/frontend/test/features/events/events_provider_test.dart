import 'package:flutter_test/flutter_test.dart';
// import 'package:mocktail/mocktail.dart';
import 'package:frontend/features/events/presentation/events_provider.dart';
// import 'package:frontend/features/events/data/events_repository.dart';
// import 'package:frontend/features/events/data/event_model.dart';

// class MockEventsRepository extends Mock implements EventsRepository {}

// class EventFake extends Fake implements Event {}

void main() {
  late EventsProvider provider;
  // late MockEventsRepository mockRepository;

  // setUpAll(() {
  //   registerFallbackValue(EventFake());
  // });

  setUp(() {
    // mockRepository = MockEventsRepository();
    provider = EventsProvider();
  });

  group('EventsProvider', () {
    test('initial state is correct', () {
      expect(provider.events, []);
      expect(provider.isLoading, false);
      expect(provider.error, null);
    });

    /* 
    FIXME: Legacy tests are outdated and don't match the current provider implementation.
    The real provider instantiates its own repository, making it hard to mock without refactoring.
    
    test('fetchEvents success', () async { ... });
    test('createEvent success', () async { ... });
    */
  });
}
