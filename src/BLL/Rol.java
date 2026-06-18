package BLL;

public enum Rol {
    ADMINISTRADOR("Admin Hotel"),
    EMPRESA("Empresa Cliente"),
    INVITADO("Invitado");

    private final String descripcion;

    Rol(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}