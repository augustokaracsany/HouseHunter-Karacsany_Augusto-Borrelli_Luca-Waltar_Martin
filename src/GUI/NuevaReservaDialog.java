package GUI;

import BLL.Empresa;
import BLL.Reserva;
import DLL.EventoController;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class NuevaReservaDialog extends JDialog {
    private JTextField txtFecha;
    private JTextField txtNumInvitados;
    private EventoController eventoController;
    private Empresa empresa;
    private Reserva reservaCreada;

    public NuevaReservaDialog(JFrame parent, Empresa empresa) {
        super(parent, "Nueva Reserva", true);
        this.empresa = empresa;
        this.eventoController = new EventoController();
        initComponents();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fecha (YYYY-MM-DD)
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Fecha (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        txtFecha = new JTextField(15);
        txtFecha.setText(LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE));
        add(txtFecha, gbc);

        // Número de invitados
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Número de invitados:"), gbc);
        gbc.gridx = 1;
        txtNumInvitados = new JTextField(5);
        txtNumInvitados.setText("10");
        add(txtNumInvitados, gbc);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnCrear = new JButton("Crear Reserva");
        btnCrear.addActionListener(e -> crearReserva());
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());

        panelBotones.add(btnCrear);
        panelBotones.add(btnCancelar);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(panelBotones, gbc);
    }

    private void crearReserva() {
        try {
            LocalDate fecha = LocalDate.parse(txtFecha.getText().trim());
            int numInvitados = Integer.parseInt(txtNumInvitados.getText().trim());

            if (eventoController.verificarDisponibilidad(fecha, numInvitados)) {
                Reserva nueva = new Reserva(empresa, fecha, numInvitados);
                eventoController.crearReserva(nueva);
                this.reservaCreada = nueva;
                JOptionPane.showMessageDialog(this, "Reserva creada con ID: " + nueva.getId());
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "No hay disponibilidad para esa fecha.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Reserva getReservaCreada() {
        return reservaCreada;
    }

    public static void main(String[] args) {
        // Prueba rápida
        SwingUtilities.invokeLater(() -> {
            JFrame dummy = new JFrame();
            dummy.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            NuevaReservaDialog dialog = new NuevaReservaDialog(dummy, new Empresa("test@test.com", "pass", "123", "Test", BLL.Rol.EMPRESA));
            dialog.setVisible(true);
        });
    }
}