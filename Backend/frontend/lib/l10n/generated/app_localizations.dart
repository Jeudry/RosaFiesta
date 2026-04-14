import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:intl/intl.dart' as intl;

import 'app_localizations_en.dart';
import 'app_localizations_es.dart';

// ignore_for_file: type=lint

/// Callers can lookup localized strings with an instance of AppLocalizations
/// returned by `AppLocalizations.of(context)`.
///
/// Applications need to include `AppLocalizations.delegate()` in their app's
/// `localizationDelegates` list, and the locales they support in the app's
/// `supportedLocales` list. For example:
///
/// ```dart
/// import 'generated/app_localizations.dart';
///
/// return MaterialApp(
///   localizationsDelegates: AppLocalizations.localizationsDelegates,
///   supportedLocales: AppLocalizations.supportedLocales,
///   home: MyApplicationHome(),
/// );
/// ```
///
/// ## Update pubspec.yaml
///
/// Please make sure to update your pubspec.yaml to include the following
/// packages:
///
/// ```yaml
/// dependencies:
///   # Internationalization support.
///   flutter_localizations:
///     sdk: flutter
///   intl: any # Use the pinned version from flutter_localizations
///
///   # Rest of dependencies
/// ```
///
/// ## iOS Applications
///
/// iOS applications define key application metadata, including supported
/// locales, in an Info.plist file that is built into the application bundle.
/// To configure the locales supported by your app, you’ll need to edit this
/// file.
///
/// First, open your project’s ios/Runner.xcworkspace Xcode workspace file.
/// Then, in the Project Navigator, open the Info.plist file under the Runner
/// project’s Runner folder.
///
/// Next, select the Information Property List item, select Add Item from the
/// Editor menu, then select Localizations from the pop-up menu.
///
/// Select and expand the newly-created Localizations item then, for each
/// locale your application supports, add a new item and select the locale
/// you wish to add from the pop-up menu in the Value field. This list should
/// be consistent with the languages listed in the AppLocalizations.supportedLocales
/// property.
abstract class AppLocalizations {
  AppLocalizations(String locale)
    : localeName = intl.Intl.canonicalizedLocale(locale.toString());

  final String localeName;

  static AppLocalizations? of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations);
  }

  static const LocalizationsDelegate<AppLocalizations> delegate =
      _AppLocalizationsDelegate();

  /// A list of this localizations delegate along with the default localizations
  /// delegates.
  ///
  /// Returns a list of localizations delegates containing this delegate along with
  /// GlobalMaterialLocalizations.delegate, GlobalCupertinoLocalizations.delegate,
  /// and GlobalWidgetsLocalizations.delegate.
  ///
  /// Additional delegates can be added by appending to this list in
  /// MaterialApp. This list does not have to be used at all if a custom list
  /// of delegates is preferred or required.
  static const List<LocalizationsDelegate<dynamic>> localizationsDelegates =
      <LocalizationsDelegate<dynamic>>[
        delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
      ];

  /// A list of this localizations delegate's supported locales.
  static const List<Locale> supportedLocales = <Locale>[
    Locale('en'),
    Locale('es'),
  ];

  /// No description provided for @appTitle.
  ///
  /// In es, this message translates to:
  /// **'RosaFiesta'**
  String get appTitle;

  /// No description provided for @countdownDays.
  ///
  /// In es, this message translates to:
  /// **'dias'**
  String get countdownDays;

  /// No description provided for @countdownHours.
  ///
  /// In es, this message translates to:
  /// **'horas'**
  String get countdownHours;

  /// No description provided for @countdownMinutes.
  ///
  /// In es, this message translates to:
  /// **'min'**
  String get countdownMinutes;

  /// No description provided for @countdownSeconds.
  ///
  /// In es, this message translates to:
  /// **'seg'**
  String get countdownSeconds;

  /// No description provided for @countdownForYourEvent.
  ///
  /// In es, this message translates to:
  /// **'para tu evento'**
  String get countdownForYourEvent;

  /// No description provided for @countdownEventInProgress.
  ///
  /// In es, this message translates to:
  /// **'¡El evento está en curso!'**
  String get countdownEventInProgress;

  /// No description provided for @countdownEventFinished.
  ///
  /// In es, this message translates to:
  /// **'Evento finalizado'**
  String get countdownEventFinished;

  /// No description provided for @welcomeHeadline.
  ///
  /// In es, this message translates to:
  /// **'RosaFiesta'**
  String get welcomeHeadline;

  /// No description provided for @welcomeSubheadline.
  ///
  /// In es, this message translates to:
  /// **'Decoración y Eventos con IA'**
  String get welcomeSubheadline;

  /// No description provided for @beginButton.
  ///
  /// In es, this message translates to:
  /// **'COMENZAR'**
  String get beginButton;

  /// No description provided for @loginButton.
  ///
  /// In es, this message translates to:
  /// **'INGRESAR'**
  String get loginButton;

  /// No description provided for @createAccountButton.
  ///
  /// In es, this message translates to:
  /// **'Crear cuenta'**
  String get createAccountButton;

  /// No description provided for @loginHeadline.
  ///
  /// In es, this message translates to:
  /// **'Bienvenido de nuevo'**
  String get loginHeadline;

  /// No description provided for @emailLabel.
  ///
  /// In es, this message translates to:
  /// **'Correo electrónico'**
  String get emailLabel;

  /// No description provided for @passwordLabel.
  ///
  /// In es, this message translates to:
  /// **'Contraseña'**
  String get passwordLabel;

  /// No description provided for @emailError.
  ///
  /// In es, this message translates to:
  /// **'Email inválido'**
  String get emailError;

  /// No description provided for @passwordError.
  ///
  /// In es, this message translates to:
  /// **'Mínimo 3 caracteres'**
  String get passwordError;

  /// No description provided for @registerHeadline.
  ///
  /// In es, this message translates to:
  /// **'Crea tu cuenta'**
  String get registerHeadline;

  /// No description provided for @usernameLabel.
  ///
  /// In es, this message translates to:
  /// **'Nombre de usuario'**
  String get usernameLabel;

  /// No description provided for @requiredField.
  ///
  /// In es, this message translates to:
  /// **'Requerido'**
  String get requiredField;

  /// No description provided for @registerButton.
  ///
  /// In es, this message translates to:
  /// **'REGISTRARSE'**
  String get registerButton;

  /// No description provided for @registrationSuccess.
  ///
  /// In es, this message translates to:
  /// **'Registro Exitoso. Por favor activa tu cuenta.'**
  String get registrationSuccess;

  /// No description provided for @loginSuccess.
  ///
  /// In es, this message translates to:
  /// **'Login Exitoso'**
  String get loginSuccess;

  /// No description provided for @onboardingTitle1.
  ///
  /// In es, this message translates to:
  /// **'Mira lo que tenemos'**
  String get onboardingTitle1;

  /// No description provided for @onboardingDesc1.
  ///
  /// In es, this message translates to:
  /// **'Navega por nuestro catálogo de decoración y equipo para eventos. Sin registrarte, sin compromiso.'**
  String get onboardingDesc1;

  /// No description provided for @onboardingTitle2.
  ///
  /// In es, this message translates to:
  /// **'Tu asistente con IA'**
  String get onboardingTitle2;

  /// No description provided for @onboardingDesc2.
  ///
  /// In es, this message translates to:
  /// **'Dinos qué evento planeas y te sugerimos artículos, categorías y más — todo guiado por nuestra asistente.'**
  String get onboardingDesc2;

  /// No description provided for @onboardingTitle3.
  ///
  /// In es, this message translates to:
  /// **'Confirma directo'**
  String get onboardingTitle3;

  /// No description provided for @onboardingDesc3.
  ///
  /// In es, this message translates to:
  /// **'Comparte tu lista por WhatsApp con María, la dueña, y confirma todo en minutos — sin trámites.'**
  String get onboardingDesc3;

  /// No description provided for @welcomeSubheadlineAuth.
  ///
  /// In es, this message translates to:
  /// **'Todo para tu evento, guiado por IA'**
  String get welcomeSubheadlineAuth;

  /// No description provided for @skipButton.
  ///
  /// In es, this message translates to:
  /// **'Omitir'**
  String get skipButton;

  /// No description provided for @nextButton.
  ///
  /// In es, this message translates to:
  /// **'Siguiente'**
  String get nextButton;

  /// No description provided for @alreadyHaveAccount.
  ///
  /// In es, this message translates to:
  /// **'¿Ya tienes una cuenta?'**
  String get alreadyHaveAccount;

  /// No description provided for @deliveryFree.
  ///
  /// In es, this message translates to:
  /// **'Gratuito'**
  String get deliveryFree;

  /// No description provided for @deliveryZone.
  ///
  /// In es, this message translates to:
  /// **'Zona'**
  String get deliveryZone;

  /// No description provided for @deliveryFee.
  ///
  /// In es, this message translates to:
  /// **'Costo de envío'**
  String get deliveryFee;

  /// No description provided for @deliveryFreeInSanCristobal.
  ///
  /// In es, this message translates to:
  /// **'Delivery gratuito en San Cristóbal'**
  String get deliveryFreeInSanCristobal;

  /// No description provided for @deliveryExtendedZone.
  ///
  /// In es, this message translates to:
  /// **'Delivery dentro de la provincia de San Cristóbal'**
  String get deliveryExtendedZone;

  /// No description provided for @deliveryRemoteZone.
  ///
  /// In es, this message translates to:
  /// **'Tu dirección está en zona remota. El equipo de RosaFiesta coordinará contigo el envío.'**
  String get deliveryRemoteZone;

  /// No description provided for @deliveryAddressHint.
  ///
  /// In es, this message translates to:
  /// **'Dirección del evento'**
  String get deliveryAddressHint;

  /// No description provided for @rsvpPending.
  ///
  /// In es, this message translates to:
  /// **'Pendiente'**
  String get rsvpPending;

  /// No description provided for @rsvpConfirmed.
  ///
  /// In es, this message translates to:
  /// **'Confirmado'**
  String get rsvpConfirmed;

  /// No description provided for @rsvpDeclined.
  ///
  /// In es, this message translates to:
  /// **'Rechazado'**
  String get rsvpDeclined;

  /// No description provided for @rsvpStatus.
  ///
  /// In es, this message translates to:
  /// **'Estado'**
  String get rsvpStatus;

  /// No description provided for @confirmedCount.
  ///
  /// In es, this message translates to:
  /// **'{count} de {total} confirmados'**
  String confirmedCount(int count, int total);
}

class _AppLocalizationsDelegate
    extends LocalizationsDelegate<AppLocalizations> {
  const _AppLocalizationsDelegate();

  @override
  Future<AppLocalizations> load(Locale locale) {
    return SynchronousFuture<AppLocalizations>(lookupAppLocalizations(locale));
  }

  @override
  bool isSupported(Locale locale) =>
      <String>['en', 'es'].contains(locale.languageCode);

  @override
  bool shouldReload(_AppLocalizationsDelegate old) => false;
}

AppLocalizations lookupAppLocalizations(Locale locale) {
  // Lookup logic when only language code is specified.
  switch (locale.languageCode) {
    case 'en':
      return AppLocalizationsEn();
    case 'es':
      return AppLocalizationsEs();
  }

  throw FlutterError(
    'AppLocalizations.delegate failed to load unsupported locale "$locale". This is likely '
    'an issue with the localizations generation tool. Please file an issue '
    'on GitHub with a reproducible sample app and the gen-l10n configuration '
    'that was used.',
  );
}
