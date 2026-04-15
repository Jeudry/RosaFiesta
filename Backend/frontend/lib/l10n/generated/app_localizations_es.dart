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
  String get countdownDays => 'dias';

  @override
  String get countdownHours => 'horas';

  @override
  String get countdownMinutes => 'min';

  @override
  String get countdownSeconds => 'seg';

  @override
  String get countdownForYourEvent => 'para tu evento';

  @override
  String get countdownEventInProgress => '¡El evento está en curso!';

  @override
  String get countdownEventFinished => 'Evento finalizado';

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

  @override
  String get deliveryFree => 'Gratuito';

  @override
  String get deliveryZone => 'Zona';

  @override
  String get deliveryFee => 'Costo de envío';

  @override
  String get deliveryFreeInSanCristobal => 'Delivery gratuito en San Cristóbal';

  @override
  String get deliveryExtendedZone =>
      'Delivery dentro de la provincia de San Cristóbal';

  @override
  String get deliveryRemoteZone =>
      'Tu dirección está en zona remota. El equipo de RosaFiesta coordinará contigo el envío.';

  @override
  String get deliveryAddressHint => 'Dirección del evento';

  @override
  String get rsvpPending => 'Pendiente';

  @override
  String get rsvpConfirmed => 'Confirmado';

  @override
  String get rsvpDeclined => 'Rechazado';

  @override
  String get rsvpStatus => 'Estado';

  @override
  String confirmedCount(int count, int total) {
    return '$count de $total confirmados';
  }

  @override
  String get colorPalette => 'Paleta de colores';

  @override
  String get selectColors => 'Seleccionar colores';

  @override
  String get colorsForEvent => 'Colores para tu evento';

  @override
  String get matchingItems => 'Artículos que combinan';

  @override
  String get filterByColors => 'Filtrar por colores';

  @override
  String get maxColorsReached => 'Máximo 5 colores';

  @override
  String get downloadContract => 'Descargar contrato';

  @override
  String get contract => 'Contrato';

  @override
  String yourEventIn(int days) {
    return 'Tu evento es en $days días';
  }

  @override
  String get checklist => 'Checklist';

  @override
  String get confirmGuests => 'Confirmar invitados (RSVP)';

  @override
  String get reviewItems => 'Revisar los artículos reservados';

  @override
  String get verifyAddress => 'Verificar dirección de entrega';

  @override
  String get prepareSpace => 'Preparar espacio para montaje';

  @override
  String get paymentPending => 'Pago restante pendiente';

  @override
  String get openChecklist => 'Ver checklist';

  @override
  String get checklistUpdated => 'Checklist actualizado';

  @override
  String get myReservations => 'Mis Reservas';

  @override
  String get upcoming => 'Próximas';

  @override
  String get past => 'Pasadas';

  @override
  String get all => 'Todas';

  @override
  String get viewPhotos => 'Fotos del evento';

  @override
  String get leaveReview => 'Dejar reseña';

  @override
  String get noReservationsYet =>
      'Aún no tienes reservas. Explora el catálogo para planificar tu primer evento.';

  @override
  String get exploreCatalog => 'Explorar catálogo';

  @override
  String get reservas => 'Reservas';

  @override
  String paymentProgress(int paid, int remaining) {
    return 'Reserva RD\$$paid · Resto RD\$$remaining';
  }

  @override
  String completedItems(int count, int total) {
    return '$count de $total completado';
  }

  @override
  String get address => 'Dirección';

  @override
  String get notes => 'Notas';

  @override
  String get payNow => 'Pagar ahora';
}
