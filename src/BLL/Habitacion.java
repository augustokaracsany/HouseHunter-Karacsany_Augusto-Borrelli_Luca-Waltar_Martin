package BLL;

public class Habitacion {
    private int id;
    private String numero;
    private int idReserva;
    private String estado; // "LIBRE" o "OCUPADA"

    public Habitacion() {}

    public Habitacion(String numero, int idReserva, String estado) {
        this.numero = numero;
        this.idReserva = idReserva;
        this.estado = estado;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public int getIdReserva() { return idReserva; }
    public void setIdReserva(int idReserva) { this.idReserva = idReserva; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}