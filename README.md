# Recapped

Aplicación móvil nativa para Android que analiza los hábitos musicales de un
usuario de Last.fm y genera recaps personalizados con estadísticas, rankings,
géneros, recomendaciones mediante inteligencia artificial e integración con
Spotify.

El proyecto fue desarrollado en Kotlin con Jetpack Compose y sigue una
arquitectura MVVM con Repository Pattern, inyección de dependencias mediante
Hilt y persistencia local y remota.

## Funcionalidades

- Inicio de sesión con Google mediante Firebase Authentication.
- Vinculación y validación de una cuenta de Last.fm.
- Consulta de artistas y canciones más escuchados.
- Rankings con visualización en formato grilla o lista.
- Búsqueda remota de artistas y canciones.
- Pantalla de detalle de artistas.
- Pantalla de detalle de canciones y álbumes.
- Detección de artistas que el usuario dejó de escuchar recientemente.
- Generación de recaps por semana, mes, trimestre o año.
- Análisis musical personalizado mediante inteligencia artificial.
- Recomendación de nuevos artistas según los hábitos musicales.
- Apertura de artistas y canciones en Spotify.
- Historial local de recaps generados.
- Configuración de período predeterminado.
- Generación, previsualización y uso compartido de una imagen del recap.
- Manejo de estados de carga, éxito, error y ausencia de resultados.
- Caché local de rankings para mejorar la disponibilidad de los datos.

## Tecnologías

| Tecnología | Uso |
|---|---|
| Kotlin | Lenguaje principal |
| Jetpack Compose | Construcción de la interfaz |
| Material 3 | Componentes y estilos visuales |
| MVVM | Separación entre interfaz, estado y acceso a datos |
| StateFlow | Estado reactivo de las pantallas |
| Coroutines | Operaciones asíncronas |
| Navigation Compose | Navegación entre pantallas |
| Hilt | Inyección de dependencias |
| Retrofit | Consumo de APIs REST |
| OkHttp | Cliente HTTP, interceptores y timeouts |
| Moshi | Conversión entre JSON y objetos Kotlin |
| Glide | Carga y caché de imágenes remotas |
| Room | Base de datos local |
| DataStore | Preferencias y estado del onboarding |
| SharedPreferences | Persistencia de tokens de Spotify |
| Firebase Authentication | Autenticación con Google |
| Cloud Firestore | Perfil y cuenta vinculada de Last.fm |
| Credential Manager | Obtención de credenciales de Google |
| JUnit y MockK | Pruebas unitarias |

## Servicios externos

### Last.fm

Es la fuente principal de las estadísticas musicales. Se utiliza para obtener:

- Artistas y canciones más escuchados.
- Cantidad de reproducciones.
- Artistas y canciones únicas.
- Historial reciente.
- Biografías de artistas.
- Etiquetas y géneros musicales.

### Deezer

Se utiliza para enriquecer los datos musicales:

- Imágenes de artistas.
- Portadas de canciones y álbumes.
- Búsqueda remota de artistas y canciones.
- Información de álbumes.
- Listado de canciones de un álbum.
- Obtención del código ISRC de una grabación.

Deezer también funciona como respaldo visual cuando Last.fm no devuelve una
imagen válida.

### Groq

La aplicación envía a Groq las estadísticas reales obtenidas desde Last.fm.
El modelo genera una respuesta JSON con:

- Un título creativo para el recap.
- Un resumen personalizado.
- Tres recomendaciones de artistas.

La inteligencia artificial no consulta directamente las cuentas ni las bases
de datos. El código reúne primero la información, construye un prompt y envía
únicamente los datos necesarios para generar el análisis.

Modelo configurado:

```text
llama-3.1-8b-instant
```

### Spotify

La integración utiliza OAuth 2.0 con PKCE, apropiado para aplicaciones móviles
que no pueden almacenar de manera segura un `client_secret`.

Para abrir una canción:

1. Deezer proporciona el ISRC de la grabación.
2. La aplicación busca ese ISRC en Spotify.
3. Spotify devuelve su identificador, URI y enlace público.
4. La aplicación abre el enlace correspondiente.

Para los artistas se realiza una búsqueda por nombre y se prioriza la
coincidencia exacta.

## Arquitectura

El proyecto utiliza MVVM junto con Repository Pattern:

```text
Pantalla Compose
       |
       v
ViewModel + UiState
       |
       v
Repositorio
       |
       +---- API remota
       +---- Room
       +---- Firebase
       +---- DataStore
```

### Capa de presentación

Contiene las pantallas Compose, componentes reutilizables y ViewModels.

Las pantallas observan un `StateFlow` mediante
`collectAsStateWithLifecycle()`. Las acciones del usuario se comunican al
ViewModel mediante callbacks. Cuando el estado cambia, Compose recompone sólo
los elementos necesarios.

Cada pantalla maneja estados explícitos, como:

```text
Loading -> Success -> Error
```

### Capa de dominio

Contiene los modelos internos utilizados por la aplicación, por ejemplo:

- `Artist`
- `ArtistDetail`
- `SongDetail`
- `RecapResult`
- `RecapArtist`
- `RecapTrack`
- `RecapRecommendation`
- `StoredRecap`
- `User`

También incluye `Resource`, una clase sellada que representa resultados de
carga, éxito o error.

### Capa de datos

Los repositorios concentran la lógica de acceso a datos y evitan que los
ViewModels dependan directamente de Retrofit, Firebase o Room.

Los DTO representan la estructura JSON de cada servicio externo. Moshi los
convierte en objetos Kotlin y los repositorios los transforman en modelos del
dominio.

## Flujo de datos

El proyecto utiliza un flujo unidireccional:

```text
Acción del usuario
        |
        v
     Pantalla
        |
        v
    ViewModel
        |
        v
   Repositorio
        |
        v
 Nuevo UiState
        |
        v
Recomposición de la pantalla
```

Este enfoque hace que el estado sea predecible, centraliza la lógica y facilita
las pruebas unitarias.

## Inyección de dependencias

Hilt crea y proporciona las dependencias requeridas por la aplicación.

Módulos principales:

- `NetworkModule`: Retrofit, OkHttp, Moshi y APIs externas.
- `FirebaseModule`: Firebase Authentication y Firestore.
- `DatabaseModule`: Room y los DAO.
- `RepositoryModule`: vinculación entre interfaces e implementaciones.

Anotaciones utilizadas:

- `@HiltAndroidApp`
- `@AndroidEntryPoint`
- `@HiltViewModel`
- `@Inject`
- `@Provides`
- `@Binds`
- `@Singleton`

Gracias a esta configuración, los ViewModels reciben repositorios por
constructor y pueden probarse utilizando mocks.

## Persistencia

| Sistema | Información almacenada |
|---|---|
| Firebase Authentication | Sesión, UID, nombre, correo y foto |
| Cloud Firestore | Perfil y usuario vinculado de Last.fm |
| Room | Caché de artistas e historial de recaps |
| DataStore | Estado del onboarding y preferencias |
| SharedPreferences | Tokens y datos temporales de OAuth de Spotify |

### Room

La base de datos local contiene dos entidades:

- `ArtistEntity`: almacena rankings por usuario y período.
- `RecapEntity`: almacena los recaps asociados al UID autenticado.

El recap completo se serializa como JSON mediante Moshi. Esto permite
reconstruir un `RecapResult` al abrir un elemento del historial.

La base de datos incluye una migración de la versión 1 a la versión 2 para
incorporar la tabla de recaps sin eliminar los datos existentes.

## Generación del recap

El flujo principal es el siguiente:

1. Se obtiene el usuario de Last.fm vinculado.
2. Se consultan en paralelo artistas, canciones y reproducciones.
3. Se calculan las estadísticas generales.
4. Se enriquecen artistas y canciones con imágenes de Deezer.
5. Se calculan los géneros según las etiquetas y reproducciones.
6. Se construye el prompt para Groq.
7. Groq devuelve el título, resumen y recomendaciones en JSON.
8. Deezer aporta imágenes para los artistas recomendados.
9. Se crea el objeto `RecapResult`.
10. El recap se almacena en Room y se muestra en pantalla.

## Imagen para compartir

`RecapShareImageGenerator` crea una imagen JPEG de `1080 x 1920` píxeles
utilizando `Canvas` y `Bitmap`.

La imagen incluye:

- Período analizado.
- Título generado por IA.
- Estadísticas.
- Artistas principales.
- Canciones principales.
- Géneros.
- Identidad visual de Recapped.

El archivo se guarda temporalmente en caché y se comparte mediante
`FileProvider`, evitando exponer rutas internas del dispositivo.

## Estructura del proyecto

```text
com.recapped.app/
|-- MainActivity.kt
|-- RecappedApp.kt
|-- data/
|   |-- local/
|   |   |-- dao/
|   |   |-- entity/
|   |   |-- RecappedDatabase.kt
|   |   `-- RecapLocalMapper.kt
|   |-- remote/
|   |   |-- dto/
|   |   |-- LastFmApi.kt
|   |   |-- DeezerApi.kt
|   |   |-- SpotifyApi.kt
|   |   `-- GroqApi.kt
|   `-- repository/
|-- di/
|   |-- DatabaseModule.kt
|   |-- FirebaseModule.kt
|   |-- NetworkModule.kt
|   `-- RepositoryModule.kt
|-- domain/
|   |-- model/
|   `-- Resource.kt
|-- ui/
|   |-- charts/
|   |-- components/
|   |-- detail/
|   |-- home/
|   |-- login/
|   |-- onboarding/
|   |-- profile/
|   |-- recap/
|   |-- songdetail/
|   |-- theme/
|   |-- RecappedNavGraph.kt
|   `-- RootViewModel.kt
`-- util/
    `-- RecapShareImageGenerator.kt
```

## Pantallas

- Splash.
- Inicio de sesión.
- Onboarding y vinculación con Last.fm.
- Inicio.
- Rankings y búsqueda.
- Detalle de artista.
- Detalle de canción.
- Generación de recap.
- Resultado del recap.
- Perfil.
- Edición del perfil.
- Historial de recaps.

La navegación inferior permite acceder a Inicio, Rankings, Recap y Perfil.

## Requisitos

- Android Studio.
- JDK 17.
- Android SDK 35.
- Dispositivo o emulador con Android 8.0, API 26, o superior.
- Proyecto configurado en Firebase.
- Credenciales de Last.fm, Spotify y Groq.

## Configuración

### 1. Clonar el repositorio

```bash
git clone <URL_DEL_REPOSITORIO>
cd RecappedAndroid
```

### 2. Configurar Firebase

Agregar el archivo correspondiente al proyecto de Firebase:

```text
app/google-services.json
```

En Firebase deben estar habilitados:

- Authentication con proveedor Google.
- Cloud Firestore.

### 3. Configurar Groq

Agregar la API key en `local.properties`:

```properties
GROQ_API_KEY=tu_api_key
```

Este archivo no debe subirse al repositorio.

### 4. Configurar Last.fm y Spotify

Definir en la configuración de compilación:

- `LASTFM_API_KEY`
- `GOOGLE_WEB_CLIENT_ID`
- `SPOTIFY_CLIENT_ID`
- `SPOTIFY_REDIRECT_URI`

El redirect URI utilizado por la aplicación debe estar registrado también en
el panel de Spotify:

```text
com.recapped.app://spotify-callback
```

### 5. Sincronizar y ejecutar

Desde Android Studio:

1. Abrir el proyecto.
2. Ejecutar **Sync Project with Gradle Files**.
3. Seleccionar un emulador o dispositivo.
4. Ejecutar la aplicación.

También puede compilarse desde una terminal:

```bash
./gradlew assembleDebug
```

En Windows:

```powershell
.\gradlew.bat assembleDebug
```

## Pruebas unitarias

Las pruebas se encuentran en:

```text
app/src/test/java/com/recapped/app/
```

Casos cubiertos:

- Login exitoso.
- Error de autenticación.
- Actualización y limpieza del estado de error.
- Validación del usuario de Last.fm.
- Finalización y omisión del onboarding.
- Búsqueda de artistas mediante Deezer.
- Eliminación de resultados duplicados.
- Manejo de errores de conexión.
- Validación de búsquedas demasiado cortas.
- Lectura del caché de Room.
- Conservación del caché cuando falla la API.
- Actualización de Room con resultados remotos.

Ejecutar todas las pruebas:

```bash
./gradlew testDebugUnitTest
```

En Windows:

```powershell
.\gradlew.bat testDebugUnitTest
```

El reporte HTML se genera en:

```text
app/build/reports/tests/testDebugUnitTest/index.html
```

## Manejo de errores y rendimiento

- Estados explícitos de carga, éxito y error.
- Mensajes diferenciados para errores HTTP y problemas de conexión.
- Timeouts configurados en OkHttp.
- Corrutinas para evitar bloquear la interfaz.
- Consultas paralelas al generar el recap.
- Límite de concurrencia al solicitar imágenes.
- Debounce de búsqueda para reducir llamadas innecesarias.
- Caché de artistas mediante Room.
- Caché temporal del artista olvidado.
- Renovación automática del token de Spotify.
- Carga y caché de imágenes mediante Glide.

## Seguridad

- Autenticación administrada por Firebase.
- OAuth 2.0 con PKCE para Spotify.
- Validación del parámetro `state` durante el callback de Spotify.
- La clave privada de Groq se obtiene desde `local.properties`.
- Uso de `FileProvider` para compartir archivos.
- Separación de datos locales por UID.
- El archivo `local.properties` no debe versionarse.

## Autoría

Proyecto académico desarrollado como aplicación Android nativa para el análisis
y la visualización personalizada de hábitos musicales.
