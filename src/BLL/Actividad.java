package BLL;

import java.time.LocalDateTime;

	public class Actividad {
	    private int id;
	    private Reserva reserva;
	    private String nombre;
	    private String descripcion;
	    private LocalDateTime fechaHora;
	    private int duracionMinutos;
	    private int cupoMaximo;
	    private Importancia importancia;
	    private String categoria;

	    public Actividad() {}

	    public Actividad(String nombre, LocalDateTime fechaHora, int duracionMinutos, int cupoMaximo, Importancia importancia, String categoria) {
	        this.nombre = nombre;
	        this.fechaHora = fechaHora;
	        this.duracionMinutos = duracionMinutos;
	        this.cupoMaximo = cupoMaximo;
	        this.importancia = importancia;
	        this.categoria = categoria;
	    }
	    
	    public int getId() { return id; }
	    public void setId(int id) { this.id = id; }
	    
	    public Reserva getReserva() { return reserva; }
	    public void setReserva(Reserva reserva) { this.reserva = reserva; }
	    
	    public String getNombre() { return nombre; }
	    public void setNombre(String nombre) { this.nombre = nombre; }
	    
	    public String getDescripcion() { return descripcion; }
	    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
	    
	    public LocalDateTime getFechaHora() { return fechaHora; }
	    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
	    
	    public int getDuracionMinutos() { return duracionMinutos; }
	    public void setDuracionMinutos(int duracionMinutos) { this.duracionMinutos = duracionMinutos; }
	    
	    public int getCupoMaximo() { return cupoMaximo; }
	    public void setCupoMaximo(int cupoMaximo) { this.cupoMaximo = cupoMaximo; }
	    
	    public Importancia getImportancia() { return importancia; }
	    public void setImportancia(Importancia importancia) { this.importancia = importancia; }
	    
	    public String getCategoria() { return categoria; }
	    public void setCategoria(String categoria) { this.categoria = categoria; }
}
	