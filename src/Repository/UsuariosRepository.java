package Repository;

import BLL.Persona;
import BLL.Rol;
import java.util.LinkedList;

public abstract class UsuariosRepository {
    // Definimos los métodos que todos los controladores deben tener Siempre.
    public abstract Persona login(String email, String password);
    public abstract LinkedList<Persona> listarTodos();
    
    // NUEVO FEAT: "Contrato" para el Registro de NUEVOS Usuarios.
    public abstract boolean registrar(String email, String password, Rol rol, String datoPrincipal, String datoSecundario);
}