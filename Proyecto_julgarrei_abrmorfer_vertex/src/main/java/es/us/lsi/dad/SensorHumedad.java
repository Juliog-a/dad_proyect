package es.us.lsi.dad;

import java.util.Calendar;
import java.util.Objects;

public class SensorHumedad {
	private Integer id;
	private Integer nPlaca;
	private Long timestamp;  //Calendar.getInstance().getTimeInMillis()
	private Float humedad;
	private Float temperatura;
	
	public SensorHumedad(int id, Integer nPlaca, long timestamp, float humedad, float temperatura) {
		super();	
		timestamp=Calendar.getInstance().getTimeInMillis();
		humedad=0.0f;
		temperatura= 0.0f;
	}
	public SensorHumedad() {
		super();	
		timestamp=Calendar.getInstance().getTimeInMillis();
		humedad=0.0f;
		temperatura= 0.0f;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Float getHumedad() {
		return humedad;
	}

	public void setHumedad(Float humedad) {
		this.humedad = humedad;
	}

	public Float getTemperatura() {
		return temperatura;
	}

	public void setTemperatura(Float temperatura) {
		this.temperatura = temperatura;
	}

	@Override
	public String toString() {
		return "SensorHumedad [id=" + id + "Placa= "+ nPlaca +", timestamp=" + timestamp + ", humedad=" + humedad + ", temperatura="
				+ temperatura + "]";
	}

	
	

	@Override
	public int hashCode() {
		return Objects.hash(humedad, id, nPlaca, temperatura, timestamp);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SensorHumedad other = (SensorHumedad) obj;
		return Objects.equals(humedad, other.humedad) && Objects.equals(id, other.id)
				&& Objects.equals(nPlaca, other.nPlaca) && Objects.equals(temperatura, other.temperatura)
				&& Objects.equals(timestamp, other.timestamp);
	}

	public Integer getnPlaca() {
		return nPlaca;
	}

	public void setnPlaca(Integer nPlaca) {
		this.nPlaca = nPlaca;
	}
	
	
}
