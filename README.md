


# Servicio de Reportes - Sistema de Rastreo Vehicular

Sistema de gestión de reportes para rastreo vehicular construido con Spring Boot que proporciona capacidades completas de generación de reportes para dispositivos GPS. El sistema procesa datos de posición, detecta viajes y paradas de vehículos, y genera reportes detallados en múltiples formatos incluyendo exportación a Excel y entrega por correo electrónico.

## Tecnologías Utilizadas

- **Framework**: Spring Boot 3.4.4
- **Base de Datos Principal**: MySQL (base de datos `traccar`)
- **Base de Datos Secundaria**: MongoDB
- **Generación de Excel**: JXLS 2.12.0, Apache POI 5.2.3
- **Motor de Plantillas**: Apache Velocity 2.3
- **Correo Electrónico**: Spring Mail
- **Java**: 17

## Requisitos Previos

- Java 17 o superior
- Maven 3.6+
- MySQL Server
- MongoDB
- Servidor SMTP (Gmail configurado por defecto)

## Configuración de Base de Datos

### MySQL
El sistema requiere una base de datos MySQL llamada `traccar` con las siguientes tablas:
- `devices` - Información de dispositivos
- `positions` - Datos de posición GPS
- `events` - Eventos del sistema

### MongoDB
Base de datos secundaria para almacenamiento de documentos.

## Instalación y Configuración

### 1. Clonar el Repositorio
```bash
git clone https://github.com/DiegoSteven/servicioReportes.git
cd servicioReportes
```

### 2. Configurar Base de Datos
Edita `src/main/resources/application.properties`:

```properties
# Configuración MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/traccar
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña

# Configuración MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/tu_base_mongo

# Configuración de Email
spring.mail.username=tu_email@gmail.com
spring.mail.password=tu_app_password
```

### 3. Compilar y Ejecutar
```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar la aplicación
mvn spring-boot:run
```

La aplicación estará disponible en `http://localhost:8081`

## Endpoints de la API

Todos los endpoints están bajo el prefijo `/api/reports` y requieren autenticación de sesión.

### Gestión de Reportes Programados

#### Obtener Reportes del Usuario
```http
GET /api/reports
```

#### Crear Nuevo Reporte
```http
POST /api/reports
Content-Type: application/json

{
  "calendarId": 1,
  "type": "trips",
  "description": "Reporte de viajes",
  "attributes": {},
  "deviceIds": [1, 2, 3]
}
```

#### Eliminar Reporte
```http
DELETE /api/reports/{id}
```

### Reportes de Datos

Todos los endpoints de datos requieren los siguientes parámetros:
- `deviceId` (opcional): Lista de IDs de dispositivos
- `groupId` (opcional): Lista de IDs de grupos
- `from`: Fecha de inicio (formato ISO: 2023-01-01T00:00:00)
- `to`: Fecha de fin (formato ISO: 2023-01-01T23:59:59)

#### Reporte Combinado
```http
GET /api/reports/combined?deviceId=1,2&from=2023-01-01T00:00:00&to=2023-01-01T23:59:59
```

#### Reporte de Ruta
```http
GET /api/reports/route?deviceId=1&from=2023-01-01T00:00:00&to=2023-01-01T23:59:59
```

#### Reporte de Eventos
```http
GET /api/reports/events?deviceId=1&from=2023-01-01T00:00:00&to=2023-01-01T23:59:59
```

#### Reporte de Resumen
```http
GET /api/reports/summary?deviceId=1&from=2023-01-01T00:00:00&to=2023-01-01T23:59:59
```

#### Reporte de Viajes
```http
GET /api/reports/trips?deviceId=1&from=2023-01-01T00:00:00&to=2023-01-01T23:59:59
```

#### Reporte de Paradas
```http
GET /api/reports/stops?deviceId=1&from=2023-01-01T00:00:00&to=2023-01-01T23:59:59
```

### Exportación a Excel y Correo

Cada tipo de reporte soporta exportación a Excel y envío por correo:

#### Exportar a Excel
```http
GET /api/reports/{tipo}/xlsx?deviceId=1&from=2023-01-01T00:00:00&to=2023-01-01T23:59:59
```

#### Enviar por Correo
```http
GET /api/reports/{tipo}/mail?deviceId=1&from=2023-01-01T00:00:00&to=2023-01-01T23:59:59
```

Donde `{tipo}` puede ser: `route`, `events`, `summary`, `trips`, `stops`, `devices`

## Configuración Avanzada

### Límites de Período
- Período máximo: 31 días (`report.period.limit.seconds=2678400`)
- Umbral rápido: 7 días (`report.fastThreshold=604800`)

### Detección de Viajes
- Distancia mínima de viaje: 500 metros
- Duración mínima de viaje: 5 minutos
- Duración mínima de estacionamiento: 5 minutos

### Configuración de Correo
El sistema está configurado para usar Gmail SMTP:
- Servidor: smtp.gmail.com:587
- Requiere autenticación STARTTLS
- Usar contraseña de aplicación para Gmail

## Estructura del Proyecto

```
src/main/java/com/example/
├── controllers/
│   └── ReportController.java          # Controlador principal de API
├── services/                          # Servicios de negocio
├── models/                           # Modelos de datos
├── dtos/
│   └── ReportCreationRequest.java    # DTO para creación de reportes
├── Util/
│   ├── GeocoderUtil.java            # Utilidad de geocodificación
│   └── ReportUtils.java             # Utilidades de reportes
└── TraccarApplication.java          # Clase principal
```

## Desarrollo

### Ejecutar en Modo Desarrollo
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Ejecutar Pruebas
```bash
mvn test
```

## Contribución

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo LICENSE para detalles.
```

**Notes**

Este README está basado en el análisis del código fuente del proyecto. [1](#0-0)  muestra que es un proyecto Spring Boot 3.4.4, [2](#0-1)  define el controlador principal con endpoints bajo `/api/reports`, y [3](#0-2)  muestra la estructura para crear reportes. La configuración de base de datos y otros parámetros se pueden ver en las propiedades del sistema mencionadas en la documentación wiki.

Wiki pages you might want to explore:
- [Overview (DiegoSteven/servicioReportes)](/wiki/DiegoSteven/servicioReportes#1)
- [Report Generation System (DiegoSteven/servicioReportes)](/wiki/DiegoSteven/servicioReportes#5)
