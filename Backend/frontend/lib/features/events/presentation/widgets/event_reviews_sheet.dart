import 'dart:io';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:image_picker/image_picker.dart';
import 'package:photo_view/photo_view.dart';
import 'package:photo_view/photo_view_gallery.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';
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
          heightFactor: 0.85,
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
  final List<XFile> _selectedPhotos = [];
  final _imagePicker = ImagePicker();

  @override
  void initState() {
    super.initState();
    _loadReviews();
  }

  @override
  void dispose() {
    _commentController.dispose();
    super.dispose();
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

  Future<void> _pickPhoto() async {
    if (_selectedPhotos.length >= 5) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Máximo 5 fotos por reseña')),
      );
      return;
    }
    final picked = await _imagePicker.pickImage(
      source: ImageSource.gallery,
      imageQuality: 80,
    );
    if (picked != null) {
      setState(() => _selectedPhotos.add(picked));
    }
  }

  void _removePhoto(int index) {
    setState(() => _selectedPhotos.removeAt(index));
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
      // For now, submit without actual file upload (client uploads to R2 first,
      // then passes URLs). This placeholder will be updated when R2 upload is wired.
      await context.read<EventsProvider>().createEventReview(
        widget.eventId,
        _rating,
        _commentController.text.trim(),
      );
      _commentController.clear();
      setState(() {
        _rating = 0;
        _isWriting = false;
        _selectedPhotos.clear();
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

  void _openLightbox(List<ReviewPhoto> photos, int initialIndex) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => _PhotoLightbox(photos: photos, initialIndex: initialIndex),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);
    return Padding(
      padding: const EdgeInsets.all(20.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'Reseñas del Evento',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  color: t.textPrimary,
                ),
              ),
              IconButton(
                icon: Icon(Icons.close, color: t.textPrimary),
                onPressed: () => Navigator.pop(context),
              ),
            ],
          ),
          Divider(color: t.borderFaint),
          if (_isLoading)
            Expanded(child: Center(child: CircularProgressIndicator(color: AppColors.hotPink)))
          else if (_isWriting)
            Expanded(child: _buildWriteReviewForm(t))
          else
            Expanded(
              child: _reviews.isEmpty
                  ? Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(Icons.reviews_outlined, size: 64, color: t.textMuted),
                          const SizedBox(height: 16),
                          Text(
                            'Aún no hay reseñas para este evento.',
                            style: TextStyle(color: t.textMuted),
                          ),
                          const SizedBox(height: 16),
                          RfLuxeButton(
                            label: 'Sé el primero en opinar',
                            onTap: () => setState(() => _isWriting = true),
                            filled: false,
                            t: t,
                          ),
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
                              return _ReviewCard(
                                review: review,
                                onPhotoTap: (photoIndex) => _openLightbox(review.photos, photoIndex),
                              );
                            },
                          ),
                        ),
                        const SizedBox(height: 12),
                        SizedBox(
                          width: double.infinity,
                          child: RfLuxeButton(
                            label: 'Escribir Reseña',
                            onTap: () => setState(() => _isWriting = true),
                            t: t,
                          ),
                        ),
                      ],
                    ),
            ),
        ],
      ),
    );
  }

  Widget _buildWriteReviewForm(RfTheme t) {
    return SingleChildScrollView(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('Tu calificación:', style: TextStyle(color: t.textPrimary, fontWeight: FontWeight.w600)),
          const SizedBox(height: 8),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: List.generate(5, (index) {
              return IconButton(
                icon: Icon(
                  index < _rating ? Icons.star : Icons.star_border,
                  color: AppColors.amber,
                  size: 36,
                ),
                onPressed: () => setState(() => _rating = index + 1),
              );
            }),
          ),
          const SizedBox(height: 20),
          Text('Tu experiencia:', style: TextStyle(color: t.textPrimary, fontWeight: FontWeight.w600)),
          const SizedBox(height: 8),
          TextField(
            controller: _commentController,
            maxLines: 4,
            style: GoogleFonts.dmSans(color: t.textPrimary),
            decoration: InputDecoration(
              hintText: 'Cuéntanos cómo estuvo tu experiencia...',
              hintStyle: GoogleFonts.dmSans(color: t.textMuted),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: BorderSide(color: t.borderFaint),
              ),
              enabledBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: BorderSide(color: t.borderFaint),
              ),
              focusedBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: BorderSide(color: AppColors.hotPink),
              ),
            ),
          ),
          const SizedBox(height: 20),
          Text('Fotos (opcional, máximo 5):', style: TextStyle(color: t.textPrimary, fontWeight: FontWeight.w600)),
          const SizedBox(height: 8),
          SizedBox(
            height: 100,
            child: ListView(
              scrollDirection: Axis.horizontal,
              children: [
                if (_selectedPhotos.length < 5)
                  _AddPhotoButton(onTap: _pickPhoto, t: t),
                ..._selectedPhotos.asMap().entries.map((entry) {
                  return _PhotoThumbnail(
                    image: File(entry.value.path),
                    onRemove: () => _removePhoto(entry.key),
                    t: t,
                  );
                }),
              ],
            ),
          ),
          const SizedBox(height: 24),
          Row(
            mainAxisAlignment: MainAxisAlignment.end,
            children: [
              TextButton(
                onPressed: () => setState(() {
                  _isWriting = false;
                  _selectedPhotos.clear();
                  _commentController.clear();
                  _rating = 0;
                }),
                child: Text('Cancelar', style: TextStyle(color: t.textMuted)),
              ),
              const SizedBox(width: 12),
              RfLuxeButton(
                label: 'Enviar Reseña',
                onTap: _submitReview,
                t: t,
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _ReviewCard extends StatelessWidget {
  final EventReview review;
  final void Function(int) onPhotoTap;

  const _ReviewCard({required this.review, required this.onPhotoTap});

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      color: t.card,
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                CircleAvatar(
                  backgroundColor: AppColors.hotPink.withValues(alpha: 0.2),
                  child: Text(
                    review.userName?.substring(0, 1).toUpperCase() ?? 'U',
                    style: TextStyle(color: AppColors.hotPink, fontWeight: FontWeight.bold),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        review.userName ?? 'Usuario Anónimo',
                        style: TextStyle(
                          fontWeight: FontWeight.w600,
                          color: t.textPrimary,
                        ),
                      ),
                      Row(
                        children: List.generate(
                          5,
                          (i) => Icon(
                            i < review.rating ? Icons.star : Icons.star_border,
                            size: 16,
                            color: AppColors.amber,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
                Text(
                  review.createdAt.toString().split(' ')[0],
                  style: TextStyle(fontSize: 11, color: t.textMuted),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Text(review.comment, style: TextStyle(color: t.textPrimary)),
            if (review.photos.isNotEmpty) ...[
              const SizedBox(height: 12),
              SizedBox(
                height: 80,
                child: ListView.builder(
                  scrollDirection: Axis.horizontal,
                  itemCount: review.photos.length,
                  itemBuilder: (context, index) {
                    final photo = review.photos[index];
                    return GestureDetector(
                      onTap: () => onPhotoTap(index),
                      child: Container(
                        width: 80,
                        height: 80,
                        margin: const EdgeInsets.only(right: 8),
                        decoration: BoxDecoration(
                          borderRadius: BorderRadius.circular(8),
                          image: DecorationImage(
                            image: NetworkImage(photo.photoUrl),
                            fit: BoxFit.cover,
                          ),
                        ),
                      ),
                    );
                  },
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

class _AddPhotoButton extends StatelessWidget {
  final VoidCallback onTap;
  final RfTheme t;

  const _AddPhotoButton({required this.onTap, required this.t});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 100,
        height: 100,
        margin: const EdgeInsets.only(right: 8),
        decoration: BoxDecoration(
          color: t.card,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: t.borderFaint),
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.add_photo_alternate, size: 32, color: AppColors.hotPink),
            const SizedBox(height: 4),
            Text('Añadir', style: TextStyle(fontSize: 12, color: t.textMuted)),
          ],
        ),
      ),
    );
  }
}

class _PhotoThumbnail extends StatelessWidget {
  final File image;
  final VoidCallback onRemove;
  final RfTheme t;

  const _PhotoThumbnail({required this.image, required this.onRemove, required this.t});

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: [
        Container(
          width: 100,
          height: 100,
          margin: const EdgeInsets.only(right: 8),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(12),
            image: DecorationImage(
              image: FileImage(image),
              fit: BoxFit.cover,
            ),
          ),
        ),
        Positioned(
          top: 4,
          right: 12,
          child: GestureDetector(
            onTap: onRemove,
            child: Container(
              padding: const EdgeInsets.all(2),
              decoration: BoxDecoration(
                color: Colors.black54,
                borderRadius: BorderRadius.circular(10),
              ),
              child: const Icon(Icons.close, size: 16, color: Colors.white),
            ),
          ),
        ),
      ],
    );
  }
}

class _PhotoLightbox extends StatefulWidget {
  final List<ReviewPhoto> photos;
  final int initialIndex;

  const _PhotoLightbox({required this.photos, required this.initialIndex});

  @override
  State<_PhotoLightbox> createState() => _PhotoLightboxState();
}

class _PhotoLightboxState extends State<_PhotoLightbox> {
  late PageController _pageController;
  late int _currentIndex;

  @override
  void initState() {
    super.initState();
    _currentIndex = widget.initialIndex;
    _pageController = PageController(initialPage: widget.initialIndex);
  }

  @override
  void dispose() {
    _pageController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        iconTheme: const IconThemeData(color: Colors.white),
        title: Text(
          '${_currentIndex + 1} / ${widget.photos.length}',
          style: const TextStyle(color: Colors.white, fontSize: 16),
        ),
      ),
      body: PhotoViewGallery.builder(
        pageController: _pageController,
        itemCount: widget.photos.length,
        builder: (context, index) {
          final photo = widget.photos[index];
          return PhotoViewGalleryPageOptions(
            imageProvider: NetworkImage(photo.photoUrl),
            minScale: PhotoViewComputedScale.contained,
            maxScale: PhotoViewComputedScale.covered * 2,
          );
        },
        onPageChanged: (index) => setState(() => _currentIndex = index),
        backgroundDecoration: const BoxDecoration(color: Colors.black),
      ),
    );
  }
}