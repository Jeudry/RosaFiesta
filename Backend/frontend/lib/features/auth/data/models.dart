class User {
  final String id;
  final String email;
  final String? name;
  final String? role;

  User({
    required this.id,
    required this.email,
    this.name,
    this.role,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'],
      email: json['email'],
      name: json['name'],
      role: json['role'],
    );
  }
}

class PendingEvent {
  final String id;
  final String name;
  final String? date;
  final String status;
  final String paymentStatus;

  PendingEvent({
    required this.id,
    required this.name,
    this.date,
    required this.status,
    required this.paymentStatus,
  });

  factory PendingEvent.fromJson(Map<String, dynamic> json) {
    return PendingEvent(
      id: json['id'],
      name: json['name'] ?? '',
      date: json['date'],
      status: json['status'] ?? '',
      paymentStatus: json['paymentStatus'] ?? '',
    );
  }

  bool get isApproved => status == 'confirmed' || status == 'paid';
}

class AuthResponse {
  final String accessToken;
  final String userId;
  final int accessTokenExpirationTimestamp;
  final String refreshToken;
  final List<PendingEvent> pendingEvents;

  AuthResponse({
    required this.accessToken,
    required this.userId,
    required this.accessTokenExpirationTimestamp,
    required this.refreshToken,
    this.pendingEvents = const [],
  });

  factory AuthResponse.fromJson(Map<String, dynamic> json) {
    final eventsList = (json['pendingEvents'] as List?)
            ?.map((e) => PendingEvent.fromJson(e))
            .toList() ??
        [];
    return AuthResponse(
      accessToken: json['accessToken'],
      userId: json['userId'],
      accessTokenExpirationTimestamp: json['accessTokenExpirationTimestamp'],
      refreshToken: json['refreshToken'],
      pendingEvents: eventsList,
    );
  }
}
