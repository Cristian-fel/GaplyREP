# Gaply

Aplicación móvil universitaria desarrollada en el curso de **Programación de
Aplicaciones Móviles**. Gaply organiza el horario del estudiante, detecta sus
huecos libres, le sugiere actividades para ese tiempo y permite publicar
actividades abiertas al resto de la comunidad del campus.


## Módulos implementados

- **Módulo 1 — Autenticación:** bienvenida, inicio de sesión, registro en 5
  pasos (cuenta, datos personales, propósito, intereses, foto y bio) y
  recuperación de contraseña con Firebase Authentication.
- **Módulo 2 — Perfil y horario:** perfil con foto (Firebase Storage), edición
  en 3 pasos, "Mi horario" con clases por día, edición y eliminación, cálculo
  de huecos libres y pantalla de sugerencias ("Mis huecos").
- **Configuración:** cuenta y seguridad (cambiar contraseña, desactivar y
  eliminar cuenta), preferencias (tema, notificaciones, idioma), acerca de la
  app, calificación con estrellas y cerrar sesión.
- **Actividades:** crear, explorar, editar y eliminar actividades; detalle con
  asistentes y opción de unirse; pestaña "Tus actividades". Se persisten en la
  colección `activities` de Firestore.


## Pantallas

Welcome · Login · Recuperar contraseña · Registro · Home (Descubrir / Matches /
Chats / Perfil) · Editar perfil (3 pasos) · Mi horario · Formulario de clase ·
Mis huecos · Configuración · Cambiar contraseña · Desactivar / Eliminar cuenta ·
Idioma · Calificar la app · Acerca de · Crear actividad · Lista de actividades ·
Detalle de actividad · Tus actividades.


## Tecnologías

- Kotlin 2.2 + Jetpack Compose (Material 3) y Navigation Compose
- Firebase: Authentication, Cloud Firestore, Storage
- Coil para carga de imágenes
- Arquitectura: MVVM + repositorios (`domain/` → `data/` → `ui/`),
  inyección de dependencias manual en `di/AppContainer`
- Sistema propio de i18n en `core/i18n`: 8 idiomas (ES, EN, FR, DE, IT, PT,
  JA, ZH); Configuración muestra Español e Inglés por ahora
- Pruebas unitarias JUnit en JVM (`app/src/test`) para el agente, el i18n y
  los ViewModels de configuración


## Estructura de datos (Firestore)

```
users/{uid}
├── name, username, email, photoUrl, receiveMarketing   (perfil)
├── schedule: {                                         (horario)
│     <classId>: { title, day, startDate, endDate,
│                  startHour, endHour, note, color }
│   }
└── rating: { stars, comment, createdAt, id }           (calificación de la app)

activities/{activityId}
├── title, category, duration, date, timeRange,
│   startTime, location, description
├── imageUri, defaultColorHex, organizerInitial         (imagen o color)
├── ownerId, ownerName, createdAt                       (quién publicó)
└── attendees[], attendeeIds[]                          (asistentes)
```

El horario y la calificación se guardan como **campos** del documento del
usuario con `SetOptions.merge()` (las reglas de Firestore cubren
`users/{uid}`) y se eliminan con `FieldValue.delete()`. La colección
`activities` requiere reglas propias: lectura autenticada y escritura solo
del dueño (`ownerId`), verificado en el repositorio.


## Sugerencias de actividades (agente)

La pantalla "Mis huecos" muestra actividades que caben en un hueco libre del
horario según los intereses del usuario. El agente aplica una función de
utilidad (0.65 intereses + 0.35 horario) con ciclo percepción → decisión →
acción; la lógica está aislada en
`app/src/main/java/com/gaply/app/domain/agent/` y es puro Kotlin con tests.


## Compilar

1. Android Studio (JDK 17+, SDK 34; AGP 9.3.0 y Gradle 9.6 con wrapper).
2. Abrir el proyecto y dejar sincronizar Gradle.
3. Ejecutar en emulador o dispositivo (`Run ▶`), o por terminal:

```bash
gradlew.bat :app:assembleDebug      # compilar APK debug
gradlew.bat :app:installDebug       # instalar en el dispositivo conectado
gradlew.bat :app:testDebugUnitTest  # correr las pruebas unitarias
```


## Próximos pasos

- Navegación asistida con realidad aumentada hacia la ubicación de cada
  actividad (cámara + GPS + brújula).
- Matches y Chats (pestañas actualmente en construcción).
