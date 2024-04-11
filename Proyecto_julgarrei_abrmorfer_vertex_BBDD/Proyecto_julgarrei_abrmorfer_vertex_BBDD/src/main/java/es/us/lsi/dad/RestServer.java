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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;


public class RestServer extends AbstractVerticle {


	MySQLPool mySqlClient;
	private Gson gson;


	public void start(Promise<Void> startFuture) {
		// Creating some synthetic data
		MySQLConnectOptions connectOptions = new MySQLConnectOptions().setPort(3306).setHost("localhost")
				.setDatabase("dad").setUser("root").setPassword("rootroot");

		PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

		mySqlClient = MySQLPool.pool(vertx, connectOptions, poolOptions);
		
		// Defining the router object
		Router router = Router.router(vertx);
        // Creating some synthetic data
        createSomeData(25);

        // Instantiating a Gson serialize object using specific date format
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        
        vertx.createHttpServer().requestHandler(router::handle).listen(8081, result -> {
            if (result.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(result.cause());
            }
        });

        router.route("/api/sensor*").handler(BodyHandler.create());
        router.get("/api/sensor/all").handler(this::getAllSensors);
        router.get("/api/sensor/:sensorid").handler(this::getSensor);
        router.post("/api/sensor").handler(this::addSensor);
        router.delete("/api/sensor/:id").handler(this::deleteSensor);
        router.put("/api/sensor/getallsensorConnect").handler(this::getAllWithConnectionSen);
        
        router.route("/api/actuador*").handler(BodyHandler.create());
        router.get("/api/actuador/all").handler(this::getAllActuadores);
        router.get("/api/actuador/:actuadorid").handler(this::getActuador);
        router.post("/api/actuador").handler(this::addActuador);
        router.delete("/api/actuador/:id").handler(this::deleteActuador);
        router.put("/api/actuador/getallactConnect").handler(this::getAllWithConnectionAct);
        
		// Handling any server startup result

	}

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        try {          
//        	sensors.clear();
            stopPromise.complete();
        } catch (Exception e) {
            stopPromise.fail(e);
        }
        super.stop(stopPromise);
    }




		 // Sensor Endpoints
	private void getAllSensors(RoutingContext routingContext) {
		//RoutingContext routingContext PARAMETRO DE LA FUNCION
//		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
//				.end(gson.toJson(new UserEntityListWrapper(users.values())));
			mySqlClient.query("SELECT * FROM dad.sensor;").execute(res -> {
				if (res.succeeded()) {
					// Get the result set
					RowSet<Row> resultSet = res.result();
					System.out.println(resultSet.size());
					JsonArray result = new JsonArray();
					for (Row elem : resultSet) {
						result.add(JsonObject.mapFrom(new SensorHumedad(elem.getInteger("id"), elem.getInteger("nPlaca"),
								elem.getLong("timestamp"), elem.getFloat("humedad"), elem.getFloat("temperatura"))));
					}
					System.out.println(result.toString());
				} else {
					System.out.println("Error: " + res.cause().getLocalizedMessage());
				}
			});
	}
		private void getAllWithConnectionSen(RoutingContext routingContext) {
			mySqlClient.getConnection(connection -> {
				if (connection.succeeded()) {
					connection.result().query("SELECT * FROM dad.sensor;").execute(res -> {
						if (res.succeeded()) {
							// Get the result set
							RowSet<Row> resultSet = res.result();
							System.out.println(resultSet.size());
							JsonArray result = new JsonArray();
							for (Row elem : resultSet) {
								result.add(JsonObject.mapFrom(new SensorHumedad(elem.getInteger("id"), elem.getInteger("nPlaca"),
										elem.getLong("timestamp"), elem.getFloat("humedad"), elem.getFloat("temperatura"))));
							}
							System.out.println(result.toString());
						} else {
							System.out.println("Error: " + res.cause().getLocalizedMessage());
						}
						connection.result().close();
					});
				} else {
					System.out.println(connection.cause().toString());
				}
			});
		}
		
		private void getByIDSen(Integer ID) {
			mySqlClient.getConnection(connection -> {
				if (connection.succeeded()) {
					connection.result().preparedQuery("SELECT * FROM dad.sensor WHERE ID = ?").execute(
							Tuple.of(ID), res -> {
								if (res.succeeded()) {
									// Get the result set
									RowSet<Row> resultSet = res.result();
									System.out.println(resultSet.size());
									JsonArray result = new JsonArray();
									for (Row elem : resultSet) {
										result.add(JsonObject.mapFrom(new SensorHumedad(elem.getInteger("id"), elem.getInteger("nPlaca"),
												elem.getLong("timestamp"), elem.getFloat("humedad"), elem.getFloat("temperatura"))));
									}
									System.out.println(result.toString());
								} else {
									System.out.println("Error: " + res.cause().getLocalizedMessage());
								}
								connection.result().close();
							});
				} else {
					System.out.println(connection.cause().toString());
				}
			});
		}

		private Date localDateToDateSen(LocalDate localDate) {
			return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		}
		
		
		//ADD=PUSH
		//get=SELECT
		/////////////////////////////////////////////////////////
		
		    private void getSensor(RoutingContext routingContext) {
		        int id = 0;
		        try {
		            id = Integer.parseInt(routingContext.request().getParam("sensorid"));

		            if (sensors.containsKey(id)) {
		                SensorHumedad sensor = sensors.get(id);
		                routingContext.response()
		                        .putHeader("content-type", "application/json; charset=utf-8")
		                        .setStatusCode(200)
		                        .end(sensor != null ? gson.toJson(sensor) : "");
		            } else {
		                routingContext.response()
		                        .putHeader("content-type", "application/json; charset=utf-8")
		                        .setStatusCode(204)
		                        .end();
		            }
		        } catch (Exception e) {
		            routingContext.response()
		                    .putHeader("content-type", "application/json; charset=utf-8")
		                    .setStatusCode(204)
		                    .end();
		        }
		    }

		    
		    
		    private void addSensor(RoutingContext routingContext) {
		        final SensorHumedad sensor = gson.fromJson(routingContext.getBodyAsString(), SensorHumedad.class);
		        sensors.put(sensor.getId(), sensor);
		        routingContext.response()
		                .setStatusCode(201)
		                .putHeader("content-type", "application/json; charset=utf-8")
		                .end(gson.toJson(sensor));
		    }

		    private void deleteSensor(RoutingContext routingContext) {
		        int id = Integer.parseInt(routingContext.request().getParam("id"));
		        if (sensors.containsKey(id)) {
		        	SensorHumedad sensor = sensors.get(id);
		            sensors.remove(id);
		            routingContext.response()
		                    .setStatusCode(200)
		                    .putHeader("content-type", "application/json; charset=utf-8")
		                    .end(gson.toJson(sensor));
		        } else {
		            routingContext.response()
		                    .setStatusCode(204)
		                    .putHeader("content-type", "application/json; charset=utf-8")
		                    .end();
		        }
		    }

		    private void updateSensor(RoutingContext routingContext) {
		        int id = Integer.parseInt(routingContext.request().getParam("id"));
		        SensorHumedad sensor = sensors.get(id);
		        final SensorHumedad updatedSensor = gson.fromJson(routingContext.getBodyAsString(), SensorHumedad.class);
		        sensor.setTimestamp(updatedSensor.getTimestamp());
		        sensor.setHumedad(updatedSensor.getHumedad());
		        sensor.setTemperatura(updatedSensor.getTemperatura());
		        sensors.put(sensor.getId(), sensor);
		        routingContext.response()
		                .setStatusCode(200)
		                .putHeader("content-type", "application/json; charset=utf-8")
		                .end(gson.toJson(sensor));
		    }


		 // Actuador Endpoints
			private void getAllActuadores(RoutingContext routingContext) {
				//RoutingContext routingContext PARAMETRO DE LA FUNCION
//				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
//						.end(gson.toJson(new UserEntityListWrapper(users.values())));
					mySqlClient.query("SELECT * FROM dad.actuador;").execute(res -> {
						if (res.succeeded()) {
							// Get the result set
							RowSet<Row> resultSet = res.result();
							System.out.println(resultSet.size());
							JsonArray result = new JsonArray();
							for (Row elem : resultSet) {
								result.add(JsonObject.mapFrom(new Actuador_rele(elem.getInteger("ID"), elem.getInteger("nPlaca"),
										elem.getLong("timestamp"), elem.getBoolean("encendido"))));
							}
							System.out.println(result.toString());
						} else {
							System.out.println("Error: " + res.cause().getLocalizedMessage());
						}
					});
			}
				private void getAllWithConnectionAct(RoutingContext routingContext) {
					mySqlClient.getConnection(connection -> {
						if (connection.succeeded()) {
							connection.result().query("SELECT * FROM dad.actuador;").execute(res -> {
								if (res.succeeded()) {
									// Get the result set
									RowSet<Row> resultSet = res.result();
									System.out.println(resultSet.size());
									JsonArray result = new JsonArray();
									for (Row elem : resultSet) {
										result.add(JsonObject.mapFrom(new Actuador_rele(elem.getInteger("ID"), elem.getInteger("nPlaca"),
												elem.getLong("timestamp"), elem.getBoolean("encendido"))));
									}
									System.out.println(result.toString());
								} else {
									System.out.println("Error: " + res.cause().getLocalizedMessage());
								}
								connection.result().close();
							});
						} else {
							System.out.println(connection.cause().toString());
						}
					});
				}
				
				private void getByIDAct(Integer ID) {
					mySqlClient.getConnection(connection -> {
						if (connection.succeeded()) {
							connection.result().preparedQuery("SELECT * FROM dad.actuador WHERE ID = ?").execute(
									Tuple.of(ID), res -> {
										if (res.succeeded()) {
											// Get the result set
											RowSet<Row> resultSet = res.result();
											System.out.println(resultSet.size());
											JsonArray result = new JsonArray();
											for (Row elem : resultSet) {
												result.add(JsonObject.mapFrom(new Actuador_rele(elem.getInteger("ID"), elem.getInteger("nPlaca"),
														elem.getLong("timestamp"), elem.getBoolean("encendido"))));
											}
											System.out.println(result.toString());
										} else {
											System.out.println("Error: " + res.cause().getLocalizedMessage());
										}
										connection.result().close();
									});
						} else {
							System.out.println(connection.cause().toString());
						}
					});
				}

				private Date localDateToDate(LocalDate localDate) {
					return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
				}

				//ADD=PUSH
				//get=SELECT
				/////////////////////////////////////////////////////////

		    private void getActuador(RoutingContext routingContext) {
		        int id = 0;
		        try {
		            id = Integer.parseInt(routingContext.request().getParam("actuadorid"));

		            if (actuadores.containsKey(id)) {
		                Actuador_rele actuador = actuadores.get(id);
		                routingContext.response()
		                        .putHeader("content-type", "application/json; charset=utf-8")
		                        .setStatusCode(200)
		                        .end(actuador != null ? gson.toJson(actuador) : "");
		            } else {
		                routingContext.response()
		                        .putHeader("content-type", "application/json; charset=utf-8")
		                        .setStatusCode(204)
		                        .end();
		            }
		        } catch (Exception e) {
		            routingContext.response()
		                    .putHeader("content-type", "application/json; charset=utf-8")
		                    .setStatusCode(204)
		                    .end();
		        }
		    }

		    private void addActuador(RoutingContext routingContext) {
		        final Actuador_rele actuador = gson.fromJson(routingContext.getBodyAsString(), Actuador_rele.class);
		        actuadores.put(actuador.getidActuador(), actuador);
		        routingContext.response()
		                .setStatusCode(201)
		                .putHeader("content-type", "application/json; charset=utf-8")
		                .end(gson.toJson(actuador));
		    }

		    private void deleteActuador(RoutingContext routingContext) {
		        int id = Integer.parseInt(routingContext.request().getParam("id"));
		        if (actuadores.containsKey(id)) {
		            Actuador_rele actuador = actuadores.get(id);
		            actuadores.remove(id);
		            routingContext.response()
		                    .setStatusCode(200)
		                    .putHeader("content-type", "application/json; charset=utf-8")
		                    .end(gson.toJson(actuador));
		        } else {
		            routingContext.response()
		                    .setStatusCode(204)
		                    .putHeader("content-type", "application/json; charset=utf-8")
		                    .end();
		        }
		    }

		    private void updateActuador(RoutingContext routingContext) {
		        int id = Integer.parseInt(routingContext.request().getParam("id"));
		        Actuador_rele actuador = actuadores.get(id);
		        final Actuador_rele updatedActuador = gson.fromJson(routingContext.getBodyAsString(), Actuador_rele.class);
		        actuador.setTimestamp(updatedActuador.getTimestamp());
		        actuador.setActivo(updatedActuador.getActivo());
		        actuador.setEncendido(updatedActuador.getEncendido());
		        actuadores.put(actuador.getidActuador(), actuador);
		        routingContext.response()
		                .setStatusCode(200)
		                .putHeader("content-type", "application/json; charset=utf-8")
		                .end(gson.toJson(actuador));
		    }


		    private void createSomeData(int number) {
		        Random rnd = new Random();
		        IntStream.range(0, number).forEach(elem -> {
		            int id = elem + 1; // Ajustamos el ID para que comience en 1 en lugar de 0
		            int nPlaca=rnd.nextInt();
		            long timestamp = Calendar.getInstance().getTimeInMillis() + rnd.nextInt(1000); // Agregamos un número aleatorio al timestamp
		            float humedad = rnd.nextFloat() * 100; // Generamos un valor aleatorio entre 0 y 100 para la humedad
		            float temperatura = rnd.nextFloat() * 50; // Generamos un valor aleatorio entre 0 y 50 para la temperatura
		            sensors.put(id, new SensorHumedad(id, nPlaca, timestamp, humedad, temperatura));
		        });
		    }
}