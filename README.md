# TP1 + TP2 + TP3 · API REST con Spring Boot, persistencia, seguridad y CI

API REST en capas (Controller → Service → Repository/Adapter) para la materia **Desarrollo Web II** de la UNViMe. El proyecto fue creciendo práctica a práctica sobre el mismo repositorio:

- **TP1**: catálogo de productos (DummyJSON) y CRUD de favoritos en memoria.
- **TP2**: favoritos y listas persistidos en PostgreSQL con JPA/Hibernate, Flyway y arquitectura hexagonal (puertos y adaptadores).
- **TP3**: usuarios, autenticación con JWT, autorización por rol y propiedad de recursos, pruebas unitarias, pruebas de integración con Testcontainers, integración continua y despliegue.

## ¿Dónde termina el TP2 y dónde arranca el TP3?

El corte está marcado en Git, igual que se hizo con el TP1:

| Hito | Referencia |
|------|------------|
| Fin del TP1 | tag `entrega-tp1` |
| Fin del TP2 | tag `entrega-tp2` (commit `47734f3`) |
| TP3 | rama `tp3-seguridad`, creada desde `entrega-tp2` |

```bash
git switch tp3-seguridad   # estado con el TP3
git switch main            # volver al cierre del TP2 (sin TP3)
git tag                    # ver los hitos
```

Si querés integrar el TP3 a `main`:

```bash
git switch main
git merge --no-ff tp3-seguridad
```

## Dominio

`Usuario → Lista → Favorito`. Un usuario común solo puede ver y modificar sus propias listas (y los favoritos que cuelgan de ellas); un usuario `ADMIN` puede ver y operar sobre las de cualquiera. El catálogo de productos sigue siendo público.

- **Usuario**: `id`, `email` único, `passwordHash` (BCrypt), `rol` (`USER` / `ADMIN`).
- **Lista**: `id`, `nombre`, `usuarioId`.
- **Favorito**: `id`, `productoId`, `listaId`, `nota`, `fechaAlta`.

## Stack

- Java 25
- Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation, Flyway)
- PostgreSQL 16+
- JWT con `jjwt` 0.12.6
- Maven Wrapper (`./mvnw`, no hace falta tener Maven instalado)
- JUnit 6 + Mockito + Testcontainers 2
- springdoc / Swagger UI

## Requisitos

- Java 25 (`java -version`)
- Docker **o** conexión a un PostgreSQL propio. Si no tenés Docker, el script incluido levanta un PostgreSQL portable sin sudo.
- Nada más: Maven se descarga solo con `./mvnw`.

## Puesta en marcha

### 1. Base de datos

Con Docker (recomendado si lo tenés):

```bash
docker compose up -d
```

Sin Docker (PostgreSQL portable dentro de `.local/`):

```bash
./scripts/local-postgres.sh start
```

El script detecta Docker automáticamente; si no existe, descarga PostgreSQL 17 portable y lo ejecuta como tu usuario. Para detenerlo:

```bash
./scripts/local-postgres.sh stop
```

### 2. Aplicación

```bash
./mvnw spring-boot:run
```

Al arrancar, **Flyway aplica V1..V6** automáticamente. La app queda en `http://localhost:8080`.

Verificación rápida:

```bash
curl http://localhost:8080/health
```

Swagger UI: <http://localhost:8080/swagger-ui.html>  
OpenAPI JSON: <http://localhost:8080/v3/api-docs>

### Variables de entorno

Todas tienen defaults pensados para desarrollo local:

| Variable | Default | Descripción |
|----------|---------|-------------|
| `PORT` | `8080` | Puerto HTTP |
| `DB_HOST` | `localhost` | Host de PostgreSQL |
| `DB_PORT` | `5432` | Puerto de PostgreSQL |
| `POSTGRES_DB` | `tp1_favoritos` | Nombre de la base |
| `POSTGRES_USER` | `tp1_user` | Usuario de la base |
| `POSTGRES_PASSWORD` | `tp1_pass` | Contraseña de la base |
| `JWT_SECRET` | secreto de desarrollo | Secreto HS256, **mínimo 32 bytes**. En producción definilo siempre |
| `JWT_EXPIRATION_MS` | `3600000` | Vencimiento del token (1 hora) |
| `DUMMYJSON_BASE_URL` | `https://dummyjson.com` | API externa de productos |

## Autenticación

1. **Registro** (`POST /api/auth/registro`): guarda la contraseña hasheada con BCrypt y asigna rol `USER`. Si el email ya existe devuelve `409`.
2. **Login** (`POST /api/auth/login`): valida contra el hash y devuelve un JWT firmado con `id`, `email` y `rol`. Si falla, `401`.
3. El resto de los endpoints protegidos se llaman con:

```http
Authorization: Bearer <token>
```

El filtro `JwtAuthenticationFilter` valida el token y deja la identidad en el `SecurityContext`. Los endpoints públicos son `/api/auth/**`, `/api/productos/**`, `/health` y Swagger. `/api/listas/**` y `/api/favoritos/**` exigen token válido.

### Convertir un usuario en ADMIN (para probar)

Registralo normalmente y después:

```sql
UPDATE usuarios SET rol = 'ADMIN' WHERE email = 'admin@example.com';
```

Volvé a hacer login para que el JWT nuevo incluya el rol `ADMIN`.

## Endpoints

### Estado

| Método | Endpoint  | Auth | Descripción |
|--------|-----------|------|-------------|
| GET    | `/health` | No   | Estado del servicio |

### Autenticación

| Método | Endpoint              | Auth | Código | Descripción |
|--------|-----------------------|------|--------|-------------|
| POST   | `/api/auth/registro`  | No   | 201 / 409 | Registro con password hasheada |
| POST   | `/api/auth/login`     | No   | 200 / 401 | Login, devuelve JWT |

### Productos (público, solo lectura)

| Método | Endpoint               | Auth | Descripción |
|--------|------------------------|------|-------------|
| GET    | `/api/productos`       | No   | Lista paginada (`?limit=&skip=`) desde DummyJSON |
| GET    | `/api/productos/{id}`  | No   | Producto por id |

### Listas (requieren token)

| Método | Endpoint                                 | Código | Descripción |
|--------|------------------------------------------|--------|-------------|
| POST   | `/api/listas`                            | 201    | Crea una lista; el dueño es el usuario autenticado |
| GET    | `/api/listas`                            | 200    | Listas propias (todas si es `ADMIN`) |
| GET    | `/api/listas/{id}`                       | 200 / 403 / 404 | Lista propia, ajena (403) o inexistente (404) |
| GET    | `/api/listas/{id}/favoritos`             | 200 / 403 / 404 | Favoritos de una lista |
| DELETE | `/api/listas/{id}`                       | 204 / 409 | Elimina una lista vacía (409 si tiene favoritos) |
| POST   | `/api/listas/{origenId}/mover-favoritos` | 204 / 403 | Mueve favoritos y borra la lista origen (transaccional) |

### Favoritos (requieren token)

| Método | Endpoint                | Código | Descripción |
|--------|-------------------------|--------|-------------|
| POST   | `/api/favoritos`        | 201    | Crea un favorito en una lista propia |
| GET    | `/api/favoritos`        | 200    | Favoritos propios (todos si es `ADMIN`) |
| GET    | `/api/favoritos/{id}`   | 200 / 403 / 404 | Favorito propio, ajeno o inexistente |
| PUT    | `/api/favoritos/{id}`   | 200 / 403 / 404 | Actualiza un favorito propio |
| DELETE | `/api/favoritos/{id}`   | 204 / 403 / 404 | Elimina un favorito propio |

En `requests.http` hay una colección completa y ejecutable con la extensión REST Client de VS Code.

## Dónde vive la regla de autorización (consigna 4.5)

La regla de propiedad **vive en la capa de servicio** (`ListaServiceImpl.buscarConAcceso` y `FavoritoService.buscarListaConAcceso`), no en el Controller ni en un filtro ni en anotaciones.

**Por qué ahí:**

- Es una **regla de negocio**: “esta lista pertenece a este usuario salvo que sea ADMIN” no depende de HTTP. Ponerla en el servicio hace que valga para cualquier punto de entrada (controllers, tests, futuros jobs o consumidores internos) y no solo para requests web.
- El **Controller** queda como traductor HTTP: recibe, valida el body, delega y devuelve códigos. Si la regla viviera ahí, cualquier nuevo endpoint que toque listas tendría que repetirla, con riesgo de olvidarla.
- Un **filtro** solo conoce la URL y el token; para decidir propiedad tendría que consultar la base y duplicar lógica de dominio. Además mezclaría autenticación (¿quién sos?) con autorización de recursos (¿podés tocar esto?).
- **Anotaciones** (`@PreAuthorize`) sirven para reglas simples basadas en roles, pero para “el dueño de esta fila” obligan a resolver la entidad antes o a escribir expresiones acopladas a SpEL. Acá la decisión depende de datos (`lista.usuarioId`), así que el servicio es el lugar natural.
- La **autenticación** sí vive en el filtro JWT; la **autorización por rol/propiedad** vive en el servicio. Esa separación es la que se documenta acá.

Comportamiento buscado: si la lista no existe → `404`; si existe pero es de otro usuario y no sos `ADMIN` → `403` (nunca `404`, para no confundir “no existe” con “no es tuya”).

## Pruebas

### Unitarias (`*Test`, las corre Surefire con `mvn test`)

- `ListaServiceImplTest`: confirma que la lista creada queda asociada al usuario autenticado (no a un `usuarioId` del body) y que una lista ajena lanza acceso denegado, salvo para `ADMIN`.
- `UsuarioServiceImplTest`: confirma que `registrar` nunca guarda la contraseña en texto plano, que hashea con `PasswordEncoder`, que el email repetido da conflicto y que una password incorrecta no autentica.

```bash
./mvnw test
```

### Integración (`*IT`, las corre Failsafe con `mvn verify`)

`ListaControllerIT` levanta un **PostgreSQL real con Testcontainers**, deja que **Flyway corra V1..V6 contra una base limpia** y prueba el flujo completo con `MockMvc` y el filtro de seguridad real:

| Escenario | Resultado esperado |
|-----------|--------------------|
| Login con credenciales válidas | `200` + token |
| Login con credenciales inválidas | `401` |
| Acceder a una lista propia | `200` |
| Acceder a una lista ajena | `403` |
| Acceder a `/api/listas` sin token | `401` |
| Un `ADMIN` accede a una lista ajena | `200` |

```bash
./mvnw verify
```

Si no hay Docker, las clases `*IT` se **saltan** (`disabledWithoutDocker = true`) para que puedas correr `./mvnw verify` en una máquina sin Docker. En CI, donde Docker sí existe, se ejecutan de verdad. Para exigir Docker localmente, quitá ese atributo de `@Testcontainers`.

Los nombres siguen la convención del práctico: clases unitarias `*Test`, integración `*IT`, y métodos `operación_escenario_resultado` (por ejemplo, `obtener_listaAjena_devuelve403`).

## Integración continua (GitHub Actions)

El workflow está en `.github/workflows/ci.yml` y corre en cada `push` y `pull_request`:

1. checkout,
2. Java 25 (Temurin),
3. `chmod +x mvnw` (por si el wrapper perdió el bit de ejecución),
4. `./mvnw -B verify` (unitarias + integración con Testcontainers),
5. publica los reportes de Surefire/Failsafe como artefacto.

Para ver el resultado: pestaña **Actions** del repo → workflow **CI** → job **Compilar, tests unitarios e integración**. Ahí están los logs y los reportes descargables.

Para comprobar que el pipeline realmente falla, rompé a propósito una aserción de `ListaServiceImplTest`, hacé push, verificá el job en rojo y después revertí el cambio.

## Migraciones V5 y V6

- `V5__create_usuarios.sql`: crea `usuarios` con email único, `password_hash` y `rol` (`USER`/`ADMIN`), con check constraint.
- `V6__add_usuario_id_a_listas.sql`: agrega `usuario_id` a `listas`, lo deja `NOT NULL` e indexa.

Como una migración ya aplicada no se edita (Flyway valida checksums), las listas que venían del TP2 se asignan a un usuario `legacy@tp3.local` con hash inválido a propósito (`!`): no puede iniciar sesión, pero conserva los datos. Si la base no tiene listas, ese usuario no se crea.

## Qué cambió del TP2 al TP3

- Se agregó `spring-boot-starter-security` + `jjwt` y el filtro JWT.
- `Lista` sumó `usuarioId`; el dueño sale del token, nunca del body.
- `ListaService` pasó a interfaz + `ListaServiceImpl`; `UsuarioService` + `UsuarioServiceImpl` son nuevos.
- `FavoritoService` ahora también valida que la lista del favorito sea del usuario autenticado.
- Se agregaron migraciones V5/V6, `UsuarioRepository` y su adapter JPA.
- Se sumaron tests unitarios, tests de integración con Testcontainers, Failsafe y el workflow de CI.
- Se agregó `/health`, Swagger con esquema Bearer y este README.

## Estructura del proyecto

```
src/main/java/com/desarrolloweb/tp1/
├── Tp1Application.java
├── config/          → RestClient, OpenAPI y SecurityConfig
├── controller/      → Producto, Favorito, Lista, Auth y Health
├── service/         → ProductoService, FavoritoService,
│                      ListaService/ListaServiceImpl,
│                      UsuarioService/UsuarioServiceImpl
├── repository/      → Puertos: Favorito, Lista, Usuario
│   └── jpa/         → Entidades, JpaRepository y adapters JPA
├── dto/             → DTOs de entrada/salida
├── model/           → Dominio: Favorito, Lista, Usuario, Rol
├── client/          → Cliente RestClient hacia DummyJSON
├── security/        → JwtService, filtro Bearer, CurrentUser y handlers 401/403
└── exception/       → Excepciones propias + manejador centralizado

src/main/resources/
├── application.properties
└── db/migration/    → V1..V6

src/test/java/com/desarrolloweb/tp1/
├── service/         → ListaServiceImplTest, UsuarioServiceImplTest (*Test)
└── controller/      → ListaControllerIT (*IT, Testcontainers)
```

## VS Code

El repo trae configuración lista en `.vscode/`:

- `extensions.json`: extensiones recomendadas (Java Pack, Spring Boot, Maven, Java Test, REST Client, Docker, GitHub Actions, XML, EditorConfig).
- `settings.json`: configuración de Java, exclusiones de `target/` y `.local/`.
- `tasks.json`: tareas para levantar la base, correr `test`, `verify` y `spring-boot:run`.
- `launch.json`: ejecutar la API con variables de entorno de desarrollo.

Las extensiones ya fueron instaladas con `code --install-extension`; si abrís el proyecto en otra máquina, VS Code te va a ofrecer instalarlas desde `extensions.json`.

## Notas de seguridad

- Las contraseñas se guardan siempre con BCrypt (`PasswordEncoder`), nunca en texto plano.
- El JWT se firma con HS256; `JWT_SECRET` debe tener al menos 32 bytes y no debe subirse al repo en producción.
- La API es stateless: no hay sesión ni cookie de autenticación.
- Los errores de seguridad devuelven JSON uniforme (`401`/`403`), no la página de login de Spring.
- El usuario `legacy@tp3.local` de la migración V6 no puede autenticarse (hash inválido a propósito).
