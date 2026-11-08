# %% [Celda 1] Importaciones y configuración
"""Gaply · Colab parte 2: datos masivos + comparación de modelos de ML.

Genera X perfiles sintéticos, arma pares (usuario_A, usuario_B), los etiqueta
con una regla lógica ruidosa y compara tres modelos de scikit-learn para
predecir si dos usuarios hacen "Match".
"""
import random

import numpy as np
import pandas as pd

from sklearn.ensemble import GradientBoostingClassifier, RandomForestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, precision_recall_fscore_support
from sklearn.model_selection import train_test_split

# Faker (preinstalado en Colab). Si no está, se usa un generador de nombres
# local para que el notebook funcione igual en cualquier entorno.
try:
    from faker import Faker

    fake = Faker("es_ES")
    USA_FAKER = True
except ImportError:  # pragma: no cover
    fake = None
    USA_FAKER = False

SEMILLA = 42
random.seed(SEMILLA)
np.random.seed(SEMILLA)
if USA_FAKER:
    Faker.seed(SEMILLA)

print(f"Celda 1 lista · pandas {pd.__version__} · numpy {np.__version__} · Faker: {USA_FAKER}")


# %% [Celda 2] Generador dinámico de perfiles
# --- Catálogos (mismos de la app Gaply: StringsEs.kt + RegisterData) -------
UNIVERSIDADES = [                  # exactamente 4 opciones, como en la app
    "Universidad Jorge Tadeo Lozano",
    "Universidad Central",
    "Universidad de los Andes",
    "Otra",
]

CARRERAS = [
    "Ingeniería de Sistemas", "Diseño Gráfico", "Medicina", "Derecho",
    "Psicología", "Administración de Empresas", "Ingeniería Industrial",
    "Historia del Arte", "Comunicación Social", "Economía", "Química",
    "Arquitectura",
]

INTERESES = [                      # los 15 intereses de RegisterData
    "Deportes", "Música", "Videojuegos", "Artes", "Juegos de mesa",
    "Películas", "Estudio", "Eventos", "Baile", "Fotografía", "Viajes",
    "Cocina", "Programación", "Moda", "Ciencia",
]

# No todos los intereses son igual de populares en la vida real (Estudio y
# Música aparecen mucho más que Moda): ponderarlos evita un dataset con
# demasiados pares que no comparten nada.
INTERESES_POPULARES = {"Estudio", "Música", "Deportes", "Eventos"}
PESOS_INTERES = [3 if i in INTERESES_POPULARES else 1 for i in INTERESES]

NOMBRES = ["Santiago", "Valentina", "Camila", "Andrés", "Laura", "Tomás",
           "Mariana", "Nicolás", "Daniela", "Felipe", "Paula", "Julián"]
APELLIDOS = ["Martínez", "Rojas", "Ortiz", "Cárdenas", "Restrepo", "Herrera",
             "Gil", "Pardo", "Vargas", "Gómez", "Mejía", "Castro"]

X_PERFILES = 2000        # CANTIDAD de perfiles a generar (cámbialo aquí)
MIN_INTERESES = 4        # cada perfil generado tiene entre 4 y 5 intereses
MAX_INTERESES = 5


def _nombre_aleatorio(rng):
    """Nombre de usuario con Faker si existe, si no con listas locales."""
    if USA_FAKER:
        return fake.name()
    return f"{rng.choice(NOMBRES)} {rng.choice(APELLIDOS)}"


def generar_perfiles(cantidad=X_PERFILES, semilla=SEMILLA):
    """Genera `cantidad` perfiles sintéticos con ID, universidad, carrera e
    intereses aleatorios (4 a 5 por perfil, sin repetir)."""
    rng = random.Random(semilla)
    perfiles = []

    for indice in range(cantidad):
        cantidad_intereses = rng.randint(MIN_INTERESES, MAX_INTERESES)
        intereses = []
        while len(intereses) < cantidad_intereses:
            elegido = rng.choices(INTERESES, weights=PESOS_INTERES, k=1)[0]
            if elegido not in intereses:
                intereses.append(elegido)

        perfiles.append(
            {
                "user_id": f"u{indice:05d}",
                "nombre": _nombre_aleatorio(rng),
                "university": rng.choice(UNIVERSIDADES),
                "career": rng.choice(CARRERAS),
                "interests": intereses,
            }
        )
    return perfiles


perfiles = generar_perfiles()

df_perfiles = pd.DataFrame(perfiles)
print(f"Celda 2 lista · {len(perfiles)} perfiles generados")
print("Distribución por universidad:")
print(df_perfiles["university"].value_counts().to_string())
print(f"Intereses por perfil: media {df_perfiles['interests'].apply(len).mean():.2f} "
      f"(rango {MIN_INTERESES}-{MAX_INTERESES})")
print("Ejemplo de perfil:")
print(perfiles[0])


# %% [Celda 3] Ingeniería de características y etiquetado
N_PARES = 20000                      # cantidad de pares a generar
RUIDO = 0.03                         # 3% de las etiquetas se invierten a propósito
PROPORCION_MISMA_UNIVERSIDAD = 0.60  # 60% de los pares se muestrean en el mismo campus

# Etiquetado por capas de afinidad (reemplaza a la regla ">2 intereses Y misma
# universidad", que solo daba ~3% de positivos). Se ordenan los pares por un
# puntaje de afinidad y cada capa recibe una probabilidad de ser match:
# con estas proporciones los positivos quedan en ~40% del total, sea cual sea
# N_PARES, y las probabilidades intermedias dejan un margen de error que
# impide que los modelos lleguen a 1.0.
CAPA_ALTA = 0.15    # 15% de los pares mejores del ranking
CAPA_MEDIA = 0.30   # 30% siguientes
CAPA_BAJA = 0.55    # 55% restantes (la suma debe dar 1.0)
P_ALTA = 0.90       # probabilidad de match de la capa alta
P_MEDIA = 0.68      # probabilidad de match de la capa media
P_BAJA = 0.14       # probabilidad de match de la capa baja

assert abs(CAPA_ALTA + CAPA_MEDIA + CAPA_BAJA - 1.0) < 1e-9


def generar_pares(perfiles, cantidad=N_PARES, ruido=RUIDO,
                  misma_universidad=PROPORCION_MISMA_UNIVERSIDAD,
                  semilla=SEMILLA):
    """Crea pares (usuario_A, usuario_B) con variables numéricas y etiqueta.

    Características:
      - misma_universidad (1/0)
      - misma_carrera (1/0)
      - intereses_en_comun (int)
      - intereses_en_comun_ratio (Jaccard, 0.0-1.0)

    Etiqueta is_match (fórmula calibrada para ~40% de positivos):
      1. Cada par recibe un puntaje de afinidad lineal:
         2*misma_universidad + intereses_en_comun + 0.5*intereses_en_comun_ratio
      2. Los pares se ordenan por ese puntaje y se reparten en tres capas
         (15% alta, 30% media, 55% baja).
      3. Cada capa tiene una probabilidad de ser match: P_ALTA, P_MEDIA y P_BAJA.
      4. Con probabilidad `ruido` la etiqueta se invierte, para que ningún
         modelo llegue a un accuracy de 1.0.
    """
    rng = random.Random(semilla)

    por_universidad = {}
    for perfil in perfiles:
        por_universidad.setdefault(perfil["university"], []).append(perfil)

    universidades = list(por_universidad)
    filas = []
    pares_vistos = set()
    intentos = 0
    limite = cantidad * 25

    while len(filas) < cantidad and intentos < limite:
        intentos += 1

        # Muestreo sesgado a propósito: en la app los matches nacen en el campus.
        if rng.random() < misma_universidad:
            grupo = por_universidad[rng.choice(universidades)]
            if len(grupo) < 2:
                continue
            usuario_a, usuario_b = rng.sample(grupo, 2)
        else:
            usuario_a, usuario_b = rng.sample(perfiles, 2)

        clave = tuple(sorted((usuario_a["user_id"], usuario_b["user_id"])))
        if clave in pares_vistos:
            continue
        pares_vistos.add(clave)

        intereses_a = set(usuario_a["interests"])
        intereses_b = set(usuario_b["interests"])
        compartidos = len(intereses_a & intereses_b)
        union = len(intereses_a | intereses_b)
        misma_uni = int(usuario_a["university"] == usuario_b["university"])

        filas.append(
            {
                "user_a": usuario_a["user_id"],
                "user_b": usuario_b["user_id"],
                "misma_universidad": misma_uni,
                "misma_carrera": int(usuario_a["career"] == usuario_b["career"]),
                "intereses_en_comun": compartidos,
                "intereses_en_comun_ratio": (compartidos / union) if union else 0.0,
            }
        )

    # --- Etiquetado por capas (funciona igual con cualquier N_PARES) -------
    df = pd.DataFrame(filas)
    afinidad = (
        2.0 * df["misma_universidad"]
        + 1.0 * df["intereses_en_comun"]
        + 0.5 * df["intereses_en_comun_ratio"]
    ).to_numpy()

    # Posición de cada par en el ranking de afinidad (0 = mayor afinidad;
    # los empates se resuelven por orden de generación)
    posicion = np.argsort(np.argsort(-afinidad))
    corte_alta = int(len(df) * CAPA_ALTA)
    corte_media = int(len(df) * (CAPA_ALTA + CAPA_MEDIA))

    etiquetas = []
    for pos in posicion:
        if pos < corte_alta:
            probabilidad = P_ALTA
        elif pos < corte_media:
            probabilidad = P_MEDIA
        else:
            probabilidad = P_BAJA

        es_match = int(rng.random() < probabilidad)
        if rng.random() < ruido:      # margen de ruido aleatorio
            es_match = 1 - es_match
        etiquetas.append(es_match)

    df["is_match"] = etiquetas
    return df


pares = generar_pares(perfiles)

print(f"Celda 3 lista · {len(pares)} pares generados")
print(f"Balance de is_match: {pares['is_match'].mean():.2%} positivos "
      f"({int(pares['is_match'].sum())} de {len(pares)})")
print("Intereses en común promedio según la etiqueta:")
print(pares.groupby("is_match")["intereses_en_comun"].mean().round(2).to_string())


# %% [Celda 4] Entrenamiento y comparación de modelos
CARACTERISTICAS = [
    "misma_universidad",
    "misma_carrera",
    "intereses_en_comun",
    "intereses_en_comun_ratio",
]

X = pares[CARACTERISTICAS]
y = pares["is_match"]

X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.20, random_state=SEMILLA, stratify=y
)

MODELOS = {
    "random_forest": RandomForestClassifier(
        n_estimators=100, random_state=SEMILLA, n_jobs=-1,
        class_weight="balanced",
    ),
    "gradient_boosting": GradientBoostingClassifier(random_state=SEMILLA),
    "logistic_regression": LogisticRegression(
        max_iter=1000, random_state=SEMILLA, class_weight="balanced"
    ),
}

metricas = {}
for nombre, modelo in MODELOS.items():
    modelo.fit(X_train, y_train)
    predichos = modelo.predict(X_test)
    precision, recall, f1, _ = precision_recall_fscore_support(
        y_test, predichos, average="binary", zero_division=0
    )
    metricas[nombre] = {
        "accuracy": accuracy_score(y_test, predichos),
        "precision": precision,
        "recall": recall,
        "f1": f1,
    }

# Mejor modelo por accuracy (en empate gana el primero: random_forest)
mejor = max(metricas, key=lambda nombre: metricas[nombre]["accuracy"])
mejor_accuracy = metricas[mejor]["accuracy"]

print("=" * 39)
print("RESULTADOS DEL ENTRENAMIENTO")
print(f"Mejor modelo: {mejor}")
print(f"Accuracy: {mejor_accuracy:.4f} ({mejor_accuracy * 100:.2f}%)")
print(f"Datos de entrenamiento: {len(X_train)}")
print(f"Datos de prueba: {len(X_test)}")
print("Metricas por modelo:")
for nombre, m in metricas.items():
    print(f"{nombre}:")
    print(f"Accuracy:  {m['accuracy']:.4f}")
    print(f"Precision: {m['precision']:.4f}")
    print(f"Recall:    {m['recall']:.4f}")
    print(f"F1:        {m['f1']:.4f}")
