# UCHoteles

Aplicación Jakarta EE para gestionar la reserva de habitaciones en una cadena hotelera.
Práctica 3 de Procesos de Ingeniería de Software (4º Grado en Ingeniería Informática, Universidad de Cantabria, curso 2026/27).

## Módulos Maven

| Módulo | Empaquetado | Contenido |
|---|---|---|
| `UCHotelesCommon` | `UCHotelesCommon.jar` (en `lib/` del EAR) | Entidades del dominio, DTO, interfaces de negocio y DAO, excepciones |
| `UCHotelesDAO` | `UCHotelesDAO.jar` (EJB) | `HotelesDAO`, `ReservasDAO` (JPA) |
| `UCHotelesBusiness` | `UCHotelesBusiness.jar` (EJB) | `GestionReservas`, `GestionHoteles` |
| `UCHotelesEAR` | `UCHoteles.ear` | Empaquetado de todo lo anterior (Figura 5 de la especificación) |
| `UCHotelesWeb` | `UCHotelesWeb.war` | Práctica 3.4 (pendiente) |

Paquetes según el diseño de la práctica 3.1:

- `es.unican.ps.uchoteles.common.domain`, `.common.businessLayer`, `.common.daoLayer`, `.common.exceptions`
- `es.unican.ps.uchoteles.businessLayer`
- `es.unican.ps.uchoteles.daoLayer`

## Estado

- **3.2 Capa de negocio**: implementada, con pruebas unitarias (JUnit 5 + Mockito) que aíslan la capa DAO y el `TimerService`.
- **3.3 Capa DAO**: hay una primera versión de los DAO para poder desplegar el EAR. Usa la fuente de datos por defecto de GlassFish (`java:comp/DefaultDataSource`); en la 3.3 se pasará a MySQL y se añadirán sus pruebas.
- 3.4 Web y 3.5 Seguridad: pendientes.

## Compilar y probar

Requisitos: JDK 17 o superior y Maven 3.9.

```bash
mvn clean verify
```

Ejecuta las pruebas unitarias y genera `UCHotelesEAR/target/UCHoteles.ear`.

## Desplegar en GlassFish 7

```bash
asadmin start-database      # Derby/H2 de GlassFish, usada por DefaultDataSource
asadmin start-domain
asadmin deploy UCHotelesEAR/target/UCHoteles.ear
```

O desde la consola de administración (http://localhost:4848) › Applications › Deploy.

## Integración continua

`.github/workflows/maven.yml` ejecuta `mvn -B verify` en cada push y pull request a `main` y guarda el `UCHoteles.ear` como artefacto de la ejecución.
