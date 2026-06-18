package DLL;

import BLL.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReporteController {

    // Obtener estadísticas para una reserva (CU13)
    public Map<String, Object> obtenerReporteEvento(int idReserva) {
        Map<String, Object> reporte = new HashMap<>();
        String sqlInvitados = "SELECT COUNT(*) as total, SUM(asistencia_confirmada) as confirmados FROM invitados WHERE id_reserva = ?";
        String sqlActividades = "SELECT COUNT(*) as total_actividades FROM actividades WHERE id_reserva = ?";
        
        try (Connection con = ConexionController.getInstance().getConnection()) {
            // Total invitados y confirmados
            try (PreparedStatement ps = con.prepareStatement(sqlInvitados)) {
                ps.setInt(1, idReserva);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    reporte.put("totalInvitados", rs.getInt("total"));
                    reporte.put("confirmados", rs.getInt("confirmados"));
                    double porcentaje = rs.getInt("total") > 0 ? (rs.getInt("confirmados") * 100.0 / rs.getInt("total")) : 0;
                    reporte.put("porcentajeConfirmacion", Math.round(porcentaje * 100.0) / 100.0);
                }
            }
            // Cantidad de actividades
            try (PreparedStatement ps = con.prepareStatement(sqlActividades)) {
                ps.setInt(1, idReserva);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    reporte.put("totalActividades", rs.getInt("total_actividades"));
                }
            }
            // También se pueden agregar más estadísticas (actividades por importancia, etc.)
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reporte;
    }
 // Obtener reporte detallado de una actividad específica
    public Map<String, Object> obtenerReporteActividad(int idActividad) {
        Map<String, Object> reporte = new HashMap<>();
        String sqlAct = "SELECT * FROM actividades WHERE id = ?";
        String sqlAsistentes = "SELECT COUNT(*) as total_presentes FROM asistencia_actividades WHERE id_actividad = ? AND presente = TRUE";
        String sqlLista = "SELECT i.nombre, i.email FROM asistencia_actividades aa JOIN invitados i ON aa.id_invitado = i.id WHERE aa.id_actividad = ? AND aa.presente = TRUE";

        try (Connection con = ConexionController.getInstance().getConnection()) {
            // Datos de la actividad
            try (PreparedStatement ps = con.prepareStatement(sqlAct)) {
                ps.setInt(1, idActividad);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    reporte.put("nombre", rs.getString("nombre"));
                    reporte.put("descripcion", rs.getString("descripcion"));
                    reporte.put("fecha_hora", rs.getTimestamp("fecha_hora").toString());
                    reporte.put("duracion", rs.getInt("duracion_minutos"));
                    reporte.put("cupo_maximo", rs.getInt("cupo_maximo"));
                }
            }
            // Cantidad de asistentes
            try (PreparedStatement ps = con.prepareStatement(sqlAsistentes)) {
                ps.setInt(1, idActividad);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    reporte.put("total_asistentes", rs.getInt("total_presentes"));
                }
            }
            // Lista de asistentes (nombres y emails)
            List<String> asistentes = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(sqlLista)) {
                ps.setInt(1, idActividad);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    asistentes.add(rs.getString("nombre") + " (" + rs.getString("email") + ")");
                }
            }
            reporte.put("lista_asistentes", asistentes);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reporte;
    }

    // Exportar reporte del evento a un archivo PDF básico (requiere librería iText, pero aquí simulamos generando HTML)
    // Para simplificar, lo haremos generando un archivo .txt con formato o usando JOptionPane para guardar.
    // En un proyecto real, agrega iTextPDF. Aquí una versión funcional que guarda como .txt.
    public boolean exportarReporteEvento(int idReserva, String rutaArchivo) {
        Map<String, Object> stats = obtenerReporteEvento(idReserva);
        List<Actividad> actividades = new EventoController().obtenerActividadesPorReserva(idReserva);
        StringBuilder contenido = new StringBuilder();
        contenido.append("=== REPORTE DEL EVENTO ===\n");
        contenido.append("ID Reserva: ").append(idReserva).append("\n");
        contenido.append("Total invitados: ").append(stats.get("totalInvitados")).append("\n");
        contenido.append("Confirmados: ").append(stats.get("confirmados")).append("\n");
        contenido.append("Porcentaje confirmación: ").append(stats.get("porcentajeConfirmacion")).append("%\n");
        contenido.append("Total actividades: ").append(stats.get("totalActividades")).append("\n\n");
        contenido.append("=== DETALLE POR ACTIVIDAD ===\n");
        for (Actividad a : actividades) {
            contenido.append("- ").append(a.getNombre()).append(" (").append(a.getFechaHora()).append(")\n");
            Map<String, Object> repAct = obtenerReporteActividad(a.getId());
            contenido.append("  Asistentes: ").append(repAct.get("total_asistentes")).append("/").append(a.getCupoMaximo()).append("\n");
        }
        try (java.io.FileWriter fw = new java.io.FileWriter(rutaArchivo)) {
            fw.write(contenido.toString());
            return true;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}