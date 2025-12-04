package org.example;


public interface DataSource {

    interface DatosListener {
        void datosRecibidos(int x, int y, int z);
    }

    boolean iniciar();
    void detener();
}