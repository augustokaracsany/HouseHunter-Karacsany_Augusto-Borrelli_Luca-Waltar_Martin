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
import GUI.UITheme;
import java.util.List;
import java.util.Map;
import GUI.Main;
public class InvitadoDashboard extends JFrame {
    private Invitado invitado;
    private Reserva reserva;

    // Controladores
    private RegistroController registroController;
    private EventoController eventoController;
    private HotelController hotelController;

    // Componentes UI
    private JTabbedPane tabbedPane;
    private JTable tablaCronograma;
    private DefaultTableModel modelCronograma;
    private JTable tablaActividades;
    private DefaultTableModel modelActividades;
    private JTextArea txtInfoHabitacion;
    private JTextArea txtInfoPremios;
    private JLabel lblEstadoConfirmacion;

    public InvitadoDashboard(Invitado invitado) {
        this.invitado = invitado;
        this.reserva = invitado.getReserva(); // debe venir cargado desde el login
        this.registroController = new RegistroController();
        this.eventoController = new EventoController();
        this.hotelController = new HotelController();

        initComponents();
        UITheme.applyTheme(this);
        cargarDatosIniciales();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        setTitle("Panel de Invitado - " + invitado.getNombre());
        setSize(850, 650);
        setLayout(new BorderLayout());

        // Panel superior con info del invitado y estado de confirmación
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel lblInvitado = new JLabel("Bienvenido, " + invitado.getNombre());
        lblInvitado.setFont(new Font("Arial", Font.BOLD, 14));
        panelSuperior.add(lblInvitado, BorderLayout.WEST);

        lblEstadoConfirmacion = new JLabel();
        actualizarEstadoConfirmacion();
        panelSuperior.add(lblEstadoConfirmacion, BorderLayout.EAST);

        add(panelSuperior, BorderLayout.NORTH);

        // TabbedPane
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Mi Cronograma", crearPanelCronograma());
        tabbedPane.addTab("Confirmar Asistencia", crearPanelConfirmar());
        tabbedPane.addTab("Actividades", crearPanelActividades());
        tabbedPane.addTab("Mi Habitación", crearPanelHabitacion());
        tabbedPane.addTab("Premios", crearPanelPremios());

        add(tabbedPane, BorderLayout.CENTER);

        // Barra de estado (opcional)
        JLabel lblStatus = new JLabel("HouseHunter - Invitado");
        lblStatus.setBorder(new EmptyBorder(5, 10, 5, 10));
        add(lblStatus, BorderLayout.SOUTH);
    }

    private void actualizarEstadoConfirmacion() {
        if (invitado.isAsistenciaConfirmada()) {
            lblEstadoConfirmacion.setText("✅ Asistencia confirmada");
            lblEstadoConfirmacion.setForeground(Color.GREEN);
        } else {
            lblEstadoConfirmacion.setText("❌ Asistencia pendiente");
            lblEstadoConfirmacion.setForeground(Color.RED);
        }
    }

    private void cargarDatosIniciales() {
        if (reserva == null) {
            JOptionPane.showMessageDialog(this, 
                "No se pudo cargar la información de tu reserva.\nContacta al administrador.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        cargarCronograma();
        cargarActividades();
        cargarInfoHabitacion();
    }

    // ================== PANEL MI CRONOGRAMA ==================
    private JPanel crearPanelCronograma() {
        JPanel panel = new JPanel(new BorderLayout());
        modelCronograma = new DefaultTableModel(
            new String[]{"Nombre", "Fecha/Hora", "Duración (min)", "Importancia"}, 0);
        tablaCronograma = new JTable(modelCronograma);
        JScrollPane scroll = new JScrollPane(tablaCronograma);
        panel.add(scroll, BorderLayout.CENTER);

        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> cargarCronograma());
        JPanel panelBotones = new JPanel();
        panelBotones.add(btnRefrescar);
        panel.add(panelBotones, BorderLayout.SOUTH);

        return panel;
    }

    private void cargarCronograma() {
        modelCronograma.setRowCount(0);
        if (reserva == null) return;
        List<Actividad> acts = registroController.obtenerCronograma(reserva.getId());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Actividad a : acts) {
            modelCronograma.addRow(new Object[]{
                a.getNombre(),
                a.getFechaHora().format(fmt),
                a.getDuracionMinutos(),
                a.getImportancia()
            });
        }
    }

    // ================== PANEL CONFIRMAR ASISTENCIA ==================
    private JPanel crearPanelConfirmar() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel lblMensaje = new JLabel();
        if (invitado.isAsistenciaConfirmada()) {
            lblMensaje.setText("Ya has confirmado tu asistencia. ¡Te esperamos!");
            lblMensaje.setFont(new Font("Arial", Font.BOLD, 14));
            lblMensaje.setForeground(Color.GREEN);
        } else {
            lblMensaje.setText("Aún no has confirmado tu asistencia.");
            lblMensaje.setFont(new Font("Arial", Font.PLAIN, 14));
        }
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblMensaje, gbc);

        JButton btnConfirmar = new JButton("Confirmar mi asistencia");
        btnConfirmar.setEnabled(!invitado.isAsistenciaConfirmada());
        btnConfirmar.addActionListener(e -> confirmarAsistencia());
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(btnConfirmar, gbc);

        // Si ya confirmó, mostrar botón para ver habitación directamente
        JButton btnVerHabitacion = new JButton("Ver mi habitación");
        btnVerHabitacion.addActionListener(e -> {
            tabbedPane.setSelectedIndex(3); // ir a la pestaña de habitación
        });
        gbc.gridx = 1;
        panel.add(btnVerHabitacion, gbc);

        return panel;
    }

    private void confirmarAsistencia() {
        if (invitado.isAsistenciaConfirmada()) {
            JOptionPane.showMessageDialog(this, "Ya confirmaste tu asistencia.");
            return;
        }

        // Llamar al controlador (que maneja la asignación de habitación)
        boolean ok = registroController.confirmarAsistencia(invitado.getId());
        if (ok) {
            invitado.setAsistenciaConfirmada(true);
            actualizarEstadoConfirmacion();
            JOptionPane.showMessageDialog(this, "✅ Asistencia confirmada. Se te ha asignado una habitación.");
            cargarInfoHabitacion(); // actualizar la pestaña de habitación
            // Refrescar la pestaña de confirmación
            tabbedPane.setComponentAt(1, crearPanelConfirmar());
        } else {
            JOptionPane.showMessageDialog(this, "❌ Error al confirmar asistencia.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
    }

    // ================== PANEL ACTIVIDADES ==================
    private JPanel crearPanelActividades() {
        JPanel panel = new JPanel(new BorderLayout());
        modelActividades = new DefaultTableModel(
            new String[]{"ID", "Nombre", "Fecha/Hora", "Cupo", "Categoría"}, 0);
        tablaActividades = new JTable(modelActividades);
        JScrollPane scroll = new JScrollPane(tablaActividades);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel();
        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> cargarActividades());

        JButton btnDetalle = new JButton("Ver Detalle");
        btnDetalle.addActionListener(e -> verDetalleActividad());

        JButton btnRegistrar = new JButton("Registrar mi asistencia");
        btnRegistrar.addActionListener(e -> registrarAsistenciaActividad());

        panelBotones.add(btnRefrescar);
        panelBotones.add(btnDetalle);
        panelBotones.add(btnRegistrar);
        panel.add(panelBotones, BorderLayout.SOUTH);

        return panel;
    }

    private void cargarActividades() {
        modelActividades.setRowCount(0);
        if (reserva == null) return;
        List<Actividad> acts = registroController.listarActividadesPorReserva(reserva.getId());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Actividad a : acts) {
            modelActividades.addRow(new Object[]{
                a.getId(),
                a.getNombre(),
                a.getFechaHora().format(fmt),
                a.getCupoMaximo(),
                a.getCategoria()
            });
        }
    }

    private void verDetalleActividad() {
        int row = tablaActividades.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una actividad.");
            return;
        }
        int idAct = (int) modelActividades.getValueAt(row, 0);
        Actividad a = registroController.obtenerDetalleActividad(idAct);
        if (a == null) {
            JOptionPane.showMessageDialog(this, "No se pudo obtener el detalle.");
            return;
        }
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
        JOptionPane.showMessageDialog(this, detalle, "Detalle de Actividad", JOptionPane.INFORMATION_MESSAGE);
    }

    private void registrarAsistenciaActividad() {
        int row = tablaActividades.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una actividad.");
            return;
        }
        int idAct = (int) modelActividades.getValueAt(row, 0);
        boolean ok = registroController.registrarAsistenciaActividad(invitado.getId(), idAct);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Asistencia registrada correctamente.");
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo registrar (quizás ya estabas registrado).", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================== PANEL MI HABITACIÓN ==================
    private JPanel crearPanelHabitacion() {
        JPanel panel = new JPanel(new BorderLayout());
        txtInfoHabitacion = new JTextArea();
        txtInfoHabitacion.setEditable(false);
        txtInfoHabitacion.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(txtInfoHabitacion);
        panel.add(scroll, BorderLayout.CENTER);

        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> cargarInfoHabitacion());
        JPanel panelBotones = new JPanel();
        panelBotones.add(btnRefrescar);
        panel.add(panelBotones, BorderLayout.SOUTH);

        return panel;
    }

    private void cargarInfoHabitacion() {
        if (invitado.isAsistenciaConfirmada()) {
            String info = registroController.obtenerHabitacionAsignada(invitado.getId());
            if (info != null) {
                txtInfoHabitacion.setText("Tu habitación asignada: " + info);
            } else {
                txtInfoHabitacion.setText("Aún no se te ha asignado una habitación.\nPor favor, contacta con recepción.");
            }
        } else {
            txtInfoHabitacion.setText("Debes confirmar tu asistencia primero para que se te asigne una habitación.");
        }
    }

    // ================== PANEL PREMIOS ==================
    private JPanel crearPanelPremios() {
        JPanel panel = new JPanel(new BorderLayout());
        txtInfoPremios = new JTextArea();
        txtInfoPremios.setEditable(false);
        txtInfoPremios.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(txtInfoPremios);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel();
        JButton btnVerificar = new JButton("Verificar elegibilidad");
        btnVerificar.addActionListener(e -> verificarPremio());

        JButton btnGenerar = new JButton("Generar Voucher");
        btnGenerar.addActionListener(e -> generarVoucher());

        panelBotones.add(btnVerificar);
        panelBotones.add(btnGenerar);
        panel.add(panelBotones, BorderLayout.SOUTH);

        return panel;
    }

    private void verificarPremio() {
        boolean cumple = registroController.verificarAsistenciaMinima(invitado.getId(), 2);
        if (cumple) {
            txtInfoPremios.setText("🎉 ¡Felicidades! Cumples con el mínimo de 2 actividades asistidas.\nPuedes generar tu voucher para participar en el sorteo.");
        } else {
            txtInfoPremios.setText("❌ No cumples con el mínimo de 2 actividades asistidas.\nParticipa en más actividades para ser elegible.");
        }
    }

    private void generarVoucher() {
        boolean cumple = registroController.verificarAsistenciaMinima(invitado.getId(), 2);
        if (!cumple) {
            JOptionPane.showMessageDialog(this, "No cumples con el mínimo de asistencias.");
            return;
        }
        String voucher = registroController.generarVoucher();
        JOptionPane.showMessageDialog(this, "🎫 Tu voucher para el sorteo es: " + voucher);
        txtInfoPremios.setText("Voucher generado: " + voucher + "\nGuarda este código para reclamar tu premio.");
    }
}
