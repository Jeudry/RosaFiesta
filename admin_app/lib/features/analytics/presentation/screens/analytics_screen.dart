import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:fl_chart/fl_chart.dart';
import '../../../../core/design_system.dart';
import '../../../../core/router/app_router.dart';
import '../../../../core/api_client/api_client.dart';

class AnalyticsScreen extends StatefulWidget {
  const AnalyticsScreen({super.key});

  @override
  State<AnalyticsScreen> createState() => _AnalyticsScreenState();
}

class _AnalyticsScreenState extends State<AnalyticsScreen> {
  Map<String, dynamic>? _monthlyStats;
  List<dynamic>? _revenueChart;
  List<dynamic>? _topProducts;
  Map<String, dynamic>? _conversionRate;
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    setState(() => _loading = true);
    try {
      final responses = await Future.wait([
        apiClient.getMonthlyStats(),
        apiClient.getRevenueChart(),
        apiClient.getTopProducts(),
        apiClient.getConversionRate(),
      ]);
      _monthlyStats = responses[0].data['data'];
      _revenueChart = responses[1].data['data'] ?? [];
      _topProducts = responses[2].data['data'] ?? [];
      _conversionRate = responses[3].data['data'];
    } catch (e) {
      // Handle error
    }
    setState(() => _loading = false);
  }

  @override
  Widget build(BuildContext context) {
    return AdminScaffold(
      title: 'Analytics',
      showBack: true,
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Monthly summary
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Resumen del Mes', style: GoogleFonts.outfit(fontSize: 18, fontWeight: FontWeight.w600)),
                          const SizedBox(height: 16),
                          Row(
                            children: [
                              Expanded(child: _MiniStat(label: 'Eventos', value: '${_monthlyStats?['events_count'] ?? 0}')),
                              Expanded(child: _MiniStat(label: 'Ingresos', value: 'RD\$${_monthlyStats?['revenue'] ?? 0}')),
                              Expanded(child: _MiniStat(label: 'Clientes', value: '${_monthlyStats?['new_clients'] ?? 0}')),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Revenue chart
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Text('Ingresos (12 meses)', style: GoogleFonts.outfit(fontSize: 16, fontWeight: FontWeight.w600)),
                              TextButton.icon(
                                onPressed: () => apiClient.exportCSV('revenue'),
                                icon: const Icon(Icons.download, size: 16),
                                label: const Text('CSV'),
                              ),
                            ],
                          ),
                          const SizedBox(height: 16),
                          SizedBox(
                            height: 200,
                            child: _revenueChart != null && _revenueChart!.isNotEmpty
                                ? BarChart(
                                    BarChartData(
                                      barGroups: _revenueChart!.asMap().entries.map((e) {
                                        return BarChartGroupData(
                                          x: e.key,
                                          barRods: [
                                            BarChartRodData(
                                              toY: (e.value['revenue'] ?? 0).toDouble(),
                                              color: AppColors.primary,
                                              width: 16,
                                              borderRadius: const BorderRadius.vertical(top: Radius.circular(4)),
                                            ),
                                          ],
                                        );
                                      }).toList(),
                                      titlesData: FlTitlesData(
                                        leftTitles: AxisTitles(
                                          sideTitles: SideTitles(
                                            showTitles: true,
                                            reservedSize: 40,
                                            getTitlesWidget: (value, meta) {
                                              if (value >= 1000) {
                                                return Text('${(value / 1000).toStringAsFixed(0)}K', style: const TextStyle(fontSize: 10));
                                              }
                                              return Text(value.toStringAsFixed(0), style: const TextStyle(fontSize: 10));
                                            },
                                          ),
                                        ),
                                        bottomTitles: AxisTitles(
                                          sideTitles: SideTitles(
                                            showTitles: true,
                                            getTitlesWidget: (value, meta) {
                                              if (value.toInt() < (_revenueChart?.length ?? 0)) {
                                                final month = _revenueChart![value.toInt()]['month'] ?? '';
                                                return Text(month.substring(5, 7), style: const TextStyle(fontSize: 10));
                                              }
                                              return const Text('');
                                            },
                                          ),
                                        ),
                                        topTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
                                        rightTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
                                      ),
                                      borderData: FlBorderData(show: false),
                                      gridData: const FlGridData(show: true, drawVerticalLine: false),
                                    ),
                                  )
                                : const Center(child: Text('No hay datos')),
                          ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Conversion rate
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Tasa de Conversión', style: GoogleFonts.outfit(fontSize: 16, fontWeight: FontWeight.w600)),
                          const SizedBox(height: 16),
                          Row(
                            children: [
                              Expanded(
                                child: Container(
                                  padding: const EdgeInsets.all(16),
                                  decoration: BoxDecoration(
                                    color: AppColors.primary.withValues(alpha: 0.1),
                                    borderRadius: BorderRadius.circular(12),
                                  ),
                                  child: Column(
                                    children: [
                                      Text(
                                        '${_conversionRate?['sent'] ?? 0}',
                                        style: GoogleFonts.outfit(fontSize: 32, fontWeight: FontWeight.w700, color: AppColors.primary),
                                      ),
                                      Text('Cotizaciones enviadas', style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textMuted)),
                                    ],
                                  ),
                                ),
                              ),
                              const SizedBox(width: 12),
                              const Icon(Icons.arrow_forward, color: AppColors.textMuted),
                              const SizedBox(width: 12),
                              Expanded(
                                child: Container(
                                  padding: const EdgeInsets.all(16),
                                  decoration: BoxDecoration(
                                    color: AppColors.success.withValues(alpha: 0.1),
                                    borderRadius: BorderRadius.circular(12),
                                  ),
                                  child: Column(
                                    children: [
                                      Text(
                                        '${_conversionRate?['paid'] ?? 0}',
                                        style: GoogleFonts.outfit(fontSize: 32, fontWeight: FontWeight.w700, color: AppColors.success),
                                      ),
                                      Text('Pagadas', style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textMuted)),
                                    ],
                                  ),
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: 12),
                          Center(
                            child: Text(
                              'Tasa: ${_conversionRate?['rate'] ?? 0}%',
                              style: GoogleFonts.outfit(fontSize: 16, fontWeight: FontWeight.w600, color: AppColors.success),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Top products
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Productos Más Alquilados', style: GoogleFonts.outfit(fontSize: 16, fontWeight: FontWeight.w600)),
                          const SizedBox(height: 12),
                          if (_topProducts != null)
                            ...List.generate(_topProducts!.length > 10 ? 10 : _topProducts!.length, (i) {
                              final product = _topProducts![i];
                              return Padding(
                                padding: const EdgeInsets.symmetric(vertical: 8),
                                child: Row(
                                  children: [
                                    Text('${i + 1}.', style: GoogleFonts.dmSans(fontWeight: FontWeight.w600, color: AppColors.textMuted)),
                                    const SizedBox(width: 8),
                                    Expanded(child: Text(product['name'] ?? '')),
                                    Text('${product['rentals'] ?? 0} alquileres', style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textSecondary)),
                                  ],
                                ),
                              );
                            }),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Export buttons
                  Row(
                    children: [
                      Expanded(
                        child: OutlinedButton.icon(
                          onPressed: () => apiClient.exportCSV('events'),
                          icon: const Icon(Icons.download),
                          label: const Text('Exportar CSV'),
                        ),
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: OutlinedButton.icon(
                          onPressed: () => apiClient.getReportPDF(),
                          icon: const Icon(Icons.picture_as_pdf),
                          label: const Text('Reporte PDF'),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
    );
  }
}

class _MiniStat extends StatelessWidget {
  final String label;
  final String value;

  const _MiniStat({required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Text(value, style: GoogleFonts.outfit(fontSize: 24, fontWeight: FontWeight.w700, color: AppColors.primary)),
        Text(label, style: GoogleFonts.dmSans(fontSize: 12, color: AppColors.textMuted)),
      ],
    );
  }
}
