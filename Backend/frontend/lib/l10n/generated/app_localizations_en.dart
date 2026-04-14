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
  String get countdownDays => 'days';

  @override
  String get countdownHours => 'hours';

  @override
  String get countdownMinutes => 'min';

  @override
  String get countdownSeconds => 'sec';

  @override
  String get countdownForYourEvent => 'for your event';

  @override
  String get countdownEventInProgress => 'Event in progress!';

  @override
  String get countdownEventFinished => 'Event finished';

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

  @override
  String get onboardingTitle1 => 'Mira lo que tenemos';

  @override
  String get onboardingDesc1 =>
      'Navega por nuestro catálogo de decoración y equipo para eventos. Sin registrarte, sin compromiso.';

  @override
  String get onboardingTitle2 => 'Tu asistente con IA';

  @override
  String get onboardingDesc2 =>
      'Dinos qué evento planeas y te sugerimos artículos, categorías y más — todo guiado por nuestra asistente.';

  @override
  String get onboardingTitle3 => 'Confirma directo';

  @override
  String get onboardingDesc3 =>
      'Comparte tu lista por WhatsApp con María, la dueña, y confirma todo en minutos — sin trámites.';

  @override
  String get welcomeSubheadlineAuth => 'Todo para tu evento, guiado por IA';

  @override
  String get skipButton => 'Skip';

  @override
  String get nextButton => 'Next';

  @override
  String get alreadyHaveAccount => 'Already have an account?';

  @override
  String get deliveryFree => 'Free';

  @override
  String get deliveryZone => 'Zone';

  @override
  String get deliveryFee => 'Delivery fee';

  @override
  String get deliveryFreeInSanCristobal => 'Free delivery in San Cristóbal';

  @override
  String get deliveryExtendedZone => 'Delivery within San Cristóbal province';

  @override
  String get deliveryRemoteZone =>
      'Your address is in a remote zone. The RosaFiesta team will coordinate delivery with you.';

  @override
  String get deliveryAddressHint => 'Event address';

  @override
  String get rsvpPending => 'Pending';

  @override
  String get rsvpConfirmed => 'Confirmed';

  @override
  String get rsvpDeclined => 'Declined';

  @override
  String get rsvpStatus => 'Status';

  @override
  String confirmedCount(int count, int total) {
    return '$count of $total confirmed';
  }
}
