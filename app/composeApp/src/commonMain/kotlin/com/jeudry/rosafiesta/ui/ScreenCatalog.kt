package com.jeudry.rosafiesta.ui

import com.jeudry.rosafiesta.designsystem.AdminTab
import com.jeudry.rosafiesta.designsystem.PersonalTab
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
 * Definición canónica de una pantalla del sistema RosaFiesta (Secciones 4.1, 4.2 y 5 de `entrega-de-diseno.md`).
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
 * Catálogo completo de las 133 pantallas de RosaFiesta (73 de App Personal + 60 de App Administrativa)
 * alineado 1:1 con las secciones 4.1, 4.2 y 5 de `docs/design/entrega-de-diseno.md`.
 */
object ScreenCatalog {
    val personalScreens: List<ScreenSpec> = buildList {
        // 4.1 Acceso (`p00`, `s01`–`s13`)
        add(ScreenSpec("p00", 1, "Acceso", "Splash · RosaFiesta", "Celebraciones con alma dominicana · Mobiliario, flores y Rosa IA", RfThemeVariant.PERSONAL, ScreenArchetype.SPLASH, RfPhotoKey.BODA, "Comenzar", "Variante B"))
        add(ScreenSpec("s01", 1, "Acceso", "Catálogo · Invitado", "Explora Silla Tiffany RD$ 4/día, Arco orgánico RD$ 95/día y Mesa dulce RD$ 150/día", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CATALOG, RfPhotoKey.SILLA_TIFFANY, "Iniciar sesión para cotizar", "Modo invitado"))
        add(ScreenSpec("s02", 1, "Acceso", "Favoritos · Invitado", "Guarda tus artículos favoritos iniciando sesión en tu cuenta personal", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CATALOG, RfPhotoKey.ARCO_ROSA, "Entrar a mi cuenta", "Modo invitado"))
        add(ScreenSpec("s03", 1, "Acceso", "Onboarding · Mobiliario real", "Alquila sillas Tiffany, mesas imperiales y centros florales con stock por fecha", RfThemeVariant.PERSONAL, ScreenArchetype.ONBOARDING, RfPhotoKey.SILLA_TIFFANY, "Siguiente", "Paso 1 de 3"))
        add(ScreenSpec("s04", 1, "Acceso", "Onboarding · Rosa IA", "Rosa IA arma tu lista de artículos y cotización en RD$ según tus invitados", RfThemeVariant.PERSONAL, ScreenArchetype.ONBOARDING, RfPhotoKey.QUINCEANERA, "Siguiente", "Paso 2 de 3"))
        add(ScreenSpec("s05", 1, "Acceso", "Onboarding · Reserva y seguimiento", "Confirma con el 30 % de anticipo y sigue el montaje y cronograma en vivo", RfThemeVariant.PERSONAL, ScreenArchetype.ONBOARDING, RfPhotoKey.MESA_HELADOS, "Ir a bienvenida", "Paso 3 de 3"))
        add(ScreenSpec("s06", 1, "Acceso", "Bienvenida", "Tu evento soñado en Santo Domingo empieza aquí · Entra o explora como invitado", RfThemeVariant.PERSONAL, ScreenArchetype.AUTH_FORM, RfPhotoKey.BODA, "Iniciar sesión", "RosaFiesta"))
        add(ScreenSpec("s07", 1, "Acceso", "Iniciar sesión", "Ingresa con tu correo camila@rosafiesta.do o continúa con Apple y Google", RfThemeVariant.PERSONAL, ScreenArchetype.AUTH_FORM, RfPhotoKey.BODA, "Entrar a mi cuenta", "camila@rosafiesta.do"))
        add(ScreenSpec("s08", 1, "Acceso", "Registro", "Crea tu cuenta personal para armar eventos, bocetos y cotizaciones", RfThemeVariant.PERSONAL, ScreenArchetype.AUTH_FORM, RfPhotoKey.BODA, "Crear mi cuenta", "Camila Rodríguez"))
        add(ScreenSpec("s09", 1, "Acceso", "Recuperar contraseña", "Te enviaremos un código o enlace seguro a camila@rosafiesta.do", RfThemeVariant.PERSONAL, ScreenArchetype.AUTH_FORM, RfPhotoKey.BODA, "Enviar instrucciones", "Recuperación"))
        add(ScreenSpec("s10", 1, "Acceso", "Nueva contraseña", "Revisa tu correo y define una nueva clave segura de al menos 8 caracteres", RfThemeVariant.PERSONAL, ScreenArchetype.AUTH_FORM, RfPhotoKey.BODA, "Actualizar contraseña", "Clave segura"))
        add(ScreenSpec("s11", 1, "Acceso", "Confirmación de correo", "Código de 6 dígitos enviado a camila@rosafiesta.do para verificar tu cuenta", RfThemeVariant.PERSONAL, ScreenArchetype.AUTH_FORM, RfPhotoKey.BODA, "Confirmar correo", "4 · 8 · 2 · 9 · 1 · 0"))
        add(ScreenSpec("s12", 1, "Acceso", "Confirmación de teléfono", "Verificación SMS enviada al +1 (809) 555-0142 para avisos de montaje", RfThemeVariant.PERSONAL, ScreenArchetype.AUTH_FORM, RfPhotoKey.BODA, "Confirmar teléfono", "Verificado"))
        add(ScreenSpec("s13", 1, "Acceso", "Sesión iniciada", "¡Bienvenida, Camila Rodríguez! Tu cuenta y tus eventos están listos", RfThemeVariant.PERSONAL, ScreenArchetype.AUTH_FORM, RfPhotoKey.BODA, "Ir al inicio", "Cuenta activa"))

        // 4.1 Descubrir (`s14`–`s25`)
        add(ScreenSpec("s14", 2, "Descubrir", "Inicio", "Hola, Camila · Boda Real Rosa & Dorado el vie 23 oct en Jardín Botánico", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_HOME, RfPhotoKey.BODA, "Ver mi evento", "Vie 23 oct"))
        add(ScreenSpec("s15", 2, "Descubrir", "Inicio · Modo oscuro", "Tema nocturno de la App Personal con tu evento activo y Rosa IA", RfThemeVariant.PERSONAL_DARK, ScreenArchetype.PERSONAL_HOME, RfPhotoKey.BODA, "Hablar con Rosa IA", "Modo oscuro"))
        add(ScreenSpec("s16", 2, "Descubrir", "Acerca de RosaFiesta", "Más de 8 años creando bodas, quinceañeras y eventos corporativos en RD", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CATALOG, RfPhotoKey.BODA, "Ver reseñas de clientes", "Santo Domingo"))
        add(ScreenSpec("s17", 2, "Descubrir", "Reseñas de clientes", "Calificación 4.9 ★ en 180 eventos · Fotos reales de montajes entregados", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CATALOG, RfPhotoKey.QUINCEANERA, "Explorar catálogo", "4.9 ★ (180)"))
        add(ScreenSpec("s18", 2, "Descubrir", "Catálogo", "Silla Tiffany RD$ 4 · Mesa redonda RD$ 25 · Centro floral RD$ 35 · Arco RD$ 95", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CATALOG, RfPhotoKey.SILLA_TIFFANY, "Buscar y filtrar", "6 categorías"))
        add(ScreenSpec("s19", 2, "Descubrir", "Artículo · Silla Tiffany", "Silla Tiffany dorada con cojín marfil · RD$ 4/día · 120 disponibles para el 23 oct", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PRODUCT_DETAIL, RfPhotoKey.SILLA_TIFFANY, "Agregar 120 sillas (RD$ 480)", "RD$ 4/día"))
        add(ScreenSpec("s20", 2, "Descubrir", "Categorías", "Mobiliario · Mesas · Centros florales · Arcos orgánicos · Estaciones dulces", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CATALOG, RfPhotoKey.CENTRO_FLORAL, "Abrir categoría", "358 piezas"))
        add(ScreenSpec("s21", 2, "Descubrir", "Paquetes", "Combos completos para Bodas, Quinceañeras, Cumpleaños y Eventos Corporativos", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CATALOG, RfPhotoKey.BODA, "Ver detalle del paquete", "Desde RD$ 405"))
        add(ScreenSpec("s22", 2, "Descubrir", "Detalle de paquete", "Boda Real Rosa & Dorado · 120 invitados · 145 artículos · Total RD$ 4,500", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PRODUCT_DETAIL, RfPhotoKey.BODA, "Usar este paquete", "RD$ 4,500"))
        add(ScreenSpec("s23", 2, "Descubrir", "Tienda y alquiler", "Colecciones destacadas por estilo: Jardín Botánico, Crystal y Infantil", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CATALOG, RfPhotoKey.MESA_HELADOS, "Agregar a mi evento", "RD$ 150/día"))
        add(ScreenSpec("s24", 2, "Descubrir", "Favoritos", "Tus artículos y paquetes guardados para Boda Real Rosa & Dorado", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CATALOG, RfPhotoKey.ARCO_ROSA, "Pasar favoritos a mi evento", "6 guardados"))
        add(ScreenSpec("s25", 2, "Descubrir", "Mi evento (borrador)", "Borrador activo: Boda Real Rosa & Dorado · 120 invitados · 145 artículos", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CART_QUOTE, RfPhotoKey.BODA, "Solicitar cotización", "RD$ 4,500"))

        // 4.1 Rosa IA (`s26`–`s36`)
        add(ScreenSpec("s26", 3, "Rosa IA", "Rosa IA · Tipo de evento", "¿Qué estamos celebrando? Boda, Quinceañera, Cumpleaños, Baby shower o Gala", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_ROSA_CHAT, RfPhotoKey.BODA, "Elegir Boda Real", "Paso 1 · Tipo"))
        add(ScreenSpec("s27", 3, "Rosa IA", "Rosa IA · ¿Cómo te ayudo?", "Elige si quieres armar desde cero, inspirarte o ajustar a tu presupuesto", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_ROSA_CHAT, RfPhotoKey.BODA, "Armar lista completa", "Asistencia IA"))
        add(ScreenSpec("s28", 3, "Rosa IA", "Rosa IA · Invitados y espacio", "120 invitados en Jardín Botánico · Calculando mesas de 8 personas y pasillos", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_ROSA_CHAT, RfPhotoKey.MESA_REDONDA, "Confirmar 120 invitados", "120 invitados"))
        add(ScreenSpec("s29", 3, "Rosa IA", "Rosa IA · Sugerencias", "Combinación recomendada en Rosa Empolvado, Dorado Cálido y Marfil", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_ROSA_CHAT, RfPhotoKey.ARCO_ROSA, "Ver categorías sugeridas", "Moodboard IA"))
        add(ScreenSpec("s30", 3, "Rosa IA", "Rosa IA · Categorías", "120 Sillas Tiffany · 15 Mesas redondas · 8 Centros florales · 1 Arco · 1 Mesa dulce", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_ROSA_CHAT, RfPhotoKey.CENTRO_FLORAL, "Revisar detalles", "5 categorías"))
        add(ScreenSpec("s31", 3, "Rosa IA", "Rosa IA · Detalles", "Viernes 23 oct · Jardín Botánico · Montaje 9:00 a. m. · Ajusta cantidades", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_ROSA_CHAT, RfPhotoKey.BODA, "Ver resumen de Rosa IA", "145 artículos"))
        add(ScreenSpec("s32", 3, "Rosa IA", "Rosa IA · Resumen", "Total estimado RD$ 4,500 · Anticipo 30 % RD$ 1,350 · Saldo RD$ 3,150", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_ROSA_CHAT, RfPhotoKey.BODA, "Enviar solicitud de cotización", "RD$ 4,500"))
        add(ScreenSpec("s33", 3, "Rosa IA", "Solicitud enviada", "¡Listo, Camila! María recibió tu propuesta de Boda Real Rosa & Dorado", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PAYMENT, RfPhotoKey.BODA, "Ir a mis eventos", "COT-2026-1023"))
        add(ScreenSpec("s34", 3, "Rosa IA", "Rosa IA · Chat", "Conversación libre con Rosa IA para ajustar flores, mantelería y cantidades", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_ROSA_CHAT, RfPhotoKey.BODA, "Aplicar cambios sugeridos", "Chat en vivo"))
        add(ScreenSpec("s35", 3, "Rosa IA", "Rosa IA · Modo voz", "Habla con Rosa IA usando micrófono en vivo mientras recorres el salón", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_ROSA_CHAT, RfPhotoKey.BODA, "Terminar nota de voz", "Escuchando…"))
        add(ScreenSpec("s36", 3, "Rosa IA", "Boceto del evento", "Distribuye las 15 mesas redondas, pista de baile, mesa dulce y arco en el plano", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_ROSA_CHAT, RfPhotoKey.MESA_REDONDA, "Confirmar boceto", "15 mesas · 120 inv."))

        // 4.1 Mis eventos (`s37`–`s47`)
        add(ScreenSpec("s37", 4, "Mis eventos", "Mis eventos", "Boda Real Rosa & Dorado (Vie 23 oct) y Quinceañera Valentina Crystal (Lun 2 nov)", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_EVENT_HUB, RfPhotoKey.BODA, "Abrir Boda Real", "2 eventos"))
        add(ScreenSpec("s38", 4, "Mis eventos", "Nuevo evento", "Crea un evento indicando nombre, fecha, salón en Santo Domingo e invitados", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_EVENT_HUB, RfPhotoKey.BODA, "Guardar y elegir artículos", "Crear evento"))
        add(ScreenSpec("s39", 4, "Mis eventos", "Calendario", "Fechas clave: jue 22 oct vence saldo · vie 23 oct Boda Real · lun 2 nov Quinceañera", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_EVENT_HUB, RfPhotoKey.BODA, "Ver detalle del vie 23 oct", "Oct – Nov"))
        add(ScreenSpec("s40", 4, "Mis eventos", "Detalle del evento", "Boda Real Rosa & Dorado · Vie 23 oct · Jardín Botánico · Menú ⋮ para posponer o cancelar", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_EVENT_HUB, RfPhotoKey.BODA, "Abrir modo ejecución", "RD$ 4,500"))
        add(ScreenSpec("s41", 4, "Mis eventos", "Chat del evento", "Conversación directa con María (Dueña) y Pedro (Líder de montaje)", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_EVENT_HUB, RfPhotoKey.BODA, "Enviar mensaje al equipo", "En línea"))
        add(ScreenSpec("s42", 4, "Mis eventos", "Chat · Fotos y acuerdos", "Evidencias del montaje en Jardín Botánico y confirmación de mantelería", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_EVENT_HUB, RfPhotoKey.ARCO_ROSA, "Confirmar acabado floral", "4 fotos"))
        add(ScreenSpec("s43", 4, "Mis eventos", "Modo ejecución", "Seguimiento en vivo del día del evento: camión en ruta, descarga y montaje", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_EVENT_HUB, RfPhotoKey.BODA, "Ver cronograma hora por hora", "Montaje 66 %"))
        add(ScreenSpec("s44", 4, "Mis eventos", "Cronograma", "08:15 Carga · 09:00 Montaje · 02:00 p. m. Entrega floral · 11:30 p. m. Recogida", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_EVENT_HUB, RfPhotoKey.BODA, "Ver tareas del evento", "5 hitos"))
        add(ScreenSpec("s45", 4, "Mis eventos", "Tareas", "Responsabilidades compartidas entre Camila, María, Pedro y Yuli", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_EVENT_HUB, RfPhotoKey.BODA, "Marcar tarea completada", "6 de 8 listas"))
        add(ScreenSpec("s46", 4, "Mis eventos", "Checklist", "Verificación de las 145 piezas entregadas en Jardín Botánico sin faltantes", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_EVENT_HUB, RfPhotoKey.SILLA_TIFFANY, "Firmar conformidad de entrega", "145 artículos"))
        add(ScreenSpec("s47", 4, "Mis eventos", "Invitados", "120 invitados confirmados distribuidos en 15 mesas redondas de 8 puestos", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_EVENT_HUB, RfPhotoKey.MESA_REDONDA, "Asignar mesa a invitado", "120 invitados"))

        // 4.1 Cotización y pago (`s48`–`s53`)
        add(ScreenSpec("s48", 5, "Cotización y pago", "Cotización", "Boda Real Rosa & Dorado · Total RD$ 4,500 · Anticipo 30 % RD$ 1,350 · Saldo RD$ 3,150", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CART_QUOTE, RfPhotoKey.BODA, "Proceder al pago", "RD$ 4,500"))
        add(ScreenSpec("s49", 5, "Cotización y pago", "Historial de cotizaciones", "Versiones v1 y v2 de Boda Real Rosa & Dorado y Quinceañera Valentina Crystal", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CART_QUOTE, RfPhotoKey.QUINCEANERA, "Abrir versión vigente", "3 cotizaciones"))
        add(ScreenSpec("s50", 5, "Cotización y pago", "Pago", "Abona el anticipo de RD$ 1,350 (30 %) o liquida el saldo de RD$ 3,150 por transferencia", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PAYMENT, RfPhotoKey.BODA, "Confirmar pago RD$ 1,350", "RD$ 1,350"))
        add(ScreenSpec("s51", 5, "Cotización y pago", "Pago confirmado", "¡Pago registrado con éxito! Anticipo de RD$ 1,350 (transferencia, 18 oct)", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PAYMENT, RfPhotoKey.BODA, "Ver recibo oficial", "Confirmado"))
        add(ScreenSpec("s52", 5, "Cotización y pago", "Post-evento", "¡Gracias por celebrar tu Boda Real con RosaFiesta! Desmontaje completado sin cargos", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_EVENT_HUB, RfPhotoKey.BODA, "Dejar reseña del evento", "Finalizado"))
        add(ScreenSpec("s53", 5, "Cotización y pago", "Reservas", "Resumen de alquileres activos y fechas bloqueadas a nombre de Camila Rodríguez", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CART_QUOTE, RfPhotoKey.BODA, "Ver contrato de reserva", "2 activas"))

        // 4.1 Cuenta (`s54`)
        add(ScreenSpec("s54", 6, "Cuenta", "Perfil", "Camila Rodríguez · camila@rosafiesta.do · Mis pagos, ajustes y ayuda", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PROFILE, RfPhotoKey.BODA, "Ir a mis pagos", "Cliente VIP"))

        // 4.1 Hojas, flujos secundarios y Sección 5 Estados (`s75`–`s92`)
        add(ScreenSpec("s75", 7, "Hojas y estados", "Inicia sesión para continuar", "Hoja inferior para invitados: guarda favoritos, cotiza con Rosa IA y reserva tu fecha", RfThemeVariant.PERSONAL, ScreenArchetype.AUTH_FORM, RfPhotoKey.BODA, "Iniciar sesión ahora", "Hoja · Invitado"))
        add(ScreenSpec("s76", 7, "Hojas y estados", "Cómo planificar", "Hoja inferior con los 4 pasos: elige fecha, diseña con Rosa IA, abona 30 % y disfruta", RfThemeVariant.PERSONAL, ScreenArchetype.ONBOARDING, RfPhotoKey.BODA, "Empezar mi evento", "Guía rápida"))
        add(ScreenSpec("s77", 7, "Hojas y estados", "Reseña del evento", "Califica la puntualidad de Pedro, el montaje floral de Yuli y el estado del mobiliario", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_EVENT_HUB, RfPhotoKey.BODA, "Publicar reseña 5 ★", "★★★★★"))
        add(ScreenSpec("s78", 7, "Hojas y estados", "Confirmar boceto", "Plano validado para 120 invitados en Jardín Botánico · Incluye botón «Ayúdame con IA»", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_ROSA_CHAT, RfPhotoKey.MESA_REDONDA, "Ayúdame con IA", "Boceto listo"))
        add(ScreenSpec("s79", 7, "Hojas y estados", "Notificaciones", "Avisos de tu Boda Real: saldo RD$ 3,150 vence el jue 22 oct y camión asignado", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PROFILE, RfPhotoKey.BODA, "Pagar saldo pendiente", "3 nuevas"))
        add(ScreenSpec("s80", 7, "Hojas y estados", "Resultados de búsqueda", "12 coincidencias para «dorado» · Silla Tiffany RD$ 4, Centro floral RD$ 35, Arco RD$ 95", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CATALOG, RfPhotoKey.SILLA_TIFFANY, "Abrir filtros", "12 resultados"))
        add(ScreenSpec("s81", 7, "Hojas y estados", "Filtros", "Hoja inferior de filtros por fecha (vie 23 oct), categoría, color y rango de precio", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_CATALOG, RfPhotoKey.SILLA_TIFFANY, "Aplicar filtros (12)", "Hoja · Filtros"))
        add(ScreenSpec("s82", 7, "Hojas y estados", "Mis pagos", "Anticipo Boda Real RD$ 1,350 (18 oct) · Quinceañera RD$ 1,860 (15 oct) · Saldos pendientes", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PAYMENT, RfPhotoKey.BODA, "Abrir recibo de pago", "RD$ 3,210 pagados"))
        add(ScreenSpec("s83", 7, "Hojas y estados", "Recibo", "Comprobante oficial #REC-2026-0184 · Anticipo 30 % por transferencia el 18 oct", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PAYMENT, RfPhotoKey.BODA, "Descargar recibo PDF", "RD$ 1,350"))
        add(ScreenSpec("s84", 7, "Hojas y estados", "Cambiar fecha", "Hoja inferior: el primer cambio de fecha es gratis con más de 7 días de anticipación", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_DATE_CANCEL, RfPhotoKey.BODA, "Confirmar nueva fecha gratis", "1er cambio gratis"))
        add(ScreenSpec("s85", 7, "Hojas y estados", "Cancelar evento", "Hoja inferior: >7 días 90 % reembolso · 3 a 7 días 50 % · menos de 3 días sin reembolso", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_DATE_CANCEL, RfPhotoKey.BODA, "Solicitar cancelación", "Política 90/50/0 %"))
        add(ScreenSpec("s86", 7, "Hojas y estados", "Ajustes", "Idioma español, notificaciones push, moneda RD$, seguridad y privacidad de cuenta", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PROFILE, RfPhotoKey.BODA, "Guardar ajustes", "Preferencias"))
        add(ScreenSpec("s87", 7, "Hojas y estados", "Términos y privacidad", "Política de alquiler, depósito de garantía, cancelaciones y tratamiento de datos", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PROFILE, RfPhotoKey.BODA, "Aceptar condiciones", "Legal (ejemplo)"))
        add(ScreenSpec("s88", 7, "Hojas y estados", "Eliminar cuenta", "Se valida primero que no haya eventos activos · Botón deshabilitado al 35 % de opacidad", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_PROFILE, RfPhotoKey.BODA, "Eliminar mi cuenta", "35 % deshabilitado"))
        add(ScreenSpec("s89", 7, "Hojas y estados", "Estado vacío · Mis eventos", "Aún no tienes eventos creados · Ilustración de globos, título 22/800 y dos acciones", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_SYSTEM_STATE, RfPhotoKey.ARCO_ROSA, "Crear evento con Rosa IA", "Estado vacío"))
        add(ScreenSpec("s90", 7, "Hojas y estados", "Estado cargando", "Esqueleto con la misma estructura de la pantalla y brillo de 1.2 s de izquierda a derecha", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_SYSTEM_STATE, RfPhotoKey.SILLA_TIFFANY, "Cargando…", "Brillo 1.2 s"))
        add(ScreenSpec("s91", 7, "Hojas y estados", "Sin conexión y error", "Banner oscuro arriba con wifi tachado, guardado local y «Algo salió mal de nuestro lado…»", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_SYSTEM_STATE, RfPhotoKey.BODA, "Reintentar", "Offline / Error"))
        add(ScreenSpec("s92", 7, "Hojas y estados", "Estado de éxito · Toast", "Toast sobre la barra inferior (#1A1A2E) con círculo lima ✓ y acción «Deshacer» en #FFD45C", RfThemeVariant.PERSONAL, ScreenArchetype.PERSONAL_SYSTEM_STATE, RfPhotoKey.BODA, "Ver mi selección", "Toast 4 s"))
    }

    val adminScreens: List<ScreenSpec> = buildList {
        // 4.2 Entrada (`A00`, `A30`, `A01`, `A29`)
        add(ScreenSpec("A00", 8, "Entrada", "Splash · RosaFiesta Admin", "Gestión integral de eventos, inventario, cotizaciones y Rosa IA en índigo→turquesa", RfThemeVariant.ADMIN, ScreenArchetype.SPLASH, RfPhotoKey.CORPORATIVO, "Entrar", "App Negocio"))
        add(ScreenSpec("A30", 8, "Entrada", "Elegir cómo entrar", "Entra como Dueña (María), Equipo (Pedro, Yuli, Andrés) o revisa la App Personal como cliente", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_ROLE_SWITCHER, RfPhotoKey.CORPORATIVO, "Entrar como María (Admin)", "Roles y vista cliente"))
        add(ScreenSpec("A01", 8, "Entrada", "Inicio", "Ingresos oct RD$ 12,450 · Por cobrar RD$ 7,724 · 4 eventos activos · 2 solicitudes nuevas", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_DASHBOARD, RfPhotoKey.BODA, "Ver solicitudes nuevas", "RD$ 12,450"))
        add(ScreenSpec("A29", 8, "Entrada", "Notificaciones", "Transferencia de Camila (RD$ 1,350), nueva solicitud de Laura Peña y aviso de stock", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_DASHBOARD, RfPhotoKey.BODA, "Atender notificaciones", "4 alertas"))

        // 4.2 Crear (`A04`, `A05`, `A06`, `A07`, `A08`, `A02`, `A03`, `A22`)
        add(ScreenSpec("A04", 9, "Crear", "Elegir modo de creación", "Crea un borrador asistido con Rosa IA (`A08`) o paso a paso en modo manual (`A05`–`A07`)", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_ROSA_COPILOT, RfPhotoKey.BODA, "Crear con Rosa IA", "IA o Manual"))
        add(ScreenSpec("A05", 9, "Crear", "Nuevo evento · Datos", "Cliente, tipo de evento, fecha, horario de montaje y salón en Santo Domingo", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_QUOTES, RfPhotoKey.BODA, "Continuar a artículos", "Paso 1 de 3"))
        add(ScreenSpec("A06", 9, "Crear", "Nuevo evento · Artículos", "Selecciona mobiliario con validación de stock: 120 Sillas Tiffany, 15 Mesas redondas", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_QUOTES, RfPhotoKey.SILLA_TIFFANY, "Ver resumen de cotización", "Paso 2 de 3"))
        add(ScreenSpec("A07", 9, "Crear", "Nuevo evento · Resumen", "Total RD$ 4,500 · Anticipo 30 % RD$ 1,350 · Saldo RD$ 3,150 vence el jue 22 oct", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_QUOTES, RfPhotoKey.BODA, "Guardar y enviar cotización", "Paso 3 de 3"))
        add(ScreenSpec("A08", 9, "Crear", "Crear con Rosa IA", "Rosa IA verifica stock, redacta la respuesta y arma el borrador de Quinceañera Valentina", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_ROSA_COPILOT, RfPhotoKey.QUINCEANERA, "Generar borrador RD$ 6,200", "Rosa IA Admin"))
        add(ScreenSpec("A02", 9, "Crear", "Solicitudes", "2 nuevas: Cumpleaños de Mateo (Laura Peña · RD$ 405) y Graduación Promo 2026 (RD$ 2,140)", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_QUOTES, RfPhotoKey.CUMPLE_INFANTIL, "Cotizar solicitud", "2 nuevas"))
        add(ScreenSpec("A03", 9, "Crear", "Cotizar solicitud", "Ajusta precios unitarios, transporte y genera enlace de pago del 30 % de anticipo", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_QUOTES, RfPhotoKey.CUMPLE_INFANTIL, "Enviar cotización a Laura", "Est. RD$ 405"))
        add(ScreenSpec("A22", 9, "Crear", "Historial de cotizaciones", "Cotizaciones emitidas, aceptadas y vencidas de octubre y noviembre 2026", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_QUOTES, RfPhotoKey.BODA, "Duplicar cotización", "14 emitidas"))

        // 4.2 Operar eventos (`A11`, `A13`, `A14`, `A15`, `A16`, `A17`, `A18`, `A19`)
        add(ScreenSpec("A11", 10, "Operar eventos", "Eventos", "4 activos: Boda Real (23 oct), Quinceañera (2 nov), Mateo (7 nov), Graduación (13 nov)", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_LOGISTICS, RfPhotoKey.BODA, "Abrir detalle de Boda Real", "4 activos"))
        add(ScreenSpec("A13", 10, "Operar eventos", "Detalle del evento", "Boda Real Rosa & Dorado · Camila Rodríguez · Vie 23 oct · Jardín Botánico · 145 artículos", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_LOGISTICS, RfPhotoKey.BODA, "Iniciar modo ejecución", "RD$ 4,500"))
        add(ScreenSpec("A14", 10, "Operar eventos", "Bandeja de chats", "Conversaciones activas con Camila Rodríguez, Laura Peña, Ana Gómez y Grupo Caribe", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_LOGISTICS, RfPhotoKey.BODA, "Abrir chat con Camila", "3 sin leer"))
        add(ScreenSpec("A15", 10, "Operar eventos", "Chat del evento", "Hilo operativo con Camila Rodríguez y notas internas para Pedro y Yuli", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_ROSA_COPILOT, RfPhotoKey.BODA, "Redactar respuesta con Rosa IA", "Boda Real"))
        add(ScreenSpec("A16", 10, "Operar eventos", "Ejecución", "Control en sitio en Jardín Botánico: carga verificada, montaje 66 % y fotos de entrega", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_LOGISTICS, RfPhotoKey.ARCO_ROSA, "Marcar etapa completada", "En montaje"))
        add(ScreenSpec("A17", 10, "Operar eventos", "Tareas del evento", "Asignación por rol: Pedro (camión y mesas), Yuli (arco y flores), Andrés (conteo)", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_LOGISTICS, RfPhotoKey.BODA, "Completar tarea de montaje", "8 tareas"))
        add(ScreenSpec("A18", 10, "Operar eventos", "Boceto → plano", "Convierte el boceto de Camila en plano técnico con medidas y puntos eléctricos", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_LOGISTICS, RfPhotoKey.MESA_REDONDA, "Aprobar plano de montaje", "15 mesas · 120 inv."))
        add(ScreenSpec("A19", 10, "Operar eventos", "Cierre del evento", "Recogida 11:30 p. m., conteo de retorno a bodega y liquidación final sin daños", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_LOGISTICS, RfPhotoKey.BODA, "Cerrar evento y liberar stock", "145/145 devueltos"))

        // 4.2 Dinero (`A20`, `A21`, `A32`, `A33`)
        add(ScreenSpec("A20", 11, "Dinero", "Pagos", "Ingresos oct RD$ 12,450 · Por cobrar RD$ 7,724 (Boda Real RD$ 3,150 + Quinceañera RD$ 4,340)", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_FINANCE, RfPhotoKey.CORPORATIVO, "Registrar nuevo pago", "RD$ 12,450"))
        add(ScreenSpec("A21", 11, "Dinero", "Registrar pago", "Acredita transferencia Banreservas (RD$ 1,350, 18 oct) o efectivo (RD$ 1,860, 15 oct)", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_FINANCE, RfPhotoKey.BODA, "Guardar y emitir recibo", "RD$ 1,350"))
        add(ScreenSpec("A32", 11, "Dinero", "Cancelaciones", "Solicitudes de cancelación con cálculo según política (>7d 90 %, 3-7d 50 %, <3d 0 %)", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_FINANCE, RfPhotoKey.BODA, "Revisar reembolso", "Política 90/50/0 %"))
        add(ScreenSpec("A33", 11, "Dinero", "Procesar reembolso", "Devolución del 90 % de anticipo (>7 días antes) por transferencia a cuenta del cliente", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_FINANCE, RfPhotoKey.BODA, "Confirmar reembolso", "RD$ 1,215"))

        // 4.2 Catálogo (`A09`, `A10`, `A23`, `A24`, `A25`, `s63`)
        add(ScreenSpec("A09", 12, "Catálogo", "Inventario", "Silla Tiffany (RD$ 4) · Mesa plegable (RD$ 20) · Mesa redonda (RD$ 25) · Arco (RD$ 95)", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_INVENTORY_STAFF, RfPhotoKey.SILLA_TIFFANY, "Nuevo artículo", "358 unidades"))
        add(ScreenSpec("A10", 12, "Catálogo", "Nuevo artículo", "Registra foto oficial, categoría, código RF, stock total y precio de alquiler por día", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_INVENTORY_STAFF, RfPhotoKey.MESA_PLEGABLE, "Guardar en inventario", "Alta de SKU"))
        add(ScreenSpec("A23", 12, "Catálogo", "Artículo", "Silla Tiffany dorada · Stock total 240 · Reservadas 23 oct: 120 · En mantenimiento: 4", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_INVENTORY_STAFF, RfPhotoKey.SILLA_TIFFANY, "Actualizar stock y precio", "RD$ 4/día"))
        add(ScreenSpec("A24", 12, "Catálogo", "Paquetes", "Boda Real (RD$ 4,500), Quinceañera Crystal (RD$ 6,200), Gala Corporativa (RD$ 8,500)", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_INVENTORY_STAFF, RfPhotoKey.BODA, "Editar paquete", "4 paquetes"))
        add(ScreenSpec("A25", 12, "Catálogo", "Editar paquete", "Configura piezas incluidas, margen de descuento y fotos de portada del paquete", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_INVENTORY_STAFF, RfPhotoKey.BODA, "Guardar paquete", "RD$ 4,500"))
        add(ScreenSpec("s63", 12, "Catálogo", "Disponibilidad", "Consulta de stock libre y comprometido por fecha para evitar sobreventa", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_CALENDAR, RfPhotoKey.SILLA_TIFFANY, "Bloquear stock para fecha", "Vie 23 oct"))

        // 4.2 Personas (`A26`, `A27`, `A28`)
        add(ScreenSpec("A26", 13, "Personas", "Clientes", "Camila Rodríguez, Laura Peña, Ana Gómez y Grupo Caribe con historial y saldos", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_INVENTORY_STAFF, RfPhotoKey.BODA, "Abrir ficha de Camila", "4 clientes"))
        add(ScreenSpec("A27", 13, "Personas", "Ficha de cliente", "Camila Rodríguez · 2 eventos (Boda Real y Quinceañera) · Saldo total RD$ 7,490", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_INVENTORY_STAFF, RfPhotoKey.BODA, "Enviar estado de cuenta", "RD$ 10,700 total"))
        add(ScreenSpec("A28", 13, "Personas", "Equipo y permisos", "María (Admin total), Pedro (Logística), Yuli (Decoración y tareas, sin pagos), Andrés (Bodega)", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_INVENTORY_STAFF, RfPhotoKey.CORPORATIVO, "Guardar permisos del equipo", "4 roles"))

        // 4.2 Más (`A12`, `s55`–`s62`, `s64`–`s74`, `A34`, `A31`, `A39`)
        add(ScreenSpec("A12", 14, "Más", "Más", "Panel ejecutivo, reportes, leads, proveedores, reseñas, bot de WhatsApp y versión web", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_DASHBOARD, RfPhotoKey.CORPORATIVO, "Abrir panel ejecutivo", "Herramientas"))
        add(ScreenSpec("s55", 14, "Más", "Proveedores", "Florería Jardín Real, Transporte Del Caribe, Globos & Arte RD y Mantelería Imperial", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_INVENTORY_STAFF, RfPhotoKey.CENTRO_FLORAL, "Ver ficha de proveedor", "6 proveedores"))
        add(ScreenSpec("s56", 14, "Más", "Detalle de proveedor", "Florería Jardín Real · Entrega de rosas frescas para Boda Real el vie 23 oct 07:00 a. m.", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_INVENTORY_STAFF, RfPhotoKey.CENTRO_FLORAL, "Emitir orden de compra", "RD$ 680"))
        add(ScreenSpec("s57", 14, "Más", "Artículos más rentados", "Ranking de rotación e ingresos por pieza: Silla Tiffany, Mesa redonda y Arco orgánico", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_FINANCE, RfPhotoKey.SILLA_TIFFANY, "Ver rotación completa", "92 % rotación"))
        add(ScreenSpec("s58", 14, "Más", "Panel · Ingresos", "Evolución de ingresos de octubre: RD$ 12,450 cobrados (+18 % vs septiembre)", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_DASHBOARD, RfPhotoKey.CORPORATIVO, "Ver desglose por evento", "RD$ 12,450"))
        add(ScreenSpec("s59", 14, "Más", "Panel · Por cobrar", "Saldos próximos a vencer: RD$ 3,150 (jue 22 oct) y RD$ 4,340 (vie 30 oct)", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_FINANCE, RfPhotoKey.BODA, "Enviar recordatorios", "RD$ 7,724"))
        add(ScreenSpec("s60", 14, "Más", "Panel · Eventos activos", "Mapa de los 4 eventos activos en Santo Domingo y estado de preparación", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_DASHBOARD, RfPhotoKey.BODA, "Ver agenda operativa", "4 activos"))
        add(ScreenSpec("s61", 14, "Más", "Panel · Calendario", "Ocupación mensual de octubre y noviembre con alertas de días pico", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_CALENDAR, RfPhotoKey.BODA, "Explorar semana del 23 oct", "Oct – Nov"))
        add(ScreenSpec("s62", 14, "Más", "Panel · Alertas de stock", "4 sillas Tiffany en retoque de pintura y reposición de bases de cristal", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_INVENTORY_STAFF, RfPhotoKey.SILLA_TIFFANY, "Liberar mantenimiento", "2 alertas"))
        add(ScreenSpec("s64", 14, "Más", "Leads · Embudo comercial", "Prospectos de Instagram, WhatsApp y web en etapas: Nuevo, Contactado y Cotizado", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_QUOTES, RfPhotoKey.QUINCEANERA, "Abrir prospecto", "9 leads activos"))
        add(ScreenSpec("s65", 14, "Más", "Leads · Ficha de prospecto", "Laura Peña · Cumpleaños de Mateo · Sáb 7 nov · 40 invitados · Est. RD$ 405", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_QUOTES, RfPhotoKey.CUMPLE_INFANTIL, "Convertir en cotización", "Est. RD$ 405"))
        add(ScreenSpec("s66", 14, "Más", "Leads · Seguimiento", "Recordatorios automáticos con Rosa IA para cerrar cotizaciones pendientes", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_ROSA_COPILOT, RfPhotoKey.GRADUACION, "Enviar seguimiento IA", "Ana Gómez · RD$ 2,140"))
        add(ScreenSpec("s67", 14, "Más", "Reportes · Financiero", "Ingresos RD$ 12,450 · Costos operativos RD$ 3,210 · Utilidad neta RD$ 9,240", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_FINANCE, RfPhotoKey.CORPORATIVO, "Ver balance detallado", "Margen 74 %"))
        add(ScreenSpec("s68", 14, "Más", "Reportes · Uso de inventario", "Tasa de utilización por categoría y retorno sobre inversión de mobiliario", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_FINANCE, RfPhotoKey.MESA_REDONDA, "Analizar categoría", "84 % uso"))
        add(ScreenSpec("s69", 14, "Más", "Reportes · Equipo y entregas", "Puntualidad de montajes (98 %) y tareas completadas por Pedro, Yuli y Andrés", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_LOGISTICS, RfPhotoKey.BODA, "Ver métricas por integrante", "98 % puntualidad"))
        add(ScreenSpec("s70", 14, "Más", "Reportes · Exportar", "Descarga reportes en PDF, Excel y CSV para contabilidad e impuestos DGII", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_FINANCE, RfPhotoKey.CORPORATIVO, "Exportar corte de octubre", "PDF / CSV"))
        add(ScreenSpec("s71", 14, "Más", "Reseñas", "Calificaciones verificadas de clientes (4.9 ★) con fotos del evento terminado", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_DASHBOARD, RfPhotoKey.BODA, "Responder reseña de Camila", "4.9 ★"))
        add(ScreenSpec("A34", 14, "Más", "Responder reseña", "Redacta o aprueba con Rosa IA el agradecimiento público a Camila Rodríguez", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_ROSA_COPILOT, RfPhotoKey.BODA, "Publicar respuesta", "5 ★ · Boda Real"))
        add(ScreenSpec("s72", 14, "Más", "Bot de WhatsApp · Estado", "Conexión activa con +1 (809) 555-0199 · Respuesta automática 24/7 con Rosa IA", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_ROSA_COPILOT, RfPhotoKey.CORPORATIVO, "Configurar horario del bot", "Conectado"))
        add(ScreenSpec("s73", 14, "Más", "Bot de WhatsApp · Plantillas", "Mensajes automáticos de bienvenida, catálogo de precios y recordatorio de saldo", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_ROSA_COPILOT, RfPhotoKey.BODA, "Guardar plantilla", "6 plantillas"))
        add(ScreenSpec("s74", 14, "Más", "Bot de WhatsApp · Conversaciones", "Consultas atendidas por el bot y traspaso a María cuando piden cotización formal", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_ROSA_COPILOT, RfPhotoKey.CUMPLE_INFANTIL, "Tomar conversación", "12 hoy"))
        add(ScreenSpec("A31", 14, "Más", "Ajustes del negocio", "Datos fiscales, cuentas Banreservas/Popular, anticipo 30 % y política 90/50/0 %", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_INVENTORY_STAFF, RfPhotoKey.CORPORATIVO, "Guardar ajustes", "Anticipo 30 %"))
        add(ScreenSpec("A39", 14, "Más", "Panel Administrativo Web", "Resumen operativo, eventos activos y disponibilidad de inventario", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_WEB_DESKTOP, RfPhotoKey.CORPORATIVO, "Exportar reporte ejecutivo", "1440×900 Web", isDesktopWeb = true))

        // 5 Estados (Admin `A35`–`A38`)
        add(ScreenSpec("A35", 15, "Estados", "Estado vacío · Solicitudes", "No tienes solicitudes nuevas por cotizar · Icono grande, título 22/800 y acción", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_QUOTES, RfPhotoKey.SILLA_TIFFANY, "Crear cotización manual", "Estado vacío"))
        add(ScreenSpec("A36", 15, "Estados", "Estado cargando", "Esqueleto administrativo con brillo de 1.2 s de izquierda a derecha sin spinners", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_DASHBOARD, RfPhotoKey.CORPORATIVO, "Sincronizando…", "Brillo 1.2 s"))
        add(ScreenSpec("A37", 15, "Estados", "Sin conexión y error · Inventario", "Banner oscuro arriba, «Reintentar», guardado local y «Algo salió mal de nuestro lado…»", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_OFFLINE_SYNC, RfPhotoKey.SILLA_TIFFANY, "Reintentar", "Offline / Error"))
        add(ScreenSpec("A38", 15, "Estados", "Estado de éxito · Toast superior", "Detalle del evento con toast superior (#151729), círculo lima ✓ y acción «Deshacer»", RfThemeVariant.ADMIN, ScreenArchetype.ADMIN_LOGISTICS, RfPhotoKey.BODA, "Ver cambios guardados", "Toast 4 s"))
    }

    val allScreens: List<ScreenSpec> = personalScreens + adminScreens

    private val byCode: Map<String, ScreenSpec> = allScreens.associateBy { it.code }

    fun findByCode(code: String): ScreenSpec = byCode[code] ?: personalScreens.first()

    /**
     * Indica si la pantalla es una hoja inferior modal (`RfBottomSheet`) según las secciones 3 y 4.1.
     */
    fun isBottomSheet(code: String): Boolean = code in setOf("s75", "s76", "s77", "s81", "s84", "s85")

    /**
     * Indica si la pantalla usa la barra de navegación en modo invitado (`Inicio · Catálogo · Rosa · Paquetes · Entrar`).
     */
    fun isGuestScreen(code: String): Boolean = code in setOf("s01", "s02", "s75")

    /**
     * Pestaña activa de `PersonalTabBar` para un código de pantalla de la App Personal.
     */
    fun activePersonalTab(code: String): PersonalTab = when (code) {
        "s01", "s18", "s19", "s20", "s23", "s80", "s81" -> PersonalTab.CATALOGO

        "s21", "s22" -> if (isGuestScreen(code)) PersonalTab.PAQUETES else PersonalTab.CATALOGO

        "s02", "s24" -> PersonalTab.FAVORITOS

        "s26", "s27", "s28", "s29", "s30", "s31", "s32", "s33", "s34", "s35", "s36", "s78" -> PersonalTab.ROSA

        "s25", "s37", "s38", "s39", "s40", "s41", "s42", "s43", "s44", "s45", "s46", "s47",
        "s48", "s49", "s50", "s51", "s52", "s53", "s77", "s84", "s85", "s89",
        -> PersonalTab.EVENTOS

        else -> PersonalTab.INICIO
    }

    /**
     * Pestaña activa de `AdminTabBar` para un código de pantalla de la App Administrativa.
     */
    fun activeAdminTab(code: String): AdminTab = when (code) {
        "A01", "A02", "A03", "A22", "A29", "A35", "A36" -> AdminTab.INICIO

        "A04", "A05", "A06", "A07", "A11", "A13", "A14", "A15", "A16", "A17", "A18", "A19",
        "A20", "A21", "A32", "A33", "A38",
        -> AdminTab.EVENTOS

        "A08", "A34", "s66", "s72", "s73", "s74" -> AdminTab.ROSA

        "A09", "A10", "A23", "A24", "A25", "s63", "A37" -> AdminTab.INVENTARIO

        else -> AdminTab.MAS
    }

    /**
     * Destino principal al pulsar la acción primaria o navegar en los flujos de las secciones 4.1 y 4.2.
     */
    fun defaultNextCode(code: String): String = when (code) {
        // 4.1 Acceso
        "p00" -> "s03"

        "s03" -> "s04"

        "s04" -> "s05"

        "s05" -> "s06"

        "s06" -> "s07"

        "s07" -> "s11"

        "s08" -> "s11"

        "s09" -> "s10"

        "s10" -> "s07"

        "s11" -> "s12"

        "s12" -> "s13"

        "s13" -> "s14"

        "s01" -> "s75"

        "s02" -> "s75"

        "s75" -> "s07"

        // 4.1 Descubrir
        "s14" -> "s40"

        "s15" -> "s26"

        "s16" -> "s17"

        "s17" -> "s18"

        "s18" -> "s80"

        "s80" -> "s81"

        "s81" -> "s19"

        "s19" -> "s25"

        "s20" -> "s18"

        "s21" -> "s22"

        "s22" -> "s25"

        "s23" -> "s25"

        "s24" -> "s25"

        "s25" -> "s48"

        // 4.1 Rosa IA
        "s26" -> "s27"

        "s27" -> "s28"

        "s28" -> "s29"

        "s29" -> "s30"

        "s30" -> "s31"

        "s31" -> "s32"

        "s32" -> "s33"

        "s33" -> "s37"

        "s34" -> "s32"

        "s35" -> "s32"

        "s36" -> "s78"

        "s78" -> "s32"

        // 4.1 Mis eventos
        "s37" -> "s40"

        "s38" -> "s40"

        "s39" -> "s40"

        "s40" -> "s43"

        "s41" -> "s42"

        "s42" -> "s40"

        "s43" -> "s44"

        "s44" -> "s45"

        "s45" -> "s46"

        "s46" -> "s47"

        "s47" -> "s40"

        "s84" -> "s40"

        "s85" -> "s37"

        // 4.1 Cotización y pago
        "s48" -> "s50"

        "s49" -> "s48"

        "s50" -> "s51"

        "s51" -> "s52"

        "s52" -> "s77"

        "s53" -> "s48"

        "s77" -> "s37"

        // 4.1 Cuenta
        "s54" -> "s82"

        "s82" -> "s83"

        "s83" -> "s54"

        "s86" -> "s87"

        "s87" -> "s88"

        "s88" -> "s54"

        "s76" -> "s26"

        "s79" -> "s50"

        "s89" -> "s38"

        "s90" -> "s18"

        "s91" -> "s14"

        "s92" -> "s25"

        // 4.2 Admin
        "A00" -> "A30"

        "A30" -> "A01"

        "A01" -> "A02"

        "A29" -> "A01"

        "A04" -> "A08"

        "A05" -> "A06"

        "A06" -> "A07"

        "A07" -> "A11"

        "A08" -> "A07"

        "A02" -> "A03"

        "A03" -> "A22"

        "A22" -> "A02"

        "A11" -> "A13"

        "A13" -> "A16"

        "A14" -> "A15"

        "A15" -> "A13"

        "A16" -> "A17"

        "A17" -> "A18"

        "A18" -> "A19"

        "A19" -> "A38"

        "A20" -> "A21"

        "A21" -> "A20"

        "A32" -> "A33"

        "A33" -> "A20"

        "A09" -> "A23"

        "A10" -> "A09"

        "A23" -> "A09"

        "A24" -> "A25"

        "A25" -> "A24"

        "s63" -> "A09"

        "A26" -> "A27"

        "A27" -> "A26"

        "A28" -> "A12"

        "A12" -> "s58"

        "s55" -> "s56"

        "s56" -> "s55"

        "s57" -> "A09"

        "s58" -> "s59"

        "s59" -> "s60"

        "s60" -> "s61"

        "s61" -> "s62"

        "s62" -> "A12"

        "s64" -> "s65"

        "s65" -> "s66"

        "s66" -> "A03"

        "s67" -> "s68"

        "s68" -> "s69"

        "s69" -> "s70"

        "s70" -> "A12"

        "s71" -> "A34"

        "A34" -> "s71"

        "s72" -> "s73"

        "s73" -> "s74"

        "s74" -> "A12"

        "A31" -> "A12"

        "A39" -> "A01"

        "A35" -> "A04"

        "A36" -> "A01"

        "A37" -> "A09"

        "A38" -> "A13"

        else -> "s14"
    }
}
