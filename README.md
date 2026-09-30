# PixelRig API

API REST para la administración de componentes de PC. La identidad de los usuarios está delegada a **Amazon Cognito** (OAuth2 / OIDC) y la autorización se resuelve por **roles**, representados como grupos dentro del User Pool.

Actividad Sumativa N.º 1 — Desarrollo Cloud Native, Duoc UC.

## Stack

| Componente | Versión | Nota |
|---|---|---|
| Java | 21 (LTS) | Base de Spring Boot 4 |
| Spring Boot | 4.1.1 | Spring Web MVC, Spring Data JPA, Spring Security |
| Hibernate ORM | 7.4.5 | Dialecto `org.hibernate.community.dialect.SQLiteDialect` |
| SQLite | driver `sqlite-jdbc` | Base de datos en archivo local (`productos_db.db`) |
| Amazon Cognito | User Pool `us-east-1_9ZxooRhyv` | Proveedor OIDC: emite los JWT que el backend valida |
| Maven | Maven Wrapper | `./mvnw` — no requiere Maven instalado en el sistema |

Las versiones de Spring Boot y de las dependencias de SQLite no se declaran a mano: las gestiona el **BOM** de Spring Boot, lo que evita el conflicto clásico entre la versión del driver, la del dialecto y la de Hibernate.

## Arquitectura

El backend sigue el patrón en capas **CSR** (Controller → Service → Repository → Entity). Cada capa tiene una única responsabilidad y solo conoce a la inmediatamente inferior.

```
src/main/java/cl/duoc/pixelrig/
├── PixelrigApplication.java    punto de entrada (escaneo de componentes y entidades)
├── controller/                 capa HTTP: rutas, verbos y códigos de respuesta
├── service/                    lógica de negocio y transacciones
├── repository/                 acceso a datos (interfaces de Spring Data JPA)
├── entity/                     mapeo objeto-relacional de las tablas
└── config/                     configuración de seguridad
```

| Capa | Responsabilidad | No debe contener |
|---|---|---|
| `controller` | Exponer endpoints REST y traducir JSON ↔ objetos | Reglas de negocio ni consultas |
| `service` | Reglas de negocio, transacciones y composición | Detalles HTTP |
| `repository` | Consultas a la base de datos | Lógica de negocio |
| `entity` | Estructura de las tablas y validaciones de forma | Lógica de negocio |

## Endpoints

| Método | Ruta | Acceso | Código de respuesta |
|---|---|---|---|
| GET | `/api/public/info` | Público | 200 |
| GET | `/api/products` | Autenticado (ADMIN, EDITOR, USER) | 200 |
| POST | `/api/products` | ADMIN | 201 |
| PUT | `/api/products/{id}` | ADMIN | 200 · 404 si no existe |
| DELETE | `/api/products/{id}` | ADMIN | 204 · 404 si no existe |
| POST | `/api/contact` | Autenticado (cualquier rol) | 201 |
| GET | `/api/contact` | ADMIN, EDITOR | 200 |

Todas las respuestas de error de seguridad son **401** (sin token o token inválido) o **403** (token válido sin el rol requerido). Un cuerpo de petición que no cumple las validaciones devuelve **400**.

## Seguridad

El backend actúa como **OAuth2 Resource Server**: no emite tokens, los valida. La emisión corresponde al Authorization Server (Cognito).

**Validación del token.** Spring Security lee la propiedad `issuer-uri`, descarga el documento OIDC del User Pool y obtiene su JWKS. En cada petición verifica la **firma**, la **expiración**, el **issuer** (`iss`) y la **audience** (`aud`) del JWT antes de que la petición alcance cualquier controlador.

**Autorización por roles.** Los roles viajan en el claim `cognito:groups` del token, con el nombre del grupo al que pertenece el usuario. Un `JwtAuthenticationConverter` los traduce a autoridades de Spring Security:

```
cognito:groups: ["ADMIN"]   →   ROLE_ADMIN
```

**Orden de las reglas.** Las reglas de `SecurityConfig` se evalúan de arriba hacia abajo y prevalece la primera coincidencia. Por eso las restricciones por verbo (`POST`, `PUT`, `DELETE` con `hasRole("ADMIN")`) se declaran **antes** de la regla general `.requestMatchers("/api/products/**").authenticated()`; en el orden inverso, cualquier usuario autenticado podría modificar el catálogo.

### Recursos de Cognito

| Dato | Valor |
|---|---|
| User Pool ID | `us-east-1_9ZxooRhyv` |
| App Client ID | `62n42rgrii24pdg7j3ufe13scr` |
| Issuer URL | `https://cognito-idp.us-east-1.amazonaws.com/us-east-1_9ZxooRhyv` |

El App Client es de tipo **SPA, sin client secret**: el cliente corre en el navegador y no puede resguardar un secreto. Ninguno de estos tres valores es confidencial; las contraseñas de los usuarios sí lo son y no se documentan.

### Usuarios de prueba

| Correo | Grupo |
|---|---|
| `admin@pixelrig.cl` | ADMIN |
| `editor@pixelrig.cl` | EDITOR |
| `user@pixelrig.cl` | USER |

## Modelo de datos

**`productos`** — catálogo de componentes.

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | entero, autoincremental | Clave primaria |
| `nombre` | texto | Obligatorio |
| `marca` | texto | — |
| `descripcion` | texto (500) | — |
| `categoria` | texto | CPU, GPU, RAM, ALMACENAMIENTO, PERIFERICO |
| `precio` | entero | Obligatorio, no negativo (peso chileno, sin decimales) |
| `stock` | entero | No negativo |
| `imagenUrl` | texto (500) | — |

**`mensajes_contacto`** — mensajes recibidos desde el formulario de contacto.

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | entero, autoincremental | Clave primaria |
| `nombre` | texto | Obligatorio |
| `email` | texto | Obligatorio, formato de correo |
| `mensaje` | texto (1000) | Obligatorio |
| `fecha` | fecha y hora | Asignada por el servidor al insertar |

Las tablas se crean y actualizan automáticamente al arrancar (`spring.jpa.hibernate.ddl-auto=update`). El archivo `productos_db.db` está excluido del control de versiones.

## Ejecución

**Requisito:** JDK 21 o superior. Maven no es necesario: el proyecto incluye el wrapper.

```bash
./mvnw spring-boot:run
```

El servicio queda disponible en `http://localhost:8081`.

**Verificación:**

```bash
# Endpoint público: debe responder 200 con JSON
curl <http://localhost:8081/api/public/info>

# Endpoint protegido sin token: debe responder 401
curl -i <http://localhost:8081/api/products>
```

### Configuración

`src/main/resources/application.properties`:

```properties
server.port=8081
spring.datasource.url=jdbc:sqlite:productos_db.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update
spring.security.oauth2.resourceserver.jwt.issuer-uri=<https://cognito-idp.us-east-1.amazonaws.com/us-east-1_9ZxooRhyv>
```

## Mapa del repositorio

| Ruta | Contenido |
|---|---|
| `src/main/java/cl/duoc/pixelrig/controller/` | Controladores REST: público, productos y contacto |
| `src/main/java/cl/duoc/pixelrig/service/` | Reglas de negocio y transacciones |
| `src/main/java/cl/duoc/pixelrig/repository/` | Interfaces de acceso a datos |
| `src/main/java/cl/duoc/pixelrig/entity/` | Entidades JPA: `Product`, `ContactMessage` |
| `src/main/java/cl/duoc/pixelrig/config/` | `SecurityConfig`: reglas de acceso y mapeo de roles |
| `src/main/resources/application.properties` | Puerto, conexión a SQLite e issuer de Cognito |
| `src/test/` | Pruebas del proyecto |
| `pom.xml` | Dependencias y build (gestionado por el BOM de Spring Boot) |
