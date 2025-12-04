package org.example;


import java.io.Serializable;

public class DatoSensor implements Serializable {
    private static final long serialVersionUID = 1L;

    public int id;
    public int x;
    public int y;
    public int z;
    public String fecha;
    public String hora;

    public DatoSensor(int id, int x, int y, int z, String fecha, String hora) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.fecha = fecha;
        this.hora = hora;
    }
    // ...
}