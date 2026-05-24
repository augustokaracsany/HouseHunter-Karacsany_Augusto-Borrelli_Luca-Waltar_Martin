package BLL;

public class Habitacion {
    private int id;
    private String numero;
    private String tipo;          // Traído de feat/luca
    private int capacidad;        // Traído de feat/luca
    private EstadoHabitacion estado; // Diseño de Augusto (Enum estratégico)

    // Constructor completo unificado
    public Habitacion(int id, String numero, String tipo, int capacidad, EstadoHabitacion estado) {
        this.id = id;
        this.numero = numero;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.estado = estado;
    }

    // Getters y Setters unificados y ordenados
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }

    public EstadoHabitacion getEstado() { return estado; }
    public void setEstado(EstadoHabitacion estado) { this.estado = estado; }
}