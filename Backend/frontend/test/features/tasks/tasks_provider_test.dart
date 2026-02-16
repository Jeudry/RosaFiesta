import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:frontend/features/tasks/presentation/tasks_provider.dart';
import 'package:frontend/features/tasks/data/tasks_repository.dart';
import 'package:frontend/features/tasks/data/task_model.dart';

class MockTasksRepository extends Mock implements EventTasksRepository {}

class EventTaskFake extends Fake implements EventTask {}

void main() {
  late EventTasksProvider provider;
  late MockTasksRepository mockRepository;

  setUpAll(() {
    registerFallbackValue(EventTaskFake());
  });

  setUp(() {
    mockRepository = MockTasksRepository();
    provider = EventTasksProvider(mockRepository);
  });

  group('EventTasksProvider', () {
    final task = EventTask(
      id: '1',
      eventId: 'event-1',
      title: 'Test Task',
      isCompleted: false,
      createdAt: DateTime.now(),
      updatedAt: DateTime.now(),
    );

    test('fetchTasks success', () async {
      when(() => mockRepository.getTasks('event-1')).thenAnswer((_) async => [task]);

      await provider.fetchTasks('event-1');

      expect(provider.tasks, [task]);
      expect(provider.isLoading, false);
      expect(provider.progress, 0.0);
    });

    test('toggleTask success updates progress', () async {
      final completedTask = EventTask(
        id: '1',
        eventId: 'event-1',
        title: 'Test Task',
        isCompleted: true,
        createdAt: DateTime.now(),
        updatedAt: DateTime.now(),
      );
      
      when(() => mockRepository.updateTask(any(), any())).thenAnswer((_) async => completedTask);
      when(() => mockRepository.getTasks(any())).thenAnswer((_) async => [completedTask]);

      final success = await provider.toggleTask('1', 'event-1', true);

      expect(success, true);
      expect(provider.progress, 1.0);
      verify(() => mockRepository.updateTask('1', {'is_completed': true})).called(1);
    });
  });
}
