package org.example;

import java.io.*;
import java.net.*;
import java.util.List;

public class Servidor {
    private static final int PUERTO = 12345;
    private DatabaseManager dbManager;

    public Servidor() {
        dbManager = new DatabaseManager();
    }

    public void iniciar() {
        System.out.println("Servidor iniciado. Escuchando en el puerto " + PUERTO);
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            while (true) {
                Socket clienteSocket = serverSocket.accept();
                System.out.println("1. Nuevo cliente conectado: " + clienteSocket.getInetAddress().getHostAddress());
                new Thread(() -> manejarCliente(clienteSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor principal: " + e.getMessage());
        }
    }

    private void manejarCliente(Socket clienteSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clienteSocket.getOutputStream(), true)) {

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String[] partes = inputLine.split("\\|", 2);

                if (partes.length < 2) continue;

                String comando = partes[0];
                String datosEncriptados = partes[1];
                String datosDesencriptados = Encriptador.desencriptar(datosEncriptados);

                if (comando.equals("GUARDAR")) {

                    String[] datos = datosDesencriptados.split(",");
                    if (datos.length == 3) {
                        dbManager.guardarDato(Integer.parseInt(datos[0]), Integer.parseInt(datos[1]), Integer.parseInt(datos[2]));
                        System.out.println("2. Cliente envió información para guardar. Datos: " + datosDesencriptados);
                    }

                } else if (comando.equals("CONSULTAR")) {
                    System.out.println("3. Cliente envió una petición de consulta de datos.");


                    String[] filtros = datosDesencriptados.split("\\|", 2);
                    String fechaFiltro = filtros[0];
                    String horaFiltro = filtros.length > 1 ? filtros[1] : "00:00";

                    List<DatoSensor> datos;

                    if (fechaFiltro.equals("GENERAL")) {
                        System.out.println("   -> CONSULTA: Solicitando historial COMPLETO.");
                        datos = dbManager.consultarTodo();
                    } else {
                        System.out.println("   -> CONSULTA: Filtrando por Fecha: " + fechaFiltro + ", Hora: " + horaFiltro);
                        datos = dbManager.consultarFiltrados(fechaFiltro, horaFiltro);
                    }


                    StringBuilder sb = new StringBuilder();
                    for (DatoSensor d : datos) {
                        sb.append(d.id).append(",").append(d.x).append(",").append(d.y).append(",").append(d.z)
                                .append(",").append(d.fecha).append(",").append(d.hora).append(";");
                    }

                    String respuesta = sb.toString();
                    String respuestaEncriptada = Encriptador.encriptar(respuesta);


                    out.println(respuestaEncriptada);
                    System.out.println("4. Se envió la información al cliente. Registros: " + datos.size());
                }
            }
        } catch (IOException e) {
            System.err.println("Error de comunicación con el cliente: " + e.getMessage());
        } finally {
            try {
                clienteSocket.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar socket: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        new Servidor().iniciar();
    }
}