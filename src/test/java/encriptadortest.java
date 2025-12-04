import org.example.Encriptador;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class encriptadortest {

    // Prueba 1: Verifica el ciclo completo (Cifrado -> Descifrado)
    @Test
    void prueba_simetria_encriptacion_desencriptacion() {

        // 1. ARRANGE: Define los datos que vas a enviar (ejemplo de datos del sensor)
        String datosOriginales = "10,50,99|2025-10-20";

        // 2. ACT: Ejecuta los métodos de la clase Encriptador
        String encriptado = Encriptador.encriptar(datosOriginales);
        String desencriptado = Encriptador.desencriptar(encriptado);

        // 3. ASSERT: Afirma que el texto desencriptado es igual al original.
        Assertions.assertEquals(datosOriginales, desencriptado,
                "El texto desencriptado debe coincidir con el original después del ciclo.");
    }

    // Prueba 2: Verifica que el texto encriptado no sea igual al original
    @Test
    void prueba_el_texto_esta_realmente_encriptado() {

        // ARRANGE
        String datosOriginales = "x:50,y:50,z:50";

        // ACT & ASSERT: Verificamos que el resultado no sea el mismo que la entrada.
        Assertions.assertNotEquals(datosOriginales, Encriptador.encriptar(datosOriginales),
                "El texto encriptado NO debe ser idéntico al original.");
    }

    // Prueba 3: Verifica el manejo de una cadena vacía
    @Test
    void prueba_cadena_vacia() {
        // ARRANGE
        String datosOriginales = "";

        // ACT
        String encriptado = Encriptador.encriptar(datosOriginales);
        String desencriptado = Encriptador.desencriptar(encriptado);

        // ASSERT: Una cadena vacía debe seguir siendo vacía después del ciclo.
        Assertions.assertEquals("", desencriptado,
                "Una cadena vacía debe seguir siendo vacía.");
    }
}
