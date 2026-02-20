import 'package:json_annotation/json_annotation.dart';

part 'event_debrief_model.g.dart';

@JsonSerializable()
class EventDebrief {
  @JsonKey(name: 'punctuality_score')
  final double punctualityScore;
  
  @JsonKey(name: 'delayed_critical')
  final List<DelayedItemInfo> delayedCritical;
  
  @JsonKey(name: 'budget_analysis')
  final BudgetSummary budgetAnalysis;
  
  @JsonKey(name: 'completion_stats')
  final CompletionStats completionStats;

  EventDebrief({
    required this.punctualityScore,
    required this.delayedCritical,
    required this.budgetAnalysis,
    required this.completionStats,
  });

  factory EventDebrief.fromJson(Map<String, dynamic> json) => _$EventDebriefFromJson(json);
  Map<String, dynamic> toJson() => _$EventDebriefToJson(this);
}

@JsonSerializable()
class DelayedItemInfo {
  final String title;
  
  @JsonKey(name: 'expected_time')
  final DateTime expectedTime;
  
  @JsonKey(name: 'actual_time')
  final DateTime actualTime;
  
  final Duration delay;

  DelayedItemInfo({
    required this.title,
    required this.expectedTime,
    required this.actualTime,
    required this.delay,
  });

  factory DelayedItemInfo.fromJson(Map<String, dynamic> json) => _$DelayedItemInfoFromJson(json);
  Map<String, dynamic> toJson() => _$DelayedItemInfoToJson(this);
}

@JsonSerializable()
class BudgetSummary {
  @JsonKey(name: 'estimated_budget')
  final double estimatedBudget;
  
  @JsonKey(name: 'actual_spent')
  final double actualSpent;
  
  final double difference;
  
  @JsonKey(name: 'is_over_budget')
  final bool isOverBudget;

  BudgetSummary({
    required this.estimatedBudget,
    required this.actualSpent,
    required this.difference,
    required this.isOverBudget,
  });

  factory BudgetSummary.fromJson(Map<String, dynamic> json) => _$BudgetSummaryFromJson(json);
  Map<String, dynamic> toJson() => _$BudgetSummaryToJson(this);
}

@JsonSerializable()
class CompletionStats {
  @JsonKey(name: 'total_tasks')
  final int totalTasks;
  
  @JsonKey(name: 'completed_tasks')
  final int completedTasks;
  
  @JsonKey(name: 'total_timeline')
  final int totalTimeline;
  
  @JsonKey(name: 'completed_timeline')
  final int completedTimeline;

  CompletionStats({
    required this.totalTasks,
    required this.completedTasks,
    required this.totalTimeline,
    required this.completedTimeline,
  });

  factory CompletionStats.fromJson(Map<String, dynamic> json) => _$CompletionStatsFromJson(json);
  Map<String, dynamic> toJson() => _$CompletionStatsToJson(this);
}
