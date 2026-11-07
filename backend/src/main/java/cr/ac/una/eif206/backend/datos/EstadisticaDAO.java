package cr.ac.una.eif206.backend.datos;

import cr.ac.una.eif206.common.modelo.EstadisticaItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Consultas AGREGADAS (GROUP BY) para las estadisticas (JORDI).
 * Solo cuentan reservas ACTIVAS: las canceladas no son estadistica.
 */
public class EstadisticaDAO {

    /**
     * Cuantas veces se reservo cada categoria entre dos fechas (incluidas).
     * Cada fila de reserva_recurso es un recurso reservado, asi que COUNT(*)
     * cuenta las reservas de esa categoria. Solo salen las categorias que
     * tuvieron al menos una reserva.
     */
    public List<EstadisticaItem> recursosPorCategoria(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql =
                "SELECT c.descripcion AS etiqueta, COUNT(*) AS cantidad "
                        + "FROM reserva re "
                        + "JOIN reserva_recurso rr ON rr.id_reserva = re.id "
                        + "JOIN recurso r ON r.id = rr.id_recurso "
                        + "JOIN categoria c ON c.id = r.id_categoria "
                        + "WHERE re.estado = 'ACTIVA' AND re.fecha BETWEEN ? AND ? "
                        + "GROUP BY c.id, c.descripcion "
                        + "ORDER BY cantidad DESC, c.descripcion";
        List<EstadisticaItem> lista = new ArrayList<>();

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, desde);
            ps.setObject(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new EstadisticaItem(rs.getString("etiqueta"), rs.getInt("cantidad")));
                }
            }
        }
        return lista;
    }

    /**
     * Cuantas actividades (reservas) hay en cada semana entre dos fechas.
     * La etiqueta es el LUNES de la semana (WEEKDAY: lunes = 0), en formato
     * ISO (yyyy-MM-dd). Solo salen las semanas con al menos una actividad;
     * el servicio completa las semanas vacias con 0.
     */
    public List<EstadisticaItem> actividadesPorSemana(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql =
                "SELECT DATE_SUB(re.fecha, INTERVAL WEEKDAY(re.fecha) DAY) AS lunes, "
                        + "       COUNT(*) AS cantidad "
                        + "FROM reserva re "
                        + "WHERE re.estado = 'ACTIVA' AND re.fecha BETWEEN ? AND ? "
                        + "GROUP BY lunes "
                        + "ORDER BY lunes";
        List<EstadisticaItem> lista = new ArrayList<>();

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, desde);
            ps.setObject(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate lunes = rs.getObject("lunes", LocalDate.class);
                    lista.add(new EstadisticaItem(lunes.toString(), rs.getInt("cantidad")));
                }
            }
        }
        return lista;
    }
}
