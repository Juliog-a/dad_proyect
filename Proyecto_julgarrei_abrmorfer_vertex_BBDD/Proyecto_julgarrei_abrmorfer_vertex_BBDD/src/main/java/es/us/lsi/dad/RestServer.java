package es.us.lsi.dad;

import java.util.ArrayList;
import java.util.Calendar;
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
import java.util.List;

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
				.setDatabase("dad").setUser("root").setPassword("");

		PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

		mySqlClient = MySQLPool.pool(vertx, connectOptions, poolOptions);
		
		// Defining the router object
		Router router = Router.router(vertx);
        // Creating some synthetic data
        //createSomeData(25);

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
      router.get("/api/sensor").handler(this::getAllWithConnectionSen);
      router.get("/api/sensor/all").handler(this::getAllSensors);
      router.get("/api/sensor/:id").handler(this::getByIDSen);
      router.post("/api/sensor").handler(this::addSensor);
      router.delete("/api/sensor/:id").handler(this::deleteSensor);
      router.put("/api/sensor/:id").handler(this::updateSensor);
      
      router.route("/api/actuador*").handler(BodyHandler.create());
      router.get("/api/actuador").handler(this::getAllWithConnectionAct);
      router.get("/api/actuador/all").handler(this::getAllActuadores);
      router.get("/api/actuador/:id").handler(this::getByIDAct);
      router.post("/api/actuador").handler(this::addActuador);
      router.delete("/api/actuador/:id").handler(this::deleteActuador);
      router.put("/api/actuador/:id").handler(this::updateActuador);
        
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
        mySqlClient.query("SELECT * FROM dad.sensor;").execute(res -> {
            if (res.succeeded()) {
                // Get the result set
                RowSet<Row> resultSet = res.result();
                List<List<Object>> result = new ArrayList<>();
                for (Row elem : resultSet) {
                    List<Object> sensorData = new ArrayList<>();
                    sensorData.add(elem.getInteger("ID"));
                    sensorData.add(elem.getInteger("nPlaca"));
                    sensorData.add(elem.getFloat("humedad"));
                    sensorData.add(elem.getLong("timestamp"));
                    sensorData.add(elem.getFloat("temperatura"));
                    result.add(sensorData);
                }
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(200)
                        .end(result.toString());
            } else {
                routingContext.response()
                        .setStatusCode(500)
                        .end("Error al obtener los sensores: " + res.cause().getMessage());
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
							List<SensorHumedad> result = new ArrayList<>();
							for (Row elem : resultSet) {
								result.add(new SensorHumedad(elem.getInteger("ID"), elem.getInteger("nPlaca"), elem.getFloat("humedad"),
										elem.getLong("timestamp"), elem.getFloat("temperatura")));
							}
			                routingContext.response()
	                        .putHeader("content-type", "application/json; charset=utf-8")
	                        .setStatusCode(200)
	                        .end(result.toString());
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
		
		private void getByIDSen(RoutingContext routingContext) {
			mySqlClient.getConnection(connection -> {
				int ID = Integer.parseInt(routingContext.request().getParam("ID"));
				if (connection.succeeded()) {
					connection.result().preparedQuery("SELECT * FROM dad.sensor WHERE ID = ?").execute(
							Tuple.of(ID), res -> {
								if (res.succeeded()) {
									// Get the result set
									RowSet<Row> resultSet = res.result();
									System.out.println(resultSet.size());
									List<SensorHumedad> result = new ArrayList<>();
									for (Row elem : resultSet) {
										result.add(new SensorHumedad(elem.getInteger("ID"), elem.getInteger("nPlaca"), elem.getFloat("humedad"),
												elem.getLong("timestamp"), elem.getFloat("temperatura")));
									}
					                routingContext.response()
			                        .putHeader("content-type", "application/json; charset=utf-8")
			                        .setStatusCode(200)
			                        .end(result.toString());
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

//		private Date localDateToDateSen(LocalDate localDate) {
//			return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//		}
//		
	    private void addSensor(RoutingContext routingContext) {
	    	
	        // Parseamos el cuerpo de la solicitud HTTP a un objeto Sensor_humedad_Entity
	        final SensorHumedad sensor = gson.fromJson(routingContext.getBodyAsString(), SensorHumedad.class);
	        
	        // Ejecutamos la inserción en la base de datos MySQL
	        mySqlClient.preparedQuery("INSERT INTO sensor (ID, nPlaca, timestamp, humedad, temperatura) VALUES (?, ?, ?, ?, ?)").execute(
	        		(Tuple.of(sensor.getId(), sensor.getnPlaca(), sensor.getHumedad(), sensor.getTimestamp(), sensor.getTemperatura())), res -> {
	                if (res.succeeded()) {
	                    // Si la inserción es exitosa, respondemos con el sensor creado
	                    routingContext.response()
	                        .setStatusCode(201)
	                        .putHeader("content-type", "application/json; charset=utf-8");
	                } else {
	                    // Si hay un error en la inserción, respondemos con el mensaje de error
	                	System.out.println("Error: " + res.cause().getLocalizedMessage());
	                	}
	            });
	    }
	    
	    private void deleteSensor(RoutingContext routingContext) {
	        // Obtenemos el ID del sensor de los parámetros de la solicitud HTTP
	        int ID = Integer.parseInt(routingContext.request().getParam("ID"));
	        
	        // Ejecutamos la eliminación en la base de datos MySQL
	        mySqlClient.preparedQuery("DELETE FROM sensor WHERE ID = ?").execute((Tuple.of(ID)), res -> {
	                if (res.succeeded()) {
	                    // Si la eliminación es exitosa, respondemos con el sensor eliminado
	                    if (res.result().rowCount() > 0) {
	                        routingContext.response()
	                            .setStatusCode(200)
	                            .putHeader("content-type", "application/json; charset=utf-8")
	                            .end(gson.toJson(new JsonObject().put("message", "Sensor eliminado correctamente")));
	                    } 
	                } else {
	                    // Si hay un error en la eliminación, respondemos con el código 500 (Error interno del servidor)
	    				System.out.println("Error: " + res.cause().getLocalizedMessage());
	                }
	            });
	    }
		//ADD=PUSH
		//get=SELECT
		/////////////////////////////////////////////////////////
	    private void updateSensor(RoutingContext routingContext) {
	        // Obtenemos el ID del sensor de los parámetros de la solicitud HTTP
	        int id = Integer.parseInt(routingContext.request().getParam("id"));
	        
	        // Obtenemos el sensor actualizado del cuerpo de la solicitud HTTP
	        final SensorHumedad updatedSensor = gson.fromJson(routingContext.getBodyAsString(), SensorHumedad.class);
	        
	        // Ejecutamos la actualización en la base de datos MySQL
	        mySqlClient.preparedQuery("UPDATE sensor SET humedad = ?, timestamp = ?, temperatura = ? WHERE ID = ?").execute( 
	        		(Tuple.of(updatedSensor.getTimestamp(), updatedSensor.getHumedad(), updatedSensor.getTemperatura(), id)), res -> {
	                if (res.succeeded()) {
	                    // Si la actualización es exitosa, respondemos con el sensor actualizado
	                    if (res.result().rowCount() > 0) {
	                        routingContext.response()
	                            .setStatusCode(200)
	                            .putHeader("content-type", "application/json; charset=utf-8")
	                            .end(gson.toJson(updatedSensor));
	                    } 
	                } else {
	                    // Si hay un error en la actualización, respondemos con el código 500 (Error interno del servidor)
	    				System.out.println("Error: " + res.cause().getLocalizedMessage());
	                }
	            });
	    }
	    

	    private void createSomeData(int number) {
	        Random rnd = new Random();
	        IntStream.range(0, number).forEach(elem -> {
	            int id = elem + 1; // Ajustamos el ID para que comience en 1 en lugar de 0
	            int nPlaca = rnd.nextInt();
	            long timestamp = Calendar.getInstance().getTimeInMillis() + rnd.nextInt(1000); // Agregamos un número aleatorio al timestamp
	            float humedad = rnd.nextFloat() * 100; // Generamos un valor aleatorio entre 0 y 100 para la humedad
	            float temperatura = rnd.nextFloat() * 50; // Generamos un valor aleatorio entre 0 y 50 para la temperatura
	            
	            // Ejecutamos la inserción en la base de datos MySQL
	            mySqlClient.preparedQuery("INSERT INTO sensor (ID, nPlaca, humedad, timestamp, temperatura) VALUES (?, ?, ?, ?, ?)").execute(
	            		(Tuple.of(id, nPlaca, timestamp, humedad, temperatura)), ar -> {
	                    if (ar.succeeded()) {
	                        System.out.println("Se ha insertado un nuevo sensor en la base de datos.");
	                    } else {
	                        System.err.println("Error " + ar.cause().getMessage());
	                    }
	                });
	        });
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
							List<Actuador_rele> result = new ArrayList<>();
							for (Row elem : resultSet) {
								result.add(new Actuador_rele(elem.getInteger("ID"), elem.getBoolean("encendido"), elem.getInteger("nPlaca"),
										elem.getLong("timestamp")));
							}
			                routingContext.response()
	                        .putHeader("content-type", "application/json; charset=utf-8")
	                        .setStatusCode(200)
	                        .end(result.toString());
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
									List<Actuador_rele> result = new ArrayList<>();
									for (Row elem : resultSet) {
										result.add(new Actuador_rele(elem.getInteger("ID"), elem.getBoolean("encendido"), elem.getInteger("nPlaca"),
												elem.getLong("timestamp")));
									}
					                routingContext.response()
			                        .putHeader("content-type", "application/json; charset=utf-8")
			                        .setStatusCode(200)
			                        .end(result.toString());								} else {
									System.out.println("Error: " + res.cause().getLocalizedMessage());
								}
								connection.result().close();
							});
						} else {
							System.out.println(connection.cause().toString());
						}
					});
				}
				
				private void getByIDAct(RoutingContext routingContext) {
					mySqlClient.getConnection(connection -> {
			            int ID = Integer.parseInt(routingContext.request().getParam("ID"));
						if (connection.succeeded()) {
							connection.result().preparedQuery("SELECT * FROM dad.actuador WHERE ID = ?").execute(
									Tuple.of(ID), res -> {
										if (res.succeeded()) {
											// Get the result set
											RowSet<Row> resultSet = res.result();
											System.out.println(resultSet.size());
											List<Actuador_rele> result = new ArrayList<>();
											for (Row elem : resultSet) {
												result.add(new Actuador_rele(elem.getInteger("ID"), elem.getBoolean("encendido"), elem.getInteger("nPlaca"),
														elem.getLong("timestamp")));
											}
							                routingContext.response()
					                        .putHeader("content-type", "application/json; charset=utf-8")
					                        .setStatusCode(200)
					                        .end(result.toString());										} else {
											System.out.println("Error: " + res.cause().getLocalizedMessage());
										}
										connection.result().close();
									});
						} else {
							System.out.println(connection.cause().toString());
						}
					});
				}

//				private Date localDateToDate(LocalDate localDate) {
//					return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//				}

				//ADD=PUSH
				//get=SELECT
				/////////////////////////////////////////////////////////
					
				    
				    private void addActuador(RoutingContext routingContext) {
				    	
				        // Parseamos el cuerpo de la solicitud HTTP a un objeto Sensor_humedad_Entity
				        final Actuador_rele actuador = gson.fromJson(routingContext.getBodyAsString(), Actuador_rele.class);
				        
				        // Ejecutamos la inserción en la base de datos MySQL
				        mySqlClient.preparedQuery("INSERT INTO actuador (ID, encendido, timestamp, nPlaca) VALUES (?, ?, ?, ?)").execute(
				        		(Tuple.of(actuador.getIdDevise(), actuador.getidActuador(), actuador.getTimestamp(), actuador.getActivo(), actuador.getEncendido())), res -> {
				                if (res.succeeded()) {
				                    // Si la inserción es exitosa, respondemos con el sensor creado
				                    routingContext.response()
				                        .setStatusCode(201)
				                        .putHeader("content-type", "application/json; charset=utf-8");
				                } else {
				                    // Si hay un error en la inserción, respondemos con el mensaje de error
				                	System.out.println("Error: " + res.cause().getLocalizedMessage());
				                	}
				            });
				    }

				    
				    private void deleteActuador(RoutingContext routingContext) {
				        // Obtenemos el ID del sensor de los parámetros de la solicitud HTTP
				        int id = Integer.parseInt(routingContext.request().getParam("id"));
				        
				        // Ejecutamos la eliminación en la base de datos MySQL
				        mySqlClient.preparedQuery("DELETE FROM actuador WHERE ID = ?").execute( (Tuple.of(id)), res -> {
				                if (res.succeeded()) {
				                    // Si la eliminación es exitosa, respondemos con el sensor eliminado
				                    if (res.result().rowCount() > 0) {
				                        routingContext.response()
				                            .setStatusCode(200)
				                            .putHeader("content-type", "application/json; charset=utf-8")
				                            .end(gson.toJson(new JsonObject().put("message", "Sensor eliminado correctamente")));
				                    } 
				                } else {
				                    // Si hay un error en la eliminación, respondemos con el código 500 (Error interno del servidor)
				    				System.out.println("Error: " + res.cause().getLocalizedMessage());
				                }
				            });
				    }

				    
				    private void updateActuador(RoutingContext routingContext) {
				        // Obtenemos el ID del sensor de los parámetros de la solicitud HTTP
				        int idActuador = Integer.parseInt(routingContext.request().getParam("ID"));
				        
				        // Obtenemos el sensor actualizado del cuerpo de la solicitud HTTP
				        final Actuador_rele updatedActuador = gson.fromJson(routingContext.getBodyAsString(), Actuador_rele.class);
				        
				        // Ejecutamos la actualización en la base de datos MySQL
				        mySqlClient.preparedQuery("UPDATE actuador SET timestamp = ?, encendido = ? WHERE ID = ?").execute(
				        		(Tuple.of(updatedActuador.getTimestamp(), updatedActuador.getActivo(), updatedActuador.getEncendido(), idActuador)), res -> {
				                if (res.succeeded()) {
				                    // Si la actualización es exitosa, respondemos con el sensor actualizado
				                    if (res.result().rowCount() > 0) {
				                        routingContext.response()
				                            .setStatusCode(200)
				                            .putHeader("content-type", "application/json; charset=utf-8")
				                            .end(gson.toJson(updatedActuador));
				                    } 
				                } else {
				                    // Si hay un error en la actualización, respondemos con el código 500 (Error interno del servidor)
				    				System.out.println("Error: " + res.cause().getLocalizedMessage());
				                }
				            });
				    }

			   
				    // crear un sensor

			  
			    

			}