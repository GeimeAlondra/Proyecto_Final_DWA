# Salami's Restaurant

Aplicación móvil Android desarrollada para digitalizar y centralizar la gestión operativa de un restaurante en tiempo real. Permite administrar productos, mesas, personal y pedidos desde un dispositivo Android.

---

## Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Kotlin | Lenguaje de desarrollo |
| Android Studio | Entorno de desarrollo |
| Firebase Authentication | Autenticación y gestión de sesiones |
| Firebase Firestore | Base de datos en tiempo real |
| Material Design 3 | Componentes de interfaz de usuario |
| ViewBinding | Enlace seguro entre layouts y código |

---
## Arquitectura

El proyecto implementa el patrón **MVVM (Model-View-ViewModel)**:

- **Model** — clases de datos en `models/`: `Producto`, `Mesa`, `Pedido`, `Empleado`
- **ViewModel** — lógica de negocio en cada `Activity`
- **View** — layouts XML en `res/layout/`

La sincronización con Firestore se realiza mediante `addSnapshotListener`, que actualiza la interfaz automáticamente ante cualquier cambio en la base de datos.

---

## Roles del sistema

### Administrador
- Gestión completa de productos, mesas y empleados (CRUD)
- Creación de cuentas de empleados con credenciales
- Activación y desactivación de empleados
- Cambio de contraseña de empleados

### Empleado
- Registro de nuevos pedidos (mesa o para llevar)
- Seguimiento de pedidos activos
- Marcado de entrega y registro de pago
- Consulta del historial personal de pedidos

---

## Instalación

1. Clonar el repositorio
```bash
git clone https://github.com/GeimeAlondra/Proyecto_Final_DWA.git
```

2. Abrir el proyecto en **Android Studio**

3. Agregar el archivo `google-services.json` en la carpeta `app/` (obtenido desde Firebase Console)

4. Sincronizar el proyecto con Gradle
```
File → Sync Project with Gradle Files
```

5. Ejecutar la app en un dispositivo o emulador con Android 7.0 o superior

> ⚠️ El archivo `google-services.json` no está incluido en el repositorio por seguridad. Debe configurar su propio proyecto en Firebase Console y descargarlo desde ahí.

---

## Requisitos

- Android 7.0 (API 24) o superior
- Conexión a internet activa
- Android Studio Hedgehog o superior
- JDK 11

---

