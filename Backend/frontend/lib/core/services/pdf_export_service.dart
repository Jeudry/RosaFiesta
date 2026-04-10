import 'dart:typed_data';
import 'package:pdf/pdf.dart';
import 'package:pdf/widgets.dart' as pw;
import 'package:printing/printing.dart';
import 'package:intl/intl.dart';
import '../../features/events/data/event_model.dart';
import '../../features/guests/data/guest_model.dart';
import '../../features/tasks/data/task_model.dart';
import '../../features/events/data/timeline_model.dart';
import '../../features/events/data/event_debrief_model.dart';

class PdfExportService {
  static Future<void> generateEventReport({
    required Event event,
    required List<EventItem> products,
    required List<Guest> guests,
    required List<EventTask> tasks,
    required List<TimelineItem> timeline,
    EventDebrief? debrief,
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
                  pw.Column(
                    crossAxisAlignment: pw.CrossAxisAlignment.start,
                    children: [
                      pw.Text(event.status == 'paid' || event.status == 'completed' ? 'FACTURA FINAL DE EVENTO' : 'Rosa Fiesta - Resumen de Evento', 
                        style: pw.TextStyle(fontSize: 24, fontWeight: pw.FontWeight.bold, color: PdfColors.pink)
                      ),
                      pw.Text('Rosa Fiesta Eventos & Decoración', style: const pw.TextStyle(fontSize: 10, color: PdfColors.grey700)),
                    ]
                  ),
                  pw.Column(
                    crossAxisAlignment: pw.CrossAxisAlignment.end,
                    children: [
                       pw.Text('Fecha de Emisión: ${dateFormat.format(DateTime.now())}', style: const pw.TextStyle(fontSize: 10, color: PdfColors.grey)),
                       pw.Text('ID: ${event.id.substring(0,8).toUpperCase()}', style: const pw.TextStyle(fontSize: 10, color: PdfColors.grey)),
                    ]
                  ),
                ],
              ),
            ),
            pw.SizedBox(height: 20),

            // Event & Client Details
            pw.Row(
              crossAxisAlignment: pw.CrossAxisAlignment.start,
              children: [
                pw.Expanded(
                  child: pw.Column(
                    crossAxisAlignment: pw.CrossAxisAlignment.start,
                    children: [
                      pw.Text('DETALLES DEL EVENTO', style: pw.TextStyle(fontWeight: pw.FontWeight.bold, fontSize: 12, color: PdfColors.pink)),
                      pw.Divider(color: PdfColors.pink, thickness: 0.5),
                      pw.Text(event.name, style: pw.TextStyle(fontSize: 16, fontWeight: pw.FontWeight.bold)),
                      pw.Text('Fecha: ${event.date != null ? dateFormat.format(event.date!) : "Sin definir"}'),
                      pw.Text('Ubicación: ${event.location}'),
                      pw.Text('Invitados: ${event.guestCount}'),
                    ]
                  ),
                ),
                pw.SizedBox(width: 20),
                pw.Expanded(
                  child: pw.Column(
                    crossAxisAlignment: pw.CrossAxisAlignment.start,
                    children: [
                      pw.Text('ESTADO DE PAGO', style: pw.TextStyle(fontWeight: pw.FontWeight.bold, fontSize: 12, color: PdfColors.pink)),
                      pw.Divider(color: PdfColors.pink, thickness: 0.5),
                      pw.Text('Estado: ${event.status.toUpperCase()}', style: pw.TextStyle(fontWeight: pw.FontWeight.bold, color: event.status == 'paid' || event.status == 'completed' ? PdfColors.green : PdfColors.orange)),
                      if (event.status == 'paid' || event.status == 'completed') ...[
                        pw.Text('Método: ${event.paymentMethod ?? "N/A"}'),
                        pw.Text('Referencia: ${event.id.substring(0,8).toUpperCase()}'),
                      ],
                    ]
                  ),
                ),
              ]
            ),
            pw.SizedBox(height: 20),

            // Analysis Section (New)
            if (debrief != null) ...[
              pw.Text('ANÁLISIS DE EJECUCIÓN', style: pw.TextStyle(fontWeight: pw.FontWeight.bold, fontSize: 12, color: PdfColors.pink)),
              pw.Divider(color: PdfColors.pink, thickness: 0.5),
              pw.Row(
                children: [
                  _buildMetricCard('Puntualidad', '${debrief.punctualityScore.toStringAsFixed(0)}%'),
                  pw.SizedBox(width: 10),
                  _buildMetricCard('Tareas', '${debrief.completionStats.completedTasks}/${debrief.completionStats.totalTasks}'),
                  pw.SizedBox(width: 10),
                  _buildMetricCard('Cronograma', '${debrief.completionStats.completedTimeline}/${debrief.completionStats.totalTimeline}'),
                ]
              ),
              pw.SizedBox(height: 20),
            ],

            // Products Table
            pw.Text('DETALLE DE PRODUCTOS Y SERVICIOS', style: pw.TextStyle(fontWeight: pw.FontWeight.bold, fontSize: 12, color: PdfColors.pink)),
            pw.Divider(color: PdfColors.pink, thickness: 0.5),
            _buildProductsTable(products),
            pw.SizedBox(height: 10),
            
            // Totals and Budget Analysis
            pw.Row(
              crossAxisAlignment: pw.CrossAxisAlignment.start,
              children: [
                if (debrief != null)
                  pw.Expanded(
                    child: pw.Column(
                      crossAxisAlignment: pw.CrossAxisAlignment.start,
                      children: [
                        pw.Text('RESUMEN DE PRESUPUESTO', style: pw.TextStyle(fontWeight: pw.FontWeight.bold, fontSize: 10)),
                        pw.Text('Presupuesto Estimado: \$${debrief.budgetAnalysis.estimatedBudget.toStringAsFixed(2)}', style: const pw.TextStyle(fontSize: 10)),
                        pw.Text('Total Gastado Real: \$${debrief.budgetAnalysis.actualSpent.toStringAsFixed(2)}', style: const pw.TextStyle(fontSize: 10)),
                        pw.Text('Diferencia: \$${debrief.budgetAnalysis.difference.toStringAsFixed(2)}', 
                          style: pw.TextStyle(fontSize: 10, color: debrief.budgetAnalysis.isOverBudget ? PdfColors.red : PdfColors.green, fontWeight: pw.FontWeight.bold)
                        ),
                      ]
                    )
                  ),
                pw.Expanded(
                  child: pw.Column(
                    crossAxisAlignment: pw.CrossAxisAlignment.end,
                    children: [
                      pw.Text('Subtotal Productos: \$${products.fold(0.0, (sum, item) => sum + (item.price ?? 0) * item.quantity).toStringAsFixed(2)}'),
                      if (event.additionalCosts > 0)
                        pw.Text('Ajustes/Costos Extra: \$${event.additionalCosts.toStringAsFixed(2)}'),
                      pw.Divider(),
                      pw.Text('TOTAL FINAL: \$${(products.fold(0.0, (sum, item) => sum + (item.price ?? 0) * item.quantity) + event.additionalCosts).toStringAsFixed(2)}', 
                        style: pw.TextStyle(fontSize: 16, fontWeight: pw.FontWeight.bold, color: PdfColors.pink)
                      ),
                    ]
                  )
                ),
              ]
            ),
            pw.SizedBox(height: 24),

            // Timeline & Tasks (Compact)
            pw.Row(
              crossAxisAlignment: pw.CrossAxisAlignment.start,
              children: [
                pw.Expanded(
                  child: pw.Column(
                    crossAxisAlignment: pw.CrossAxisAlignment.start,
                    children: [
                      pw.Text('CRONOGRAMA', style: pw.TextStyle(fontWeight: pw.FontWeight.bold, fontSize: 12, color: PdfColors.pink)),
                      pw.Divider(color: PdfColors.pink, thickness: 0.5),
                      _buildTimeline(timeline, timeFormat),
                    ]
                  ),
                ),
                pw.SizedBox(width: 20),
                pw.Expanded(
                  child: pw.Column(
                    crossAxisAlignment: pw.CrossAxisAlignment.start,
                    children: [
                      pw.Text('CONTROL DE TAREAS', style: pw.TextStyle(fontWeight: pw.FontWeight.bold, fontSize: 12, color: PdfColors.pink)),
                      pw.Divider(color: PdfColors.pink, thickness: 0.5),
                      _buildTasksList(tasks),
                    ]
                  ),
                ),
              ]
            ),
            
            pw.Padding(
              padding: const pw.EdgeInsets.only(top: 40),
              child: pw.Center(
                child: pw.Text('Gracias por confiar en Rosa Fiesta para su evento especial.', 
                  style: pw.TextStyle(fontStyle: pw.FontStyle.italic, color: PdfColors.grey700, fontSize: 10)
                ),
              )
            )
          ];
        },
      ),
    );

    await Printing.layoutPdf(
      onLayout: (PdfPageFormat format) async => pdf.save(),
      name: 'Factura_RosaFiesta_${event.name.replaceAll(' ', '_')}.pdf',
    );
  }

  static pw.Widget _buildMetricCard(String label, String value) {
    return pw.Container(
      padding: const pw.EdgeInsets.all(8),
      decoration: pw.BoxDecoration(
        border: pw.Border.all(color: PdfColors.grey300),
        borderRadius: const pw.BorderRadius.all(pw.Radius.circular(4)),
      ),
      child: pw.Column(
        children: [
          pw.Text(label, style: const pw.TextStyle(fontSize: 8, color: PdfColors.grey)),
          pw.Text(value, style: pw.TextStyle(fontSize: 12, fontWeight: pw.FontWeight.bold)),
        ],
      ),
    );
  }

  static pw.Widget _buildProductsTable(List<EventItem> products) {
    return pw.Table(
      border: pw.TableBorder.all(color: PdfColors.grey300, width: 0.5),
      children: [
        pw.TableRow(
          decoration: const pw.BoxDecoration(color: PdfColors.pink),
          children: [
            _buildTableHeader('Producto'),
            _buildTableHeader('Cant.'),
            _buildTableHeader('Precio Unit.'),
            _buildTableHeader('Total'),
          ],
        ),
        ...products.map((item) => pw.TableRow(
          children: [
            _buildTableCell(item.article?.nameTemplate ?? 'N/A'),
            _buildTableCell(item.quantity.toString()),
            _buildTableCell('\$${(item.price ?? 0).toStringAsFixed(2)}'),
            _buildTableCell('\$${((item.price ?? 0) * item.quantity).toStringAsFixed(2)}'),
          ],
        )),
      ],
    );
  }

  static pw.Widget _buildTableHeader(String text) {
    return pw.Padding(
      padding: const pw.EdgeInsets.all(5),
      child: pw.Text(text, style: pw.TextStyle(fontWeight: pw.FontWeight.bold, color: PdfColors.white, fontSize: 10)),
    );
  }

  static pw.Widget _buildTableCell(String text) {
    return pw.Padding(
      padding: const pw.EdgeInsets.all(5),
      child: pw.Text(text, style: const pw.TextStyle(fontSize: 9)),
    );
  }

  static pw.Widget _buildTimeline(List<TimelineItem> items, DateFormat timeFormat) {
    if (items.isEmpty) return pw.Text('No hay actividades.', style: const pw.TextStyle(fontSize: 10));
    return pw.Column(
      crossAxisAlignment: pw.CrossAxisAlignment.start,
      children: items.map((item) => pw.Padding(
        padding: const pw.EdgeInsets.symmetric(vertical: 2),
        child: pw.Text('${timeFormat.format(item.startTime)} - ${item.title}', style: const pw.TextStyle(fontSize: 9)),
      )).toList(),
    );
  }

  static pw.Widget _buildTasksList(List<EventTask> tasks) {
    if (tasks.isEmpty) return pw.Text('No hay tareas.', style: const pw.TextStyle(fontSize: 10));
    return pw.Column(
      crossAxisAlignment: pw.CrossAxisAlignment.start,
      children: tasks.map((t) => pw.Row(
        children: [
          pw.Container(
            width: 6,
            height: 6,
            decoration: pw.BoxDecoration(
              shape: pw.BoxShape.circle,
              color: t.isCompleted ? PdfColors.green : PdfColors.orange,
            ),
          ),
          pw.SizedBox(width: 4),
          pw.Text(t.title, style: const pw.TextStyle(fontSize: 9)),
        ],
      )).toList(),
    );
  }
}
