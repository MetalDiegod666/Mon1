package org.example;


import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteSocket {
    private static final String HOST = "localhost";
    private static final int PUERTO = 12345;


    public void enviarDatos(int x, int y, int z) {
        try (Socket socket = new Socket(HOST, PUERTO);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String datos = x + "," + y + "," + z;
            String datosEncriptados = Encriptador.encriptar(datos);

            out.println("GUARDAR|" + datosEncriptados);

        } catch (ConnectException e) {
            // Este error ocurre si el Servidor no está corriendo
            System.err.println("⚠️ ADVERTENCIA: Servidor no disponible. Los datos no se están guardando.");
        } catch (Exception e) {
            System.err.println("Error al enviar datos: " + e.getMessage());
        }
    }


    public List<DatoSensor> consultarDatos(String fechaFiltro, String horaFiltro) throws Exception {
        List<DatoSensor> datos = new ArrayList<>();

        try (Socket socket = new Socket(HOST, PUERTO);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String filtros = fechaFiltro + "|" + horaFiltro;
            out.println("CONSULTAR|" + Encriptador.encriptar(filtros));

            String lineaEncriptada = in.readLine();

            if (lineaEncriptada == null) {

                throw new Exception("El servidor no devolvió datos.");
            }

            String lineaDesencriptada = Encriptador.desencriptar(lineaEncriptada);


            if (lineaDesencriptada.isEmpty()) {
                return datos; // Devuelve lista vacía
            }


            String[] registros = lineaDesencriptada.split(";");
            for (String reg : registros) {
                if (reg.isEmpty()) continue;
                String[] campos = reg.split(",");
                if (campos.length == 6) {
                    datos.add(new DatoSensor(
                            Integer.parseInt(campos[0]),
                            Integer.parseInt(campos[1]),
                            Integer.parseInt(campos[2]),
                            Integer.parseInt(campos[3]),
                            campos[4],
                            campos[5]
                    ));
                }
            }


        } catch (ConnectException e) {
            throw new Exception("ERROR CRÍTICO: No se pudo conectar al Servidor para consultar el histórico. Inicie Servidor.java primero.", e);
        } catch (SocketTimeoutException e) {
            throw new Exception("ERROR: El servidor tardó demasiado en responder.", e);
        }
        return datos;
    }
}