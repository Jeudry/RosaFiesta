import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:fl_chart/fl_chart.dart';
import '../debrief_provider.dart';
import 'package:frontend/core/app_theme.dart';

class EventDebriefScreen extends StatefulWidget {
  final String eventId;

  const EventDebriefScreen({super.key, required this.eventId});

  @override
  State<EventDebriefScreen> createState() => _EventDebriefScreenState();
}

class _EventDebriefScreenState extends State<EventDebriefScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<DebriefProvider>().fetchDebrief(widget.eventId);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Análisis Post-Evento'),
      ),
      body: Consumer<DebriefProvider>(
        builder: (context, provider, child) {
          if (provider.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (provider.error != null) {
            return Center(child: Text('Error: ${provider.error}'));
          }

          final debrief = provider.debrief;
          if (debrief == null) {
            return const Center(child: Text('No hay datos de análisis disponibles.'));
          }

          return SingleChildScrollView(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _buildScoreCard(debrief.punctualityScore),
                const SizedBox(height: 24),
                _buildCompletionStats(debrief.completionStats),
                const SizedBox(height: 24),
                _buildBudgetComparison(debrief.budgetAnalysis),
                const SizedBox(height: 24),
                if (debrief.delayedCritical.isNotEmpty) ...[
                  const Text(
                    'Retrasos Críticos',
                    style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 12),
                  ...debrief.delayedCritical.map((info) => _buildDelayedItem(info)),
                ],
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _buildScoreCard(double score) {
    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [AppColors.primary, AppColors.primary.withOpacity(0.7)],
        ),
        borderRadius: BorderRadius.circular(24),
        boxShadow: AppDecorations.softShadow,
      ),
      child: Row(
        children: [
          SizedBox(
            height: 80,
            width: 80,
            child: PieChart(
              PieChartData(
                sections: [
                  PieChartSectionData(
                    value: score,
                    color: Colors.white,
                    radius: 8,
                    showTitle: false,
                  ),
                  PieChartSectionData(
                    value: 100 - score,
                    color: Colors.white.withOpacity(0.2),
                    radius: 8,
                    showTitle: false,
                  ),
                ],
                centerSpaceRadius: 30,
                sectionsSpace: 0,
              ),
            ),
          ),
          const SizedBox(width: 24),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Puntualidad General',
                style: TextStyle(color: Colors.white70, fontSize: 14),
              ),
              Text(
                '${score.toStringAsFixed(0)}%',
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 32,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildCompletionStats(dynamic stats) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Progreso Final',
          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 16),
        _buildStatRow('Tareas Completadas', stats.completedTasks, stats.totalTasks, Colors.blue),
        const SizedBox(height: 12),
        _buildStatRow('Cronograma Ejecutado', stats.completedTimeline, stats.totalTimeline, Colors.purple),
      ],
    );
  }

  Widget _buildStatRow(String label, int current, int total, Color color) {
    final progress = total > 0 ? current / total : 0.0;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(label, style: const TextStyle(fontSize: 14, color: Colors.grey)),
            Text('$current / $total', style: const TextStyle(fontWeight: FontWeight.bold)),
          ],
        ),
        const SizedBox(height: 8),
        LinearProgressIndicator(
          value: progress,
          backgroundColor: color.withOpacity(0.1),
          valueColor: AlwaysStoppedAnimation<Color>(color),
          borderRadius: BorderRadius.circular(4),
          minHeight: 8,
        ),
      ],
    );
  }

  Widget _buildBudgetComparison(dynamic budget) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        boxShadow: AppDecorations.softShadow,
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Análisis de Presupuesto',
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 20),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              _buildPriceColumn('Estimado', budget.estimatedBudget, Colors.grey),
              _buildPriceColumn('Real', budget.actualSpent, budget.isOverBudget ? Colors.red : Colors.green),
            ],
          ),
          const Divider(height: 32),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text('Diferencia', style: TextStyle(color: Colors.grey)),
              Text(
                '${budget.difference >= 0 ? "+" : ""}\$${budget.difference.toStringAsFixed(2)}',
                style: TextStyle(
                  fontWeight: FontWeight.bold,
                  color: budget.isOverBudget ? Colors.red : Colors.green,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildPriceColumn(String label, double value, Color color) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
        Text(
          '\$${value.toStringAsFixed(2)}',
          style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: color),
        ),
      ],
    );
  }

  Widget _buildDelayedItem(dynamic info) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.red.withOpacity(0.05),
        border: Border.all(color: Colors.red.withOpacity(0.2)),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Row(
        children: [
          const Icon(Icons.warning_amber_rounded, color: Colors.red),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(info.title, style: const TextStyle(fontWeight: FontWeight.bold)),
                Text(
                  'Retraso: ${_formatDuration(info.delay)}',
                  style: const TextStyle(color: Colors.red, fontSize: 13),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  String _formatDuration(Duration duration) {
    if (duration.inMinutes < 60) {
      return '${duration.inMinutes} min';
    }
    return '${duration.inHours}h ${duration.inMinutes % 60}m';
  }
}
