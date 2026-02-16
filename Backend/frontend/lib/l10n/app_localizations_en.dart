// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for English (`en`).
class AppLocalizationsEn extends AppLocalizations {
  AppLocalizationsEn([String locale = 'en']) : super(locale);

  @override
  String get appTitle => 'RosaFiesta';

  @override
  String get welcomeHeadline => 'RosaFiesta';

  @override
  String get welcomeSubheadline => 'Decoration & Events with AI';

  @override
  String get beginButton => 'GET STARTED';

  @override
  String get loginButton => 'LOGIN';

  @override
  String get createAccountButton => 'Create account';

  @override
  String get loginHeadline => 'Welcome back';

  @override
  String get emailLabel => 'Email address';

  @override
  String get passwordLabel => 'Password';

  @override
  String get emailError => 'Invalid email';

  @override
  String get passwordError => 'Minimum 3 characters';

  @override
  String get registerHeadline => 'Create your account';

  @override
  String get usernameLabel => 'Username';

  @override
  String get requiredField => 'Required';

  @override
  String get registerButton => 'REGISTER';

  @override
  String get registrationSuccess =>
      'Registration Successful. Please activate your account.';

  @override
  String get loginSuccess => 'Login Successful';
}
