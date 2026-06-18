package GUI;

import BLL.Invitado;
import DLL.RegistroController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TokenLoginDialog extends JDialog {
    private JTextField txtToken;
    private JButton btnValidar;
    private JButton btnCancelar;
    private RegistroController registroController;

    public TokenLoginDialog(JFrame parent) {
        super(parent, "Acceso con Token", true);
        this.registroController = new RegistroController();
        initComponents();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Instrucciones
        JLabel lblInstruccion = new JLabel("Ingrese su token de acceso:", SwingConstants.CENTER);
        lblInstruccion.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(lblInstruccion, gbc);

        // Campo de token
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        add(new JLabel("Token:"), gbc);
        txtToken = new JTextField(30);
        gbc.gridx = 1;
        add(txtToken, gbc);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        btnValidar = new JButton("Validar");
        btnCancelar = new JButton("Cancelar");

        btnValidar.addActionListener(e -> validarToken());
        btnCancelar.addActionListener(e -> dispose());

        panelBotones.add(btnValidar);
        panelBotones.add(btnCancelar);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        add(panelBotones, gbc);

        // Enter key para validar
        getRootPane().setDefaultButton(btnValidar);
    }

    private void validarToken() {
        String token = txtToken.getText().trim();
        if (token.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un token.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validar token con el controlador
        Invitado invitado = registroController.validarToken(token);
        if (invitado != null) {
            JOptionPane.showMessageDialog(this, "✅ Token válido. Bienvenido " + invitado.getNombre());
            dispose(); // cerrar diálogo
            
            // Abrir el dashboard del invitado
            SwingUtilities.invokeLater(() -> {
                InvitadoDashboard dashboard = new InvitadoDashboard(invitado);
                dashboard.setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(this, "❌ Token inválido o evento cancelado.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}