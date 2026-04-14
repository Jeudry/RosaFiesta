import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';

class CompanyReviewsScreen extends StatefulWidget {
  const CompanyReviewsScreen({super.key});

  static Future<void> open(BuildContext context) {
    return Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const CompanyReviewsScreen()),
    );
  }

  @override
  State<CompanyReviewsScreen> createState() => _CompanyReviewsScreenState();
}

class _CompanyReviewsScreenState extends State<CompanyReviewsScreen>
    with TickerProviderStateMixin {
  int _selectedRating = 5;
  final _commentController = TextEditingController();
  bool _isSubmitting = false;
  late final AnimationController _gradientCtrl;

  @override
  void initState() {
    super.initState();
    _gradientCtrl = AnimationController(
        vsync: this, duration: const Duration(seconds: 12))
      ..repeat();
  }

  @override
  void dispose() {
    _gradientCtrl.dispose();
    _commentController.dispose();
    super.dispose();
  }

  // Placeholder — real data wired to backend via ReviewsApiService
  final List<_FakeReview> _reviews = [
    _FakeReview(userName: 'María González', rating: 5, comment: '¡Servicio excepcional! Todo llegó perfecto para mi boda.', daysAgo: 3),
    _FakeReview(userName: 'Carlos Pérez', rating: 5, comment: 'La atención es de primera. Muy profesionales.', daysAgo: 7),
    _FakeReview(userName: 'Laura Martínez', rating: 4, comment: 'Muy buena experiencia, los建议 decorations eran hermosos.', daysAgo: 12),
    _FakeReview(userName: 'Juan Rodríguez', rating: 5, comment: 'Rápidos y confiables. Los recomiendo ampliamente.', daysAgo: 20),
    _FakeReview(userName: 'Ana Sánchez', rating: 4, comment: 'Buen trabajo en general. El equipo fue muy amable.', daysAgo: 30),
  ];

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);
    return Scaffold(
      backgroundColor: t.base,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: Icon(Icons.arrow_back_ios_rounded, color: t.textPrimary),
          onPressed: () => Navigator.pop(context),
        ),
        title: Text('Reseñas de RosaFiesta',
            style: GoogleFonts.outfit(
                fontSize: 20, fontWeight: FontWeight.w800, color: t.textPrimary)),
        centerTitle: true,
      ),
      body: Stack(
        children: [
          RfGradientOrbs(
            controller: _gradientCtrl,
            color1: AppColors.hotPink,
            color2: AppColors.violet,
            isDark: t.isDark,
          ),
          Positioned.fill(
            child: IgnorePointer(
              child: CustomPaint(
                painter: RfGridPainter(
                    color: (t.isDark ? Colors.white : Colors.black)
                        .withOpacity(0.006)),
              ),
            ),
          ),
          SafeArea(
            child: Column(
              children: [
                _buildSummaryCard(t),
                const SizedBox(height: 16),
                Expanded(
                  child: ListView.builder(
                    padding: const EdgeInsets.symmetric(horizontal: 20),
                    itemCount: _reviews.length,
                    itemBuilder: (_, i) => _reviewCard(_reviews[i], t),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
      floatingActionButton: _buildWriteFab(t),
    );
  }

  Widget _buildSummaryCard(RfTheme t) {
    const starColor = Color(0xFFFFB800);
    final avg = 4.7;
    final count = _reviews.length;
    return Container(
      margin: const EdgeInsets.fromLTRB(20, 8, 20, 0),
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: t.isDark ? t.card : Colors.white,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: t.borderFaint),
        boxShadow: [
          BoxShadow(
            color: AppColors.hotPink.withOpacity(0.08),
            blurRadius: 24,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Row(
        children: [
          Container(
            width: 72, height: 72,
            decoration: BoxDecoration(
              gradient: const LinearGradient(
                  colors: [AppColors.violet, AppColors.hotPink]),
              borderRadius: BorderRadius.circular(20),
            ),
            child: Center(
              child: Text(
                avg.toStringAsFixed(1),
                style: GoogleFonts.outfit(
                  fontSize: 26, fontWeight: FontWeight.w900, color: Colors.white,
                ),
              ),
            ),
          ),
          const SizedBox(width: 20),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(children: List.generate(5, (i) => Icon(
                  Icons.star_rounded,
                  color: i < avg.floor()
                      ? starColor
                      : (i < avg.ceil() ? starColor.withOpacity(0.5) : starColor.withOpacity(0.2)),
                  size: 22,
                ))),
                const SizedBox(height: 6),
                Text(
                  '$count reseñas verificadas',
                  style: GoogleFonts.dmSans(
                      fontSize: 13, fontWeight: FontWeight.w600, color: t.textMuted)),
                const SizedBox(height: 2),
                Text(
                  'Conoce la experiencia de otros clientes',
                  style: GoogleFonts.dmSans(fontSize: 12, color: t.textDim)),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _reviewCard(_FakeReview review, RfTheme t) {
    const starColor = Color(0xFFFFB800);
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: t.isDark ? t.card.withOpacity(0.7) : Colors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: t.borderFaint),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(children: [
                Container(
                  width: 38, height: 38,
                  decoration: const BoxDecoration(
                    gradient: LinearGradient(
                        colors: [AppColors.violet, AppColors.hotPink]),
                    shape: BoxShape.circle,
                  ),
                  child: Center(
                    child: Text(
                      review.userName[0].toUpperCase(),
                      style: GoogleFonts.outfit(
                          fontSize: 16, fontWeight: FontWeight.w800,
                          color: Colors.white),
                    ),
                  ),
                ),
                const SizedBox(width: 10),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(review.userName,
                        style: GoogleFonts.dmSans(
                            fontSize: 14, fontWeight: FontWeight.w700,
                            color: t.textPrimary)),
                    Row(children: List.generate(5, (i) => Icon(
                      Icons.star_rounded,
                      color: i < review.rating ? starColor : starColor.withOpacity(0.2),
                      size: 14,
                    ))),
                  ],
                ),
              ]),
              Text('Hace ${review.daysAgo}d',
                  style: GoogleFonts.dmSans(fontSize: 11, color: t.textDim)),
            ],
          ),
          const SizedBox(height: 12),
          Text(review.comment,
              style: GoogleFonts.dmSans(
                  fontSize: 13, color: t.textMuted, height: 1.5)),
        ],
      ),
    );
  }

  Widget _buildWriteFab(RfTheme t) {
    return GestureDetector(
      onTap: () => _showWriteDialog(context, t),
      child: Container(
        width: 56, height: 56,
        decoration: BoxDecoration(
          gradient: const LinearGradient(
              colors: [AppColors.violet, AppColors.hotPink]),
          borderRadius: BorderRadius.circular(18),
          boxShadow: [
            BoxShadow(
              color: AppColors.hotPink.withOpacity(0.4),
              blurRadius: 16,
              offset: const Offset(0, 6),
            ),
          ],
        ),
        child: const Icon(Icons.edit_rounded, color: Colors.white, size: 24),
      ),
    );
  }

  void _showWriteDialog(BuildContext ctx, RfTheme t) {
    showModalBottomSheet(
      context: ctx,
      backgroundColor: t.card,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.vertical(top: Radius.circular(24))),
      builder: (_) => StatefulBuilder(
        builder: (ctx, setState) => Padding(
          padding: EdgeInsets.fromLTRB(
              24, 24, 24, MediaQuery.of(ctx).viewInsets.bottom + 24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                width: 40, height: 4,
                decoration: BoxDecoration(
                  color: t.textDim.withOpacity(0.3),
                  borderRadius: BorderRadius.circular(2)),
              ),
              const SizedBox(height: 20),
              Text('Escribe tu reseña',
                  style: GoogleFonts.outfit(
                      fontSize: 20, fontWeight: FontWeight.w800,
                      color: t.textPrimary)),
              const SizedBox(height: 6),
              Text('Cuéntanos tu experiencia con RosaFiesta',
                  style: GoogleFonts.dmSans(fontSize: 13, color: t.textDim)),
              const SizedBox(height: 24),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: List.generate(5, (i) => IconButton(
                  icon: Icon(Icons.star_rounded,
                      color: i < _selectedRating
                          ? const Color(0xFFFFB800)
                          : const Color(0xFFFFB800).withOpacity(0.2),
                      size: 36),
                  onPressed: () => setState(() => _selectedRating = i + 1),
                )),
              ),
              const SizedBox(height: 16),
              Container(
                decoration: BoxDecoration(
                  color: t.isDark
                      ? Colors.white.withOpacity(0.04)
                      : const Color(0xFFF3F4F6),
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: t.borderFaint),
                ),
                child: TextField(
                  controller: _commentController,
                  maxLines: 4,
                  style: GoogleFonts.dmSans(color: t.textPrimary, fontSize: 14),
                  decoration: InputDecoration(
                    hintText: 'Tu experiencia...',
                    hintStyle: GoogleFonts.dmSans(color: t.textDim, fontSize: 14),
                    border: InputBorder.none,
                    contentPadding: const EdgeInsets.all(16),
                  ),
                ),
              ),
              const SizedBox(height: 20),
              RfLuxeButton(
                label: _isSubmitting ? 'Enviando...' : 'Publicar reseña',
                loading: _isSubmitting,
                onTap: () async {
                  if (_commentController.text.trim().isEmpty) return;
                  setState(() => _isSubmitting = true);
                  await Future.delayed(const Duration(seconds: 1));
                  if (ctx.mounted) {
                    Navigator.pop(ctx);
                    setState(() {
                      _reviews.insert(0, _FakeReview(
                        userName: 'Tú',
                        rating: _selectedRating,
                        comment: _commentController.text,
                        daysAgo: 0,
                      ));
                      _commentController.clear();
                      _selectedRating = 5;
                      _isSubmitting = false;
                    });
                  }
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _FakeReview {
  final String userName;
  final int rating;
  final String comment;
  final int daysAgo;
  _FakeReview({
    required this.userName,
    required this.rating,
    required this.comment,
    required this.daysAgo,
  });
}
