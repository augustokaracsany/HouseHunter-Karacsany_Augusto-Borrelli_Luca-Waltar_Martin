package BLL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Reserva {
	  private int id;
	    private Empresa empresa;          // Objeto empresa (dueño de la reserva)
	    private LocalDateTime fechaReserva;
	    private LocalDate fechaEvento;
	    private int numInvitados;
	    private String estado;            // PENDIENTE, CONFIRMADA, CANCELADA
	    private Plantilla plantilla;
	    private List<Actividad> actividades;
	    private List<Invitado> invitados;
	
	    public Reserva() {
	        this.fechaReserva = LocalDateTime.now();
	        this.estado = "PENDIENTE";
	        this.actividades = new ArrayList<>();
	        this.invitados = new ArrayList<>();

	    }
	    
	    public Reserva(Empresa empresa, LocalDate fechaEvento, int numInvitados) {
	        this();
	        this.empresa = empresa;
	        this.fechaEvento = fechaEvento;
	        this.numInvitados = numInvitados;
	    }
	    
	    // Getters y Setters
	    public int getId() { return id; }
	    public void setId(int id) { this.id = id; }
	    
	    public Empresa getEmpresa() { return empresa; }
	    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
	    
	    public LocalDateTime getFechaReserva() { return fechaReserva; }
	    public void setFechaReserva(LocalDateTime fechaReserva) { this.fechaReserva = fechaReserva; }
	    
	    public LocalDate getFechaEvento() { return fechaEvento; }
	    public void setFechaEvento(LocalDate fechaEvento) { this.fechaEvento = fechaEvento; }
	    
	    public int getNumInvitados() { return numInvitados; }
	    public void setNumInvitados(int numInvitados) { this.numInvitados = numInvitados; }
	    
	    public String getEstado() { return estado; }
	    public void setEstado(String estado) { this.estado = estado; }
	    
	    public Plantilla getPlantilla() { return plantilla; }
	    public void setPlantilla(Plantilla plantilla) { this.plantilla = plantilla; }
	    
	    public List<Actividad> getActividades() { return actividades; }
	    public void setActividades(List<Actividad> actividades) { this.actividades = actividades; }
	    
	    public List<Invitado> getInvitados() { return invitados; }
	    public void setInvitados(List<Invitado> invitados) { this.invitados = invitados; }
	}





