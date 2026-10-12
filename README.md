# GaplyREP

Aplicación móvil universitaria desarrollada para las materias de **Programación de
Aplicaciones Móviles** e **Inteligencia Artificial**. Gaply organiza el horario del
estudiante, detecta sus huecos libres y le sugiere actividades con un **agente de
utilidad** basado en sus intereses.

## Módulos implementados

- **Módulo 1 — Autenticación:** bienvenida, inicio de sesión, registro y recuperación
  de contraseña con Firebase Authentication.
- **Módulo 2 — Perfil y horario:** perfil con foto (Firebase Storage), edición en
  3 pasos, "Mi horario" con clases por día, edición y eliminación, cálculo de huecos
  libres y sugerencias del agente de utilidad.

## Pantallas

Welcome · Login · Recuperar contraseña · Registro · Home (Descubrir / Matches /
Chats / Perfil) · Editar perfil (3 pasos) · Mi horario · Formulario de clase ·
Mis huecos (sugerencias del agente).

## Tecnologías

- Kotlin + Jetpack Compose (Material 3)
- Firebase: Authentication, Cloud Firestore, Storage
- Arquitectura: MVVM + repositorios (`domain/` → `data/` → `ui/`)

## Estructura de datos (Firestore)

```
users/{uid}
├── name, username, email, photoUrl, receiveMarketing   (perfil)
└── schedule: {                                         (horario)
      <classId>: { title, day, startDate, endDate,
                   startHour, endHour, note, color }
    }
```

El horario se guarda como **campo** del documento del usuario con
`SetOptions.merge()` (las reglas de Firestore cubren `users/{uid}`) y se elimina
con `FieldValue.delete()`.

## El agente de utilidad

En `app/src/main/java/com/gaply/app/domain/agent/` vive un agente puro de Kotlin
(modelo **PEAS**): percibe los huecos libres del horario, evalúa actividades
candidatas con una función de utilidad
`0.65 · intereses + 0.35 · encaje horario` y devuelve recomendaciones explicadas.

## Compilar

1. Android Studio (JDK 21, SDK 35+).
2. Abrir el proyecto y dejar sincronizar Gradle.
3. Ejecutar en emulador o dispositivo (`Run ▶`), o por terminal:
   `gradlew.bat :app:assembleDebug`.

## Equipo

- Iván Santiago García Ñustes
- Albeiro José Pérez Terán
- Juan Camilo Vargas Rodríguez
