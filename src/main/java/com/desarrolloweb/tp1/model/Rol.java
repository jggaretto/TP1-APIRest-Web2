package com.desarrolloweb.tp1.model;

/**
 * Roles disponibles en el sistema. Se guardan como texto en la base
 * (@Enumerated(EnumType.STRING)) para que el esquema sea legible y estable
 * ante reordenamientos del enum.
 */
public enum Rol {
    USER,
    ADMIN
}
