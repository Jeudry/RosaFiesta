import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/app_colors.dart';
import 'package:frontend/core/design_system.dart';

import '../../../auth/presentation/screens/auth_required_sheet.dart';
import '../events_provider.dart';
import 'order_confirmation_screen.dart';

class CheckoutScreen extends StatefulWidget {
  final String eventId;
  final String eventName;
  final double totalAmount;
  final String? recipientName;
  final String? deliveryAddress;

  const CheckoutScreen({
    super.key,
    required this.eventId,
    required this.eventName,
    required this.totalAmount,
    this.recipientName,
    this.deliveryAddress,
  });

  @override
  State<CheckoutScreen> createState() => _CheckoutScreenState();
}

class _CheckoutScreenState extends State<CheckoutScreen>
    with SingleTickerProviderStateMixin {
  int _selectedMethodIndex = 0;
  final _cardNumberController = TextEditingController();
  final _expiryController = TextEditingController();
  final _cvvController = TextEditingController();
  final _nameController = TextEditingController();
  final _phoneController = TextEditingController();
  final _addressController = TextEditingController();
  final _deliveryFeeController = TextEditingController();

  int _deliveryFee = 0;
  String _deliveryZone = '';
  String _deliveryMessage = '';
  bool _isCalculatingDelivery = false;
  bool _isRemoteZone = false;

  final _methods = [
    _PaymentMethod('Tarjeta', Icons.credit_card, AppColors.sky),
    _PaymentMethod('Banco Popular', Icons.account_balance, AppColors.violet),
    _PaymentMethod('Banreservas', Icons.account_balance, AppColors.teal),
    _PaymentMethod('Efectivo', Icons.payments, AppColors.amber),
  ];

  @override
  void initState() {
    super.initState();
    _addressController.text = widget.deliveryAddress ?? '';
    _addressController.addListener(_onAddressChanged);
  }

  void _onAddressChanged() {
    if (_addressController.text.length > 10) {
      _calculateDeliveryFee();
    }
  }

  Future<void> _calculateDeliveryFee() async {
    if (_addressController.text.isEmpty) return;

    setState(() => _isCalculatingDelivery = true);

    try {
      final response = await _callDeliveryApi(_addressController.text);
      if (mounted && response != null) {
        setState(() {
          _deliveryFee = response['fee'] ?? 0;
          _deliveryZone = response['zone'] ?? '';
          _deliveryMessage = response['message'] ?? '';
          _isRemoteZone = _deliveryFee > 2000;
        });
      }
    } catch (e) {
      debugPrint('Error calculating delivery: $e');
    } finally {
      if (mounted) {
        setState(() => _isCalculatingDelivery = false);
      }
    }
  }

  Future<Map<String, dynamic>?> _callDeliveryApi(String address) async {
    // Use ApiClient to call the calculate-delivery endpoint
    try {
      // This would need a method in EventsProvider or direct API call
      // For now, we'll use a simple heuristic based on address
      return _calculateDeliveryLocally(address);
    } catch (e) {
      return null;
    }
  }

  Map<String, dynamic> _calculateDeliveryLocally(String address) {
    final addr = address.toLowerCase();

    // Check for local San Cristóbal keywords
    final localKeywords = ['san cristobal', 'santa cruz', 'ranchitos', 'hatillo', 'berjon', 'mendoza', 'pizarro', 'km 27', 'km 30'];
    final isLocal = localKeywords.any((k) => addr.contains(k));

    // Check for remote keywords
    final remoteKeywords = ['santo domingo', 'santiago', 'la romana', 'punta cana', 'bayahibe', 'puerto plata', 'sosua', 'cabarete', 'samana', 'nagua', 'higuey', 'el seibo'];
    final isRemote = remoteKeywords.any((k) => addr.contains(k));

    if (isLocal) {
      return {'fee': 0, 'zone': 'San Cristóbal Centro', 'message': 'Delivery gratuito en San Cristóbal'};
    } else if (isRemote) {
      return {'fee': 3500, 'zone': 'Zona Remota', 'message': 'Tu dirección está en zona remota. El equipo de RosaFiesta coordinará contigo el envío.'};
    } else {
      return {'fee': 1500, 'zone': 'San Cristóbal Extendido', 'message': 'Delivery dentro de la provincia de San Cristóbal'};
    }
  }

  double get grandTotal => widget.totalAmount + _deliveryFee.toDouble();

  @override
  void dispose() {
    _cardNumberController.dispose();
    _expiryController.dispose();
    _cvvController.dispose();
    _nameController.dispose();
    _phoneController.dispose();
    _addressController.dispose();
    _deliveryFeeController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);
    final isDark = context.watch<ThemeProvider>().isDark;

    return Scaffold(
      backgroundColor: isDark ? const Color(0xFF0D0B1E) : const Color(0xFFF5F0FF),
      body: Stack(
        children: [
          // Background orbs
          Positioned(
            top: -80,
            right: -80,
            child: Container(
              width: 250,
              height: 250,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                gradient: RadialGradient(
                  colors: [
                    AppColors.violet.withValues(alpha: 0.3),
                    Colors.transparent,
                  ],
                ),
              ),
            ),
          ),
          Positioned(
            bottom: -60,
            left: -60,
            child: Container(
              width: 200,
              height: 200,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                gradient: RadialGradient(
                  colors: [
                    AppColors.hotPink.withValues(alpha: 0.2),
                    Colors.transparent,
                  ],
                ),
              ),
            ),
          ),
          // Content
          SafeArea(
            child: Column(
              children: [
                _buildHeader(t),
                Expanded(
                  child: SingleChildScrollView(
                    padding: const EdgeInsets.fromLTRB(20, 0, 20, 20),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const SizedBox(height: 8),
                        _buildDeliveryCard(t),
                        const SizedBox(height: 16),
                        _buildPaymentMethodsSection(t),
                        const SizedBox(height: 20),
                        _buildCardForm(t),
                        const SizedBox(height: 24),
                      ],
                    ),
                  ),
                ),
                _buildConfirmButton(t),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildHeader(RfTheme t) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 8),
      child: Row(
        children: [
          GestureDetector(
            onTap: () => Navigator.pop(context),
            child: Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                color: t.card,
                borderRadius: BorderRadius.circular(14),
                border: Border.all(color: t.borderFaint),
              ),
              child: Icon(Icons.arrow_back_rounded, color: t.textPrimary, size: 22),
            ),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Text(
              'Pago',
              style: GoogleFonts.outfit(
                fontSize: 24,
                fontWeight: FontWeight.w800,
                color: Colors.white,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDeliveryCard(RfTheme t) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: t.card,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: t.borderFaint),
        boxShadow: [
          BoxShadow(
            color: AppColors.violet.withValues(alpha: 0.08),
            blurRadius: 20,
            offset: const Offset(0, 6),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                width: 40,
                height: 40,
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                    colors: [AppColors.teal, AppColors.sky],
                  ),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: const Icon(Icons.location_on_rounded, color: Colors.white, size: 20),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Entrega en',
                      style: GoogleFonts.dmSans(
                        fontSize: 11,
                        fontWeight: FontWeight.w600,
                        color: t.textDim,
                        letterSpacing: 0.5,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      widget.recipientName ?? 'Olivia Rhye',
                      style: GoogleFonts.dmSans(
                        fontSize: 15,
                        fontWeight: FontWeight.w700,
                        color: t.textPrimary,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          // Address input field
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
            decoration: BoxDecoration(
              color: t.isDark
                  ? Colors.white.withValues(alpha: 0.05)
                  : const Color(0xFFF5F0FF),
              borderRadius: BorderRadius.circular(12),
            ),
            child: TextField(
              controller: _addressController,
              style: GoogleFonts.dmSans(
                fontSize: 13,
                fontWeight: FontWeight.w500,
                color: t.textPrimary,
              ),
              decoration: InputDecoration(
                hintText: 'Dirección del evento',
                hintStyle: GoogleFonts.dmSans(fontSize: 13, color: t.textDim),
                prefixIcon: Icon(Icons.home_rounded, color: t.textDim, size: 18),
                border: InputBorder.none,
              ),
              onChanged: (value) {
                if (value.length > 10) {
                  _calculateDeliveryFee();
                }
              },
            ),
          ),
          const SizedBox(height: 12),
          // Delivery fee section
          if (_isCalculatingDelivery)
            const Center(
              child: Padding(
                padding: EdgeInsets.all(8.0),
                child: SizedBox(
                  width: 20,
                  height: 20,
                  child: CircularProgressIndicator(strokeWidth: 2),
                ),
              ),
            )
          else if (_deliveryFee > 0 || _deliveryZone.isNotEmpty) ...[
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: _isRemoteZone
                    ? AppColors.coral.withValues(alpha: 0.1)
                    : AppColors.teal.withValues(alpha: 0.1),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(
                  color: _isRemoteZone
                      ? AppColors.coral.withValues(alpha: 0.3)
                      : AppColors.teal.withValues(alpha: 0.3),
                ),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        'Zona: $_deliveryZone',
                        style: GoogleFonts.dmSans(
                          fontSize: 12,
                          fontWeight: FontWeight.w600,
                          color: t.textPrimary,
                        ),
                      ),
                      Text(
                        _deliveryFee == 0 ? 'Gratuito' : 'RD\$${_deliveryFee.toStringAsFixed(0)}',
                        style: GoogleFonts.outfit(
                          fontSize: 16,
                          fontWeight: FontWeight.w800,
                          color: _isRemoteZone ? AppColors.coral : AppColors.teal,
                        ),
                      ),
                    ],
                  ),
                  if (_deliveryMessage.isNotEmpty) ...[
                    const SizedBox(height: 6),
                    Row(
                      children: [
                        Icon(
                          _isRemoteZone ? Icons.warning_rounded : Icons.info_outline,
                          size: 14,
                          color: _isRemoteZone ? AppColors.coral : t.textDim,
                        ),
                        const SizedBox(width: 6),
                        Expanded(
                          child: Text(
                            _deliveryMessage,
                            style: GoogleFonts.dmSans(
                              fontSize: 11,
                              color: _isRemoteZone ? AppColors.coral : t.textDim,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ],
                ],
              ),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildPaymentMethodsSection(RfTheme t) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Método de pago',
          style: GoogleFonts.outfit(
            fontSize: 18,
            fontWeight: FontWeight.w700,
            color: t.textPrimary,
          ),
        ),
        const SizedBox(height: 12),
        SizedBox(
          height: 90,
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            itemCount: _methods.length,
            separatorBuilder: (_, __) => const SizedBox(width: 10),
            itemBuilder: (context, i) => _buildMethodChip(_methods[i], i, t),
          ),
        ),
      ],
    );
  }

  Widget _buildMethodChip(_PaymentMethod method, int index, RfTheme t) {
    final isSelected = _selectedMethodIndex == index;
    return GestureDetector(
      onTap: () => setState(() => _selectedMethodIndex = index),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        width: 80,
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: isSelected ? method.color.withValues(alpha: 0.15) : t.card,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(
            color: isSelected ? method.color : t.borderFaint,
            width: isSelected ? 2 : 1,
          ),
          boxShadow: isSelected
              ? [
                  BoxShadow(
                    color: method.color.withValues(alpha: 0.3),
                    blurRadius: 12,
                    offset: const Offset(0, 4),
                  ),
                ]
              : null,
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 36,
              height: 36,
              decoration: BoxDecoration(
                color: isSelected
                    ? method.color.withValues(alpha: 0.2)
                    : t.isDark
                        ? Colors.white.withValues(alpha: 0.08)
                        : Colors.grey.withValues(alpha: 0.1),
                borderRadius: BorderRadius.circular(10),
              ),
              child: Icon(
                method.icon,
                color: isSelected ? method.color : t.textDim,
                size: 18,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              method.name,
              style: GoogleFonts.dmSans(
                fontSize: 10,
                fontWeight: FontWeight.w600,
                color: isSelected ? method.color : t.textDim,
              ),
              textAlign: TextAlign.center,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildCardForm(RfTheme t) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: t.card,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: t.borderFaint),
        boxShadow: [
          BoxShadow(
            color: AppColors.hotPink.withValues(alpha: 0.06),
            blurRadius: 20,
            offset: const Offset(0, 6),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Detalles de contacto',
            style: GoogleFonts.outfit(
              fontSize: 16,
              fontWeight: FontWeight.w700,
              color: t.textPrimary,
            ),
          ),
          const SizedBox(height: 16),
          _buildFormField(
            label: 'Teléfono',
            hint: '(809) 555-1234',
            controller: _phoneController,
            t: t,
            prefixIcon: Icons.phone_outlined,
            keyboardType: TextInputType.phone,
          ),
          const SizedBox(height: 24),
          _buildPaymentDetails(t),
        ],
      ),
    );
  }

  Widget _buildPaymentDetails(RfTheme t) {
    final method = _methods[_selectedMethodIndex].name;
    if (method == 'Tarjeta') {
      return _buildCardFields(t);
    } else if (method == 'Efectivo') {
      return _buildCashInstructions(t);
    } else {
      return _buildBankTransferInstructions(t, method);
    }
  }

  Widget _buildCardFields(RfTheme t) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Datos de tarjeta',
          style: GoogleFonts.outfit(
            fontSize: 16,
            fontWeight: FontWeight.w700,
            color: t.textPrimary,
          ),
        ),
        const SizedBox(height: 16),
        _buildFormField(
          label: 'Nombre en la tarjeta',
          hint: 'Olivia Rhye',
          controller: _nameController,
          t: t,
          prefixIcon: Icons.person_outline_rounded,
        ),
        const SizedBox(height: 14),
        _buildFormField(
          label: 'Número de tarjeta',
          hint: '1234 1234 1234 1234',
          controller: _cardNumberController,
          t: t,
          prefixIcon: Icons.credit_card_rounded,
          keyboardType: TextInputType.number,
          inputFormatters: [
            FilteringTextInputFormatter.digitsOnly,
            LengthLimitingTextInputFormatter(16),
            _CardNumberFormatter(),
          ],
        ),
        const SizedBox(height: 14),
        Row(
          children: [
            Expanded(
              child: _buildFormField(
                label: 'Vence',
                hint: 'MM/YY',
                controller: _expiryController,
                t: t,
                keyboardType: TextInputType.datetime,
                inputFormatters: [
                  FilteringTextInputFormatter.digitsOnly,
                  LengthLimitingTextInputFormatter(4),
                  _ExpiryFormatter(),
                ],
              ),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: _buildFormField(
                label: 'CVV',
                hint: '123',
                controller: _cvvController,
                t: t,
                keyboardType: TextInputType.number,
                inputFormatters: [
                  FilteringTextInputFormatter.digitsOnly,
                  LengthLimitingTextInputFormatter(3),
                ],
                obscure: true,
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildBankTransferInstructions(RfTheme t, String bank) {
    final bankInfo = bank == 'Banco Popular'
        ? _BankInfo(
            bankName: 'Banco Popular Dominicano',
            accountType: 'Cuenta Corriente',
            accountNumber: '0123-4567-8901-2345',
            accountHolder: 'Rosa Fiesta Events SRL',
            cedula: '012-3456789-0',
          )
        : _BankInfo(
            bankName: 'Banreservas',
            accountType: 'Cuenta de Ahorros',
            accountNumber: '9876-5432-1098-7654',
            accountHolder: 'Rosa Fiesta Events SRL',
            cedula: '012-3456789-0',
          );

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Transferencia $bank',
          style: GoogleFonts.outfit(
            fontSize: 16,
            fontWeight: FontWeight.w700,
            color: t.textPrimary,
          ),
        ),
        const SizedBox(height: 8),
        Text(
          'Realiza tu transferencia a la siguiente cuenta y envía el comprobante por WhatsApp.',
          style: GoogleFonts.dmSans(
            fontSize: 13,
            color: t.textDim,
          ),
        ),
        const SizedBox(height: 16),
        _buildInfoRow('Banco', bankInfo.bankName, t),
        const SizedBox(height: 10),
        _buildInfoRow('Tipo', bankInfo.accountType, t),
        const SizedBox(height: 10),
        _buildInfoRow('Cuenta', bankInfo.accountNumber, t),
        const SizedBox(height: 10),
        _buildInfoRow('Titular', bankInfo.accountHolder, t),
        const SizedBox(height: 10),
        _buildInfoRow('Cédula', bankInfo.cedula, t),
        const SizedBox(height: 16),
        Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: AppColors.amber.withValues(alpha: 0.1),
            borderRadius: BorderRadius.circular(12),
            border: Border.all(color: AppColors.amber.withValues(alpha: 0.3)),
          ),
          child: Row(
            children: [
              const Icon(Icons.warning_amber_rounded, color: AppColors.amber, size: 20),
              const SizedBox(width: 10),
              Expanded(
                child: Text(
                  'Tu reserva se confirma al verificar el pago.',
                  style: GoogleFonts.dmSans(
                    fontSize: 12,
                    fontWeight: FontWeight.w600,
                    color: AppColors.amber,
                  ),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildCashInstructions(RfTheme t) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Pago en Efectivo',
          style: GoogleFonts.outfit(
            fontSize: 16,
            fontWeight: FontWeight.w700,
            color: t.textPrimary,
          ),
        ),
        const SizedBox(height: 8),
        Text(
          'Puedes pagar en efectivo directamente en nuestra tienda o medianteelada.',
          style: GoogleFonts.dmSans(
            fontSize: 13,
            color: t.textDim,
          ),
        ),
        const SizedBox(height: 16),
        Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: AppColors.teal.withValues(alpha: 0.1),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Row(
            children: [
              const Icon(Icons.location_on, color: AppColors.teal, size: 20),
              const SizedBox(width: 10),
              Expanded(
                child: Text(
                  'Av. Principal 123, San Cristóbal\nLunes a Viernes 9am - 6pm',
                  style: GoogleFonts.dmSans(fontSize: 12, color: t.textPrimary),
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 12),
        Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: AppColors.amber.withValues(alpha: 0.1),
            borderRadius: BorderRadius.circular(12),
            border: Border.all(color: AppColors.amber.withValues(alpha: 0.3)),
          ),
          child: Row(
            children: [
              const Icon(Icons.warning_amber_rounded, color: AppColors.amber, size: 20),
              const SizedBox(width: 10),
              Expanded(
                child: Text(
                  'Tu reserva se confirma al realizar el pago.',
                  style: GoogleFonts.dmSans(
                    fontSize: 12,
                    fontWeight: FontWeight.w600,
                    color: AppColors.amber,
                  ),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildInfoRow(String label, String value, RfTheme t) {
    return Row(
      children: [
        SizedBox(
          width: 80,
          child: Text(
            label,
            style: GoogleFonts.dmSans(
              fontSize: 12,
              fontWeight: FontWeight.w600,
              color: t.textDim,
            ),
          ),
        ),
        Expanded(
          child: Text(
            value,
            style: GoogleFonts.dmSans(
              fontSize: 14,
              fontWeight: FontWeight.w700,
              color: t.textPrimary,
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildFormField({
    required String label,
    required String hint,
    required TextEditingController controller,
    required RfTheme t,
    IconData? prefixIcon,
    TextInputType? keyboardType,
    List<TextInputFormatter>? inputFormatters,
    bool obscure = false,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: GoogleFonts.dmSans(
            fontSize: 11,
            fontWeight: FontWeight.w600,
            color: t.textDim,
            letterSpacing: 0.5,
          ),
        ),
        const SizedBox(height: 6),
        Container(
          decoration: BoxDecoration(
            color: t.isDark
                ? Colors.white.withValues(alpha: 0.04)
                : const Color(0xFFF8F6FF),
            borderRadius: BorderRadius.circular(14),
            border: Border.all(color: t.borderFaint),
          ),
          child: TextField(
            controller: controller,
            obscureText: obscure,
            keyboardType: keyboardType,
            inputFormatters: inputFormatters,
            style: GoogleFonts.dmSans(
              fontSize: 15,
              fontWeight: FontWeight.w500,
              color: t.textPrimary,
            ),
            decoration: InputDecoration(
              hintText: hint,
              hintStyle: GoogleFonts.dmSans(
                fontSize: 14,
                color: t.textDim,
              ),
              prefixIcon: prefixIcon != null
                  ? Icon(prefixIcon, color: t.textDim, size: 20)
                  : null,
              border: InputBorder.none,
              contentPadding: const EdgeInsets.symmetric(
                horizontal: 16,
                vertical: 14,
              ),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildConfirmButton(RfTheme t) {
    return Container(
      padding: EdgeInsets.fromLTRB(
        20,
        16,
        20,
        MediaQuery.of(context).padding.bottom + 16,
      ),
      decoration: BoxDecoration(
        color: t.card,
        border: Border(
          top: BorderSide(color: t.borderFaint),
        ),
      ),
      child: Consumer<EventsProvider>(
        builder: (context, provider, _) {
          return GestureDetector(
            onTap: provider.isLoading ? null : () => _processPayment(context, provider),
            child: Container(
              height: 60,
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: [AppColors.hotPink, AppColors.violet],
                ),
                borderRadius: BorderRadius.circular(30),
                boxShadow: [
                  BoxShadow(
                    color: AppColors.hotPink.withValues(alpha: 0.4),
                    blurRadius: 20,
                    offset: const Offset(0, 6),
                  ),
                ],
              ),
              child: Center(
                child: provider.isLoading
                    ? const SizedBox(
                        width: 24,
                        height: 24,
                        child: CircularProgressIndicator(
                          color: Colors.white,
                          strokeWidth: 2.5,
                        ),
                      )
                    : Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Text(
                            'Confirmar pedido',
                            style: GoogleFonts.dmSans(
                              fontSize: 17,
                              fontWeight: FontWeight.w800,
                              color: Colors.white,
                            ),
                          ),
                          const SizedBox(width: 10),
                          Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 14,
                              vertical: 6,
                            ),
                            decoration: BoxDecoration(
                              color: Colors.white.withValues(alpha: 0.2),
                              borderRadius: BorderRadius.circular(20),
                            ),
                            child: Text(
                              'RD\$${grandTotal.toStringAsFixed(0)}',
                              style: GoogleFonts.outfit(
                                fontSize: 14,
                                fontWeight: FontWeight.w800,
                                color: Colors.white,
                              ),
                            ),
                          ),
                        ],
                      ),
              ),
            ),
          );
        },
      ),
    );
  }

  Future<void> _processPayment(BuildContext context, EventsProvider provider) async {
    // Auth check — block unauthenticated users from paying
    if (AuthRequiredSheet.checkAndShow(context)) return;

    final method = _methods[_selectedMethodIndex].name;
    final success = await provider.payEvent(
      widget.eventId,
      method,
      phone: _phoneController.text,
    );
    if (mounted) {
      if (!success) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(provider.error ?? 'Error procesando pago'),
            backgroundColor: AppColors.coral,
          ),
        );
        return;
      }
      showDialog(
        context: context,
        barrierDismissible: false,
        builder: (ctx) => Dialog(
          backgroundColor: Colors.transparent,
          child: Container(
            padding: const EdgeInsets.all(28),
            decoration: BoxDecoration(
              color: RfTheme.of(ctx).card,
              borderRadius: BorderRadius.circular(24),
            ),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Container(
                  width: 70,
                  height: 70,
                  decoration: BoxDecoration(
                    gradient: const LinearGradient(
                      colors: [AppColors.teal, AppColors.sky],
                    ),
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(Icons.check_rounded, color: Colors.white, size: 36),
                ),
                const SizedBox(height: 20),
                ShaderMask(
                  shaderCallback: (b) => AppColors.titleGradient.createShader(b),
                  child: Text(
                    '¡Pago Exitoso!',
                    style: GoogleFonts.outfit(
                      fontSize: 22,
                      fontWeight: FontWeight.w800,
                      color: Colors.white,
                    ),
                  ),
                ),
                const SizedBox(height: 10),
                Text(
                  'Tu evento ha sido reservado correctamente.',
                  textAlign: TextAlign.center,
                  style: GoogleFonts.dmSans(
                    fontSize: 14,
                    color: RfTheme.of(ctx).textDim,
                  ),
                ),
                const SizedBox(height: 24),
                GestureDetector(
                  onTap: () {
                    Navigator.pop(ctx);
                    Navigator.pop(context);
                    // Navigate to order confirmation screen
                    Navigator.pushReplacement(
                      context,
                      MaterialPageRoute(
                        builder: (_) => OrderConfirmationScreen(eventId: widget.eventId),
                      ),
                    );
                  },
                  child: Container(
                    width: double.infinity,
                    height: 50,
                    decoration: BoxDecoration(
                      gradient: AppColors.buttonGradient,
                      borderRadius: BorderRadius.circular(25),
                    ),
                    child: Center(
                      child: Text(
                        'Ver confirmación',
                        style: GoogleFonts.dmSans(
                          fontSize: 15,
                          fontWeight: FontWeight.w700,
                          color: Colors.white,
                        ),
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      );
    }
  }
}

class _PaymentMethod {
  final String name;
  final IconData icon;
  final Color color;
  const _PaymentMethod(this.name, this.icon, this.color);
}

class _BankInfo {
  final String bankName;
  final String accountType;
  final String accountNumber;
  final String accountHolder;
  final String cedula;
  const _BankInfo({
    required this.bankName,
    required this.accountType,
    required this.accountNumber,
    required this.accountHolder,
    required this.cedula,
  });
}

class _CardNumberFormatter extends TextInputFormatter {
  @override
  TextEditingValue formatEditUpdate(
    TextEditingValue oldValue,
    TextEditingValue newValue,
  ) {
    final text = newValue.text.replaceAll(' ', '');
    final buffer = StringBuffer();
    for (int i = 0; i < text.length; i++) {
      if (i > 0 && i % 4 == 0) buffer.write(' ');
      buffer.write(text[i]);
    }
    return TextEditingValue(
      text: buffer.toString(),
      selection: TextSelection.collapsed(offset: buffer.length),
    );
  }
}

class _ExpiryFormatter extends TextInputFormatter {
  @override
  TextEditingValue formatEditUpdate(
    TextEditingValue oldValue,
    TextEditingValue newValue,
  ) {
    final text = newValue.text.replaceAll('/', '');
    if (text.length >= 2) {
      return TextEditingValue(
        text: '${text.substring(0, 2)}/${text.substring(2)}',
        selection: TextSelection.collapsed(
          offset: text.length >= 2 ? 3 + text.length - 2 : text.length,
        ),
      );
    }
    return newValue;
  }
}