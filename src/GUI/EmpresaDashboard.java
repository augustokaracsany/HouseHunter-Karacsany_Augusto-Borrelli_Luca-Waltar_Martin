package GUI;

import BLL.*;
import DLL.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class EmpresaDashboard extends JFrame {
    private Empresa empresa;
    
    // Controladores
    private EventoController eventoController;
    private InvitadoController invitadoController;
    private ReporteController reporteController;
    private HotelController hotelController; // opcional
    
    // Reserva actual
    private Reserva reservaActual;
    
    // Componentes UI
    private JTabbedPane tabbedPane;
    private JTable tablaActividades;
    private DefaultTableModel modelActividades;
    private JTable tablaInvitados;
    private DefaultTableModel modelInvitados;
    private JTextArea txtReporte;
    public EmpresaDashboard(Empresa empresa) {
        this.empresa = empresa;
        this.eventoController = new EventoController();
        this.invitadoController = new InvitadoController();
        this.reporteController = new ReporteController();
        this.hotelController = new HotelController();
        
        initComponents();
        cargarReservas();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void initComponents() {
        setTitle("Panel de Empresa - " + empresa.getNombre());
        setSize(900, 700);
        setLayout(new BorderLayout());
        
        // Panel superior con info de empresa y selección de reserva
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel lblEmpresa = new JLabel("Empresa: " + empresa.getNombre());
        lblEmpresa.setFont(new Font("Arial", Font.BOLD, 14));
        panelSuperior.add(lblEmpresa, BorderLayout.WEST);
        
        JPanel panelReserva = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelReserva.add(new JLabel("Reserva actual:"));
        JComboBox<Reserva> comboReservas = new JComboBox<>();
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
        
        // Pestaña: Gestión de Evento
        tabbedPane.addTab("Gestión de Evento", crearPanelGestionEvento());
        
        // Pestaña: Planificación (Cronograma)
        tabbedPane.addTab("Planificación", crearPanelPlanificacion());
        
        // Pestaña: Invitaciones
        tabbedPane.addTab("Invitaciones", crearPanelInvitaciones());
        
        // Pestaña: Reportes
        tabbedPane.addTab("Reportes", crearPanelReportes());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Guardar referencia del combo para usarlo después
        this.comboReservas = comboReservas;
    }
    
    private JComboBox<Reserva> comboReservas; // para acceder desde otro método
    
    private void cargarReservas() {
        List<Reserva> reservas = eventoController.listarReservasPorEmpresa(empresa.getId());
        comboReservas.removeAllItems();
        for (Reserva r : reservas) {
            comboReservas.addItem(r);
        }
        if (!reservas.isEmpty()) {
            reservaActual = reservas.get(0);
            actualizarPaneles();
        }
    }
    
    private void actualizarPaneles() {
        if (reservaActual != null) {
            cargarActividades();
            cargarInvitados();
            // actualizar otros paneles
        }
    }
    
    // ================== PANEL GESTIÓN DE EVENTO ==================
    private JPanel crearPanelGestionEvento() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Botón Realizar Reserva
        JButton btnReserva = new JButton("Realizar Reserva");
        btnReserva.addActionListener(e -> realizarReserva());
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(btnReserva, gbc);
        
        // Botón Cargar Invitados
        JButton btnCargarInvitados = new JButton("Cargar Invitados (Masivo)");
        btnCargarInvitados.addActionListener(e -> cargarInvitadosMasivo());
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(btnCargarInvitados, gbc);
        
        // Botón Seleccionar Plantilla
        JButton btnPlantilla = new JButton("Seleccionar Plantilla");
        btnPlantilla.addActionListener(e -> seleccionarPlantilla());
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(btnPlantilla, gbc);
        
        return panel;
    }
    
    private void realizarReserva() {
        // Crear un JDialog para ingresar datos
        JDialog dialog = new JDialog(this, "Nueva Reserva", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField txtFecha = new JTextField(10);
        JTextField txtInvitados = new JTextField(10);
        
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Fecha (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        dialog.add(txtFecha, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Número de invitados:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtInvitados, gbc);
        
        JButton btnGuardar = new JButton("Crear Reserva");
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        dialog.add(btnGuardar, gbc);
        
        btnGuardar.addActionListener(e -> {
            try {
                LocalDate fecha = LocalDate.parse(txtFecha.getText());
                int num = Integer.parseInt(txtInvitados.getText());
                if (eventoController.verificarDisponibilidad(fecha, num)) {
                    Reserva nueva = new Reserva(empresa, fecha, num);
                    eventoController.crearReserva(nueva);
                    JOptionPane.showMessageDialog(dialog, "Reserva creada con ID: " + nueva.getId());
                    cargarReservas(); // recargar combo
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "No hay disponibilidad para esa fecha.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void cargarInvitadosMasivo() {
        if (reservaActual == null) {
            JOptionPane.showMessageDialog(this, "Primero seleccione o cree una reserva.");
            return;
        }
        JDialog dialog = new JDialog(this, "Carga Masiva de Invitados", true);
        dialog.setLayout(new BorderLayout());
        
        JTextArea textArea = new JTextArea(10, 40);
        textArea.setText("nombre,email,dni,telefono\nEjemplo:\nJuan Perez,juan@mail.com,12345678,555-1234;Maria Gomez,maria@mail.com,87654321,555-5678");
        JScrollPane scroll = new JScrollPane(textArea);
        dialog.add(scroll, BorderLayout.CENTER);
        
        JButton btnCargar = new JButton("Cargar");
        btnCargar.addActionListener(e -> {
            String datos = textArea.getText();
            // procesar igual que en Empresa.cargarInvitadosMasivo()
            String[] lineas = datos.split(";");
            java.util.List<Invitado> lista = new java.util.ArrayList<>();
            int errores = 0;
            for (String linea : lineas) {
                String[] campos = linea.split(",");
                if (campos.length < 3) { errores++; continue; }
                String nombre = campos[0].trim();
                String email = campos[1].trim();
                String dni = campos[2].trim();
                String telefono = (campos.length > 3) ? campos[3].trim() : "";
                Invitado inv = new Invitado(email, "", nombre, Rol.INVITADO);
                inv.setDni(dni);
                inv.setTelefono(telefono);
                if (invitadoController.validarDatosInvitado(inv)) {
                    lista.add(inv);
                } else { errores++; }
            }
            if (!lista.isEmpty()) {
                if (invitadoController.cargarInvitados(reservaActual.getId(), lista)) {
                    JOptionPane.showMessageDialog(dialog, "Cargados " + lista.size() + " invitados. Errores: " + errores);
                    cargarInvitados(); // refrescar tabla
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Error al guardar.");
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "No hay invitados válidos.");
            }
        });
        dialog.add(btnCargar, BorderLayout.SOUTH);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void seleccionarPlantilla() {
        if (reservaActual == null) return;
        List<Plantilla> plantas = eventoController.listarPlantillas();
        if (plantas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay plantillas disponibles.");
            return;
        }
        String[] nombres = plantas.stream().map(Plantilla::getNombre).toArray(String[]::new);
        int sel = JOptionPane.showOptionDialog(this, "Seleccione plantilla:", "Plantillas",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, nombres, nombres[0]);
        if (sel >= 0) {
            Plantilla p = plantas.get(sel);
            if (eventoController.asignarPlantilla(reservaActual.getId(), p.getId())) {
                JOptionPane.showMessageDialog(this, "Plantilla asignada: " + p.getNombre());
            } else {
                JOptionPane.showMessageDialog(this, "Error al asignar.");
            }
        }
    }
    
    // ================== PANEL PLANIFICACIÓN ==================
    private JPanel crearPanelPlanificacion() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Modelo de tabla para actividades
        modelActividades = new DefaultTableModel(new String[]{"ID", "Nombre", "Fecha/Hora", "Duración", "Cupo", "Importancia", "Categoría"}, 0);
        tablaActividades = new JTable(modelActividades);
        JScrollPane scrollTabla = new JScrollPane(tablaActividades);
        panel.add(scrollTabla, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnNueva = new JButton("Nueva Actividad");
        JButton btnImportancia = new JButton("Cambiar Importancia");
        JButton btnGuardar = new JButton("Guardar Cronograma");
        
        btnNueva.addActionListener(e -> nuevaActividad());
        btnImportancia.addActionListener(e -> cambiarImportancia());
        btnGuardar.addActionListener(e -> guardarCronograma());
        
        panelBotones.add(btnNueva);
        panelBotones.add(btnImportancia);
        panelBotones.add(btnGuardar);
        panel.add(panelBotones, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void cargarActividades() {
        modelActividades.setRowCount(0);
        if (reservaActual == null) return;
        List<Actividad> acts = eventoController.obtenerActividadesPorReserva(reservaActual.getId());
        for (Actividad a : acts) {
            modelActividades.addRow(new Object[]{
                a.getId(), a.getNombre(),
                a.getFechaHora().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                a.getDuracionMinutos(), a.getCupoMaximo(),
                a.getImportancia(), a.getCategoria()
            });
        }
    }
    
    private void nuevaActividad() {
        if (reservaActual == null) return;
        JDialog dialog = new JDialog(this, "Nueva Actividad", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField txtNombre = new JTextField(15);
        JTextField txtFecha = new JTextField(15);
        JTextField txtDuracion = new JTextField(5);
        JTextField txtCupo = new JTextField(5);
        JComboBox<String> comboImportancia = new JComboBox<>(new String[]{"BAJA","MEDIA","ALTA"});
        JComboBox<CategoriaActividad> comboCategoria = new JComboBox<>(CategoriaActividad.values());
        
        int row = 0;
        gbc.gridx=0; gbc.gridy=row; dialog.add(new JLabel("Nombre:"), gbc);
        gbc.gridx=1; dialog.add(txtNombre, gbc);
        row++;
        gbc.gridx=0; gbc.gridy=row; dialog.add(new JLabel("Fecha y hora (YYYY-MM-DD HH:MM):"), gbc);
        gbc.gridx=1; dialog.add(txtFecha, gbc);
        row++;
        gbc.gridx=0; gbc.gridy=row; dialog.add(new JLabel("Duración (min):"), gbc);
        gbc.gridx=1; dialog.add(txtDuracion, gbc);
        row++;
        gbc.gridx=0; gbc.gridy=row; dialog.add(new JLabel("Cupo máximo:"), gbc);
        gbc.gridx=1; dialog.add(txtCupo, gbc);
        row++;
        gbc.gridx=0; gbc.gridy=row; dialog.add(new JLabel("Importancia:"), gbc);
        gbc.gridx=1; dialog.add(comboImportancia, gbc);
        row++;
        gbc.gridx=0; gbc.gridy=row; dialog.add(new JLabel("Categoría:"), gbc);
        gbc.gridx=1; dialog.add(comboCategoria, gbc);
        row++;
        
        JButton btnGuardar = new JButton("Guardar");
        gbc.gridx=0; gbc.gridy=row; gbc.gridwidth=2;
        dialog.add(btnGuardar, gbc);
        
        btnGuardar.addActionListener(e -> {
            try {
                String nombre = txtNombre.getText();
                String fechaHoraStr = txtFecha.getText();
                LocalDateTime fechaHora = LocalDateTime.parse(fechaHoraStr.replace(" ", "T"));
                int duracion = Integer.parseInt(txtDuracion.getText());
                int cupo = Integer.parseInt(txtCupo.getText());
                String importancia = (String) comboImportancia.getSelectedItem();
                String categoria = comboCategoria.getSelectedItem().toString();
                
                Actividad act = new Actividad(nombre, fechaHora, duracion, cupo, Importancia.valueOf(importancia), categoria);
                act.setReserva(reservaActual);
                List<Actividad> acts = eventoController.obtenerActividadesPorReserva(reservaActual.getId());
                acts.add(act);
                if (eventoController.guardarCronograma(reservaActual.getId(), acts)) {
                    JOptionPane.showMessageDialog(dialog, "Actividad guardada.");
                    cargarActividades();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Error al guardar.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void cambiarImportancia() {
        int selectedRow = tablaActividades.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una actividad.");
            return;
        }
        int idAct = (int) modelActividades.getValueAt(selectedRow, 0);
        String nueva = (String) JOptionPane.showInputDialog(this, "Nueva importancia:", "Importancia",
                JOptionPane.QUESTION_MESSAGE, null, new String[]{"BAJA","MEDIA","ALTA"}, "MEDIA");
        if (nueva != null) {
            List<Actividad> acts = eventoController.obtenerActividadesPorReserva(reservaActual.getId());
            for (Actividad a : acts) {
                if (a.getId() == idAct) {
                    a.setImportancia(Importancia.valueOf(nueva));
                    break;
                }
            }
            if (eventoController.guardarCronograma(reservaActual.getId(), acts)) {
                cargarActividades();
                JOptionPane.showMessageDialog(this, "Importancia actualizada.");
            }
        }
    }
    
    private void guardarCronograma() {
        // Ya se guarda automáticamente, pero refrescamos
        cargarActividades();
        JOptionPane.showMessageDialog(this, "Cronograma sincronizado.");
    }
    
    // ================== PANEL INVITACIONES ==================
    private JPanel crearPanelInvitaciones() {
        JPanel panel = new JPanel(new BorderLayout());
        
        modelInvitados = new DefaultTableModel(new String[]{"ID", "Nombre", "Email", "DNI", "Teléfono", "Token", "Confirmado"}, 0);
        tablaInvitados = new JTable(modelInvitados);
        JScrollPane scroll = new JScrollPane(tablaInvitados);
        panel.add(scroll, BorderLayout.CENTER);
        
        JPanel botones = new JPanel();
        JButton btnRefrescar = new JButton("Refrescar");
        JButton btnEnviarNotif = new JButton("Enviar Notificaciones");
        btnRefrescar.addActionListener(e -> cargarInvitados());
        btnEnviarNotif.addActionListener(e -> enviarNotificaciones());
        botones.add(btnRefrescar);
        botones.add(btnEnviarNotif);
        panel.add(botones, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void cargarInvitados() {
        modelInvitados.setRowCount(0);
        if (reservaActual == null) return;
        List<Invitado> invitados = invitadoController.listarInvitadosPorReserva(reservaActual.getId());
        for (Invitado i : invitados) {
            modelInvitados.addRow(new Object[]{
                i.getId(), i.getNombre(), i.getEmail(), i.getDni(), i.getTelefono(),
                i.getTokenAcceso(), i.isAsistenciaConfirmada() ? "Sí" : "No"
            });
        }
    }
    
    private void enviarNotificaciones() {
        if (reservaActual == null) return;
        if (invitadoController.enviarNotificaciones(reservaActual.getId())) {
            JOptionPane.showMessageDialog(this, "Notificaciones enviadas (ver consola).");
        } else {
            JOptionPane.showMessageDialog(this, "Error al enviar.");
        }
    }
    
    // ================== PANEL REPORTES ==================
    private JPanel crearPanelReportes() {
        JPanel panel = new JPanel(new BorderLayout());
        
        txtReporte = new JTextArea();
        txtReporte.setEditable(false);
        JScrollPane scroll = new JScrollPane(txtReporte);
        panel.add(scroll, BorderLayout.CENTER);
        
        JPanel botones = new JPanel(new FlowLayout());
        JButton btnEstadisticas = new JButton("Estadísticas Generales");
        JButton btnPorActividad = new JButton("Reporte por Actividad");
        JButton btnExportar = new JButton("Exportar a Archivo");
        
        btnEstadisticas.addActionListener(e -> mostrarEstadisticas());
        btnPorActividad.addActionListener(e -> reportePorActividad());
        btnExportar.addActionListener(e -> exportarReporte());
        
        botones.add(btnEstadisticas);
        botones.add(btnPorActividad);
        botones.add(btnExportar);
        panel.add(botones, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void mostrarEstadisticas() {
        if (reservaActual == null) return;
        Map<String, Object> stats = reporteController.obtenerReporteEvento(reservaActual.getId());
        String mensaje = String.format(
            "=== REPORTE DEL EVENTO ===\nTotal invitados: %d\nConfirmados: %d\nPorcentaje confirmación: %.2f%%\nTotal actividades: %d",
            stats.getOrDefault("totalInvitados", 0),
            stats.getOrDefault("confirmados", 0),
            stats.getOrDefault("porcentajeConfirmacion", 0.0),
            stats.getOrDefault("totalActividades", 0)
        );
        txtReporte.setText(mensaje);
    }
    
    private void reportePorActividad() {
        if (reservaActual == null) return;
        List<Actividad> acts = eventoController.obtenerActividadesPorReserva(reservaActual.getId());
        if (acts.isEmpty()) {
            txtReporte.setText("No hay actividades.");
            return;
        }
        String[] nombres = acts.stream().map(Actividad::getNombre).toArray(String[]::new);
        int sel = JOptionPane.showOptionDialog(this, "Seleccione actividad:", "Reporte",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, nombres, nombres[0]);
        if (sel >= 0) {
            Actividad a = acts.get(sel);
            Map<String, Object> rep = reporteController.obtenerReporteActividad(a.getId());
            StringBuilder sb = new StringBuilder();
            sb.append("Actividad: ").append(rep.get("nombre")).append("\n");
            sb.append("Fecha: ").append(rep.get("fecha_hora")).append("\n");
            sb.append("Asistentes: ").append(rep.get("total_asistentes")).append("/").append(a.getCupoMaximo()).append("\n");
            sb.append("Lista:\n");
            java.util.List<String> asistentes = (java.util.List<String>) rep.get("lista_asistentes");
            if (asistentes.isEmpty()) sb.append("  (ninguno)\n");
            else asistentes.forEach(n -> sb.append("  - ").append(n).append("\n"));
            txtReporte.setText(sb.toString());
        }
    }
    
    private void exportarReporte() {
        if (reservaActual == null) return;
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fc.getSelectedFile().getAbsolutePath();
            if (reporteController.exportarReporteEvento(reservaActual.getId(), path)) {
                JOptionPane.showMessageDialog(this, "Reporte exportado a " + path);
            } else {
                JOptionPane.showMessageDialog(this, "Error al exportar.");
            }
        }
    }
    
    // Método auxiliar para obtener la empresa actual (usado por el login)
    public Empresa getEmpresa() { return empresa; }
}