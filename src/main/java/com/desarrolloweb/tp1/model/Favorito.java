package com.desarrolloweb.tp1.model;

import java.time.LocalDateTime;

public class Favorito {

    private Long id;
    private Long productoId;
    private String nota;
    private LocalDateTime fechaAgregado;

    public Favorito() {
    }

    public Favorito(Long id, Long productoId, String nota, LocalDateTime fechaAgregado) {
        this.id = id;
        this.productoId = productoId;
        this.nota = nota;
        this.fechaAgregado = fechaAgregado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    public LocalDateTime getFechaAgregado() {
        return fechaAgregado;
    }

    public void setFechaAgregado(LocalDateTime fechaAgregado) {
        this.fechaAgregado = fechaAgregado;
    }
}
