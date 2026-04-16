import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';
import 'package:frontend/features/profile/admin/data/client_portal_repository.dart';
import 'package:frontend/features/profile/admin/data/financial_repository.dart';

class AdminDashboardScreen extends StatefulWidget {
  const AdminDashboardScreen({super.key});

  @override
  State<AdminDashboardScreen> createState() => _AdminDashboardScreenState();
}

class _AdminDashboardScreenState extends State<AdminDashboardScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  final _clientPortalRepo = ClientPortalRepository();
  final _financialRepo = FinancialRepository();
  final _insuranceRepo = InsuranceRepository();
  final _auditRepo = AuditRepository();

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 5, vsync: this);
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
          'Panel Administrativo',
          style: GoogleFonts.outfit(
            color: t.textPrimary,
            fontWeight: FontWeight.w600,
          ),
        ),
        bottom: TabBar(
          controller: _tabController,
          labelColor: AppColors.hotPink,
          unselectedLabelColor: t.textMuted,
          indicatorColor: AppColors.hotPink,
          tabs: const [
            Tab(icon: Icon(Icons.dashboard), text: 'Resumen'),
            Tab(icon: Icon(Icons.account_balance_wallet), text: 'Finanzas'),
            Tab(icon: Icon(Icons.security), text: 'Insurance'),
            Tab(icon: Icon(Icons.history), text: 'Auditoría'),
            Tab(icon: Icon(Icons.person), text: 'Clientes'),
          ],
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          _buildSummaryTab(t),
          _buildFinancialTab(t),
          _buildInsuranceTab(t),
          _buildAuditTab(t),
          _buildClientPortalTab(t),
        ],
      ),
    );
  }

  Widget _buildSummaryTab(RfTheme t) {
    return SingleChildScrollView(
      padding: EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildSummaryCard(
            t,
            'Ingresos del Mes',
            '\$12,450',
            Icons.trending_up,
            AppColors.teal,
          ),
          SizedBox(height: 12),
          _buildSummaryCard(
            t,
            'Eventos Activos',
            '24',
            Icons.event,
            AppColors.hotPink,
          ),
          SizedBox(height: 12),
          _buildSummaryCard(
            t,
            'Siniestros Pendientes',
            '3',
            Icons.warning,
            AppColors.coral,
          ),
          SizedBox(height: 12),
          _buildSummaryCard(
            t,
            'Nuevos Clientes',
            '8',
            Icons.person_add,
            AppColors.amber,
          ),
        ],
      ),
    );
  }

  Widget _buildSummaryCard(
    RfTheme t,
    String title,
    String value,
    IconData icon,
    Color color,
  ) {
    return Container(
      padding: EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: t.card,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: t.borderFaint),
      ),
      child: Row(
        children: [
          Container(
            padding: EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: color.withOpacity(0.1),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Icon(icon, color: color, size: 28),
          ),
          SizedBox(width: 16),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                title,
                style: GoogleFonts.dmSans(color: t.textMuted, fontSize: 14),
              ),
              SizedBox(height: 4),
              Text(
                value,
                style: GoogleFonts.outfit(
                  color: t.textPrimary,
                  fontSize: 28,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildFinancialTab(RfTheme t) {
    return DefaultTabController(
      length: 3,
      child: Column(
        children: [
          Container(
            color: t.card,
            child: TabBar(
              labelColor: AppColors.hotPink,
              unselectedLabelColor: t.textMuted,
              indicatorColor: AppColors.hotPink,
              tabs: [
                Tab(text: 'Registros'),
                Tab(text: 'Facturas'),
                Tab(text: 'Proveedores'),
              ],
            ),
          ),
          Expanded(
            child: TabBarView(
              children: [
                _buildFinancialRecordsTab(t),
                _buildInvoicesTab(t),
                _buildVendorsTab(t),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFinancialRecordsTab(RfTheme t) {
    return FutureBuilder(
      future: _financialRepo.getRecords(
        startDate: '2024-01-01',
        endDate: '2024-12-31',
      ),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return Center(
            child: CircularProgressIndicator(color: AppColors.hotPink),
          );
        }
        final records = snapshot.data ?? [];
        return ListView.builder(
          padding: EdgeInsets.all(16),
          itemCount: records.length,
          itemBuilder: (context, index) {
            final record = records[index] as Map<String, dynamic>;
            return Container(
              margin: EdgeInsets.only(bottom: 12),
              padding: EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: t.card,
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: t.borderFaint),
              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        record['description'] ?? 'N/A',
                        style: GoogleFonts.dmSans(color: t.textPrimary),
                      ),
                      Text(
                        record['type'] ?? '',
                        style: GoogleFonts.dmSans(
                          color: t.textMuted,
                          fontSize: 12,
                        ),
                      ),
                    ],
                  ),
                  Text(
                    '\$${record['amount'] ?? 0}',
                    style: GoogleFonts.outfit(
                      color: AppColors.teal,
                      fontSize: 18,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ],
              ),
            );
          },
        );
      },
    );
  }

  Widget _buildInvoicesTab(RfTheme t) {
    return FutureBuilder(
      future: _financialRepo.getInvoices(),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return Center(
            child: CircularProgressIndicator(color: AppColors.hotPink),
          );
        }
        final invoices = snapshot.data ?? [];
        return ListView.builder(
          padding: EdgeInsets.all(16),
          itemCount: invoices.length,
          itemBuilder: (context, index) {
            final invoice = invoices[index] as Map<String, dynamic>;
            return Container(
              margin: EdgeInsets.only(bottom: 12),
              padding: EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: t.card,
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: t.borderFaint),
              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        invoice['invoice_number'] ?? 'N/A',
                        style: GoogleFonts.dmSans(
                          color: t.textPrimary,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                      Text(
                        invoice['status'] ?? '',
                        style: GoogleFonts.dmSans(
                          color: t.textMuted,
                          fontSize: 12,
                        ),
                      ),
                    ],
                  ),
                  Text(
                    '\$${invoice['total'] ?? 0}',
                    style: GoogleFonts.outfit(
                      color: AppColors.amber,
                      fontSize: 18,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ],
              ),
            );
          },
        );
      },
    );
  }

  Widget _buildVendorsTab(RfTheme t) {
    return FutureBuilder(
      future: _financialRepo.getVendors(),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return Center(
            child: CircularProgressIndicator(color: AppColors.hotPink),
          );
        }
        final vendors = snapshot.data ?? [];
        return ListView.builder(
          padding: EdgeInsets.all(16),
          itemCount: vendors.length,
          itemBuilder: (context, index) {
            final vendor = vendors[index] as Map<String, dynamic>;
            return Container(
              margin: EdgeInsets.only(bottom: 12),
              padding: EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: t.card,
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: t.borderFaint),
              ),
              child: Row(
                children: [
                  CircleAvatar(
                    backgroundColor: AppColors.violet.withOpacity(0.2),
                    child: Icon(Icons.business, color: AppColors.violet),
                  ),
                  SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          vendor['name'] ?? 'N/A',
                          style: GoogleFonts.dmSans(
                            color: t.textPrimary,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        Text(
                          vendor['category'] ?? '',
                          style: GoogleFonts.dmSans(
                            color: t.textMuted,
                            fontSize: 12,
                          ),
                        ),
                      ],
                    ),
                  ),
                  if (vendor['is_active'] == true)
                    Container(
                      padding: EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                      decoration: BoxDecoration(
                        color: AppColors.teal.withOpacity(0.1),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Text(
                        'Activo',
                        style: GoogleFonts.dmSans(
                          color: AppColors.teal,
                          fontSize: 12,
                        ),
                      ),
                    ),
                ],
              ),
            );
          },
        );
      },
    );
  }

  Widget _buildInsuranceTab(RfTheme t) {
    return FutureBuilder(
      future: _insuranceRepo.getAllArticleInsurance(),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return Center(
            child: CircularProgressIndicator(color: AppColors.hotPink),
          );
        }
        final insurances = snapshot.data ?? [];
        return ListView.builder(
          padding: EdgeInsets.all(16),
          itemCount: insurances.length,
          itemBuilder: (context, index) {
            final insurance = insurances[index] as Map<String, dynamic>;
            return Container(
              margin: EdgeInsets.only(bottom: 12),
              padding: EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: t.card,
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: t.borderFaint),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        insurance['policy_number'] ?? 'N/A',
                        style: GoogleFonts.outfit(
                          color: t.textPrimary,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                      Container(
                        padding: EdgeInsets.symmetric(
                          horizontal: 8,
                          vertical: 4,
                        ),
                        decoration: BoxDecoration(
                          color: insurance['is_active'] == true
                              ? AppColors.teal.withOpacity(0.1)
                              : AppColors.coral.withOpacity(0.1),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: Text(
                          insurance['is_active'] == true
                              ? 'Activo'
                              : 'Inactivo',
                          style: GoogleFonts.dmSans(
                            color: insurance['is_active'] == true
                                ? AppColors.teal
                                : AppColors.coral,
                            fontSize: 12,
                          ),
                        ),
                      ),
                    ],
                  ),
                  SizedBox(height: 8),
                  Text(
                    'Proveedor: ${insurance['provider'] ?? 'N/A'}',
                    style: GoogleFonts.dmSans(color: t.textMuted, fontSize: 13),
                  ),
                  Text(
                    'Cobertura: \$${insurance['coverage_amount'] ?? 0}',
                    style: GoogleFonts.dmSans(color: t.textMuted, fontSize: 13),
                  ),
                ],
              ),
            );
          },
        );
      },
    );
  }

  Widget _buildAuditTab(RfTheme t) {
    return FutureBuilder(
      future: _auditRepo.getAllAuditLogs(),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return Center(
            child: CircularProgressIndicator(color: AppColors.hotPink),
          );
        }
        final data = snapshot.data as Map<String, dynamic>?;
        final logs = data?['data'] as List<dynamic>? ?? [];
        return ListView.builder(
          padding: EdgeInsets.all(16),
          itemCount: logs.length,
          itemBuilder: (context, index) {
            final log = logs[index] as Map<String, dynamic>;
            return Container(
              margin: EdgeInsets.only(bottom: 12),
              padding: EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: t.card,
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: t.borderFaint),
              ),
              child: Row(
                children: [
                  Container(
                    padding: EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      color: AppColors.sky.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Icon(Icons.history, color: AppColors.sky, size: 20),
                  ),
                  SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          log['action'] ?? 'N/A',
                          style: GoogleFonts.dmSans(
                            color: t.textPrimary,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                        Text(
                          log['entity_type'] ?? '',
                          style: GoogleFonts.dmSans(
                            color: t.textMuted,
                            fontSize: 12,
                          ),
                        ),
                      ],
                    ),
                  ),
                  Text(
                    _formatTimestamp(log['created_at']),
                    style: GoogleFonts.dmSans(color: t.textDim, fontSize: 11),
                  ),
                ],
              ),
            );
          },
        );
      },
    );
  }

  Widget _buildClientPortalTab(RfTheme t) {
    return FutureBuilder(
      future: _clientPortalRepo.getDashboard(),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return Center(
            child: CircularProgressIndicator(color: AppColors.hotPink),
          );
        }
        final data = snapshot.data as Map<String, dynamic>?;
        final user = data?['user'] as Map<String, dynamic>?;
        final upcoming = data?['upcoming_event'] as Map<String, dynamic>?;
        return SingleChildScrollView(
          padding: EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                padding: EdgeInsets.all(20),
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    colors: [AppColors.hotPink, AppColors.violet],
                  ),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Row(
                  children: [
                    CircleAvatar(
                      radius: 30,
                      backgroundColor: Colors.white24,
                      child: Icon(Icons.person, color: Colors.white, size: 30),
                    ),
                    SizedBox(width: 16),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            user?['name'] ?? 'Cliente',
                            style: GoogleFonts.outfit(
                              color: Colors.white,
                              fontSize: 20,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          Text(
                            user?['email'] ?? '',
                            style: GoogleFonts.dmSans(
                              color: Colors.white70,
                              fontSize: 14,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
              SizedBox(height: 20),
              Text(
                'Próximo Evento',
                style: GoogleFonts.outfit(
                  color: t.textPrimary,
                  fontSize: 18,
                  fontWeight: FontWeight.w600,
                ),
              ),
              SizedBox(height: 12),
              if (upcoming != null)
                Container(
                  padding: EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: t.card,
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: AppColors.amber.withOpacity(0.3)),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        upcoming['name'] ?? 'Evento',
                        style: GoogleFonts.outfit(
                          color: t.textPrimary,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                      Text(
                        'Fecha: ${upcoming['date'] ?? 'N/A'}',
                        style: GoogleFonts.dmSans(color: t.textMuted),
                      ),
                    ],
                  ),
                )
              else
                Text(
                  'No hay eventos próximos',
                  style: GoogleFonts.dmSans(color: t.textMuted),
                ),
              SizedBox(height: 20),
              _buildPortalAction(
                t,
                'Ver Mis Eventos',
                Icons.event,
                AppColors.teal,
              ),
              _buildPortalAction(
                t,
                'Mis Pagos',
                Icons.payment,
                AppColors.amber,
              ),
              _buildPortalAction(
                t,
                'Notificaciones',
                Icons.notifications,
                AppColors.violet,
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildPortalAction(
    RfTheme t,
    String title,
    IconData icon,
    Color color,
  ) {
    return Container(
      margin: EdgeInsets.only(bottom: 12),
      padding: EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: t.card,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: t.borderFaint),
      ),
      child: Row(
        children: [
          Container(
            padding: EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: color.withOpacity(0.1),
              borderRadius: BorderRadius.circular(10),
            ),
            child: Icon(icon, color: color),
          ),
          SizedBox(width: 12),
          Expanded(
            child: Text(
              title,
              style: GoogleFonts.dmSans(color: t.textPrimary, fontSize: 16),
            ),
          ),
          Icon(Icons.chevron_right, color: t.textMuted),
        ],
      ),
    );
  }

  String _formatTimestamp(dynamic timestamp) {
    if (timestamp == null) return '';
    if (timestamp is String) {
      final dt = DateTime.tryParse(timestamp);
      if (dt != null)
        return '${dt.day}/${dt.month} ${dt.hour}:${dt.minute.toString().padLeft(2, '0')}';
    }
    return '';
  }
}
