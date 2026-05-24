package DLL;

import BLL.Premio;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.JOptionPane;

public class PremioController {
    // Estructura Singleton de Augusto
    private static PremioController instance;

    private PremioController() {}

    public static PremioController getInstance() {
        if (instance == null) {
            instance = new PremioController();
        }
        return instance;
    }

    /**
     * Sorteo rápido del Administrador (Augusto)
     * Elige un ganador al azar en caliente basado en la asistencia real registrada.
     */
    public boolean ejecutarSorteoPremio(String codigoEvento, String descripcionPremio) {
        Connection con = ConexionController.getInstance().getConnection();
        String sqlSorteo = "SELECT rh.id AS id_res_real, aa.id_usuario, dp.nombre, dp.apellido, dp.dni " +
                           "FROM asistencias_actividades aa " +
                           "JOIN actividades act ON aa.id_actividad = act.id " +
                           "JOIN reservas_hotel rh ON act.id_reserva = rh.id " +
                           "JOIN datos_personas dp ON aa.id_usuario = dp.id_usuario " +
                           "WHERE rh.codigo_unico_evento = ? AND aa.asistio = 'S' " +
                           "ORDER BY RAND() LIMIT 1";
        
        String sqlInsertPremio = "INSERT INTO premios (id_reserva, nombre_premio, entregado, id_ganador_usuario) VALUES (?, ?, 'S', ?)";

        try {
            int idReserva = 0, idUsuarioGanador = 0;
            String nombreCompleto = "", dniGanador = "";

            try (PreparedStatement ps = con.prepareStatement(sqlSorteo)) {
                ps.setString(1, codigoEvento);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        idReserva = rs.getInt("id_res_real");
                        idUsuarioGanador = rs.getInt("id_usuario");
                        nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido");
                        dniGanador = rs.getString("dni");
                    }
                }
            }

            if (idUsuarioGanador == 0) {
                JOptionPane.showMessageDialog(null, "❌ No hay asistentes confirmados para este sorteo.", "Sorteo Vacío", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(sqlInsertPremio)) {
                ps.setInt(1, idReserva);
                ps.setString(2, descripcionPremio);
                ps.setInt(3, idUsuarioGanador);
                ps.executeUpdate();
            }

            String mensajeExito = "<html><body style='width: 250px; text-align: center;'>"
                                + "<h2 style='color: #2ecc71;'>🎉 ¡Tenemos Ganador! 🎉</h2>"
                                + "<p><b>Invitado:</b> " + nombreCompleto + "</p>"
                                + "<p><b>DNI:</b> " + dniGanador + "</p><hr>"
                                + "<p><b>Premio:</b><br>" + descripcionPremio + "</p></body></html>";
            
            JOptionPane.showMessageDialog(null, mensajeExito, "Sorteo Exitoso", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    // =========================================================================
    // 🌿 CASOS DE USO DEL INVITADO (Traídos de feat/luca)
    // =========================================================================

    // CU29: Listar premios activos (los que tienen stock > 0 y están activos)
    public List<Premio> listarPremiosDisponibles() {
        List<Premio> premios = new ArrayList<>();
        String sql = "SELECT * FROM premios WHERE activo = 1 AND cantidad_disponible > 0";
        try (Connection con = ConexionController.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Premio p = new Premio();
                p.setId(rs.getInt("id"));
                p.setNombre(rs.getString("nombre"));
                p.setDescripcion(rs.getString("descripcion"));
                p.setCantidadDisponible(rs.getInt("cantidad_disponible"));
                p.setActivo(rs.getBoolean("activo"));
                premios.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return premios;
    }

    // CU29: Participar en sorteo postulándose manualmente
    public boolean participarEnSorteo(int idInvitado, int idPremio) {
        String checkSql = "SELECT id FROM participaciones_premios WHERE id_invitado = ? AND id_premio = ?";
        try (Connection con = ConexionController.getInstance().getConnection();
             PreparedStatement psCheck = con.prepareStatement(checkSql)) {
            psCheck.setInt(1, idInvitado);
            psCheck.setInt(2, idPremio);
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next()) {
                    System.out.println("El invitado ya participó en este premio.");
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        String insertSql = "INSERT INTO participaciones_premios (id_invitado, id_premio, elegible, ganador, voucher) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionController.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(insertSql)) {
            ps.setInt(1, idInvitado);
            ps.setInt(2, idPremio);
            ps.setBoolean(3, false); 
            ps.setBoolean(4, false); 
            ps.setString(5, null);   
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // CU30: Verificar si el invitado cumple los requisitos de asistencia
    public boolean esElegible(int idInvitado) {
        String sql = "SELECT asistencia_confirmada FROM invitados WHERE id = ?";
        try (Connection con = ConexionController.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idInvitado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("asistencia_confirmada");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // CU31: Obtener o generar el voucher único si salió ganador de la postulación
    public String obtenerVoucher(int idInvitado, int idPremio) {
        String selectSql = "SELECT voucher, ganador FROM participaciones_premios WHERE id_invitado = ? AND id_premio = ?";
        try (Connection con = ConexionController.getInstance().getConnection();
             PreparedStatement ps = selectSql != null ? con.prepareStatement(selectSql) : null) {
            ps.setInt(1, idInvitado);
            ps.setInt(2, idPremio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String voucherExistente = rs.getString("voucher");
                    if (voucherExistente != null && !voucherExistente.isEmpty()) {
                        return voucherExistente;
                    }
                    boolean esGanador = rs.getBoolean("ganador");
                    if (!esGanador) {
                        return null;
                    }
                    
                    String nuevoVoucher = generarVoucherUnico();
                    String updateSql = "UPDATE participaciones_premios SET voucher = ? WHERE id_invitado = ? AND id_premio = ?";
                    try (PreparedStatement psUpd = con.prepareStatement(updateSql)) {
                        psUpd.setString(1, nuevoVoucher);
                        psUpd.setInt(2, idInvitado);
                        psUpd.setInt(3, idPremio);
                        if (psUpd.executeUpdate() > 0) {
                            return nuevoVoucher;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String generarVoucherUnico() {
        return "VCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // Método de Luca para sortear sobre los postulados de la tabla intermedia
    public boolean realizarSorteo(int idPremio) {
        String checkStock = "SELECT cantidad_disponible FROM premios WHERE id = ?";
        try (Connection con = ConexionController.getInstance().getConnection();
             PreparedStatement psCheck = con.prepareStatement(checkStock)) {
            psCheck.setInt(1, idPremio);
            try (ResultSet rsStock = psCheck.executeQuery()) {
                if (rsStock.next() && rsStock.getInt("cantidad_disponible") <= 0) {
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        String updateElegibles = "UPDATE participaciones_premios SET elegible = ? WHERE id_premio = ? AND elegible = 0";
        try (Connection con = ConexionController.getInstance().getConnection();
             PreparedStatement psUpd = con.prepareStatement(updateElegibles)) {
            psUpd.setBoolean(1, true);
            psUpd.setInt(2, idPremio);
            psUpd.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String seleccionarGanador = "SELECT id_invitado FROM participaciones_premios WHERE id_premio = ? AND elegible = 1 AND ganador = 0 ORDER BY RAND() LIMIT 1";
        try (Connection con = ConexionController.getInstance().getConnection();
             PreparedStatement psSel = con.prepareStatement(seleccionarGanador)) {
            psSel.setInt(1, idPremio);
            try (ResultSet rs = psSel.executeQuery()) {
                if (rs.next()) {
                    int idGanador = rs.getInt("id_invitado");
                    
                    String marcar = "UPDATE participaciones_premios SET ganador = 1 WHERE id_invitado = ? AND id_premio = ?";
                    try (PreparedStatement psGan = con.prepareStatement(marcar)) {
                        psGan.setInt(1, idGanador);
                        psGan.setInt(2, idPremio);
                        psGan.executeUpdate();
                    }
                    
                    String reducirStock = "UPDATE premios SET cantidad_disponible = cantidad_disponible - 1 WHERE id = ?";
                    try (PreparedStatement psStock = con.prepareStatement(reducirStock)) {
                        psStock.setInt(1, idPremio);
                        psStock.executeUpdate();
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}