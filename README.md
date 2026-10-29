# Sistema de Reserva de Recursos

Proyecto #1 de EIF206 - Programación 3 (UNA Costa Rica). Aplicación de escritorio en Java (Swing + MVC) para gestionar reservas de recursos (salas, laptops, proyectores, etc.) de una organización.

## Requisitos previos


- Maven (o usar el que trae integrado IntelliJ)
- Conexión a internet (solo si vas a usar envío de correo real o la IA)

## Cómo ejecutar el programa

1. Abre el proyecto en IntelliJ (o cualquier IDE con soporte Maven).
2. Espera a que Maven descargue las dependencias (`pom.xml`).
3. Ejecuta la clase `Main.java` (paquete `cr.ac.una.eif206`).
4. Se abrirá la ventana de **Login**.

La primera vez que corres el programa se crean automáticamente:
- Una carpeta `data/` con los archivos XML donde se guarda toda la información.
- Un usuario administrador de prueba: **id `admin`, clave `admin1234`**.
- Categorías y recursos de ejemplo para poder probar Reservas de inmediato.

## Roles del sistema

El sistema tiene dos tipos de usuario, cada uno ve pestañas distintas al ingresar:

| Rol | Puede hacer |
|---|---|
| **Administrador** | Gestionar Funcionarios, Categorías de recursos, Recursos, Calendarización, Actividades y Estadísticas. **No** tiene acceso a Reservas (esa función es exclusiva de Funcionarios, según el enunciado). |
| **Funcionario** | Reservas (crear, ver, modificar, cancelar), Calendarización, Actividades, Estadísticas, y cambiar su propia clave. |

## Ingreso al sistema

- Escribe tu **id** y **clave** en la ventana de Login y presiona "Ingresar".
- Si olvidaste tu clave, usa el botón **"Olvidé mi clave"**: se genera una clave nueva y se envía a tu correo registrado (requiere que el sistema tenga configurado el envío de correo, ver más abajo).
- Una vez dentro, puedes cambiar tu clave en cualquier momento con el botón **"Cambiar clave"** en la parte superior de la ventana principal.
- El botón **"Cerrar sesión"** te regresa a la pantalla de Login.

## Gestión de Funcionarios (solo Administrador)

En la pestaña **Funcionarios**:
- **Registrar** un funcionario nuevo: completa id, nombre, teléfono y correo, y presiona "Registrar y enviar clave". El sistema genera una clave y la envía por correo al funcionario.
- **Buscar** un funcionario por id o nombre con el campo de búsqueda.
- **Modificar** los datos de un funcionario: selecciónalo en la tabla (se cargan sus datos en el formulario), edita lo que necesites y presiona "Modificar datos".
- **Eliminar**: selecciona un funcionario en la tabla y presiona "Eliminar seleccionado".

## Reservas (solo Funcionario)

En la pestaña **Reservas**:

1. Completa el formulario: actividad, fecha, hora de inicio, hora de fin, y selecciona una o varias categorías de recurso que necesitas (puedes seleccionar varias con Ctrl+clic).
2. Presiona **"Reservar"**.
   - Si hay disponibilidad de al menos un recurso libre en cada categoría solicitada, la reserva se crea con el primer recurso disponible de cada una.
   - Si alguna categoría no tiene disponibilidad, el sistema te lo indica y puedes ajustar los datos e intentar de nuevo.
3. Tu reserva aparece en la tabla **"Mis reservas"**, donde puedes:
   - Seleccionarla y presionar **"Modificar reserva seleccionada"** para cambiar sus datos.
   - Seleccionarla y presionar **"Cancelar reserva seleccionada"** (solo si es una fecha futura).
   - Presionar **"Imprimir"** para generar un reporte en PDF de tus reservas.

### Llenar la reserva con Inteligencia Artificial

En vez de llenar el formulario a mano, puedes escribir una frase describiendo lo que necesitas en el campo **"Frase"** y presionar **"Extraer IA"**. Por ejemplo:

```
Necesito coordinar una reunión de planificación el 25 de setiembre de 2pm a 4pm,
voy a necesitar la Sala de Juntas
```

El sistema completa automáticamente actividad, fecha, horas y categorías. Siempre puedes revisar y corregir lo que se llenó antes de presionar "Reservar".

## Reportes en PDF

Cualquier pantalla que tenga un botón **"Imprimir"** genera un archivo PDF real, guardado en la carpeta `reportes/` en la raíz del proyecto.

## Configuración opcional (`config.properties`)

El archivo `src/main/resources/config.properties` controla dos funcionalidades opcionales. Si se dejan vacías, el sistema sigue funcionando normalmente, solo sin esas mejoras:

```properties
# Envío real de correo (Gmail) — necesario para "Registrar funcionario" y "Olvidé mi clave"
gmail.usuario=
gmail.password=

# IA real (Google Gemini, gratis) — si se deja vacío, se usa un extractor simple sin costo
gemini.apiKey=
gemini.modelo=gemini-2.0-flash
```

- Para `gmail.password`, necesitas una **contraseña de aplicación** de Gmail (no tu clave normal), generada en https://myaccount.google.com/apppasswords (requiere verificación en 2 pasos activada).
- Para `gemini.apiKey`, consigue una llave gratuita (sin tarjeta de crédito) en https://aistudio.google.com/ → "Get API key".

**Importante:** nunca subas este archivo con tus credenciales reales a GitHub. Mantén la versión del repositorio con los campos vacíos, y llena los tuyos solo en tu copia local.

## Estructura del proyecto

```
src/main/java/cr/ac/una/eif206/
├── modelo/         → Entidades del sistema (Usuario, Reserva, Funcionario, etc.)
├── persistencia/   → Acceso a los archivos XML (DAOs)
├── negocio/        → Lógica de negocio (autenticación, reservas, IA)
├── presentacion/   → Vistas y controladores (Swing, MVC)
├── reportes/       → Generación de reportes PDF
└── util/           → Utilidades generales (configuración, constantes)
```

## Pruebas automatizadas

El proyecto incluye pruebas con JUnit Jupiter:

```bash
mvn test      # ejecuta las pruebas de unidad (Surefire)
mvn verify    # ejecuta las pruebas de integración (Failsafe)
```
