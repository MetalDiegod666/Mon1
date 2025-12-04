package org.example;


public class Encriptador {

    private static final int KEY = 13;

    public static String encriptar(String texto) {
        StringBuilder resultado = new StringBuilder();
        for (char caracter : texto.toCharArray()) {
            resultado.append((char) (caracter + KEY));
        }
        return resultado.toString();
    }

    public static String desencriptar(String textoEncriptado) {
        StringBuilder resultado = new StringBuilder();
        for (char caracter : textoEncriptado.toCharArray()) {
            resultado.append((char) (caracter - KEY));
        }
        return resultado.toString();
    }
}