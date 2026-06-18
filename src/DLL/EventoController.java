package DLL;
	
import BLL.*;
import Repository.Hashing;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

	public class EventoController {

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
		    // Generar habitaciones según el número de invitados
		    generarHabitacionesParaReserva(reserva.getId(), reserva.getNumInvitados());
		    return reserva;
		}
	    
	 // Después de crearReserva, genera las habitaciones automáticamente
	    public boolean generarHabitacionesParaReserva(int idReserva, int numHabitaciones) {
	        String sql = "INSERT INTO habitaciones (numero, id_reserva, estado) VALUES (?, ?, 'LIBRE')";
	        Connection con = ConexionController.getInstance().getConnection();
	        try {
	            con.setAutoCommit(false);
	            try (PreparedStatement ps = con.prepareStatement(sql)) {
	                for (int i = 1; i <= numHabitaciones; i++) {
	                    String numero = String.format("%03d", i); // Ej: 001, 002, ...
	                    ps.setString(1, numero);
	                    ps.setInt(2, idReserva);
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
	                // Crear objeto plantilla ligero
	                Plantilla p = new Plantilla();
	                p.setId(rs.getInt("id_plantilla"));
	                p.setNombre(rs.getString("plantilla_nombre"));
	                r.setPlantilla(p);
	                // La empresa se asignará desde fuera
	                reservas.add(r);
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return reservas;
	    }

	    // Guardar cronograma completo (CU12)
	    public boolean guardarCronograma(int idReserva, List<Actividad> actividades) {
	        // Primero eliminar actividades anteriores para esa reserva (opcional)
	        String deleteSql = "DELETE FROM actividades WHERE id_reserva = ?";
	        String insertSql = "INSERT INTO actividades (id_reserva, nombre, descripcion, fecha_hora, duracion_minutos, cupo_maximo, importancia, categoria) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	        Connection con = ConexionController.getInstance().getConnection();
	        try {
	            con.setAutoCommit(false);
	            // Eliminar existentes
	            try (PreparedStatement psDel = con.prepareStatement(deleteSql)) {
	                psDel.setInt(1, idReserva);
	                psDel.executeUpdate();
	            }
	            // Insertar nuevas
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
	 // En EventoController.java
	    public List<Reserva> listarTodasReservas() {
	        List<Reserva> reservas = new ArrayList<>();
	        String sql = "SELECT r.*, p.nombre as plantilla_nombre, u.email as empresa_email " +
	                     "FROM reservas r " +
	                     "LEFT JOIN plantillas p ON r.id_plantilla = p.id " +
	                     "LEFT JOIN usuarios u ON r.id_empresa = u.id " +
	                     "ORDER BY r.fecha_evento DESC";
	        try (Connection con = ConexionController.getInstance().getConnection();
	             PreparedStatement ps = con.prepareStatement(sql);
	             ResultSet rs = ps.executeQuery()) {
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
	                // Cargar empresa (solo email para mostrar)
	                Empresa emp = new Empresa(rs.getString("empresa_email"), "", "", "", Rol.EMPRESA);
	                emp.setId(rs.getInt("id_empresa"));
	                r.setEmpresa(emp);
	                reservas.add(r);
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return reservas;
	    }

	    // Asignar plantilla a reserva (CU07)
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

	    // Obtener todas las plantillas disponibles
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
}

