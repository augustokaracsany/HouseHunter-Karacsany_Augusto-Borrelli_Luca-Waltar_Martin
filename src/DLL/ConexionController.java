package DLL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionController {
    private static Connection conect;
    private static ConexionController instance;

    private ConexionController() {
        conectar();
    }

    private void conectar() {
        try {
            conect = DriverManager.getConnection("jdbc:mysql://localhost:3306/househunter", "root", "");
            System.out.println(">> Conexión Establecida con Éxito a la Base de Datos.");
        } catch (SQLException e) {
            System.err.println(">> ERROR de conexión: " + e.getMessage());
            conect = null;
        }
    }

    public static ConexionController getInstance() {
        if (instance == null) instance = new ConexionController();
        return instance;
    }

    public Connection getConnection() {
        try {
            // Si la conexión es nula o está cerrada, reconectar
            if (conect == null || conect.isClosed()) {
                System.out.println(">> Conexión cerrada o nula. Reconectando...");
                conectar();
            }
        } catch (SQLException e) {
            System.err.println(">> Error verificando estado de conexión: " + e.getMessage());
            conectar();
        }
        return conect;
    }
}