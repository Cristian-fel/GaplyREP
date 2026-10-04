# Prompt de Desarrollo: Módulo de Autenticación, Registro y Perfil de Usuario para Gaply (Android - Kotlin)

## Contexto del Proyecto
**Gaply** es una aplicación móvil desarrollada para estudiantes universitarios con el objetivo principal de conectar alumnos en tiempo real dentro del campus (networking, estudio, actividades en huecos libres y matchmaking por intereses/horarios). 

### Reglas clave de alcance:
- En esta etapa **únicamente** nos enfocaremos en este primer módulo funcional (Autenticación, Registro, Configuración de Perfil Inicial y Home/Feed de bienvenida).
- **No anticipar** funcionalidades futuras avanzadas como el motor de algoritmos de horarios ni el sistema de chat/matchmaking directo en el código, salvo la persistencia de datos necesarios.
- Toda la información ingresada debe persistir en una **base de datos funcional local/remota** (p. ej. Room/SQLite para entorno local o Firebase Auth + Firestore/Room según la arquitectura seleccionada).

---

## Stack Tecnológico Requerido
- **Lenguaje:** Kotlin (última versión estable).
- **Entorno:** Android Studio.
- **UI:** Jetpack Compose (recomendado) o XML View Binding.
- **Arquitectura:** MVVM (Model-View-ViewModel) con Clean Architecture.
- **Persistencia de datos:** SQLite con Room local o Firebase Authentication + Firestore (debe quedar funcional el registro, inicio de sesión y almacenamiento del perfil de usuario).
- **Navegación:** Jetpack Navigation Component.

---

## Requisitos Funcionales y Flujo de Pantallas

### 1. Pantalla Inicial (Splash / Welcome)
- **Elementos UI:**
  - Logo e isotipo de Gaply con la frase: *"Encuentra algo que hacer entre tus clases"*.
  - Ilustración de bienvenida.
  - Botón principal: `Inicia Sesión`.
  - Botón secundario/outline: `Crear Cuenta`.

---

### 2. Flujo de Inicio de Sesión (Login)
- **Formulario:**
  - Campo `Usuario` (Email / Nombre de usuario).
  - Campo `Contraseña` (con opción de alternar visibilidad de contraseña: icono ojo).
  - Enlace: `¿Olvidaste tu contraseña?`.
  - Checkbox: *"Si no deseas recibir comunicaciones de marketing sobre nuestros productos y servicios, marca esta casilla."*
  - Botón: `Ingresar`.
  - Enlace a Registro: `¿Aún no tienes cuenta? Regístrate`.
- **Validaciones y Modales de Error:**
  - Validar campos vacíos o credenciales incorrectas.
  - Mostrar estado de error en rojo: `"Usuario o contraseña son incorrectos"`.
  - Pop-up / Diálogo modal de error de conexión: `"¡Error de conexión! Comprueba tu conexión, inténtalo más tarde."`
  - Pop-up / Diálogo modal de usuario inexistente: `"¡Cuenta no encontrada! No se han encontrado usuarios."`

---

### 3. Recuperación de Contraseña
- **Formulario:**
  - Campo `Nueva Contraseña` (con toggle de visibilidad).
  - Campo `Confirmar Contraseña` (con toggle de visibilidad).
  - Botón: `Guardar Contraseña`.
- **Modales de Respuesta:**
  - **Éxito:** Modal verde con icono de candado `"¡Contraseña actualizada! Tu contraseña se ha cambiado con éxito. Ya puedes iniciar sesión con tus nuevos datos."`
  - **Error:** Modal rojo con alerta `"¡Error al guardar su contraseña! Disculpa, inténtalo más tarde."`

---

### 4. Flujo de Registro de Usuario (Multi-paso)

#### Paso 1: Datos de la Cuenta
- Campos: `Usuario` (@username), `Correo` (Validar que sea institucional si aplica), `Contraseña` y `Confirmar Contraseña`.
- Botón: `Siguiente`.

#### Paso 2: Información Personal
- Campos:
  - `Nombres *` y `Apellidos`.
  - `Fecha de nacimiento`: Selectores desplegables (Dropdowns) para Mes, Día y Año.
  - `Universidad *`: Menú desplegable para seleccionar la institución universitaria.
  - `Elige tu género`: Menú desplegable de opciones.
- Botón: `Siguiente`.

#### Paso 3: Propósito / ¿Qué te trae a Gaply?
- Selección de objetivo en tarjetas (Grid / Radio Selection):
  - 📚 *Necesito estudiar*
  - 🏀 *Qué hay para hacer*
  - 😉 *Tengo tiempo libre*
  - 🥺 *Aún no lo tengo claro*
- Botón: `Siguiente` u `Omitir` (esquina superior derecha).

#### Paso 4: Selección de Intereses
- Header: `"Selecciona algunos de tus intereses"` y contador de selección (ej. `3/3`).
- Rejilla de tarjetas seleccionables con chip/check:
  - Deportes, Música, Videojuegos, Artes, Juegos de mesa, Películas, Estudio, Eventos, Baile.
- Botones: `Agregar intereses` (para añadir más) y `Siguiente` / `Omitir`.

#### Paso 5: Foto y Biografía del Perfil
- Sección superior:
  - Imagen de avatar / foto de perfil.
  - Botón para simular subir foto o cámara: `Cargar foto` / `Tomar Foto`.
  - Modales informativos al presionar "Cargar/Tomar Foto":
    - *"En la versión final, este botón abrirá la cámara de tu celular."*
    - *"En la versión final, este botón abrirá tu galería de fotos."*
- Saludo personalizado: `"Hola [Nombre del usuario]"` + `"Danos una breve descripción:"`.
- Campo de texto multi-línea (Text Area) para la descripción (biografía).
- Botón `Guardar perfil`:
  - Al guardar, dispara el modal: `"¡Tu perfil se ha guardado! La información de tu perfil se ha guardado correctamente."`
- Botón: `Siguiente` (Redirige al Home).

---

### 5. Pantalla Principal (Home / Descubrir)
- **Header:**
  - Icono de perfil de usuario con saludo: `"Bienvenido"`.
  - Icono de notificaciones.
  - Card destacada de estado actual de disponibilidad/hueco:  
    `🕒 Tienes 2 horas libres 12:00 - 2:00 p.m.`
- **Sección "Sugerencias para hoy":**
  - Galería horizontal/Grid de lugares dentro del campus:
    - 🏛️ Biblioteca
    - 🏀 Cancha M7A
    - 🏋️ Salón M...
- **Sección "Crear la actividad":**
  - Banner interactivo con botón `+`: *"Agregar tu propia actividad - Invita a otros a unirse en tu hueco"*.
- **Sección "Agenda cultural":**
  - Lista/Carousel de posters de eventos universitarios (ej. *Expo Tadeo, Dale Rumbo, Torneo de Ajedrez, El Paro*).
  - Enlace: `ir al sitio ↗`.
- **Barra de Navegación Inferior (Bottom Navigation Bar):**
  - 🔍 Descubrir (Seleccionado)
  - 👥 Matches
  - 💬 Chats
  - 👤 Perfil

---

## Requisitos Base de Datos y Modelo de Datos (Data Class)

Definir e implementar las siguientes entidades/tablas en la base de datos (Room / Firestore):

```kotlin
data class User(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val passwordHash: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: String = "", // O Long/Timestamp
    val university: String = "",
    val gender: String = "",
    val goal: String = "", // Propósito principal en la app
    val interests: List<String> = emptyList(),
    val bio: String = "",
    val profilePictureUrl: String = "",
    val receiveMarketing: Boolean = false
)