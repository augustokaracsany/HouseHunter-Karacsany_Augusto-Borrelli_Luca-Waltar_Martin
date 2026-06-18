package DLL;

import BLL.Invitado;
import BLL.Reserva;
import Repository.Hashing;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InvitadoController {

    // Carga masiva de invitados (CU06) - recibimos lista de objetos Invitado
    public boolean cargarInvitados(int idReserva, List<Invitado> invitados) {
        String sql = "INSERT INTO invitados (id_reserva, nombre, email, telefono, dni, token_acceso) VALUES (?, ?, ?, ?, ?, ?)";
        Connection con = ConexionController.getInstance().getConnection();
        try {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (Invitado inv : invitados) {
                    ps.setInt(1, idReserva);
                    ps.setString(2, inv.getNombre());
                    ps.setString(3, inv.getEmail());
                    ps.setString(4, inv.getTelefono());
                    ps.setString(5, inv.getDni());
                    ps.setString(6, generarTokenUnico());
                    ps.addBatch();
                }
                ps.executeBatch();
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

    // Validar datos individuales de invitado (CU09) - comprueba campos obligatorios
    public boolean validarDatosInvitado(Invitado inv) {
        return inv.getNombre() != null && !inv.getNombre().trim().isEmpty()
                && inv.getEmail() != null && !inv.getEmail().trim().isEmpty()
                && inv.getEmail().contains("@")
                && inv.getDni() != null && !inv.getDni().trim().isEmpty();
    }

    // Generar token único (CU08)
    public String generarTokenUnico() {
        return UUID.randomUUID().toString();
    }

    // Enviar notificaciones (simulado) (CU08)
    public boolean enviarNotificaciones(int idReserva) {
        // Obtener todos los invitados de la reserva que no tengan token o que no hayan sido notificados
        String sql = "SELECT id, nombre, email, token_acceso FROM invitados WHERE id_reserva = ? AND token_acceso IS NOT NULL";
        List<Invitado> pendientes = new ArrayList<>();
        try (Connection con = ConexionController.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idReserva);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Invitado inv = new Invitado(rs.getInt("id"), rs.getString("email"), rs.getString("nombre"), "", "", "", rs.getString("token_acceso"), false);
                pendientes.add(inv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // Simular envío (en un sistema real sería email/SMS)
        for (Invitado inv : pendientes) {
            System.out.println("Enviando notificación a " + inv.getNombre() + " (" + inv.getEmail() + ") con token: " + inv.getTokenAcceso());
            // Aquí se podría actualizar un campo "notificado" en la BD
        }
        return true;
    }

    // Obtener invitados de una reserva
    public List<Invitado> listarInvitadosPorReserva(int idReserva) {
        List<Invitado> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, email, telefono, dni, token_acceso, asistencia_confirmada FROM invitados WHERE id_reserva = ?";
        try (Connection con = ConexionController.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idReserva);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Invitado inv = new Invitado(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("nombre"),
                        "", // apellido no está en tabla invitados, lo dejamos vacío
                        rs.getString("dni"),
                        rs.getString("telefono"),
                        rs.getString("token_acceso"),
                        rs.getBoolean("asistencia_confirmada")
                );
                lista.add(inv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}