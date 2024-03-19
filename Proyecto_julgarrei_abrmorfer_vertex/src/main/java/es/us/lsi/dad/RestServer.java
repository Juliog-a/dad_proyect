package es.us.lsi.dad;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class RestServer extends AbstractVerticle {


    private Map<Integer, SensorHumedad> sensors = new HashMap<>();
    private Gson gson;

    public void start(Promise<Void> startFuture) {
        // Creating some synthetic data
        createSomeData(25);

        // Instantiating a Gson serialize object using specific date format
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();


        Router router = Router.router(vertx);

  
        vertx.createHttpServer().requestHandler(router::handle).listen(8084, result -> {
            if (result.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(result.cause());
            }
        });


		// Defining URI paths for each method in RESTful interface, including body
		// handling by /api/users* or /api/users/*
        router.route("/api/sensor*").handler(BodyHandler.create());
        router.get("/api/sensor").handler(this::getAllWithParams);
        router.get("/api/sensor/all").handler(this::getAll);
        router.get("/api/sensor/:sensorid").handler(this::getOne);
        router.post("/api/sensor").handler(this::addOne);
        router.delete("/api/sensor/:id").handler(this::deleteOne);
        router.put("/api/sensor/:id").handler(this::putOne);
    }


	@Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        try {
            sensors.clear();
            stopPromise.complete();
        } catch (Exception e) {
            stopPromise.fail(e);
        }
        super.stop(stopPromise);
    }
	
	private void getAll(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(new SensorEntityListWrapper(sensors.values())));
	}

	private void getAllWithParams(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
        .end(gson.toJson(new SensorEntityListWrapper(sensors.values())));

	}

	   private void getOne(RoutingContext routingContext) {
	        int id = 0;
	        try {
	            id = Integer.parseInt(routingContext.request().getParam("sensorid"));

	            if (sensors.containsKey(id)) {
	            	SensorHumedad sensor = sensors.get(id);
	                routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
	                        .setStatusCode(200).end(sensor != null ? gson.toJson(sensor) : "");
	            } else {
	                routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
	                        .setStatusCode(204).end();
	            }
	        } catch (Exception e) {
	            routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(204)
	                    .end();
	        }
	    }


    private void addOne(RoutingContext routingContext) {
        final SensorHumedad sensor = gson.fromJson(routingContext.getBodyAsString(), SensorHumedad.class);
        sensors.put(sensor.getId(), sensor);
        routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
                .end(gson.toJson(sensor));
    }


    private void deleteOne(RoutingContext routingContext) {
        int id = Integer.parseInt(routingContext.request().getParam("id"));
        if (sensors.containsKey(id)) {
        	SensorHumedad sensor = sensors.get(id);
            sensors.remove(id);
            routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
                    .end(gson.toJson(sensor));
        } else {
            routingContext.response().setStatusCode(204).putHeader("content-type", "application/json; charset=utf-8")
                    .end();
        }
    }


    private void putOne(RoutingContext routingContext) {
        int id = Integer.parseInt(routingContext.request().getParam("id"));
        SensorHumedad sensor = sensors.get(id);
        final SensorHumedad updatedSensor = gson.fromJson(routingContext.getBodyAsString(), SensorHumedad.class);
        sensor.setTimestamp(updatedSensor.getTimestamp());
        sensor.setHumedad(updatedSensor.getHumedad());
        sensor.setTemperatura(updatedSensor.getTemperatura());
        sensors.put(sensor.getId(), sensor);
        routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
                .end(gson.toJson(sensor));
    }


	   private void createSomeData(int number) {
	        Random rnd = new Random();
	        IntStream.range(0, number).forEach(elem -> {
	            int id = elem + 1; // Ajustamos el ID para que comience en 1 en lugar de 0
	        	Integer nPlaca = elem + 1;
	            long timestamp = Calendar.getInstance().getTimeInMillis() + rnd.nextInt(1000); // Agregamos un número aleatorio al timestamp
	            float humedad = rnd.nextFloat() * 100; // Generamos un valor aleatorio entre 0 y 100 para la humedad
	            float temperatura = rnd.nextFloat() * 50; // Generamos un valor aleatorio entre 0 y 50 para la temperatura
	            sensors.put(id, new SensorHumedad(id, nPlaca, timestamp,humedad, temperatura));
	        });
	    }

}
