# TP1 + TP2 · Spring Boot, API REST, arquitectura en capas y persistencia

API REST en capas (Controller → Service → Repository/Adapter) que expone:

- **Catálogo de productos** (`/api/productos`, solo lectura) — consume la API pública [DummyJSON](https://dummyjson.com) y devuelve los datos mapeados a un DTO propio. No cambió respecto del TP1.
- **Favoritos** (`/api/favoritos`, CRUD completo) — ahora persistido en **PostgreSQL** vía JPA/Hibernate (antes vivía en memoria, TP1).
- **Listas** (`/api/listas`, CRUD + relación) — entidad nueva del TP2 para organizar favoritos.

## Requisitos

- Java 25
- Maven 3.9+
- Docker y Docker Compose (o una instalación local de PostgreSQL)

## Cómo levantar el proyecto

### 1. Levantar PostgreSQL

Con Docker (recomendado):

```bash
docker compose up -d
```

Esto levanta Postgres en `localhost:5432` con base `tp1_favoritos`, usuario `tp1_user` y contraseña `tp1_pass` (configurable por variables de entorno `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` — ver `docker-compose.yml`).

Si no podés usar Docker: instalá PostgreSQL localmente y creá una base y usuario con esos mismos datos (o ajustá `application.properties`).

### 2. Levantar la aplicación

```bash
mvn spring-boot:run
```

Al arrancar, **Flyway aplica automáticamente las migraciones** de `src/main/resources/db/migration/`. Para confirmar que corrieron:

- En el log vas a ver líneas como `Migrating schema "public" to version "1 - create favoritos"`, etc.
- Conectándote a la base (`psql` o un cliente gráfico), la tabla `flyway_schema_history` lista las migraciones aplicadas, y deberían existir `favoritos` y `listas`.

La aplicación queda escuchando en `http://localhost:8080`.

## Documentación (Swagger)

- Swagger UI: http://localhost:8080/swagger-ui.html — muestra `productos`, `favoritos` y `listas`.
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Endpoints

### Productos (solo lectura, sin cambios respecto del TP1)

| Método | Endpoint               | Descripción                                  |
|--------|--------------------------|------------------------------------------------|
| GET    | `/api/productos`        | Lista productos (`?limit=&skip=` opcionales)    |
| GET    | `/api/productos/{id}`   | Obtiene un producto por id                      |

### Favoritos (CRUD, ahora sobre PostgreSQL)

| Método | Endpoint               | Código de éxito | Descripción             |
|--------|--------------------------|------------------|---------------------------|
| POST   | `/api/favoritos`        | 201 Created      | Crea un favorito (requiere `listaId`) |
| GET    | `/api/favoritos`        | 200 OK           | Lista todos los favoritos |
| GET    | `/api/favoritos/{id}`   | 200 OK           | Obtiene uno por id         |
| PUT    | `/api/favoritos/{id}`   | 200 OK           | Actualiza un favorito      |
| DELETE | `/api/favoritos/{id}`   | 204 No Content   | Elimina un favorito        |

### Listas (nuevo en TP2)

| Método | Endpoint                          | Código de éxito  | Descripción                                  |
|--------|-------------------------------------|-------------------|-----------------------------------------------|
| POST   | `/api/listas`                      | 201 Created       | Crea una lista                                 |
| GET    | `/api/listas`                      | 200 OK            | Lista todas las listas                         |
| GET    | `/api/listas/{id}`                 | 200 OK            | Obtiene una lista por id                       |
| GET    | `/api/listas/{id}/favoritos`       | 200 OK            | Lista los favoritos de esa lista               |
| DELETE | `/api/listas/{id}`                 | 204 / 409         | Elimina una lista vacía (409 si tiene favoritos) |
| POST   | `/api/listas/{origenId}/mover-favoritos` | 204 No Content | Mueve todos los favoritos de `origenId` a la lista del body, y borra `origenId` (transaccional) |

## Manejo de errores

Todas las respuestas de error siguen el mismo formato (`ErrorResponseDTO`), manejado en un `@RestControllerAdvice` central:

- Favorito o lista inexistente → `404 Not Found`
- Validación fallida del body → `400 Bad Request` (detalle de campo/motivo)
- Intentar borrar una lista con favoritos → `409 Conflict`
- Falla al consumir DummyJSON (caída o timeout) → `502 Bad Gateway`
- Cualquier otro error no controlado → `500 Internal Server Error`

## Qué cambió del TP1 al TP2 (puertos y adaptadores)

El puerto `FavoritoRepository` (la interfaz definida en el TP1) **no cambió su forma** — solo se le agregó `findByListaId`, necesario para el nuevo dominio de Listas. Lo que cambió fue el adapter detrás de esa interfaz:

- **TP1**: `InMemoryFavoritoRepository`, con un `Map` en memoria.
- **TP2**: `FavoritoRepositoryAdapter`, que delega en Spring Data JPA (`FavoritoJpaRepository`) contra PostgreSQL.

`FavoritoService`, `FavoritoController` y el dominio `Favorito` (record) **no se tocaron en su lógica** — siguen hablando contra el puerto `FavoritoRepository`, sin saber si detrás hay un `Map` o una base de datos real. Eso es exactamente lo que habilita la arquitectura hexagonal: el puerto es un contrato estable, el adapter es intercambiable.

Sí se ampliaron los DTOs (`FavoritoRequestDTO`/`FavoritoResponseDTO` ahora incluyen `listaId`), porque eso es un cambio de contrato de la API, no de la arquitectura de persistencia.

## Evolución del esquema (por qué una migración nueva y no editar una vieja)

Cuando en el punto 5 se agregó `lista_id` a `favoritos` (V3), esa columna quedó nullable para no romper filas ya existentes de favoritos creados antes de que existiera el concepto de "lista". Pero el modelo final requiere que todo favorito tenga una lista.

Resolver esto editando `V1` o `V3` directamente sería incorrecto: Flyway calcula un checksum de cada migración ya aplicada y, si detecta que el contenido de una migración con una versión ya registrada en `flyway_schema_history` cambió, **falla el arranque** en vez de reaplicarla — porque el historial de versiones tiene que reflejar exactamente lo que se ejecutó contra cada entorno (dev, staging, producción), y "reescribir el pasado" rompe esa garantía en cualquier entorno que ya haya corrido esa versión.

Por eso `V4__lista_id_obligatorio.sql` es una migración *nueva* que hace la transición segura: crea una lista por defecto, reasigna los favoritos huérfanos a ella, y **recién después** declara la columna `NOT NULL` — así una base con datos previos del punto 3 no rompe al migrar.

## Transacciones y ACID

`ListaService.moverFavoritos` está anotado con `@Transactional`. La operación hace, en una sola transacción: valida que ambas listas existan, reasigna todos los favoritos de la lista origen a la destino (un `UPDATE` por favorito), y elimina la lista origen.

Si sacáramos el `@Transactional` y una escritura fallara después de que otra ya se confirmó — por ejemplo, se reasignan 3 de 5 favoritos y el proceso se cae antes del `DELETE` de la lista origen — la base quedaría en un estado inconsistente: 3 favoritos ya apuntando a la lista destino, 2 todavía en la lista origen, y la lista origen sin borrarse. Ningún cliente que haya pedido "mover todo y cerrar la lista" esperaría ese resultado a medias. Eso es exactamente lo que la **atomicidad** de ACID garantiza: la operación se aplica completa o no se aplica nada — `@Transactional` hace que, ante cualquier excepción durante el método, Spring haga rollback de todo lo escrito hasta ese punto.

## Estructura del proyecto

```
src/main/java/com/desarrolloweb/tp1/
├── Tp1Application.java
├── config/          → RestClient y metadata de OpenAPI
├── controller/      → ProductoController, FavoritoController, ListaController
├── service/         → ProductoService, FavoritoService, ListaService
├── repository/       → Puertos: FavoritoRepository, ListaRepository
│   └── jpa/          → Adapters: entidades JPA, JpaRepository de Spring Data,
│                        y los *RepositoryAdapter que implementan los puertos
├── dto/             → DTOs de entrada/salida, desacoplados del modelo interno
├── model/           → Dominio: records Favorito y Lista
├── client/          → Cliente RestClient hacia DummyJSON + modelos internos del JSON externo
└── exception/       → Excepciones propias + manejador centralizado de errores

src/main/resources/
├── application.properties
└── db/migration/    → V1..V4, migraciones versionadas de Flyway
```
