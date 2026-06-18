package GUI;

import BLL.Actividad;
import BLL.CategoriaActividad;
import BLL.Importancia;
import BLL.Reserva;
import DLL.EventoController;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NuevaActividadDialog extends JDialog {
    private Reserva reserva;
    private JTextField txtNombre;
    private JTextField txtFechaHora;
    private JTextField txtDuracion;
    private JTextField txtCupo;
    private JComboBox<String> comboImportancia;
    private JComboBox<CategoriaActividad> comboCategoria;
    private EventoController eventoController;
    private boolean guardado = false;

    public NuevaActividadDialog(JFrame parent, Reserva reserva) {
        super(parent, "Nueva Actividad", true);
        this.reserva = reserva;
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

        int row = 0;
        // Nombre
        gbc.gridx = 0; gbc.gridy = row;
        add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        txtNombre = new JTextField(20);
        add(txtNombre, gbc);

        // Fecha y hora (YYYY-MM-DD HH:MM)
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        add(new JLabel("Fecha y hora (YYYY-MM-DD HH:MM):"), gbc);
        gbc.gridx = 1;
        txtFechaHora = new JTextField(20);
        txtFechaHora.setText(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        add(txtFechaHora, gbc);

        // Duración
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        add(new JLabel("Duración (min):"), gbc);
        gbc.gridx = 1;
        txtDuracion = new JTextField(5);
        txtDuracion.setText("60");
        add(txtDuracion, gbc);

        // Cupo máximo
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        add(new JLabel("Cupo máximo:"), gbc);
        gbc.gridx = 1;
        txtCupo = new JTextField(5);
        txtCupo.setText("20");
        add(txtCupo, gbc);

        // Importancia
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        add(new JLabel("Importancia:"), gbc);
        gbc.gridx = 1;
        comboImportancia = new JComboBox<>(new String[]{"BAJA", "MEDIA", "ALTA"});
        add(comboImportancia, gbc);

        // Categoría
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        add(new JLabel("Categoría:"), gbc);
        gbc.gridx = 1;
        comboCategoria = new JComboBox<>(CategoriaActividad.values());
        add(comboCategoria, gbc);

        // Botones
        row++;
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> guardarActividad());
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());

        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);

        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        add(panelBotones, gbc);
    }

    private void guardarActividad() {
        try {
            String nombre = txtNombre.getText().trim();
            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre es obligatorio.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String fechaHoraStr = txtFechaHora.getText().trim();
            LocalDateTime fechaHora = LocalDateTime.parse(fechaHoraStr.replace(" ", "T"));

            int duracion = Integer.parseInt(txtDuracion.getText().trim());
            int cupo = Integer.parseInt(txtCupo.getText().trim());

            String importancia = (String) comboImportancia.getSelectedItem();
            String categoria = comboCategoria.getSelectedItem().toString();

            Actividad act = new Actividad(nombre, fechaHora, duracion, cupo, Importancia.valueOf(importancia), categoria);
            act.setReserva(reserva);

            List<Actividad> actividadesExistentes = eventoController.obtenerActividadesPorReserva(reserva.getId());
            actividadesExistentes.add(act);

            if (eventoController.guardarCronograma(reserva.getId(), actividadesExistentes)) {
                JOptionPane.showMessageDialog(this, "Actividad guardada correctamente.");
                guardado = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error al guardar la actividad.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en los datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isGuardado() {
        return guardado;
    }
}