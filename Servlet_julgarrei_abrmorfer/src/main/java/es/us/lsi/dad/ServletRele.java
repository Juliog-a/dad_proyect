package es.us.lsi.dad;

import jakarta.servlet.http.HttpServlet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class ServletRele extends HttpServlet{
	
	private static final long serialVersionUID = 1L;

	private List<Actuador_rele> Actuador;

	public void init() throws ServletException {
			Actuador = new ArrayList<>();
			Actuador_rele prueba = new Actuador_rele();
			prueba.setnPlaca(0);
			prueba.setId(1);
			prueba.setTimestamp(123456);
			prueba.setEncendido(false);
			super.init();
		}

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			Integer id = Integer.parseInt(req.getParameter("id"));
			Gson gson = new Gson();
			if (Actuador.stream().anyMatch(act -> act.getId()==id)) {
				Optional<Actuador_rele> foundActuator = Actuador.stream()
		                .filter(Actuator -> Actuator.getId() == id)
		                .findFirst();
				resp.getWriter().println(gson.toJson(foundActuator.get()));
			} else {
				response(resp, "Actuator with ID:" + id + " doesnt exist");
			}
		}
		
		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		    BufferedReader reader = req.getReader();
		    
		    Gson gson = new Gson();
			Actuador_rele newActuator = gson.fromJson(reader, Actuador_rele.class);
			if (!Actuador.stream().anyMatch(act -> act.getId() == newActuator.getId())) {
				Actuador.add(newActuator);
				resp.getWriter().println(gson.toJson(newActuator));
				resp.setStatus(201);
			}else{
				resp.setStatus(300);
				response(resp, "ID:" + newActuator.getId() + " is already assigned");
			}
		}
		
		
		@Override
		protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		    BufferedReader reader = req.getReader();
		    
		    Gson gson = new Gson();
		    Actuador_rele targetActuator = gson.fromJson(reader, Actuador_rele.class);
			if (Actuador.stream().anyMatch(Actuator -> targetActuator.getId() == targetActuator.getId())) {
				Actuador.removeIf(act -> act.getId() == targetActuator.getId());
				resp.getWriter().println(gson.toJson(targetActuator));
				resp.setStatus(201);
			}else{
				resp.setStatus(300);
				response(resp, "Actuador with ID:" + targetActuator.getId() + " doesnt exist");
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