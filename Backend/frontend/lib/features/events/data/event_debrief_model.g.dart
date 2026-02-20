// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event_debrief_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventDebrief _$EventDebriefFromJson(Map<String, dynamic> json) => EventDebrief(
      punctualityScore: (json['punctuality_score'] as num).toDouble(),
      delayedCritical: (json['delayed_critical'] as List<dynamic>)
          .map((e) => DelayedItemInfo.fromJson(e as Map<String, dynamic>))
          .toList(),
      budgetAnalysis: BudgetSummary.fromJson(
          json['budget_analysis'] as Map<String, dynamic>),
      completionStats: CompletionStats.fromJson(
          json['completion_stats'] as Map<String, dynamic>),
    );

Map<String, dynamic> _$EventDebriefToJson(EventDebrief instance) =>
    <String, dynamic>{
      'punctuality_score': instance.punctualityScore,
      'delayed_critical': instance.delayedCritical,
      'budget_analysis': instance.budgetAnalysis,
      'completion_stats': instance.completionStats,
    };

DelayedItemInfo _$DelayedItemInfoFromJson(Map<String, dynamic> json) =>
    DelayedItemInfo(
      title: json['title'] as String,
      expectedTime: DateTime.parse(json['expected_time'] as String),
      actualTime: DateTime.parse(json['actual_time'] as String),
      delay: Duration(microseconds: (json['delay'] as num).toInt()),
    );

Map<String, dynamic> _$DelayedItemInfoToJson(DelayedItemInfo instance) =>
    <String, dynamic>{
      'title': instance.title,
      'expected_time': instance.expectedTime.toIso8601String(),
      'actual_time': instance.actualTime.toIso8601String(),
      'delay': instance.delay.inMicroseconds,
    };

BudgetSummary _$BudgetSummaryFromJson(Map<String, dynamic> json) =>
    BudgetSummary(
      estimatedBudget: (json['estimated_budget'] as num).toDouble(),
      actualSpent: (json['actual_spent'] as num).toDouble(),
      difference: (json['difference'] as num).toDouble(),
      isOverBudget: json['is_over_budget'] as bool,
    );

Map<String, dynamic> _$BudgetSummaryToJson(BudgetSummary instance) =>
    <String, dynamic>{
      'estimated_budget': instance.estimatedBudget,
      'actual_spent': instance.actualSpent,
      'difference': instance.difference,
      'is_over_budget': instance.isOverBudget,
    };

CompletionStats _$CompletionStatsFromJson(Map<String, dynamic> json) =>
    CompletionStats(
      totalTasks: (json['total_tasks'] as num).toInt(),
      completedTasks: (json['completed_tasks'] as num).toInt(),
      totalTimeline: (json['total_timeline'] as num).toInt(),
      completedTimeline: (json['completed_timeline'] as num).toInt(),
    );

Map<String, dynamic> _$CompletionStatsToJson(CompletionStats instance) =>
    <String, dynamic>{
      'total_tasks': instance.totalTasks,
      'completed_tasks': instance.completedTasks,
      'total_timeline': instance.totalTimeline,
      'completed_timeline': instance.completedTimeline,
    };
