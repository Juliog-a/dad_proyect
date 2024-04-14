package es.us.lsi.dad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ActuadorEntityListWrapper {
	
	private List<Actuador_rele> actuadorList;
	
	public ActuadorEntityListWrapper() {
		super();
	}
	
	public ActuadorEntityListWrapper(Collection<Actuador_rele> actuadorList) {
		super();
		this.actuadorList = new ArrayList<>(actuadorList);
	}
	
	public ActuadorEntityListWrapper(List<Actuador_rele> actuadorList) {
		super();
		this.actuadorList = new ArrayList<>(actuadorList);
	}
	
	public List<Actuador_rele> getActuadorList() {
		return actuadorList;
	}
	
	public void setActuadorList(List<Actuador_rele> actuadorList) {
		this.actuadorList = actuadorList;
	}

	@Override
	public int hashCode() {
		return Objects.hash(actuadorList);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActuadorEntityListWrapper other = (ActuadorEntityListWrapper) obj;
		return Objects.equals(actuadorList, other.actuadorList);
	}

	@Override
	public String toString() {
		return "ActuadorEntityListWrapper [actuadorList=" + actuadorList + "]";
	}
	
	

	    
}