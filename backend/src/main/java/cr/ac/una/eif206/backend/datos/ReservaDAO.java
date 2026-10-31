package cr.ac.una.eif206.backend.datos;

import cr.ac.una.eif206.common.modelo.EstadoReserva;
import cr.ac.una.eif206.common.modelo.Recurso;
import cr.ac.una.eif206.common.modelo.Reserva;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Acceso a las tablas "reserva" y "reserva_recurso".
 *
 * Cada reserva se lee COMPLETA: con el nombre del funcionario (JOIN con
 * funcionario) y la lista de recursos asignados (JOIN con reserva_recurso,
 * recurso y categoria).
 *
 * JORDI: listarActivasEntre(desde, hasta) sirve para Calendarizacion,
 * Actividades y Estadisticas.
 */
public class ReservaDAO {

    /**
     * Trae una fila por cada (reserva, recurso). Si una reserva tiene 3
     * recursos, salen 3 filas con los mismos datos de la reserva; el metodo
     * agrupar(...) las junta en un solo objeto Reserva.
     * LEFT JOIN: para que salga la reserva aunque no tenga recursos.
     */
    private static final String SELECT_COMPLETO =
            "SELECT re.id, re.id_funcionario, f.nombre AS nombre_funcionario, re.actividad, re.fecha, "
                    + "       re.hora_inicio, re.hora_fin, re.estado, "
                    + "       r.id AS id_recurso, r.id_categoria, r.descripcion AS desc_recurso, "
                    + "       c.descripcion AS desc_categoria "
                    + "FROM reserva re "
                    + "JOIN funcionario f ON f.id = re.id_funcionario "
                    + "LEFT JOIN reserva_recurso rr ON rr.id_reserva = re.id "
                    + "LEFT JOIN recurso r ON r.id = rr.id_recurso "
                    + "LEFT JOIN categoria c ON c.id = r.id_categoria ";

    // ==================================================================
    // Consultas
    // ==================================================================

    /** Todas las reservas de un funcionario, las mas recientes primero. */
    public List<Reserva> listarPorFuncionario(String idFuncionario) throws SQLException {
        String sql = SELECT_COMPLETO
                + "WHERE re.id_funcionario = ? "
                + "ORDER BY re.fecha DESC, re.hora_inicio DESC, r.id";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idFuncionario);
            try (ResultSet rs = ps.executeQuery()) {
                return agrupar(rs);
            }
        }
    }

    /** Reservas ACTIVAS entre dos fechas (incluidas). Util para Jordi. */
    public List<Reserva> listarActivasEntre(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = SELECT_COMPLETO
                + "WHERE re.estado = 'ACTIVA' AND re.fecha BETWEEN ? AND ? "
                + "ORDER BY re.fecha, re.hora_inicio, r.id";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, desde);
            ps.setObject(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                return agrupar(rs);
            }
        }
    }

    public Optional<Reserva> buscarPorId(int id) throws SQLException {
        String sql = SELECT_COMPLETO + "WHERE re.id = ? ORDER BY r.id";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                List<Reserva> lista = agrupar(rs);
                return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
            }
        }
    }

    /**
     * Busca el PRIMER recurso de la categoria que NO este ocupado por otra
     * reserva ACTIVA que se cruce en fecha y horario.
     *
     * Dos horarios se cruzan si: inicioExistente < finNuevo  Y  inicioNuevo < finExistente.
     *
     * @param idReservaExcluida al MODIFICAR una reserva, su propio id (para que no
     *                          "choque consigo misma"). Al crear, se manda 0.
     */
    public Optional<Recurso> buscarRecursoLibre(int idCategoria, LocalDate fecha, LocalTime horaInicio,
                                                LocalTime horaFin, int idReservaExcluida) throws SQLException {
        String sql =
                "SELECT r.id, r.id_categoria, r.descripcion, c.descripcion AS desc_categoria "
                        + "FROM recurso r JOIN categoria c ON c.id = r.id_categoria "
                        + "WHERE r.id_categoria = ? "
                        + "  AND r.id NOT IN ( "
                        + "      SELECT rr.id_recurso "
                        + "      FROM reserva_recurso rr JOIN reserva re ON re.id = rr.id_reserva "
                        + "      WHERE re.estado = 'ACTIVA' "
                        + "        AND re.fecha = ? "
                        + "        AND re.hora_inicio < ? "     // inicio existente < fin nuevo
                        + "        AND ? < re.hora_fin "        // inicio nuevo < fin existente
                        + "        AND re.id <> ? ) "
                        + "ORDER BY r.id "
                        + "LIMIT 1";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCategoria);
            ps.setObject(2, fecha);
            ps.setObject(3, horaFin);
            ps.setObject(4, horaInicio);
            ps.setInt(5, idReservaExcluida);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Recurso recurso = new Recurso(rs.getString("id"), rs.getInt("id_categoria"),
                            rs.getString("descripcion"));
                    recurso.setDescripcionCategoria(rs.getString("desc_categoria"));
                    return Optional.of(recurso);
                }
            }
        }
        return Optional.empty();
    }

    // ==================================================================
    // Escritura (con TRANSACCIONES)
    // ==================================================================

    /**
     * Inserta la reserva y sus recursos en UNA transaccion:
     * si algo falla a medio camino, se deshace todo (rollback).
     * Devuelve la reserva con el id que genero MySQL.
     */
    public Reserva insertar(Reserva reserva) throws SQLException {
        String sql = "INSERT INTO reserva (id_funcionario, actividad, fecha, hora_inicio, hora_fin, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);   // inicia la transaccion
            try {
                try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, reserva.getIdFuncionario());
                    ps.setString(2, reserva.getActividad());
                    ps.setObject(3, reserva.getFecha());
                    ps.setObject(4, reserva.getHoraInicio());
                    ps.setObject(5, reserva.getHoraFin());
                    ps.setString(6, reserva.getEstado().name());
                    ps.executeUpdate();

                    try (ResultSet llaves = ps.getGeneratedKeys()) {
                        if (llaves.next()) {
                            reserva.setId(llaves.getInt(1));
                        }
                    }
                }
                insertarRecursos(con, reserva);
                con.commit();           // todo salio bien: se guarda
            } catch (SQLException e) {
                con.rollback();         // algo fallo: se deshace todo
                throw e;
            }
        }
        return reserva;
    }

    /** Actualiza datos y recursos de una reserva existente (tambien en una transaccion). */
    public void actualizar(Reserva reserva) throws SQLException {
        String sqlReserva = "UPDATE reserva SET actividad = ?, fecha = ?, hora_inicio = ?, hora_fin = ?, estado = ? "
                + "WHERE id = ?";
        String sqlBorrarRecursos = "DELETE FROM reserva_recurso WHERE id_reserva = ?";

        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sqlReserva)) {
                    ps.setString(1, reserva.getActividad());
                    ps.setObject(2, reserva.getFecha());
                    ps.setObject(3, reserva.getHoraInicio());
                    ps.setObject(4, reserva.getHoraFin());
                    ps.setString(5, reserva.getEstado().name());
                    ps.setInt(6, reserva.getId());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(sqlBorrarRecursos)) {
                    ps.setInt(1, reserva.getId());
                    ps.executeUpdate();
                }
                insertarRecursos(con, reserva);
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** Cambia solo el estado (ej: CANCELADA). Los recursos quedan libres para otras reservas. */
    public void actualizarEstado(int id, EstadoReserva estado) throws SQLException {
        String sql = "UPDATE reserva SET estado = ? WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /**
     * Borra la reserva definitivamente (sus filas de reserva_recurso se borran
     * solas por el ON DELETE CASCADE). Se usa en las pruebas para limpiar.
     */
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM reserva WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ==================================================================
    // Apoyo
    // ==================================================================

    /** Inserta una fila en reserva_recurso por cada recurso (usando la MISMA conexion/transaccion). */
    private void insertarRecursos(Connection con, Reserva reserva) throws SQLException {
        String sql = "INSERT INTO reserva_recurso (id_reserva, id_recurso) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Recurso recurso : reserva.getRecursos()) {
                ps.setInt(1, reserva.getId());
                ps.setString(2, recurso.getId());
                ps.addBatch();          // se acumulan y se envian todas juntas
            }
            ps.executeBatch();
        }
    }

    /**
     * Junta las filas del SELECT_COMPLETO: varias filas de la misma reserva
     * (una por recurso) se convierten en UN objeto Reserva con su lista de recursos.
     * LinkedHashMap conserva el orden en que llegaron (el ORDER BY).
     */
    private List<Reserva> agrupar(ResultSet rs) throws SQLException {
        Map<Integer, Reserva> reservasPorId = new LinkedHashMap<>();

        while (rs.next()) {
            int id = rs.getInt("id");
            Reserva reserva = reservasPorId.get(id);

            if (reserva == null) {      // primera fila de esta reserva
                reserva = new Reserva(
                        rs.getString("id_funcionario"),
                        rs.getString("actividad"),
                        rs.getObject("fecha", LocalDate.class),
                        rs.getObject("hora_inicio", LocalTime.class),
                        rs.getObject("hora_fin", LocalTime.class));
                reserva.setId(id);
                reserva.setNombreFuncionario(rs.getString("nombre_funcionario"));
                reserva.setEstado(EstadoReserva.valueOf(rs.getString("estado")));
                reservasPorId.put(id, reserva);
            }

            String idRecurso = rs.getString("id_recurso");
            if (idRecurso != null) {    // null si la reserva no tiene recursos
                Recurso recurso = new Recurso(idRecurso, rs.getInt("id_categoria"), rs.getString("desc_recurso"));
                recurso.setDescripcionCategoria(rs.getString("desc_categoria"));
                reserva.getRecursos().add(recurso);
            }
        }
        return new ArrayList<>(reservasPorId.values());
    }
}