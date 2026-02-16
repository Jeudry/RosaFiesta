class UserProfile {
  final String id;
  final String userName;
  final String firstName;
  final String lastName;
  final String email;
  final String phoneNumber;
  final String? avatar;
  final String? bornDate;

  UserProfile({
    required this.id,
    required this.userName,
    required this.firstName,
    required this.lastName,
    required this.email,
    required this.phoneNumber,
    this.avatar,
    this.bornDate,
  });

  factory UserProfile.fromJson(Map<String, dynamic> json) {
    return UserProfile(
      id: json['id'],
      userName: json['userName'] ?? '',
      firstName: json['firstName'] ?? '',
      lastName: json['lastName'] ?? '',
      email: json['email'] ?? '',
      phoneNumber: json['phone_number'] ?? '',
      avatar: json['avatar'],
      bornDate: json['born_date'],
    );
  }

  String get fullName => '$firstName $lastName'.trim().isEmpty ? userName : '$firstName $lastName';
}
