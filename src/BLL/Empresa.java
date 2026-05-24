package BLL;

import DLL.ActividadController;
import DLL.EventoController;
import DLL.InvitadoController;
import DLL.ReporteController;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Empresa extends Persona {
    private String cuit;
    private String razonSocial;

 // Controladores activos de forma unificada
    private EventoController eventoController = EventoController.getInstance();
    private InvitadoController invitadoController = InvitadoController.getInstance();
    private ReporteController reporteController = new ReporteController();
    private ActividadController actividadController = ActividadController.getInstance(); // ¡CORREGIDO al Singleton real!
    // Reserva actual ( La que se está gestionando. )
    private Reserva reservaActual;

    public Empresa(String email, String password, String cuit, String razonSocial, Rol rol) {
        super(email, password, rol);
        this.cuit = cuit;
        this.razonSocial = razonSocial;
    }

    @Override
    public String getNombre() {
        return razonSocial;
    }

    @Override
    public void mostrarMenu() {
        ImageIcon iconoMenu = new ImageIcon("src/img/HouseHunter_Menu-Empresa.png");
        
        String tituloMenu = "<html><body style='width: 300px; text-align: center;'>"
                          + "<h2>🏢 Panel de Empresa</h2>"
                          + "<b>Entidad:</b> " + getNombre() 
                          + "<hr>Seleccione un módulo de gestión:</body></html>";

        String[] modulos = {
            "📁 GESTIÓN DE EVENTO", 
            "📅 PLANIFICACIÓN", 
            "📧 INVITACIONES", 
            "📊 REPORTES", 
            "❌ CERRAR SESIÓN"
        };

        int seleccion;
        do {
            seleccion = JOptionPane.showOptionDialog(
                null, tituloMenu, "HouseHunter v1.0",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                iconoMenu, modulos, modulos[0]
            );

            switch (seleccion) {
                case 0: subMenuGestionEvento(); break;
                case 1: subMenuCronograma(); break; 
                case 2: subMenuInvitaciones(); break; 
                case 3: subMenuReportes(); break;
                case 4: JOptionPane.showMessageDialog(null, "Cerrando sesión de " + getNombre()); break;
            }
        } while (seleccion != 4 && seleccion != -1);
    }

    // ====================== GESTIÓN DE EVENTO ======================
    private void subMenuGestionEvento() {
        ImageIcon iconoGestion = new ImageIcon("src/img/HouseHunter_Menu-Empresa_Gestion.png");
        String[] opciones = {"Realizar Reserva", "Cargar Invitados", "Seleccionar Plantilla", "Volver"};
        
        int op = JOptionPane.showOptionDialog(
            null, "<html><body style='width:250px; text-align:center;'><h3>Módulo de Eventos</h3>Seleccione una acción:</body></html>", 
            "Gestión", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, 
            iconoGestion, opciones, opciones[0]
        );
        
        if (op == 0) {
            realizarReserva();
        } else if (op == 1) {
            cargarInvitadosMasivo();
        } else if (op == 2) {
            seleccionarPlantilla();
        }
    }

    private void realizarReserva() {
        String fechaStr = JOptionPane.showInputDialog(null, "Fecha del evento (YYYY-MM-DD):", "Nueva Reserva", JOptionPane.QUESTION_MESSAGE);
        if (fechaStr == null) return;
        try {
            LocalDate fechaEvento = LocalDate.parse(fechaStr);
            String numInvStr = JOptionPane.showInputDialog(null, "Número estimado de invitados:", "Cantidad", JOptionPane.QUESTION_MESSAGE);
            if (numInvStr == null) return;
            int numInvitados = Integer.parseInt(numInvStr);

            if (eventoController.verificarDisponibilidad(fechaEvento, numInvitados)) {
                Reserva nueva = new Reserva(this, fechaEvento, numInvitados);
                eventoController.crearReserva(nueva);
                reservaActual = nueva;
                JOptionPane.showMessageDialog(null, "✅ Reserva creada exitosamente.\nID: " + nueva.getId());
            } else {
                JOptionPane.showMessageDialog(null, "❌ No hay disponibilidad para la fecha seleccionada.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Datos inválidos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarInvitadosMasivo() {
        if (!seleccionarReservaActual()) return;

        String datos = JOptionPane.showInputDialog(null,
                "Ingrese los invitados en el siguiente formato:\n"
                + "nombre,email,dni,telefono\n"
                + "Separe cada invitado con punto y coma (;)\n\n"
                + "Ejemplo:\n"
                + "Juan Perez,juan@mail.com,12345678,555-1234;Maria Gomez,maria@mail.com,87654321,555-5678",
                "Carga Masiva de Invitados", JOptionPane.QUESTION_MESSAGE);
        if (datos == null || datos.trim().isEmpty()) return;

        String[] lineas = datos.split(";");
        List<Invitado> lista = new ArrayList<>();
        int errores = 0;

        for (String linea : lineas) {
            String[] campos = linea.split(",");
            if (campos.length < 3) {
                errores++;
                continue;
            }
            String nombre = campos[0].trim();
            String email = campos[1].trim();
            String dni = campos[2].trim();
            String telefono = (campos.length > 3) ? campos[3].trim() : "";

            Invitado inv = new Invitado(email, "", nombre, Rol.INVITADO);
            inv.setDni(dni);
            inv.setTelefono(telefono);

            if (invitadoController.validarDatosInvitado(inv)) {
                lista.add(inv);
            } else {
                errores++;
            }
        }

        if (lista.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay invitados válidos para cargar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (invitadoController.cargarInvitados(reservaActual.getId(), lista)) {
            JOptionPane.showMessageDialog(null, "✅ Se cargaron " + lista.size() + " invitados.\n❌ " + errores + " registros inválidos.");
        } else {
            JOptionPane.showMessageDialog(null, "Error al guardar los invitados.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarPlantilla() {
        if (!seleccionarReservaActual()) return;

        List<Plantilla> plantillas = eventoController.listarPlantillas();
        if (plantillas.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay plantillas disponibles.");
            return;
        }

        String[] nombres = plantillas.stream().map(Plantilla::getNombre).toArray(String[]::new);
        int sel = JOptionPane.showOptionDialog(null, "Seleccione una plantilla para el evento:", "Plantillas",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, nombres, nombres[0]);
        if (sel >= 0) {
            Plantilla p = plantillas.get(sel);
            if (eventoController.asignarPlantilla(reservaActual.getId(), p.getId())) {
                JOptionPane.showMessageDialog(null, "Plantilla '" + p.getNombre() + "' asignada correctamente.");
            } else {
                JOptionPane.showMessageDialog(null, "Error al asignar plantilla.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ====================== PLANIFICACIÓN (CRONOGRAMA) ======================
    private void subMenuCronograma() {
        if (!seleccionarReservaActual()) return;

        ImageIcon iconoPlanif = new ImageIcon("src/img/HouseHunter_Menu-Empresa_Planificacion.png");
        String[] sub = {"Crear actividad", "Asignar importancia", "Guardar cronograma", "Volver"};
        int op;
        do {
            op = JOptionPane.showOptionDialog(
                null, "<html><body style='width:250px; text-align:center;'><h3>Cronograma de Actividades</h3></body></html>", 
                "Submenú Planificación", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, 
                iconoPlanif, sub, sub[0]
            );
            switch (op) {
                case 0: crearActividad(); break;
                case 1: asignarImportancia(); break;
                case 2: guardarCronograma(); break;
            }
        } while (op != 3 && op != -1);
    }

    private void crearActividad() {
        String nombre = JOptionPane.showInputDialog("Nombre de la actividad:");
        if (nombre == null) return;
        String fechaHoraStr = JOptionPane.showInputDialog("Fecha y hora (YYYY-MM-DD HH:MM):");
        if (fechaHoraStr == null) return;
        String duracionStr = JOptionPane.showInputDialog("Duración (minutos):");
        if (duracionStr == null) return;
        String cupoStr = JOptionPane.showInputDialog("Cupo máximo:");
        if (cupoStr == null) return;
        String importanciaStr = (String) JOptionPane.showInputDialog(null, "Importancia:", "Importancia",
                JOptionPane.QUESTION_MESSAGE, null, new String[]{"BAJA","MEDIA","ALTA"}, "MEDIA");
        
        CategoriaActividad categoriaSeleccionada = (CategoriaActividad) JOptionPane.showInputDialog(null, "Categoría:", "Categoría",
                JOptionPane.QUESTION_MESSAGE, null, CategoriaActividad.values(), CategoriaActividad.CHARLA);
        String categoria = (categoriaSeleccionada != null) ? categoriaSeleccionada.toString() : "OTRO";

        try {
            LocalDateTime fechaHora = LocalDateTime.parse(fechaHoraStr.replace(" ", "T"));
            int duracion = Integer.parseInt(duracionStr);
            int cupo = Integer.parseInt(cupoStr);
            Actividad act = new Actividad(nombre, fechaHora, duracion, cupo, Importancia.valueOf(importanciaStr), categoria);
            act.setReserva(reservaActual);
            List<Actividad> lista = new ArrayList<>();
            lista.add(act);
            if (eventoController.guardarCronograma(reservaActual.getId(), lista)) {
                JOptionPane.showMessageDialog(null, "Actividad agregada al cronograma.");
            } else {
                JOptionPane.showMessageDialog(null, "Error al guardar actividad.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error en datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void asignarImportancia() {
        List<Actividad> actividades = eventoController.obtenerActividadesPorReserva(reservaActual.getId());
        if (actividades.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay actividades aún. Cree una primero.");
            return;
        }
        String[] nombresActs = actividades.stream().map(Actividad::getNombre).toArray(String[]::new);
        int sel = JOptionPane.showOptionDialog(null, "Seleccione la actividad a modificar:", "Asignar importancia",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, nombresActs, nombresActs[0]);
        if (sel < 0) return;
        Actividad act = actividades.get(sel);
        String nuevaImp = (String) JOptionPane.showInputDialog(null, "Nueva importancia:", "Importancia",
                JOptionPane.QUESTION_MESSAGE, null, new String[]{"BAJA","MEDIA","ALTA"}, act.getImportancia().toString());
        if (nuevaImp != null) {
            act.setImportancia(Importancia.valueOf(nuevaImp));
            List<Actividad> todas = eventoController.obtenerActividadesPorReserva(reservaActual.getId());
            for (Actividad a : todas) {
                if (a.getId() == act.getId()) a.setImportancia(act.getImportancia());
            }
            if (eventoController.guardarCronograma(reservaActual.getId(), todas)) {
                JOptionPane.showMessageDialog(null, "Importancia actualizada.");
            } else {
                JOptionPane.showMessageDialog(null, "Error al actualizar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void guardarCronograma() {
        List<Actividad> actividades = eventoController.obtenerActividadesPorReserva(reservaActual.getId());
        if (actividades.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay actividades para guardar.");
            return;
        }
        JOptionPane.showMessageDialog(null, "Cronograma guardado correctamente.\nTotal actividades: " + actividades.size());
    }

    // ====================== INVITACIONES ======================
    private void subMenuInvitaciones() {
        if (!seleccionarReservaActual()) return;

        ImageIcon iconoInvit = new ImageIcon("src/img/HouseHunter_Menu-Empresa_Invitaciones.png");
        String[] sub = {"Listar invitados", "Generar tokens (automático)", "Enviar notificaciones", "Volver"};
        int op;
        do {
            op = JOptionPane.showOptionDialog(
                null, "<html><body style='width:250px; text-align:center;'><h3>Envío de Invitaciones</h3></body></html>", 
                "Submenú Invitaciones", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, 
                iconoInvit, sub, sub[0]
            );
            switch (op) {
                case 0: listarInvitados(); break;
                case 1: JOptionPane.showMessageDialog(null, "Los tokens se generan automáticamente al cargar invitados."); break;
                case 2: enviarNotificaciones(); break;
            }
        } while (op != 3 && op != -1);
    }

    private void listarInvitados() {
        List<Invitado> invitados = invitadoController.listarInvitadosPorReserva(reservaActual.getId());
        if (invitados.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay invitados cargados.");
            return;
        }
        StringBuilder sb = new StringBuilder("📋 Invitados:\n");
        for (Invitado i : invitados) {
            sb.append("- ").append(i.getNombre())
              .append(" | Email: ").append(i.getEmail())
              .append(" | Token: ").append(i.getTokenAcceso())
              .append("\n");
        }
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    private void enviarNotificaciones() {
        if (invitadoController.enviarNotificaciones(reservaActual.getId())) {
            JOptionPane.showMessageDialog(null, "✅ Notificaciones enviadas (simulado).\nRevise la consola para ver los tokens.");
        } else {
            JOptionPane.showMessageDialog(null, "Error al enviar notificaciones.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ====================== REPORTES ======================
    private void subMenuReportes() {
        if (!seleccionarReservaActual()) return;

        ImageIcon iconoReportes = new ImageIcon("src/img/HouseHunter_Menu-Empresa_Reportes.png");
        String[] sub = {"Ver estadísticas", "Exportar (simular)", "Volver"};
        int op = JOptionPane.showOptionDialog(
            null, "<html><body style='width:250px; text-align:center;'><h3>Reportes Corporativos</h3></body></html>", 
            "Reportes", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, 
            iconoReportes, sub, sub[0]
        );
        if (op == 0) {
            mostrarEstadisticas();
        } else if (op == 1) {
            JOptionPane.showMessageDialog(null, "Exportación a PDF/Excel (simulada).");
        }
    }

    private void mostrarEstadisticas() {
        Map<String, Object> stats = reporteController.obtenerReporteEvento(reservaActual.getId());
        String mensaje = String.format(
            "📊 REPORTE DEL EVENTO ID %d\n\n" +
            "Total invitados: %d\n" +
            "Confirmados: %d\n" +
            "Porcentaje confirmación: %.2f%%\n" +
            "Total actividades programadas: %d",
            reservaActual.getId(),
            stats.getOrDefault("totalInvitados", 0),
            stats.getOrDefault("confirmados", 0),
            stats.getOrDefault("porcentajeConfirmacion", 0.0),
            stats.getOrDefault("totalActividades", 0)
        );
        JOptionPane.showMessageDialog(null, mensaje);
    }

    private boolean seleccionarReservaActual() {
        if (reservaActual != null) return true;
        List<Reserva> reservas = eventoController.listarReservasPorEmpresa(this.getId());
        if (reservas.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay reservas para esta empresa.\nPrimero debe crear una reserva.");
            return false;
        }
        String[] opciones = reservas.stream().map(r -> "ID " + r.getId() + " - " + r.getFechaEvento() + " (" + r.getEstado() + ")").toArray(String[]::new);
        int sel = JOptionPane.showOptionDialog(null, "Seleccione la reserva a gestionar:", "Reservas",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
        if (sel >= 0) {
            reservaActual = reservas.get(sel);
            return true;
        }
        return false;
    }
}