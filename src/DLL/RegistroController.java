package DLL;

	import BLL.*;
	import java.sql.*;
	import java.util.ArrayList;
	import java.util.List;

	public class RegistroController {

		// CU23 - Validar token y obtener el invitado asociado
		public Invitado validarToken(String token) {
		    String sql = "SELECT i.id, i.email, i.nombre, i.dni, i.telefono, i.token_acceso, i.asistencia_confirmada, " +
		                 "r.id as reserva_id, r.fecha_evento, r.estado as reserva_estado " +
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
		                "", // apellido
		                rs.getString("dni"),
		                rs.getString("telefono"),
		                rs.getString("token_acceso"),
		                rs.getBoolean("asistencia_confirmada")
		            );
		            // Cargar datos de reserva
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
	    
	 // Obtener una habitación libre para la reserva del invitado
	    private Integer obtenerHabitacionLibre1(int idReserva) {
	        String sql = "SELECT id FROM habitaciones WHERE id_reserva = ? AND estado = 'LIBRE' LIMIT 1";
	        try (Connection con = ConexionController.getInstance().getConnection();
	             PreparedStatement ps = con.prepareStatement(sql)) {
	            ps.setInt(1, idReserva);
	            ResultSet rs = ps.executeQuery();
	            if (rs.next()) {
	                return rs.getInt("id");
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }

	    // Marcar habitación como ocupada y asignarla al invitado
	    private boolean asignarHabitacionAInvitado(int idInvitado, int idHabitacion, int idReserva) {
	        String sqlUpdateHabitacion = "UPDATE habitaciones SET estado = 'OCUPADA' WHERE id = ?";
	        String sqlUpdateInvitado = "UPDATE invitados SET id_habitacion = ? WHERE id = ?";
	        Connection con = ConexionController.getInstance().getConnection();
	        try {
	            con.setAutoCommit(false);
	            try (PreparedStatement ps1 = con.prepareStatement(sqlUpdateHabitacion)) {
	                ps1.setInt(1, idHabitacion);
	                ps1.executeUpdate();
	            }
	            try (PreparedStatement ps2 = con.prepareStatement(sqlUpdateInvitado)) {
	                ps2.setInt(1, idHabitacion);
	                ps2.setInt(2, idInvitado);
	                ps2.executeUpdate();
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
	    
	 // En RegistroController.java

	 // Obtener una habitación libre (pública para el admin)
	 public Integer obtenerHabitacionLibre(int idReserva) {
	     String sql = "SELECT id FROM habitaciones WHERE id_reserva = ? AND estado = 'LIBRE' LIMIT 1";
	     try (Connection con = ConexionController.getInstance().getConnection();
	          PreparedStatement ps = con.prepareStatement(sql)) {
	         ps.setInt(1, idReserva);
	         ResultSet rs = ps.executeQuery();
	         if (rs.next()) {
	             return rs.getInt("id");
	         }
	     } catch (SQLException e) {
	         e.printStackTrace();
	     }
	     return null;
	 }

	 // Asignar habitación a un invitado (pública para el admin)
	 public boolean asignarHabitacionAInvitado(int idInvitado, int idHabitacion, int idReserva) {
	     String sqlUpdateHabitacion = "UPDATE habitaciones SET estado = 'OCUPADA' WHERE id = ?";
	     String sqlUpdateInvitado = "UPDATE invitados SET id_habitacion = ? WHERE id = ?";
	     Connection con = ConexionController.getInstance().getConnection();
	     try {
	         con.setAutoCommit(false);
	         try (PreparedStatement ps1 = con.prepareStatement(sqlUpdateHabitacion)) {
	             ps1.setInt(1, idHabitacion);
	             ps1.executeUpdate();
	         }
	         try (PreparedStatement ps2 = con.prepareStatement(sqlUpdateInvitado)) {
	             ps2.setInt(1, idHabitacion);
	             ps2.setInt(2, idInvitado);
	             ps2.executeUpdate();
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
	    
	    // CU25 - Confirmar asistencia del invitado
	    public boolean confirmarAsistencia(int idInvitado) {
	        // Primero obtener id_reserva del invitado
	        String sqlReserva = "SELECT id_reserva FROM invitados WHERE id = ?";
	        Integer idReserva = null;
	        try (Connection con = ConexionController.getInstance().getConnection();
	             PreparedStatement ps = con.prepareStatement(sqlReserva)) {
	            ps.setInt(1, idInvitado);
	            ResultSet rs = ps.executeQuery();
	            if (rs.next()) {
	                idReserva = rs.getInt("id_reserva");
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	            return false;
	        }
	        if (idReserva == null) return false;

	        // Verificar si ya tiene habitación asignada
	        String checkHabitacion = "SELECT id_habitacion FROM invitados WHERE id = ?";
	        try (Connection con = ConexionController.getInstance().getConnection();
	             PreparedStatement ps = con.prepareStatement(checkHabitacion)) {
	            ps.setInt(1, idInvitado);
	            ResultSet rs = ps.executeQuery();
	            if (rs.next()) {
	                int idHab = rs.getInt("id_habitacion");
	                if (!rs.wasNull() && idHab > 0) {
	                    // Ya tiene habitación, solo confirmar asistencia
	                    String sqlConfirm = "UPDATE invitados SET asistencia_confirmada = TRUE, fecha_confirmacion = NOW() WHERE id = ?";
	                    try (PreparedStatement ps2 = con.prepareStatement(sqlConfirm)) {
	                        ps2.setInt(1, idInvitado);
	                        return ps2.executeUpdate() > 0;
	                    }
	                }
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

	        // No tiene habitación, buscar una libre
	        Integer idHabitacionLibre = obtenerHabitacionLibre1(idReserva);
	        if (idHabitacionLibre == null) {
	            System.err.println("No hay habitaciones libres para la reserva " + idReserva);
	            return false;
	        }

	        // Asignar habitación y confirmar asistencia
	        boolean asignada = asignarHabitacionAInvitado(idInvitado, idHabitacionLibre, idReserva);
	        if (!asignada) return false;

	        // Finalmente confirmar asistencia
	        String sqlConfirm = "UPDATE invitados SET asistencia_confirmada = TRUE, fecha_confirmacion = NOW() WHERE id = ?";
	        try (Connection con = ConexionController.getInstance().getConnection();
	             PreparedStatement ps = con.prepareStatement(sqlConfirm)) {
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
	        String sql = "SELECT h.numero FROM habitaciones h " +
	                     "JOIN invitados i ON i.id_habitacion = h.id " +
	                     "WHERE i.id = ?";
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

