# Política de Privacidad — RosaFiesta SRL

**Última actualización:** 21 de octubre de 2026  
**Marco legal aplicable:** Ley No. 172-13 sobre Protección de Datos de Carácter Personal de la República Dominicana y Ley No. 358-05 General de Protección de los Derechos del Consumidor o Usuario (Pro Consumidor).

## 1. Responsable del Tratamiento
**RosaFiesta SRL**, con domicilio social en Santo Domingo, Distrito Nacional, República Dominicana (`privacidad@rosafiesta.do`), es responsable del tratamiento de los datos personales recolectados a través de la **App Personal RosaFiesta** y la **App Administrativa RosaFiesta**.

## 2. Datos Personales Recolectados
1. **Datos de identificación y contacto:** Nombre completo, correo electrónico, número telefónico y Cédula/RNC (únicamente cuando el cliente solicita comprobante fiscal NCF B01 ante la DGII).
2. **Datos logísticos del evento:** Fecha, horario de montaje/desmontaje, dirección del salón o residencia y cantidad de invitados.
3. **Datos de pago y conciliación:** Referencia de transferencia bancaria (Banco BHD, Banco Popular, Banreservas), últimos 4 dígitos de tarjeta procesada mediante pasarela certificada PCI-DSS o recibo de efectivo. **RosaFiesta nunca almacena números completos de tarjeta ni códigos CVV.**
4. **Firma digital del contrato (`s28`):** Trazo vectorial SVG vinculado al hash de la cotización aprobada y marca de tiempo UTC.

## 3. Censura y Protección de Datos Sensibles
- Todos los registros técnicos (`logs`) y respuestas de error del servidor aplican censura automática (`redact.Text`) que enmascara correos (`c***a@rosafiesta.do`), teléfonos (`809-***-**42`), tokens de sesión (`Bearer [REDACTED]`) y referencias bancarias (`BHD-***-41`).
- Las contraseñas se almacenan exclusivamente mediante hash criptográfico `bcrypt` irreversible.
- Los tokens de sesión en los dispositivos clientes se resguardan mediante `SecureStorage` cifrado (`AES-GCM` / Keychain / Keystore).

## 4. Derechos ARCO y Eliminación de Cuenta (`s88`)
El titular puede ejercer sus derechos de Acceso, Rectificación, Cancelación y Oposición (ARCO) o eliminar su cuenta directamente desde **Perfil → Eliminar cuenta (`s88`)**, sujeto únicamente a que no existan eventos activos o saldos pendientes de liquidación.
