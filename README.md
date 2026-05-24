# HouseHunter 🏨

## 📝 Temática del Proyecto
HouseHunter es un sistema web responsivo de **gestión hotelera orientado a eventos corporativos**. La plataforma permite centralizar la interacción entre la administración del hotel, las empresas organizadoras ( clientes que alquilan el hotel completo ) y los invitados/huéspedes. 

El sistema optimiza los flujos de control de acceso, cronogramas de actividades, asignación manual de habitaciones dobles y el registro de asistencia gamificado mediante un sistema de premios integrados.

## 👥 Equipo de Desarrollo
* **Augusto Abel Karacsany** ( Backend, Arquitectura y BdD )
* **Martin Ignacio Waltar Zunino** ( Backend y Controladores )
* **Luca Giménez Borrelli** ( Frontend & UI )

---

## 🚀 Estado Actual del Desarrollo

### 🗄️ Base de Datos e Infraestructura Core
* **Conexión Blindada:** Integración del esquema relacional definitivo `househunter.sql` enfocado en la persistencia de datos habitacionales y eventos.
* **Capa BLL ( Business Logic Layer ):** Modelos de datos y mapeo estructural empaquetados en archivos `.jar` validados en la rama principal.

### 🔐 Módulo de Autenticación ( Auth. ) & Seguridad
* **Hashing de Contraseñas:** Seguridad del lado del servidor implementada mediante algoritmo de encriptación **BCrypt** para logins seguros.
* **Control de Sesiones:** Flujos lógicos de Registro, Inicio de Sesión y Cierre de Sesión completamente funcionales según los roles de la plataforma.

### 👥 Estructura de Paneles por Roles ( Basado en Casos de Uso. )
El sistema divide su lógica y vistas en tres frentes de interacción según nuestro diagrama de arquitectura:
* **Panel de Empresa ( Cliente Corporativo ):** Gestión de reserva total del hotel, carga masiva de listas de invitados mediante plantillas ( Nombre, DNI, Celular y asignación de parejas ), envío de invitaciones masivas y consulta de reportes/feedback de rendimiento.
* **Panel de Administrador del Hotel:** Control de check-in en tiempo real validando DNI y Código Único, gestión incremental del estado de habitaciones ( **Libre, Half, Completa** ), asignación de cronogramas con categorías de actividad ( Cowork, Charlas, Recreación ) e importancia ( Baja, Media, Alta ), y toggle de asistencia.
* **Panel de Invitado / Huésped:** Interfaz simplificada ( móvil/tablet ) para confirmación de asistencia ( Y/N ), visualización de cronograma diario, consulta de número de habitación/acompañante asignado y obtención de vouchers para sorteos de premios.

### 🎨 Interfaz Gráfica ( UI )
* Maquetación responsiva orientada al control de accesos en dispositivos móviles.
* Integración de recursos visuales y assets en el Menú Principal y submenúes internos de navegación de módulos.

---

## 🛠️ Especificaciones Técnicas del Sistema ( Reglas de Negocio Clave )
* **Asignación Habitacional:** Todas las habitaciones del hotel son dobles de asignación manual previa por la Empresa organizadora.
* **Lógica Incremental de Check-in:** Las habitaciones transicionan de estado en base a la concurrencia física en la recepción: `Libre` ➡️ `Half` ( al ingresar el primer huésped ) ➡️ `Completa` ( al ingresar el compañero ).
* **Módulo de Premios:** El sistema calcula las estadísticas de asistencia de los huéspedes. Si un invitado registra asistencia perfecta a las actividades de **Alta Importancia**, el sistema lo habilita de forma automática para el sorteo de premios al final de la jornada.