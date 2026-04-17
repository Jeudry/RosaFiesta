import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:frontend/core/app_colors.dart';
import 'package:frontend/core/design_system.dart';
import 'package:frontend/core/api_client.dart';

class VerifiedReviewsScreen extends StatefulWidget {
  const VerifiedReviewsScreen({super.key});

  @override
  State<VerifiedReviewsScreen> createState() => _VerifiedReviewsScreenState();
}

class _VerifiedReviewsScreenState extends State<VerifiedReviewsScreen> {
  List<dynamic> _reviews = [];
  Map<String, dynamic>? _stats;
  bool _isLoading = true;
  String _filterStatus = 'pending';

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    setState(() => _isLoading = true);
    try {
      final statsData = await ApiClient.get('/reviews/stats');
      setState(() => _stats = statsData);
      await _loadReviews();
    } catch (e) {
      debugPrint('Error loading data: $e');
    } finally {
      setState(() => _isLoading = false);
    }
  }

  Future<void> _loadReviews() async {
    try {
      String path = '/reviews?limit=50&offset=0';
      if (_filterStatus.isNotEmpty) path += '&verified=$_filterStatus';
      final data = await ApiClient.get(path);
      setState(() => _reviews = data as List<dynamic>? ?? []);
    } catch (e) {
      debugPrint('Error loading reviews: $e');
    }
  }

  Future<void> _verifyReview(int id) async {
    await ApiClient.put('/reviews/$id/verify', {'is_verified': true});
    _loadData();
  }

  Future<void> _rejectReview(int id) async {
    await ApiClient.put('/reviews/$id/verify', {
      'is_verified': false,
      'rejection_reason': 'Policy violation',
    });
    _loadData();
  }

  @override
  Widget build(BuildContext context) {
    final t = RfTheme.of(context);

    return Scaffold(
      backgroundColor: t.base,
      appBar: AppBar(
        backgroundColor: t.card,
        title: ShaderMask(
          shaderCallback: (bounds) =>
              AppColors.titleGradient.createShader(bounds),
          child: Text(
            'Verified Reviews',
            style: GoogleFonts.outfit(
              color: Colors.white,
              fontSize: 20,
              fontWeight: FontWeight.w700,
            ),
          ),
        ),
        leading: IconButton(
          icon: Icon(Icons.arrow_back, color: t.textPrimary),
          onPressed: () => Navigator.pop(context),
        ),
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : Column(
              children: [
                if (_stats != null)
                  Container(
                    padding: const EdgeInsets.all(16),
                    color: t.card,
                    child: Row(
                      children: [
                        _statCard(
                          'Total',
                          _stats!['total']?.toString() ?? '0',
                          AppColors.sky,
                          t,
                        ),
                        _statCard(
                          'Verified',
                          _stats!['verified']?.toString() ?? '0',
                          AppColors.teal,
                          t,
                        ),
                        _statCard(
                          'Pending',
                          _stats!['pending']?.toString() ?? '0',
                          AppColors.amber,
                          t,
                        ),
                        _statCard(
                          'Rejected',
                          _stats!['rejected']?.toString() ?? '0',
                          AppColors.coral,
                          t,
                        ),
                      ],
                    ),
                  ),
                Container(
                  padding: const EdgeInsets.all(12),
                  color: t.card.withValues(alpha: 0.5),
                  child: Row(
                    children: [
                      _filterChip('Pending', 'pending', t),
                      const SizedBox(width: 8),
                      _filterChip('Verified', 'verified', t),
                      const SizedBox(width: 8),
                      _filterChip('Rejected', 'rejected', t),
                      const SizedBox(width: 8),
                      _filterChip('All', '', t),
                    ],
                  ),
                ),
                Expanded(
                  child: ListView.builder(
                    padding: const EdgeInsets.all(16),
                    itemCount: _reviews.length,
                    itemBuilder: (context, index) =>
                        _buildReviewCard(_reviews[index], t),
                  ),
                ),
              ],
            ),
    );
  }

  Widget _statCard(String label, String value, Color color, RfTheme t) {
    return Expanded(
      child: Container(
        margin: const EdgeInsets.symmetric(horizontal: 4),
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: color.withValues(alpha: 0.1),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Column(
          children: [
            Text(
              value,
              style: GoogleFonts.outfit(
                color: color,
                fontSize: 24,
                fontWeight: FontWeight.w700,
              ),
            ),
            Text(label, style: TextStyle(color: t.textMuted, fontSize: 12)),
          ],
        ),
      ),
    );
  }

  Widget _filterChip(String label, String value, RfTheme t) {
    final isSelected = _filterStatus == value;
    return GestureDetector(
      onTap: () {
        setState(() => _filterStatus = value);
        _loadReviews();
      },
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        decoration: BoxDecoration(
          color: isSelected ? AppColors.hotPink : t.card,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(
            color: isSelected ? AppColors.hotPink : t.borderFaint,
          ),
        ),
        child: Text(
          label,
          style: TextStyle(
            color: isSelected ? Colors.white : t.textMuted,
            fontWeight: FontWeight.w600,
            fontSize: 13,
          ),
        ),
      ),
    );
  }

  Widget _buildReviewCard(Map<String, dynamic> review, RfTheme t) {
    final isVerified = review['is_verified'] == true;
    final isRejected =
        review['rejection_reason'] != null &&
        review['rejection_reason'].toString().isNotEmpty;

    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: t.card,
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              CircleAvatar(
                radius: 22,
                backgroundColor: AppColors.violet.withValues(alpha: 0.2),
                child: Text(
                  (review['client_name'] ?? 'C')[0].toString().toUpperCase(),
                  style: GoogleFonts.outfit(
                    color: AppColors.violet,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      review['client_name'] ?? 'Client',
                      style: GoogleFonts.outfit(
                        color: t.textPrimary,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    Text(
                      review['event_name'] ?? '',
                      style: TextStyle(color: t.textMuted, fontSize: 12),
                    ),
                  ],
                ),
              ),
              Row(
                children: List.generate(
                  5,
                  (i) => Icon(
                    i < (review['rating'] ?? 0)
                        ? Icons.star
                        : Icons.star_border,
                    color: AppColors.amber,
                    size: 18,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Text(
            review['comment'] ?? '',
            style: GoogleFonts.dmSans(color: t.textMuted, fontSize: 14),
          ),
          if (review['admin_response'] != null &&
              review['admin_response'].toString().isNotEmpty) ...[
            const SizedBox(height: 12),
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: AppColors.teal.withValues(alpha: 0.1),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Row(
                children: [
                  Icon(Icons.reply, color: AppColors.teal, size: 16),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      review['admin_response'].toString(),
                      style: TextStyle(color: AppColors.teal, fontSize: 13),
                    ),
                  ),
                ],
              ),
            ),
          ],
          const SizedBox(height: 12),
          Row(
            children: [
              if (!isVerified && !isRejected) ...[
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: () => _verifyReview(review['id']),
                    icon: const Icon(Icons.verified, size: 18),
                    label: const Text('Verify'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: AppColors.teal,
                      side: BorderSide(color: AppColors.teal),
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: () => _rejectReview(review['id']),
                    icon: const Icon(Icons.cancel, size: 18),
                    label: const Text('Reject'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: AppColors.coral,
                      side: BorderSide(color: AppColors.coral),
                    ),
                  ),
                ),
              ] else ...[
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 12,
                    vertical: 6,
                  ),
                  decoration: BoxDecoration(
                    color: isVerified
                        ? Colors.green.withValues(alpha: 0.1)
                        : Colors.red.withValues(alpha: 0.1),
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(
                        isVerified ? Icons.verified : Icons.cancel,
                        color: isVerified ? Colors.green : Colors.red,
                        size: 16,
                      ),
                      const SizedBox(width: 4),
                      Text(
                        isVerified ? 'Verified' : 'Rejected',
                        style: TextStyle(
                          color: isVerified ? Colors.green : Colors.red,
                          fontSize: 12,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
              const Spacer(),
              Text(
                _formatDate(review['created_at']),
                style: TextStyle(color: t.textDim, fontSize: 11),
              ),
            ],
          ),
          if (isVerified) ...[
            const SizedBox(height: 8),
            Row(
              children: [
                Icon(Icons.verified_user, color: AppColors.sky, size: 14),
                const SizedBox(width: 4),
                Text(
                  'Verified Purchase',
                  style: TextStyle(color: AppColors.sky, fontSize: 11),
                ),
                if (review['verification_date'] != null) ...[
                  const SizedBox(width: 8),
                  Text(
                    'on ${_formatDate(review['verification_date'])}',
                    style: TextStyle(color: t.textDim, fontSize: 11),
                  ),
                ],
              ],
            ),
          ],
        ],
      ),
    );
  }

  String _formatDate(String? dateStr) {
    if (dateStr == null) return '';
    try {
      final date = DateTime.parse(dateStr);
      return '${date.day}/${date.month}/${date.year}';
    } catch (_) {
      return dateStr;
    }
  }
}
