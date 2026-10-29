package cr.ac.una.eif206.common.protocolo;

/**
 * Lista de todas las operaciones que el frontend puede pedirle al backend.
 * Cada Request lleva uno de estos comandos, y el ManejadorCliente del backend
 * decide a que servicio llamar segun el comando.
 *
 * Organizado por bloques segun quien lo implementa. Si alguien necesita un
 * comando nuevo, lo agrega SOLO en su bloque y avisa al grupo.
 */
public enum Comando {

    // ---------- ANDY: sesion, reservas y alertas ----------
    LOGIN,
    CAMBIAR_CLAVE,
    RECUPERAR_CLAVE,
    CERRAR_SESION,
    LISTAR_CATEGORIAS,          // lo usan Reservas, Recursos y Calendarizacion
    LISTAR_MIS_RESERVAS,
    CREAR_RESERVA,
    MODIFICAR_RESERVA,
    CANCELAR_RESERVA,
    EXTRAER_RESERVA_IA,
    SUSCRIBIR_ALERTAS,          // lo envia el 2do socket del frontend al conectarse
    ALERTA_PRUEBA,              // dispara una alerta falsa (para que Benja pruebe)

    // ---------- BENJA: funcionarios y categorias ----------
    LISTAR_FUNCIONARIOS,
    BUSCAR_FUNCIONARIOS,
    REGISTRAR_FUNCIONARIO,
    MODIFICAR_FUNCIONARIO,
    ELIMINAR_FUNCIONARIO,
    BUSCAR_CATEGORIAS,
    AGREGAR_CATEGORIA,
    MODIFICAR_CATEGORIA,
    ELIMINAR_CATEGORIA,

    // ---------- JORDI: recursos, calendarizacion, actividades, estadisticas ----------
    LISTAR_RECURSOS,
    LISTAR_RECURSOS_POR_CATEGORIA,
    AGREGAR_RECURSO,
    MODIFICAR_RECURSO,
    ELIMINAR_RECURSO,
    CALENDARIZACION,            // reservas de una fecha y categoria
    PROGRAMACION_SEMANAL,       // reservas de una semana
    ESTADISTICAS_RECURSOS,
    ESTADISTICAS_ACTIVIDADES
}