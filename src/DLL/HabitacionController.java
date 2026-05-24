package DLL;

import BLL.EstadoHabitacion;
import java.sql.*;
import javax.swing.JOptionPane;

public class HabitacionController {
    private static HabitacionController instance;

    private HabitacionController() {}

    public static HabitacionController getInstance() {
        if (instance == null) {
            instance = new HabitacionController();
        }
        return instance;
    }

    public boolean procesarCheckInHabitacion(String codigoEvento, String dniHuesped, String numeroHabitacion) {
        Connection con = ConexionController.getInstance().getConnection();
        String sqlReserva = "SELECT id FROM reservas_hotel WHERE codigo_unico_evento = ?";
        String sqlUsuario = "SELECT id_usuario FROM datos_personas WHERE dni = ?";
        String sqlHabitacion = "SELECT id, estado FROM habitaciones WHERE numero = ?";
        
        try {
            con.setAutoCommit(false);
            int idReserva = 0;
            try (PreparedStatement ps = con.prepareStatement(sqlReserva)) {
                ps.setString(1, codigoEvento);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) idReserva = rs.getInt("id");
                }
            }

            int idUsuario = 0;
            try (PreparedStatement ps = con.prepareStatement(sqlUsuario)) {
                ps.setString(1, dniHuesped);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) idUsuario = rs.getInt("id_usuario");
                }
            }

            if (idReserva == 0 || idUsuario == 0) {
                JOptionPane.showMessageDialog(null, "Error: Evento o DNI no encontrados.", "Error de Check-in", JOptionPane.ERROR_MESSAGE);
                con.rollback();
                return false;
            }

            int idHabitacion = 0;
            String estadoStr = "";
            try (PreparedStatement ps = con.prepareStatement(sqlHabitacion)) {
                ps.setString(1, numeroHabitacion);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        idHabitacion = rs.getInt("id");
                        estadoStr = rs.getString("estado");
                    }
                }
            }

            EstadoHabitacion estadoActual = EstadoHabitacion.valueOf(estadoStr);
            EstadoHabitacion nuevoEstado;
            String casillero = "";

            if (estadoActual == EstadoHabitacion.Libre) {
                nuevoEstado = EstadoHabitacion.Half;
                casillero = "1";
            } else if (estadoActual == EstadoHabitacion.Half) {
                nuevoEstado = EstadoHabitacion.Completa;
                casillero = "2";
            } else {
                JOptionPane.showMessageDialog(null, "Error: Habitación Completa o SOBRE-ASIGNACIÓN.", "Límite Excedido", JOptionPane.WARNING_MESSAGE);
                con.rollback();
                return false;
            }

            String sqlAsignar = "INSERT INTO asignaciones_habitaciones (id_reserva, id_habitacion, id_usuario, casillero_checkin) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlAsignar)) {
                ps.setInt(1, idReserva);
                ps.setInt(2, idHabitacion);
                ps.setInt(3, idUsuario);
                ps.setString(4, casillero);
                ps.executeUpdate();
            }

            String sqlUpdateHab = "UPDATE habitaciones SET estado = ? WHERE id = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlUpdateHab)) {
                ps.setString(1, nuevoEstado.toString());
                ps.setInt(2, idHabitacion);
                ps.executeUpdate();
            }

            con.commit();
            JOptionPane.showMessageDialog(null, "Check-in Exitoso! Habitación " + numeroHabitacion + " pasó a: " + nuevoEstado);
            return true;

        } catch (SQLException e) {
            try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            if (e.getErrorCode() == 1062) {
                JOptionPane.showMessageDialog(null, "Error: El invitado ya cuenta con un Check-in registrado.", "DNI Duplicado", JOptionPane.ERROR_MESSAGE);
            } else {
                System.err.println("Error en transacción de Check-in: " + e.getMessage());
            }
            return false;
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public String obtenerEstadoHabitacionesHtml() {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT numero, estado FROM habitaciones ORDER BY numero ASC";
        Connection con = ConexionController.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            sb.append("<html><body style='width: 260px;'>");
            sb.append("<h2 style='text-align: center; color: #27ae60;'>🏨 Ocupación de Habitaciones</h2><hr>");
            sb.append("<table style='width: 100%; border-collapse: collapse;'>");

            boolean tieneHabitaciones = false;
            while (rs.next()) {
                tieneHabitaciones = true;
                String num = rs.getString("numero");
                String est = rs.getString("estado");
                
                String color = "green";
                if (est.equalsIgnoreCase("Half")) color = "orange";
                else if (est.equalsIgnoreCase("Completa")) color = "red";

                sb.append("<tr>")
                  .append("<td style='padding: 5px; border-bottom: 1px solid #eee;'><b>Habitación ").append(num).append("</b></td>")
                  .append("<td style='text-align: right; padding: 5px; border-bottom: 1px solid #eee; color: ").append(color).append(";'><b>").append(est).append("</b></td>")
                  .append("</tr>");
            }
            sb.append("</table></body></html>");
            return sb.toString();
        } catch (SQLException e) {
            return "<html><body>❌ Error técnico al consultar habitaciones.</body></html>";
        }
    }
}