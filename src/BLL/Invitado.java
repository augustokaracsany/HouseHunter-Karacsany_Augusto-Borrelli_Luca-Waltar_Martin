package BLL;

import java.time.LocalDateTime;
import java.util.List;

public class Invitado extends Persona {
    private String nombre;
    private String apellido;
    private String dni;
    private String telefono;
    private String tokenAcceso;
    private boolean asistenciaConfirmada;
    private LocalDateTime fechaConfirmacion;
    private Reserva reserva;
    private Habitacion habitacion;  // si ya tienes esta clase

    // ========== CONSTRUCTORES ==========
    // 1. Constructor original (usado por login)
    public Invitado(String email, String password, String nombre, Rol rol) {
        super(email, password, rol);
        this.nombre = nombre;
        this.apellido = "";
        this.asistenciaConfirmada = false;
    }

    // 2. Constructor completo para nuevos registros (nombre + apellido)
    public Invitado(String email, String password, String nombre, String apellido, Rol rol) {
        super(email, password, rol);
        this.nombre = nombre;
        this.apellido = apellido;
        this.asistenciaConfirmada = false;
    }

    // 3. Constructor para recuperar desde BD (sin password)
    public Invitado(int id, String email, String nombre, String apellido, String dni, String telefono, String tokenAcceso, boolean asistenciaConfirmada) {
        super(email, "", Rol.INVITADO);
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.telefono = telefono;
        this.tokenAcceso = tokenAcceso;
        this.asistenciaConfirmada = asistenciaConfirmada;
    }

    // ========== MÉTODOS ==========
    @Override
    public String getNombre() {
        return nombre + (apellido != null && !apellido.isEmpty() ? " " + apellido : "");
    }

    	public void mostrarMenu() {
    	    javax.swing.JOptionPane.showMessageDialog(null, "¡Bienvenido " + getNombre() + "! Para acceder necesitas tu token.");
    	    String token = javax.swing.JOptionPane.showInputDialog("Ingrese su token de acceso:");
    	    if (token == null || token.trim().isEmpty()) {
    	        javax.swing.JOptionPane.showMessageDialog(null, "Token no ingresado. Saliendo del menú.");
    	        return;
    	    }

    	    DLL.RegistroController registroCtrl = new DLL.RegistroController();
    	    Invitado invitadoLogeado = registroCtrl.validarToken(token);
    	    if (invitadoLogeado == null) {
    	        javax.swing.JOptionPane.showMessageDialog(null, "Token inválido o evento cancelado. Acceso denegado.");
    	        return;
    	    }

    	    // Guardamos en la instancia actual los datos del invitado real (incluido id)
    	    this.id = invitadoLogeado.getId();
    	    this.reserva = invitadoLogeado.getReserva();
    	    this.tokenAcceso = token;
    	    this.asistenciaConfirmada = invitadoLogeado.isAsistenciaConfirmada();

    	    String[] opciones = {
    	        " Visualizar cronograma",
    	        " Confirmar mi asistencia",
    	        " Consultar actividades",
    	        " Ver mi habitación",
    	        " Participar en premios",
    	        " Cerrar sesión"
    	    };

    	    int opcion;
    	    do {
    	        opcion = javax.swing.JOptionPane.showOptionDialog(
    	            null, "Menú de Invitado - " + getNombre(),
    	            "HouseHunter", javax.swing.JOptionPane.DEFAULT_OPTION,
    	            javax.swing.JOptionPane.INFORMATION_MESSAGE,
    	            null, opciones, opciones[0]
    	        );

    	        switch (opcion) {
    	            case 0: // Cronograma
    	                List<Actividad> actividades = registroCtrl.obtenerCronograma(this.reserva.getId());
    	                if (actividades.isEmpty()) {
    	                    javax.swing.JOptionPane.showMessageDialog(null, "No hay actividades programadas aún.");
    	                } else {
    	                    StringBuilder sb = new StringBuilder("📅 CRONOGRAMA DEL EVENTO:\n\n");
    	                    for (Actividad a : actividades) {
    	                        sb.append("• ").append(a.getNombre())
    	                          .append(" - ").append(a.getFechaHora().toString().replace("T", " "))
    	                          .append(" (Duración: ").append(a.getDuracionMinutos()).append(" min)\n");
    	                    }
    	                    javax.swing.JOptionPane.showMessageDialog(null, sb.toString());
    	                }
    	                break;
    	            case 1: // Confirmar asistencia
    	                if (this.asistenciaConfirmada) {
    	                    javax.swing.JOptionPane.showMessageDialog(null, "Ya has confirmado tu asistencia anteriormente.");
    	                } else {
    	                    boolean ok = registroCtrl.confirmarAsistencia(this.id);
    	                    if (ok) {
    	                        this.asistenciaConfirmada = true;
    	                        javax.swing.JOptionPane.showMessageDialog(null, "✅ Asistencia confirmada. ¡Te esperamos!");
    	                    } else {
    	                        javax.swing.JOptionPane.showMessageDialog(null, "Error al confirmar asistencia.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
    	                    }
    	                }
    	                break;
    	            case 2: // Consultar actividades con detalles (CU26 + CU27)
    	                List<Actividad> acts = registroCtrl.listarActividadesPorReserva(this.reserva.getId());
    	                if (acts.isEmpty()) {
    	                    javax.swing.JOptionPane.showMessageDialog(null, "No hay actividades disponibles.");
    	                    break;
    	                }
    	                String[] nombresActs = acts.stream().map(Actividad::getNombre).toArray(String[]::new);
    	                int sel = javax.swing.JOptionPane.showOptionDialog(
    	                    null, "Seleccione una actividad para ver detalles:", "Actividades",
    	                    javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE,
    	                    null, nombresActs, nombresActs[0]
    	                );
    	                if (sel >= 0) {
    	                    Actividad a = acts.get(sel);
    	                    String detalle = String.format(
    	                        "📌 %s\n\nDescripción: %s\nFecha: %s\nDuración: %d min\nCupo máximo: %d\nImportancia: %s\nCategoría: %s",
    	                        a.getNombre(),
    	                        a.getDescripcion() != null ? a.getDescripcion() : "(sin descripción)",
    	                        a.getFechaHora().toString().replace("T", " "),
    	                        a.getDuracionMinutos(),
    	                        a.getCupoMaximo(),
    	                        a.getImportancia(),
    	                        a.getCategoria()
    	                    );
    	                    javax.swing.JOptionPane.showMessageDialog(null, detalle);
    	                    // Opcional: preguntar si desea registrar su asistencia a esa actividad (si es presencial)
    	                    int confirm = javax.swing.JOptionPane.showConfirmDialog(null, "¿Desea registrar su asistencia a esta actividad?", "Registrar asistencia", javax.swing.JOptionPane.YES_NO_OPTION);
    	                    if (confirm == javax.swing.JOptionPane.YES_OPTION) {
    	                        if (registroCtrl.registrarAsistenciaActividad(this.id, a.getId())) {
    	                            javax.swing.JOptionPane.showMessageDialog(null, "Asistencia registrada correctamente.");
    	                        } else {
    	                            javax.swing.JOptionPane.showMessageDialog(null, "No se pudo registrar (quizás ya estaba registrado).");
    	                        }
    	                    }
    	                }
    	                break;
    	            case 3: // Consultar habitación (CU28)
    	                String hab = registroCtrl.obtenerHabitacionAsignada(this.id);
    	                if (hab == null) {
    	                    javax.swing.JOptionPane.showMessageDialog(null, "Aún no se le ha asignado una habitación. Consulte en recepción.");
    	                } else {
    	                    javax.swing.JOptionPane.showMessageDialog(null, "Su habitación asignada es la Nº " + hab);
    	                }
    	                break;
    	            case 4: // Participar en premios (CU29-31)
    	                boolean cumple = registroCtrl.verificarAsistenciaMinima(this.id, 2);
    	                if (!cumple) {
    	                    javax.swing.JOptionPane.showMessageDialog(null, "No cumple con el mínimo de 2 actividades asistidas. No puede participar.");
    	                } else {
    	                    String voucher = registroCtrl.generarVoucher();
    	                    javax.swing.JOptionPane.showMessageDialog(null, "🎉 ¡Felicidades! Participa en el sorteo.\nSu voucher es: " + voucher);
    	                }
    	                break;
    	            case 5:
    	                javax.swing.JOptionPane.showMessageDialog(null, "Sesión cerrada. ¡Gracias por participar!");
    	                break;
    	        }
    	    } while (opcion != 5);
    	}
    

    // ========== GETTERS Y SETTERS ==========
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getTokenAcceso() { return tokenAcceso; }
    public void setTokenAcceso(String tokenAcceso) { this.tokenAcceso = tokenAcceso; }
    public boolean isAsistenciaConfirmada() { return asistenciaConfirmada; }
    public void setAsistenciaConfirmada(boolean asistenciaConfirmada) { this.asistenciaConfirmada = asistenciaConfirmada; }
    public LocalDateTime getFechaConfirmacion() { return fechaConfirmacion; }
    public void setFechaConfirmacion(LocalDateTime fechaConfirmacion) { this.fechaConfirmacion = fechaConfirmacion; }
    public Reserva getReserva() { return reserva; }
    public void setReserva(Reserva reserva) { this.reserva = reserva; }
    public Habitacion getHabitacion() { return habitacion; }
    public void setHabitacion(Habitacion habitacion) { this.habitacion = habitacion; }
}