# TP1 · Spring Boot, API REST y arquitectura en capas

API REST en capas (Controller → Service → Repository) que expone:

- **Catálogo de productos** (`/api/productos`, solo lectura) — consume la API pública [DummyJSON](https://dummyjson.com) y devuelve los datos mapeados a un DTO propio.
- **Favoritos** (`/api/favoritos`, CRUD completo) — recurso propio guardado en memoria (sin base de datos).

## Requisitos

- Java 25
- Maven 3.9+
- Conexión a internet (para consumir DummyJSON)

## Cómo levantar el proyecto

```bash
mvn spring-boot:run
```

La aplicación queda escuchando en `http://localhost:8080`.

## Documentación (Swagger)

Con la app corriendo:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Endpoints

### Productos (solo lectura)

| Método | Endpoint                 | Descripción                                   |
|--------|---------------------------|------------------------------------------------|
| GET    | `/api/productos`          | Lista productos (`?limit=&skip=` opcionales)    |
| GET    | `/api/productos/{id}`     | Obtiene un producto por id                      |

### Favoritos (CRUD)

| Método | Endpoint               | Código de éxito | Descripción            |
|--------|-------------------------|------------------|-------------------------|
| POST   | `/api/favoritos`        | 201 Created      | Crea un favorito         |
| GET    | `/api/favoritos`        | 200 OK           | Lista todos los favoritos|
| GET    | `/api/favoritos/{id}`   | 200 OK           | Obtiene uno por id        |
| PUT    | `/api/favoritos/{id}`   | 200 OK           | Actualiza un favorito     |
| DELETE | `/api/favoritos/{id}`   | 204 No Content   | Elimina un favorito       |

## Ejemplos de prueba

### Crear un favorito (éxito)

```bash
curl -i -X POST http://localhost:8080/api/favoritos \
  -H "Content-Type: application/json" \
  -d '{"productoId": 1, "nota": "Regalo para mi hermana"}'
```

### Crear un favorito (error de validación → 400)

```bash
curl -i -X POST http://localhost:8080/api/favoritos \
  -H "Content-Type: application/json" \
  -d '{"nota": "Sin productoId"}'
```

### Obtener un favorito inexistente (error → 404)

```bash
curl -i http://localhost:8080/api/favoritos/999
```

### Listar productos del catálogo externo (éxito)

```bash
curl -i "http://localhost:8080/api/productos?limit=5&skip=0"
```

## Manejo de errores

Todas las respuestas de error siguen el mismo formato (`ErrorResponseDTO`):

```json
{
  "timestamp": "2026-09-15T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "No existe un favorito con id 999",
  "detalles": null
}
```

Casos contemplados por el `@RestControllerAdvice`:

- Favorito inexistente → `404 Not Found`
- Validación fallida del body → `400 Bad Request` (con detalle de campo/motivo en `detalles`)
- Falla al consumir DummyJSON (caída o timeout) → `502 Bad Gateway`
- Cualquier otro error no controlado → `500 Internal Server Error`

## Estructura del proyecto

```
src/main/java/com/desarrolloweb/tp1/
├── Tp1Application.java
├── config/          → RestClient y metadata de OpenAPI
├── controller/      → ProductoController, FavoritoController
├── service/         → ProductoService, FavoritoService
├── repository/      → FavoritoRepository (interfaz + implementación en memoria)
├── dto/             → DTOs de entrada/salida, desacoplados del modelo interno
├── model/           → Entidad de dominio Favorito
├── client/          → Cliente RestClient hacia DummyJSON + modelos internos del JSON externo
└── exception/       → Excepciones propias + manejador centralizado de errores
```
