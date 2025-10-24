# 🚀 Sistema de Gestión de Empleados con JPA y Spring Boot

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.0-green)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2025.0.0-blue)
![Java](https://img.shields.io/badge/Java-21-orange)
![Maven](https://img.shields.io/badge/Maven-3.9.0-red)
![JPA](https://img.shields.io/badge/JPA-3.0-blue)
![Hibernate](https://img.shields.io/badge/Hibernate-6.4-purple)
![Docker](https://img.shields.io/badge/Docker-24.0-cyan)
![MySQL](https://img.shields.io/badge/MySQL-8.0-orange)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![H2](https://img.shields.io/badge/H2-2.2-green)

## 📋 Descripción

Sistema de gestión de empleados desarrollado con Spring Boot y JPA/Hibernate que permite gestionar empleados, departamentos y proyectos. El sistema implementa una API REST completa con validaciones, manejo de excepciones y soporte para múltiples bases de datos.

## 🎯 Características

- ✅ API REST completa con operaciones CRUD
- ✅ Validación de datos con Bean Validation
- ✅ Manejo global de excepciones
- ✅ Soporte multi-base de datos (H2, MySQL, PostgreSQL)
- ✅ Perfiles de Spring para diferentes entornos
- ✅ Relaciones JPA (OneToMany, ManyToOne, ManyToMany)
- ✅ Tests unitarios e integración con JUnit 5 y Mockito
- ✅ Tests de integración con Testcontainers
- ✅ Configuración con Docker Compose

## 🛠️ Tecnologías

- **Java 21**: Lenguaje de programación
- **Spring Boot 3.5.0**: Framework principal
- **Spring Data JPA**: Persistencia de datos
- **Hibernate 6.4**: ORM
- **Maven**: Gestión de dependencias
- **H2**: Base de datos en memoria (desarrollo)
- **MySQL 8.0**: Base de datos relacional (producción)
- **PostgreSQL 16**: Base de datos alternativa
- **Docker & Docker Compose**: Contenedorización
- **JUnit 5 & Mockito**: Testing
- **Testcontainers**: Tests de integración

## 📦 Requisitos Previos

- Java 21 o superior
- Maven 3.9.0 o superior
- Docker y Docker Compose (para bases de datos)
- IDE (IntelliJ IDEA, Eclipse, VS Code)

## 🚀 Instalación y Configuración

### 1. Clonar el Repositorio

```bash
git clone <url-del-repositorio>
cd TP5
```

### 2. Configurar Base de Datos con Docker

El proyecto incluye un archivo `docker-compose.yml` con configuraciones para MySQL y PostgreSQL.

**Iniciar los servicios de base de datos:**

```bash
docker-compose up -d
```

**Verificar que los contenedores estén corriendo:**

```bash
docker-compose ps
```

**Ver logs de los servicios:**

```bash
docker-compose logs -f
```

**Detener los servicios:**

```bash
docker-compose down
```

**Eliminar los servicios y volúmenes (⚠️ borra todos los datos):**

```bash
docker-compose down -v
```

### 3. Configuración de Perfiles

El proyecto incluye tres perfiles configurados en `application.yml`:

#### **Perfil `dev` (Por defecto)**
- Base de datos: H2 en memoria
- Consola H2: http://localhost:8080/h2-console
- No requiere Docker

#### **Perfil `mysql`**
- Base de datos: MySQL 8.0
- Requiere Docker Compose corriendo
- Puerto: 3306

#### **Perfil `postgres`**
- Base de datos: PostgreSQL 16
- Requiere Docker Compose corriendo
- Puerto: 5432

### 4. Compilar el Proyecto

```bash
mvn clean install
```

### 5. Ejecutar la Aplicación

**Con perfil por defecto (H2):**

```bash
mvn spring-boot:run
```

**Con perfil MySQL:**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

**Con perfil PostgreSQL:**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

**O usando variables de entorno:**

```bash
# Windows
set SPRING_PROFILES_ACTIVE=mysql
mvn spring-boot:run

# Linux/Mac
export SPRING_PROFILES_ACTIVE=mysql
mvn spring-boot:run
```

### 6. Ejecutar Tests

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar solo tests unitarios
mvn test -Dtest=*Test

# Ejecutar solo tests de integración
mvn test -Dtest=*IntegrationTest
```

## 📚 API Endpoints

La aplicación expone una API REST en `http://localhost:8080/api`

### 👥 Empleados (`/api/empleados`)

| Método | Endpoint | Descripción | Body |
|--------|----------|-------------|------|
| GET | `/api/empleados` | Obtener todos los empleados | - |
| GET | `/api/empleados/{id}` | Obtener empleado por ID | - |
| GET | `/api/empleados/departamento/{nombre}` | Empleados por departamento | - |
| GET | `/api/empleados/email/{email}` | Buscar por email | - |
| GET | `/api/empleados/salario?salarioMinimo={monto}` | Empleados con salario mayor a | - |
| POST | `/api/empleados` | Crear nuevo empleado | JSON Empleado |
| PUT | `/api/empleados/{id}` | Actualizar empleado | JSON Empleado |
| DELETE | `/api/empleados/{id}` | Eliminar empleado | - |

**Ejemplo de body para crear/actualizar empleado:**

```json
{
  "nombre": "Juan Pérez",
  "email": "juan.perez@example.com",
  "salario": 75000.00,
  "departamento": {
    "id": 1
  }
}
```

### 🏢 Departamentos (`/api/departamentos`)

| Método | Endpoint | Descripción | Body |
|--------|----------|-------------|------|
| GET | `/api/departamentos` | Obtener todos los departamentos | - |
| GET | `/api/departamentos/{id}` | Obtener departamento por ID | - |
| GET | `/api/departamentos/nombre/{nombre}` | Buscar por nombre | - |
| GET | `/api/departamentos/presupuesto?presupuestoMinimo={monto}` | Departamentos con presupuesto mayor a | - |
| POST | `/api/departamentos` | Crear nuevo departamento | JSON Departamento |
| PUT | `/api/departamentos/{id}` | Actualizar departamento | JSON Departamento |
| DELETE | `/api/departamentos/{id}` | Eliminar departamento | - |

**Ejemplo de body para crear/actualizar departamento:**

```json
{
  "nombre": "Recursos Humanos",
  "presupuesto": 500000.00
}
```

### 📊 Proyectos (`/api/proyectos`)

| Método | Endpoint | Descripción | Body |
|--------|----------|-------------|------|
| GET | `/api/proyectos` | Obtener todos los proyectos | - |
| GET | `/api/proyectos/{id}` | Obtener proyecto por ID | - |
| GET | `/api/proyectos/activos` | Obtener proyectos activos | - |
| GET | `/api/proyectos/{id}/empleados` | Empleados del proyecto | - |
| POST | `/api/proyectos` | Crear nuevo proyecto | JSON Proyecto |
| POST | `/api/proyectos/{id}/asignar-empleados` | Asignar empleados | JSON IDs |
| PUT | `/api/proyectos/{id}` | Actualizar proyecto | JSON Proyecto |
| PUT | `/api/proyectos/{id}/empleados` | Actualizar empleados | JSON IDs |
| DELETE | `/api/proyectos/{id}` | Eliminar proyecto | - |

**Ejemplo de body para crear/actualizar proyecto:**

```json
{
  "nombre": "Proyecto Alpha",
  "descripcion": "Sistema de gestión interna",
  "fechaInicio": "2025-01-01",
  "fechaFin": "2025-12-31",
  "activo": true
}
```

**Ejemplo de body para asignar empleados:**

```json
{
  "empleadosIds": [1, 2, 3, 4]
}
```

## 🧪 Ejemplos de Uso con cURL

### Crear un departamento

```bash
curl -X POST http://localhost:8080/api/departamentos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Desarrollo",
    "presupuesto": 1000000.00
  }'
```

### Crear un empleado

```bash
curl -X POST http://localhost:8080/api/empleados \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Ana García",
    "email": "ana.garcia@example.com",
    "salario": 85000.00,
    "departamento": {"id": 1}
  }'
```

### Obtener todos los empleados

```bash
curl http://localhost:8080/api/empleados
```

### Buscar empleados por salario

```bash
curl "http://localhost:8080/api/empleados/salario?salarioMinimo=70000"
```

### Crear un proyecto

```bash
curl -X POST http://localhost:8080/api/proyectos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Sistema CRM",
    "descripcion": "Sistema de gestión de clientes",
    "fechaInicio": "2025-02-01",
    "fechaFin": "2025-08-31",
    "activo": true
  }'
```

### Asignar empleados a un proyecto

```bash
curl -X POST http://localhost:8080/api/proyectos/1/asignar-empleados \
  -H "Content-Type: application/json" \
  -d '{
    "empleadosIds": [1, 2, 3]
  }'
```

## 🐳 Configuración de Docker

### Variables de Entorno - MySQL

```yaml
MYSQL_ROOT_PASSWORD: root_password    # Contraseña del usuario root
MYSQL_DATABASE: empleados_db          # Nombre de la base de datos
MYSQL_USER: empleados_user            # Usuario de aplicación
MYSQL_PASSWORD: empleados_pass        # Contraseña del usuario
```

### Variables de Entorno - PostgreSQL

```yaml
POSTGRES_DB: empleados_db             # Nombre de la base de datos
POSTGRES_USER: empleados_user         # Usuario de la base de datos
POSTGRES_PASSWORD: empleados_pass     # Contraseña del usuario
```

### Puertos Expuestos

- **MySQL**: `localhost:3306`
- **PostgreSQL**: `localhost:5432`
- **Aplicación Spring Boot**: `localhost:8080`
- **Consola H2** (solo perfil dev): `http://localhost:8080/h2-console`

### Persistencia de Datos

Los datos se almacenan en volúmenes Docker nombrados:
- `mysql_data`: Datos de MySQL
- `postgres_data`: Datos de PostgreSQL

Los datos persisten entre reinicios del contenedor, pero se eliminan con `docker-compose down -v`.

### Comandos Útiles de Docker

```bash
# Ver estado de los contenedores
docker-compose ps

# Acceder a la consola de MySQL
docker exec -it empleados_mysql mysql -u empleados_user -pempleados_pass empleados_db

# Acceder a la consola de PostgreSQL
docker exec -it empleados_postgres psql -U empleados_user -d empleados_db

# Ver logs en tiempo real
docker-compose logs -f mysql
docker-compose logs -f postgres

# Reiniciar un servicio específico
docker-compose restart mysql
docker-compose restart postgres
```

## 📁 Estructura del Proyecto

```
TP5/
├── src/
│   ├── main/
│   │   ├── java/um/prog2/TP5/
│   │   │   ├── controller/          # Controladores REST
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── entity/              # Entidades JPA
│   │   │   ├── exception/           # Manejo de excepciones
│   │   │   ├── repository/          # Repositorios JPA
│   │   │   ├── service/             # Lógica de negocio
│   │   │   ├── validation/          # Validaciones personalizadas
│   │   │   └── Tp5Application.java  # Clase principal
│   │   └── resources/
│   │       └── application.yml      # Configuración de la aplicación
│   └── test/
│       ├── java/                    # Tests unitarios e integración
│       └── resources/               # Recursos para tests
├── docker-compose.yml               # Configuración de Docker
├── pom.xml                          # Configuración de Maven
└── README.md                        # Este archivo
```

## 🔧 Configuración de application.yml

El archivo `application.yml` contiene tres perfiles:

- **dev**: H2 en memoria (sin Docker)
- **mysql**: MySQL con Docker Compose
- **postgres**: PostgreSQL con Docker Compose

Puedes cambiar el perfil activo editando la propiedad:

```yaml
spring:
  profiles:
    active: dev  # Cambiar a: mysql o postgres
```

## ✅ Validaciones

El sistema implementa validaciones automáticas:

### Empleado
- Nombre: requerido, no vacío
- Email: requerido, formato válido, único
- Salario: positivo
- Departamento: requerido

### Departamento
- Nombre: requerido, único
- Presupuesto: positivo

### Proyecto
- Nombre: requerido
- Descripción: opcional
- Fechas: formato válido
- Activo: booleano

## 🚨 Manejo de Errores

La API devuelve respuestas estructuradas para errores:

```json
{
  "timestamp": "2025-01-24T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Empleado no encontrado con id: 999",
  "path": "/api/empleados/999"
}
```

Códigos de estado HTTP:
- `200 OK`: Operación exitosa
- `201 Created`: Recurso creado
- `204 No Content`: Eliminación exitosa
- `400 Bad Request`: Error de validación
- `404 Not Found`: Recurso no encontrado
- `500 Internal Server Error`: Error del servidor

## 📊 Base de Datos

### Modelo de Datos

```
Departamento (1) -----> (*) Empleado
     |                       |
     |                       |
     |                  (*) --- (*) Proyecto
```

**Relaciones:**
- Un Departamento tiene muchos Empleados (OneToMany)
- Un Empleado pertenece a un Departamento (ManyToOne)
- Empleados y Proyectos tienen relación Many-to-Many

## 🧪 Testing

El proyecto incluye:

- **Tests Unitarios**: Servicios, repositorios y controladores
- **Tests de Integración**: Pruebas end-to-end con Testcontainers
- **Tests de Validación**: Validaciones de Bean Validation

```bash
# Ejecutar todos los tests
mvn test

# Ver reporte de cobertura
mvn test jacoco:report
```

## 📝 Licencia

Este proyecto es parte de un trabajo práctico académico.

## 👥 Autores

- Desarrollo: Tomás Bourguet
- Institución: Universidad de Mendoza

## 📞 Soporte

Para preguntas o problemas:
- Crear un issue en el repositorio
- Contactar al equipo docente

