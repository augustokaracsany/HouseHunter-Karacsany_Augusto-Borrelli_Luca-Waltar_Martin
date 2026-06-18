package GUI;

import BLL.*;
import Repository.UsuariosController;
import Repository.UsuariosRepository;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegistro;
    private JButton btnSalir;

    private UsuariosRepository repo;

    public LoginFrame() {
        repo = new UsuariosController();
        initComponents();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("HouseHunter - Login");
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Email
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Email:"), gbc);
        txtEmail = new JTextField(20);
        gbc.gridx = 1;
        add(txtEmail, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Contraseña:"), gbc);
        txtPassword = new JPasswordField(20);
        gbc.gridx = 1;
        add(txtPassword, gbc);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        btnLogin = new JButton("Iniciar Sesión");
        btnRegistro = new JButton("Registrarse");
     // En LoginFrame.java, dentro de initComponents(), después de agregar btnSalir

        JButton btnToken = new JButton("Ingresar con Token");
        btnToken.addActionListener(e -> {
            new TokenLoginDialog(LoginFrame.this).setVisible(true);
        });

        
        btnSalir = new JButton("Salir");

        panelBotones.add(btnLogin);
        panelBotones.add(btnRegistro);
        panelBotones.add(btnToken);
        panelBotones.add(btnSalir);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(panelBotones, gbc);
        
        // Eventos
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

        btnRegistro.addActionListener(e -> {
            new RegistroDialog(null).setVisible(true);
        });

        btnSalir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        pack();
    }

    private void login() {
        String email = txtEmail.getText();
        String password = new String(txtPassword.getPassword());

        if (email.trim().isEmpty() || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos.");
            return;
        }

        Persona usuario = repo.login(email, password);
        if (usuario != null) {
            this.dispose();
            if (usuario.getRol() == Rol.EMPRESA) {
                new EmpresaDashboard((Empresa) usuario).setVisible(true);
            } else if (usuario.getRol() == Rol.INVITADO) {
                new InvitadoDashboard((Invitado) usuario).setVisible(true);
            } else if (usuario.getRol() == Rol.ADMINISTRADOR) {
                // 👇 NUEVO: usar AdminDashboard
                new AdminDashboard((Administrador) usuario).setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Credenciales inválidas.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Para probar el login directamente
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }
}