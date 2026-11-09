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
- **Módulo 3 - Configuración:** cuenta y seguridad (cambiar contraseña, desactivar y
  eliminar cuenta), preferencias (tema, notificaciones, idioma), acerca de la
  app, calificación con estrellas y cerrar sesión.
- **Módulo 4 - Actividades:** crear, explorar, editar y eliminar actividades; detalle con
  asistentes y opción de unirse; pestaña "Tus actividades". Se persisten en la
  colección `activities` de Firestore.
- **Módulo 5 - Matches y Chats:** mazo de swipe con perfiles (PASS / LIKE / SUPER)
  y "Flash Activities" (unirse o pasar), detección de match mutuo 100% en el
  cliente, diálogo "¡Es un match!" con acceso al chat, diálogo "¡Ya estás
  dentro!" al unirte a una actividad, sección **Mis matches** + badge de la
  campana en Descubrir, lista de chats en tiempo real con no leídos y
  conversaciones de texto (directas y de grupo).
- **Módulo 6 - Radar AR:** navegación asistida con realidad aumentada hacia la
  ubicación de cada actividad. Cámara en vivo (CameraX) como fondo, brújula
  (sensor de rotación / acelerómetro + magnetómetro) y GPS (`FusedLocationProvider`)
  para calcular distancia y rumbo; overlay con flecha direccional, tarjeta de
  destino dentro del campo de visión ±35°, flecha de borde para girar y
  mini-radar. Se entra desde el detalle de actividad con el botón "Cómo llegar".
  Permisos de cámara y ubicación. Catálogo de puntos del campus
  (`CampusLocations`, 11 lugares de la sede Bogotá) para resolver el nombre de
  la actividad a coordenadas con fallback al centro del campus.


## Pantallas

Welcome · Login · Recuperar contraseña · Registro · Home (Descubrir / Matches /
Chats / Perfil) · Editar perfil (3 pasos) · Mi horario · Formulario de clase ·
Mis huecos · Configuración · Cambiar contraseña · Desactivar / Eliminar cuenta ·
Idioma · Calificar la app · Acerca de · Crear actividad · Lista de actividades ·
Detalle de actividad · Tus actividades · Detalle de chat · Radar AR.


## Tecnologías

- Kotlin 2.2 + Jetpack Compose (Material 3) y Navigation Compose
- Firebase: Authentication, Cloud Firestore, Storage
- Coil para carga de imágenes
- CameraX (preview de cámara) + Play Services Location (GPS) para el Radar AR
- Arquitectura: MVVM + repositorios (`domain/` → `data/` → `ui/`),
  inyección de dependencias manual en `di/AppContainer`
- Sistema propio de i18n en `core/i18n`: 8 idiomas (ES, EN, FR, DE, IT, PT,
  JA, ZH); Configuración muestra Español e Inglés por ahora
- Pruebas unitarias JUnit en JVM (`app/src/test`) para los agentes (recomendación
  y matches), el i18n, los ViewModels de configuración, el catálogo de
  ubicaciones del campus y el destino inicial de navegación


## Estructura de datos (Firestore)

```
users/{uid}
├── name, username, email, photoUrl, receiveMarketing   (perfil)
├── schedule: {                                         (horario)
│     <classId>: { title, day, startDate, endDate,
│                  startHour, endHour, note, color }
│   }
└── rating: { stars, comment, createdAt, id }           (calificación de la app)

swipes/{swipeId}                                        (swipes del Módulo 5)
    id = swipe_{fromUid}_{targetId}
    fromUid, toId, targetId,
    kind: "PROFILE" | "ACTIVITY",
    action: "LIKE" | "PASS" | "SUPER", createdAt

activities/{activityId}
├── title, category, duration, date, timeRange,
│   startTime, location, description
├── imageUri, defaultColorHex, organizerInitial         (imagen o color)
├── ownerId, ownerName, createdAt                       (quién publicó)
└── attendees[], attendeeIds[]                          (asistentes)

chats/{chatId}
├── type: "DIRECT" | "GROUP", members[], activityId?      (solo GROUP)
├── title, photoUrl, lastMessage, updatedAt
├── unreadCounts: { uid: Number }        (no leídos; el badge del Home)
└── lastReadAt: { uid: millis }          (última vez que cada uid lo abrió)
      └── messages/{messageId}
            senderId, senderName, text, timestamp
```

Un chat directo es exactamente un match: la sección **Mis matches** del
Descubrir y el badge de la campana salen de esta colección (`type == DIRECT`,
no leídos + los que nunca abriste).

Los ids de chat son determinísticos para que dos clientes nunca lo dupliquen:
`direct_{uidA}_{uidB}` (uids ordenados) y `act_{activityId}`. El id del swipe
también lo es (`swipe_{fromUid}_{targetId}`), así repetir un swipe sobrescribe.

Los nombres de campo **`members`** (chats) y **`fromUid`/`toId`** (swipes) los
fija `firestore.rules`: el modelo Kotlin usa `participants`/`targetId` y la
traducción ocurre solo en `ChatMappers` y `MatchMappers`. Renombrarlos en un
lado sin el otro rompe lecturas y escrituras.

El horario y la calificación se guardan como **campos** del documento del
usuario con `SetOptions.merge()` y se eliminan con `FieldValue.delete()`. El
perfil **nunca** escribe credenciales en Firestore: `passwordHash` se quitó de
`UserMappers.toFirestoreMap()` (las contraseñas viven en Firebase Auth) — es
obligatorio porque las reglas dejan `users/{uid}` con lectura pública.


## Reglas de Firestore y Storage (Módulo 5)

Los dos archivos viven en la raíz y son **el contrato** que cumple el código:

- [`firestore.rules`](firestore.rules) → Firestore (texto)
- [`storage.rules`](storage.rules) → Storage (imágenes)

| Ruta (Firestore) | Lectura | Escritura |
| --- | --- | --- |
| `users/{uid}` | pública | crear/actualizar solo el dueño; borrar no |
| `activities/{id}` | autenticado | crear con `ownerId` propio, actualizar autenticado, borrar solo el dueño |
| `swipes/{id}` | lo mío (`fromUid`) o lo dirigido a mí (`toId`) | solo quien hace el swipe |
| `matches/{id}` | miembros del campo `members` | crear solo miembro |
| `chats/{chatId}` | miembro de `members` | crear/actualizar/borrar miembro (o quien acaba de entrar) |
| `chats/{chatId}/messages` | miembro del chat padre | solo crear, con `senderId == yo` |

| Ruta (Storage) | Lectura | Escritura |
| --- | --- | --- |
| `users/{uid}/*` (foto de perfil) | autenticado | solo el dueño |
| `activities/{uid}/*` (foto del lugar) | autenticado | solo el dueño |
| `chat_images/{chatId}/*` | autenticado | autenticado, máx. 5 MB (futuras imágenes del chat) |

Publicación: `firebase deploy --only firestore:rules,storage` (o pegando cada
archivo en su consola: Firestore → Reglas, Storage → Reglas). El
[`firebase.json`](firebase.json) apunta a ambos por si usas la CLI.

La lista de chats usa
`where("members", array-contains, miUid).orderBy("updatedAt", desc).limit(30)`,
lo que requiere un **índice compuesto** en `chats`
(`members` tipo matriz + `updatedAt` DESC). Firestore resuelve con un enlace
en el error `FAILED_PRECONDITION`; si aún no existe, la app cae a la
consulta sin `orderBy` y ordena en el cliente para no quedarse sin pantalla.
Los swipes usan solo filtros de igualdad (`fromUid == yo` y
`toId == yo && fromUid == otro`), que no piden índice compuesto.


## Sugerencias de actividades (agente)

La pantalla "Mis huecos" muestra actividades que caben en un hueco libre del
horario según los intereses del usuario. El agente aplica una función de
utilidad (0.65 intereses + 0.35 horario) con ciclo percepción → decisión →
acción; la lógica está aislada en
`app/src/main/java/com/gaply/app/domain/agent/` y es puro Kotlin con tests.

## Agente de matches

`domain/agent/AgenteMatches.kt` sigue el mismo ciclo percepción → decisión →
acción con su propia función de utilidad para perfiles:

```
utilidad = 0.55 × compatibilidadDeIntereses
         + 0.30 × solapeDeHuecosLibres
         + 0.15 × coincidenciaUniversidad
```

(Pesos de `PesosUtilidadMatches.CON_UNIVERSIDAD_Y_META`; la variante por defecto
es 0.60 intereses + 0.40 horario, y el eje `meta` queda en 0.) La percepción
solo descarta perfiles que **teniendo horario** no comparten ni un minuto con el
usuario (motivo vía i18n); si falta el horario de alguno de los dos no hay
evidencia de incompatibilidad, el perfil entra al mazo y el eje de tiempo
puntúa 0 (mandan los intereses). La decisión puntúa 0-100 con razones
explicables y la acción devuelve el top 5. Los huecos libres de cada candidato
salen de `users/{uid}.schedule` con la misma lógica de "Mis huecos"
(`huecosDelHorario`). Sin candidatos la pestaña muestra
"Tus matches aparecerán aquí"; con el mazo recorrido, "No tenemos más
sugerencias por el momento".

Cuando `usarRefinamientoIa` está activo, la compatibilidad de intereses usa
`RefinadorIaMatches` (`AgenteLlmRefinador`): una matriz de similitud semántica
entrenada sobre los 15 intereses oficiales (coseno discreto en [0, 1],
simétrica entre ambos perfiles) que permite pares afines aunque no coincidan
exactamente (p. ej. Programación ↔ Ciencia = 0.75), más la generación de un
prompt estructurado para un LLM (Gemini / OpenAI). El umbral de afinidad es
0.50.

Las cartas de actividad rápida del mazo son las **futuras** (incluidas las
propias), ordenadas por fecha más cercana; si no hay ninguna futura se cae a
todas por fecha de publicación. Un fallo de Firestore en una fuente no vacía el
mazo: se cargan perfiles y actividades de forma independiente y el error se
muestra (y se registra en Logcat con el tag `Gaply`) sin dejar la pestaña muda.


## Compilar

1. Android Studio (JDK 17+, SDK 34; AGP 9.3.0 y Gradle 9.6 con wrapper).
2. Abrir el proyecto y dejar sincronizar Gradle.
3. Ejecutar en emulador o dispositivo (`Run ▶`), o por terminal:

```bash
gradlew.bat :app:assembleDebug      # compilar APK debug
gradlew.bat :app:installDebug       # instalar en el dispositivo conectado
gradlew.bat :app:testDebugUnitTest  # correr las pruebas unitarias
```
