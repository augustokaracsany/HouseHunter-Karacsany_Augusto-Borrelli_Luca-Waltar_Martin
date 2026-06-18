package GUI;

import BLL.Rol;
import Repository.UsuariosController;
import Repository.UsuariosRepository;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegistroDialog extends JDialog {
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JComboBox<Rol> comboRol;
    private JPanel panelCamposDinamicos;
    private CardLayout cardLayout;
    private JTextField txtCuit;
    private JTextField txtRazonSocial;
    private JTextField txtNombre;
    private JTextField txtApellido;

    private UsuariosRepository repo;

    public RegistroDialog(JFrame parent) {
        super(parent, "Registro de Usuario", true);
        repo = new UsuariosController();
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
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        // Título
        JLabel lblTitulo = new JLabel("Crear nueva cuenta", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        add(lblTitulo, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;

        // Email
        gbc.gridx = 0;
        add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(20);
        add(txtEmail, gbc);

        // Contraseña
        gbc.gridy = 2;
        gbc.gridx = 0;
        add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        txtPassword = new JPasswordField(20);
        add(txtPassword, gbc);

        // Rol
        gbc.gridy = 3;
        gbc.gridx = 0;
        add(new JLabel("Rol:"), gbc);
        gbc.gridx = 1;
        comboRol = new JComboBox<>(Rol.values());
        // Eliminar ADMINISTRADOR de las opciones de registro (solo EMPRESA e INVITADO)
        comboRol.removeItem(Rol.ADMINISTRADOR);
        comboRol.addActionListener(e -> actualizarCamposDinamicos());
        add(comboRol, gbc);

        // Panel dinámico (CardLayout)
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        cardLayout = new CardLayout();
        panelCamposDinamicos = new JPanel(cardLayout);

        // Panel para EMPRESA
        JPanel panelEmpresa = new JPanel(new GridLayout(2, 2, 5, 5));
        panelEmpresa.add(new JLabel("CUIT:"));
        txtCuit = new JTextField(15);
        panelEmpresa.add(txtCuit);
        panelEmpresa.add(new JLabel("Razón Social:"));
        txtRazonSocial = new JTextField(15);
        panelEmpresa.add(txtRazonSocial);

        // Panel para INVITADO
        JPanel panelInvitado = new JPanel(new GridLayout(2, 2, 5, 5));
        panelInvitado.add(new JLabel("Nombre:"));
        txtNombre = new JTextField(15);
        panelInvitado.add(txtNombre);
        panelInvitado.add(new JLabel("Apellido:"));
        txtApellido = new JTextField(15);
        panelInvitado.add(txtApellido);

        // Agregar paneles al CardLayout
        panelCamposDinamicos.add(panelEmpresa, "EMPRESA");
        panelCamposDinamicos.add(panelInvitado, "INVITADO");

        add(panelCamposDinamicos, gbc);

        // Botón registrar
        gbc.gridy = 5;
        JButton btnRegistrar = new JButton("Registrar");
        btnRegistrar.addActionListener(e -> registrar());
        add(btnRegistrar, gbc);

        // Botón cancelar
        gbc.gridy = 6;
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        add(btnCancelar, gbc);

        // Configurar estado inicial
        actualizarCamposDinamicos();
        pack();
    }

    private void actualizarCamposDinamicos() {
        Rol rolSeleccionado = (Rol) comboRol.getSelectedItem();
        if (rolSeleccionado != null) {
            cardLayout.show(panelCamposDinamicos, rolSeleccionado.toString());
        }
    }

    private void registrar() {
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email y contraseña son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Rol rol = (Rol) comboRol.getSelectedItem();
        String datoPrincipal = "";
        String datoSecundario = "";

        if (rol == Rol.EMPRESA) {
            datoPrincipal = txtCuit.getText().trim();
            datoSecundario = txtRazonSocial.getText().trim();
            if (datoPrincipal.isEmpty() || datoSecundario.isEmpty()) {
                JOptionPane.showMessageDialog(this, "CUIT y Razón Social son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else if (rol == Rol.INVITADO) {
            datoPrincipal = txtNombre.getText().trim();
            datoSecundario = txtApellido.getText().trim();
            if (datoPrincipal.isEmpty() || datoSecundario.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nombre y Apellido son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            JOptionPane.showMessageDialog(this, "Rol no válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Llamar al repositorio
        boolean exito = repo.registrar(email, password, rol, datoPrincipal, datoSecundario);
        if (exito) {
            JOptionPane.showMessageDialog(this, "¡Registro exitoso! Ya puedes iniciar sesión.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // cerrar el diálogo
        } else {
            JOptionPane.showMessageDialog(this, "Error al registrar. El email podría estar duplicado.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Para pruebas independientes (opcional)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame dummy = new JFrame();
            dummy.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            RegistroDialog dialog = new RegistroDialog(dummy);
            dialog.setVisible(true);
        });
    }
}