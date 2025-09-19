package com.app.GoldenFeets.Exceptions;

// Puedes crear un nuevo paquete "Exception" para organizar estas clases
public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(String message) {
        super(message);
    }
}