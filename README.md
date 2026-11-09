# Sistema de Reserva de Recursos

Proyecto #2 de **EIF206 - Programación 3** (Universidad Nacional, Costa Rica).

Sistema **distribuido** para gestionar reservas de recursos (salas, laptops, proyectores, etc.) de una organización. Es la evolución del Proyecto #1 (aplicación de escritorio con archivos XML) a una arquitectura **cliente-servidor**:

- **Backend:** servidor con **sockets** e **hilos** (un hilo por cliente) que guarda todo en **MySQL**.
- **Frontend:** aplicación de escritorio **Java Swing** con patrón **MVC**, que se comunica con el backend mediante un **ServiceProxy** (patrón **Proxy**).
- **Alertas asíncronas:** cuando un funcionario crea una reserva, a los demás usuarios conectados les aparece una alerta al instante.

**Integrantes:** Andy, Benja y Jordi.

---

## Arquitectura

```
┌──────────────────────┐        sockets (puerto 5000)        ┌──────────────────────┐        JDBC        ┌─────────┐
│  FRONTEND (Swing)    │  ── Request (comando + datos) ──▶   │  BACKEND (servidor)  │  ───────────────▶  │  MySQL  │
│  Vista + Controlador │  ◀── Response (ok / error) ──────   │  Hilo por cliente    │                    │         │
│  ServiceProxy        │                                     │  Service + DAO       │                    └─────────┘
│                      │  ◀── Notificación (alertas) ─────   │  RegistroConexiones  │
└──────────────────────┘     (segunda conexión, asíncrona)   └──────────────────────┘
```

El proyecto es Maven **multi-módulo**:

| Módulo | Contenido |
|---|---|
| `common` | Lo que comparten los dos lados: modelo (`Usuario`, `Reserva`, `Recurso`...) y protocolo (`Request`, `Response`, `Comando`, `Notificacion`). Todo es `Serializable` para viajar por el socket. |
| `backend` | Servidor de sockets, lógica de negocio (Service), acceso a MySQL (DAO), envío de correo e IA. |
| `frontend` | Interfaz gráfica Swing (vistas y controladores), `ServiceProxy`, reportes PDF. |

### Comunicación

1. El controlador llama un método del `ServiceProxy`, por ejemplo `proxy.crearReserva(...)`.
2. El proxy arma un `Request` con un `Comando` y sus parámetros, y lo envía por el socket.
3. En el servidor, el hilo `ManejadorCliente` del usuario recibe el `Request` y lo pasa al `ManejadorComandos` correspondiente.
4. Este valida la sesión y el rol, llama al Service, y el Service al DAO (MySQL).
5. El servidor responde un `Response` (`ok` con datos, o `error` con un mensaje). El proxy devuelve los datos o lanza una `Exception` con ese mensaje.

### Alertas asíncronas

Al iniciar sesión, el frontend abre una **segunda conexión** (hilo `EscuchaAlertas`) que solo escucha. Cuando un funcionario crea una reserva, el servidor envía una `Notificacion` a todos los demás conectados (`RegistroConexiones`). La alerta aparece en el panel derecho con las columnas **Actividad | Ver | Desc.**:

- **Ver** (carpeta): muestra encargado, fecha, horas y recursos de la reserva.
- **Desc.** (círculo rojo): descarta la alerta, solo en esa pantalla.

---

## Requisitos

- **JDK 21**
- **Maven** (o el que trae IntelliJ)
- **MySQL Community Server 8.x**
- Un cliente SQL para ejecutar el script. Nosotros usamos **Oracle SQL Developer** con el driver MySQL Connector/J.
- Internet, solo para el envío de correos (Gmail) y la IA (Gemini).

---

## Instalación

### 1. Crear la base de datos y el usuario (una sola vez)

Conectado a MySQL como `root`:

```sql
CREATE DATABASE reservas_db;
CREATE USER 'reservas'@'localhost' IDENTIFIED BY 'una_clave_segura';
GRANT ALL PRIVILEGES ON reservas_db.* TO 'reservas'@'localhost';
```

### 2. Crear las tablas y los datos de prueba

Con el usuario `reservas`, ejecutar el script **`database/schema.sql`** (en SQL Developer: *Ejecutar Script*, F5).

El script se puede ejecutar las veces que se quiera: borra las tablas, las vuelve a crear y carga los datos de prueba.

### 3. Configurar el backend

Copiar `backend/src/main/resources/config.example.properties` como **`config.properties`** en la misma carpeta y llenarlo:

```properties
db.url=jdbc:mysql://localhost:3306/reservas_db
db.usuario=reservas
db.clave=una_clave_segura

servidor.puerto=5000

# Envío de correo (Gmail): necesario para registrar funcionarios y "Olvidé mi clave"
gmail.usuario=
gmail.password=

# IA (Google Gemini). Si se deja vacío, se usa un extractor simple sin IA
gemini.apiKey=
gemini.modelo=gemini-3.6-flash
```

- `gmail.password` es una **contraseña de aplicación** de Gmail, no la clave normal. Se genera en https://myaccount.google.com/apppasswords y requiere tener la verificación en 2 pasos activada.
- `gemini.apiKey` es una llave gratuita que se obtiene en https://aistudio.google.com/ → *Get API key*.

> ⚠️ `config.properties` está en el `.gitignore` y **nunca** debe subirse a GitHub, porque contiene credenciales. En el repositorio solo va la plantilla `config.example.properties`, vacía.

### 4. Configurar el frontend

En `frontend/src/main/resources/cliente.properties` se indica a qué servidor se conecta el frontend:

```properties
servidor.host=localhost   # o la IP de la computadora donde corre el backend
servidor.puerto=5000
```

---

## Ejecución

1. Compilar desde la raíz del proyecto: `mvn clean install -DskipTests` (o Maven → *install* en IntelliJ).
2. Iniciar el **backend**: ejecutar `cr.ac.una.eif206.backend.ServidorMain`. La consola muestra que el servidor escucha en el puerto 5000.
3. Iniciar uno o varios **frontends**: ejecutar `cr.ac.una.eif206.frontend.FrontendMain`. Se abre la ventana de **Login**.

**En dos computadoras:** el backend y MySQL corren en una. En la otra, se pone en `cliente.properties` la IP de la primera (se ve con `ipconfig`, en "Dirección IPv4"). Ambas deben estar en la misma red, y el firewall debe permitir el puerto 5000.

### Usuarios de prueba

| Id | Clave | Rol | Nombre |
|---|---|---|---|
| `admin` | `admin1234` | Administrador | — |
| `111` | `111` | Funcionario | Juan Mora |
| `222` | `222` | Funcionario | Maria Mata |
| `333` | `333` | Funcionario | Pedro Arias |
| `444` | `444` | Funcionario | Ana Lopez |

Los funcionarios que registra el administrador reciben una **clave generada** en su correo.

---

## Funcionalidades

### Roles

| Rol | Pestañas |
|---|---|
| **Administrador** | Funcionarios, Categorías, Recursos, Calendarización, Actividades, Estadísticas |
| **Funcionario** | Reservas, Calendarización, Actividades, Estadísticas |

Todos los usuarios ven el área de **Alertas** a la derecha y tienen los botones **Cambiar clave** y **Cerrar sesión**. El servidor también valida el rol en cada comando: un funcionario no puede ejecutar acciones de administrador, aunque intente enviarlas.

### Ingreso y claves
- Login con id y clave.
- **Olvidé mi clave:** genera una clave nueva y la envía al correo registrado. Solo se guarda si el correo se envió bien.
- **Cambiar clave**, una vez dentro.

### Funcionarios (Administrador)
- **Registrar:** se indican id, nombre, teléfono y correo. El sistema genera la clave y la envía por correo; el funcionario solo se guarda si el correo se envió correctamente.
- Buscar por id o nombre, modificar datos y eliminar. No se puede eliminar un funcionario que tiene reservas.

### Categorías de recursos (Administrador)
- Agregar, modificar, buscar por descripción y eliminar.
- No se permiten descripciones repetidas.
- No se puede eliminar una categoría que tiene recursos.

### Recursos (Administrador)
- CRUD de recursos, cada uno asociado a una categoría.

### Reservas (Funcionario)
- Crear una reserva con actividad, fecha, hora de inicio, hora de fin y una o varias categorías. Por cada categoría se asigna el primer recurso libre en ese horario.
- Ver, modificar y cancelar las reservas propias.
- **Llenar con IA:** se escribe una frase y el sistema completa el formulario. Ejemplo: *"Necesito una reunión de planificación el 25 de noviembre de 2pm a 4pm en la Sala de Juntas"*. Usa Google Gemini; si no hay llave o falla, usa un extractor simple.
- Al crear una reserva, los demás usuarios conectados reciben una **alerta**.

### Calendarización, Actividades y Estadísticas (ambos roles)
- **Calendarización:** ocupación por hora de los recursos de una categoría en una fecha.
- **Actividades:** programación semanal, de lunes a domingo.
- **Estadísticas:** uso de recursos y de actividades en un rango de fechas.

### Reportes PDF
Todas las pestañas con botón **Imprimir PDF** generan un archivo en la carpeta `reportes/`, que se crea sola en la carpeta desde donde se ejecuta el frontend.

---

## Estructura del proyecto

```
sistema-reservas/
├── pom.xml                       → POM padre (módulos y versiones)
├── database/schema.sql           → Tablas y datos de prueba de MySQL
├── docs/                         → Enunciado del proyecto
├── common/src/main/java/cr/ac/una/eif206/common/
│   ├── modelo/                   → Usuario, Administrador, Funcionario, Reserva, Recurso, CategoriaRecurso...
│   └── protocolo/                → Request, Response, Comando, Notificacion
├── backend/src/main/java/cr/ac/una/eif206/backend/
│   ├── ServidorMain.java         → Punto de entrada del servidor
│   ├── red/                      → Servidor, ManejadorCliente (hilos), comandos, sesión y alertas
│   ├── logica/                   → Servicios (reglas de negocio y validaciones)
│   │   └── llm/                  → Extracción de reservas con IA
│   ├── datos/                    → DAOs (JDBC + MySQL)
│   └── util/                     → Configuración, correo, generador de claves
└── frontend/src/main/java/cr/ac/una/eif206/frontend/
    ├── FrontendMain.java         → Punto de entrada del cliente
    ├── proxy/                    → ServiceProxy (patrón Proxy) y EscuchaAlertas
    ├── vista/                    → Pantallas Swing (MVC: Vista)
    │   └── alertas/              → Panel, íconos y detalle de alertas
    ├── controlador/              → Controladores (MVC: Controlador)
    ├── reportes/                 → Generación de PDF (OpenPDF)
    └── util/                     → Constantes y diálogos
```

---

## Pruebas automatizadas (JUnit 5)

```bash
mvn test      # pruebas de UNIDAD (Surefire, archivos *Test.java): no necesitan base de datos
mvn verify    # además, pruebas de INTEGRACIÓN (Failsafe, archivos *IT.java)
```

Las pruebas de integración necesitan MySQL con `schema.sql` recién ejecutado y el `config.properties` del backend configurado. Prueban los servicios contra la base de datos real y el servidor de sockets: login, reservas, alertas, recursos, categorías, funcionarios y consultas.

---

## Tecnologías

Java 21 · Swing · Maven · MySQL 8 + Connector/J · Sockets / ObjectStreams · Hilos · Jakarta Mail (Gmail) · Google Gemini API · OpenPDF · JUnit 5