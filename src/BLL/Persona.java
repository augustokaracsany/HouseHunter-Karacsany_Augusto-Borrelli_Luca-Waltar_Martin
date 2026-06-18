package BLL;

public abstract class Persona {
    protected int id;
    protected String email;
    protected String password;
    protected Rol rol;

    // Constructor Fixeado.
    public Persona(String email, String password, Rol rol) {
        this.email = email;
        this.password = password;
        this.rol = rol;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEmail() { return email; }
    public Rol getRol() { return rol; }
    
    public abstract String getNombre(); 
    public abstract void mostrarMenu();
}