package org.example;
import com.fazecast.jSerialComm.SerialPort;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SerialReader extends Thread implements DataSource {


    private DataSource.DatosListener listener;
    private volatile boolean corriendo = false;
    private SerialPort puertoSerial;
    private static final Pattern PATRON_DATOS = Pattern.compile("x:(\\d+),y:(\\d+),z:(\\d+)");


    public SerialReader(String nombrePuerto, DataSource.DatosListener listener) {
        this.listener = listener;
        this.puertoSerial = SerialPort.getCommPort(nombrePuerto);
    }

    public static String[] listarPuertos() {
        SerialPort[] puertos = SerialPort.getCommPorts();
        List<String> nombres = new ArrayList<>();
        for (SerialPort p : puertos) {
            nombres.add(p.getSystemPortName());
        }
        return nombres.toArray(new String[0]);
    }


    @Override
    public boolean iniciar() {
        if (puertoSerial.openPort()) {

            puertoSerial.setComPortParameters(9600, 8, 1, 0);
            puertoSerial.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
            corriendo = true;
            start();
            return true;
        } else {
            System.err.println("Error: No se pudo abrir el puerto " + puertoSerial.getSystemPortName());
            return false;
        }
    }

    @Override
    public void detener() {
        corriendo = false;
        if (puertoSerial != null && puertoSerial.isOpen()) {
            puertoSerial.closePort();
        }
    }

    @Override
    public void run() {
        try (InputStream inputStream = puertoSerial.getInputStream()) {

            byte[] buffer = new byte[4096];
            int len;
            StringBuilder dataBuffer = new StringBuilder();

            while (corriendo) {
                if (inputStream.available() > 0 && (len = inputStream.read(buffer)) > 0) {

                    dataBuffer.append(new String(buffer, 0, len));
                    int newlineIndex;
                    while ((newlineIndex = dataBuffer.indexOf("\n")) != -1) {
                        String linea = dataBuffer.substring(0, newlineIndex);
                        dataBuffer.delete(0, newlineIndex + 1); // Eliminar la línea procesada
                        procesarLinea(linea);
                    }
                }

                Thread.sleep(10);
            }
        } catch (Exception e) {
            if (corriendo) {
                System.err.println("Error de lectura serial: " + e.getMessage());
            }
        }
    }

    private void procesarLinea(String linea) {
        String trimmedLine = linea.trim();


        Matcher matcher = PATRON_DATOS.matcher(trimmedLine);

        if (matcher.find() && matcher.groupCount() == 3) {
            try {

                int x = Integer.parseInt(matcher.group(1));
                int y = Integer.parseInt(matcher.group(2));
                int z = Integer.parseInt(matcher.group(3));

                if (listener != null) {
                    listener.datosRecibidos(x, y, z);
                }
            } catch (NumberFormatException e) {
                System.err.println("Error de formato de número. Datos recibidos: " + trimmedLine);
            }
        }
    }
}