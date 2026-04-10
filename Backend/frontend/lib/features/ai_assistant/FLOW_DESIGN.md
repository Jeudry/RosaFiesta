# AI Assistant — Guided Flow Design

## Steps

### Step 1: Tipo de evento
- Pregunta: "¿Qué evento quieres celebrar?"
- Opciones: Cards con fotos (Boda, Cumpleaños, Baby Shower, Gender Reveal, Corporativo, Otro)

### Step 1.5: Tutorial de input
- La IA dice algo como: "Si en cualquier momento quieres darme más detalles sobre lo que buscas, puedes usar los botones de **Hablar** o **Chatear** de abajo."
- Pequeña animación/highlight en los botones inferiores
- Botones: "Entendido" (gradient) / "No gracias" (outline) → ambos avanzan

### Step 2: ¿Buscar artículos?
- Pregunta: "¡[Evento]! Me encanta ✨ ¿Quieres que te ayude a elegir los artículos de alquiler desde aquí?"
- Botones: "No, por ahora" (outline) / "Sí, busquemos" (gradient)
- Si "No" → minimiza, deja mensaje pendiente
- Si "Sí" → avanza
- Recordatorio sutil: "Recuerda que puedes opinar por micrófono en cualquier momento"

### Step 3: Sugerencias de artículos
- Card con tabs de categorías + productos horizontales
- Botones: "Ver otras sugerencias" (blanco) / "Continuar" (gradient)
- "Ver otras sugerencias" → carga más productos
- "Continuar" → avanza al paso 4

### Step 4: Categorías que podrían faltar
- En vez de artículos, muestra **categorías** de cosas que le vendrían bien
- Ej: Sillas ✓ (ya elegido), Mesas (elegir más →), Iluminación, Mantelería, Centros de mesa...
- Checkmark en lo que ya se eligió
- Botón "elegir más" al lado de cada categoría
- Botones: "Continuar viendo sugerencias" (blanco) / "Siguiente: detalles" (gradient)

### Step 5: Detalles del evento
- Formulario con:
  - Fecha del evento (date picker)
  - Descripción adicional (text field)
  - Cantidad de personas (opcional)
  - Ubicación con helper de Google Maps
  - **Boceto de distribución** (opcional): el usuario puede dibujar un boceto 2D cenital del espacio y la IA generará ejemplos de distribución basados en ese boceto. Canvas simple de dibujo libre.
- Botones: "Continuar viendo sugerencias" (blanco) / "Resumen del pedido" (gradient)

### Step 6: Resumen del pedido
- Resume TODO: artículos elegidos + lo del carrito + detalles del evento
- Lista visual de items con cantidades y precios
- Total estimado
- Botones: "Agregar algo más" (blanco) / "Solicitar cotización" (gradient)

### Step 7: Finalizado (no es un paso, es un estado final)
- Pantalla de confirmación
- Mensaje: "¡Tu solicitud ha sido enviada! El equipo de RosaFiesta te cotizará y recibirás respuesta por correo y WhatsApp."
- Icono de check / confetti
- Botón: "Volver al inicio"

## Notas de UX
- Entre sugerencias, recordar al usuario que puede opinar por micrófono/chat
- Los botones de Hablar/Chatear en el bottom bar siempre están disponibles
- El progreso (step dots) refleja: evento → artículos → categorías → detalles → resumen → listo
