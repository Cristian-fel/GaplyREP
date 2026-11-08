# %% [Celda 1] Importaciones y configuración
"""Gaply · Simulación del agente de matchmaking para Google Colab.

El agente de Gaply no solo responde preguntas: su papel principal es motor de
recomendación y emparejamiento (matchmaking). Esta notebook aísla esa lógica
(intereses + motivos + perfil del onboarding) para probarla contra una base
sintética de usuarios.

Modo local: no requiere API key ni internet. El System Prompt de la Celda 3 ya
está escrito para conectarse a un LLM cuando quieras dar ese paso.
"""
import json
import re
import textwrap
import unicodedata

# --- (Opcional) Cliente de IA para cuando quieras pasar a modo LLM ---------
# import os
# from google import genai
# os.environ["GOOGLE_API_KEY"] = ""   # clave gratuita de https://aistudio.google.com
# cliente = genai.Client()
# MODELO = "gemini-2.0-flash"

# Pesos de la función de utilidad (reflejan la prioridad del agente de Gaply:
# los intereses pesan más que el resto, como en AgenteRecomendacion.kt).
# Deben sumar 1.0.
PESOS = {
    "intereses": 0.45,
    "universidad": 0.20,
    "objetivo": 0.20,
    "bio": 0.15,
}
assert abs(sum(PESOS.values()) - 1.0) < 1e-9, "Los pesos deben sumar 1.0"

MAX_INTERES_PERFIL = 3   # RegisterData.MAX_INTERESTS en la app
TARJETAS_TOP = 5         # AgenteRecomendacion.maximoRecomendaciones
UMBRAL_BIO = 0.12        # similitud mínima para mencionar la biografía
VER_DETALLE = True       # imprime el desglose de utilidad por candidato

print(f"Celda 1 lista · pesos: {PESOS} · top {TARJETAS_TOP} matches")


# %% [Celda 2] Base de datos sintética de perfiles
# Campos alineados con data class User (app/src/main/java/.../domain/model/User.kt)
# y con lo que el onboarding guarda en RegisterUiState: university, gender, goal,
# interests (máx. 3) y bio. Se omiten email/passwordHash por ser datos sensibles.

# Catálogos REALES de la app (RegisterData.interests + extraOptions)
INTERESES_BASE = [
    "Deportes", "Música", "Videojuegos", "Artes",
    "Juegos de mesa", "Películas", "Estudio", "Eventos", "Baile",
]
INTERESES_EXTRA = [
    "Fotografía", "Viajes", "Cocina", "Programación", "Moda", "Ciencia",
]
INTERESES = INTERESES_BASE + INTERESES_EXTRA

# Catálogos REALES de StringsEs.kt
UNIVERSIDADES = [
    "Universidad Jorge Tadeo Lozano",
    "Universidad Central",
    "Universidad de los Andes",
    "Otra",
]
OBJETIVOS = [
    "📚 Necesito estudiar",
    "🏀 Qué hay para hacer",
    "😉 Tengo tiempo libre",
    "🤔 Aún no lo tengo claro",
]
GENEROS = ["Femenino", "Masculino", "No binario", "Prefiero no decirlo"]

USUARIOS = [
    {
        "user_id": "u01",
        "username": "valerojas",
        "first_name": "Valentina",
        "last_name": "Rojas",
        "birth_date": "2004-07-19",
        "university": "Universidad de los Andes",
        "gender": "Femenino",
        "goal": "📚 Necesito estudiar",
        "interests": ["Estudio", "Música", "Fotografía"],
        "bio": "Diseño en los Andes. Vinilo, fotos por la ciudad y muchas horas "
               "en la biblioteca preparando parciales. Quiero armar un grupo de estudio.",
    },
    {
        "user_id": "u02",
        "username": "acardenas",
        "first_name": "Andrés",
        "last_name": "Cárdenas",
        "birth_date": "2003-02-08",
        "university": "Universidad de los Andes",
        "gender": "Masculino",
        "goal": "📚 Necesito estudiar",
        "interests": ["Estudio", "Programación", "Ciencia"],
        "bio": "Ingeniería industrial en los Andes. Estudiar en grupo me sale mejor "
               "que solo: biblioteca, resúmenes y muchos parciales. Programo en los huecos.",
    },
    {
        "user_id": "u03",
        "username": "camortiz",
        "first_name": "Camila",
        "last_name": "Ortiz",
        "birth_date": "2005-11-30",
        "university": "Universidad de los Andes",
        "gender": "Femenino",
        "goal": "😉 Tengo tiempo libre",
        "interests": ["Música", "Baile", "Eventos"],
        "bio": "Busco planes para el tiempo libre: conciertos, baile y eventos en Bogotá. "
               "La biblioteca no es lo mío, pero un café después de clase sí.",
    },
    {
        "user_id": "u04",
        "username": "sluna",
        "first_name": "Sebastián",
        "last_name": "Luna",
        "birth_date": "2002-05-14",
        "university": "Universidad Central",
        "gender": "Masculino",
        "goal": "📚 Necesito estudiar",
        "interests": ["Estudio", "Programación", "Videojuegos"],
        "bio": "Estudio en la Central y organizo grupos de estudio virtuales. "
               "Programo apps y me sé el ritual de parciales y prácticas.",
    },
    {
        "user_id": "u05",
        "username": "magil",
        "first_name": "Mariana",
        "last_name": "Gil",
        "birth_date": "2004-09-03",
        "university": "Universidad Jorge Tadeo Lozano",
        "gender": "Femenino",
        "goal": "📚 Necesito estudiar",
        "interests": ["Estudio", "Artes", "Películas"],
        "bio": "Historia del arte en la Tadeo. Necesito compañía para estudiar "
               "en el campus y después ver una película como premio.",
    },
    {
        "user_id": "u06",
        "username": "npardo",
        "first_name": "Nicolás",
        "last_name": "Pardo",
        "birth_date": "2003-12-21",
        "university": "Universidad de los Andes",
        "gender": "Masculino",
        "goal": "🏀 Qué hay para hacer",
        "interests": ["Deportes", "Eventos", "Baile"],
        "bio": "A la cancha todos los días después de clase. Me gustan los torneos "
               "universitarios y la agenda de eventos. Estudio en la noche.",
    },
    {
        "user_id": "u07",
        "username": "lrestrepo",
        "first_name": "Laura",
        "last_name": "Restrepo",
        "birth_date": "2004-01-27",
        "university": "Universidad de los Andes",
        "gender": "Femenino",
        "goal": "📚 Necesito estudiar",
        "interests": ["Música", "Estudio", "Cocina"],
        "bio": "Medicina en los Andes. Estudio temprano en la biblioteca y cocino "
               "para relajarme. Busco gente con horarios parecidos para repasar.",
    },
    {
        "user_id": "u08",
        "username": "fgomez",
        "first_name": "Felipe",
        "last_name": "Gómez",
        "birth_date": "2005-04-11",
        "university": "Otra",
        "gender": "Masculino",
        "goal": "🤔 Aún no lo tengo claro",
        "interests": ["Juegos de mesa", "Videojuegos", "Películas"],
        "bio": "Todavía no sé qué quiero. Paso el tiempo en tableros y series. "
               "Si alguien quiere jugar una tarde, escribo.",
    },
    {
        "user_id": "u09",
        "username": "dvargas",
        "first_name": "Daniela",
        "last_name": "Vargas",
        "birth_date": "2003-08-06",
        "university": "Universidad Central",
        "gender": "Femenino",
        "goal": "😉 Tengo tiempo libre",
        "interests": ["Viajes", "Fotografía", "Música"],
        "bio": "Salgo a fotografiar la ciudad los fines de semana y planeo viajes "
               "cortos. La música va siempre en el audífono.",
    },
    {
        "user_id": "u10",
        "username": "therrera",
        "first_name": "Tomás",
        "last_name": "Herrera",
        "birth_date": "2002-10-15",
        "university": "Universidad de los Andes",
        "gender": "Masculino",
        "goal": "📚 Necesito estudiar",
        "interests": ["Estudio", "Ciencia", "Deportes"],
        "bio": "Química en los Andes. Grupos de estudio en el laboratorio y en la "
               "biblioteca; busco compañeros para los parciales difíciles.",
    },
    {
        "user_id": "u11",
        "username": "pmejia",
        "first_name": "Paula",
        "last_name": "Mejía",
        "birth_date": "2004-06-25",
        "university": "Universidad Jorge Tadeo Lozano",
        "gender": "Femenino",
        "goal": "🏀 Qué hay para hacer",
        "interests": ["Deportes", "Baile", "Eventos"],
        "bio": "Entreno volei y busco gente para el gym y torneos. Organizo "
               "salidas y eventos con mis amigos.",
    },
    {
        "user_id": "u12",
        "username": "jcastro",
        "first_name": "Julián",
        "last_name": "Castro",
        "birth_date": "2003-03-17",
        "university": "Universidad de los Andes",
        "gender": "No binario",
        "goal": "😉 Tengo tiempo libre",
        "interests": ["Programación", "Videojuegos", "Música"],
        "bio": "Cuando no tengo clases programo videojuegos y toco en una banda. "
               "Busco con quién salir a eventos o a tocar.",
    },
]

print(f"Celda 2 lista · {len(USUARIOS)} perfiles sintéticos · {len(INTERESES)} intereses en catálogo")


# %% [Celda 3] Agente, System Prompt y función de emparejamiento

# --- System Prompt (listo para un LLM) -------------------------------------
SYSTEM_PROMPT_MATCHMAKING = """
Eres 'Gaply Match', el agente de emparejamiento (matchmaking) de la app Gaply.

ROL
Tu única tarea es evaluar la afinidad entre el "usuario activo" (quien acaba de
completar el onboarding) y cada usuario candidato de la base de datos entregada.
No respondes preguntas generales ni das consejos: produces matches.

ENTRADA
Recibirás un JSON con:
  - "usuario_actual": perfil del usuario activo (university, goal, interests[], bio).
  - "candidatos": lista de perfiles de la base (mismos campos).
  - "n": cantidad de matches a devolver.

CRITERIOS DE AFINIDAD (orden de prioridad)
1. Intereses compartidos (cada usuario tiene máximo 3; coincidir en varios es el
   mejor indicador de compatibilidad).
2. Misma universidad (facilita encontrarse en el campus; "Otra" no cuenta como
   coincidencia).
3. Mismo objetivo del onboarding (estudiar, salir, tiempo libre, etc.).
4. Afinidad semántica de la biografía (temas, planes y hábitos en común).
El género y la edad NUNCA son criterios de emparejamiento.

SALIDA
Responde SOLO con JSON válido, sin markdown ni texto adicional:
[
  {"user_id": "...", "puntuacion": 0, "justificacion": "..."}
]
- Ordena de mayor a menor puntuación y devuelve como máximo "n" elementos.
- "puntuacion" es un entero 0-100 de afinidad global.
- "justificacion" es una frase breve y concreta en español (máx. 25 palabras),
  por ejemplo: "Ambos estudian en la Universidad de los Andes y buscan
  compañeros de estudio".
- Usa exclusivamente user_id existentes; nunca inventes perfiles ni datos.
- Si ningún candidato encaja de verdad, devuelve la lista vacía [].
""".strip()


def construir_mensaje_usuario(usuario, base=None, n=TARJETAS_TOP):
    """Payload que se enviaría junto al system prompt si se activa el LLM."""
    base = USUARIOS if base is None else base
    return json.dumps(
        {"usuario_actual": usuario, "candidatos": base, "n": n},
        ensure_ascii=False,
        indent=2,
    )


# --- Funciones de afinidad -------------------------------------------------
STOPWORDS = set(
    """de la que el en y a los del se las por un para con no una su al lo como
    pero sus le ya o este sí porque esta entre cuando muy sin sobre también me
    hasta hay donde quien desde todo nos durante todos uno les ni contra otros
    ese eso ante ellos e esto mí antes algunos qué unos yo otro otras otra él
    tanto esa estos mucho quienes nada muchos cual poco ella estar estas
    algunas algo nosotros mis tu te ti
    busco busca buscan quiero buscamos gente puedo hace hago gusta encanta
    siempre además despues organiza organizo mejor mejores parecidos""".split()
)


def normalizar(texto):
    """Sin tildes ni mayúsculas, igual que AgenteRecomendacion.normalizar()."""
    texto = unicodedata.normalize("NFD", str(texto).lower())
    return "".join(c for c in texto if unicodedata.category(c) != "Mn")


def _tokens(texto):
    return {
        t
        for t in re.findall(r"[a-z]+", normalizar(texto))
        if len(t) > 2 and t not in STOPWORDS
    }


def _vocabulario(texto):
    """Mapa forma normalizada -> forma original, para mostrarla tal cual."""
    vocabulario = {}
    for palabra in re.findall(r"[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+", str(texto)):
        clave = normalizar(palabra)
        if len(clave) > 2 and clave not in STOPWORDS:
            vocabulario.setdefault(clave, palabra)
    return vocabulario


def intereses_compartidos(perfil, candidato):
    propios = {normalizar(i) for i in perfil.get("interests", [])}
    return [i for i in candidato.get("interests", []) if normalizar(i) in propios]


def similitud_bio(bio_a, bio_b):
    """Similitud de Jaccard sobre palabras significativas (0.0 - 1.0)."""
    a, b = _tokens(bio_a), _tokens(bio_b)
    if not a or not b:
        return 0.0
    return len(a & b) / len(a | b)


def componentes_de_afinidad(perfil, candidato):
    """Cada dimensión de la utilidad, normalizada a 0.0 - 1.0."""
    propios = [normalizar(i) for i in perfil.get("interests", [])]
    compartidos = {normalizar(i) for i in intereses_compartidos(perfil, candidato)}

    intereses = (len(compartidos) / len(set(propios))) if propios else 0.0

    uni_a = normalizar(perfil.get("university", ""))
    uni_b = normalizar(candidato.get("university", ""))
    valida = uni_a and uni_b and uni_a != normalizar("Otra") and uni_b != normalizar("Otra")
    universidad = 1.0 if valida and uni_a == uni_b else 0.0

    objetivo = 1.0 if perfil.get("goal") == candidato.get("goal") else 0.0

    bio = similitud_bio(perfil.get("bio", ""), candidato.get("bio", ""))

    return {
        "intereses": min(intereses, 1.0),
        "universidad": universidad,
        "objetivo": objetivo,
        "bio": bio,
    }


def puntuacion_de(componentes):
    utilidad = sum(PESOS[k] * componentes[k] for k in PESOS)
    return max(0, min(100, round(utilidad * 100)))


def _listar(items):
    """'a', 'a y b' o 'a, b y c' según la cantidad de elementos."""
    items = [str(i) for i in items]
    if len(items) == 1:
        return items[0]
    return ", ".join(items[:-1]) + " y " + items[-1]


def temas_en_comun(perfil, candidato):
    """Palabras de la biografía que destacan el vínculo, sin repetir datos ya
    mencionados (universidad, intereses u objetivo). Se devuelven con su forma
    original (tildes y ñ incluidos)."""
    ya_dichos = set(_tokens(perfil.get("university", "")))
    for interes in perfil.get("interests", []):
        ya_dichos |= _tokens(interes)
    compartidas = (_tokens(perfil.get("bio", "")) & _tokens(candidato.get("bio", ""))) - ya_dichos
    original = _vocabulario(perfil.get("bio", ""))
    return [original.get(t, t) for t in sorted(compartidas)]


def justificar(perfil, candidato, componentes):
    """Justificación breve del match (lo que imprime la tarjeta)."""
    partes = []

    if componentes["universidad"] == 1.0:
        universidad = perfil["university"]
        articulo = "la " if normalizar(universidad).startswith("universidad") else ""
        partes.append(f"Ambos estudian en {articulo}{universidad}")

    compartidos = intereses_compartidos(perfil, candidato)
    if compartidos:
        partes.append(f"comparten los intereses {_listar(compartidos)}")

    if componentes["objetivo"] == 1.0:
        partes.append(f"tienen el mismo objetivo ({perfil.get('goal', '')})")

    if componentes["bio"] >= UMBRAL_BIO:
        temas = temas_en_comun(perfil, candidato)[:2]
        if temas:
            partes.append(f"su biografía menciona {', '.join(temas)}")

    if not partes:
        return "Afinidad baja: no comparten intereses, universidad ni objetivo."

    frase = _listar(partes)
    frase = frase[0].upper() + frase[1:]
    return frase + "."


# --- Ciclo del agente: percepción -> decisión -> acción --------------------
def percibir(usuario, base):
    """Percepción: descarta al propio usuario de la base candidata."""
    return [c for c in base if c.get("user_id") != usuario.get("user_id")]


def decidir(usuario, candidatos):
    """Decisión: utilidad (0-100) + justificación para cada candidato."""
    evaluados = []
    for candidato in candidatos:
        componentes = componentes_de_afinidad(usuario, candidato)
        evaluados.append(
            {
                "usuario": candidato,
                "puntuacion": puntuacion_de(componentes),
                "componentes": componentes,
                "justificacion": justificar(usuario, candidato, componentes),
            }
        )
    return evaluados


def actuar(evaluados, n=TARJETAS_TOP):
    """Acción: ordena por relevancia y limita a los N mejores."""
    ordenados = sorted(
        evaluados,
        key=lambda m: (-m["puntuacion"], normalizar(m["usuario"]["first_name"])),
    )
    return ordenados[:n]


def buscar_matches(usuario_actual, base=None, n=TARJETAS_TOP):
    """API principal: devuelve los N mejores matches de la base, ordenados."""
    base = USUARIOS if base is None else base
    return actuar(decidir(usuario_actual, percibir(usuario_actual, base)), n)


# Ruta opcional con LLM (descomentar cuando exista el cliente en la Celda 1):
# respuesta = cliente.models.generate_content(
#     model=MODELO,
#     contents=[SYSTEM_PROMPT_MATCHMAKING + "\n\n" + construir_mensaje_usuario(USUARIO_ACTUAL)],
# )
# matches_llm = json.loads(respuesta.text)

print("Celda 3 lista · agente local + System Prompt de matchmaking")


# %% [Celda 4] Ejecución de prueba con un usuario ejemplo
# Este diccionario es lo que el onboarding dejaría en RegisterUiState/User.
USUARIO_ACTUAL = {
    "user_id": "yo-001",
    "username": "smperez",
    "first_name": "Santiago",
    "last_name": "Martínez",
    "birth_date": "2004-03-12",
    "university": "Universidad de los Andes",
    "gender": "Masculino",
    "goal": "📚 Necesito estudiar",
    "interests": ["Estudio", "Música", "Programación"],
    "bio": "Estudiante de ingeniería de sistemas en los Andes. Organizo sesiones "
           "de estudio en la biblioteca y busco compañeros para los parciales. "
           "Cuando descanso, toco la guitarra o programa proyectos personales.",
}

LINEA = "-" * 78


def imprimir_cabecera(usuario):
    print(LINEA)
    print("GAPLY · AGENTE DE MATCHMAKING (simulación local)")
    print(LINEA)
    print("USUARIO ACTUAL (onboarding)")
    print(f"  Nombre:      {usuario['first_name']} {usuario['last_name']} (@{usuario['username']})")
    print(f"  Universidad: {usuario['university']}")
    print(f"  Objetivo:    {usuario['goal']}")
    print(f"  Intereses:   {', '.join(usuario['interests'])}")
    print(f"  Biografía:   {textwrap.fill(usuario['bio'], width=74, subsequent_indent='               ')}")
    print(LINEA)


def imprimir_tarjeta(posicion, match):
    u = match["usuario"]
    print(f"MATCH #{posicion}  ·  afinidad {match['puntuacion']}/100")
    print(f"  Nombre:      {u['first_name']} {u['last_name']} (@{u['username']})")
    print(f"  Universidad: {u['university']}")
    print(f"  Objetivo:    {u['goal']}")
    print(f"  Intereses:   {', '.join(u['interests'])}")
    print(f"  Biografía:   {textwrap.fill(u['bio'], width=74, subsequent_indent='               ')}")
    print(f"  Por qué:     {match['justificacion']}")
    if VER_DETALLE:
        c = match["componentes"]
        print(
            "  Desglose:    "
            f"intereses {c['intereses']:.2f} · universidad {c['universidad']:.2f} · "
            f"objetivo {c['objetivo']:.2f} · bio {c['bio']:.2f}"
        )
    print(LINEA)


imprimir_cabecera(USUARIO_ACTUAL)

matches = buscar_matches(USUARIO_ACTUAL, base=USUARIOS, n=TARJETAS_TOP)

if not matches:
    print("Sin matches compatibles.")
else:
    for posicion, match in enumerate(matches, start=1):
        imprimir_tarjeta(posicion, match)
    print(f"Total evaluado: {len(USUARIOS) - 1} candidatos · mostrados: {len(matches)}")
