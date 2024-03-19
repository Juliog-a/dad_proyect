package es.us.lsi.dad;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ServletLoginHumedad extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//	private Map<Integer, Long> sensorPass;
	private List<SensorHumedad> sensor1;

	public void init() throws ServletException {
		sensor1 = new ArrayList();
		SensorHumedad prueba = new SensorHumedad();
		prueba.setId(0);
		prueba.setnPlaca(0);
		prueba.setTimestamp(123456L);
		prueba.setHumedad(13.0f);
		prueba.setTemperatura(18.0f);
		sensor1.add(prueba);
		
//		sensorPass = new HashMap<Integer, Long>();
//		sensorPass.put(0, 123456L);
		super.init();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Integer id = Integer.parseInt(req.getParameter("id"));
		Integer Placa = Integer.parseInt(req.getParameter("nPlaca"));
		
		//EN ALGO COMUN MENTE CAMBIABLE NO LO PONDRE EN EL GET, SOLO COGEREMOS 
		//EL SENSOR MEDIANTE ID Y YA. DE PLACA Y DEL SENSOR.
//		Long timestamp = Long.parseLong(req.getParameter("timestamp"));
//		Float humedad = Float.parseFloat(req.getParameter("Humedad"));
//		Float temperatura = Float.parseFloat(req.getParameter("Temperatura"));
		if (sensor1.stream().anyMatch(s->s.getId()==id && s.getnPlaca()==Placa)) {
			BufferedReader reader = req.getReader();
		    Gson gson = new Gson();
			SensorHumedad pepe = sensor1.stream().filter(s->s.getId()==id && s.getnPlaca()==Placa).findFirst().get();
			resp.getWriter().println(gson.toJson(pepe));
			response(resp, "login ok");
		} else {
			response(resp, "invalid login");
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	    BufferedReader reader = req.getReader();
	    Gson gson = new Gson();
		SensorHumedad sensor = gson.fromJson(reader, SensorHumedad.class);
		//aqui vemos si esta en el map nuestro nuevo sensor, si no debemos de añadir otro.
		if(!sensor1.stream().anyMatch(s->s.getId()==sensor.getId()&&s.getnPlaca()==sensor.getnPlaca())) {
			sensor1.add(sensor);
//			sensorPass.put(sensor.getId(), sensor.getTimestamp());
			resp.getWriter().println(gson.toJson(sensor));
			resp.setStatus(201);
		}else {
			resp.setStatus(300);
			response(resp, "Ya hay un sensor con ese id en esa placa");
		}
		
//		if ((sensor.getTimestamp() == 123456L) && sensor.getId().equals(0)) {
//			sensorPass.put(sensor.getId(), sensor.getTimestamp());
//			resp.getWriter().println(gson.toJson(sensor));
//			resp.setStatus(201);
//		}else{
//			resp.setStatus(300);
//			response(resp, "Wrong ID and Tiempo");
//		}
		
		
//		BufferedReader reader = req.getReader();
//	    Gson gson = new Gson();
//	    SensorHumedad sensor = null;
//	    
//	    try {
//	        sensor = gson.fromJson(reader, SensorHumedad.class);
//	        
//	        if (sensor != null && sensor.getId() != null && sensor.getTimestamp() != 0L) {
//	            if (sensor.getTimestamp() == 123456L && sensor.getId() == 0) {
//	                sensorPass.put(sensor.getId(), sensor.getTimestamp());
//	                resp.getWriter().println(gson.toJson(sensor));
//	                resp.setStatus(201);
//	            } else {
//	                resp.setStatus(300);
//	                response(resp, "Wrong ID and Tiempo");
//	            }
//	        } else {
//	            resp.setStatus(400); // Bad request
//	            response(resp, "Invalid sensor data");
//	        }
//	    } catch (JsonSyntaxException e) {
//	        resp.setStatus(400); // Bad request
//	        response(resp, "Invalid JSON format");
//	    }  
	}
	
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	    BufferedReader reader = req.getReader();
	    
	    Gson gson = new Gson();
		SensorHumedad sensor = gson.fromJson(reader, SensorHumedad.class);
		if (!(sensor.getTimestamp() == 0L) && !sensor.getId().equals("")&&sensor.getnPlaca().equals("")
				&& sensor1.contains(sensor)) {
			sensor1.remove(sensor);
			resp.getWriter().println(gson.toJson(sensor));
			resp.setStatus(201);
		}else{
			resp.setStatus(300);
			response(resp, "Wrong user and password");
		}
	   
	}

	private void response(HttpServletResponse resp, String msg) throws IOException {
		PrintWriter out = resp.getWriter();
		out.println("<html>");
		out.println("<body>");
		out.println("<t1>" + msg + "</t1>");
		out.println("</body>");
		out.println("</html>");
}

}
