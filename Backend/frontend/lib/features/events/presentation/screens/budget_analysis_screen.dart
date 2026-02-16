
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:fl_chart/fl_chart.dart';
import '../../data/event_model.dart';
import '../events_provider.dart';
import '../../../categories/presentation/categories_provider.dart';

class BudgetAnalysisScreen extends StatefulWidget {
  final Event event;

  const BudgetAnalysisScreen({super.key, required this.event});

  @override
  State<BudgetAnalysisScreen> createState() => _BudgetAnalysisScreenState();
}

class _BudgetAnalysisScreenState extends State<BudgetAnalysisScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      Provider.of<CategoriesProvider>(context, listen: false).fetchCategories();
      Provider.of<EventsProvider>(context, listen: false).fetchEventItems(widget.event.id);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Análisis de Presupuesto'),
      ),
      body: Consumer2<EventsProvider, CategoriesProvider>(
        builder: (context, eventsProvider, categoriesProvider, child) {
          final spending = eventsProvider.getCategorySpending(categoriesProvider.categories);
          final totalSpent = eventsProvider.totalSpent;
          
          return SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _buildSummaryCard(totalSpent),
                const SizedBox(height: 24),
                const Text(
                  'Desglose por Categoría',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 16),
                if (spending.isEmpty)
                  const Center(child: Padding(
                    padding: EdgeInsets.all(32.0),
                    child: Text('No hay gastos registrados'),
                  ))
                else
                  SizedBox(
                    height: 300,
                    child: PieChart(
                      PieChartData(
                        sections: _buildChartSections(spending),
                        centerSpaceRadius: 40,
                        sectionsSpace: 2,
                      ),
                    ),
                  ),
                const SizedBox(height: 24),
                _buildLegend(spending),
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _buildSummaryCard(double totalSpent) {
    final remaining = widget.event.budget - totalSpent;
    final isOverBudget = remaining < 0;

    return Card(
      elevation: 4,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                _buildStat('Presupuesto', '\$${widget.event.budget.toStringAsFixed(2)}', Colors.grey),
                _buildStat('Gastado', '\$${totalSpent.toStringAsFixed(2)}', Colors.blue),
              ],
            ),
            const SizedBox(height: 20),
            LinearProgressIndicator(
              value: widget.event.budget > 0 ? (totalSpent / widget.event.budget).clamp(0.0, 1.0) : 0.0,
              backgroundColor: Colors.grey[200],
              color: isOverBudget ? Colors.red : Colors.green,
              minHeight: 12,
              borderRadius: BorderRadius.circular(6),
            ),
            const SizedBox(height: 20),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  isOverBudget ? 'Exceso' : 'Saldo',
                  style: const TextStyle(fontWeight: FontWeight.bold),
                ),
                Text(
                  '\$${remaining.abs().toStringAsFixed(2)}',
                  style: TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                    color: isOverBudget ? Colors.red : Colors.green,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStat(String label, String value, Color color) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
        Text(
          value,
          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: color),
        ),
      ],
    );
  }

  List<PieChartSectionData> _buildChartSections(Map<String, double> spending) {
    final List<Color> colors = [
      Colors.blue,
      Colors.green,
      Colors.orange,
      Colors.purple,
      Colors.red,
      Colors.teal,
      Colors.amber,
    ];

    int index = 0;
    return spending.entries.map((entry) {
      final color = colors[index % colors.length];
      index++;
      return PieChartSectionData(
        color: color,
        value: entry.value,
        title: '${(entry.value / spending.values.reduce((a, b) => a + b) * 100).toStringAsFixed(0)}%',
        radius: 60,
        titleStyle: const TextStyle(
          fontSize: 14,
          fontWeight: FontWeight.bold,
          color: Colors.white,
        ),
      );
    }).toList();
  }

  Widget _buildLegend(Map<String, double> spending) {
    final List<Color> colors = [
      Colors.blue,
      Colors.green,
      Colors.orange,
      Colors.purple,
      Colors.red,
      Colors.teal,
      Colors.amber,
    ];

    int index = 0;
    return Column(
      children: spending.entries.map((entry) {
        final color = colors[index % colors.length];
        index++;
        return Padding(
          padding: const EdgeInsets.symmetric(vertical: 4.0),
          child: Row(
            children: [
              Container(width: 16, height: 16, color: color),
              const SizedBox(width: 8),
              Expanded(child: Text(entry.key)),
              Text(
                '\$${entry.value.toStringAsFixed(2)}',
                style: const TextStyle(fontWeight: FontWeight.bold),
              ),
            ],
          ),
        );
      }).toList(),
    );
  }
}
