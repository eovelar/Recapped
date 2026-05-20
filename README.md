App nativa Android para analizar hábitos musicales en base a Last.fm.


| Requisito | Dónde está |
|---|---|
| Splash con API nativa | `Theme.Recapped.Starting` (`themes.xml`) + `installSplashScreen()` en `MainActivity` |
| Auth Firebase (Gmail) con persistencia | `AuthRepositoryImpl` + `LoginRoute` (Credential Manager + Google ID Token) |
| Listado con búsqueda y estados Loading/Success/Error | `ChartsViewModel` (`ChartsPhase`) + `ChartsRoute` |
| Pantalla de detalle | `DetailViewModel` + `DetailRoute` |
| Kotlin + Jetpack Compose | Todo el módulo `ui/` |
| State Hoisting | Cada screen es función pura que recibe state + callbacks |
| `collectAsStateWithLifecycle` | Usado en todas las screens conectadas a un VM |
| MVVM + Repository Pattern | `data/repository/` (interfaz + impl) ↔ `ui/<screen>/<Screen>ViewModel` |
| Inyección de dependencias | Hilt (`di/NetworkModule`, `FirebaseModule`, `RepositoryModule`) |
| Retrofit | `LastFmApi` + `NetworkModule` |
| Glide | `GlideImage` en charts y detail |

## 🏗️ Arquitectura

```
app/src/main/java/com/recapped/app/
├── RecappedApp.kt              # @HiltAndroidApp
├── MainActivity.kt             # Splash API + setContent
├── di/
│   ├── NetworkModule.kt        # Retrofit, OkHttp, Moshi
│   ├── FirebaseModule.kt       # FirebaseAuth
│   └── RepositoryModule.kt     # @Binds de interfaces
├── data/
│   ├── remote/
│   │   ├── LastFmApi.kt
│   │   └── dto/                # ChartDtos.kt, ArtistInfoDtos.kt
│   └── repository/
│       ├── ArtistRepository.kt + Impl
│       └── AuthRepository.kt + Impl
├── domain/
│   ├── Resource.kt
│   └── model/                  # Artist.kt, User.kt
└── ui/
    ├── RootViewModel.kt        # Determina si entrar a Login o Charts
    ├── RecappedNavGraph.kt
    ├── theme/                  # Color, Type, Theme
    ├── components/             # Glass, Chip, GradientText
    ├── login/
    ├── charts/                 # Listado + búsqueda
    └── detail/                 # Pantalla profunda
```
