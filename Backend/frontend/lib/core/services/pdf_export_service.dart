import 'dart:typed_data';
import 'package:pdf/pdf.dart';
import 'package:pdf/widgets.dart' as pw;
import 'package:printing/printing.dart';
import 'package:intl/intl.dart';
import '../../features/events/data/event_model.dart';
import '../../features/guests/data/guest_model.dart';
import '../../features/tasks/data/task_model.dart';
import '../../features/events/data/timeline_model.dart';

class PdfExportService {
  static Future<void> generateEventReport({
    required Event event,
    required List<EventItem> products,
    required List<Guest> guests,
    required List<EventTask> tasks,
    required List<TimelineItem> timeline,
  }) async {
    final pdf = pw.Document();

    final dateFormat = DateFormat('dd/MM/yyyy');
    final timeFormat = DateFormat('HH:mm');

    pdf.addPage(
      pw.MultiPage(
        pageFormat: PdfPageFormat.a4,
        margin: const pw.EdgeInsets.all(32),
        build: (pw.Context context) {
          return [
            // Header
            pw.Header(
              level: 0,
              child: pw.Row(
                mainAxisAlignment: pw.MainAxisAlignment.spaceBetween,
                children: [
                  pw.Text(event.status == 'paid' ? 'FACTURA DE RESERVA' : 'Rosa Fiesta - Resumen de Evento', 
                    style: pw.TextStyle(fontSize: 24, fontWeight: pw.FontWeight.bold, color: PdfColors.pink)
                  ),
                  pw.Text(dateFormat.format(DateTime.now()), style: const pw.TextStyle(color: PdfColors.grey)),
                ],
              ),
            ),
            pw.SizedBox(height: 20),

            // Event Details
            pw.Container(
              padding: const pw.EdgeInsets.all(10),
              decoration: pw.BoxDecoration(
                color: PdfColors.grey100,
                borderRadius: const pw.BorderRadius.all(pw.Radius.circular(8)),
              ),
              child: pw.Column(
                crossAxisAlignment: pw.Start,
                children: [
                   pw.Row(
                     mainAxisAlignment: pw.MainAxisAlignment.spaceBetween,
                     children: [
                       pw.Column(
                         crossAxisAlignment: pw.Start,
                         children: [
                           pw.Text(event.name, style: pw.TextStyle(fontSize: 20, fontWeight: pw.FontWeight.bold)),
                           pw.Text('Fecha: ${dateFormat.format(event.date)}'),
                           pw.Text('Ubicación: ${event.location}'),
                         ]
                       ),
                       if (event.status == 'paid')
                        pw.Column(
                          crossAxisAlignment: pw.End,
                          children: [
                            pw.Text('ESTADO: PAGADO', style: pw.TextStyle(fontWeight: pw.FontWeight.bold, color: PdfColors.green)),
                            pw.Text('Método: ${event.paymentMethod}'),
                            pw.Text('ID Pago: ${event.id.substring(0,8).toUpperCase()}'),
                          ]
                        ),
                     ]
                   ),
                  pw.SizedBox(height: 5),
                  pw.Text('Presupuesto Estimado: \$${event.budget.toStringAsFixed(2)}'),
                  if (event.additionalCosts > 0)
                    pw.Text('Costos Adicionales: \$${event.additionalCosts.toStringAsFixed(2)}'),
                  pw.Text('Total Final: \$${(event.budget + event.additionalCosts).toStringAsFixed(2)}', style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
                ],
              ),
            ),
            pw.SizedBox(height: 20),

            // Budget Section
            pw.Text('Presupuesto y Productos', style: pw.TextStyle(fontSize: 18, fontWeight: pw.FontWeight.bold)),
            pw.Divider(),
            _buildProductsTable(products),
            pw.SizedBox(height: 10),
            
            // Totals
            pw.Row(
              mainAxisAlignment: pw.MainAxisAlignment.end,
              children: [
                pw.Column(
                  crossAxisAlignment: pw.End,
                  children: [
                    pw.Text('Subtotal Productos: \$${products.fold(0.0, (sum, item) => sum + (item.price ?? 0) * item.quantity).toStringAsFixed(2)}'),
                    if (event.additionalCosts > 0)
                      pw.Text('Costos Adicionales: \$${event.additionalCosts.toStringAsFixed(2)}'),
                    pw.Text('TOTAL: \$${(products.fold(0.0, (sum, item) => sum + (item.price ?? 0) * item.quantity) + event.additionalCosts).toStringAsFixed(2)}', 
                      style: pw.TextStyle(fontSize: 16, fontWeight: pw.FontWeight.bold)
                    ),
                  ]
                )
              ]
            ),
            pw.SizedBox(height: 20),

            // Guests Section
            pw.Text('Lista de Invitados (${guests.length})', style: pw.TextStyle(fontSize: 18, fontWeight: pw.FontWeight.bold)),
            pw.Divider(),
            _buildGuestsList(guests),
            pw.SizedBox(height: 20),

            // Timeline Section
            pw.Text('Cronograma (Timeline)', style: pw.TextStyle(fontSize: 18, fontWeight: pw.FontWeight.bold)),
            pw.Divider(),
            _buildTimeline(timeline, timeFormat),
            pw.SizedBox(height: 20),

            // Tasks Section
            pw.Text('Tareas Pendientes/Completadas', style: pw.TextStyle(fontSize: 18, fontWeight: pw.FontWeight.bold)),
            pw.Divider(),
            _buildTasksList(tasks),
          ];
        },
      ),
    );

    await Printing.layoutPdf(
      onLayout: (PdfPageFormat format) async => pdf.save(),
      name: 'RosaFiesta_${event.name.replaceAll(' ', '_')}.pdf',
    );
  }

  static pw.Widget _buildProductsTable(List<EventItem> products) {
    return pw.TableHelper.fromTextArray(
      context: null,
      headerStyle: pw.TextStyle(fontWeight: pw.FontWeight.bold),
      data: <List<String>>[
        <String>['Producto', 'Cant.', 'Precio Unit.', 'Total'],
        ...products.map((item) => [
          item.article?.nameTemplate ?? 'N/A',
          item.quantity.toString(),
          '\$${(item.price ?? 0).toStringAsFixed(2)}',
          '\$${((item.price ?? 0) * item.quantity).toStringAsFixed(2)}',
        ]),
      ],
    );
  }

  static pw.Widget _buildGuestsList(List<Guest> guests) {
    if (guests.isEmpty) return pw.Text('No hay invitados registrados.');
    return pw.Column(
      crossAxisAlignment: pw.CrossAxisAlignment.start,
      children: guests.map((g) => pw.Bullet(text: '${g.name} (${g.email ?? "Sin email"}) - ${g.status}')).toList(),
    );
  }

  static pw.Widget _buildTimeline(List<TimelineItem> items, DateFormat timeFormat) {
    if (items.isEmpty) return pw.Text('No hay actividades en el cronograma.');
    return pw.Column(
      crossAxisAlignment: pw.CrossAxisAlignment.start,
      children: items.map((item) => pw.Padding(
        padding: const pw.EdgeInsets.symmetric(vertical: 2),
        child: pw.Text('${timeFormat.format(item.startTime)} - ${timeFormat.format(item.endTime)}: ${item.title}'),
      )).toList(),
    );
  }

  static pw.Widget _buildTasksList(List<EventTask> tasks) {
    if (tasks.isEmpty) return pw.Text('No hay tareas registradas.');
    return pw.Column(
      crossAxisAlignment: pw.CrossAxisAlignment.start,
      children: tasks.map((t) => pw.Row(
        children: [
          pw.Container(
            width: 10,
            height: 10,
            decoration: pw.BoxDecoration(
              shape: pw.BoxShape.circle,
              color: t.status == 'completed' ? PdfColors.green : PdfColors.orange,
            ),
          ),
          pw.SizedBox(width: 5),
          pw.Text(t.title),
        ],
      )).toList(),
    );
  }
}
