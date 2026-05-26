package DLL;

	import BLL.*;
	import java.sql.*;
	import java.util.ArrayList;
	import java.util.List;

	public class RegistroController {

	    // CU23 - Validar token y obtener el invitado asociado
	    public Invitado validarToken(String token) {
	        String sql = "SELECT i.*, r.id as reserva_id, r.fecha_evento, r.estado as reserva_estado " +
	                     "FROM invitados i " +
	                     "JOIN reservas r ON i.id_reserva = r.id " +
	                     "WHERE i.token_acceso = ? AND r.estado != 'CANCELADA'";
	        try (Connection con = ConexionController.getInstance().getConnection();
	             PreparedStatement ps = con.prepareStatement(sql)) {
	            ps.setString(1, token);
	            ResultSet rs = ps.executeQuery();
	            if (rs.next()) {
	                Invitado inv = new Invitado(
	                    rs.getInt("id"),
	                    rs.getString("email"),
	                    rs.getString("nombre"),
	                    "", // apellido no está en esta tabla, se podría agregar después
	                    rs.getString("dni"),
	                    rs.getString("telefono"),
	                    rs.getString("token_acceso"),
	                    rs.getBoolean("asistencia_confirmada")
	                );
	                // Cargar datos de reserva (para saber a qué evento pertenece)
	                Reserva r = new Reserva();
	                r.setId(rs.getInt("reserva_id"));
	                r.setFechaEvento(rs.getDate("fecha_evento").toLocalDate());
	                inv.setReserva(r);
	                return inv;
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }

	    // Obtener cronograma (actividades) de la reserva a la que pertenece el invitado
	    public List<Actividad> obtenerCronograma(int idReserva) {
	        return new EventoController().obtenerActividadesPorReserva(idReserva);
	    }

	    // CU25 - Confirmar asistencia del invitado
	    public boolean confirmarAsistencia(int idInvitado) {
	        String sql = "UPDATE invitados SET asistencia_confirmada = TRUE, fecha_confirmacion = NOW() WHERE id = ?";
	        try (Connection con = ConexionController.getInstance().getConnection();
	             PreparedStatement ps = con.prepareStatement(sql)) {
	            ps.setInt(1, idInvitado);
	            return ps.executeUpdate() > 0;
	        } catch (SQLException e) {
	            e.printStackTrace();
	            return false;
	        }
	    }

	    // Obtener lista de actividades de una reserva (para mostrar al invitado)
	    public List<Actividad> listarActividadesPorReserva(int idReserva) {
	        return new EventoController().obtenerActividadesPorReserva(idReserva);
	    }

	    // Obtener detalle de una actividad específica (CU27)
	    public Actividad obtenerDetalleActividad(int idActividad) {
	        String sql = "SELECT * FROM actividades WHERE id = ?";
	        try (Connection con = ConexionController.getInstance().getConnection();
	             PreparedStatement ps = con.prepareStatement(sql)) {
	            ps.setInt(1, idActividad);
	            ResultSet rs = ps.executeQuery();
	            if (rs.next()) {
	                Actividad a = new Actividad();
	                a.setId(rs.getInt("id"));
	                a.setNombre(rs.getString("nombre"));
	                a.setDescripcion(rs.getString("descripcion"));
	                a.setFechaHora(rs.getTimestamp("fecha_hora").toLocalDateTime());
	                a.setDuracionMinutos(rs.getInt("duracion_minutos"));
	                a.setCupoMaximo(rs.getInt("cupo_maximo"));
	                a.setImportancia(Importancia.valueOf(rs.getString("importancia")));
	                a.setCategoria(rs.getString("categoria"));
	                return a;
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }

	    // Registrar presencia del invitado en una actividad (se llama cuando escanea QR o confirma)
	    public boolean registrarAsistenciaActividad(int idInvitado, int idActividad) {
	        String sql = "INSERT INTO asistencia_actividades (id_invitado, id_actividad, presente) VALUES (?, ?, TRUE) " +
	                     "ON DUPLICATE KEY UPDATE presente = TRUE";
	        try (Connection con = ConexionController.getInstance().getConnection();
	             PreparedStatement ps = con.prepareStatement(sql)) {
	            ps.setInt(1, idInvitado);
	            ps.setInt(2, idActividad);
	            return ps.executeUpdate() > 0;
	        } catch (SQLException e) {
	            e.printStackTrace();
	            return false;
	        }
	    }

	    // CU28 - Obtener número de habitación asignada al invitado
	    public String obtenerHabitacionAsignada(int idInvitado) {
	        String sql = "SELECT h.numero FROM asignaciones_habitacion ah " +
	                     "JOIN habitaciones h ON ah.id_habitacion = h.id " +
	                     "WHERE ah.id_invitado = ?";
	        try (Connection con = ConexionController.getInstance().getConnection();
	             PreparedStatement ps = con.prepareStatement(sql)) {
	            ps.setInt(1, idInvitado);
	            ResultSet rs = ps.executeQuery();
	            if (rs.next()) {
	                return rs.getString("numero");
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }

	    // CU29-CU31 - Participar en premios: verifica mínimo 2 asistencias a actividades
	    public boolean verificarAsistenciaMinima(int idInvitado, int minimo) {
	        String sql = "SELECT COUNT(*) FROM asistencia_actividades WHERE id_invitado = ? AND presente = TRUE";
	        try (Connection con = ConexionController.getInstance().getConnection();
	             PreparedStatement ps = con.prepareStatement(sql)) {
	            ps.setInt(1, idInvitado);
	            ResultSet rs = ps.executeQuery();
	            if (rs.next()) {
	                return rs.getInt(1) >= minimo;
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return false;
	    }

	    // Generar un voucher (simulado) para el sorteo
	    public String generarVoucher() {
	        return "VOUCHER-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	    }
	}

