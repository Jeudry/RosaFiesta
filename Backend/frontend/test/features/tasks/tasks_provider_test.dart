import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:hive_flutter/hive_flutter.dart';
import 'package:frontend/features/tasks/presentation/tasks_provider.dart';
import 'package:frontend/features/tasks/data/tasks_repository.dart';
import 'package:frontend/features/tasks/data/task_model.dart';
import 'package:frontend/core/models/sync_action.dart';
import 'package:frontend/core/services/notification_service.dart';
import 'package:frontend/core/services/sync_service.dart';

class MockTasksRepository extends Mock implements EventTasksRepository {}

class MockNotificationService extends Mock implements NotificationServiceInterface {}

class MockSyncService extends Mock implements SyncService {}

class EventTaskFake extends Fake implements EventTask {}

void main() {
  late EventTasksProvider provider;
  late MockTasksRepository mockRepository;
  late MockNotificationService mockNotificationService;
  late MockSyncService mockSyncService;

  setUpAll(() async {
    TestWidgetsFlutterBinding.ensureInitialized();
    const pathChannel = MethodChannel('plugins.flutter.io/path_provider');
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(pathChannel, (call) async {
      if (call.method == 'getApplicationDocumentsDirectory') return '/tmp/hive_test';
      return null;
    });
    registerFallbackValue(EventTaskFake());
    registerFallbackValue(SyncOperation.create);
    await Hive.initFlutter('/tmp/hive_test');
    Hive.registerAdapter(EventTaskAdapter());
    Hive.registerAdapter(SyncOperationAdapter());
    Hive.registerAdapter(SyncActionAdapter());
    await Hive.openBox<EventTask>('tasks');
    await Hive.openBox<SyncAction>('sync_queue');
    await Hive.openBox<dynamic>('timeline');
  });

  tearDownAll(() async {
    await Hive.close();
  });

  setUp(() {
    mockRepository = MockTasksRepository();
    mockNotificationService = MockNotificationService();
    mockSyncService = MockSyncService();
    provider = EventTasksProvider(
      mockRepository,
      notificationService: mockNotificationService,
      syncService: mockSyncService,
    );
  });

  group('EventTasksProvider', () {
    test('fetchTasks success', () async {
      final task = EventTask(
        id: '1',
        eventId: 'event-1',
        title: 'Test Task',
        isCompleted: false,
        createdAt: DateTime.now(),
        updatedAt: DateTime.now(),
      );
      when(() => mockRepository.getTasks('event-1')).thenAnswer((_) async => [task]);

      await provider.fetchTasks('event-1');

      expect(provider.tasks, [task]);
      expect(provider.isLoading, false);
      expect(provider.progress, 0.0);
    });

    test('toggleTask success - queues sync action', () async {
      final task = EventTask(
        id: '1',
        eventId: 'event-1',
        title: 'Test Task',
        isCompleted: false,
        createdAt: DateTime.now(),
        updatedAt: DateTime.now(),
      );

      when(() => mockSyncService.addAction(
        entityType: any(named: 'entityType'),
        entityId: any(named: 'entityId'),
        operation: any(named: 'operation'),
        payload: any(named: 'payload'),
      )).thenAnswer((_) async {});

      // Load task into provider
      when(() => mockRepository.getTasks('event-1')).thenAnswer((_) async => [task]);
      await provider.fetchTasks('event-1');

      // Toggle
      final success = await provider.toggleTask('1', 'event-1', true);

      expect(success, true);
      // toggleTask queues a sync action, does not call updateTask directly
      verify(() => mockSyncService.addAction(
        entityType: 'task',
        entityId: '1',
        operation: SyncOperation.update,
        payload: {'is_completed': true},
      )).called(1);
    });
  });
}
