# Guía de uso — Chat Backend (Contexto Identity)

Backend del monolito modular **Chat Social** (Spring Boot 4.1.1 + Java 21 + PostgreSQL 16 + Flyway).
Esta guía cubre cómo arrancar el proyecto y probar el contexto **Identity**
(registro, login, sesiones, cuentas) desde **Swagger UI**.

---

## 1. Requisitos

| Requisito  | Versión / detalle                          |
|------------|--------------------------------------------|
| Java (JDK) | 21                                         |
| Maven      | No hace falta instalarlo: usa `.\mvnw.cmd` |
| PostgreSQL | 16.x corriendo en `localhost:5432`         |
| Base datos | `chat_social` (crear con `CREATE DATABASE chat_social;`) |

## 2. Configuración (variables de entorno)

Todas tienen valor por defecto para desarrollo local. Solo cámbialas si tu entorno difiere:

| Variable      | Default                                                      | Descripción                        |
|---------------|--------------------------------------------------------------|------------------------------------|
| `DB_HOST`     | `localhost`                                                  | Host de PostgreSQL                 |
| `DB_PORT`     | `5432`                                                       | Puerto de PostgreSQL               |
| `DB_NAME`     | `chat_social`                                                | Base de datos                      |
| `DB_USER`     | `postgres`                                                   | Usuario                            |
| `DB_PASSWORD` | `1234`                                                       | Contraseña                         |
| `DB_URL`      | *(se arma con las anteriores)*                               | URL JDBC completa (tiene prioridad)|
| `SERVER_PORT` | `8081`                                                       | Puerto HTTP de la app              |
| `JWT_SECRET`  | *(clave solo-dev incluida)*                                  | Secreto HS256 (mín. 32 caracteres) |

> ⚠️ En producción define `DB_PASSWORD` y `JWT_SECRET` como secretos reales.
> Los valores por defecto son solo para desarrollo local.

Ejemplo en PowerShell:

```powershell
$env:DB_PASSWORD = "1234"
$env:SERVER_PORT = "8081"
```

## 3. Arrancar la aplicación

```powershell
cd chat-backend
.\mvnw.cmd spring-boot:run
```

Al arrancar, **Flyway** crea los 6 esquemas (`identity`, `profiles`, `social`,
`content`, `engagement`, `notifications`) y aplica las migraciones `V1..V6`.
Hibernate valida el mapeo JPA contra esas tablas (`ddl-auto: validate`).

Verás:

```
Tomcat started on port 8081 (http)
Started ChatBackendApplication in ... seconds
```

## 4. Swagger UI

| Recurso                        | URL                                   |
|--------------------------------|---------------------------------------|
| Consola Swagger                | http://localhost:8081/swagger-ui.html |
| Documento OpenAPI (JSON)       | http://localhost:8081/v3/api-docs     |

---

## 5. Flujo de prueba como usuario (paso a paso en Swagger)

Sigue este orden. Los pasos 1-2 son públicos; del 3 en adelante necesitas el token.

### Paso 1 — Registrar una cuenta

`POST /api/v1/auth/register` → *"Try it out"* → body:

```json
{
  "username": "ana_dev",
  "email": "ana@example.com",
  "password": "secret123"
}
```

✅ Esperado: **201** con el usuario creado (`id`, `username`, `email`, `active: true`).
❌ **409** si el username o email ya existen. **400** si los datos son inválidos
(username 3-50 con letras/dígitos/`.`/`_`; contraseña 8-72 con letra + dígito).

> Guarda el `id` del usuario: lo usarás en el paso 7.

### Paso 2 — Iniciar sesión

`POST /api/v1/auth/login` → body (acepta **username o email**):

```json
{
  "usernameOrEmail": "ana_dev",
  "password": "secret123"
}
```

✅ Esperado: **200** con:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "x7Qp...",
  "accessTokenExpiresAt": "...",
  "refreshTokenExpiresAt": "..."
}
```

> Copia el `accessToken` y el `refreshToken` (el refresh solo se muestra esta vez;
> en la BD queda únicamente su hash).

### Paso 3 — Autorizarte en Swagger (botón Authorize 🔓)

1. Pulsa **Authorize** (arriba a la derecha).
2. En `bearerAuth` pega **solo el token** (sin la palabra `Bearer`).
3. Pulsa *Authorize* → *Close*.

A partir de aquí Swagger envía `Authorization: Bearer <token>` en cada petición.

### Paso 4 — Ver mi cuenta

`GET /api/v1/users/me` → ✅ **200** con tu usuario.
Sin token (o con token inválido/expirado): **401**.

### Paso 5 — Ver mis sesiones activas

`GET /api/v1/auth/sessions` → ✅ **200**, p. ej.:

```json
[{ "id": "...", "createdAt": "...", "expiresAt": "..." }]
```

Cada login o refresh crea una sesión (= un "dispositivo"). Nunca expone el token.

### Paso 6 — Renovar la sesión (rotación)

`POST /api/v1/auth/refresh` (**sin necesidad de Authorize**) → body:

```json
{ "refreshToken": "<el refreshToken del paso 2>" }
```

✅ Esperado: **200** con un par **nuevo** de tokens.
⚠️ El refresh token usado queda **revocado**: si lo reutilizas obtienes **401**.
Actualiza tu token y vuelve a pulsar **Authorize** con el nuevo `accessToken`.

### Paso 7 — Probar errores típicos (opcional)

| Prueba                                              | Esperado |
|-----------------------------------------------------|----------|
| `GET /api/v1/users/me` sin Authorize                | 401      |
| Login con contraseña incorrecta                     | 401      |
| Registrar de nuevo `ana_dev` / `ana@example.com`    | 409      |
| `GET /api/v1/users/{id}` con un UUID inexistente    | 404      |

### Paso 8 — Cambiar la contraseña

`POST /api/v1/users/me/password` (con Authorize) → body:

```json
{
  "currentPassword": "secret123",
  "newPassword": "nuevaClave456"
}
```

✅ Esperado: **204**. La contraseña actual debe ser correcta (**401** si no).

### Paso 9 — Cerrar sesión en este dispositivo

`POST /api/v1/auth/logout` (con Authorize) → body:

```json
{ "refreshToken": "<tu refreshToken vigente>" }
```

✅ Esperado: **204**. Ese refresh token queda revocado (usarlo en `/refresh` → **401**).

### Paso 10 — Desactivar / reactivar (soft delete)

`POST /api/v1/users/{id}/deactivate` con el `id` del paso 1 → ✅ **204**.
Efectos: `active: false` y **todas las sesiones revocadas**.
Una cuenta desactivada no puede hacer login (**401**).

`POST /api/v1/users/{id}/activate` → ✅ **204**, la cuenta vuelve a operar.

---

## 6. Referencia rápida de endpoints

### Auth (`/api/v1/auth`)

| Método | Ruta        | Auth | Descripción                          | Éxito |
|--------|-------------|------|--------------------------------------|-------|
| POST   | `/register` | No   | Crear cuenta                         | 201   |
| POST   | `/login`    | No   | Obtener par de tokens                | 200   |
| POST   | `/refresh`  | No   | Rotar sesión (revoca el token usado) | 200   |
| POST   | `/logout`   | Sí   | Revocar un refresh token             | 204   |
| GET    | `/sessions` | Sí   | Listar sesiones activas              | 200   |

### Users (`/api/v1/users`, todos requieren JWT)

| Método | Ruta                 | Descripción                          | Éxito |
|--------|----------------------|--------------------------------------|-------|
| GET    | `/me`                | Mi cuenta                            | 200   |
| GET    | `/{id}`              | Cuenta por id                        | 200   |
| GET    | `/username/{username}` | Cuenta por username exacto         | 200   |
| GET    | `/email/{email}` | Cuenta por correo exacto               | 200   |
| POST   | `/me/password`       | Cambiar mi contraseña                | 204   |
| POST   | `/me/deactivate`     | Desactivar mi cuenta + revocar mis sesiones | 204   |
| POST   | `/me/activate`       | Reactivar mi cuenta                        | 204   |
| POST   | `/{id}/promote`      | Promover cuenta a ADMIN (solo ADMIN)       | 204   |
| POST   | `/{id}/demote`       | Degradar cuenta a USER (solo ADMIN)        | 204   |

### Códigos de error (formato `ErrorResponse`: `timestamp`, `status`, `error`, `message`, `path`)

| Código | Cuándo                                                      |
|--------|-------------------------------------------------------------|
| 400    | Datos inválidos (formato o validación `@Valid`)             |
| 401    | Sin JWT, JWT inválido/expirado, credenciales incorrectas    |
| 403    | Cuenta desactivada (`is_active = false`)                    |
| 404    | Usuario no encontrado                                       |
| 409    | Username o email ya registrados                             |
| 423    | Cuenta bloqueada temporalmente (5 fallos de login, 15 min)  |
| 500    | Error interno (sin detalles al cliente)                     |

> Por seguridad, el login **no distingue** "usuario inexistente" de
> "contraseña incorrecta": ambos devuelven 401 genérico.

---

## 7. Ejemplos con cURL / PowerShell

Base: `http://localhost:8081/api/v1`.

```bash
# Registro
curl -X POST localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"ana_dev","email":"ana@example.com","password":"secret123"}'

# Login (guarda los tokens)
curl -X POST localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"ana_dev","password":"secret123"}'

# Mi cuenta (sustituye $TOKEN)
curl localhost:8081/api/v1/users/me \
  -H "Authorization: Bearer $TOKEN"

# Refresh (rota: el token viejo queda revocado)
curl -X POST localhost:8081/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"$REFRESH"}'

# Logout
curl -X POST localhost:8081/api/v1/auth/logout \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"refreshToken":"$REFRESH"}'
```

Equivalente en PowerShell:

```powershell
$base = "http://localhost:8081/api/v1"
$reg = Invoke-RestMethod -Method Post -Uri "$base/auth/register" `
  -ContentType "application/json" `
  -Body '{"username":"ana_dev","email":"ana@example.com","password":"secret123"}'
$login = Invoke-RestMethod -Method Post -Uri "$base/auth/login" `
  -ContentType "application/json" `
  -Body '{"usernameOrEmail":"ana_dev","password":"secret123"}'
$headers = @{ Authorization = "Bearer $($login.accessToken)" }
$me = Invoke-RestMethod -Method Get -Uri "$base/users/me" -Headers $headers
```

---

## 8. Notas de diseño (para desarrolladores)

- **Access token** (JWT HS256, 15 min): stateless, va en `Authorization: Bearer`.
- **Refresh token** (256 bits, 30 días): solo se guarda su **hash SHA-256** en
  `identity.refresh_tokens`; el texto plano se entrega una sola vez al cliente.
- **Rotación**: cada `/refresh` revoca el token usado y emite uno nuevo.
- **Eventos de dominio** (`UserRegistered`, `UserDeactivated`, `PasswordChanged`):
  Spring Data los publica al hacer `save()`; los consumidores deben usar
  `@TransactionalEventListener(phase = AFTER_COMMIT)`.
- **Tests**: `.\mvnw.cmd test` (contexto + Flyway + validación de esquema).
