# Prompts de Explicacion Tecnica de Codigo

## Prompt 1: Jerarquias de Carpetas para Proyectos

### Prompt Utilizado: 

Cuales eran las distintas formas de administrar un proyecto? Estoy hablando de jerarquias de carpetas. Cual me recomiendas?

### Respuesta Recibida:

Existen varias formas de organizar la estructura de carpetas en un proyecto Spring Boot:

### 1. Estructura por capas (tradicional)
```
src/main/java/um/prog2/TP5/
├── controller/
├── service/
├── repository/
├── entity/      (donde está tu Proyecto.java)
├── dto/
├── exception/
└── config/
```

### 2. Estructura por funcionalidades
```
src/main/java/um/prog2/TP5/
├── proyecto/
│   ├── ProyectoController.java
│   ├── ProyectoService.java
│   ├── ProyectoRepository.java
│   └── Proyecto.java
├── empleado/
└── common/
```

### 3. Estructura hexagonal
```
src/main/java/um/prog2/TP5/
├── domain/
├── application/
│   ├── port/
│   └── service/
└── adapter/
    ├── persistence/
    └── web/
```

### 4. Estructura DDD
```
src/main/java/um/prog2/TP5/
├── domain/
├── application/
├── infrastructure/
└── interfaces/
```

### Recomendación
Considerando tu código actual, recomendaría seguir con la estructura por capas que ya has comenzado. Es clara, intuitiva y ampliamente utilizada en proyectos Spring Boot. Para proyectos educativos o de tamaño mediano, esta estructura ofrece buena separación de responsabilidades y es fácil de mantener.

Si el proyecto crece significativamente, podrías considerar migrar hacia una estructura por funcionalidades que facilita la comprensión y mantenimiento de cada módulo.

### Modificaciones Realizadas:
- Dicho prompt no realiza modificaciones en el código, pero sugiere una estructura de carpetas para organizar mejor el proyecto.

### Explicación del Prompt:
- El prompt solicita diferentes formas de organizar la estructura de carpetas en un proyecto Spring Boot, proporcionando ejemplos claros y recomendando una estructura adecuada según el contexto del proyecto. Esta siempre es una pratica que me resulta util saber, aunque al suelo preguntar seguido.

### Aprendizajes Obtenidos:
- Existen varias formas de organizar la estructura de carpetas en un proyecto Spring Boot, cada una con sus ventajas y desventajas.
- La estructura por capas es una opción sólida para proyectos educativos o de tamaño mediano, ofreciendo buena separación de responsabilidades.
- A medida que un proyecto crece, puede ser beneficioso considerar estructuras por funcionalidades para mejorar la mantenibilidad y comprensión del código.

---

## Prompt 2: Metodos en Repositorios

### Prompt Utilizado:

Porque el metodo findAverageSalarioByDepartamento, que necesita datos de otras entidades esta definido en el repositorio de empleado? No tendria sentido definirlo en el repositorio de departamento? (En el ejemplo de implementacion Etapa 2 esta asi, README.md)

### Respuesta Recibida:

# Ubicación de métodos de consulta en repositorios Spring Data

Es una excelente pregunta que toca un tema de diseño importante en aplicaciones Spring Data JPA.

El método `findAverageSalarioByDepartamento` está definido en `EmpleadoRepository` y no en `DepartamentoRepository` por estas razones:

1. **Entidad principal de la consulta**: Aunque filtramos por departamento, la consulta JPQL opera principalmente sobre la entidad `Empleado`:
   ```sql
   SELECT AVG(e.salario) FROM Empleado e WHERE e.departamento.id = :departamentoId
   ```

2. **Acceso a datos**: La tabla principal que se consulta es la de empleados, donde está almacenado el campo `salario`.

3. **Consistencia con operaciones de agregación**: Las operaciones de agregación (AVG, SUM, COUNT) típicamente se colocan en el repositorio de la entidad sobre la que se calculan.

Sin embargo, sería perfectamente válido moverlo a `DepartamentoRepository` si:
- Conceptualmente tiene más sentido en tu dominio
- Estás agrupando todas las estadísticas relacionadas con departamentos
- El equipo considera que es más intuitivo encontrarlo allí

Ambos enfoques son correctos, pero es importante mantener consistencia en toda la aplicación con el patrón que elijas.

### Modificaciones Realizadas:
- No se realizaron modificaciones en el código, pero se aclaró la razón detrás de la ubicación del método en el repositorio de `Empleado`.

### Explicación del Prompt:
- El prompt busca entender la lógica detrás de la ubicación de un método específico en un repositorio, lo que es crucial para el diseño y mantenimiento del código. Esta es una duda que me surgio al leer el README.md del proyecto, y me parecio interesante aclararla.

### Aprendizajes Obtenidos:
- La ubicación de métodos de consulta en repositorios debe basarse en la entidad principal de la consulta y la consistencia con las operaciones de agregación.
- Es importante considerar la lógica del dominio y la intuición del equipo al decidir dónde colocar ciertos métodos.
- Mantener consistencia en la estructura del código es clave para la mantenibilidad a largo plazo.

---

## Prompt 3: Queries en Repositorios JPA

### Prompt Utilizado:

Cuando es necesario definir una query para una funcion en un repositorio JPA?

### Respuesta Recibida:

# Cuándo definir consultas personalizadas en repositorios JPA

Es necesario definir una query personalizada con `@Query` en los siguientes casos:

1. **Consultas complejas** que no pueden expresarse fácilmente mediante la convención de nombres:
   ```java
   @Query("SELECT e FROM Empleado e WHERE e.departamento.nombre = :nombreDepartamento")
   List<Empleado> findByNombreDepartamento(@Param("nombreDepartamento") String nombreDepartamento);
   ```

2. **Operaciones de agregación** (COUNT, SUM, AVG, MIN, MAX):
   ```java
   @Query("SELECT AVG(e.salario) FROM Empleado e WHERE e.departamento.id = :departamentoId")
   Optional<BigDecimal> findAverageSalarioByDepartamento(@Param("departamentoId") Long departamentoId);
   ```

3. **Joins complejos** entre múltiples entidades

4. **Proyecciones específicas** cuando necesitas solo ciertos campos

5. **Consultas con subconsultas**

6. **Consultas nativas SQL** usando `nativeQuery = true` cuando necesitas funcionalidad específica de la base de datos

Los métodos derivados (como `findByEmail`, `findBySalarioBetween`) son suficientes para consultas simples y directas, pero para lógica más compleja, es mejor usar `@Query`.

En tu repositorio actual, las queries personalizadas están correctamente aplicadas para casos que superan las capacidades de la derivación de métodos.

### Modificaciones Realizadas:
- No se realizaron modificaciones en el código, pero se proporcionó una guía clara sobre cuándo es apropiado usar consultas personalizadas en repositorios JPA.

### Explicación del Prompt:
- El prompt busca entender los escenarios en los que es necesario definir consultas personalizadas en repositorios JPA, lo cual es fundamental para el diseño eficiente de la capa de acceso a datos. Esta es una duda que me surgio al leer el README.md del proyecto.

### Aprendizajes Obtenidos:
- Las consultas personalizadas son necesarias para manejar casos complejos que no pueden ser resueltos mediante la convención de nombres.
- Es importante evaluar la complejidad de la consulta y la claridad del código al decidir entre métodos derivados y consultas personalizadas.
- Mantener un equilibrio entre simplicidad y funcionalidad es clave
---

## Prompt 4: Diversas dudas sobre repositorios JPA

### Prompt Utilizado:

Me interesa fincByDepartamento. Porque no es necesario escribirle una Query? Es una buena practica pasarle como parametro la entidad en vez de la llave de dicha entidad?

### Respuesta Recibida:

# Sobre findByDepartamento en repositorios JPA

No es necesario escribir una `@Query` explícita para `findByDepartamento` porque Spring Data JPA utiliza convenciones de nomenclatura para generar consultas automáticamente.

Cuando escribes un método como `findByDepartamento(Departamento departamento)`, Spring Data JPA genera una consulta JPQL equivalente a:
```jpql
SELECT e FROM Empleado e WHERE e.departamento = :departamento
```

## Sobre pasar la entidad vs. la clave primaria

Ambos enfoques son válidos, pero con distintos propósitos:

**Usar la entidad completa (actual)**:
```java
List<Empleado> findByDepartamento(Departamento departamento);
```
- Ventajas: Más expresivo y semántico
- Uso ideal: Cuando ya tienes la entidad Departamento cargada

**Usar solo el ID**:
```java
List<Empleado> findByDepartamentoId(Long departamentoId);
```
- Ventajas: Más eficiente cuando solo tienes el ID
- Uso ideal: Cuando no necesitas cargar el departamento completo

La elección depende de tu caso de uso específico. Si habitualmente accedes a los empleados a partir de un departamento ya cargado, el enfoque actual es adecuado.

### Modificaciones Realizadas:
- No se realizaron modificaciones en el código, pero se aclararon las razones detrás de la ausencia de una consulta explícita y las consideraciones al elegir entre pasar una entidad completa o solo su clave primaria.

### Explicación del Prompt:
- El prompt busca entender por qué no es necesario definir una consulta explícita para un método específico en un repositorio JPA y las implicaciones de pasar una entidad completa versus solo su clave primaria. 

### Aprendizajes Obtenidos:
- Spring Data JPA puede generar consultas automáticamente basándose en convenciones de nomenclatura, eliminando la necesidad de consultas explícitas en muchos casos.
- La elección entre pasar una entidad completa o solo su clave primaria depende del contexto y los requisitos de la aplicación.
- Considerar la eficiencia y la claridad del código al diseñar métodos en repositorios es crucial para un buen diseño de software.

---

## Prompt 5: Comandos Maven

### Prompt Utilizado:

como eran los comandos mvn para correr el sistema y probar apps, y demas comandos basicos?

### Respuesta Recibida:

# Comandos básicos de Maven

Maven es una herramienta para gestionar y construir proyectos Java. Aquí están los comandos más útiles:

## Comandos básicos

```bash
mvn clean             # Elimina la carpeta target/ y archivos compilados
mvn compile           # Compila el código fuente
mvn test              # Ejecuta todos los tests
mvn package           # Compila y empaqueta la aplicación (genera JAR/WAR)
mvn install           # Instala el paquete en el repositorio local
```

## Comandos combinados

```bash
mvn clean install     # Limpia y luego instala
mvn clean test        # Limpia y ejecuta tests
mvn clean package     # Limpia y empaqueta
```

## Spring Boot

```bash
mvn spring-boot:run   # Ejecuta la aplicación Spring Boot
mvn spring-boot:run -Dspring-boot.run.profiles=test  # Ejecuta con perfil específico
```

## Tests específicos

```bash
mvn test -Dtest=EmpleadoRepositoryTest           # Ejecuta una clase de test específica
mvn test -Dtest=EmpleadoRepositoryTest#findByEmail_empleadoExistente_deberiaRetornarEmpleado  # Ejecuta un método específico
```

## Otras utilidades

```bash
mvn dependency:tree   # Muestra el árbol de dependencias
mvn help:effective-pom  # Muestra el POM efectivo
```

Estos comandos se ejecutan desde la terminal en el directorio raíz del proyecto donde se encuentra el archivo `pom.xml`.

### Modificaciones Realizadas:
- No se realizaron modificaciones en el código, pero se proporcionó una lista clara y concisa de comandos Maven esenciales para la gestión y construcción de proyectos Java.

### Explicación del Prompt:
- El prompt busca obtener una referencia rápida de los comandos Maven más comunes y útiles para gestionar proyectos Java, especialmente en el contexto de Spring Boot. Esta es una duda que me surgio al leer el README.md del proyecto, y me parecio interesante aclararla.

### Aprendizajes Obtenidos:
- Familiarización con los comandos básicos de Maven para compilar, probar y empaquetar proyectos Java.
- Comprensión de comandos específicos para ejecutar aplicaciones Spring Boot y tests.

---

## Prompt 6:

### Prompt Utilizado:



### Respuesta Recibida:



### Modificaciones Realizadas:
- [Lista de cambios realizados al código generado]
- [Explicación de por qué se modificó]

### Explicación del Prompt:
[Explicar por qué se usó este prompt específico]

### Aprendizajes Obtenidos:
- [Lista de aprendizajes]
- [Conceptos nuevos comprendidos]
- [Buenas prácticas identificadas]

---
