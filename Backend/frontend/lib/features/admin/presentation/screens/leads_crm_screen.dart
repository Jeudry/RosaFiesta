import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:frontend/core/app_colors.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/api_client.dart';

class LeadsCRMScreen extends StatefulWidget {
  const LeadsCRMScreen({super.key});

  @override
  State<LeadsCRMScreen> createState() => _LeadsCRMScreenState();
}

class _LeadsCRMScreenState extends State<LeadsCRMScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  final _apiClient = ApiClient();
  List<dynamic> _leads = [];
  Map<String, dynamic>? _stats;
  bool _isLoading = true;
  String _filterStatus = '';

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 3, vsync: this);
    _loadData();
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  Future<void> _loadData() async {
    setState(() => _isLoading = true);
    try {
      final statsData = await _apiClient.get('/leads/stats');
      setState(() => _stats = statsData['data']);
      await _loadLeads();
    } catch (e) {
      debugPrint('Error loading data: $e');
    } finally {
      setState(() => _isLoading = false);
    }
  }

  Future<void> _loadLeads() async {
    try {
      String path = '/leads?limit=50&offset=0';
      if (_filterStatus.isNotEmpty) path += '&status=$_filterStatus';
      final data = await _apiClient.get(path);
      setState(() => _leads = data['data'] as List<dynamic>? ?? []);
    } catch (e) {
      debugPrint('Error loading leads: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);
    return Scaffold(
      backgroundColor: t.base,
      appBar: AppBar(
        backgroundColor: t.card,
        title: Text(
          'CRM - Leads',
          style: GoogleFonts.outfit(
            color: t.textPrimary,
            fontWeight: FontWeight.w600,
          ),
        ),
        actions: [
          IconButton(
            icon: Icon(Icons.refresh, color: t.textPrimary),
            onPressed: _loadData,
          ),
        ],
        bottom: TabBar(
          controller: _tabController,
          labelColor: AppColors.hotPink,
          unselectedLabelColor: t.textMuted,
          indicatorColor: AppColors.hotPink,
          tabs: const [
            Tab(text: 'Pipeline'),
            Tab(text: 'Seguimientos'),
            Tab(text: 'Estadísticas'),
          ],
        ),
      ),
      body: _isLoading
          ? Center(child: CircularProgressIndicator(color: AppColors.hotPink))
          : TabBarView(
              controller: _tabController,
              children: [
                _buildPipelineTab(t),
                _buildFollowupsTab(t),
                _buildStatsTab(t),
              ],
            ),
      floatingActionButton: FloatingActionButton(
        backgroundColor: AppColors.hotPink,
        onPressed: () => _showCreateLeadDialog(context),
        child: Icon(Icons.add, color: Colors.white),
      ),
    );
  }

  Widget _buildPipelineTab(RfTheme t) {
    final statusColors = {
      'new': AppColors.sky,
      'contacted': AppColors.amber,
      'qualified': AppColors.teal,
      'proposal': AppColors.violet,
      'negotiating': AppColors.coral,
      'won': AppColors.hotPink,
      'lost': Colors.grey,
    };

    return Column(
      children: [
        Container(
          padding: EdgeInsets.all(12),
          child: SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(
              children:
                  [
                    '',
                    'new',
                    'contacted',
                    'qualified',
                    'proposal',
                    'negotiating',
                    'won',
                    'lost',
                  ].map((status) {
                    final isSelected = _filterStatus == status;
                    return Padding(
                      padding: EdgeInsets.only(right: 8),
                      child: FilterChip(
                        label: Text(
                          status.isEmpty ? 'Todos' : status.toUpperCase(),
                        ),
                        selected: isSelected,
                        onSelected: (_) {
                          setState(() => _filterStatus = status);
                          _loadLeads();
                        },
                        selectedColor:
                            statusColors[status]?.withValues(alpha: 0.2) ??
                            t.card,
                        checkmarkColor: statusColors[status] ?? Colors.white,
                      ),
                    );
                  }).toList(),
            ),
          ),
        ),
        Expanded(
          child: ListView.builder(
            padding: EdgeInsets.all(16),
            itemCount: _leads.length,
            itemBuilder: (context, index) {
              final lead = _leads[index] as Map<String, dynamic>;
              final status = lead['status'] ?? 'new';
              final priority = lead['priority'] ?? 'medium';

              return Container(
                margin: EdgeInsets.only(bottom: 12),
                padding: EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: t.card,
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(
                    color: (statusColors[status] ?? Colors.grey).withValues(
                      alpha: 0.3,
                    ),
                  ),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Expanded(
                          child: Text(
                            lead['client_name'] ?? 'Unknown',
                            style: GoogleFonts.outfit(
                              color: t.textPrimary,
                              fontSize: 16,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ),
                        Container(
                          padding: EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          decoration: BoxDecoration(
                            color: (statusColors[status] ?? Colors.grey)
                                .withValues(alpha: 0.1),
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: Text(
                            status.toUpperCase(),
                            style: GoogleFonts.dmSans(
                              color: statusColors[status] ?? Colors.grey,
                              fontSize: 11,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ),
                      ],
                    ),
                    SizedBox(height: 8),
                    Row(
                      children: [
                        if (lead['client_phone'] != null) ...[
                          Icon(Icons.phone, size: 14, color: t.textMuted),
                          SizedBox(width: 4),
                          Text(
                            lead['client_phone'],
                            style: GoogleFonts.dmSans(
                              color: t.textMuted,
                              fontSize: 12,
                            ),
                          ),
                          SizedBox(width: 16),
                        ],
                        if (lead['event_type'] != null) ...[
                          Icon(Icons.event, size: 14, color: t.textMuted),
                          SizedBox(width: 4),
                          Text(
                            lead['event_type'],
                            style: GoogleFonts.dmSans(
                              color: t.textMuted,
                              fontSize: 12,
                            ),
                          ),
                        ],
                      ],
                    ),
                    SizedBox(height: 8),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Row(
                          children: [
                            _buildPriorityBadge(priority, t),
                            if (lead['budget_min'] != null &&
                                lead['budget_max'] != null) ...[
                              SizedBox(width: 8),
                              Text(
                                'RD\$${lead['budget_min']} - RD\$${lead['budget_max']}',
                                style: GoogleFonts.dmSans(
                                  color: AppColors.teal,
                                  fontSize: 12,
                                ),
                              ),
                            ],
                          ],
                        ),
                        IconButton(
                          icon: Icon(Icons.chevron_right, color: t.textMuted),
                          onPressed: () => _showLeadDetail(lead),
                        ),
                      ],
                    ),
                  ],
                ),
              );
            },
          ),
        ),
      ],
    );
  }

  Widget _buildPriorityBadge(String priority, RfTheme t) {
    final colors = {
      'low': AppColors.sky,
      'medium': AppColors.amber,
      'high': AppColors.coral,
      'urgent': AppColors.hotPink,
    };
    return Container(
      padding: EdgeInsets.symmetric(horizontal: 6, vertical: 2),
      decoration: BoxDecoration(
        color: (colors[priority] ?? Colors.grey).withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(
        priority.toUpperCase(),
        style: GoogleFonts.dmSans(
          color: colors[priority] ?? Colors.grey,
          fontSize: 10,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }

  Widget _buildFollowupsTab(RfTheme t) {
    return FutureBuilder(
      future: _apiClient.get('/leads/overdue-followups'),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return Center(
            child: CircularProgressIndicator(color: AppColors.hotPink),
          );
        }
        final followups = snapshot.data?['data'] as List<dynamic>? ?? [];

        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: EdgeInsets.all(16),
              child: Text(
                'Seguimientos Pendientes',
                style: GoogleFonts.outfit(
                  color: t.textPrimary,
                  fontSize: 18,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
            if (followups.isEmpty)
              Expanded(
                child: Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(Icons.check_circle, color: AppColors.teal, size: 64),
                      SizedBox(height: 16),
                      Text(
                        '¡No hay seguimientos pendientes!',
                        style: GoogleFonts.outfit(
                          color: t.textMuted,
                          fontSize: 16,
                        ),
                      ),
                    ],
                  ),
                ),
              )
            else
              Expanded(
                child: ListView.builder(
                  padding: EdgeInsets.symmetric(horizontal: 16),
                  itemCount: followups.length,
                  itemBuilder: (context, index) {
                    final followup = followups[index] as Map<String, dynamic>;
                    return Container(
                      margin: EdgeInsets.only(bottom: 12),
                      padding: EdgeInsets.all(16),
                      decoration: BoxDecoration(
                        color: AppColors.coral.withValues(alpha: 0.1),
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(
                          color: AppColors.coral.withValues(alpha: 0.3),
                        ),
                      ),
                      child: Row(
                        children: [
                          Icon(Icons.warning_amber, color: AppColors.coral),
                          SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  followup['notes'] ?? 'Sin descripción',
                                  style: GoogleFonts.dmSans(
                                    color: t.textPrimary,
                                  ),
                                ),
                                Text(
                                  followup['follow_up_date'] ?? '',
                                  style: GoogleFonts.dmSans(
                                    color: t.textMuted,
                                    fontSize: 12,
                                  ),
                                ),
                              ],
                            ),
                          ),
                          TextButton(
                            onPressed: () async {
                              await _apiClient.patch(
                                '/leads/followups/${followup['id']}/complete',
                                {},
                              );
                              _loadData();
                            },
                            child: Text(
                              'Completar',
                              style: GoogleFonts.dmSans(color: AppColors.teal),
                            ),
                          ),
                        ],
                      ),
                    );
                  },
                ),
              ),
          ],
        );
      },
    );
  }

  Widget _buildStatsTab(RfTheme t) {
    if (_stats == null)
      return Center(child: CircularProgressIndicator(color: AppColors.hotPink));

    return SingleChildScrollView(
      padding: EdgeInsets.all(16),
      child: Column(
        children: [
          Row(
            children: [
              _buildStatCard(
                'Total Leads',
                _stats!['total']?.toString() ?? '0',
                AppColors.sky,
                t,
              ),
              SizedBox(width: 12),
              _buildStatCard(
                'Nuevos',
                _stats!['new_count']?.toString() ?? '0',
                AppColors.teal,
                t,
              ),
            ],
          ),
          SizedBox(height: 12),
          Row(
            children: [
              _buildStatCard(
                'Calificados',
                _stats!['qualified_count']?.toString() ?? '0',
                AppColors.violet,
                t,
              ),
              SizedBox(width: 12),
              _buildStatCard(
                'Ganados',
                _stats!['won_count']?.toString() ?? '0',
                AppColors.hotPink,
                t,
              ),
            ],
          ),
          SizedBox(height: 12),
          Row(
            children: [
              _buildStatCard(
                'Perdidos',
                _stats!['lost_count']?.toString() ?? '0',
                AppColors.coral,
                t,
              ),
              SizedBox(width: 12),
              _buildStatCard(
                'Tasa Conversión',
                '${_stats!['conversion_rate']?.toStringAsFixed(1) ?? '0'}%',
                AppColors.amber,
                t,
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildStatCard(String title, String value, Color color, RfTheme t) {
    return Expanded(
      child: Container(
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
      ),
    );
  }

  void _showCreateLeadDialog(BuildContext context) {
    final nameController = TextEditingController();
    final phoneController = TextEditingController();
    final emailController = TextEditingController();

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: RfTheme.of(ctx).card,
        title: Text(
          'Nuevo Lead',
          style: GoogleFonts.outfit(color: RfTheme.of(ctx).textPrimary),
        ),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: nameController,
              decoration: InputDecoration(labelText: 'Nombre del cliente'),
            ),
            TextField(
              controller: phoneController,
              decoration: InputDecoration(labelText: 'Teléfono'),
            ),
            TextField(
              controller: emailController,
              decoration: InputDecoration(labelText: 'Email'),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: Text('Cancelar'),
          ),
          ElevatedButton(
            onPressed: () async {
              await _apiClient.post('/leads', {
                'client_name': nameController.text,
                'client_phone': phoneController.text,
                'client_email': emailController.text,
                'source': 'manual',
              });
              Navigator.pop(ctx);
              _loadData();
            },
            child: Text('Crear'),
          ),
        ],
      ),
    );
  }

  void _showLeadDetail(Map<String, dynamic> lead) {
    showModalBottomSheet(
      context: context,
      builder: (ctx) => Container(
        padding: EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              lead['client_name'] ?? 'Lead',
              style: GoogleFonts.outfit(
                fontSize: 20,
                fontWeight: FontWeight.bold,
              ),
            ),
            SizedBox(height: 16),
            if (lead['client_phone'] != null)
              ListTile(
                leading: Icon(Icons.phone),
                title: Text(lead['client_phone']),
              ),
            if (lead['client_email'] != null)
              ListTile(
                leading: Icon(Icons.email),
                title: Text(lead['client_email']),
              ),
            if (lead['notes'] != null)
              ListTile(leading: Icon(Icons.note), title: Text(lead['notes'])),
            SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: ElevatedButton(
                    onPressed: () {
                      Navigator.pop(ctx);
                    },
                    child: Text('Ver Seguimientos'),
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
