package org.example;


import java.util.Random;

public class SimuladorArduino extends Thread implements DataSource {

    private DataSource.DatosListener listener;
    private volatile boolean corriendo = false;
    private Random random = new Random();

    public SimuladorArduino(DataSource.DatosListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean iniciar() {
        corriendo = true;
        start();
        return true;
    }

    @Override
    public void detener() {
        corriendo = false;
    }

    @Override
    public void run() {
        while (corriendo) {
            try {

                int x = random.nextInt(101);
                int y = random.nextInt(101);
                int z = random.nextInt(101);

                if (listener != null) {
                    listener.datosRecibidos(x, y, z);
                }

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                corriendo = false;
            }
        }
    }
}