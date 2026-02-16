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

class AuthResponse {
  final String accessToken;
  final String userId;
  final int accessTokenExpirationTimestamp;
  final String refreshToken;

  AuthResponse({
    required this.accessToken,
    required this.userId,
    required this.accessTokenExpirationTimestamp,
    required this.refreshToken,
  });

  factory AuthResponse.fromJson(Map<String, dynamic> json) {
    return AuthResponse(
      accessToken: json['accessToken'],
      userId: json['userId'],
      accessTokenExpirationTimestamp: json['accessTokenExpirationTimestamp'],
      refreshToken: json['refreshToken'],
    );
  }
}
