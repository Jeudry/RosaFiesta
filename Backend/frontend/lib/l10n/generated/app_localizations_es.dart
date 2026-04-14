// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Spanish Castilian (`es`).
class AppLocalizationsEs extends AppLocalizations {
  AppLocalizationsEs([String locale = 'es']) : super(locale);

  @override
  String get appTitle => 'RosaFiesta';

  @override
  String get welcomeHeadline => 'RosaFiesta';

  @override
  String get welcomeSubheadline => 'Decoración y Eventos con IA';

  @override
  String get beginButton => 'COMENZAR';

  @override
  String get loginButton => 'INGRESAR';

  @override
  String get createAccountButton => 'Crear cuenta';

  @override
  String get loginHeadline => 'Bienvenido de nuevo';

  @override
  String get emailLabel => 'Correo electrónico';

  @override
  String get passwordLabel => 'Contraseña';

  @override
  String get emailError => 'Email inválido';

  @override
  String get passwordError => 'Mínimo 3 caracteres';

  @override
  String get registerHeadline => 'Crea tu cuenta';

  @override
  String get usernameLabel => 'Nombre de usuario';

  @override
  String get requiredField => 'Requerido';

  @override
  String get registerButton => 'REGISTRARSE';

  @override
  String get registrationSuccess =>
      'Registro Exitoso. Por favor activa tu cuenta.';

  @override
  String get loginSuccess => 'Login Exitoso';

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
  String get skipButton => 'Omitir';

  @override
  String get nextButton => 'Siguiente';

  @override
  String get alreadyHaveAccount => '¿Ya tienes una cuenta?';
}
