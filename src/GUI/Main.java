package GUI;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import BLL.Persona;
import BLL.Rol;
import Repository.UsuariosController;
import Repository.UsuariosRepository;
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) {
        UsuariosRepository repo = new UsuariosController();
        
        // Imagen institucional o de bienvenida del proyecto, no editar ruta o nombre del archivo por favor.
        ImageIcon iconoBienvenida = new ImageIcon("src/img/HouseHunter_Menu-Principal.gif");

        String textoHtml = "<html><body style='width: 300px; text-align: center;'>"
                         + "<h2>🏠 Sistema HouseHunter v1.0</h2>"
                         + "Bienvenido al gestor de accesos hoteleros corporativos.<br>"
                         + "<hr>Seleccione una opción para continuar:</body></html>";

        String[] opcionesInicio = {"🔐 INICIAR SESIÓN", "📝 REGISTRARSE", "❌ SALIR"};

        int menuPrincipal;
        do {
            menuPrincipal = JOptionPane.showOptionDialog(
                null, textoHtml, "HouseHunter Principal",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                iconoBienvenida, opcionesInicio, opcionesInicio[0]
            );

            if (menuPrincipal == 0) {
                // ================= FLUX DE LOGUEO =================
                String email = JOptionPane.showInputDialog(null, "Ingrese su email:", "Login", JOptionPane.QUESTION_MESSAGE);
                if (email != null && !email.trim().isEmpty()) {
                    String password = JOptionPane.showInputDialog(null, "Ingrese su contraseña:", "Login", JOptionPane.QUESTION_MESSAGE);
                    if (password != null && !password.trim().isEmpty()) {
                        
                        Persona usuario = repo.login(email, password);

                        if (usuario != null) {
                            JOptionPane.showMessageDialog(null, "¡Login exitoso!\nBienvenido " + usuario.getNombre());
                            usuario.mostrarMenu();
                            
                            // Log por consola/terminal del listado técnico para Verificación
                            System.out.println("--- LISTA DE USUARIOS EN BASE DE DATOS ---");
                            LinkedList<Persona> todos = repo.listarTodos();
                            for (Persona p : todos) {
                                System.out.println("ID: " + p.getId() + " | Nombre: " + p.getNombre() + 
                                                   " | Email: " + p.getEmail() + " | Rol: " + p.getRol());
                            }
                            System.out.println("------------------------------------------");
                        } else {
                            JOptionPane.showMessageDialog(null, "Credenciales inválidas. Intente nuevamente.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }

            } else if (menuPrincipal == 1) {
                // ================= FLUX DE REGISTRO =================
                String emailReg = JOptionPane.showInputDialog(null, "Cree su email de usuario:", "Registro", JOptionPane.QUESTION_MESSAGE);
                if (emailReg != null && !emailReg.trim().isEmpty()) {
                    String passReg = JOptionPane.showInputDialog(null, "Cree su contraseña:", "Registro", JOptionPane.QUESTION_MESSAGE);
                    if (passReg != null && !passReg.trim().isEmpty()) {
                        
                        String[] rolesDisponibles = {"EMPRESA", "INVITADO"};
                        int seleccionRol = JOptionPane.showOptionDialog(
                            null, "Seleccione el tipo de cuenta corporativa que desea crear:", "Selector de Rol",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, rolesDisponibles, rolesDisponibles[0]
                        );

                        if (seleccionRol != -1) {
                            Rol rolElegido = (seleccionRol == 0) ? Rol.EMPRESA : Rol.INVITADO;
                            String dato1 = "", dato2 = "", dato3 = ""; // Agregado el dato3, no volver a borrar.

                            if (rolElegido == Rol.EMPRESA) {
                                dato1 = JOptionPane.showInputDialog(null, "Ingrese el CUIT de la empresa:", "Datos Empresa", JOptionPane.QUESTION_MESSAGE);
                                dato2 = JOptionPane.showInputDialog(null, "Ingrese la Razón Social:", "Datos Empresa", JOptionPane.QUESTION_MESSAGE);
                                dato3 = null; // Empresa no usa un tercer dato.
                            } else {
                                dato1 = JOptionPane.showInputDialog(null, "Ingrese su Nombre:", "Datos Invitado", JOptionPane.QUESTION_MESSAGE);
                                dato2 = JOptionPane.showInputDialog(null, "Ingrese su Apellido:", "Datos Invitado", JOptionPane.QUESTION_MESSAGE);
                                dato3 = JOptionPane.showInputDialog(null, "Ingrese su DNI:", "Datos Invitado", JOptionPane.QUESTION_MESSAGE); // 🚀 Capturamos DNI
                            }

                            // Validación Agregando 'dato3' para el Caso de Invitado.
                            boolean datosValidos = (rolElegido == Rol.EMPRESA) 
                                ? (dato1 != null && dato2 != null && !dato1.trim().isEmpty() && !dato2.trim().isEmpty())
                                : (dato1 != null && dato2 != null && dato3 != null && !dato1.trim().isEmpty() && !dato2.trim().isEmpty() && !dato3.trim().isEmpty());

                            if (datosValidos) {
                                // Pasamos los 6 parámetros requeridos por el Nuevo Contrato Fixeado.
                                boolean exito = repo.registrar(emailReg, passReg, rolElegido, dato1, dato2, dato3);
                                if (exito) {
                                    JOptionPane.showMessageDialog(null, "¡Registro completado de forma segura!\nYa puede iniciar sesión con sus credenciales.");
                                } else {
                                    JOptionPane.showMessageDialog(null, "Hubo un error al guardar los datos. El email o DNI podría estar duplicado.", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    }
                }
            }
        } while (menuPrincipal != 2 && menuPrincipal != -1);
        
        JOptionPane.showMessageDialog(null, "¡Gracias por utilizar HouseHunter!");
    }
}