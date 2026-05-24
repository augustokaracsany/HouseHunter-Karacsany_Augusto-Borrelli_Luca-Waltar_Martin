package DLL;
	
import BLL.*;
import Repository.Hashing;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventoController {
    private static EventoController instance;

    // Cambiado a privado para Singleton
    private EventoController() {}

    public static EventoController getInstance() {
        if (instance == null) {
            instance = new EventoController();
        }
        return instance;
    }

    public boolean verificarDisponibilidad(LocalDate fechaEvento, int numInvitados) {
        String sql = "SELECT COUNT(*) FROM reservas WHERE fecha_evento = ? AND estado != 'CANCELADA'";
        Connection con = ConexionController.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fechaEvento));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error verificando disponibilidad: " + e.getMessage());
        }
        return true;
    }

    // Crear nueva reserva (CU04)
    public Reserva crearReserva(Reserva reserva) throws SQLException {
        String sql = "INSERT INTO reservas (id_empresa, fecha_evento, num_invitados, estado, id_plantilla) VALUES (?, ?, ?, ?, ?)";
        Connection con = ConexionController.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reserva.getEmpresa().getId());
            ps.setDate(2, Date.valueOf(reserva.getFechaEvento()));
            ps.setInt(3, reserva.getNumInvitados());
            ps.setString(4, reserva.getEstado());
            if (reserva.getPlantilla() != null)
                ps.setInt(5, reserva.getPlantilla().getId());
            else
                ps.setNull(5, Types.INTEGER);
            
            int affected = ps.executeUpdate();
            if (affected == 0) throw new SQLException("No se pudo crear la reserva");
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reserva.setId(generatedKeys.getInt(1));
                }
            }
        }
        return reserva;
    }

    // Obtener reservas de una empresa
    public List<Reserva> listarReservasPorEmpresa(int idEmpresa) {
        List<Reserva> reservas = new ArrayList<>();
        String sql = "SELECT r.*, p.nombre as plantilla_nombre FROM reservas r LEFT JOIN plantillas p ON r.id_plantilla = p.id WHERE r.id_empresa = ? ORDER BY r.fecha_evento DESC";
        try (Connection con = ConexionController.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpresa);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Reserva r = new Reserva();
                r.setId(rs.getInt("id"));
                r.setFechaEvento(rs.getDate("fecha_evento").toLocalDate());
                r.setFechaReserva(rs.getTimestamp("fecha_reserva").toLocalDateTime());
                r.setNumInvitados(rs.getInt("num_invitados"));
                r.setEstado(rs.getString("estado"));
                Plantilla p = new Plantilla();
                p.setId(rs.getInt("id_plantilla"));
                p.setNombre(rs.getString("plantilla_nombre"));
                r.setPlantilla(p);
                reservas.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservas;
    }

    // Guardar cronograma completo (CU12)
    public boolean guardarCronograma(int idReserva, List<Actividad> actividades) {
        String deleteSql = "DELETE FROM actividades WHERE id_reserva = ?";
        String insertSql = "INSERT INTO actividades (id_reserva, nombre, descripcion, fecha_hora, duracion_minutos, cupo_maximo, importancia, categoria) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection con = ConexionController.getInstance().getConnection();
        try {
            con.setAutoCommit(false);
            try (PreparedStatement psDel = con.prepareStatement(deleteSql)) {
                psDel.setInt(1, idReserva);
                psDel.executeUpdate();
            }
            try (PreparedStatement psIns = con.prepareStatement(insertSql)) {
                for (Actividad act : actividades) {
                    psIns.setInt(1, idReserva);
                    psIns.setString(2, act.getNombre());
                    psIns.setString(3, act.getDescripcion());
                    psIns.setTimestamp(4, Timestamp.valueOf(act.getFechaHora()));
                    psIns.setInt(5, act.getDuracionMinutos());
                    psIns.setInt(6, act.getCupoMaximo());
                    psIns.setString(7, act.getImportancia().toString());
                    psIns.setString(8, act.getCategoria());
                    psIns.addBatch();
                }
                psIns.executeBatch();
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Obtener actividades de una reserva
    public List<Actividad> obtenerActividadesPorReserva(int idReserva) {
        List<Actividad> lista = new ArrayList<>();
        String sql = "SELECT * FROM actividades WHERE id_reserva = ? ORDER BY fecha_hora";
        try (Connection con = ConexionController.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idReserva);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Actividad a = new Actividad();
                a.setId(rs.getInt("id"));
                a.setNombre(rs.getString("nombre"));
                a.setDescripcion(rs.getString("descripcion"));
                a.setFechaHora(rs.getTimestamp("fecha_hora").toLocalDateTime());
                a.setDuracionMinutos(rs.getInt("duracion_minutos"));
                a.setCupoMaximo(rs.getInt("cupo_maximo"));
                a.setImportancia(Importancia.valueOf(rs.getString("importancia")));
                a.setCategoria(rs.getString("categoria"));
                lista.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean asignarPlantilla(int idReserva, int idPlantilla) {
        String sql = "UPDATE reservas SET id_plantilla = ? WHERE id = ?";
        try (Connection con = ConexionController.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPlantilla);
            ps.setInt(2, idReserva);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Plantilla> listarPlantillas() {
        List<Plantilla> plantas = new ArrayList<>();
        String sql = "SELECT * FROM plantillas WHERE activa = 1";
        try (Connection con = ConexionController.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Plantilla p = new Plantilla();
                p.setId(rs.getInt("id"));
                p.setNombre(rs.getString("nombre"));
                p.setDescripcion(rs.getString("descripcion"));
                p.setUrlImagen(rs.getString("url_imagen"));
                p.setActiva(rs.getBoolean("activa"));
                plantas.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return plantas;
    }

    // --- MÉTODOS MIGRADOS DE HOTELCONTROLLER ---
    public boolean validarInvitadoPrevia(String codigoEvento, String dni) {
        String sql = "SELECT lip.* FROM lista_invitados_previa lip " +
                     "JOIN reservas_hotel rh ON lip.id_reserva = rh.id " +
                     "WHERE rh.codigo_unico_evento = ? AND lip.dni = ?";
        Connection con = ConexionController.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigoEvento);
            ps.setString(2, dni);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public String obtenerReporteConsolidadoEvento(String codigoEvento) {
        StringBuilder reporte = new StringBuilder();
        String sql = "SELECT rh.id, " +
                     "  (SELECT COUNT(*) FROM lista_invitados_previa lip WHERE lip.id_reserva = rh.id) as total_invitados, " +
                     "  (SELECT COUNT(DISTINCT ah.id_usuario) FROM asignaciones_habitaciones ah WHERE ah.id_reserva = rh.id) as total_checkins, " +
                     "  (SELECT COUNT(*) FROM actividades act " +
                     "   JOIN asistencias_actividades aa ON aa.id_actividad = act.id " +
                     "   WHERE act.id_reserva = rh.id AND aa.asistio = 'S') as total_asistencias " +
                     "FROM reservas_hotel rh " +
                     "WHERE rh.codigo_unico_evento = ?";
        Connection con = ConexionController.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigoEvento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int totalInvitados = rs.getInt("total_invitados");
                    int totalCheckins = rs.getInt("total_checkins");
                    int totalAsistencias = rs.getInt("total_asistencias");
                    int porcentajeOcupacion = totalInvitados > 0 ? (totalCheckins * 100 / totalInvitados) : 0;

                    reporte.append("<html><body style='width: 300px;'>");
                    reporte.append("<h2 style='text-align: center; color: #2c3e50;'>📊 Reporte Consolidado</h2>");
                    reporte.append("<p style='text-align: center; margin-top:0;'><b>Evento:</b> ").append(codigoEvento).append("</p><hr>");
                    reporte.append("<table style='width: 100%; border-collapse: collapse;'>");
                    reporte.append("<tr><td><b>📋 Invitados en Lista:</b></td><td style='text-align: right;'>").append(totalInvitados).append("</td></tr>");
                    reporte.append("<tr><td><b>🏨 Check-ins Exitosos:</b></td><td style='text-align: right;'>").append(totalCheckins).append("</td></tr>");
                    reporte.append("<tr><td><b>📉 Porcentaje Ocupación:</b></td><td style='text-align: right; color: green;'><b>").append(porcentajeOcupacion).append("%</b></td></tr>");
                    reporte.append("<tr><td colspan='2'><hr style='border-top: 1px dashed #ccc;'></td></tr>");
                    reporte.append("<tr><td><b>🎮 Asistencias Totales:</b></td><td style='text-align: right; color: #2980b9;'><b>").append(totalAsistencias).append("</b></td></tr>");
                    reporte.append("</table></body></html>");
                    return reporte.toString();
                }
                return "<html><body>❌ Código inválido.</body></html>";
            }
        } catch (SQLException e) {
            return "<html><body>❌ Error técnico.</body></html>";
        }
    }
}