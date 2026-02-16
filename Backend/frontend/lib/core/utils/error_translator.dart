class ErrorTranslator {
  static const Map<String, String> _translations = {
    // Auth
    'invalid credentials': 'Credenciales inválidas. Por favor verifique su correo y contraseña.',
    'record not found': 'Registro no encontrado.',
    'duplicate email': 'El correo electrónico ya está registrado.',
    'duplicate username': 'El nombre de usuario ya está en uso.',
    'unauthorized': 'No autorizado. Por favor inicie sesión nuevamente.',
    'token expired': 'Su sesión ha expirado.',
    
    // General
    'network error': 'Error de conexión. Verifique su internet.',
    'server error': 'Error interno del servidor. Intente más tarde.',
    'bad request': 'Solicitud inválida.',
    
    // Shop
    'out of stock': 'Producto agotado.',
    'insufficient quantity': 'Cantidad solicitada no disponible.',
  };

  static String translate(String originalError) {
    if (originalError.isEmpty) return 'Ha ocurrido un error desconocido.';

    final lowerCaseError = originalError.toLowerCase();
    
    // Check for exact matches first
    if (_translations.containsKey(lowerCaseError)) {
      return _translations[lowerCaseError]!;
    }

    // Check for partial matches (simple heuristic)
    for (final key in _translations.keys) {
      if (lowerCaseError.contains(key)) {
        return _translations[key]!;
      }
    }

    // Return original if no translation found, but capitalized
    return originalError.substring(0, 1).toUpperCase() + originalError.substring(1);
  }
}
