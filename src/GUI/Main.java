package GUI;

import GUI.EmpresaDashboard;
import GUI.InvitadoDashboard;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import BLL.Persona;
import BLL.Rol;
import Repository.UsuariosController;
import Repository.UsuariosRepository;
import java.util.LinkedList;
import BLL.Empresa;
import BLL.Invitado;
import GUI.LoginFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}