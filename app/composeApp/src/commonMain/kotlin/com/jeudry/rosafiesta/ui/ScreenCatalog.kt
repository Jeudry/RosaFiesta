package com.jeudry.rosafiesta.ui

import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RfThemeVariant

/**
 * Arquetipo de diseño de pantalla para renderizar con fidelidad cada código del catálogo (`p00`, `s01`–`s92`, `A00`–`A39`).
 */
enum class ScreenArchetype {
    BRAND_COVER,
    SPLASH,
    ONBOARDING,
    AUTH_FORM,
    PERSONAL_HOME,
    PERSONAL_CATALOG,
    PERSONAL_PRODUCT_DETAIL,
    PERSONAL_CART_QUOTE,
    PERSONAL_CONTRACT_SIGN,
    PERSONAL_PAYMENT,
    PERSONAL_EVENT_HUB,
    PERSONAL_DATE_CANCEL,
    PERSONAL_ROSA_CHAT,
    PERSONAL_PROFILE,
    PERSONAL_SYSTEM_STATE,
    ADMIN_LOGIN_GATE,
    ADMIN_DASHBOARD,
    ADMIN_CALENDAR,
    ADMIN_QUOTES,
    ADMIN_LOGISTICS,
    ADMIN_ROSA_COPILOT,
    ADMIN_FINANCE,
    ADMIN_INVENTORY_STAFF,
    ADMIN_ROLE_SWITCHER,
    ADMIN_OFFLINE_SYNC,
    ADMIN_WEB_DESKTOP,
}

/**
 * Definición canónica de una pantalla del sistema RosaFiesta (Sección 5 de `entrega-de-diseno.md`).
 */
data class ScreenSpec(
    val code: String,
    val sectionNumber: Int,
    val sectionTitle: String,
    val title: String,
    val subtitle: String,
    val variant: RfThemeVariant,
    val archetype: ScreenArchetype,
    val heroPhoto: RfPhotoKey = RfPhotoKey.BODA,
    val primaryCta: String = "Continuar",
    val highlightMetric: String = "RD$ 4,500",
    val isDesktopWeb: Boolean = false,
)

/**
 * Catálogo completo de las 133 pantallas de RosaFiesta (73 de App Personal + 60 de App Administrativa).
 */
object ScreenCatalog {
    val personalScreens: List<ScreenSpec> = buildList {
        // Sección 1 — Identidad y bienvenida (5 pantallas)
        add(ScreenSpec("p00", 1, "Identidad y bienvenida", "Portada de marca", "Celebraciones con alma dominicana · Mobiliario, flores y Rosa IA", RfThemeVariant.PERSONAL, ScreenArchetype.BRAND_COVER, RfPhotoKey.BODA, "Explorar RosaFiesta", "Variante B"))
        add(ScreenSpec("s01", 1, "Identidad y bienvenida", "Vitrina de marca", "Silla Tiffany RD$ 4/día · Arco orgánico RD$ 95/día · Mesa dulce RD$ 150/día", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CATALOG, RfPhotoKey.ARCO_ROSA, "Ver catálogo completo", "RD$ 4/día"))
        add(ScreenSpec("s02", 1, "Identidad y bienvenida", "Pantalla de arranque", "RosaFiesta · Tu celebración soñada, pieza por pieza", RfThemeVariant.PERSONAL, ScreenArchetype.SPLASH, RfPhotoKey.BODA, "Comenzar", "RosaFiesta"))
        add(ScreenSpec("s03", 1, "Identidad y bienvenida", "Bienvenida · Paso 1", "Alquila mobiliario, arcos orgánicos y centros florales con disponibilidad real", RfThemeVariant.PERSONAL, ScreenArchetype.ONBOARDING, RfPhotoKey.SILLA_TIFFANY, "Siguiente paso", "Paso 1 de 3"))
        add(ScreenSpec("s04", 1, "Identidad y bienvenida", "Bienvenida · Paso 2", "Diseña tu evento con Rosa IA según tus invitados y tu presupuesto en RD$", RfThemeVariant.PERSONAL, ScreenArchetype.ONBOARDING, RfPhotoKey.QUINCEANERA, "Siguiente paso", "Paso 2 de 3"))
        add(ScreenSpec("s05", 1, "Identidad y bienvenida", "Bienvenida · Paso 3", "Reserva con el 30 % de anticipo y sigue el montaje en tiempo real", RfThemeVariant.PERSONAL, ScreenArchetype.ONBOARDING, RfPhotoKey.MESA_HELADOS, "Crear mi cuenta", "30 % anticipo"))

        // Sección 2 — Acceso y cuenta (8 pantallas)
        add(ScreenSpec("s06", 2, "Acceso y cuenta", "Crear cuenta", "Regístrate como cliente para cotizar y guardar tus moodboards", RfThemeVariant.PERSONAL, ScreenArchetype.AUTH_FORM, RfPhotoKey.BODA, "Crear cuenta", "Camila Rodríguez"))
        add(ScreenSpec("s07", 2, "Acceso y cuenta", "Iniciar sesión", "Ingresa con tu correo o continúa con Apple / Google", RfThemeVariant.PERSONAL, ScreenArchetype.AUTH_FORM, RfPhotoKey.BODA, "Entrar a mi cuenta", "camila@rosafiesta.do"))
        add(ScreenSpec("s08", 2, "Acceso y cuenta", "Verificación por código", "Introduce el código de 6 dígitos enviado al 809-555-0142", RfThemeVariant.PERSONAL, ScreenArchetype.AUTH_FORM, RfPhotoKey.BODA, "Verificar código", "4 · 8 · 2 · 9 · 1 · 0"))
        add(ScreenSpec("s09", 2, "Acceso y cuenta", "Preferencias iniciales", "¿Qué tipo de celebración estás planeando hoy?", RfThemeVariant.PERSONAL, ScreenArchetype.ONBOARDING, RfPhotoKey.BODA, "Guardar preferencias", "Bodas y Quinceañeras"))
        add(ScreenSpec("s10", 2, "Acceso y cuenta", "Recuperar contraseña", "Te enviaremos un enlace seguro a camila@rosafiesta.do", RfThemeVariant.PERSONAL, ScreenArchetype.AUTH_FORM, RfPhotoKey.BODA, "Enviar enlace", "Enlace por correo"))
        add(ScreenSpec("s11", 2, "Acceso y cuenta", "Correo enviado", "Revisa tu bandeja de entrada para restablecer tu acceso", RfThemeVariant.PERSONAL, ScreenArchetype.AUTH_FORM, RfPhotoKey.BODA, "Abrir correo", "Enviado hace 1 min"))
        add(ScreenSpec("s12", 2, "Acceso y cuenta", "Nueva contraseña", "Define una clave segura de al menos 8 caracteres", RfThemeVariant.PERSONAL, ScreenArchetype.AUTH_FORM, RfPhotoKey.BODA, "Actualizar contraseña", "Seguridad alta"))
        add(ScreenSpec("s13", 2, "Acceso y cuenta", "Ubicación y zona", "Cobertura en Santo Domingo DN, Jardín Botánico, Piantini y Naco", RfThemeVariant.PERSONAL, ScreenArchetype.AUTH_FORM, RfPhotoKey.BODA, "Confirmar zona", "Santo Domingo DN"))

        // Sección 3 — Inicio y descubrimiento (4 pantallas)
        add(ScreenSpec("s14", 3, "Inicio y descubrimiento", "Inicio", "Hola, Camila · Tu Boda Real Rosa & Dorado es el vie 23 oct en Jardín Botánico", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_HOME, RfPhotoKey.BODA, "Ver mi evento", "Saldo RD$ 3,150"))
        add(ScreenSpec("s15", 3, "Inicio y descubrimiento", "Inicio en modo oscuro", "Vista nocturna de la App Personal con tu próximo evento y acceso a Rosa IA", RfThemeVariant.PERSONAL_DARK, ScreenArchetype.PERSONAL_HOME, RfPhotoKey.BODA, "Abrir Rosa IA", "Vie 23 oct"))
        add(ScreenSpec("s16", 3, "Inicio y descubrimiento", "Plantillas de eventos", "Bodas, Quinceañeras, Cumpleaños infantil, Baby shower, Graduaciones y Corporativos", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CATALOG, RfPhotoKey.QUINCEANERA, "Usar plantilla", "6 plantillas"))
        add(ScreenSpec("s17", 3, "Inicio y descubrimiento", "Detalle de plantilla", "Boda Real Rosa & Dorado · 120 invitados · 145 artículos desde RD$ 4,500", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PRODUCT_DETAIL, RfPhotoKey.BODA, "Personalizar paquete", "RD$ 4,500"))

        // Sección 4 — Catálogo y paquetes (6 pantallas)
        add(ScreenSpec("s18", 4, "Catálogo y paquetes", "Catálogo", "Explora mobiliario, arcos orgánicos, diseño floral y mesas de dulces", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CATALOG, RfPhotoKey.SILLA_TIFFANY, "Filtrar por fecha", "6 categorías"))
        add(ScreenSpec("s19", 4, "Catálogo y paquetes", "Filtros del catálogo", "Disponibilidad para vie 23 oct · Presupuesto RD$ 4 a RD$ 150/día", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CATALOG, RfPhotoKey.MESA_REDONDA, "Aplicar 4 filtros", "Vie 23 oct"))
        add(ScreenSpec("s20", 4, "Catálogo y paquetes", "Categorías", "Mobiliario · Arcos y globos · Diseño floral · Mesas de dulces · Mantelería", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CATALOG, RfPhotoKey.CENTRO_FLORAL, "Ver categoría", "145 artículos"))
        add(ScreenSpec("s21", 4, "Catálogo y paquetes", "Paquetes para eventos", "Combos completos con ahorro incluido y transporte en Santo Domingo", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CATALOG, RfPhotoKey.BODA, "Elegir paquete", "Ahorra RD$ 480"))
        add(ScreenSpec("s22", 4, "Catálogo y paquetes", "Detalle de paquete", "Paquete Boda Real Rosa & Dorado · 120 invitados · 145 artículos · RD$ 4,500", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PRODUCT_DETAIL, RfPhotoKey.BODA, "Agregar paquete completo", "RD$ 4,500"))
        add(ScreenSpec("s23", 4, "Catálogo y paquetes", "Detalle de artículo", "Silla Tiffany dorada · Resina champagne con cojín marfil · 120 disponibles el 23 oct", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PRODUCT_DETAIL, RfPhotoKey.SILLA_TIFFANY, "Agregar 120 sillas (RD$ 480)", "RD$ 4/día"))

        // Sección 5 — Cotización y pago (9 pantallas)
        add(ScreenSpec("s24", 5, "Cotización y pago", "Mi selección (carrito)", "145 artículos seleccionados para Boda Real Rosa & Dorado · Vie 23 oct", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CART_QUOTE, RfPhotoKey.SILLA_TIFFANY, "Solicitar cotización", "RD$ 4,500"))
        add(ScreenSpec("s25", 5, "Cotización y pago", "Solicitud de cotización", "Jardín Botánico · Vie 23 oct · 120 invitados · Montaje 09:00 AM", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CART_QUOTE, RfPhotoKey.BODA, "Enviar solicitud a RosaFiesta", "120 invitados"))
        add(ScreenSpec("s26", 5, "Cotización y pago", "Cotización · En revisión", "María está verificando disponibilidad y logística para tu evento", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CART_QUOTE, RfPhotoKey.BODA, "Ver desglose", "COT-2026-1023"))
        add(ScreenSpec("s27", 5, "Cotización y pago", "Cotización · Lista para pagar", "Total RD$ 4,500 · Anticipo 30 % RD$ 1,350 · Saldo RD$ 3,150 vence el jue 22 oct", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CART_QUOTE, RfPhotoKey.BODA, "Revisar y firmar contrato", "Anticipo RD$ 1,350"))
        add(ScreenSpec("s28", 5, "Cotización y pago", "Firma de contrato", "Contrato de alquiler y política de cancelación (>7d 90 %, 3-7d 50 %, <3d 0 %)", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CONTRACT_SIGN, RfPhotoKey.BODA, "Firmar como Camila Rodríguez", "Firma digital"))
        add(ScreenSpec("s29", 5, "Cotización y pago", "Contrato firmado", "Documento firmado por Camila Rodríguez · Listo para pago de anticipo (30 %)", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CONTRACT_SIGN, RfPhotoKey.BODA, "Pagar anticipo RD$ 1,350", "Firmado"))
        add(ScreenSpec("s30", 5, "Cotización y pago", "Pago de anticipo (30 %)", "Selecciona método: Transferencia Banreservas/Popular, Tarjeta o Efectivo", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PAYMENT, RfPhotoKey.BODA, "Confirmar pago RD$ 1,350", "RD$ 1,350 (30 %)"))
        add(ScreenSpec("s31", 5, "Cotización y pago", "Pago por transferencia", "Cuenta Banreservas · Sube tu comprobante del 18 oct por RD$ 1,350", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PAYMENT, RfPhotoKey.BODA, "Subir comprobante BR-9948120", "Transferencia 18 oct"))
        add(ScreenSpec("s32", 5, "Cotización y pago", "Reserva confirmada", "¡Tu Boda Real Rosa & Dorado está confirmada para el vie 23 oct!", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PAYMENT, RfPhotoKey.BODA, "Ir a mi evento", "REC-2026-0184"))

        // Sección 6 — Mis eventos (7 pantallas)
        add(ScreenSpec("s33", 6, "Mis eventos", "Mis eventos", "2 eventos confirmados: Boda Real Rosa & Dorado (23 oct) y Quinceañera Valentina Crystal (2 nov)", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_EVENT_HUB, RfPhotoKey.BODA, "Abrir Boda Real", "2 activos"))
        add(ScreenSpec("s34", 6, "Mis eventos", "Mi evento · En vivo", "Pedro y su equipo están en Jardín Botánico · Avance de montaje 66 %", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_EVENT_HUB, RfPhotoKey.BODA, "Ver fotos del montaje", "Montaje 66 %"))
        add(ScreenSpec("s35", 6, "Mis eventos", "Error en el pago", "La tarjeta terminada en 4821 fue rechazada por el banco emisor", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PAYMENT, RfPhotoKey.BODA, "Reintentar o usar transferencia", "Error de cobro"))
        add(ScreenSpec("s36", 6, "Mis eventos", "Cambiar fecha del evento", "Tu primer cambio de fecha es gratis sujeto a disponibilidad en calendario", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_DATE_CANCEL, RfPhotoKey.BODA, "Solicitar cambio sin costo", "1er cambio gratis"))
        add(ScreenSpec("s37", 6, "Mis eventos", "Cancelar evento", "Política: >7d reembolso 90 % · 3-7d reembolso 50 % · <3d sin reembolso (0 %)", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_DATE_CANCEL, RfPhotoKey.BODA, "Revisar desglose de cancelación", "Política 90/50/0 %"))
        add(ScreenSpec("s38", 6, "Mis eventos", "Pagar saldo restante (70 %)", "Saldo pendiente RD$ 3,150 · Vence el jue 22 oct (48 h antes del evento)", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PAYMENT, RfPhotoKey.BODA, "Liquidar saldo RD$ 3,150", "Saldo RD$ 3,150"))
        add(ScreenSpec("s39", 6, "Mis eventos", "Evento liquidado", "Pagado 100 % (RD$ 4,500) · Recibo oficial listo para descargar", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PAYMENT, RfPhotoKey.BODA, "Descargar comprobante PDF", "Pagado RD$ 4,500"))

        // Sección 7 — Asistente Rosa IA (10 pantallas)
        add(ScreenSpec("s40", 7, "Asistente Rosa IA", "Rosa IA · Inicio", "Hola Camila, soy Rosa IA · Diseñemos tu celebración paso a paso", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_ROSA_CHAT, RfPhotoKey.BODA, "Comenzar diseño con Rosa IA", "7 pasos guiados"))
        add(ScreenSpec("s41", 7, "Asistente Rosa IA", "Rosa IA · Paso 1 (Tipo)", "¿Qué celebramos? Boda Real Rosa & Dorado en jardín", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_ROSA_CHAT, RfPhotoKey.BODA, "Confirmar tipo: Boda", "Paso 1 de 7"))
        add(ScreenSpec("s42", 7, "Asistente Rosa IA", "Rosa IA · Paso 2 (Fecha y lugar)", "Viernes 23 de octubre en Jardín Botánico, Santo Domingo DN", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_ROSA_CHAT, RfPhotoKey.BODA, "Confirmar fecha y salón", "Paso 2 de 7"))
        add(ScreenSpec("s43", 7, "Asistente Rosa IA", "Rosa IA · Paso 3 (Invitados)", "120 invitados · Calculando 120 sillas Tiffany y 15 mesas redondas", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_ROSA_CHAT, RfPhotoKey.MESA_REDONDA, "Confirmar 120 invitados", "Paso 3 de 7"))
        add(ScreenSpec("s44", 7, "Asistente Rosa IA", "Rosa IA · Paso 4 (Colores y estilo)", "Paleta Rosa Empolvado (#EA4EA9), Dorado Cálido (#FFD45C) y Marfil", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_ROSA_CHAT, RfPhotoKey.ARCO_ROSA, "Elegir paleta Rosa & Dorado", "Paso 4 de 7"))
        add(ScreenSpec("s45", 7, "Asistente Rosa IA", "Rosa IA · Paso 5 (Presupuesto)", "Presupuesto objetivo: RD$ 4,500 con anticipo del 30 % (RD$ 1,350)", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_ROSA_CHAT, RfPhotoKey.CENTRO_FLORAL, "Ajustar a RD$ 4,500", "Paso 5 de 7"))
        add(ScreenSpec("s46", 7, "Asistente Rosa IA", "Rosa IA · Paso 6 (Propuesta)", "Moodboard listo: 120 sillas Tiffany, 15 mesas redondas, 8 centros florales, 1 arco y mesa dulce", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_ROSA_CHAT, RfPhotoKey.BODA, "Ver desglose de 145 artículos", "Paso 6 de 7"))
        add(ScreenSpec("s47", 7, "Asistente Rosa IA", "Rosa IA · Personalizar propuesta", "Ajusta cantidades en vivo sin superar tu presupuesto de RD$ 4,500", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_ROSA_CHAT, RfPhotoKey.MESA_HELADOS, "Guardar ajustes", "145 artículos"))
        add(ScreenSpec("s48", 7, "Asistente Rosa IA", "Rosa IA · Paso 7 (Listo)", "Propuesta final por RD$ 4,500 · Anticipo 30 % RD$ 1,350", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_ROSA_CHAT, RfPhotoKey.BODA, "Convertir en cotización formal", "Paso 7 de 7"))
        add(ScreenSpec("s49", 7, "Asistente Rosa IA", "Mis conversaciones con Rosa", "Historial de moodboards: Boda Real Rosa & Dorado y Quinceañera Valentina Crystal", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_ROSA_CHAT, RfPhotoKey.QUINCEANERA, "Retomar conversación", "2 propuestas"))

        // Sección 8 — Mi perfil y ajustes (6 pantallas: s50-s54 + s88)
        add(ScreenSpec("s50", 8, "Mi perfil y ajustes", "Mi perfil", "Camila Rodríguez · camila@rosafiesta.do · 809-555-0142 · 2 eventos activos", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PROFILE, RfPhotoKey.BODA, "Editar mis datos", "Cliente frecuente"))
        add(ScreenSpec("s51", 8, "Mi perfil y ajustes", "Mis favoritos", "Artículos y paquetes guardados para futuras celebraciones", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PROFILE, RfPhotoKey.ARCO_ROSA, "Agregar favoritos al carrito", "6 guardados"))
        add(ScreenSpec("s52", 8, "Mi perfil y ajustes", "Notificaciones", "Recordatorios de saldo (jue 22 oct) y avisos de llegada del camión", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PROFILE, RfPhotoKey.BODA, "Marcar todo como leído", "3 nuevas"))
        add(ScreenSpec("s53", 8, "Mi perfil y ajustes", "Direcciones y métodos de pago", "Jardín Botánico · Salón Gran Mirador · Cuenta Banreservas", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PROFILE, RfPhotoKey.BODA, "Agregar dirección", "2 lugares"))
        add(ScreenSpec("s54", 8, "Mi perfil y ajustes", "Ayuda y soporte", "Chat directo con María por WhatsApp y preguntas frecuentes", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PROFILE, RfPhotoKey.BODA, "Escribir por WhatsApp", "Respuesta < 5 min"))
        add(ScreenSpec("s88", 8, "Mi perfil y ajustes", "Eliminar mi cuenta", "No puedes eliminar tu cuenta mientras tengas 2 eventos activos pendientes de liquidar", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PROFILE, RfPhotoKey.BODA, "Ver eventos activos que bloquean", "Bloqueado (2 eventos)"))

        // Sección 9 — Estados del sistema y casos de borde (17 pantallas: s75-s87, s89-s92)
        add(ScreenSpec("s75", 9, "Estados del sistema", "Posponer evento · Fecha confirmada", "Nueva fecha reservada sin recargo (primer cambio gratis aplicado)", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_DATE_CANCEL, RfPhotoKey.BODA, "Ver contrato actualizado", "Cambio gratis"))
        add(ScreenSpec("s76", 9, "Estados del sistema", "Evento cancelado · Reembolso", "Reembolso en proceso según tramo de política (90 % / 50 % / 0 %)", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_DATE_CANCEL, RfPhotoKey.BODA, "Descargar constancia", "Reembolso"))
        add(ScreenSpec("s77", 9, "Estados del sistema", "Cargando contenido", "Preparando catálogo y disponibilidad actualizada para tu fecha", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_SYSTEM_STATE, RfPhotoKey.SILLA_TIFFANY, "Actualizando...", "Skeleton"))
        add(ScreenSpec("s78", 9, "Estados del sistema", "Estados vacíos", "Aún no tienes cotizaciones en borrador · Explora el catálogo o habla con Rosa IA", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_SYSTEM_STATE, RfPhotoKey.ARCO_ROSA, "Explorar catálogo", "Sin borradores"))
        add(ScreenSpec("s79", 9, "Estados del sistema", "Sin conexión a internet", "Mostrando tu Boda Real Rosa & Dorado y catálogo guardados en el dispositivo", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_SYSTEM_STATE, RfPhotoKey.BODA, "Reintentar conexión", "Modo offline"))
        add(ScreenSpec("s80", 9, "Estados del sistema", "Búsqueda en el catálogo", "Resultados para «Silla Tiffany dorada» · RD$ 4/día · 120 disponibles", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CATALOG, RfPhotoKey.SILLA_TIFFANY, "Ver Silla Tiffany", "RD$ 4/día"))
        add(ScreenSpec("s81", 9, "Estados del sistema", "Búsqueda sin resultados", "No encontramos artículos con ese nombre · Rosa IA te sugiere alternativas", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_SYSTEM_STATE, RfPhotoKey.CENTRO_FLORAL, "Pedir alternativa a Rosa IA", "0 coincidencias"))
        add(ScreenSpec("s82", 9, "Estados del sistema", "Galería a pantalla completa", "Inspección de acabados de la Silla Tiffany dorada y mesa imperial", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PRODUCT_DETAIL, RfPhotoKey.SILLA_TIFFANY, "Cerrar galería", "Foto 1 de 4"))
        add(ScreenSpec("s83", 9, "Estados del sistema", "Selector de fecha y hora", "Viernes 23 de octubre 2026 · Montaje 09:00 AM · Recogida 11:30 PM", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CART_QUOTE, RfPhotoKey.BODA, "Confirmar horario", "Vie 23 oct"))
        add(ScreenSpec("s84", 9, "Estados del sistema", "Confirmación y aviso", "Hoja inferior modal de confirmación con resumen de anticipo del 30 %", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_SYSTEM_STATE, RfPhotoKey.BODA, "Entendido", "Aviso"))
        add(ScreenSpec("s85", 9, "Estados del sistema", "Estados de formulario", "Validación en vivo de campos requeridos, teléfono dominicano y correo", RfThemeVariant.PERSONAL, ScreenArchetype.AUTH_FORM, RfPhotoKey.BODA, "Corregir campos marcados", "Validación"))
        add(ScreenSpec("s86", 9, "Estados del sistema", "Calificar mi evento", "¡Gracias por confiar en RosaFiesta! Califica el montaje de Pedro y Yuli", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_EVENT_HUB, RfPhotoKey.BODA, "Enviar reseña 5 estrellas", "★★★★★"))
        add(ScreenSpec("s87", 9, "Estados del sistema", "Compartir mi evento", "Comparte el moodboard y ubicación de tu Boda Real Rosa & Dorado", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_EVENT_HUB, RfPhotoKey.BODA, "Compartir enlace por WhatsApp", "Enlace listo"))
        add(ScreenSpec("s89", 9, "Estados del sistema", "Términos y privacidad", "Condiciones de alquiler, cuidado del mobiliario y protección de datos", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PROFILE, RfPhotoKey.BODA, "Aceptar términos", "Vigente 2026"))
        add(ScreenSpec("s90", 9, "Estados del sistema", "Permisos del dispositivo", "Activa cámara y notificaciones para subir comprobantes y recibir alertas", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_SYSTEM_STATE, RfPhotoKey.BODA, "Permitir notificaciones", "Permisos"))
        add(ScreenSpec("s91", 9, "Estados del sistema", "Actualización disponible", "Nueva versión de RosaFiesta con seguimiento de ruta en vivo mejorado", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_SYSTEM_STATE, RfPhotoKey.BODA, "Actualizar ahora", "v1.2 disponible"))
        add(ScreenSpec("s92", 9, "Estados del sistema", "En mantenimiento", "Estamos mejorando la plataforma · Tus reservas del 23 oct siguen activas", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_SYSTEM_STATE, RfPhotoKey.BODA, "Consultar estado por WhatsApp", "Regresamos pronto"))
    }

    val adminScreens: List<ScreenSpec> = buildList {
        // Sección 10 — Acceso y panel (6 pantallas: A00, A01, A02, s55, s56, s57)
        add(ScreenSpec("A00", 10, "Acceso y panel", "Portada App Administrativa", "RosaFiesta Admin · Operación, inventario, logística, finanzas y Rosa IA", RfThemeVariant.ADMIN, ScreenArchetype.BRAND_COVER, RfPhotoKey.CORPORATIVO, "Entrar al panel", "App Administrativa"))
        add(ScreenSpec("A01", 10, "Acceso y panel", "Acceso del equipo", "Ingresa como María (Dueña), Pedro (Logística), Yuli (Decoración) o Andrés (Almacén)", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_LOGIN_GATE, RfPhotoKey.CORPORATIVO, "Entrar como María (Admin)", "maria@rosafiesta.do"))
        add(ScreenSpec("A02", 10, "Acceso y panel", "Acceso denegado a clientes", "Esta app es exclusiva para el equipo de RosaFiesta · Usa la App Personal", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_LOGIN_GATE, RfPhotoKey.BODA, "Abrir App Personal de clientes", "Solo personal"))
        add(ScreenSpec("s55", 10, "Acceso y panel", "Panel del negocio · Resumen", "Ingresos oct RD$ 12,450 · Por cobrar RD$ 7,724 · 4 eventos activos · 2 solicitudes nuevas", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_DASHBOARD, RfPhotoKey.BODA, "Revisar 2 solicitudes nuevas", "RD$ 12,450"))
        add(ScreenSpec("s56", 10, "Acceso y panel", "Panel · Alertas y cobros", "Saldo de Boda Real (RD$ 3,150) vence el jue 22 oct · 1 transferencia por verificar", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_DASHBOARD, RfPhotoKey.BODA, "Enviar recordatorio a Camila", "Por cobrar RD$ 7,724"))
        add(ScreenSpec("s57", 10, "Acceso y panel", "Panel · Operación del día", "Hoy en montaje: Boda Real Rosa & Dorado en Jardín Botánico · Equipo Pedro, Yuli, Andrés", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_DASHBOARD, RfPhotoKey.BODA, "Ver hoja de ruta", "Montaje 66 %"))

        // Sección 11 — Agenda y calendario (4 pantallas: A03, A04, s58, s59)
        add(ScreenSpec("A03", 11, "Agenda y calendario", "Calendario mensual", "Octubre y Noviembre 2026: 23 oct Boda Real, 2 nov Quinceañera, 7 nov Mateo, 13 nov Graduación", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_CALENDAR, RfPhotoKey.BODA, "Ver día 23 oct", "4 eventos activos"))
        add(ScreenSpec("A04", 11, "Agenda y calendario", "Bloquear fechas", "Reserva días de mantenimiento de mobiliario o inventario general en bodega", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_CALENDAR, RfPhotoKey.SILLA_TIFFANY, "Confirmar bloqueo en agenda", "Bloqueo operativo"))
        add(ScreenSpec("s58", 11, "Agenda y calendario", "Agenda · Vista semanal", "Semana del 19 al 25 de octubre · Viernes 23 oct: Boda Real Rosa & Dorado (120 inv.)", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_CALENDAR, RfPhotoKey.BODA, "Abrir orden de montaje", "Vie 23 oct"))
        add(ScreenSpec("s59", 11, "Agenda y calendario", "Agenda · Detalle del día", "08:15 AM Carga en bodega · 09:00 AM Montaje Jardín Botánico · 11:30 PM Recogida", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_CALENDAR, RfPhotoKey.BODA, "Asignar vehículo", "3 bloques horarios"))

        // Sección 12 — Solicitudes y cotizaciones (6 pantallas: A05-A08, s60, s61)
        add(ScreenSpec("A05", 12, "Solicitudes y cotizaciones", "Solicitudes recibidas", "2 nuevas: Cumpleaños de Mateo (Laura Peña, RD$ 405) y Graduación Promo 2026 (Ana Gómez, RD$ 2,140)", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_QUOTES, RfPhotoKey.CUMPLE_INFANTIL, "Revisar solicitud de Laura Peña", "2 nuevas"))
        add(ScreenSpec("A06", 12, "Solicitudes y cotizaciones", "Crear cotización manual", "Selecciona cliente, fecha, lugar y artículos con cálculo automático de 30 % de anticipo", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_QUOTES, RfPhotoKey.MESA_REDONDA, "Emitir cotización", "30 % anticipo"))
        add(ScreenSpec("A07", 12, "Solicitudes y cotizaciones", "Editar precios y descuentos", "Ajuste de transporte a Jardín Botánico y descuento de cliente frecuente", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_QUOTES, RfPhotoKey.BODA, "Guardar y enviar a Camila", "Total RD$ 4,500"))
        add(ScreenSpec("A08", 12, "Solicitudes y cotizaciones", "Declinar solicitud", "Envía respuesta cortés cuando la fecha no tiene disponibilidad de mobiliario", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_QUOTES, RfPhotoKey.BODA, "Enviar mensaje y alternativa", "Respuesta amable"))
        add(ScreenSpec("s60", 12, "Solicitudes y cotizaciones", "Detalle de solicitud", "Cumpleaños de Mateo · Laura Peña · Sáb 7 nov · 40 invitados · Est. RD$ 405", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_QUOTES, RfPhotoKey.CUMPLE_INFANTIL, "Aprobar y enviar contrato", "RD$ 405"))
        add(ScreenSpec("s61", 12, "Solicitudes y cotizaciones", "Cotización enviada", "Enviada por WhatsApp y correo · Esperando firma digital y anticipo del 30 %", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_QUOTES, RfPhotoKey.CUMPLE_INFANTIL, "Ver seguimiento", "Anticipo RD$ 121"))

        // Sección 13 — Eventos y logística (13 pantallas: A09-A15, s62-s64, s66-s68)
        add(ScreenSpec("A09", 13, "Eventos y logística", "Disponibilidad por fecha", "Vie 23 oct: 120/240 Sillas Tiffany libres · 17/35 Mesas redondas libres · 5/10 Arcos", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_INVENTORY_STAFF, RfPhotoKey.SILLA_TIFFANY, "Reservar lote para evento", "Stock en vivo"))
        add(ScreenSpec("A10", 13, "Eventos y logística", "Asignar equipo al evento", "Boda Real Rosa & Dorado: Pedro (Líder), Yuli (Decoración), Andrés (Transporte)", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_LOGISTICS, RfPhotoKey.BODA, "Confirmar cuadrilla (3)", "Pedro · Yuli · Andrés"))
        add(ScreenSpec("A11", 13, "Eventos y logística", "Hoja de montaje y carga", "Lista de carga verificada en bodega: 120 sillas, 15 mesas, 8 centros, 1 arco, 1 mesa dulce", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_LOGISTICS, RfPhotoKey.SILLA_TIFFANY, "Marcar carga completada", "145 artículos"))
        add(ScreenSpec("A12", 13, "Eventos y logística", "Subir fotos del montaje", "4 fotos de evidencia en Jardín Botánico compartidas con Camila Rodríguez", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_LOGISTICS, RfPhotoKey.ARCO_ROSA, "Subir nueva evidencia", "4 fotos"))
        add(ScreenSpec("A13", 13, "Eventos y logística", "Ruta de entrega del día", "Parada 1: Jardín Botánico (09:00 AM) · Parada 2: Salón Gran Mirador (03:30 PM)", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_LOGISTICS, RfPhotoKey.BODA, "Iniciar navegación", "2 paradas hoy"))
        add(ScreenSpec("A14", 13, "Eventos y logística", "Incidencias y daños post-evento", "1 base de cristal astillada en mesa #4 · Cargo de reposición RD$ 120", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_LOGISTICS, RfPhotoKey.CENTRO_FLORAL, "Registrar incidencia", "Reposición RD$ 120"))
        add(ScreenSpec("A15", 13, "Eventos y logística", "Escanear código QR de artículo", "Lectura rápida de lote RF-001 (Silla Tiffany dorada) al salir y regresar a bodega", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_LOGISTICS, RfPhotoKey.SILLA_TIFFANY, "Confirmar lectura QR", "Lote RF-001"))
        add(ScreenSpec("s62", 13, "Eventos y logística", "Eventos confirmados", "Boda Real (23 oct · RD$ 4,500), Quinceañera Valentina (2 nov · RD$ 6,200), Graduación (13 nov · RD$ 2,140)", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_LOGISTICS, RfPhotoKey.BODA, "Abrir Boda Real", "4 activos"))
        add(ScreenSpec("s63", 13, "Eventos y logística", "Reserva de inventario", "145 artículos bloqueados en almacén para el vie 23 oct en Jardín Botánico", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_INVENTORY_STAFF, RfPhotoKey.MESA_REDONDA, "Imprimir marbetes de salida", "145 reservados"))
        add(ScreenSpec("s64", 13, "Eventos y logística", "Contrato firmado por cliente", "Camila Rodríguez firmó digitalmente · Anticipo RD$ 1,350 verificado", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_QUOTES, RfPhotoKey.BODA, "Descargar contrato PDF", "COT-2026-1023"))
        add(ScreenSpec("s66", 13, "Eventos y logística", "Ficha operativa del evento", "Boda Real Rosa & Dorado · Vie 23 oct · Jardín Botánico · Líder: Pedro", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_LOGISTICS, RfPhotoKey.BODA, "Ver checklist de montaje", "RF-2026-1023"))
        add(ScreenSpec("s67", 13, "Eventos y logística", "Evento en montaje", "Cuadrilla en Jardín Botánico · Instalación de arco orgánico y centros florales", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_LOGISTICS, RfPhotoKey.ARCO_ROSA, "Actualizar estado a 80 %", "En sitio"))
        add(ScreenSpec("s68", 13, "Eventos y logística", "Checklist de montaje y desmontaje", "4 de 6 pasos completados por Pedro y Andrés · Pendiente arco y recogida nocturna", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_LOGISTICS, RfPhotoKey.BODA, "Marcar paso 5 completado", "66 % completado"))

        // Sección 14 — Asistente Rosa IA para el negocio (4 pantallas: s65, A16, A17, A18)
        add(ScreenSpec("s65", 14, "Asistente Rosa IA (Admin)", "Rosa IA · Copiloto del negocio", "Asistente administrativo en índigo→turquesa: redacta cotizaciones, verifica stock y sugiere cobros", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_ROSA_COPILOT, RfPhotoKey.CORPORATIVO, "Generar cotización con Rosa IA", "Rosa IA Admin"))
        add(ScreenSpec("A16", 14, "Asistente Rosa IA (Admin)", "Rosa IA · Conversación activa", "María: «Arma propuesta para Quinceañera de 100 invitados con presupuesto RD$ 6,200»", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_ROSA_COPILOT, RfPhotoKey.QUINCEANERA, "Ver desglose sugerido", "RD$ 6,200"))
        add(ScreenSpec("A17", 14, "Asistente Rosa IA (Admin)", "Rosa IA · Propuesta lista", "Stock verificado para lun 2 nov: 100 sillas Tiffany, 13 mesas redondas, arco cristal y mesa dulce", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_ROSA_COPILOT, RfPhotoKey.QUINCEANERA, "Aprobar borrador", "Anticipo RD$ 1,860"))
        add(ScreenSpec("A18", 14, "Asistente Rosa IA (Admin)", "Rosa IA · Enviar al cliente", "Envía el moodboard y enlace de pago del 30 % directamente al WhatsApp de Camila", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_ROSA_COPILOT, RfPhotoKey.QUINCEANERA, "Enviar propuesta a Camila", "Lista para enviar"))

        // Sección 15 — Cobros y finanzas (7 pantallas: s69, s70, s71, A19, A20, A21, A22)
        add(ScreenSpec("s69", 15, "Cobros y finanzas", "Finanzas del mes", "Ingresos de octubre RD$ 12,450 · Por cobrar RD$ 7,724 · 4 eventos activos", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_FINANCE, RfPhotoKey.CORPORATIVO, "Exportar corte mensual", "RD$ 12,450"))
        add(ScreenSpec("s70", 15, "Cobros y finanzas", "Registrar pago recibido", "Verifica transferencia Banreservas BR-9948120 por RD$ 1,350 (Camila Rodríguez)", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_FINANCE, RfPhotoKey.BODA, "Confirmar abono RD$ 1,350", "RD$ 1,350"))
        add(ScreenSpec("s71", 15, "Cobros y finanzas", "Recordatorios de saldo", "Boda Real (RD$ 3,150 vence jue 22 oct) y Quinceañera Valentina (RD$ 4,340 vence vie 30 oct)", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_FINANCE, RfPhotoKey.BODA, "Enviar recordatorio por WhatsApp", "Por cobrar RD$ 7,724"))
        add(ScreenSpec("A19", 15, "Cobros y finanzas", "Emitir recibo y factura", "Recibo oficial REC-2026-0184 a nombre de Camila Rodríguez con desglose de anticipo", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_FINANCE, RfPhotoKey.BODA, "Compartir recibo PDF", "REC-2026-0184"))
        add(ScreenSpec("A20", 15, "Cobros y finanzas", "Reporte mensual e indicadores", "Octubre 2026: Gala Caribe RD$ 8,500 + Anticipos RD$ 3,950 = RD$ 12,450 cobrados", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_FINANCE, RfPhotoKey.CORPORATIVO, "Descargar reporte contable", "84 % ocupación"))
        add(ScreenSpec("A21", 15, "Cobros y finanzas", "Gastos operativos del evento", "Combustible camión RD$ 450 · Flores frescas RD$ 680 · Ayudante extra RD$ 500", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_FINANCE, RfPhotoKey.CENTRO_FLORAL, "Registrar gasto operativo", "Margen 74 %"))
        add(ScreenSpec("A22", 15, "Cobros y finanzas", "Reembolsos y cancelaciones", "Cálculo automático según política: >7d 90 % · 3-7d 50 % · <3d 0 %", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_FINANCE, RfPhotoKey.BODA, "Procesar reembolso", "Política 90/50/0 %"))

        // Sección 16 — Catálogo, clientes y equipo (11 pantallas: s72-s74, A23-A30)
        add(ScreenSpec("s72", 16, "Catálogo, clientes y equipo", "Catálogo e inventario", "6 líneas principales · 358 piezas totales en almacén · Precios de RD$ 4 a RD$ 150/día", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_INVENTORY_STAFF, RfPhotoKey.SILLA_TIFFANY, "Nuevo artículo", "358 piezas"))
        add(ScreenSpec("s73", 16, "Catálogo, clientes y equipo", "Editar artículo del catálogo", "Silla Tiffany dorada · Precio RD$ 4/día · Stock total 240 · Disponibles 23 oct: 120", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_INVENTORY_STAFF, RfPhotoKey.SILLA_TIFFANY, "Guardar cambios del artículo", "RD$ 4/día"))
        add(ScreenSpec("s74", 16, "Catálogo, clientes y equipo", "Ajustes del negocio", "RosaFiesta SRL · Anticipo estándar 30 % · Primer cambio de fecha gratis", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_INVENTORY_STAFF, RfPhotoKey.CORPORATIVO, "Guardar configuración", "Anticipo 30 %"))
        add(ScreenSpec("A23", 16, "Catálogo, clientes y equipo", "Nuevo artículo", "Da de alta mobiliario o decoración con foto, código RF, stock y precio por día en RD$", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_INVENTORY_STAFF, RfPhotoKey.MESA_PLEGABLE, "Publicar en el catálogo", "Nuevo SKU"))
        add(ScreenSpec("A24", 16, "Catálogo, clientes y equipo", "Gestionar paquetes", "Paquete Boda Real (RD$ 4,500), Quinceañera Crystal (RD$ 6,200) y Cumpleaños (RD$ 405)", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_INVENTORY_STAFF, RfPhotoKey.BODA, "Editar paquete Boda Real", "3 paquetes"))
        add(ScreenSpec("A25", 16, "Catálogo, clientes y equipo", "Bajas y mantenimiento", "4 sillas Tiffany en taller de pintura dorada · Regresan al stock el lun 26 oct", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_INVENTORY_STAFF, RfPhotoKey.SILLA_TIFFANY, "Liberar piezas reparadas", "4 en taller"))
        add(ScreenSpec("A26", 16, "Catálogo, clientes y equipo", "Cartera de clientes", "Camila Rodríguez (2 eventos), Laura Peña, Ana Gómez y Grupo Caribe", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_INVENTORY_STAFF, RfPhotoKey.BODA, "Abrir ficha de Camila", "4 clientes activos"))
        add(ScreenSpec("A27", 16, "Catálogo, clientes y equipo", "Ficha del cliente", "Camila Rodríguez · Total contratado RD$ 10,700 · Pagado RD$ 3,210 · Saldo RD$ 7,490", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_INVENTORY_STAFF, RfPhotoKey.BODA, "Contactar a Camila", "Saldo RD$ 7,490"))
        add(ScreenSpec("A28", 16, "Catálogo, clientes y equipo", "Equipo y permisos", "María (Admin total), Pedro (Logística), Yuli (Decoración y tareas, sin pagos), Andrés (Almacén)", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_INVENTORY_STAFF, RfPhotoKey.CORPORATIVO, "Guardar permisos de Yuli", "4 integrantes"))
        add(ScreenSpec("A29", 16, "Catálogo, clientes y equipo", "Cuentas bancarias y cobro", "Banreservas · Banco Popular · Terminal de tarjetas · Efectivo en showroom", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_FINANCE, RfPhotoKey.CORPORATIVO, "Actualizar datos bancarios", "3 métodos activos"))
        add(ScreenSpec("A30", 16, "Catálogo, clientes y equipo", "Cambio de vista (Admin ↔ Cliente)", "Permite a María o al equipo probar la App Personal como cliente y volver al panel", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_ROLE_SWITCHER, RfPhotoKey.BODA, "Entrar como cliente (App Personal)", "Cambio de rol"))

        // Sección 17 — Estados del sistema y escritorio (9 pantallas: A31-A39)
        add(ScreenSpec("A31", 17, "Estados y escritorio", "Centro de notificaciones", "Comprobante subido por Ana Gómez (RD$ 642) y checklist actualizado por Pedro", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_DASHBOARD, RfPhotoKey.BODA, "Atender alertas", "4 alertas"))
        add(ScreenSpec("A32", 17, "Estados y escritorio", "Buscador global", "Busca por cliente (Camila), código (RF-2026-1023), salón (Jardín Botánico) o artículo", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_DASHBOARD, RfPhotoKey.SILLA_TIFFANY, "Abrir resultado", "Búsqueda rápida"))
        add(ScreenSpec("A33", 17, "Estados y escritorio", "Búsqueda sin resultados", "No hay órdenes ni clientes que coincidan con el filtro aplicado", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_DASHBOARD, RfPhotoKey.CORPORATIVO, "Limpiar filtros", "0 resultados"))
        add(ScreenSpec("A34", 17, "Estados y escritorio", "Estados vacíos del panel", "Sin incidencias pendientes hoy · Todo el mobiliario está contabilizado", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_DASHBOARD, RfPhotoKey.SILLA_TIFFANY, "Volver al panel", "Al día"))
        add(ScreenSpec("A35", 17, "Estados y escritorio", "Cargando datos operativos", "Sincronizando inventario, agenda de octubre y cobros pendientes", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_DASHBOARD, RfPhotoKey.CORPORATIVO, "Sincronizando...", "Cargando"))
        add(ScreenSpec("A36", 17, "Estados y escritorio", "Modo sin señal en salón", "Trabajando offline en Jardín Botánico · Puedes seguir marcando el checklist y tomando fotos", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_OFFLINE_SYNC, RfPhotoKey.BODA, "Ver 3 cambios en cola", "Offline activo"))
        add(ScreenSpec("A37", 17, "Estados y escritorio", "Cola de sincronización", "3 cambios guardados en el teléfono de Pedro listos para subir al recuperar señal", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_OFFLINE_SYNC, RfPhotoKey.BODA, "Sincronizar 3 cambios ahora", "3 en cola"))
        add(ScreenSpec("A38", 17, "Estados y escritorio", "Confirmaciones críticas", "Confirmar anulación de orden o ajuste mayor de inventario en almacén", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_DASHBOARD, RfPhotoKey.BODA, "Confirmar operación", "Doble verificación"))
        add(ScreenSpec("A39", 17, "Estados y escritorio", "Panel web de escritorio (1440×900)", "Vista panorámica de 3 columnas para María: KPIs de octubre, agenda de montaje, inventario y Rosa IA", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_WEB_DESKTOP, RfPhotoKey.CORPORATIVO, "Exportar reporte ejecutivo", "1440×900 Web", isDesktopWeb = true))
    }

    val allScreens: List<ScreenSpec> = personalScreens + adminScreens

    private val byCode: Map<String, ScreenSpec> = allScreens.associateBy { it.code }

    fun findByCode(code: String): ScreenSpec = byCode[code] ?: personalScreens.first()
}
