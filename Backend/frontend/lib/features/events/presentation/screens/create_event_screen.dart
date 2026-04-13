import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';
import '../events_provider.dart';

/// Event Creation Wizard - Step-by-step flow
class CreateEventScreen extends StatefulWidget {
  const CreateEventScreen({super.key});

  @override
  State<CreateEventScreen> createState() => _CreateEventScreenState();
}

class _CreateEventScreenState extends State<CreateEventScreen> {
  int _currentStep = 0;
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _locationController = TextEditingController();
  final _budgetController = TextEditingController();
  final _guestCountController = TextEditingController();
  DateTime _selectedDate = DateTime.now().add(const Duration(days: 7));
  String? _selectedEventType;

  final _eventTypes = [
    _EventType('Cumpleaños', Icons.cake_rounded, [AppColors.hotPink, AppColors.coral]),
    _EventType('Boda', Icons.favorite_rounded, [AppColors.teal, AppColors.sky]),
    _EventType('Quinceañera', Icons.auto_awesome_rounded, [AppColors.violet, AppColors.hotPink]),
    _EventType('Baby Shower', Icons.child_friendly_rounded, [AppColors.amber, Color(0xFFFF8C00)]),
    _EventType('Graduación', Icons.school_rounded, [AppColors.violet, Color(0xFF6366F1)]),
    _EventType('Corporativo', Icons.business_rounded, [AppColors.teal, AppColors.violet]),
    _EventType('Otro', Icons.celebration_rounded, [AppColors.coral, AppColors.amber]),
  ];

  @override
  void dispose() {
    _nameController.dispose();
    _locationController.dispose();
    _budgetController.dispose();
    _guestCountController.dispose();
    super.dispose();
  }

  void _nextStep() {
    if (_currentStep == 0 && _selectedEventType == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Selecciona un tipo de evento')),
      );
      return;
    }
    if (_currentStep < 3) {
      setState(() => _currentStep++);
    } else {
      _submit();
    }
  }

  void _prevStep() {
    if (_currentStep > 0) {
      setState(() => _currentStep--);
    } else {
      Navigator.pop(context);
    }
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;

    final name = _nameController.text.isEmpty
        ? '${_eventTypes.firstWhere((e) => e.name == _selectedEventType).name} de ${_guestCountController.text} personas'
        : _nameController.text;

    final success = await context.read<EventsProvider>().createEvent({
      'name': name,
      'date': _selectedDate.toIso8601String(),
      'location': _locationController.text,
      'budget': double.tryParse(_budgetController.text) ?? 0.0,
      'guest_count': int.tryParse(_guestCountController.text) ?? 0,
    });

    if (success && mounted) {
      Navigator.pop(context);
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('¡Evento creado! Ahora agrega artículos desde el catálogo')),
      );
    } else if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Error al crear evento')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);

    return Scaffold(
      backgroundColor: t.base,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: Icon(Icons.arrow_back_rounded, color: t.textPrimary),
          onPressed: _prevStep,
        ),
        title: ShaderMask(
          shaderCallback: (b) => AppColors.titleGradient.createShader(b),
          child: Text(
            'Nuevo Evento',
            style: GoogleFonts.outfit(
              fontSize: 20,
              fontWeight: FontWeight.w800,
              color: Colors.white,
            ),
          ),
        ),
      ),
      body: Column(
        children: [
          _buildProgressIndicator(t),
          Expanded(
            child: Form(
              key: _formKey,
              child: AnimatedSwitcher(
                duration: const Duration(milliseconds: 300),
                child: _buildStepContent(t),
              ),
            ),
          ),
          _buildBottomBar(t),
        ],
      ),
    );
  }

  Widget _buildProgressIndicator(RfTheme t) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
      child: Row(
        children: List.generate(4, (i) {
          final isActive = i <= _currentStep;
          final isCompleted = i < _currentStep;
          return Expanded(
            child: Row(
              children: [
                Container(
                  width: 28,
                  height: 28,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    gradient: isActive
                        ? LinearGradient(colors: [AppColors.hotPink, AppColors.violet])
                        : null,
                    color: isActive ? null : t.card,
                    border: Border.all(
                      color: isActive ? Colors.transparent : t.borderFaint,
                    ),
                  ),
                  child: Center(
                    child: isCompleted
                        ? const Icon(Icons.check, color: Colors.white, size: 16)
                        : Text(
                            '${i + 1}',
                            style: GoogleFonts.dmSans(
                              fontSize: 12,
                              fontWeight: FontWeight.w700,
                              color: isActive ? Colors.white : t.textDim,
                            ),
                          ),
                  ),
                ),
                if (i < 3)
                  Expanded(
                    child: Container(
                      height: 2,
                      margin: const EdgeInsets.symmetric(horizontal: 4),
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(1),
                        color: i < _currentStep
                            ? AppColors.hotPink
                            : (t.isDark ? Colors.white.withOpacity(0.1) : Colors.black.withOpacity(0.1)),
                      ),
                    ),
                  ),
              ],
            ),
          );
        }),
      ),
    );
  }

  Widget _buildStepContent(RfTheme t) {
    switch (_currentStep) {
      case 0:
        return _buildEventTypeStep(t);
      case 1:
        return _buildDateLocationStep(t);
      case 2:
        return _buildBudgetGuestsStep(t);
      case 3:
        return _buildSummaryStep(t);
      default:
        return const SizedBox.shrink();
    }
  }

  Widget _buildEventTypeStep(RfTheme t) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '¿Qué tipo de evento\nplaneas?',
            style: GoogleFonts.outfit(
              fontSize: 28,
              fontWeight: FontWeight.w800,
              color: t.textPrimary,
              height: 1.2,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Selecciona la categoría que mejor describa tu celebración',
            style: GoogleFonts.dmSans(
              fontSize: 14,
              color: t.textMuted,
            ),
          ),
          const SizedBox(height: 24),
          ...List.generate(_eventTypes.length, (i) {
            final type = _eventTypes[i];
            final isSelected = _selectedEventType == type.name;
            return Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: GestureDetector(
                onTap: () => setState(() => _selectedEventType = type.name),
                child: AnimatedContainer(
                  duration: const Duration(milliseconds: 200),
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: t.card,
                    borderRadius: BorderRadius.circular(20),
                    border: Border.all(
                      color: isSelected ? type.colors.first : t.borderFaint,
                      width: isSelected ? 2 : 1,
                    ),
                    boxShadow: isSelected
                        ? [
                            BoxShadow(
                              color: type.colors.first.withOpacity(0.2),
                              blurRadius: 12,
                              offset: const Offset(0, 4),
                            ),
                          ]
                        : null,
                  ),
                  child: Row(
                    children: [
                      Container(
                        width: 50,
                        height: 50,
                        decoration: BoxDecoration(
                          gradient: LinearGradient(colors: type.colors),
                          borderRadius: BorderRadius.circular(14),
                        ),
                        child: Icon(type.icon, color: Colors.white, size: 24),
                      ),
                      const SizedBox(width: 16),
                      Expanded(
                        child: Text(
                          type.name,
                          style: GoogleFonts.dmSans(
                            fontSize: 16,
                            fontWeight: FontWeight.w600,
                            color: t.textPrimary,
                          ),
                        ),
                      ),
                      if (isSelected)
                        Container(
                          width: 24,
                          height: 24,
                          decoration: BoxDecoration(
                            gradient: LinearGradient(colors: type.colors),
                            shape: BoxShape.circle,
                          ),
                          child: const Icon(Icons.check, color: Colors.white, size: 16),
                        ),
                    ],
                  ),
                ),
              ),
            );
          }),
        ],
      ),
    );
  }

  Widget _buildDateLocationStep(RfTheme t) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '¿Cuándo y dónde?',
            style: GoogleFonts.outfit(
              fontSize: 28,
              fontWeight: FontWeight.w800,
              color: t.textPrimary,
              height: 1.2,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Selecciona la fecha y describe la ubicación de tu evento',
            style: GoogleFonts.dmSans(
              fontSize: 14,
              color: t.textMuted,
            ),
          ),
          const SizedBox(height: 24),
          // Date picker card
          GestureDetector(
            onTap: () async {
              final picked = await showDatePicker(
                context: context,
                initialDate: _selectedDate,
                firstDate: DateTime.now(),
                lastDate: DateTime(2030),
              );
              if (picked != null) {
                setState(() => _selectedDate = picked);
              }
            },
            child: Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: t.card,
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: t.borderFaint),
              ),
              child: Row(
                children: [
                  Container(
                    width: 50,
                    height: 50,
                    decoration: BoxDecoration(
                      gradient: const LinearGradient(
                        colors: [AppColors.hotPink, AppColors.violet],
                      ),
                      borderRadius: BorderRadius.circular(14),
                    ),
                    child: const Icon(Icons.calendar_today_rounded, color: Colors.white, size: 24),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Fecha del evento',
                          style: GoogleFonts.dmSans(
                            fontSize: 12,
                            color: t.textDim,
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          '${_selectedDate.day} de ${_getMonthName(_selectedDate.month)} del ${_selectedDate.year}',
                          style: GoogleFonts.outfit(
                            fontSize: 18,
                            fontWeight: FontWeight.w700,
                            color: t.textPrimary,
                          ),
                        ),
                      ],
                    ),
                  ),
                  Icon(Icons.chevron_right, color: t.textDim),
                ],
              ),
            ),
          ),
          const SizedBox(height: 20),
          // Location field
          Container(
            decoration: BoxDecoration(
              color: t.card,
              borderRadius: BorderRadius.circular(20),
              border: Border.all(color: t.borderFaint),
            ),
            child: TextFormField(
              controller: _locationController,
              style: GoogleFonts.dmSans(fontSize: 16, color: t.textPrimary),
              decoration: InputDecoration(
                hintText: 'Ej: Salón Azul, Av. Principal 123',
                hintStyle: GoogleFonts.dmSans(fontSize: 16, color: t.textDim),
                prefixIcon: Container(
                  margin: const EdgeInsets.all(12),
                  width: 40,
                  height: 40,
                  decoration: BoxDecoration(
                    color: AppColors.teal.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: const Icon(Icons.location_on_rounded, color: AppColors.teal, size: 20),
                ),
                border: InputBorder.none,
                contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBudgetGuestsStep(RfTheme t) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Presupuesto y\nguestes',
            style: GoogleFonts.outfit(
              fontSize: 28,
              fontWeight: FontWeight.w800,
              color: t.textPrimary,
              height: 1.2,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Configura tu presupuesto estimado y número de invitados',
            style: GoogleFonts.dmSans(
              fontSize: 14,
              color: t.textMuted,
            ),
          ),
          const SizedBox(height: 24),
          // Budget field
          Container(
            decoration: BoxDecoration(
              color: t.card,
              borderRadius: BorderRadius.circular(20),
              border: Border.all(color: t.borderFaint),
            ),
            child: TextFormField(
              controller: _budgetController,
              keyboardType: TextInputType.number,
              style: GoogleFonts.dmSans(fontSize: 24, fontWeight: FontWeight.w700, color: t.textPrimary),
              decoration: InputDecoration(
                hintText: '0',
                hintStyle: GoogleFonts.outfit(fontSize: 24, fontWeight: FontWeight.w700, color: t.textDim),
                prefixIcon: Container(
                  margin: const EdgeInsets.all(12),
                  width: 40,
                  height: 40,
                  decoration: BoxDecoration(
                    gradient: const LinearGradient(colors: [AppColors.violet, AppColors.hotPink]),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: Center(
                    child: Text(
                      'RD\$',
                      style: GoogleFonts.dmSans(
                        fontSize: 12,
                        fontWeight: FontWeight.w800,
                        color: Colors.white,
                      ),
                    ),
                  ),
                ),
                border: InputBorder.none,
                contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
              ),
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Presupuesto estimado para decoraciones y alquiler',
            style: GoogleFonts.dmSans(fontSize: 12, color: t.textDim),
          ),
          const SizedBox(height: 24),
          // Guest count field
          Container(
            decoration: BoxDecoration(
              color: t.card,
              borderRadius: BorderRadius.circular(20),
              border: Border.all(color: t.borderFaint),
            ),
            child: TextFormField(
              controller: _guestCountController,
              keyboardType: TextInputType.number,
              style: GoogleFonts.dmSans(fontSize: 24, fontWeight: FontWeight.w700, color: t.textPrimary),
              decoration: InputDecoration(
                hintText: '0',
                hintStyle: GoogleFonts.outfit(fontSize: 24, fontWeight: FontWeight.w700, color: t.textDim),
                prefixIcon: Container(
                  margin: const EdgeInsets.all(12),
                  width: 40,
                  height: 40,
                  decoration: BoxDecoration(
                    gradient: const LinearGradient(colors: [AppColors.teal, AppColors.sky]),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: const Icon(Icons.people_rounded, color: Colors.white, size: 20),
                ),
                border: InputBorder.none,
                contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
              ),
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Cantidad aproximada de invitados',
            style: GoogleFonts.dmSans(fontSize: 12, color: t.textDim),
          ),
        ],
      ),
    );
  }

  Widget _buildSummaryStep(RfTheme t) {
    final eventType = _eventTypes.firstWhere(
      (e) => e.name == _selectedEventType,
      orElse: () => _eventTypes.first,
    );

    return SingleChildScrollView(
      padding: const EdgeInsets.all(24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Resumen',
            style: GoogleFonts.outfit(
              fontSize: 28,
              fontWeight: FontWeight.w800,
              color: t.textPrimary,
              height: 1.2,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Revisa los detalles de tu nuevo evento',
            style: GoogleFonts.dmSans(
              fontSize: 14,
              color: t.textMuted,
            ),
          ),
          const SizedBox(height: 24),
          // Summary card
          Container(
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: eventType.colors,
              ),
              borderRadius: BorderRadius.circular(24),
              boxShadow: [
                BoxShadow(
                  color: eventType.colors.first.withOpacity(0.3),
                  blurRadius: 20,
                  offset: const Offset(0, 8),
                ),
              ],
            ),
            child: Column(
              children: [
                Container(
                  width: 70,
                  height: 70,
                  decoration: BoxDecoration(
                    color: Colors.white.withOpacity(0.2),
                    shape: BoxShape.circle,
                  ),
                  child: Icon(eventType.icon, color: Colors.white, size: 36),
                ),
                const SizedBox(height: 16),
                Text(
                  _selectedEventType ?? 'Evento',
                  style: GoogleFonts.outfit(
                    fontSize: 24,
                    fontWeight: FontWeight.w800,
                    color: Colors.white,
                  ),
                ),
                const SizedBox(height: 20),
                _summaryRow(
                  Icons.calendar_today_rounded,
                  '${_selectedDate.day} de ${_getMonthName(_selectedDate.month)} del ${_selectedDate.year}',
                  Colors.white,
                ),
                if (_locationController.text.isNotEmpty) ...[
                  const SizedBox(height: 12),
                  _summaryRow(
                    Icons.location_on_rounded,
                    _locationController.text,
                    Colors.white,
                  ),
                ],
                if (_guestCountController.text.isNotEmpty) ...[
                  const SizedBox(height: 12),
                  _summaryRow(
                    Icons.people_rounded,
                    '${_guestCountController.text} invitados',
                    Colors.white,
                  ),
                ],
                if (_budgetController.text.isNotEmpty) ...[
                  const SizedBox(height: 12),
                  _summaryRow(
                    Icons.attach_money_rounded,
                    'RD\$ ${_budgetController.text} presupuesto',
                    Colors.white,
                  ),
                ],
              ],
            ),
          ),
          const SizedBox(height: 20),
          Text(
            'Después de crear el evento, podrás explorar el catálogo y agregar artículos a tu evento.',
            style: GoogleFonts.dmSans(
              fontSize: 13,
              color: t.textMuted,
              height: 1.5,
            ),
          ),
        ],
      ),
    );
  }

  Widget _summaryRow(IconData icon, String text, Color color) {
    return Row(
      children: [
        Icon(icon, color: Colors.white.withOpacity(0.8), size: 18),
        const SizedBox(width: 10),
        Flexible(
          child: Text(
            text,
            style: GoogleFonts.dmSans(
              fontSize: 14,
              color: Colors.white,
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildBottomBar(RfTheme t) {
    final stepLabels = ['Tipo', 'Fecha', 'Detalles', 'Crear'];
    return Container(
      padding: EdgeInsets.fromLTRB(
        24,
        16,
        24,
        16 + MediaQuery.of(context).padding.bottom,
      ),
      decoration: BoxDecoration(
        color: t.card,
        border: Border(top: BorderSide(color: t.borderFaint)),
      ),
      child: Row(
        children: [
          if (_currentStep > 0)
            TextButton(
              onPressed: _prevStep,
              child: Text(
                'Atrás',
                style: GoogleFonts.dmSans(
                  fontSize: 14,
                  color: t.textDim,
                ),
              ),
            ),
          const Spacer(),
          GestureDetector(
            onTap: _nextStep,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 14),
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                  colors: [AppColors.hotPink, AppColors.violet],
                ),
                borderRadius: BorderRadius.circular(30),
                boxShadow: [
                  BoxShadow(
                    color: AppColors.hotPink.withOpacity(0.3),
                    blurRadius: 16,
                    offset: const Offset(0, 4),
                  ),
                ],
              ),
              child: Row(
                children: [
                  Text(
                    _currentStep == 3 ? 'Crear Evento' : stepLabels[_currentStep + 1],
                    style: GoogleFonts.dmSans(
                      fontSize: 15,
                      fontWeight: FontWeight.w700,
                      color: Colors.white,
                    ),
                  ),
                  const SizedBox(width: 8),
                  Icon(
                    _currentStep == 3 ? Icons.celebration_rounded : Icons.arrow_forward_rounded,
                    color: Colors.white,
                    size: 18,
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  String _getMonthName(int month) {
    const months = [
      'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
      'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
    ];
    return months[month - 1];
  }
}

class _EventType {
  final String name;
  final IconData icon;
  final List<Color> colors;

  const _EventType(this.name, this.icon, this.colors);
}
