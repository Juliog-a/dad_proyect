package es.us.lsi.dad;

import java.util.Calendar;
import java.util.Objects;

public class Actuador_rele {
	private Integer id;
	private boolean encendido;
	private long timestamp;
	private Integer nPlaca;
	
	public Actuador_rele() {
		super();
		timestamp=Calendar.getInstance().getTimeInMillis();
		encendido=false;
	}
	
	public Actuador_rele(long timestamp, boolean encendido, Integer id, Integer nPlaca) {
	super();
	this.id= id;
	this.timestamp=timestamp;
	this.encendido=encendido;
	this.nPlaca=nPlaca;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public boolean isEncendido() {
		return encendido;
	}

	public void setEncendido(boolean encendido) {
		this.encendido = encendido;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Integer getnPlaca() {
		return nPlaca;
	}

	public void setnPlaca(Integer nPlaca) {
		this.nPlaca = nPlaca;
	}

	@Override
	public int hashCode() {
		return Objects.hash(encendido, id, nPlaca, timestamp);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Actuador_rele other = (Actuador_rele) obj;
		return encendido == other.encendido && Objects.equals(id, other.id) && Objects.equals(nPlaca, other.nPlaca)
				&& timestamp == other.timestamp;
	}

	@Override
	public String toString() {
		return "Actuador_rele [id=" + id + ", encendido=" + encendido + ", timestamp=" + timestamp + ", nPlaca="
				+ nPlaca + ", getId()=" + getId() + ", isEncendido()=" + isEncendido() + ", getTimestamp()="
				+ getTimestamp() + ", getnPlaca()=" + getnPlaca() + ", hashCode()=" + hashCode() + ", getClass()="
				+ getClass() + ", toString()=" + super.toString() + "]";
	}

}
