package DLL;

import BLL.Habitacion;
import BLL.EstadoHabitacion; // Importamos el Enum que diseñaste
import BLL.Invitado;
import BLL.Reserva;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InvitadoController {

    // Estructura Singleton para mantener el estándar del proyecto
    private static InvitadoController instance;

    private InvitadoController() {}

    public static InvitadoController getInstance() {
        if (instance == null) {
            instance = new InvitadoController();
        }
        return instance;
    }

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
        String sql = "SELECT id, nombre, email, token_acceso FROM invitados WHERE id_reserva = ? AND token_acceso IS NOT NULL";
        List<Invitado> pendientes = new ArrayList<>();
        try (Connection con = ConexionController.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idReserva);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Invitado inv = new Invitado(rs.getInt("id"), rs.getString("email"), rs.getString("nombre"), "", "", "", rs.getString("token_acceso"), false);
                    pendientes.add(inv);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        for (Invitado inv : pendientes) {
            System.out.println("Enviando notificación a " + inv.getNombre() + " (" + inv.getEmail() + ") con token: " + inv.getTokenAcceso());
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
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Invitado inv = new Invitado(
                            rs.getInt("id"),
                            rs.getString("email"),
                            rs.getString("nombre"),
                            "", 
                            rs.getString("dni"),
                            rs.getString("telefono"),
                            rs.getString("token_acceso"),
                            rs.getBoolean("asistencia_confirmada")
                    );
                    lista.add(inv);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Validar token de acceso (CU23)
    public Invitado validarToken(String token) {
        String sql = "SELECT i.*, r.id as id_reserva, r.fecha_evento FROM invitados i " +
                     "JOIN reservas r ON i.id_reserva = r.id WHERE i.token_acceso = ?";
        try (Connection con = ConexionController.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Invitado inv = new Invitado(
                        rs.getInt("id"), rs.getString("email"), rs.getString("nombre"),
                        "", rs.getString("dni"), rs.getString("telefono"),
                        rs.getString("token_acceso"), rs.getBoolean("asistencia_confirmada")
                    );
                    Reserva r = new Reserva();
                    r.setId(rs.getInt("id_reserva"));
                    r.setFechaEvento(rs.getDate("fecha_evento").toLocalDate());
                    inv.setReserva(r);
                    return inv;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Confirmar asistencia (CU25)
    public boolean confirmarAsistencia(int idInvitado) {
        String sql = "UPDATE invitados SET asistencia_confirmada = TRUE, fecha_confirmacion = NOW() WHERE id = ?";
        try (Connection con = ConexionController.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idInvitado);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // Obtener habitación asignada (CU28) - CORREGIDO CON EL NUEVO CONSTRUCTOR
    public Habitacion obtenerHabitacionInvitado(int idInvitado) {
        String sql = "SELECT h.* FROM habitaciones h JOIN invitados i ON i.id_habitacion = h.id WHERE i.id = ?";
        try (Connection con = ConexionController.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idInvitado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Usamos el constructor unificado que armamos en la convivencia:
                    // Habitacion(id, numero, tipo, capacidad, estado)
                    // Mapeamos temporalmente el estado como OCUPADA ya que el invitado la está consultando.
                    Habitacion hab = new Habitacion(
                        rs.getInt("id"),
                        rs.getString("numero"),
                        rs.getString("tipo"),
                        rs.getInt("capacidad"),
                        EstadoHabitacion.Completa
                    );
                    return hab;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}