# GaplyREP

Aplicación móvil universitaria desarrollada en el curso de **Programación de
Aplicaciones Móviles**. Gaply organiza el horario del estudiante, detecta sus
huecos libres y le sugiere actividades para ese tiempo.

## Módulos implementados

- **Módulo 1 — Autenticación:** bienvenida, inicio de sesión, registro y recuperación
  de contraseña con Firebase Authentication.
- **Módulo 2 — Perfil y horario:** perfil con foto (Firebase Storage), edición en
  3 pasos, "Mi horario" con clases por día, edición y eliminación, cálculo de huecos
  libres y pantalla de sugerencias ("Mis huecos").

## Pantallas

Welcome · Login · Recuperar contraseña · Registro · Home (Descubrir / Matches /
Chats / Perfil) · Editar perfil (3 pasos) · Mi horario · Formulario de clase ·
Mis huecos (sugerencias para un hueco libre).

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

## Sugerencias de actividades

La pantalla "Mis huecos" muestra actividades que caben en un hueco libre del
horario según los intereses del usuario. La lógica está aislada en
`app/src/main/java/com/gaply/app/domain/agent/`.

## Compilar

1. Android Studio (JDK 21, SDK 35+).
2. Abrir el proyecto y dejar sincronizar Gradle.
3. Ejecutar en emulador o dispositivo (`Run ▶`), o por terminal:
   `gradlew.bat :app:assembleDebug`.
