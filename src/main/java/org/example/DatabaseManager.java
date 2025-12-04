package org.example;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;

public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:monitorBD.db";

    public DatabaseManager() {
        crearTabla();
    }


    private void crearTabla() {

        String sql = "CREATE TABLE IF NOT EXISTS datos_sensor (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "x INTEGER NOT NULL, " +
                "y INTEGER NOT NULL, " +
                "z INTEGER NOT NULL, " +
                "fecha_de_captura TEXT NOT NULL, " +
                "hora_de_captura TEXT NOT NULL)";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            System.out.println("✅ Base de datos 'monitorBD.db' y tabla 'datos_sensor' listas.");

        } catch (SQLException e) {
            System.err.println("❌ ERROR CRÍTICO al conectar o crear la tabla:");
            System.err.println("   Causa: " + e.getMessage());
            System.err.println("   Asegúrese de que el driver 'sqlite-jdbc' esté en el pom.xml o librerías.");
        }
    }
    // =========================================================

    public void guardarDato(int x, int y, int z) {
        String sql = "INSERT INTO datos_sensor(x, y, z, fecha_de_captura, hora_de_captura) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {


            String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String hora = new SimpleDateFormat("HH:mm:ss").format(new Date());

            pstmt.setInt(1, x);
            pstmt.setInt(2, y);
            pstmt.setInt(3, z);
            pstmt.setString(4, fecha);
            pstmt.setString(5, hora);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al guardar dato: " + e.getMessage());
        }
    }

    public List<DatoSensor> consultarFiltrados(String fechaFiltro, String horaFiltro) {
        List<DatoSensor> datos = new ArrayList<>();

        String sql = "SELECT id, x, y, z, fecha_de_captura, hora_de_captura FROM datos_sensor WHERE fecha_de_captura = ? AND hora_de_captura >= ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fechaFiltro);
            pstmt.setString(2, horaFiltro);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                datos.add(new DatoSensor(
                        rs.getInt("id"),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        rs.getString("fecha_de_captura"),
                        rs.getString("hora_de_captura")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar datos filtrados: " + e.getMessage());
        }
        return datos;
    }
}