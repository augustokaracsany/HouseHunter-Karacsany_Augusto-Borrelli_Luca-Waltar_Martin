package Repository;

import BLL.*;
import DLL.ConexionController;
import java.sql.*;
import java.util.LinkedList;

public class UsuariosController extends UsuariosRepository {

    @Override
    public Persona login(String email, String password) {
        Persona usuario = null;
        String sql = "SELECT u.*, p.dni, p.nombre, p.apellido, e.cuit, e.razon_social " +
                     "FROM usuarios u " +
                     "LEFT JOIN datos_personas p ON u.id = p.id_usuario " +
                     "LEFT JOIN datos_empresas e ON u.id = e.id_usuario " +
                     "WHERE u.email = ?";
        
        Connection con = ConexionController.getInstance().getConnection();
        
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String passHasheadaBD = rs.getString("password");

                    if (Hashing.verificar(password, passHasheadaBD)) {
                        Rol rolEnum = Rol.valueOf(rs.getString("rol"));
                        String mail = rs.getString("email");

                        if (rolEnum == Rol.EMPRESA) {
                            usuario = new Empresa(mail, passHasheadaBD, rs.getString("cuit"), rs.getString("razon_social"), rolEnum);
                        } else if (rolEnum == Rol.ADMINISTRADOR) {
                            String nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido");
                            usuario = new Administrador(mail, passHasheadaBD, nombreCompleto, rs.getString("dni"), rolEnum);
                        } else {
                            usuario = new Invitado(mail, passHasheadaBD, rs.getString("nombre"), rolEnum);
                        }
                        
                        usuario.setId(rs.getInt("id"));
                        System.out.println("ℹ️ Hashing: Login exitoso para el usuario: " + mail);
                        
                    } else {
                        System.out.println("❌ Hashing: Contraseña incorrecta para el usuario: " + email);
                    }
                } else {
                    System.out.println("❌ Hashing: No se encontró ningún usuario con el email: " + email);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en login: " + e.getMessage());
        }
        return usuario;
    }

    @Override
    public LinkedList<Persona> listarTodos() {
        LinkedList<Persona> lista = new LinkedList<>();
        String sql = "SELECT u.*, p.dni, p.nombre, p.apellido, e.cuit, e.razon_social " +
                     "FROM usuarios u " +
                     "LEFT JOIN datos_personas p ON u.id = p.id_usuario " +
                     "LEFT JOIN datos_empresas e ON u.id = e.id_usuario";
                     
        Connection con = ConexionController.getInstance().getConnection();
        
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Rol rolEnum = Rol.valueOf(rs.getString("rol"));
                Persona p;
                String mail = rs.getString("email");
                String pass = rs.getString("password");

                if (rolEnum == Rol.EMPRESA) {
                    p = new Empresa(mail, pass, rs.getString("cuit"), rs.getString("razon_social"), rolEnum);
                } else if (rolEnum == Rol.ADMINISTRADOR) {
                    String nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido");
                    p = new Administrador(mail, pass, nombreCompleto, rs.getString("dni"), rolEnum);
                } else {
                    p = new Invitado(mail, pass, rs.getString("nombre"), rolEnum);
                }
                
                p.setId(rs.getInt("id"));
                lista.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public boolean registrar(String email, String password, Rol rol, String datoPrincipal, String datoSecundario, String datoTerciario) {
        String sqlUsuario = "INSERT INTO usuarios (email, password, rol) VALUES (?, ?, ?)";
        Connection con = ConexionController.getInstance().getConnection();
        
        try {
        	// Desactivamos el AUTOCOMMIT para manejarlo como una Única Transacción Segura.
            con.setAutoCommit(false);
            
            try (PreparedStatement psUser = con.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, email);
                // Acá se Encripta la Clave con la configuración de Gamaliel.
                psUser.setString(2, Hashing.hash(password));
                psUser.setString(3, rol.toString());
                
                // Obtenemos el ID Auto-Incremental asignado automáticamente por MySQL.
                int filasAfectadas = psUser.executeUpdate();
                if (filasAfectadas == 0) {
                    con.rollback();
                    return false;
                }
                
                int idUsuarioGenerado = 0;
                try (ResultSet generatedKeys = psUser.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        idUsuarioGenerado = generatedKeys.getInt(1);
                    }
                }
                
                // Dependiendo del Rol, insertamos en su tabla de datos complementaria que corresponden.
                if (rol == Rol.EMPRESA) {
                    String sqlEmpresa = "INSERT INTO datos_empresas (id_usuario, cuit, razon_social) VALUES (?, ?, ?)";
                    try (PreparedStatement psEmp = con.prepareStatement(sqlEmpresa)) {
                        psEmp.setInt(1, idUsuarioGenerado);
                        psEmp.setString(2, datoPrincipal);  // CUIT.
                        psEmp.setString(3, datoSecundario); // Razón Social.
                        psEmp.executeUpdate();
                    }
                } else if (rol == Rol.INVITADO) {
                    String sqlPersona = "INSERT INTO datos_personas (id_usuario, nombre, apellido, dni) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement psPers = con.prepareStatement(sqlPersona)) {
                        psPers.setInt(1, idUsuarioGenerado);
                        psPers.setString(2, datoPrincipal);   // Nombre.
                        psPers.setString(3, datoSecundario);  // Apellido.
                        psPers.setString(4, datoTerciario);   // 🚀 Ahora el DNI debería funcionar, como nadie se dió cuenta de esto antes.
                        psPers.executeUpdate();
                    }
                }
                
                con.commit();
                System.out.println("ℹ️ Hashing/SQL: Usuario registrado con éxito: " + email);
                return true;
                
            } catch (SQLException e) {
                con.rollback();
                System.err.println("Error en transacción de registro: " + e.getMessage());
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error de conexión al registrar: " + e.getMessage());
            return false;
        }
    }
}