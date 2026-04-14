import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/app_colors.dart';
import '../company_reviews_provider.dart';

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
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<CompanyReviewsProvider>().fetchAll();
    });
  }

  @override
  void dispose() {
    _gradientCtrl.dispose();
    _commentController.dispose();
    super.dispose();
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
                  child: Consumer<CompanyReviewsProvider>(
                    builder: (_, provider, __) {
                      if (provider.isLoading && provider.reviews.isEmpty) {
                        return const Center(child: CircularProgressIndicator());
                      }
                      if (provider.reviews.isEmpty) {
                        return Center(
                          child: Text('Sin reseñas aún', style: GoogleFonts.dmSans(color: t.textMuted)),
                        );
                      }
                      return ListView.builder(
                        padding: const EdgeInsets.symmetric(horizontal: 20),
                        itemCount: provider.reviews.length,
                        itemBuilder: (_, i) => _reviewCard(provider.reviews[i], t),
                      );
                    },
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
    return Consumer<CompanyReviewsProvider>(
      builder: (_, provider, __) {
        final avg = provider.averageRating;
        final count = provider.reviewCount;
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
                    avg > 0 ? avg.toStringAsFixed(1) : '--',
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
                      color: avg > 0
                          ? (i < avg.floor()
                              ? starColor
                              : (i < avg.ceil() ? starColor.withOpacity(0.5) : starColor.withOpacity(0.2)))
                          : starColor.withOpacity(0.2),
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
      },
    );
  }

  Widget _reviewCard(CompanyReview review, RfTheme t) {
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
      builder: (sheetCtx) => StatefulBuilder(
        builder: (sheetCtx, setSheetState) => Padding(
          padding: EdgeInsets.fromLTRB(
              24, 24, 24, MediaQuery.of(sheetCtx).viewInsets.bottom + 24),
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
                  onPressed: () => setSheetState(() => _selectedRating = i + 1),
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
                  setSheetState(() => _isSubmitting = true);
                  final success = await context.read<CompanyReviewsProvider>().addReview(
                    _selectedRating,
                    _commentController.text,
                  );
                  if (sheetCtx.mounted) {
                    Navigator.pop(sheetCtx);
                    if (success) {
                      _commentController.clear();
                      setSheetState(() {
                        _selectedRating = 5;
                        _isSubmitting = false;
                      });
                    } else {
                      setSheetState(() => _isSubmitting = false);
                    }
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