import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../events_provider.dart';
import '../../data/event_review.dart';

class EventReviewsSheet extends StatefulWidget {
  final String eventId;

  const EventReviewsSheet({super.key, required this.eventId});

  static void show(BuildContext context, String eventId) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => Padding(
        padding: EdgeInsets.only(
          bottom: MediaQuery.of(context).viewInsets.bottom,
        ),
        child: FractionallySizedBox(
          heightFactor: 0.8,
          child: EventReviewsSheet(eventId: eventId),
        ),
      ),
    );
  }

  @override
  State<EventReviewsSheet> createState() => _EventReviewsSheetState();
}

class _EventReviewsSheetState extends State<EventReviewsSheet> {
  bool _isLoading = true;
  List<EventReview> _reviews = [];
  bool _isWriting = false;

  final _commentController = TextEditingController();
  int _rating = 0;

  @override
  void initState() {
    super.initState();
    _loadReviews();
  }

  Future<void> _loadReviews() async {
    setState(() => _isLoading = true);
    try {
      final reviews = await context.read<EventsProvider>().getEventReviews(widget.eventId);
      setState(() {
        _reviews = reviews;
        _isLoading = false;
      });
    } catch (e) {
      if (mounted) {
        setState(() => _isLoading = false);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error al cargar reseñas: $e')),
        );
      }
    }
  }

  Future<void> _submitReview() async {
    if (_rating == 0) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Por favor, selecciona una calificación (estrellas)')),
      );
      return;
    }
    if (_commentController.text.trim().isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Por favor, escribe un comentario')),
      );
      return;
    }

    setState(() => _isLoading = true);
    try {
      await context.read<EventsProvider>().createEventReview(
        widget.eventId,
        _rating,
        _commentController.text.trim(),
      );
      _commentController.clear();
      setState(() {
        _rating = 0;
        _isWriting = false;
      });
      await _loadReviews();
    } catch (e) {
      if (mounted) {
        setState(() => _isLoading = false);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error al enviar reseña: $e\nAsegúrate de que el evento esté finalizado y no hayas dejado una reseña antes.')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(20.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'Reseñas del Evento',
                style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
              ),
              IconButton(
                icon: const Icon(Icons.close),
                onPressed: () => Navigator.pop(context),
              ),
            ],
          ),
          const Divider(),
          if (_isLoading)
            const Expanded(child: Center(child: CircularProgressIndicator()))
          else if (_isWriting)
            Expanded(child: _buildWriteReviewForm())
          else
            Expanded(
              child: _reviews.isEmpty
                  ? Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          const Icon(Icons.reviews_outlined, size: 64, color: Colors.grey),
                          const SizedBox(height: 16),
                          const Text('Aún no hay reseñas para este evento.'),
                          const SizedBox(height: 16),
                          ElevatedButton(
                            onPressed: () => setState(() => _isWriting = true),
                            child: const Text('Sé el primero en opinar'),
                          )
                        ],
                      ),
                    )
                  : Column(
                      children: [
                        Expanded(
                          child: ListView.builder(
                            itemCount: _reviews.length,
                            itemBuilder: (context, index) {
                              final review = _reviews[index];
                              return Card(
                                margin: const EdgeInsets.only(bottom: 12),
                                child: ListTile(
                                  leading: CircleAvatar(
                                    child: Text(review.userName?.substring(0, 1).toUpperCase() ?? 'U'),
                                  ),
                                  title: Row(
                                    children: [
                                      Text(review.userName ?? 'Usuario Anonimo'),
                                      const Spacer(),
                                      Row(
                                        children: List.generate(
                                          5,
                                          (i) => Icon(
                                            i < review.rating ? Icons.star : Icons.star_border,
                                            size: 16,
                                            color: Colors.amber,
                                          ),
                                        ),
                                      ),
                                    ],
                                  ),
                                  subtitle: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      const SizedBox(height: 4),
                                      Text(review.comment),
                                      const SizedBox(height: 4),
                                      Text(
                                        review.createdAt.toString().split(' ')[0],
                                        style: const TextStyle(fontSize: 10, color: Colors.grey),
                                      ),
                                    ],
                                  ),
                                ),
                              );
                            },
                          ),
                        ),
                        const SizedBox(height: 12),
                        SizedBox(
                          width: double.infinity,
                          child: ElevatedButton.icon(
                            onPressed: () => setState(() => _isWriting = true),
                            icon: const Icon(Icons.edit),
                            label: const Text('Escribir Reseña'),
                          ),
                        ),
                      ],
                    ),
            ),
        ],
      ),
    );
  }

  Widget _buildWriteReviewForm() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text('Tu calificación:'),
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: List.generate(5, (index) {
            return IconButton(
              icon: Icon(
                index < _rating ? Icons.star : Icons.star_border,
                color: Colors.amber,
                size: 36,
              ),
              onPressed: () => setState(() => _rating = index + 1),
            );
          }),
        ),
        const SizedBox(height: 16),
        TextField(
          controller: _commentController,
          maxLines: 4,
          decoration: const InputDecoration(
            hintText: 'Cuéntanos cómo estuvo tu experiencia...',
            border: OutlineInputBorder(),
          ),
        ),
        const SizedBox(height: 16),
        Row(
          mainAxisAlignment: MainAxisAlignment.end,
          children: [
            TextButton(
              onPressed: () => setState(() => _isWriting = false),
              child: const Text('Cancelar'),
            ),
            const SizedBox(width: 8),
            ElevatedButton(
              onPressed: _submitReview,
              child: const Text('Enviar Reseña'),
            ),
          ],
        )
      ],
    );
  }
}
