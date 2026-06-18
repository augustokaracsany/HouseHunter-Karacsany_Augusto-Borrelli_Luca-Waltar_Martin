package BLL;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class Administrador extends Persona {
    private String dni;
    private String nombreCompleto;

    public Administrador(String email, String password, String nombreCompleto, String dni, Rol rol) {
        super(email, password, rol);
        this.dni = dni;
        this.nombreCompleto = nombreCompleto;
    }

    @Override
    public String getNombre() {
        return nombreCompleto;
    }

    @Override
    public void mostrarMenu() {
        ImageIcon iconoMenu = new ImageIcon("src/img/HouseHunter_Menu-Administrador.png");
        
        String tituloHtml = "<html><body style='width: 300px; text-align: center;'>"
                          + "<h2>🔑 Panel de Recepción</h2>"
                          + "<b>Admin:</b> " + getNombre() 
                          + "<hr>Seleccione un área de gestión:</body></html>";

        String[] modulos = {
            "🏨 RECEPCIÓN", 
            "🎮 ACTIVIDADES", 
            "📊 REPORTES", 
            "❌ CERRAR SESIÓN"
        };

        int seleccion;
        do {
            seleccion = JOptionPane.showOptionDialog(
                null, tituloHtml, "HouseHunter v1.0",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                iconoMenu, modulos, modulos[0]
            );

            switch (seleccion) {
                case 0: subMenuRecepcion(); break; 
                case 1: subMenuActividades(); break; 
                case 2: subMenuAdminReportes(); break; 
                case 3: JOptionPane.showMessageDialog(null, "Sesión cerrada de " + getNombre()); break;
            }
        } while (seleccion != 3 && seleccion != -1);
    }

    private void subMenuRecepcion() {
        ImageIcon iconoRecepcion = new ImageIcon("src/img/HouseHunter_Menu-Administrador_Recepcion.png");
        String[] opciones = {"Check-in de invitado", "Asignar habitación", "Volver"};
        
        int op = JOptionPane.showOptionDialog(
            null, "<html><body style='width:250px; text-align:center;'><h3>Gestión de Recepción</h3>Seleccione una acción:</body></html>", 
            "Recepción", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, 
            iconoRecepcion, opciones, opciones[0]
        );
        
        if(op == 0) subMenuCheckIn();
        if(op == 1) subMenuHabitacion();
    }

    private void subMenuActividades() {
        ImageIcon iconoActividades = new ImageIcon("src/img/HouseHunter_Menu-Administrador_Actividades.png");
        String[] opciones = {"Monitorear actividades", "Visualizar cronograma", "Actualizar estado", "Entregar premio", "Volver"};
        
        int op = JOptionPane.showOptionDialog(
            null, "<html><body style='width:250px; text-align:center;'><h3>Control de Eventos</h3>Seleccione una opción:</body></html>", 
            "Actividades", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, 
            iconoActividades, opciones, opciones[0]
        );
        
        if (op == 3) {
            subMenuPremio();
        } else if(op != 4 && op != -1) {
            mostrarMensaje(opciones[op]);
        }
    }

    private void subMenuAdminReportes() {
        ImageIcon iconoReportes = new ImageIcon("src/img/HouseHunter_Menu-Administrador_Reportes.png");
        
        JOptionPane.showOptionDialog(
            null, "<html><body style='width:250px; text-align:center;'><h3>Módulo de Reportes</h3>¿Desea generar el reporte consolidado?</body></html>", 
            "Reportes", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, 
            iconoReportes, new String[]{"Generar reporte de evento", "Volver"}, "Generar reporte de evento"
        );
    }

    private void subMenuCheckIn() {
        ImageIcon iconoRecepcion = new ImageIcon("src/img/HouseHunter_Menu-Administrador_Recepcion.png");
        String[] sub = {"Validar invitado autorizado", "Registrar check-in exitoso", "Volver"};
        String op;
        do {
            op = (String)JOptionPane.showInputDialog(
                null, "Seleccione la operación de check-in:", "Submenú Check-in", 
                JOptionPane.PLAIN_MESSAGE, iconoRecepcion, sub, sub[0]
            );
            if(op == null) break;
            if(!op.equals("Volver")) mostrarMensaje(op);
        } while (!op.equals("Volver"));
    }

    private void subMenuHabitacion() {
        ImageIcon iconoRecepcion = new ImageIcon("src/img/HouseHunter_Menu-Administrador_Recepcion.png");
        String[] sub = {"Validar disponibilidad", "Asignar habitación", "Volver"};
        
        int op = JOptionPane.showOptionDialog(
            null, "<html><body style='width:250px; text-align:center;'><h3>Gestión Habitaciones</h3></body></html>", 
            "Submenú Habitaciones", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, 
            iconoRecepcion, sub, sub[0]
        );
        if(op != 2 && op != -1) mostrarMensaje(sub[op]);
    }

    private void subMenuPremio() {
        ImageIcon iconoActividades = new ImageIcon("src/img/HouseHunter_Menu-Administrador_Actividades.png");
        String[] sub = {"Obtener un ganador", "Entregar premio", "Volver"};
        
        int op = JOptionPane.showOptionDialog(
            null, "<html><body style='width:250px; text-align:center;'><h3>Entrega de Premios</h3></body></html>", 
            "Submenú Premios", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, 
            iconoActividades, sub, sub[0]
        );
        if(op != 2 && op != -1) mostrarMensaje(sub[op]);
    }

    private void mostrarMensaje(String accion) {
        JOptionPane.showMessageDialog(null, "Función: " + accion + "\n(En desarrollo)", "Módulo en Construcción", JOptionPane.INFORMATION_MESSAGE);
    }
}