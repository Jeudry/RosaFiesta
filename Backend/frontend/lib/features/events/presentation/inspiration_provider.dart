import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:dio/dio.dart';
import 'package:frontend/core/config/env_config.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class InspirationPhoto {
  final String id;
  final String eventId;
  final String photoUrl;
  final String? caption;
  final DateTime uploadedAt;

  InspirationPhoto({
    required this.id,
    required this.eventId,
    required this.photoUrl,
    this.caption,
    required this.uploadedAt,
  });

  factory InspirationPhoto.fromJson(Map<String, dynamic> json) {
    return InspirationPhoto(
      id: json['id'] ?? '',
      eventId: json['event_id'] ?? '',
      photoUrl: json['photo_url'] ?? '',
      caption: json['caption'],
      uploadedAt: json['uploaded_at'] != null
          ? DateTime.parse(json['uploaded_at'])
          : DateTime.now(),
    );
  }
}

class InspirationProvider extends ChangeNotifier {
  List<InspirationPhoto> _photos = [];
  bool _isLoading = false;
  String? _error;

  List<InspirationPhoto> get photos => _photos;
  bool get isLoading => _isLoading;
  String? get error => _error;

  static const _storage = FlutterSecureStorage();

  Future<void> fetchInspiration(String eventId) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final token = await _storage.read(key: 'access_token');
      final response = await Dio().get(
        '${EnvConfig.apiUrl}/events/$eventId/inspiration',
        options: Options(
          headers: {'Authorization': 'Bearer $token'},
        ),
      );

      final data = response.data;
      if (data is List) {
        _photos = data.map((json) => InspirationPhoto.fromJson(json)).toList();
      }
    } catch (e) {
      _error = 'Failed to load inspiration photos';
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> uploadInspiration(BuildContext context, String eventId, File imageFile, {String? caption}) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final token = await _storage.read(key: 'access_token');
      final formData = FormData.fromMap({
        'file': await MultipartFile.fromFile(
          imageFile.path,
          filename: imageFile.path.split('/').last,
        ),
        if (caption != null && caption.isNotEmpty) 'caption': caption,
      });

      final response = await Dio().post(
        '${EnvConfig.apiUrl}/events/$eventId/inspiration',
        data: formData,
        options: Options(
          headers: {
            'Authorization': 'Bearer $token',
            'Content-Type': 'multipart/form-data',
          },
        ),
      );

      if (response.statusCode == 200) {
        final data = response.data['data'];
        if (data != null) {
          _photos.insert(0, InspirationPhoto.fromJson(data));
        }
        _isLoading = false;
        notifyListeners();
        return true;
      }
    } catch (e) {
      _error = 'Failed to upload inspiration photo';
    } finally {
      _isLoading = false;
      notifyListeners();
    }
    return false;
  }

  Future<bool> deleteInspiration(String eventId, String photoId) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final token = await _storage.read(key: 'access_token');
      final response = await Dio().delete(
        '${EnvConfig.apiUrl}/events/$eventId/inspiration/$photoId',
        options: Options(
          headers: {'Authorization': 'Bearer $token'},
        ),
      );

      if (response.statusCode == 204) {
        _photos.removeWhere((p) => p.id == photoId);
        _isLoading = false;
        notifyListeners();
        return true;
      }
    } catch (e) {
      _error = 'Failed to delete inspiration photo';
    } finally {
      _isLoading = false;
      notifyListeners();
    }
    return false;
  }

  Future<File?> pickImage(BuildContext context) async {
    final picker = ImagePicker();
    final source = await showModalBottomSheet<ImageSource>(
      context: context,
      builder: (ctx) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              leading: const Icon(Icons.photo_camera),
              title: const Text('Cámara'),
              onTap: () => Navigator.pop(ctx, ImageSource.camera),
            ),
            ListTile(
              leading: const Icon(Icons.photo_library),
              title: const Text('Galería'),
              onTap: () => Navigator.pop(ctx, ImageSource.gallery),
            ),
          ],
        ),
      ),
    );

    if (source == null) return null;

    final pickedFile = await picker.pickImage(
      source: source,
      maxWidth: 1920,
      maxHeight: 1920,
      imageQuality: 85,
    );

    if (pickedFile != null) {
      return File(pickedFile.path);
    }
    return null;
  }

  void clearError() {
    _error = null;
    notifyListeners();
  }
}