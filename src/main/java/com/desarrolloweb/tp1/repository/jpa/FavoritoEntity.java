package com.desarrolloweb.tp1.repository.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entidad JPA de Favorito. No puede ser un record: Hibernate necesita
 * constructor vacío y estado mutable. El record Favorito del dominio (TP1)
 * no se toca.
 */
@Entity
@Table(name = "favoritos")
public class FavoritoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column
    private String nota;

    @Column(name = "fecha_alta", nullable = false)
    private LocalDateTime fechaAlta;

    // Lado propietario de la relación. No se modela el lado inverso
    // (@OneToMany en ListaEntity) para evitar las complicaciones de carga
    // perezosa de una relación bidireccional: se resuelve con una consulta
    // derivada (findByListaId) desde FavoritoJpaRepository.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lista_id", nullable = false)
    private ListaEntity lista;

    protected FavoritoEntity() {
        // requerido por Hibernate
    }

    public FavoritoEntity(Long id, Long productoId, String nota, LocalDateTime fechaAlta, ListaEntity lista) {
        this.id = id;
        this.productoId = productoId;
        this.nota = nota;
        this.fechaAlta = fechaAlta;
        this.lista = lista;
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

    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(LocalDateTime fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public ListaEntity getLista() {
        return lista;
    }

    public void setLista(ListaEntity lista) {
        this.lista = lista;
    }
}
