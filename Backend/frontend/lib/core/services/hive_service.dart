import 'package:hive_flutter/hive_flutter.dart';
import '../../features/tasks/data/task_model.dart';
import '../../features/events/data/timeline_model.dart';
import '../models/sync_action.dart';

class HiveService {
  static const String tasksBoxName = 'tasks';
  static const String timelineBoxName = 'timeline';
  static const String syncBoxName = 'sync_queue';

  static Future<void> init() async {
    await Hive.initFlutter();

    // Register Adapters
    Hive.registerAdapter(EventTaskAdapter());
    Hive.registerAdapter(TimelineItemAdapter());
    Hive.registerAdapter(SyncOperationAdapter());
    Hive.registerAdapter(SyncActionAdapter());

    // Open Boxes
    await Hive.openBox<EventTask>(tasksBoxName);
    await Hive.openBox<TimelineItem>(timelineBoxName);
    await Hive.openBox<SyncAction>(syncBoxName);
  }

  // Generic methods for box access
  static Box<T> getBox<T>(String name) => Hive.box<T>(name);

  // Helper for tasks
  static Box<EventTask> get tasksBox => getBox<EventTask>(tasksBoxName);
  static Box<TimelineItem> get timelineBox => getBox<TimelineItem>(timelineBoxName);
  static Box<SyncAction> get syncBox => getBox<SyncAction>(syncBoxName);

  static Future<void> clearAll() async {
    await tasksBox.clear();
    await timelineBox.clear();
    await syncBox.clear();
  }
}
