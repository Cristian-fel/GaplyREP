package com.gaply.app.data.seed

import android.util.Log
import com.gaply.app.data.CampusLocations
import com.gaply.app.di.AppContainer
import com.gaply.app.domain.model.ActivityItem
import com.gaply.app.ui.screens.activities.ActivityFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId

/**
 * Semilla de datos de prueba del proyecto Gaply (QA de flujos).
 *
 * Inserta directamente en Firestore, en un solo [com.google.firebase.firestore.WriteBatch]:
 *  - `users`: 10 usuarios de prueba con id fijo `usr_01`..`usr_10`, con los
 *    MISMOS campos que escribe [com.gaply.app.data.mapper.toFirestoreMap]
 *    (sin `passwordHash`: las credenciales viven en Firebase Auth) y con el
 *    campo `schedule` (mapa { classId: {...} }) que usa la app para calcular
 *    los huecos libres.
 *  - `activities`: 20 actividades con id fijo `act_21`..`act_40` (2 por
 *    usuario; `act_01`..`act_20` ya existen en el proyecto y no se tocan),
 *    usando los MISMOS campos que escribe `FirebaseActivityRepository.aMapa`
 *    (incluye `createdAt`, obligatorio porque la lista ordena por ese campo).
 *
 * Reglas de seguridad de la semilla:
 *  - Se ejecuta al iniciar la app desde [com.gaply.app.GaplyApp]; primero
 *    espera a que Firebase Auth restaure la sesión (las reglas exigen usuario).
 *  - Antes de escribir consulta `activities/act_21`: si ya existe, NO hace nada
 *    (ni sobrescribe ni duplica datos en cada inicio).
 *  - Todo va en try/catch: si las reglas u otro fallo impiden escribir, solo
 *    deja constancia en log y la app arranca normalmente.
 *
 * Fechas: 14 de las 20 actividades se fechan con `LocalDate.now()` (hoy, en el
 * formato millis que usa el filtro `ActivityFormat.esDeFecha`) para que aparezcan
 * de inmediato en "Actividades para tu hueco"; las 6 restantes van a mañana y
 * los próximos días. El mismo contrato vive en `mock_data_gaply.json` (raíz del
 * proyecto) con las fechas de referencia.
 */
object MockDataSeeder {

    private const val TAG = "MockDataSeeder"

    private const val COLECCION_USUARIOS = "users"
    private const val COLECCION_ACTIVIDADES = "activities"

    /** Documento comodín: si existe, la semilla ya corrió y se omite todo. */
    private const val DOCUMENTO_MARCADOR = "act_21"

    private const val SESION_MAXIMA_MS = 15_000L
    private const val INTERVALO_SESION_MS = 500L

    /** Fecha del semestre para startDate/endDate de cada clase (dd/MM/yyyy). */
    private const val INICIO_SEMESTRE = "03/08/2026"
    private const val FIN_SEMESTRE = "11/12/2026"

    /**
     * Punto de entrada seguro: llama a esto al iniciar la app.
     * Idempotente, no bloquea el arranque y jamás lanza excepciones hacia afuera.
     */
    fun seedIfEmpty() {
        if (!AppContainer.USE_FIREBASE) return
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val auth = FirebaseAuth.getInstance()
                esperarSesion(auth)

                val firestore = FirebaseFirestore.getInstance()
                val marcador = firestore
                    .collection(COLECCION_ACTIVIDADES)
                    .document(DOCUMENTO_MARCADOR)
                    .get()
                    .await()
                if (marcador.exists()) {
                    Log.i(TAG, "$DOCUMENTO_MARCADOR ya existe en Firestore: se omite la semilla")
                    return@launch
                }

                val ahora = System.currentTimeMillis()
                val batch = firestore.batch()
                USUARIOS_MOCK.forEach { usuario ->
                    batch.set(
                        firestore.collection(COLECCION_USUARIOS).document(usuario.id),
                        mapaUsuario(usuario),
                    )
                }
                ACTIVIDADES_MOCK.forEachIndexed { indice, actividad ->
                    batch.set(
                        firestore.collection(COLECCION_ACTIVIDADES).document(actividad.id),
                        mapaActividad(actividad, numero = indice + 1, ahora = ahora),
                    )
                }
                batch.commit().await()
                Log.i(
                    TAG,
                    "Semilla insertada: ${USUARIOS_MOCK.size} usuarios (usr_01..usr_10) y " +
                        "${ACTIVIDADES_MOCK.size} actividades (act_21..act_40)",
                )
            } catch (e: Exception) {
                // Las reglas de Firestore exigen sesión autenticada (u otro fallo
                // puntual de red): se registra y la app sigue funcionando igual.
                Log.w(TAG, "No se pudo insertar la semilla de prueba: ${e.message}")
            }
        }
    }

    /**
     * Las reglas de Firestore piden usuario autenticado; Firebase Auth restaura
     * la sesión persistida unos milisegundos después del arranque en frío.
     * Espera a que aparezca el usuario (máximo [SESION_MAXIMA_MS]) sin bloquear.
     */
    private suspend fun esperarSesion(auth: FirebaseAuth) {
        var transcurrido = 0L
        while (auth.currentUser == null && transcurrido < SESION_MAXIMA_MS) {
            delay(INTERVALO_SESION_MS)
            transcurrido += INTERVALO_SESION_MS
        }
    }

    /** Documento `users/{id}` con los campos exactos del mapper de usuario + `schedule`. */
    private fun mapaUsuario(u: MockUsuario): Map<String, Any> = mapOf(
        "userId" to u.id,
        "username" to u.username,
        "email" to u.email,
        "firstName" to u.nombre,
        "lastName" to u.apellido,
        "birthDate" to u.nacimiento,
        "university" to u.universidad,
        "gender" to u.genero,
        "goal" to u.meta,
        "interests" to u.intereses,
        "bio" to u.bio,
        "profilePictureUrl" to u.foto,
        "receiveMarketing" to u.marketing,
        // Mismo mapa { classId: { ... } } que escribe FirebaseScheduleRepository
        // (horas en 24 h "HH:mm": la app las muestra como 12 h a.m./p.m.).
        "schedule" to u.clases.mapIndexed { indice, clase ->
            "${u.id}_clase_${indice + 1}" to mapOf(
                "title" to clase.titulo,
                "day" to clase.dia,
                "startDate" to INICIO_SEMESTRE,
                "endDate" to FIN_SEMESTRE,
                "startHour" to clase.inicio,
                "endHour" to clase.fin,
                "note" to "",
                "color" to clase.color,
            )
        }.toMap(),
    )

    /** Documento `activities/{id}` con los campos exactos de `FirebaseActivityRepository.aMapa`. */
    private fun mapaActividad(
        a: MockActividad,
        numero: Int,
        ahora: Long,
    ): Map<String, Any?> {
        val creador = USUARIOS_MOCK[a.creador]
        val duracionMinutos = ActivityFormat.duracionMinutos(a.duracion)
        // Fecha igual que la app: millis del día a las 12:00 en la zona del
        // dispositivo; `ActivityFormat.esDeFecha` la convierte a LocalDate.
        val fecha = LocalDate.now().plusDays(a.dias)
            .atTime(12, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val asistentesNombres = a.asistentes.map { indice ->
            val usuario = USUARIOS_MOCK[indice]
            "${usuario.nombre} ${usuario.apellido}"
        }
        return mapOf(
            "title" to a.titulo,
            "category" to a.categoria,
            "duration" to a.duracion,
            "date" to fecha.toString(),
            // Mismo formateador 12 h que usa la app (rango legible de la tarjeta).
            "timeRange" to ActivityFormat.rangoHora(a.hora, duracionMinutos),
            "startTime" to a.hora,
            // Ubicación dividida en 4 campos (mismos que escribe aMapa).
            "locationName" to a.lugar,
            "latitude" to (CampusLocations.find(a.lugar)?.latitude
                ?: CampusLocations.CENTER.latitude),
            "longitude" to (CampusLocations.find(a.lugar)?.longitude
                ?: CampusLocations.CENTER.longitude),
            "locationDetails" to null,
            "description" to a.descripcion,
            // null = sin foto -> la app pinta el color verde sólido por defecto.
            "imageUri" to a.foto,
            "defaultColorHex" to ActivityItem.DEFAULT_COLOR_HEX,
            "organizerInitial" to creador.nombre.take(1),
            "ownerId" to creador.id,
            "ownerName" to "${creador.nombre} ${creador.apellido}",
            "attendees" to asistentesNombres,
            "attendeeIds" to a.asistentes.map { USUARIOS_MOCK[it].id },
            // Obligatorio: la lista de Firestore ordena por createdAt DESC.
            "createdAt" to (ahora - (ACTIVIDADES_MOCK.size - numero) * 3_600_000L),
        )
    }

    /** Clase del horario de un usuario mock (formato idéntico a ScheduleClass). */
    private class MockClase(
        val titulo: String,
        val dia: String,
        val inicio: String,
        val fin: String,
        val color: String,
    )

    /** Usuario de prueba: campos de `User` + su horario semanal. */
    private class MockUsuario(
        val id: String,
        val username: String,
        val email: String,
        val nombre: String,
        val apellido: String,
        val nacimiento: String,
        val universidad: String,
        val genero: String,
        val meta: String,
        val intereses: List<String>,
        val bio: String,
        val foto: String,
        val marketing: Boolean,
        val clases: List<MockClase>,
    )

    /** Actividad de prueba (campos de `ActivityItem` persistidos en Firestore). */
    private class MockActividad(
        val id: String,
        /** Índice del creador en [USUARIOS_MOCK] (act_21 y act_22 -> usr_01). */
        val creador: Int,
        val titulo: String,
        val categoria: String,
        /** Uno de: ActivityFormat.DUR_30 / DUR_1H / DUR_2H. */
        val duracion: String,
        /** Hora de inicio en 24 h "HH:mm" (formato `startTime` de la app). */
        val hora: String,
        /** Días desde hoy: 0 = hoy (aparece en "Actividades para tu hueco"). */
        val dias: Long,
        val lugar: String,
        val descripcion: String,
        /** URL de la foto del lugar o null (tarjeta de color verde sólido). */
        val foto: String?,
        /** Índices en [USUARIOS_MOCK] que ya se unieron (asistentes). */
        val asistentes: List<Int>,
    )

    private fun retrato(id: String): String =
        "https://images.unsplash.com/photo-$id?auto=format&fit=crop&w=400&q=60"

    private fun lugar(id: String): String =
        "https://images.unsplash.com/photo-$id?auto=format&fit=crop&w=800&q=70"

    // =================================================================
    // 10 USUARIOS (usr_01..usr_10) — 3 intereses de la lista oficial de 15
    // =================================================================
    private val USUARIOS_MOCK = listOf(
        MockUsuario(
            id = "usr_01",
            username = "valentina.rojas",
            email = "valentina.rojas@tadeo.co",
            nombre = "Valentina",
            apellido = "Rojas Camargo",
            nacimiento = "14/03/2004",
            universidad = "Universidad Jorge Tadeo Lozano",
            genero = "Femenino",
            meta = "📚 Necesito estudiar",
            intereses = listOf("Música", "Fotografía", "Baile"),
            bio = "Diseño Gráfico · 5 semestre. Me encanta sacar fotos y bailar salsa los viernes.",
            foto = retrato("1494790108377-be9c29b29330"),
            marketing = true,
            clases = listOf(
                MockClase("Fundamentos de Diseño", "Lunes", "08:00", "10:00", "#F2A9E8"),
                MockClase("Taller de Color", "Lunes", "11:00", "13:00", "#C469AC"),
                MockClase("Historia del Arte", "Miércoles", "10:00", "12:00", "#A88BD3"),
                MockClase("Fotografía I", "Miércoles", "13:00", "15:00", "#5CB9DD"),
                MockClase("Diseño Editorial", "Viernes", "09:00", "11:00", "#DCB040"),
                MockClase("Taller Abierto", "Viernes", "12:00", "14:00", "#F0A96B"),
                MockClase("Proyecto Final", "Domingo", "10:00", "12:00", "#51A391"),
                MockClase("Portafolio", "Domingo", "13:00", "15:00", "#82B192"),
            ),
        ),
        MockUsuario(
            id = "usr_02",
            username = "andres.gomez",
            email = "andres.gomez@tadeo.co",
            nombre = "Andrés Felipe",
            apellido = "Gómez",
            nacimiento = "02/11/2003",
            universidad = "Universidad Jorge Tadeo Lozano",
            genero = "Masculino",
            meta = "🏀 Qué hay para hacer",
            intereses = listOf("Deportes", "Videojuegos", "Estudio"),
            bio = "Administración de Empresas · 3 semestre. Futbol los martes y FIFA cuando hay tiempo.",
            foto = retrato("1507003211169-0a1dd7228f2d"),
            marketing = false,
            clases = listOf(
                MockClase("Cálculo Empresarial", "Lunes", "07:00", "09:00", "#92A3CD"),
                MockClase("Microeconomía", "Lunes", "10:00", "12:00", "#3E7DA8"),
                MockClase("Contabilidad General", "Martes", "08:00", "10:00", "#528F61"),
                MockClase("Taller de Marketing", "Martes", "11:00", "13:00", "#A06184"),
                MockClase("Cálculo Empresarial", "Jueves", "07:00", "09:00", "#92A3CD"),
                MockClase("Estadística I", "Jueves", "10:00", "12:00", "#3E7DA8"),
                MockClase("Fútbol semanal", "Sábado", "08:00", "10:00", "#F26B60"),
                MockClase("Refuerzo de Exámenes", "Sábado", "11:00", "13:00", "#B85C70"),
            ),
        ),
        MockUsuario(
            id = "usr_03",
            username = "maria.ortiz",
            email = "maria.ortiz@central.edu.co",
            nombre = "María Camila",
            apellido = "Ortiz León",
            nacimiento = "21/07/2004",
            universidad = "Universidad Central",
            genero = "Femenino",
            meta = "🥺 Aún no lo tengo claro",
            intereses = listOf("Películas", "Estudio", "Eventos"),
            bio = "Psicología · 6 semestre. Cine, café y grupos de estudio en la biblioteca.",
            foto = retrato("1438761681033-6461ffad8d80"),
            marketing = true,
            clases = listOf(
                MockClase("Psicología General", "Lunes", "08:00", "10:00", "#EA9770"),
                MockClase("Neurociencia", "Lunes", "11:00", "13:00", "#DCB040"),
                MockClase("Taller de Escucha", "Miércoles", "09:00", "11:00", "#F0A96B"),
                MockClase("Semiología", "Miércoles", "12:00", "14:00", "#92A3CD"),
                MockClase("Psicología Social", "Viernes", "10:00", "12:00", "#5CB9DD"),
                MockClase("Laboratorio de Conducta", "Viernes", "13:00", "15:00", "#3E7DA8"),
            ),
        ),
        MockUsuario(
            id = "usr_04",
            username = "santiago.mejia",
            email = "santiago.mejia@tadeo.co",
            nombre = "Santiago",
            apellido = "Mejía Ríos",
            nacimiento = "09/01/2003",
            universidad = "Universidad Jorge Tadeo Lozano",
            genero = "Masculino",
            meta = "📚 Necesito estudiar",
            intereses = listOf("Programación", "Videojuegos", "Ciencia"),
            bio = "Ingeniería de Sistemas · 7 semestre. Programando desde el semestre 1. #IA",
            foto = retrato("1500648767791-00dcc994a43e"),
            marketing = false,
            clases = listOf(
                MockClase("Estructuras de Datos", "Lunes", "08:00", "10:00", "#51A391"),
                MockClase("Bases de Datos", "Lunes", "11:00", "13:00", "#528F61"),
                MockClase("Cálculo I", "Martes", "07:00", "09:00", "#A88BD3"),
                MockClase("Programación Avanzada", "Martes", "10:00", "12:00", "#3E7DA8"),
                MockClase("Redes", "Jueves", "08:00", "10:00", "#5CB9DD"),
                MockClase("Introducción a la IA", "Jueves", "11:00", "13:00", "#A06184"),
                MockClase("Proyecto de Software", "Viernes", "14:00", "16:00", "#51A391"),
                MockClase("Reunión de Equipo", "Viernes", "17:00", "18:00", "#82B192"),
            ),
        ),
        MockUsuario(
            id = "usr_05",
            username = "laura.hernandez",
            email = "laura.hernandez@tadeo.co",
            nombre = "Laura Sofía",
            apellido = "Hernández Cruz",
            nacimiento = "30/05/2005",
            universidad = "Universidad Jorge Tadeo Lozano",
            genero = "Femenino",
            meta = "😉 Tengo tiempo libre",
            intereses = listOf("Viajes", "Fotografía", "Moda"),
            bio = "Comunicación Social · 4 semestre. Viajar barato, fotos análogas y podcasts.",
            foto = retrato("1544005313-94ddf0286df2"),
            marketing = true,
            clases = listOf(
                MockClase("Semiótica Visual", "Lunes", "10:00", "12:00", "#C469AC"),
                MockClase("Fotografía Periodística", "Lunes", "13:00", "15:00", "#EA9770"),
                MockClase("Redacción", "Miércoles", "08:00", "10:00", "#F2A9E8"),
                MockClase("Producción de TV", "Miércoles", "11:00", "13:00", "#92A3CD"),
                MockClase("Ética Periodística", "Viernes", "09:00", "11:00", "#DCB040"),
                MockClase("Podcast Lab", "Viernes", "12:00", "14:00", "#F0A96B"),
                MockClase("Ensayo de Tesis", "Domingo", "15:00", "17:00", "#A06184"),
                MockClase("Cierre de Semana", "Domingo", "18:00", "19:00", "#B85C70"),
            ),
        ),
        MockUsuario(
            id = "usr_06",
            username = "diego.ruiz",
            email = "diego.ruiz@uniandes.edu.co",
            nombre = "Diego Alejandro",
            apellido = "Ruiz Ospina",
            nacimiento = "12/09/2002",
            universidad = "Universidad de los Andes",
            genero = "Masculino",
            meta = "🏀 Qué hay para hacer",
            intereses = listOf("Deportes", "Música", "Juegos de mesa"),
            bio = "Negocios Internacionales · 5 semestre. Ajedrez, música y partidos los lunes.",
            foto = retrato("1506794778202-cad84cf45f1d"),
            marketing = false,
            clases = listOf(
                MockClase("Comercio Internacional", "Lunes", "08:00", "10:00", "#3E7DA8"),
                MockClase("Inglés de Negocios", "Lunes", "11:00", "13:00", "#5CB9DD"),
                MockClase("Derecho Aduanero", "Martes", "10:00", "12:00", "#92A3CD"),
                MockClase("Logística", "Martes", "13:00", "15:00", "#528F61"),
                MockClase("Comercio Internacional", "Jueves", "08:00", "10:00", "#3E7DA8"),
                MockClase("Negociación", "Jueves", "11:00", "13:00", "#EA9770"),
                MockClase("Banda: ensayo", "Sábado", "10:00", "12:00", "#F26B60"),
                MockClase("Estudio dirigido", "Sábado", "13:00", "15:00", "#DCB040"),
            ),
        ),
        MockUsuario(
            id = "usr_07",
            username = "isabella.torres",
            email = "isabella.torres@tadeo.co",
            nombre = "Isabella",
            apellido = "Torres Vargas",
            nacimiento = "17/12/2004",
            universidad = "Universidad Jorge Tadeo Lozano",
            genero = "Femenino",
            meta = "📚 Necesito estudiar",
            intereses = listOf("Artes", "Baile", "Películas"),
            bio = "Artes · 8 semestre. Pintura, murales y mucho movimiento.",
            foto = retrato("1534528741775-53994a69daeb"),
            marketing = true,
            clases = listOf(
                MockClase("Pintura I", "Lunes", "09:00", "11:00", "#F2A9E8"),
                MockClase("Escultura", "Lunes", "12:00", "14:00", "#A88BD3"),
                MockClase("Arte Contemporáneo", "Miércoles", "08:00", "10:00", "#C469AC"),
                MockClase("Danza Contemporánea", "Miércoles", "11:00", "13:00", "#B85C70"),
                MockClase("Taller de Mural", "Viernes", "14:00", "16:00", "#82B192"),
                MockClase("Crítica de Arte", "Viernes", "17:00", "18:30", "#528F61"),
                MockClase("Exposición", "Sábado", "10:00", "12:00", "#EA9770"),
                MockClase("Montaje", "Sábado", "13:00", "15:00", "#F0A96B"),
            ),
        ),
        MockUsuario(
            id = "usr_08",
            username = "mateo.castano",
            email = "mateo.castano@central.edu.co",
            nombre = "Mateo",
            apellido = "Castaño Pérez",
            nacimiento = "05/04/2003",
            universidad = "Universidad Central",
            genero = "Masculino",
            meta = "😉 Tengo tiempo libre",
            intereses = listOf("Estudio", "Deportes", "Cocina"),
            bio = "Economía · 2 semestre. Gym temprano, datos y buena comida.",
            foto = retrato("1519345182560-3f2917c472ef"),
            marketing = false,
            clases = listOf(
                MockClase("Macroeconomía", "Lunes", "07:00", "09:00", "#5CB9DD"),
                MockClase("Matemáticas Financieras", "Lunes", "10:00", "12:00", "#3E7DA8"),
                MockClase("Microeconomía", "Martes", "08:00", "10:00", "#92A3CD"),
                MockClase("Taller de Econometría", "Martes", "11:00", "13:00", "#528F61"),
                MockClase("Macroeconomía", "Jueves", "07:00", "09:00", "#5CB9DD"),
                MockClase("Historia Económica", "Jueves", "10:00", "12:00", "#A06184"),
                MockClase("Gimnasio", "Viernes", "06:00", "07:00", "#F26B60"),
                MockClase("Estadística", "Viernes", "09:00", "11:00", "#DCB040"),
            ),
        ),
        MockUsuario(
            id = "usr_09",
            username = "camila.velez",
            email = "camila.velez@tadeo.co",
            nombre = "Camila Natalia",
            apellido = "Vélez Ortega",
            nacimiento = "28/08/2005",
            universidad = "Universidad Jorge Tadeo Lozano",
            genero = "No binario",
            meta = "🥺 Aún no lo tengo claro",
            intereses = listOf("Moda", "Eventos", "Música"),
            bio = "Diseño de Moda · 6 semestre. Moda sostenible y eventos en el campus.",
            foto = retrato("1524504388940-b1c1722653e1"),
            marketing = true,
            clases = listOf(
                MockClase("Patronaje", "Lunes", "10:00", "12:00", "#F2A9E8"),
                MockClase("Costura", "Lunes", "13:00", "15:00", "#C469AC"),
                MockClase("Historia de la Moda", "Miércoles", "09:00", "11:00", "#A88BD3"),
                MockClase("Tejidos", "Miércoles", "12:00", "14:00", "#B85C70"),
                MockClase("Ilustración de Moda", "Jueves", "08:00", "10:00", "#EA9770"),
                MockClase("Pasarela", "Jueves", "11:00", "13:00", "#DCB040"),
                MockClase("Diseño de Colección", "Domingo", "14:00", "16:00", "#A06184"),
                MockClase("Prueba de Vestuario", "Domingo", "17:00", "18:00", "#F0A96B"),
            ),
        ),
        MockUsuario(
            id = "usr_10",
            username = "juan.herrera",
            email = "juan.herrera@tadeo.co",
            nombre = "Juan Pablo",
            apellido = "Herrera Salazar",
            nacimiento = "11/06/2002",
            universidad = "Universidad Jorge Tadeo Lozano",
            genero = "Masculino",
            meta = "📚 Necesito estudiar",
            intereses = listOf("Ciencia", "Fotografía", "Juegos de mesa"),
            bio = "Arquitectura · 5 semestre. Maquetas, ciencia amateur y juegos de mesa.",
            foto = retrato("1552374196-c4e7ffc6e126"),
            marketing = false,
            clases = listOf(
                MockClase("Diseño Arquitectónico", "Lunes", "08:00", "11:00", "#51A391"),
                MockClase("Historia de la Arquitectura", "Lunes", "12:00", "14:00", "#DCB040"),
                MockClase("Cálculo de Estructuras", "Martes", "10:00", "12:00", "#3E7DA8"),
                MockClase("Taller de Maquetas", "Martes", "13:00", "16:00", "#528F61"),
                MockClase("Urbanismo", "Jueves", "09:00", "11:00", "#92A3CD"),
                MockClase("Representación", "Jueves", "12:00", "14:00", "#F26B60"),
                MockClase("Visita Guiada", "Sábado", "09:00", "11:00", "#B85C70"),
                MockClase("Taller de Modelado", "Sábado", "12:00", "14:00", "#82B192"),
            ),
        ),
    )

    // =================================================================
    // 20 ACTIVIDADES (act_21..act_40) — 2 por usuario
    // act_21..act_34 = HOY (14) · act_35..act_40 = próximos días (6)
    // =================================================================
    private val ACTIVIDADES_MOCK = listOf(
        MockActividad(
            id = "act_21",
            creador = 0,
            titulo = "Golden hour en la plazoleta",
            categoria = "Fotografía",
            duracion = ActivityFormat.DUR_1H,
            hora = "16:00",
            dias = 0,
            lugar = "Plazoleta central",
            descripcion = "Nos juntamos a tomar fotos del atardecer entre clases. Trae tu cámara o tu celular.",
            foto = lugar("1541339907198-e08756dedf3f"),
            asistentes = listOf(2, 4, 8),
        ),
        MockActividad(
            id = "act_22",
            creador = 0,
            titulo = "Ensayo de baile urbano",
            categoria = "Baile",
            duracion = ActivityFormat.DUR_1H,
            hora = "18:00",
            dias = 0,
            lugar = "Salón Polifuncional",
            descripcion = "Ensayo abierto para la fiesta de fin de semestre. Nivel principiante.",
            foto = lugar("1508700115892-45ecd05ae2ad"),
            asistentes = listOf(6, 4),
        ),
        MockActividad(
            id = "act_23",
            creador = 1,
            titulo = "Futbol 7 en la Cancha M7A",
            categoria = "Deportes",
            duracion = ActivityFormat.DUR_2H,
            hora = "14:00",
            dias = 0,
            lugar = "Módulo 24 - Cancha de fútbol",
            descripcion = "Partido amistoso entre facultades. Camisetas blancas y verdes.",
            foto = lugar("1517649763962-0c623066013b"),
            asistentes = listOf(5, 7, 3),
        ),
        MockActividad(
            id = "act_24",
            creador = 1,
            titulo = "Torneo FIFA en la sala de cómputo",
            categoria = "Videojuegos",
            duracion = ActivityFormat.DUR_2H,
            hora = "17:00",
            dias = 0,
            lugar = "Laboratorio de Cómputo Módulo 5",
            descripcion = "Torneo por parejas; el que pierde paga el desayuno.",
            foto = lugar("1542751371-adc38448a05e"),
            asistentes = listOf(3, 9),
        ),
        MockActividad(
            id = "act_25",
            creador = 2,
            titulo = "Cine club: clásicos colombianos",
            categoria = "Películas",
            duracion = ActivityFormat.DUR_2H,
            hora = "18:00",
            dias = 0,
            lugar = "Auditorio",
            descripcion = "Proyección + debate corto. Entrada libre con carné estudiantil.",
            foto = lugar("1489599849927-2ee91cede3ba"),
            asistentes = listOf(6, 8, 4),
        ),
        MockActividad(
            id = "act_26",
            creador = 2,
            titulo = "Grupos de estudio para parciales",
            categoria = "Estudio",
            duracion = ActivityFormat.DUR_2H,
            hora = "10:00",
            dias = 0,
            lugar = "Biblioteca",
            descripcion = "Repaso grupal de psicología y estadística. Lleva tus apuntes.",
            foto = lugar("1521587760476-6c12a4b040da"),
            asistentes = listOf(7, 3),
        ),
        MockActividad(
            id = "act_27",
            creador = 3,
            titulo = "Hackeo en el Laboratorio de Cómputo",
            categoria = "Programación",
            duracion = ActivityFormat.DUR_2H,
            hora = "15:00",
            dias = 0,
            lugar = "Laboratorio de Cómputo Módulo 5",
            descripcion = "Llevamos a la práctica lo de la clase. Puedes ir a tu ritmo.",
            foto = lugar("1517694712202-14dd9538aa97"),
            asistentes = listOf(1, 9),
        ),
        MockActividad(
            id = "act_28",
            creador = 3,
            titulo = "Café y debate: ¿y la IA?",
            categoria = "Ciencia",
            duracion = ActivityFormat.DUR_30,
            hora = "12:00",
            dias = 0,
            lugar = "Cafetería Módulo 1",
            descripcion = "Debate relajado sobre inteligencia artificial y su futuro.",
            foto = lugar("1554118811-1e0d58224f24"),
            asistentes = listOf(9, 8),
        ),
        MockActividad(
            id = "act_29",
            creador = 4,
            titulo = "Ruta de fotos por el campus",
            categoria = "Viajes",
            duracion = ActivityFormat.DUR_30,
            hora = "09:00",
            dias = 0,
            lugar = "Zona Verde",
            descripcion = "Caminata corta para fotos de otoño entre clases.",
            foto = lugar("1441974231531-c6227db76b6e"),
            asistentes = listOf(0, 8),
        ),
        MockActividad(
            id = "act_30",
            creador = 4,
            titulo = "Trueque de ropa sostenible",
            categoria = "Moda",
            duracion = ActivityFormat.DUR_1H,
            hora = "11:00",
            dias = 0,
            lugar = "Salón Polifuncional",
            descripcion = "Intercambia prendas que ya no uses. Sin dinero de por medio.",
            foto = lugar("1483985988355-763728e1935b"),
            asistentes = listOf(6, 2),
        ),
        MockActividad(
            id = "act_31",
            creador = 5,
            titulo = "Noche de catán y ajedrez",
            categoria = "Juegos de mesa",
            duracion = ActivityFormat.DUR_2H,
            hora = "19:00",
            dias = 0,
            lugar = "Biblioteca",
            descripcion = "Traemos los juegos, tú traes la estrategia.",
            foto = null, // Sin foto -> tarjeta de color verde sólido (QA)
            asistentes = listOf(9, 7, 1),
        ),
        MockActividad(
            id = "act_32",
            creador = 5,
            titulo = "Jam sesión acústica",
            categoria = "Música",
            duracion = ActivityFormat.DUR_1H,
            hora = "13:00",
            dias = 0,
            lugar = "Zona Verde",
            descripcion = "Toca, canta o solo escucha. Trae tu instrumento si tienes.",
            foto = lugar("1493225457124-a3eb161ffa5f"),
            asistentes = listOf(0, 6, 8),
        ),
        MockActividad(
            id = "act_33",
            creador = 6,
            titulo = "Mural colaborativo en la Zona Verde",
            categoria = "Artes",
            duracion = ActivityFormat.DUR_2H,
            hora = "10:00",
            dias = 0,
            lugar = "Zona Verde",
            descripcion = "Pintamos un mural con el lema del semestre. No necesitas experiencia.",
            foto = lugar("1513364776144-60967b0f800f"),
            asistentes = listOf(0, 8, 4),
        ),
        MockActividad(
            id = "act_34",
            creador = 6,
            titulo = "Clase de salsa para principiantes",
            categoria = "Baile",
            duracion = ActivityFormat.DUR_1H,
            hora = "17:00",
            dias = 0,
            lugar = "Salón Polifuncional",
            descripcion = "Dos pasos básicos y mucha buena vibra. Zapatos cómodos.",
            foto = lugar("1547153760-18fc86324498"),
            asistentes = listOf(2, 5),
        ),
        MockActividad(
            id = "act_35",
            creador = 7,
            titulo = "Cross training en el potrero",
            categoria = "Deportes",
            duracion = ActivityFormat.DUR_1H,
            hora = "07:00",
            dias = 1,
            lugar = "Módulo 24 - Cancha de fútbol",
            descripcion = "Rutina corta para arrancar el día. Puedes ir a tu ritmo.",
            foto = lugar("1517836357463-d25dfeac3438"),
            asistentes = listOf(1, 3),
        ),
        MockActividad(
            id = "act_36",
            creador = 7,
            titulo = "Taller de arepas colombianas",
            categoria = "Cocina",
            duracion = ActivityFormat.DUR_2H,
            hora = "12:00",
            dias = 1,
            lugar = "Cafetería Módulo 1",
            descripcion = "Arepas antioqueñas y de choclo; ingredientes incluidos.",
            foto = lugar("1556910103-1c02745aae4d"),
            asistentes = listOf(4, 2, 0),
        ),
        MockActividad(
            id = "act_37",
            creador = 8,
            titulo = "Feria de emprendimientos estudiantiles",
            categoria = "Eventos",
            duracion = ActivityFormat.DUR_2H,
            hora = "11:00",
            dias = 2,
            lugar = "Plazoleta central",
            descripcion = "Puestos de estudiantes, comida y música en vivo.",
            foto = lugar("1540575467063-178a50c2df87"),
            asistentes = listOf(4, 6, 0, 9),
        ),
        MockActividad(
            id = "act_38",
            creador = 8,
            titulo = "Desfile de moda emergente",
            categoria = "Moda",
            duracion = ActivityFormat.DUR_1H,
            hora = "16:00",
            dias = 2,
            lugar = "Auditorio",
            descripcion = "Colecciones de estudiantes de diseño. Puertas abiertas a las 3:30 p.m.",
            foto = lugar("1469334031218-e382a71b716b"),
            asistentes = listOf(6, 4),
        ),
        MockActividad(
            id = "act_39",
            creador = 9,
            titulo = "Observación astronómica en la terraza",
            categoria = "Ciencia",
            duracion = ActivityFormat.DUR_2H,
            hora = "19:00",
            dias = 3,
            lugar = "Azotea Módulo 7",
            descripcion = "Salimos a mirar las estrellas con telescopio prestado. Trae un abrigo.",
            foto = lugar("1419242902214-272b3f66ee7a"),
            asistentes = listOf(3, 7),
        ),
        MockActividad(
            id = "act_40",
            creador = 9,
            titulo = "Ajedrez relajado en la Biblioteca",
            categoria = "Juegos de mesa",
            duracion = ActivityFormat.DUR_1H,
            hora = "14:00",
            dias = 3,
            lugar = "Biblioteca",
            descripcion = "Tableros disponibles; sirve para novatos y expertos.",
            foto = null, // Segunda sin foto -> color verde sólido (QA)
            asistentes = listOf(5, 1),
        ),
    )
}
