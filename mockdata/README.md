# Gaply · Datos de prueba (mock data)

Conjunto de datos de QA para validar los flujos principales: mazo de **Matches**
(deck + agente de utilidad), **Chats** (directos y de grupo), "Mis huecos",
"Actividades actuales" y el **Descubrir**.

- JSON completo → [`gaply_mock_data.json`](gaply_mock_data.json)
- Colecciones de destino: `users/{userId}` y `activities/{id}` (Firestore)

---

## Decisiones de diseño (adaptado a Gaply)

| Requisito original | Cómo quedó en Gaply | Por qué |
| --- | --- | --- |
| Intereses tipo "Fotografía, Running, Gaming…" | **Solo claves canónicas** de nuestro catálogo: `Deportes, Música, Videojuegos, Artes, Juegos de mesa, Películas, Estudio, Eventos, Baile` | Los chips y el agente comparan la clave canónica en español (match sin tildes); una clave inventada rompe intereses, categorías y puntuaciones |
| "Horario de disponibilidad" | **`schedule`** (clases con día + rango) y los **huecos libres** se derivan de ese horario | La app no guarda disponibilidad: calcula el hueco como la resta entre dos clases del mismo día (≥ 60 min, `HuecoLibre.deClases`) |
| Categoría de actividad | Mismo catálogo de 9 (es el desplegable real del formulario) | `NewActivityScreen` usa los intereses como categorías |
| "Capacidad máxima" | **No existe en el esquema**: va dentro de `description` como `Cupo: N` | Evita inventar campos que la app no pinta; si quieres el cupo real, sería un campo nuevo en `ActivityItem` + UI |
| Fotos de perfil | `https://i.pravatar.cc/300?img=NN` (deterministas, sin API key) | `source.unsplash.com` está retirado; pravatar siempre devuelve la misma imagen por `img` |
| Fotos de actividad | 10 con `imageUri` (picsum con seed) y 10 con `imageUri: null` + `defaultColorHex` | Cubre los dos caminos de render (`AsyncImage` y bloque de color) |
| Fecha (`date`) | ISO `yyyy-MM-dd` | Soportado por `ActivityFormat.esDeFecha`, `fechaMillis` y `ordenDeProximidad` |
| Creador de la actividad | `attendeeIds`/`attendees` **incluyen al dueño** | Desde "creador unido al publicar" la app escribe eso al crear; los datos deben reflejarlo |
| IDs | `usr_01…usr_10` y `act_01…act_20` | Opacos para la app. Para entrar con esas cuentas hay que crearlos también en Firebase Auth con ese uid (o mapearlos) |

> Nota: `id` de actividad = **nombre del documento** (`activities/act_01`); no hace
> falta guardarlo como campo. `isJoined` / `isCreatedByMe` **no se guardan**: se
> calculan en cada lectura.

---

## 1 · Usuarios

| ID | Nombre | Género | Intereses | Huecos libres (derivados) | Foto |
| --- | --- | --- | --- | --- | --- |
| `usr_01` | Valentina Restrepo Ocampo | Femenino | Deportes, Estudio, Eventos | Lun 10:00-13:00 · Mié 10:00-13:00 | [img=47](https://i.pravatar.cc/300?img=47) |
| `usr_02` | Sebastián Acosta Peña | Masculino | Videojuegos, Música, Estudio | Mar 11:00-14:00 · Jue 10:00-11:00 | [img=12](https://i.pravatar.cc/300?img=12) |
| `usr_03` | Camila Herrera Núñez | Femenino | Música, Artes, Películas | Mar 12:00-14:00 · Vie 11:00-12:00 | [img=45](https://i.pravatar.cc/300?img=45) |
| `usr_04` | Mateo Gómez Villalba | Masculino | Deportes, Juegos de mesa, Eventos | Lun 09:00-11:00 · Sáb 10:00-12:00 | [img=33](https://i.pravatar.cc/300?img=33) |
| `usr_05` | Lucía Fernández Arango | Femenino | Música, Baile, Eventos, Artes | Mar 12:00-13:00 · Jue 11:00-13:00 | [img=26](https://i.pravatar.cc/300?img=26) |
| `usr_06` | Juan Esteban Cortés Rivas | Masculino | Estudio, Videojuegos, Deportes | Mié 10:00-11:00 · Vie 16:00-17:00 | [img=15](https://i.pravatar.cc/300?img=15) |
| `usr_07` | Daniela Sofía Peñaloza Vera | No binario | Estudio, Películas, Artes | Jue 12:00-13:00 · Lun 17:00-18:00 | [img=44](https://i.pravatar.cc/300?img=44) |
| `usr_08` | Andrés Felipe Quiroga Salas | Masculino | Deportes, Música, Eventos | Lun 12:00-14:00 · Mié 11:00-12:00 | [img=60](https://i.pravatar.cc/300?img=60) |
| `usr_09` | Ximena Torres Buitrago | Femenino | Juegos de mesa, Estudio, Películas, Videojuegos | Mar 11:00-12:00 · Jue 16:00-17:00 | [img=49](https://i.pravatar.cc/300?img=49) |
| `usr_10` | Óscar Iván Mendoza Cardona | Prefiero no decirlo | Baile, Música, Eventos | Vie 10:00-12:00 · Sáb 12:00-13:00 | [img=11](https://i.pravatar.cc/300?img=11) |

Universidades: 8 × *Jorge Tadeo Lozano*, `usr_07` × *Universidad Central*, `usr_10` × *U. de los Andes*.
Cada usuario tiene **2 días de clase con 2 clases por día** (mínimo para que exista hueco).

---

## 2 · Actividades (2 por usuario)

| ID | Título | Categoría | Creador | Fecha | Hora | Duración | Lugar | Cupo |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `act_01` | Entrenamiento funcional en la cancha | Deportes | `usr_01` | 2026-10-27 | 6:30 a.m. - 7:30 a.m. | 1 hora | Cancha M7A | 8 |
| `act_02` | Club de lectura: ciencia ficción | Estudio | `usr_01` | 2026-10-29 | 4:00 p.m. - 5:00 p.m. | 1 hora | Biblioteca Central | 6 |
| `act_03` | Retro gaming en la sala de cómputo | Videojuegos | `usr_02` | 2026-10-30 | 3:00 p.m. - 5:00 p.m. | 2 horas | Sala de cómputo 3 | 10 |
| `act_04` | Jam sesión acústica del Tadeo | Música | `usr_02` | 2026-11-04 | 12:00 p.m. - 1:00 p.m. | 1 hora | Cafetería Tadeo | 10 |
| `act_05` | Cine club: cortos de animación | Películas | `usr_03` | 2026-10-25 | 6:00 p.m. - 8:00 p.m. | 2 horas | Auditorio 201 | 9 |
| `act_06` | Taller de acuarela al aire libre | Artes | `usr_03` | 2026-11-07 | 10:00 a.m. - 12:00 p.m. | 2 horas | Zona Verde (Patio Central) | 7 |
| `act_07` | Torneo relámpago de fulbito | Deportes | `usr_04` | 2026-10-26 | 5:00 p.m. - 6:00 p.m. | 1 hora | Cancha M7A | 10 |
| `act_08` | Noche de juegos de mesa | Juegos de mesa | `usr_04` | 2026-11-05 | 5:00 p.m. - 7:00 p.m. | 2 horas | Salón 204 | 8 |
| `act_09` | Clase de salsa para principiantes | Baile | `usr_05` | 2026-10-28 | 6:00 p.m. - 7:00 p.m. | 1 hora | Terraza W | 10 |
| `act_10` | Mercadillo de ilustración | Eventos | `usr_05` | 2026-11-08 | 11:00 a.m. - 1:00 p.m. | 2 horas | Glorieta de la Candelaria | 10 |
| `act_11` | Sesión de estudio grupal: cálculo II | Estudio | `usr_06` | 2026-10-26 | 10:00 a.m. - 11:00 a.m. | 1 hora | Biblioteca Central | 6 |
| `act_12` | Torneo de FIFA por parejas | Videojuegos | `usr_06` | 2026-10-31 | 2:00 p.m. - 4:00 p.m. | 2 horas | Salón 204 | 8 |
| `act_13` | Maratón de cine de autor | Películas | `usr_07` | 2026-11-01 | 4:00 p.m. - 6:00 p.m. | 2 horas | Auditorio 201 | 9 |
| `act_14` | Dibujo del natural en el Museo | Artes | `usr_07` | 2026-11-06 | 10:00 a.m. - 11:00 a.m. | 1 hora | Museo de Arte de la U. Jorge Tadeo Lozano | 5 |
| `act_15` | Ruta de running por La Candelaria | Deportes | `usr_08` | 2026-10-25 | 7:00 a.m. - 8:00 a.m. | 1 hora | Salida: puerta principal del Tadeo | 10 |
| `act_16` | Fiesta de disfraces del campus | Eventos | `usr_08` | 2026-10-31 | 7:00 p.m. - 9:00 p.m. | 2 horas | Terraza W | 10 |
| `act_17` | Ajedrez relámpago en el patio | Juegos de mesa | `usr_09` | 2026-10-28 | 12:00 p.m. - 1:00 p.m. | 1 hora | Zona Verde (Patio Central) | 6 |
| `act_18` | Club de anime y manga | Películas | `usr_09` | 2026-11-03 | 4:00 p.m. - 6:00 p.m. | 2 horas | Salón 204 | 8 |
| `act_19` | Práctica de breakdance | Baile | `usr_10` | 2026-10-26 | 7:00 p.m. - 8:00 p.m. | 1 hora | Terraza W | 7 |
| `act_20` | Soda & vinilos: tarde de música | Música | `usr_10` | 2026-11-14 | 3:00 p.m. - 5:00 p.m. | 2 horas | Cafetería Tadeo | 10 |

Distribución: Deportes 3 · Películas 3 · Artes/Baile/Eventos/Estudio/Juegos de mesa/Música/Videojuegos 2 c/u.
`act_05` y `act_15` son de **hoy** (2026-10-25) → sirven para "más próximas primero" y para el modo
**Actividades actuales**; el resto son futuras (26/10 → 14/11).

---

## 3 · Casos de prueba que cubre el set

**Matches (agente 0.60 intereses + 0.40 huecos en común)** — pares con intereses
compartidos **y** solape de hueco (deben aparecer en el mazo):

| Par | Día/hueco solapado | Intereses en común |
| --- | --- | --- |
| `usr_01` ↔ `usr_04` | Lun 10:00-11:00 | Deportes, Eventos |
| `usr_01` ↔ `usr_08` | Lun 12:00-13:00 y Mié 11:00-12:00 | Deportes, Eventos |
| `usr_01` ↔ `usr_06` | Mié 10:00-11:00 | Estudio, Deportes |
| `usr_02` ↔ `usr_09` | Mar 11:00-12:00 | Videojuegos, Estudio |
| `usr_02` ↔ `usr_03` | Mar 12:00-14:00 | Música |
| `usr_02` ↔ `usr_05` | Mar 12:00-13:00 | Música |
| `usr_03` ↔ `usr_05` | Mar 12:00-13:00 | Música, Artes |
| `usr_05` ↔ `usr_07` | Jue 12:00-13:00 | Artes |
| `usr_03` ↔ `usr_10` | Vie 11:00-12:00 | Música |

- **Caso negativo (debe descartarse):** `usr_04` ↔ `usr_10` comparten *Eventos*
  pero ninguno tiene hueco ese día → el agente lo descarta con "No tienen huecos libres en común".
- **Caso borde:** perfiles sin horario ya cargado no se descartan (puntúan solo con intereses).

**Otros flujos**
- Deck de actividades: hoy hay 2 (`act_05`, `act_15`) + las futuras, ordenadas por fecha.
- Unirse → chat de grupo `act_01…act_20` con aviso "X se unió"; salir → aviso + sale del chat.
- Chat directo: match mutuo entre cualquier par de la tabla.
- Estado vacío: borrar/ocultar todos los chats → "Tus conversaciones aparecerán aquí".
- `defaultColorHex` (10 actividades) vs `imageUri` (10) → los dos caminos de render.

## 4 · Importarlo

**Ya está hecho** en el proyecto `gaply-ccb85`: el seeder
[`seed_gaply.ps1`](seed_gaply.ps1) creó las 10 cuentas en **Firebase Auth** y
escribió los 10 perfiles + 20 actividades en **Firestore** (ids reales de Auth
mapeados desde `usr_01…usr_10`).

```powershell
# re-ejecutable (idempotente: si la cuenta ya existe hace signIn y reescribe)
powershell -ExecutionPolicy Bypass -File mockdata\seed_gaply.ps1
```

Credenciales (misma contraseña para todas):

| id origen | uid real | email | usuario |
| --- | --- | --- | --- |
| `usr_01` | `avBtHLVAOPaQvkfdjtosmRu3xFg2` | valentina.restrepo@example.com | valen.restrepo |
| `usr_02` | `P4Hu28beuGeAEmIpL6bJBG2fIn23` | sebastian.acosta@example.com | sebas.acosta |
| `usr_03` | `fouuiOm2itbj4XvNEyQjxn2U8Rn2` | camila.herrera@example.com | camila.hn |
| `usr_04` | `jxu1C88cWjWoHjOUEx7Xa7U7dwq1` | mateo.gomez@example.com | mateo.gv |
| `usr_05` | `7oQfsSLDGVTTbJHs7e2HpyrKno53` | lucia.fernandez@example.com | lucia.fa |
| `usr_06` | `qPQsjjJzszQz0VQLmFENkDuBhd03` | juan.cortes@example.com | juance.cortes |
| `usr_07` | `udu4MkZiY8hJFC4RN59kaNXKw462` | daniela.penaloza@example.com | dani.sp |
| `usr_08` | `1dmuMswCaUblKViilqDZvxzjCCQ2` | andres.quiroga@example.com | andresfq |
| `usr_09` | `bLjriY9g9GeiL8IYnsdhmPsRS2s1` | ximena.torres@example.com | xime.tb |
| `usr_10` | `YDvdM747nnMZ08yjYXtdy1SCmmO2` | oscar.mendoza@example.com | oscarmendoza |

**Contraseña de todas: `Gaply2026!`** (se puede entrar con email o con usuario).

Verificación tras el seed (lectura de vuelta contra Firestore):
`10 perfiles con 4 clases de horario y 3-5 intereses` · `20 actividades con
ownerId = uid real y attendeeIds = [dueño]` · emoji del `goal` intacto ·
`act_02` sin `imageUri` (usa `defaultColorHex`) ✓.

> Ojo: el proyecto ya tenía datos previos (≈13 usuarios y ≈14 actividades de
> pruebas anteriores); el mazo y las listas los incluirán también.

**Caveats de QA**
1. Las cuentas solo existen en Firestore; para entrar con ellas hay que crear
   usuarios en **Firebase Auth** con esos uids (o cambiar los ids por los reales).
2. La sección *Actividades para tu hueco* del Descubrir solo llena si **hoy**
   el usuario tiene clases (2+) y hay actividad de hoy dentro de su hueco: en
   domingo no hay clases en este set → ese banner queda vacío (comportamiento esperado).
3. `Cupo: N` es informativo (texto de la descripción); la app no lo valida.
