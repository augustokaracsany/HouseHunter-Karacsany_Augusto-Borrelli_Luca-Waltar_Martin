package Repository;

import BLL.Persona;
import BLL.Rol;
import java.util.LinkedList;

public abstract class UsuariosRepository {
	// Definimos los métodos que todos los controladores deben tener Siempre.
    public abstract Persona login(String email, String password);
    public abstract LinkedList<Persona> listarTodos();
    
    // NUEVO FEAT: "Contrato" para el Registro de NUEVOS Usuarios.
    // agregamos datoTerciario para soportar estructuras con 3 campos complementarios. ( Como el DNI. )
    // NUEVO FIX: Al Registrar Usuarios pide correctamente Ingresar el DNI.
    public abstract boolean registrar(String email, String password, Rol rol, String datoPrincipal, String datoSecundario, String datoTerciario);
}