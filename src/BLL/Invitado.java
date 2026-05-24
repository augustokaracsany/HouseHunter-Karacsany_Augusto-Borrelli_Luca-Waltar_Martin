package BLL;

import DLL.EventoController;
import DLL.InvitadoController;
import DLL.PremioController;
import DLL.ReporteController;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

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
    private Reserva reserva;      // Reserva asociada al invitado
    private Habitacion habitacion;

    // Controladores (Se corrigió PremioController a su patrón Singleton)
    private transient InvitadoController invitadoController = InvitadoController.getInstance();
    private transient EventoController eventoController = EventoController.getInstance();
    private transient PremioController premioController = PremioController.getInstance(); 
    private transient ReporteController reporteController = new ReporteController();

    // =========================================================================
    // CONSTRUCTORES LIMPIOS (Sin duplicados)
    // =========================================================================

    // 1. Constructor básico (Email, Password, Nombre, Rol)
    public Invitado(String email, String password, String nombre, Rol rol) {
        super(email, password, rol);
        this.nombre = nombre;
        this.apellido = "";
        this.asistenciaConfirmada = false;
    }

    // 2. Constructor completo para nuevos registros (Email, Password, Nombre, Apellido, Rol)
    public Invitado(String email, String password, String nombre, String apellido, Rol rol) {
        super(email, password, rol);
        this.nombre = nombre;
        this.apellido = apellido;
        this.asistenciaConfirmada = false;
    }

    // 3. Constructor para recuperar desde BD (Con ID, datos personales y asistencia)
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

    // ======================== MENÚ PRINCIPAL DEL INVITADO ========================
    @Override
    public void mostrarMenu() {
        // Validación de token si no tiene reserva
        if (reserva == null) {
            String token = JOptionPane.showInputDialog(null, 
                "Para acceder a su evento, ingrese el token que recibió por correo:\n(Ej: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx)", 
                "Validación de acceso (CU23)", JOptionPane.QUESTION_MESSAGE);
            if (token == null || token.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Acceso denegado. No se proporcionó token.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Invitado invitadoValidado = InvitadoController.getInstance().validarToken(token.trim());
            if (invitadoValidado == null) {
                JOptionPane.showMessageDialog(null, "Token inválido o expirado. No puede acceder al sistema.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            this.id = invitadoValidado.getId();
            this.reserva = invitadoValidado.getReserva();
            this.tokenAcceso = invitadoValidado.getTokenAcceso();
            this.asistenciaConfirmada = invitadoValidado.isAsistenciaConfirmada();
            JOptionPane.showMessageDialog(null, "✅ Acceso concedido. Bienvenido al evento: " + reserva.getFechaEvento());
        }

        // Menú principal
        ImageIcon iconoInvitado = new ImageIcon("src/img/HouseHunter_Menu-Invitado.png");
        String tituloHtml = "<html><body style='width: 350px; text-align: center;'>"
                          + "<h2>🎟️ Panel del Invitado</h2>"
                          + "<b>Hola, " + getNombre() + "</b><br>"
                          + "Evento: " + reserva.getFechaEvento() + "<br>"
                          + "Estado asistencia: " + (asistenciaConfirmada ? "✔️ Confirmada" : "⏳ Pendiente")
                          + "<hr>Seleccione una opción:</body></html>";

        String[] opciones = {
            "📅 Ver cronograma completo (CU24)",
            "✅ Confirmar mi asistencia (CU25)",
            "🎯 Explorar actividades (CU26)",
            "🔍 Ver detalle de una actividad (CU27)",
            "🛏️ Consultar mi habitación (CU28)",
            "🎁 Participar en sorteos / Premios (CU29)",
            "🎫 Obtener voucher (CU31)",
            "❌ Cerrar sesión"
        };

        int seleccion;
        do {
            seleccion = JOptionPane.showOptionDialog(null, tituloHtml, "HouseHunter - Invitado",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    iconoInvitado, opciones, opciones[0]);

            switch (seleccion) {
                case 0: verCronograma(); break;
                case 1: confirmarAsistencia(); break;
                case 2: listarActividades(); break;
                case 3: verDetalleActividad(); break;
                case 4: consultarHabitacion(); break;
                case 5: participarSorteo(); break;
                case 6: obtenerVoucher(); break;
                case 7: JOptionPane.showMessageDialog(null, "Sesión cerrada. ¡Hasta pronto!"); break;
            }
        } while (seleccion != 7 && seleccion != -1);
    }

    // ======================== IMPLEMENTACIÓN DE CADA CASO DE USO ========================

    // CU24: Visualizar cronograma
    private void verCronograma() {
        if (reserva == null) { mostrarErrorSinReserva(); return; }
        List<Actividad> actividades = eventoController.obtenerActividadesPorReserva(reserva.getId());
        if (actividades.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay actividades programadas para este evento aún.", "Cronograma", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder("📅 CRONOGRAMA DEL EVENTO\n\n");
        for (Actividad a : actividades) {
            sb.append("🔹 ").append(a.getNombre())
              .append("\n    📅 Fecha: ").append(a.getFechaHora().toLocalDate())
              .append(" ⏰ Hora: ").append(a.getFechaHora().toLocalTime())
              .append("\n    ⏱️ Duración: ").append(a.getDuracionMinutos()).append(" min")
              .append("\n    🎯 Categoría: ").append(a.getCategoria())
              .append("\n    ⭐ Importancia: ").append(a.getImportancia())
              .append("\n    👥 Cupo máx.: ").append(a.getCupoMaximo())
              .append("\n\n");
        }
        JOptionPane.showMessageDialog(null, sb.toString(), "Cronograma - CU24", JOptionPane.PLAIN_MESSAGE);
    }

    // CU25: Confirmar asistencia
    private void confirmarAsistencia() {
        if (asistenciaConfirmada) {
            JOptionPane.showMessageDialog(null, "Ya has confirmado tu asistencia anteriormente.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int opcion = JOptionPane.showConfirmDialog(null, 
            "¿Confirmas tu asistencia al evento del día " + reserva.getFechaEvento() + "?\nEsta acción no se puede deshacer.", 
            "Confirmar asistencia - CU25", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opcion == JOptionPane.YES_OPTION) {
            boolean ok = invitadoController.confirmarAsistencia(this.id);
            if (ok) {
                this.asistenciaConfirmada = true;
                JOptionPane.showMessageDialog(null, "¡Gracias! Tu asistencia ha sido registrada. ✅");
            } else {
                JOptionPane.showMessageDialog(null, "Error al confirmar asistencia. Intente más tarde.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // CU26: Consultar actividades (lista resumida)
    private void listarActividades() {
        if (reserva == null) { mostrarErrorSinReserva(); return; }
        List<Actividad> actividades = eventoController.obtenerActividadesPorReserva(reserva.getId());
        if (actividades.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay actividades cargadas.", "Actividades - CU26", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String[] nombres = actividades.stream().map(Actividad::getNombre).toArray(String[]::new);
        JOptionPane.showOptionDialog(null, "Seleccione una actividad para ver más detalles (CU27):", 
                "Lista de actividades - CU26", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, nombres, nombres[0]);
    }

    // CU27: Ver detalles actividad (seleccionando una)
    private void verDetalleActividad() {
        if (reserva == null) { mostrarErrorSinReserva(); return; }
        List<Actividad> actividades = eventoController.obtenerActividadesPorReserva(reserva.getId());
        if (actividades.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay actividades disponibles.", "Detalle - CU27", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String[] nombres = actividades.stream().map(Actividad::getNombre).toArray(String[]::new);
        int idx = JOptionPane.showOptionDialog(null, "Seleccione la actividad que desea consultar:", 
                "Detalle de actividad - CU27", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, nombres, nombres[0]);
        if (idx >= 0) {
            Actividad a = actividades.get(idx);
            String detalle = String.format(
                "📌 NOMBRE: %s\n\n📝 Descripción: %s\n\n⏰ Fecha y hora: %s\n⏱️ Duración: %d min\n👥 Cupo máximo: %d\n🏷️ Categoría: %s\n⭐ Importancia: %s",
                a.getNombre(),
                a.getDescripcion() != null ? a.getDescripcion() : "(Sin descripción)",
                a.getFechaHora(),
                a.getDuracionMinutos(),
                a.getCupoMaximo(),
                a.getCategoria(),
                a.getImportancia()
            );
            JOptionPane.showMessageDialog(null, detalle, "Detalle completo - CU27", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // CU28: Consultar datos de habitación
    private void consultarHabitacion() {
        Habitacion hab = invitadoController.obtenerHabitacionInvitado(this.id);
        if (hab == null) {
            JOptionPane.showMessageDialog(null, "Aún no se le ha asignado una habitación. Consulte con recepción.", 
                    "Habitación - CU28", JOptionPane.WARNING_MESSAGE);
        } else {
            String info = String.format("🏨 Su habitación asignada:\n\nNúmero: %s\nTipo: %s\nCapacidad: %d personas",
                    hab.getNumero(), hab.getTipo(), hab.getCapacidad());
            JOptionPane.showMessageDialog(null, info, "Mi habitación - CU28", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // CU29: Participar en premios (sorteo)
    private void participarSorteo() {
        if (!asistenciaConfirmada) {
            JOptionPane.showMessageDialog(null, "Debe confirmar su asistencia al evento antes de participar en sorteos.", 
                    "Requisito - CU30 (asistencia mínima)", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<Premio> premios = premioController.listarPremiosDisponibles();
        if (premios.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay premios activos en este momento.", "Premios", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String[] nombresPremios = premios.stream().map(Premio::getNombre).toArray(String[]::new);
        int sel = JOptionPane.showOptionDialog(null, "Seleccione el premio al que desea participar:", 
                "Participar en sorteo - CU29", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, nombresPremios, nombresPremios[0]);
        if (sel >= 0) {
            Premio p = premios.get(sel);
            boolean exito = premioController.participarEnSorteo(this.id, p.getId());
            if (exito) {
                JOptionPane.showMessageDialog(null, "¡Has participado correctamente!\nSe verificará tu elegibilidad (CU30) y si resultas ganador podrás obtener un voucher (CU31).", 
                        "Participación registrada", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "No se pudo registrar la participación. Quizás ya participaste en este premio.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // CU31: Obtener voucher (si es ganador de algún premio)
    private void obtenerVoucher() {
        List<Premio> premios = premioController.listarPremiosDisponibles();
        if (premios.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay premios activos.", "Voucher - CU31", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String[] nombresPremios = premios.stream().map(Premio::getNombre).toArray(String[]::new);
        int sel = JOptionPane.showOptionDialog(null, "¿Para qué premio desea obtener su voucher?", 
                "Obtener voucher - CU31", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, nombresPremios, nombresPremios[0]);
        if (sel >= 0) {
            Premio p = premios.get(sel);
            String voucher = premioController.obtenerVoucher(this.id, p.getId());
            if (voucher != null && !voucher.isEmpty()) {
                JOptionPane.showMessageDialog(null, "🎫 ¡Felicidades! Usted es ganador.\n\nVoucher: " + voucher + "\nPresente este código en recepción para canjear su premio.", 
                        "Voucher - CU31", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "No es ganador de este premio o aún no se ha realizado el sorteo.\nVerifique más tarde.", 
                        "No ganador", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void mostrarErrorSinReserva() {
        JOptionPane.showMessageDialog(null, "No hay información de reserva asociada a su cuenta. Contacte al organizador.", 
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ======================== GETTERS Y SETTERS ========================
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