package GUI;

import BLL.*;
import DLL.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminDashboard extends JFrame {
    private Administrador admin;
    
    // Controladores
    private EventoController eventoController;
    private InvitadoController invitadoController;
    private RegistroController registroController;
    private ReporteController reporteController;
    
    // Reserva seleccionada
    private Reserva reservaActual;
    
    // Componentes UI
    private JTabbedPane tabbedPane;
    private JComboBox<Reserva> comboReservas;
    
    // Tablas
    private JTable tablaActividades;
    private DefaultTableModel modelActividades;
    private JTable tablaInvitados;
    private DefaultTableModel modelInvitados;
    
    // Área de reportes
    private JTextArea txtReporte;
    
    // Panel de premios
    private JTextArea txtPremios;

    public AdminDashboard(Administrador admin) {
        this.admin = admin;
        this.eventoController = new EventoController();
        this.invitadoController = new InvitadoController();
        this.registroController = new RegistroController();
        this.reporteController = new ReporteController();
        
        initComponents();
        cargarReservas();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void initComponents() {
        setTitle("Panel de Administrador - " + admin.getNombre());
        setSize(950, 750);
        setLayout(new BorderLayout());
        
        // Panel superior con selector de reserva
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel lblAdmin = new JLabel("Administrador: " + admin.getNombre());
        lblAdmin.setFont(new Font("Arial", Font.BOLD, 14));
        panelSuperior.add(lblAdmin, BorderLayout.WEST);
        
        JPanel panelReserva = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelReserva.add(new JLabel("Reserva actual:"));
        comboReservas = new JComboBox<>();
        comboReservas.addActionListener(e -> {
            reservaActual = (Reserva) comboReservas.getSelectedItem();
            if (reservaActual != null) {
                actualizarPaneles();
            }
        });
        panelReserva.add(comboReservas);
        panelSuperior.add(panelReserva, BorderLayout.EAST);
        
        add(panelSuperior, BorderLayout.NORTH);
        
        // TabbedPane principal
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Recepción", crearPanelRecepcion());
        tabbedPane.addTab("Actividades", crearPanelActividades());
        tabbedPane.addTab("Invitados", crearPanelInvitados());
        tabbedPane.addTab("Reportes", crearPanelReportes());
        tabbedPane.addTab("Premios", crearPanelPremios());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Barra de estado
        JLabel lblStatus = new JLabel("HouseHunter - Administrador");
        lblStatus.setBorder(new EmptyBorder(5, 10, 5, 10));
        add(lblStatus, BorderLayout.SOUTH);
    }
    
    private void cargarReservas() {
        // Cargar todas las reservas de todas las empresas (para admin)
        List<Reserva> todas = eventoController.listarTodasReservas();
        comboReservas.removeAllItems();
        for (Reserva r : todas) {
            comboReservas.addItem(r);
        }
        if (!todas.isEmpty()) {
            reservaActual = todas.get(0);
            actualizarPaneles();
        }
    }
    
    private void actualizarPaneles() {
        if (reservaActual != null) {
            cargarActividades();
            cargarInvitados();
            // Actualizar otros paneles si es necesario
        }
    }
    
    // ================== PANEL RECEPCIÓN ==================
    private JPanel crearPanelRecepcion() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        JButton btnCheckIn = new JButton("Check-in de Invitado");
        btnCheckIn.addActionListener(e -> realizarCheckIn());
        panel.add(btnCheckIn, gbc);
        
        gbc.gridy = 1;
        JButton btnAsignarHabitacion = new JButton("Asignar Habitación");
        btnAsignarHabitacion.addActionListener(e -> asignarHabitacionManual());
        panel.add(btnAsignarHabitacion, gbc);
        
        gbc.gridy = 2;
        JButton btnListarInvitados = new JButton("Listar Invitados de Reserva");
        btnListarInvitados.addActionListener(e -> {
            if (reservaActual != null) {
                List<Invitado> invitados = invitadoController.listarInvitadosPorReserva(reservaActual.getId());
                mostrarInvitadosEnDialogo(invitados);
            }
        });
        panel.add(btnListarInvitados, gbc);
        
        return panel;
    }
    
    private void realizarCheckIn() {
        if (reservaActual == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una reserva primero.");
            return;
        }
        String token = JOptionPane.showInputDialog(this, "Ingrese el token del invitado:");
        if (token == null || token.trim().isEmpty()) return;
        
        Invitado inv = registroController.validarToken(token.trim());
        if (inv == null) {
            JOptionPane.showMessageDialog(this, "Token inválido o evento cancelado.");
            return;
        }
        // Verificar que pertenezca a la reserva actual
        if (inv.getReserva().getId() != reservaActual.getId()) {
            JOptionPane.showMessageDialog(this, "El invitado no corresponde a la reserva seleccionada.");
            return;
        }
        // Confirmar asistencia
        boolean ok = registroController.confirmarAsistencia(inv.getId());
        if (ok) {
            JOptionPane.showMessageDialog(this, "✅ Check-in exitoso para " + inv.getNombre());
            cargarInvitados();
        } else {
            JOptionPane.showMessageDialog(this, "❌ Error en el check-in.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void asignarHabitacionManual() {
        if (reservaActual == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una reserva primero.");
            return;
        }
        String idInvitadoStr = JOptionPane.showInputDialog(this, "ID del invitado:");
        if (idInvitadoStr == null || idInvitadoStr.trim().isEmpty()) return;
        try {
            int idInvitado = Integer.parseInt(idInvitadoStr.trim());
            // Buscar una habitación libre
            Integer idHabitacion = registroController.obtenerHabitacionLibre(reservaActual.getId());
            if (idHabitacion == null) {
                JOptionPane.showMessageDialog(this, "No hay habitaciones libres para esta reserva.");
                return;
            }
            // Asignar (actualizar invitado)
            boolean ok = registroController.asignarHabitacionAInvitado(idInvitado, idHabitacion, reservaActual.getId());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Habitación asignada correctamente.");
                cargarInvitados();
            } else {
                JOptionPane.showMessageDialog(this, "Error al asignar habitación.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void mostrarInvitadosEnDialogo(List<Invitado> invitados) {
        if (invitados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay invitados en esta reserva.");
            return;
        }
        StringBuilder sb = new StringBuilder("Lista de invitados:\n");
        for (Invitado i : invitados) {
            sb.append("ID: ").append(i.getId())
              .append(" | Nombre: ").append(i.getNombre())
              .append(" | Email: ").append(i.getEmail())
              .append(" | Confirmado: ").append(i.isAsistenciaConfirmada() ? "Sí" : "No")
              .append("\n");
        }
        JOptionPane.showMessageDialog(this, sb.toString());
    }
    
    // ================== PANEL ACTIVIDADES ==================
    private JPanel crearPanelActividades() {
        JPanel panel = new JPanel(new BorderLayout());
        
        modelActividades = new DefaultTableModel(
            new String[]{"ID", "Nombre", "Fecha/Hora", "Duración", "Cupo", "Importancia", "Categoría"}, 0);
        tablaActividades = new JTable(modelActividades);
        JScrollPane scroll = new JScrollPane(tablaActividades);
        panel.add(scroll, BorderLayout.CENTER);
        
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnRefrescar = new JButton("Refrescar");
        JButton btnMonitorear = new JButton("Monitorear Actividad");
        JButton btnVerCronograma = new JButton("Ver Cronograma");
        JButton btnActualizarEstado = new JButton("Actualizar Estado");
        
        btnRefrescar.addActionListener(e -> cargarActividades());
        btnMonitorear.addActionListener(e -> monitorearActividad());
        btnVerCronograma.addActionListener(e -> verCronograma());
        btnActualizarEstado.addActionListener(e -> actualizarEstadoActividad());
        
        panelBotones.add(btnRefrescar);
        panelBotones.add(btnMonitorear);
        panelBotones.add(btnVerCronograma);
        panelBotones.add(btnActualizarEstado);
        panel.add(panelBotones, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void cargarActividades() {
        modelActividades.setRowCount(0);
        if (reservaActual == null) return;
        List<Actividad> acts = eventoController.obtenerActividadesPorReserva(reservaActual.getId());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Actividad a : acts) {
            modelActividades.addRow(new Object[]{
                a.getId(),
                a.getNombre(),
                a.getFechaHora().format(fmt),
                a.getDuracionMinutos(),
                a.getCupoMaximo(),
                a.getImportancia(),
                a.getCategoria()
            });
        }
    }
    
    private void monitorearActividad() {
        int row = tablaActividades.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una actividad.");
            return;
        }
        int idAct = (int) modelActividades.getValueAt(row, 0);
        Actividad a = registroController.obtenerDetalleActividad(idAct);
        if (a == null) {
            JOptionPane.showMessageDialog(this, "No se pudo obtener el detalle.");
            return;
        }
        Map<String, Object> rep = reporteController.obtenerReporteActividad(idAct);
        String mensaje = String.format(
            "📊 Monitoreo de Actividad\n\nNombre: %s\nFecha: %s\nAsistentes: %d/%d\nLista:\n%s",
            a.getNombre(),
            a.getFechaHora().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            rep.get("total_asistentes"),
            a.getCupoMaximo(),
            ((List<String>) rep.get("lista_asistentes")).stream().collect(Collectors.joining("\n  - ", "  - ", ""))
        );
        JOptionPane.showMessageDialog(this, mensaje);
    }
    
    private void verCronograma() {
        if (reservaActual == null) return;
        List<Actividad> acts = eventoController.obtenerActividadesPorReserva(reservaActual.getId());
        if (acts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay actividades programadas.");
            return;
        }
        StringBuilder sb = new StringBuilder("📅 CRONOGRAMA COMPLETO:\n\n");
        for (Actividad a : acts) {
            sb.append("• ").append(a.getNombre())
              .append(" - ").append(a.getFechaHora().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
              .append(" (Importancia: ").append(a.getImportancia()).append(")\n");
        }
        JOptionPane.showMessageDialog(this, sb.toString());
    }
    
    private void actualizarEstadoActividad() {
        // Simulación: cambiar estado de una actividad (ej. de programada a en curso)
        JOptionPane.showMessageDialog(this, "Función en desarrollo.");
    }
    
    // ================== PANEL INVITADOS ==================
    private JPanel crearPanelInvitados() {
        JPanel panel = new JPanel(new BorderLayout());
        
        modelInvitados = new DefaultTableModel(
            new String[]{"ID", "Nombre", "Email", "DNI", "Teléfono", "Habitación", "Confirmado"}, 0);
        tablaInvitados = new JTable(modelInvitados);
        JScrollPane scroll = new JScrollPane(tablaInvitados);
        panel.add(scroll, BorderLayout.CENTER);
        
        JPanel panelBotones = new JPanel();
        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> cargarInvitados());
        panelBotones.add(btnRefrescar);
        panel.add(panelBotones, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void cargarInvitados() {
        modelInvitados.setRowCount(0);
        if (reservaActual == null) return;
        List<Invitado> invitados = invitadoController.listarInvitadosPorReserva(reservaActual.getId());
        for (Invitado i : invitados) {
            String hab = registroController.obtenerHabitacionAsignada(i.getId());
            modelInvitados.addRow(new Object[]{
                i.getId(),
                i.getNombre(),
                i.getEmail(),
                i.getDni(),
                i.getTelefono(),
                hab != null ? hab : "Sin asignar",
                i.isAsistenciaConfirmada() ? "Sí" : "No"
            });
        }
    }
    
    // ================== PANEL REPORTES ==================
    private JPanel crearPanelReportes() {
        JPanel panel = new JPanel(new BorderLayout());
        
        txtReporte = new JTextArea();
        txtReporte.setEditable(false);
        txtReporte.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(txtReporte);
        panel.add(scroll, BorderLayout.CENTER);
        
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnGenerar = new JButton("Generar Reporte General");
        JButton btnExportar = new JButton("Exportar Reporte");
        
        btnGenerar.addActionListener(e -> generarReporteGeneral());
        btnExportar.addActionListener(e -> exportarReporte());
        
        panelBotones.add(btnGenerar);
        panelBotones.add(btnExportar);
        panel.add(panelBotones, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void generarReporteGeneral() {
        if (reservaActual == null) {
            txtReporte.setText("Seleccione una reserva.");
            return;
        }
        Map<String, Object> stats = reporteController.obtenerReporteEvento(reservaActual.getId());
        List<Actividad> acts = eventoController.obtenerActividadesPorReserva(reservaActual.getId());
        StringBuilder sb = new StringBuilder();
        sb.append("=== REPORTE GENERAL DEL EVENTO ===\n");
        sb.append("Reserva ID: ").append(reservaActual.getId()).append("\n");
        sb.append("Fecha: ").append(reservaActual.getFechaEvento()).append("\n");
        sb.append("Total invitados: ").append(stats.get("totalInvitados")).append("\n");
        sb.append("Confirmados: ").append(stats.get("confirmados")).append("\n");
        sb.append("Porcentaje: ").append(stats.get("porcentajeConfirmacion")).append("%\n");
        sb.append("Actividades programadas: ").append(stats.get("totalActividades")).append("\n\n");
        sb.append("=== DETALLE POR ACTIVIDAD ===\n");
        for (Actividad a : acts) {
            Map<String, Object> rep = reporteController.obtenerReporteActividad(a.getId());
            sb.append("- ").append(a.getNombre())
              .append(" | Asistentes: ").append(rep.get("total_asistentes"))
              .append("/").append(a.getCupoMaximo()).append("\n");
        }
        txtReporte.setText(sb.toString());
    }
    
    private void exportarReporte() {
        if (reservaActual == null) return;
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fc.getSelectedFile().getAbsolutePath();
            boolean ok = reporteController.exportarReporteEvento(reservaActual.getId(), path);
            JOptionPane.showMessageDialog(this, ok ? "Reporte exportado a " + path : "Error al exportar.");
        }
    }
    
    // ================== PANEL PREMIOS ==================
    private JPanel crearPanelPremios() {
        JPanel panel = new JPanel(new BorderLayout());
        
        txtPremios = new JTextArea();
        txtPremios.setEditable(false);
        txtPremios.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(txtPremios);
        panel.add(scroll, BorderLayout.CENTER);
        
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnObtenerGanador = new JButton("Obtener Ganador");
        JButton btnEntregarPremio = new JButton("Entregar Premio");
        
        btnObtenerGanador.addActionListener(e -> obtenerGanador());
        btnEntregarPremio.addActionListener(e -> entregarPremio());
        
        panelBotones.add(btnObtenerGanador);
        panelBotones.add(btnEntregarPremio);
        panel.add(panelBotones, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void obtenerGanador() {
        if (reservaActual == null) {
            txtPremios.setText("Seleccione una reserva.");
            return;
        }
        List<Invitado> invitados = invitadoController.listarInvitadosPorReserva(reservaActual.getId());
        // Filtrar los que cumplen con mínimo 2 actividades
        List<Invitado> elegibles = invitados.stream()
            .filter(i -> registroController.verificarAsistenciaMinima(i.getId(), 2))
            .collect(Collectors.toList());
        
        if (elegibles.isEmpty()) {
            txtPremios.setText("No hay invitados elegibles para el sorteo.");
            return;
        }
        
        // Seleccionar uno al azar
        int index = (int) (Math.random() * elegibles.size());
        Invitado ganador = elegibles.get(index);
        txtPremios.setText("🎉 GANADOR DEL SORTEO:\n\n" +
            "Nombre: " + ganador.getNombre() + "\n" +
            "Email: " + ganador.getEmail() + "\n" +
            "DNI: " + ganador.getDni() + "\n" +
            "¡Felicidades!");
    }
    
    private void entregarPremio() {
        if (txtPremios.getText().contains("GANADOR")) {
            JOptionPane.showMessageDialog(this, "🏆 Premio entregado al ganador.");
            txtPremios.append("\n\n✅ Premio entregado.");
        } else {
            JOptionPane.showMessageDialog(this, "Primero obtenga un ganador.");
        }
    }
}