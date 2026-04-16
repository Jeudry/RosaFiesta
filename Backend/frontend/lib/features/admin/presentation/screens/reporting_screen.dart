import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:frontend/core/app_colors.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/features/admin/data/stats_repository.dart';
import 'package:frontend/features/admin/data/stats_model.dart';

class ReportingScreen extends StatefulWidget {
  const ReportingScreen({super.key});

  @override
  State<ReportingScreen> createState() => _ReportingScreenState();
}

class _ReportingScreenState extends State<ReportingScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  final _statsRepo = StatsRepository();
  String _selectedPeriod = 'month';
  AdminStats? _stats;
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 4, vsync: this);
    _loadStats();
  }

  Future<void> _loadStats() async {
    setState(() => _isLoading = true);
    try {
      final stats = await _statsRepo.getStats();
      if (mounted) setState(() => _stats = stats);
    } catch (e) {
      debugPrint('Error loading stats: $e');
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);
    return Scaffold(
      backgroundColor: t.base,
      appBar: AppBar(
        backgroundColor: t.card,
        title: Text(
          'Reportes Avanzados',
          style: GoogleFonts.outfit(
            color: t.textPrimary,
            fontWeight: FontWeight.w600,
          ),
        ),
        actions: [
          PopupMenuButton<String>(
            icon: Icon(Icons.calendar_today, color: t.textPrimary),
            onSelected: (value) => setState(() => _selectedPeriod = value),
            itemBuilder: (context) => [
              PopupMenuItem(value: 'week', child: Text('Esta semana')),
              PopupMenuItem(value: 'month', child: Text('Este mes')),
              PopupMenuItem(value: 'quarter', child: Text('Este trimestre')),
              PopupMenuItem(value: 'year', child: Text('Este año')),
            ],
          ),
          IconButton(
            icon: Icon(Icons.download, color: t.textPrimary),
            onPressed: _exportReport,
          ),
        ],
        bottom: TabBar(
          controller: _tabController,
          labelColor: AppColors.hotPink,
          unselectedLabelColor: t.textMuted,
          indicatorColor: AppColors.hotPink,
          tabs: const [
            Tab(text: 'Ingresos'),
            Tab(text: 'Eventos'),
            Tab(text: 'Clientes'),
            Tab(text: 'Inventario'),
          ],
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          _buildRevenueTab(t),
          _buildEventsTab(t),
          _buildClientsTab(t),
          _buildInventoryTab(t),
        ],
      ),
    );
  }

  Widget _buildRevenueTab(RfTheme t) {
    return SingleChildScrollView(
      padding: EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildPeriodSummary(t),
          SizedBox(height: 20),
          _buildRevenueChart(t),
          SizedBox(height: 20),
          _buildIncomeBreakdown(t),
          SizedBox(height: 20),
          _buildTopEventsTable(t),
        ],
      ),
    );
  }

  Widget _buildPeriodSummary(RfTheme t) {
    return Row(
      children: [
        Expanded(
          child: _buildSummaryCard(
            'Ingresos Totales',
            '\$45,230',
            AppColors.teal,
            Icons.trending_up,
            t,
          ),
        ),
        SizedBox(width: 12),
        Expanded(
          child: _buildSummaryCard(
            'Gastos',
            '\$12,450',
            AppColors.coral,
            Icons.trending_down,
            t,
          ),
        ),
        SizedBox(width: 12),
        Expanded(
          child: _buildSummaryCard(
            'Ganancia Neta',
            '\$32,780',
            AppColors.hotPink,
            Icons.account_balance_wallet,
            t,
          ),
        ),
      ],
    );
  }

  Widget _buildSummaryCard(
    String title,
    String value,
    Color color,
    IconData icon,
    RfTheme t,
  ) {
    return Container(
      padding: EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: t.card,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: t.borderFaint),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: color.withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Icon(icon, color: color, size: 16),
              ),
              SizedBox(width: 8),
              Expanded(
                child: Text(
                  title,
                  style: GoogleFonts.dmSans(color: t.textMuted, fontSize: 11),
                ),
              ),
            ],
          ),
          SizedBox(height: 12),
          Text(
            value,
            style: GoogleFonts.outfit(
              color: t.textPrimary,
              fontSize: 22,
              fontWeight: FontWeight.bold,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildRevenueChart(RfTheme t) {
    return Container(
      padding: EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: t.card,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: t.borderFaint),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Ingresos por Mes',
            style: GoogleFonts.outfit(
              color: t.textPrimary,
              fontSize: 18,
              fontWeight: FontWeight.w600,
            ),
          ),
          SizedBox(height: 20),
          SizedBox(
            height: 200,
            child: BarChart(
              BarChartData(
                alignment: BarChartAlignment.spaceAround,
                maxY: 20000,
                barTouchData: BarTouchData(enabled: true),
                titlesData: FlTitlesData(
                  show: true,
                  bottomTitles: AxisTitles(
                    sideTitles: SideTitles(
                      showTitles: true,
                      getTitlesWidget: (value, meta) {
                        const months = [
                          'Ene',
                          'Feb',
                          'Mar',
                          'Abr',
                          'May',
                          'Jun',
                        ];
                        return Text(
                          months[value.toInt() % 6],
                          style: GoogleFonts.dmSans(
                            color: t.textDim,
                            fontSize: 10,
                          ),
                        );
                      },
                    ),
                  ),
                  leftTitles: AxisTitles(
                    sideTitles: SideTitles(
                      showTitles: true,
                      reservedSize: 40,
                      getTitlesWidget: (value, meta) {
                        return Text(
                          '\$${value.toInt()}',
                          style: GoogleFonts.dmSans(
                            color: t.textDim,
                            fontSize: 10,
                          ),
                        );
                      },
                    ),
                  ),
                  topTitles: AxisTitles(
                    sideTitles: SideTitles(showTitles: false),
                  ),
                  rightTitles: AxisTitles(
                    sideTitles: SideTitles(showTitles: false),
                  ),
                ),
                gridData: FlGridData(
                  show: true,
                  drawVerticalLine: false,
                  horizontalInterval: 5000,
                  getDrawingHorizontalLine: (value) =>
                      FlLine(color: t.borderFaint, strokeWidth: 1),
                ),
                borderData: FlBorderData(show: false),
                barGroups: [
                  _makeBarGroup(0, 12000, AppColors.hotPink),
                  _makeBarGroup(1, 15000, AppColors.hotPink),
                  _makeBarGroup(2, 8000, AppColors.hotPink),
                  _makeBarGroup(3, 18000, AppColors.hotPink),
                  _makeBarGroup(4, 14000, AppColors.hotPink),
                  _makeBarGroup(5, 16000, AppColors.hotPink),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  BarChartGroupData _makeBarGroup(int x, double y, Color color) {
    return BarChartGroupData(
      x: x,
      barRods: [
        BarChartRodData(
          toY: y,
          color: color,
          width: 20,
          borderRadius: BorderRadius.circular(4),
        ),
      ],
    );
  }

  Widget _buildIncomeBreakdown(RfTheme t) {
    return Container(
      padding: EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: t.card,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: t.borderFaint),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Desglose de Ingresos',
            style: GoogleFonts.outfit(
              color: t.textPrimary,
              fontSize: 18,
              fontWeight: FontWeight.w600,
            ),
          ),
          SizedBox(height: 16),
          _buildIncomeRow(
            'Alquiler de equipos',
            '\$28,500',
            0.63,
            AppColors.hotPink,
            t,
          ),
          SizedBox(height: 12),
          _buildIncomeRow('Decoración', '\$12,200', 0.27, AppColors.violet, t),
          SizedBox(height: 12),
          _buildIncomeRow(
            'Servicios adicionales',
            '\$4,530',
            0.10,
            AppColors.teal,
            t,
          ),
        ],
      ),
    );
  }

  Widget _buildIncomeRow(
    String label,
    String value,
    double percentage,
    Color color,
    RfTheme t,
  ) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(label, style: GoogleFonts.dmSans(color: t.textPrimary)),
            Text(
              value,
              style: GoogleFonts.outfit(
                color: t.textPrimary,
                fontWeight: FontWeight.w600,
              ),
            ),
          ],
        ),
        SizedBox(height: 8),
        ClipRRect(
          borderRadius: BorderRadius.circular(4),
          child: LinearProgressIndicator(
            value: percentage,
            backgroundColor: t.borderFaint,
            valueColor: AlwaysStoppedAnimation(color),
            minHeight: 8,
          ),
        ),
      ],
    );
  }

  Widget _buildTopEventsTable(RfTheme t) {
    return Container(
      padding: EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: t.card,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: t.borderFaint),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'Top Eventos',
                style: GoogleFonts.outfit(
                  color: t.textPrimary,
                  fontSize: 18,
                  fontWeight: FontWeight.w600,
                ),
              ),
              TextButton(
                onPressed: () {},
                child: Text(
                  'Ver todos',
                  style: GoogleFonts.dmSans(color: AppColors.hotPink),
                ),
              ),
            ],
          ),
          SizedBox(height: 12),
          DataTable(
            headingRowColor: WidgetStateProperty.all(
              t.base.withValues(alpha: 0.5),
            ),
            columns: [
              DataColumn(
                label: Text(
                  'Evento',
                  style: GoogleFonts.dmSans(
                    color: t.textMuted,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ),
              DataColumn(
                label: Text(
                  'Fecha',
                  style: GoogleFonts.dmSans(
                    color: t.textMuted,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ),
              DataColumn(
                label: Text(
                  'Ingreso',
                  style: GoogleFonts.dmSans(
                    color: t.textMuted,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ),
            ],
            rows: [
              DataRow(
                cells: [
                  DataCell(
                    Text(
                      'Boda Rodríguez-García',
                      style: GoogleFonts.dmSans(color: t.textPrimary),
                    ),
                  ),
                  DataCell(
                    Text(
                      '15 Mar 2026',
                      style: GoogleFonts.dmSans(color: t.textMuted),
                    ),
                  ),
                  DataCell(
                    Text(
                      '\$8,500',
                      style: GoogleFonts.outfit(
                        color: AppColors.teal,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                ],
              ),
              DataRow(
                cells: [
                  DataCell(
                    Text(
                      'Cumpleaños 15 - Sofía',
                      style: GoogleFonts.dmSans(color: t.textPrimary),
                    ),
                  ),
                  DataCell(
                    Text(
                      '22 Feb 2026',
                      style: GoogleFonts.dmSans(color: t.textMuted),
                    ),
                  ),
                  DataCell(
                    Text(
                      '\$6,200',
                      style: GoogleFonts.outfit(
                        color: AppColors.teal,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                ],
              ),
              DataRow(
                cells: [
                  DataCell(
                    Text(
                      'Baby Shower - Martínez',
                      style: GoogleFonts.dmSans(color: t.textPrimary),
                    ),
                  ),
                  DataCell(
                    Text(
                      '8 Ene 2026',
                      style: GoogleFonts.dmSans(color: t.textMuted),
                    ),
                  ),
                  DataCell(
                    Text(
                      '\$4,800',
                      style: GoogleFonts.outfit(
                        color: AppColors.teal,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildEventsTab(RfTheme t) {
    return SingleChildScrollView(
      padding: EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: _buildStatCard(
                  'Total Eventos',
                  '156',
                  AppColors.hotPink,
                  t,
                ),
              ),
              SizedBox(width: 12),
              Expanded(
                child: _buildStatCard('Completados', '134', AppColors.teal, t),
              ),
            ],
          ),
          SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: _buildStatCard('En Proceso', '18', AppColors.amber, t),
              ),
              SizedBox(width: 12),
              Expanded(
                child: _buildStatCard('Cancelados', '4', AppColors.coral, t),
              ),
            ],
          ),
          SizedBox(height: 20),
          _buildEventsByType(t),
          SizedBox(height: 20),
          _buildMonthlyTrend(t),
        ],
      ),
    );
  }

  Widget _buildStatCard(String title, String value, Color color, RfTheme t) {
    return Container(
      padding: EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: t.card,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: color.withValues(alpha: 0.3)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: GoogleFonts.dmSans(color: t.textMuted, fontSize: 13),
          ),
          SizedBox(height: 8),
          Text(
            value,
            style: GoogleFonts.outfit(
              color: color,
              fontSize: 32,
              fontWeight: FontWeight.bold,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildEventsByType(RfTheme t) {
    return Container(
      padding: EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: t.card,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: t.borderFaint),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Eventos por Tipo',
            style: GoogleFonts.outfit(
              color: t.textPrimary,
              fontSize: 18,
              fontWeight: FontWeight.w600,
            ),
          ),
          SizedBox(height: 20),
          SizedBox(
            height: 200,
            child: PieChart(
              PieChartData(
                sectionsSpace: 2,
                centerSpaceRadius: 40,
                sections: [
                  PieChartSectionData(
                    color: AppColors.hotPink,
                    value: 45,
                    title: '45%',
                    titleStyle: GoogleFonts.outfit(
                      color: Colors.white,
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  PieChartSectionData(
                    color: AppColors.teal,
                    value: 25,
                    title: '25%',
                    titleStyle: GoogleFonts.outfit(
                      color: Colors.white,
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  PieChartSectionData(
                    color: AppColors.amber,
                    value: 15,
                    title: '15%',
                    titleStyle: GoogleFonts.outfit(
                      color: Colors.white,
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  PieChartSectionData(
                    color: AppColors.violet,
                    value: 15,
                    title: '15%',
                    titleStyle: GoogleFonts.outfit(
                      color: Colors.white,
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
            ),
          ),
          SizedBox(height: 16),
          Wrap(
            spacing: 16,
            runSpacing: 8,
            children: [
              _buildLegendItem('Bodas', AppColors.hotPink, '70'),
              _buildLegendItem('Cumpleaños', AppColors.teal, '39'),
              _buildLegendItem('Corporativos', AppColors.amber, '23'),
              _buildLegendItem('Otros', AppColors.violet, '24'),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildLegendItem(String label, Color color, String count) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          width: 12,
          height: 12,
          decoration: BoxDecoration(
            color: color,
            borderRadius: BorderRadius.circular(3),
          ),
        ),
        SizedBox(width: 6),
        Text('$label ($count)', style: GoogleFonts.dmSans(fontSize: 12)),
      ],
    );
  }

  Widget _buildMonthlyTrend(RfTheme t) {
    return Container(
      padding: EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: t.card,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: t.borderFaint),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Tendencia Mensual',
            style: GoogleFonts.outfit(
              color: t.textPrimary,
              fontSize: 18,
              fontWeight: FontWeight.w600,
            ),
          ),
          SizedBox(height: 20),
          SizedBox(
            height: 150,
            child: LineChart(
              LineChartData(
                gridData: FlGridData(show: false),
                titlesData: FlTitlesData(
                  bottomTitles: AxisTitles(
                    sideTitles: SideTitles(showTitles: false),
                  ),
                  leftTitles: AxisTitles(
                    sideTitles: SideTitles(showTitles: false),
                  ),
                  topTitles: AxisTitles(
                    sideTitles: SideTitles(showTitles: false),
                  ),
                  rightTitles: AxisTitles(
                    sideTitles: SideTitles(showTitles: false),
                  ),
                ),
                borderData: FlBorderData(show: false),
                lineBarsData: [
                  LineChartBarData(
                    spots: [
                      FlSpot(0, 12),
                      FlSpot(1, 15),
                      FlSpot(2, 10),
                      FlSpot(3, 18),
                      FlSpot(4, 22),
                      FlSpot(5, 20),
                    ],
                    isCurved: true,
                    color: AppColors.hotPink,
                    barWidth: 3,
                    dotData: FlDotData(show: false),
                    belowBarData: BarAreaData(
                      show: true,
                      color: AppColors.hotPink.withValues(alpha: 0.2),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildClientsTab(RfTheme t) {
    return SingleChildScrollView(
      padding: EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: _buildStatCard(
                  'Total Clientes',
                  '248',
                  AppColors.violet,
                  t,
                ),
              ),
              SizedBox(width: 12),
              Expanded(
                child: _buildStatCard('Nuevos (mes)', '18', AppColors.teal, t),
              ),
            ],
          ),
          SizedBox(height: 20),
          _buildTopClientsTable(t),
          SizedBox(height: 20),
          _buildClientRetention(t),
        ],
      ),
    );
  }

  Widget _buildTopClientsTable(RfTheme t) {
    return Container(
      padding: EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: t.card,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: t.borderFaint),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Top Clientes',
            style: GoogleFonts.outfit(
              color: t.textPrimary,
              fontSize: 18,
              fontWeight: FontWeight.w600,
            ),
          ),
          SizedBox(height: 16),
          DataTable(
            headingRowColor: WidgetStateProperty.all(
              t.base.withValues(alpha: 0.5),
            ),
            columns: [
              DataColumn(
                label: Text(
                  'Cliente',
                  style: GoogleFonts.dmSans(
                    color: t.textMuted,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ),
              DataColumn(
                label: Text(
                  'Eventos',
                  style: GoogleFonts.dmSans(
                    color: t.textMuted,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ),
              DataColumn(
                label: Text(
                  'Total Gastado',
                  style: GoogleFonts.dmSans(
                    color: t.textMuted,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ),
            ],
            rows: [
              DataRow(
                cells: [
                  DataCell(
                    Text(
                      'María González',
                      style: GoogleFonts.dmSans(color: t.textPrimary),
                    ),
                  ),
                  DataCell(
                    Text('5', style: GoogleFonts.dmSans(color: t.textMuted)),
                  ),
                  DataCell(
                    Text(
                      '\$42,500',
                      style: GoogleFonts.outfit(
                        color: AppColors.teal,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                ],
              ),
              DataRow(
                cells: [
                  DataCell(
                    Text(
                      'Carlos Rodríguez',
                      style: GoogleFonts.dmSans(color: t.textPrimary),
                    ),
                  ),
                  DataCell(
                    Text('4', style: GoogleFonts.dmSans(color: t.textMuted)),
                  ),
                  DataCell(
                    Text(
                      '\$38,200',
                      style: GoogleFonts.outfit(
                        color: AppColors.teal,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                ],
              ),
              DataRow(
                cells: [
                  DataCell(
                    Text(
                      'Ana Martínez',
                      style: GoogleFonts.dmSans(color: t.textPrimary),
                    ),
                  ),
                  DataCell(
                    Text('3', style: GoogleFonts.dmSans(color: t.textMuted)),
                  ),
                  DataCell(
                    Text(
                      '\$28,900',
                      style: GoogleFonts.outfit(
                        color: AppColors.teal,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildClientRetention(RfTheme t) {
    return Container(
      padding: EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: t.card,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: t.borderFaint),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Retención de Clientes',
            style: GoogleFonts.outfit(
              color: t.textPrimary,
              fontSize: 18,
              fontWeight: FontWeight.w600,
            ),
          ),
          SizedBox(height: 20),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              _buildRetentionMetric('Primera vez', '45%', AppColors.hotPink, t),
              _buildRetentionMetric('Recurrentes', '38%', AppColors.teal, t),
              _buildRetentionMetric('VIP', '17%', AppColors.amber, t),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildRetentionMetric(
    String label,
    String value,
    Color color,
    RfTheme t,
  ) {
    return Column(
      children: [
        Container(
          width: 60,
          height: 60,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            border: Border.all(color: color, width: 4),
          ),
          child: Center(
            child: Text(
              value,
              style: GoogleFonts.outfit(
                color: color,
                fontSize: 16,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
        ),
        SizedBox(height: 8),
        Text(
          label,
          style: GoogleFonts.dmSans(color: t.textMuted, fontSize: 12),
        ),
      ],
    );
  }

  Widget _buildInventoryTab(RfTheme t) {
    return SingleChildScrollView(
      padding: EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: _buildStatCard(
                  'Total Artículos',
                  '1,245',
                  AppColors.sky,
                  t,
                ),
              ),
              SizedBox(width: 12),
              Expanded(
                child: _buildStatCard('En Uso', '892', AppColors.amber, t),
              ),
            ],
          ),
          SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: _buildStatCard('Stock Bajo', '23', AppColors.coral, t),
              ),
              SizedBox(width: 12),
              Expanded(
                child: _buildStatCard(
                  'Mantenimiento',
                  '8',
                  AppColors.violet,
                  t,
                ),
              ),
            ],
          ),
          SizedBox(height: 20),
          _buildInventoryUsageChart(t),
          SizedBox(height: 20),
          _buildMaintenanceSchedule(t),
        ],
      ),
    );
  }

  Widget _buildInventoryUsageChart(RfTheme t) {
    return Container(
      padding: EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: t.card,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: t.borderFaint),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Uso de Inventario',
            style: GoogleFonts.outfit(
              color: t.textPrimary,
              fontSize: 18,
              fontWeight: FontWeight.w600,
            ),
          ),
          SizedBox(height: 20),
          SizedBox(
            height: 200,
            child: BarChart(
              BarChartData(
                alignment: BarChartAlignment.spaceAround,
                maxY: 100,
                barTouchData: BarTouchData(enabled: true),
                titlesData: FlTitlesData(
                  show: true,
                  bottomTitles: AxisTitles(
                    sideTitles: SideTitles(
                      showTitles: true,
                      getTitlesWidget: (value, meta) {
                        const categories = [
                          'Mesas',
                          'Sillas',
                          'Linos',
                          'Iluminación',
                          'Decoración',
                        ];
                        return Text(
                          categories[value.toInt() % 5],
                          style: GoogleFonts.dmSans(
                            color: t.textDim,
                            fontSize: 10,
                          ),
                        );
                      },
                    ),
                  ),
                  leftTitles: AxisTitles(
                    sideTitles: SideTitles(
                      showTitles: true,
                      reservedSize: 30,
                      getTitlesWidget: (value, meta) => Text(
                        '$value%',
                        style: GoogleFonts.dmSans(
                          color: t.textDim,
                          fontSize: 10,
                        ),
                      ),
                    ),
                  ),
                  topTitles: AxisTitles(
                    sideTitles: SideTitles(showTitles: false),
                  ),
                  rightTitles: AxisTitles(
                    sideTitles: SideTitles(showTitles: false),
                  ),
                ),
                gridData: FlGridData(show: false),
                borderData: FlBorderData(show: false),
                barGroups: [
                  _makeBarGroup(0, 85, AppColors.hotPink),
                  _makeBarGroup(1, 92, AppColors.teal),
                  _makeBarGroup(2, 78, AppColors.amber),
                  _makeBarGroup(3, 65, AppColors.violet),
                  _makeBarGroup(4, 88, AppColors.sky),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildMaintenanceSchedule(RfTheme t) {
    return Container(
      padding: EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: t.card,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: t.borderFaint),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'Próximo Mantenimiento',
                style: GoogleFonts.outfit(
                  color: t.textPrimary,
                  fontSize: 18,
                  fontWeight: FontWeight.w600,
                ),
              ),
              TextButton(
                onPressed: () {},
                child: Text(
                  'Ver calendario',
                  style: GoogleFonts.dmSans(color: AppColors.hotPink),
                ),
              ),
            ],
          ),
          SizedBox(height: 16),
          _buildMaintenanceItem(
            'Sillas plegables',
            'Limpieza profunda',
            '20 Abr',
            AppColors.teal,
            t,
          ),
          SizedBox(height: 12),
          _buildMaintenanceItem(
            'Linos blancos',
            'Inspección',
            '25 Abr',
            AppColors.amber,
            t,
          ),
          SizedBox(height: 12),
          _buildMaintenanceItem(
            'Sistema de iluminación',
            'Mantenimiento eléctrico',
            '28 Abr',
            AppColors.violet,
            t,
          ),
        ],
      ),
    );
  }

  Widget _buildMaintenanceItem(
    String item,
    String task,
    String date,
    Color color,
    RfTheme t,
  ) {
    return Container(
      padding: EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: color.withValues(alpha: 0.3)),
      ),
      child: Row(
        children: [
          Container(
            width: 40,
            height: 40,
            decoration: BoxDecoration(
              color: color.withValues(alpha: 0.2),
              borderRadius: BorderRadius.circular(10),
            ),
            child: Icon(Icons.build, color: color, size: 20),
          ),
          SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  item,
                  style: GoogleFonts.dmSans(
                    color: t.textPrimary,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                Text(
                  task,
                  style: GoogleFonts.dmSans(color: t.textMuted, fontSize: 12),
                ),
              ],
            ),
          ),
          Container(
            padding: EdgeInsets.symmetric(horizontal: 10, vertical: 4),
            decoration: BoxDecoration(
              color: color,
              borderRadius: BorderRadius.circular(12),
            ),
            child: Text(
              date,
              style: GoogleFonts.dmSans(
                color: Colors.white,
                fontSize: 12,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _exportReport() async {
    setState(() => _isLoading = true);
    await Future.delayed(Duration(seconds: 2));
    if (mounted) {
      setState(() => _isLoading = false);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Reporte exportado exitosamente'),
          backgroundColor: AppColors.teal,
        ),
      );
    }
  }
}
