package BLL;

import DLL.ActividadController;
import DLL.EventoController;
import DLL.HabitacionController;
import DLL.PremioController;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class Administrador extends Persona {
    private String dni;
    private String nombreCompleto;

    public Administrador(String email, String password, String nombreCompleto, String dni, Rol rol) {
        super(email, password, rol);
        this.dni = dni;
        this.nombreCompleto = nombreCompleto;
    }

    @Override
    public String getNombre() {
        return nombreCompleto;
    }

    @Override
    public void mostrarMenu() {
        ImageIcon iconoMenu = new ImageIcon("src/img/HouseHunter_Menu-Administrador.png");
        
        String tituloHtml = "<html><body style='width: 300px; text-align: center;'>"
                          + "<h2>🔑 Panel de Recepción</h2>"
                          + "<b>Admin:</b> " + getNombre() 
                          + "<hr>Seleccione un área de gestión:</body></html>";

        String[] modulos = {
            "🏨 RECEPCIÓN", 
            "🎮 ACTIVIDADES", 
            "📊 REPORTES", 
            "❌ CERRAR SESIÓN"
        };

        int seleccion;
        do {
            seleccion = JOptionPane.showOptionDialog(
                null, tituloHtml, "HouseHunter v1.0",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                iconoMenu, modulos, modulos[0]
            );

            switch (seleccion) {
                case 0: subMenuRecepcion(); break; 
                case 1: subMenuActividades(); break; 
                case 2: subMenuAdminReportes(); break; 
                case 3: JOptionPane.showMessageDialog(null, "Sesión cerrada de " + getNombre()); break;
            }
        } while (seleccion != 3 && seleccion != -1);
    }

    private void subMenuRecepcion() {
        ImageIcon iconoRecepcion = new ImageIcon("src/img/HouseHunter_Menu-Administrador_Recepcion.png"); 
        String[] opciones = {"Registrar Check-In", "Monitorear Habitaciones", "Volver"};
        
        int op = JOptionPane.showOptionDialog(
            null, 
            "<html><body style='width:250px; text-align:center;'><h3>Gestión de Recepción</h3>Seleccione una operación:</body></html>", 
            "Módulo de Recepción", 
            JOptionPane.DEFAULT_OPTION, 
            JOptionPane.PLAIN_MESSAGE, 
            iconoRecepcion, 
            opciones, 
            opciones[0]
        );
        
        if (op == 0) { // 🏨 Registrar Check-In
            ejecutarFlujoCheckInCompleto();
            
        } else if (op == 1) { // 🔍 Monitorear Habitaciones
            // Cambiado al controlador específico de habitaciones
            String estadoHabitacionesHtml = HabitacionController.getInstance().obtenerEstadoHabitacionesHtml(); 
            
         // Lo mostramos usando el banner verde estético de RECEPCIÓN
            JOptionPane.showMessageDialog(
                null, 
                estadoHabitacionesHtml, 
                "Control de Ocupación Real", 
                JOptionPane.PLAIN_MESSAGE, 
                iconoRecepcion
            );
        }
    }

    private void subMenuActividades() {
        ImageIcon iconoActividades = new ImageIcon("src/img/HouseHunter_Menu-Administrador_Actividades.png");
        String[] opciones = {"Monitorear actividades", "Visualizar cronograma", "Actualizar estado", "Entregar premio", "Volver"};
        
        int op = JOptionPane.showOptionDialog(
            null, "<html><body style='width:250px; text-align:center;'><h3>Control de Eventos</h3>Seleccione una opción:</body></html>", 
            "Actividades", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, 
            iconoActividades, opciones, opciones[0]
        );
        
     // Agrupamos el caso 0 y 2 para ofrecer una experiencia fluida de monitoreo y carga de datos
        if (op == 0 || op == 2) { 
            String codEvento = JOptionPane.showInputDialog(null, "Ingrese el Código Único del Evento:", "Control de Asistencias", JOptionPane.QUESTION_MESSAGE);
            if (codEvento == null || codEvento.trim().isEmpty()) return;
            
            // Busco las actividades de este evento en la BD para armar el combo de selección
            // Usa limpiamente tu patrón Singleton nativo
            String[] actividades = ActividadController.getInstance().obtenerNombresActividades(codEvento.trim());
            
            if (actividades.length == 0) {
                JOptionPane.showMessageDialog(null, "❌ No hay actividades cargadas o el evento no existe.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
         // El administrador elige de forma segura qué actividad quiere monitorear/actualizar
            String actividadSeleccionada = (String) JOptionPane.showInputDialog(
                null, "Seleccione la actividad a gestionar:", "Monitoreo de Bloques",
                JOptionPane.PLAIN_MESSAGE, iconoActividades, actividades, actividades[0]
            );
            
         // Impactamos la base de datos de forma directa mediante el controlador
            if (actividadSeleccionada != null) {
                String dniInvitado = JOptionPane.showInputDialog(null, "Ingrese el DNI del Invitado que asistió:", "Tomar Asistencia", JOptionPane.QUESTION_MESSAGE);
                if (dniInvitado != null && !dniInvitado.trim().isEmpty()) {
                    // Sincronizado mediante Singleton
                    ActividadController.getInstance().registrarAsistenciaActividad(codEvento.trim(), actividadSeleccionada, dniInvitado.trim());
                }
            }
            
        } else if (op == 1) { // 📊 Visualizar cronograma
            String codEvento = JOptionPane.showInputDialog(null, "Ingrese el Código Único del Evento:", "Consultar Cronograma", JOptionPane.QUESTION_MESSAGE);
            if (codEvento != null && !codEvento.trim().isEmpty()) {
                // Sincronizado mediante Singleton
                String agendaHtml = ActividadController.getInstance().obtenerCronogramaEventos(codEvento.trim());
                JOptionPane.showMessageDialog(null, agendaHtml, "Agenda - Evento: " + codEvento.trim(), JOptionPane.PLAIN_MESSAGE, iconoActividades);
            }
        } else if (op == 3) {
            subMenuPremio();
        }
    }

    private void subMenuAdminReportes() {
        ImageIcon iconoReportes = new ImageIcon("src/img/HouseHunter_Menu-Administrador_Reportes.png");
        String[] opciones = {"Generar reporte de evento", "Volver"};
        
        int op = JOptionPane.showOptionDialog(
            null, "<html><body style='width:250px; text-align:center;'><h3>Módulo de Reportes</h3>Seleccione una acción:</body></html>", 
            "Reportes", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, 
            iconoReportes, opciones, opciones[0]
        );
        
        if (op == 0) { // 📊 Generar reporte de evento
            String codEvento = JOptionPane.showInputDialog(null, "Ingrese el Código Único del Evento para consolidar:", "Generar Reporte", JOptionPane.QUESTION_MESSAGE);
            if (codEvento != null && !codEvento.trim().isEmpty()) {
                // Cambiado a EventoController (Analítica cross-table)
                String reporteHtml = EventoController.getInstance().obtenerReporteConsolidadoEvento(codEvento.trim());
                JOptionPane.showMessageDialog(null, reporteHtml, "Métricas del Evento: " + codEvento.trim(), JOptionPane.PLAIN_MESSAGE, iconoReportes);
            }
        }
    }

    private void subMenuCheckIn() {
        ImageIcon iconoRecepcion = new ImageIcon("src/img/HouseHunter_Menu-Administrador_Recepcion.png");
        String[] sub = {"Validar invitado autorizado", "Registrar check-in exitoso", "Volver"};
        String op;
        do {
            op = (String)JOptionPane.showInputDialog(
                null, "Seleccione la operación de check-in:", "Submenú Check-in", 
                JOptionPane.PLAIN_MESSAGE, iconoRecepcion, sub, sub[0]
            );
            
            if(op == null || op.equals("Volver")) break;
            
            if(op.equals("Validar invitado autorizado")) {
                String codEvento = JOptionPane.showInputDialog(null, "Ingrese el Código Único del Evento:", "Validación Previa", JOptionPane.QUESTION_MESSAGE);
                if (codEvento != null && !codEvento.trim().isEmpty()) {
                    String dniInvitado = JOptionPane.showInputDialog(null, "Ingrese el DNI del Invitado:", "Validación Previa", JOptionPane.QUESTION_MESSAGE);
                    if (dniInvitado != null && !dniInvitado.trim().isEmpty()) {
                        
                        // Cambiado a EventoController para chequear listas previas
                        boolean autorizado = EventoController.getInstance().validarInvitadoPrevia(codEvento.trim(), dniInvitado.trim());
                        if (autorizado) {
                            JOptionPane.showMessageDialog(null, "✅ El invitado con DNI " + dniInvitado + " se encuentra AUTORIZADO en la lista previa.", "Verificación Exitosa", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, "❌ El invitado NO figura en la lista previa de este evento o el código es incorrecto.", "Verificación Fallida", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            } else if(op.equals("Registrar check-in exitoso")) {
                ejecutarFlujoCheckInCompleto();
            }
            
        } while (!op.equals("Volver"));
    }

    private void subMenuHabitacion() {
        ImageIcon iconoRecepcion = new ImageIcon("src/img/HouseHunter_Menu-Administrador_Recepcion.png");
        String[] sub = {"Validar disponibilidad (Ver Estado)", "Asignar habitación directa", "Volver"};
        
        int op = JOptionPane.showOptionDialog(
            null, "<html><body style='width:250px; text-align:center;'><h3>Gestión Habitaciones</h3></body></html>", 
            "Submenú Habitaciones", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, 
            iconoRecepcion, sub, sub[0]
        );
        
        if (op == 0) {
            JOptionPane.showMessageDialog(null, "Para validar disponibilidad en tiempo real o modificar estados,\nutilice la opción 'Registrar check-in exitoso' del menú anterior.", "Control de Habitaciones", JOptionPane.INFORMATION_MESSAGE);
        } else if (op == 1) {
            ejecutarFlujoCheckInCompleto();
        }
    }

    private void ejecutarFlujoCheckInCompleto() {
        String codEvento = JOptionPane.showInputDialog(null, "Ingrese el Código Único del Evento:", "Procesar Asignación", JOptionPane.QUESTION_MESSAGE);
        if (codEvento == null || codEvento.trim().isEmpty()) return;

        String dniInvitado = JOptionPane.showInputDialog(null, "Ingrese el DNI del Huésped:", "Procesar Asignación", JOptionPane.QUESTION_MESSAGE);
        if (dniInvitado == null || dniInvitado.trim().isEmpty()) return;

        String numHabitacion = JOptionPane.showInputDialog(null, "Ingrese el Número de Habitación (Ej: 101, 102, 201):", "Procesar Asignación", JOptionPane.QUESTION_MESSAGE);
        if (numHabitacion == null || numHabitacion.trim().isEmpty()) return;

        // Cambiado al controlador específico de habitaciones transaccionales
        HabitacionController.getInstance().procesarCheckInHabitacion(codEvento.trim(), dniInvitado.trim(), numHabitacion.trim());
    }

    private void subMenuPremio() {
        ImageIcon iconoActividades = new ImageIcon("src/img/HouseHunter_Menu-Administrador_Actividades.png");
        String[] sub = {"Lanzar Sorteo de Evento", "Volver"}; 
        
        int op = JOptionPane.showOptionDialog( 
            null, "<html><body style='width:250px; text-align:center;'><h3>Entrega de Premios</h3>Seleccione una operación:</body></html>", 
            "Submenú Premios", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, 
            iconoActividades, sub, sub[0]
        );
        
        if (op == 0) {  // Lanzar Sorteo de Evento
            String codEvento = JOptionPane.showInputDialog(null, "Ingrese el Código Único del Evento:", "Configurar Sorteo", JOptionPane.QUESTION_MESSAGE);
            if (codEvento == null || codEvento.trim().isEmpty()) return;
            
            String descripcionPremio = JOptionPane.showInputDialog(null, "Ingrese la descripción del Premio (Ej: Voucher Estadía 5 Estrellas):", "Detalle del Premio", JOptionPane.QUESTION_MESSAGE);
            if (descripcionPremio == null || descripcionPremio.trim().isEmpty()) return;
            
            // Cambiado a PremioController
            PremioController.getInstance().ejecutarSorteoPremio(codEvento.trim(), descripcionPremio.trim());
        }
    }

    private void mostrarMensaje(String accion) {
        JOptionPane.showMessageDialog(null, "Función: " + accion + "\n(En desarrollo)", "Módulo en Construcción", JOptionPane.INFORMATION_MESSAGE);
    }
}