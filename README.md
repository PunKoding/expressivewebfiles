# Expressive Web Files (`com.fotospixel.transfer`)

![Android API](https://img.shields.io/badge/Android-API%2026%E2%80%9335%2B%20(8.0%E2%80%9316%2B)-3DDC84?logo=android&logoColor=white)
![Pure Java](https://img.shields.io/badge/Java-100%25%20Java%20(Sin%20dependencias)-ED8B00?logo=openjdk&logoColor=white)
![Build System](https://img.shields.io/badge/Build-Sin%20Gradle%20(aapt2%20%2B%20javac%20%2B%20d8)-4285F4)
![LAN HTTP](https://img.shields.io/badge/Red-HTTP%2F1.1%20LAN%20(:8080)-0A7EA4)

**Expressive Web Files** es una aplicación Android ligera y sin dependencias externas que combina en una sola herramienta:

1. **Portal Web LAN de alta velocidad** para enviar y recibir archivos y carpetas completas desde cualquier navegador en la misma red Wi-Fi.
2. **Explorador de archivos avanzado estilo MiXplorer** con pestañas múltiples, bibliotecas por categoría, compresión `.zip` / `.tar.gz`, editor de texto, visor de imágenes y soporte para almacenamiento externo (**microSD y USB OTG / Tipo-C**).
3. **Analizador visual de almacenamiento estilo DaisyDisk / SquirrelDisk** con gráfico interactivo **Sunburst** tanto en la app nativa como en la web para detectar y eliminar lo que más espacio ocupa.
4. **Diseño camaleónico nativo** que detecta la versión de Android o la capa del fabricante (One UI, HyperOS, Nothing OS, ColorOS, realme UI, MagicOS, EMUI, Material Design/Theme/You/Expressive) y adapta su interfaz nativa y el portal web en tiempo real.

---

## Características principales

### 1. Portal Web LAN y Servidor HTTP de alto rendimiento (`Servidor`)
- **Motor HTTP/1.1 propio en Java puro** (`TransferServer`): conexiones persistentes *keep-alive*, *pool* de 24 hilos, buffers de 256–512 KB y escritura directa mediante `FileChannel`.
- **Subida paralela por bloques**: transferencias fragmentadas (*chunked upload*) con soporte multihilo configurable desde la interfaz web (1, 2, 4 o 6 hilos simultáneos).
- **Subida de carpetas completas**: conservación de la estructura de subdirectorios mediante selección de carpetas (`webkitdirectory`) o arrastrar y soltar (*drag & drop* recursivo).
- **Descarga directa y ZIP al vuelo**: soporte de cabeceras `HEAD` / `HTTP Range` para descargas reanudables y empaquetado `.zip` en *streaming* de carpetas completas o selecciones múltiples.
- **Telemetría y control en tiempo real**: gráfica de velocidad en vivo (`SpeedGraphView`), código **QR integrado** (ampliable a pantalla completa al tocarlo), cancelación de transferencias activas e historial de sesión.
- **Autenticación opcional**: acceso protegido por usuario y contraseña (`pixel` / `pixel` por defecto) mediante cookies `HttpOnly` (`SameSite=Lax`, sesión de 24 h) o modo abierto configurable desde la app.

### 2. Explorador de archivos avanzado estilo MiXplorer (`Archivos`)
- **Pestañas múltiples** en App y Portal Web (hasta 6 pestañas simultáneas) para mover o comparar rutas rápidamente.
- **Marcadores y bibliotecas inteligentes**: acceso directo a *Imágenes*, *Vídeos*, *Audio*, *Documentos*, *Archivos comprimidos* y *Recientes (7 días)*.
- **Soporte para medios externos (SD y USB OTG / USB-C)**: detección de volúmenes montados (`StorageVolumes`) mediante rutas virtuales `@ext/<volId>` con lectura, navegación y copia entre almacenamiento interno y externo.
- **Operaciones completas de portapapeles**: copiar, cortar, pegar, mover, renombrar, crear carpetas y archivos vacíos, borrado individual o por lotes y búsqueda recursiva.
- **Motor de archivos comprimidos (`.zip` y `.tar.gz` / `.tgz`)**: creación de paquetes, listado de contenido sin descomprimir y extracción segura con protección frente a *Zip-Slip* (`FolderZipper`).
- **Visor y editor integrados**: visor de imágenes en la web, editor de texto/código en vivo y diálogo de propiedades detalladas con cálculo de hashes **MD5 y SHA-256**.

### 3. Analizador visual de almacenamiento estilo DaisyDisk / SquirrelDisk (`Almacenamiento`)
- **Gráfico Sunburst interactivo multicapa**: renderizado nativo en `Canvas` (`SunburstChartView`) y en SVG interactivo en el portal web (`WebUiStorage`).
- **Exploración *drill-down***: toca un sector para entrar en la subcarpeta, toca el núcleo central para subir de nivel o mantén pulsado para marcar elementos pesados.
- **Cola «Liberar espacio»**: añade carpetas o archivos de gran tamaño al colector para revisar el espacio recuperable y eliminarlos en lote con confirmación y progreso en vivo.

### 4. Diseño camaleónico nativo por sistema y capa OEM (`OsDesignSystem` + `OsSkinRenderer`)
- Detección automática de plataforma mediante `Build` y propiedades del sistema, con soporte completo de **Modo Claro / Modo Oscuro**, contraste verificado **WCAG AA** y sincronización de variables CSS hacia la interfaz web (`ThemeTokens`).
- **11 lenguajes de diseño nativos incluidos**:
  - **Capas de fabricante**: Samsung **One UI**, Xiaomi **HyperOS**, **Nothing OS**, OPPO **ColorOS**, **realme UI**, Honor **MagicOS** y Huawei **EMUI / HarmonyOS**.
  - **Android puro por versión**: **Material Design** (Android 8–9), **Material Theme** (Android 10–11), **Material You** (Android 12–15) y **Material 3 Expressive** (Android 16+).
- Incluye un selector manual en **Ajustes** para probar o fijar cualquiera de los diseños en cualquier dispositivo.

### 5. Live Updates (Android 16+) y doble canal de notificaciones en vivo
- **Canal principal de servidor y transferencias (`fotospixel_live_updates`)**: compatible con *Promoted Ongoing Live Updates* de Android 16+ (`Notification.ProgressStyle`) y notificaciones persistentes con botón funcional de **Detener servidor** desde Android 8/9 en adelante (`TransferActionReceiver`).
- **Segundo canal para operaciones de archivos (`fotospixel_file_ops`)**: gestionado por `FileOpMonitor` para mostrar barra de progreso cuando se copian, mueven o eliminan carpetas grandes, indicando en tiempo real qué archivo se está procesando o borrando.

---

## Arquitectura sin dependencias (Zero-Dependency & Zero-Gradle)

El proyecto está escrito íntegramente en **Java 11 + Android SDK** estándar, sin librerías de terceros ni *wrapper* de Gradle. Se compila directamente con las herramientas oficiales del Android SDK (`aapt2`, `javac`, `d8`, `zipalign` y `apksigner`).

### Estructura del proyecto

```text
fotospixel/
├── build.sh
└── app/
    └── src/
        └── main/
            ├── AndroidManifest.xml
            ├── res/
            │   ├── drawable/
            │   └── values/
            └── java/com/fotospixel/transfer/
                ├── MainActivity.java
                ├── TransferService.java
                ├── TransferActionReceiver.java
                ├── TransferServer.java
                ├── TransferStats.java
                ├── WebUi.java
                ├── WebUiExplorer.java
                ├── WebUiStorage.java
                ├── FileOps.java
                ├── FileOpMonitor.java
                ├── FolderZipper.java
                ├── StorageVolumes.java
                ├── StorageAnalyzer.java
                ├── SunburstChartView.java
                ├── SpeedGraphView.java
                ├── QrCodeGenerator.java
                ├── OsDesignSystem.java
                ├── OsSkinRenderer.java
                └── ThemeTokens.java
```

### Módulos principales

| Archivo | Responsabilidad |
| :--- | :--- |
| [`MainActivity.java`](app/src/main/java/com/fotospixel/transfer/MainActivity.java) | Interfaz nativa con 4 secciones (`Servidor`, `Archivos`, `Almacenamiento`, `Ajustes`), permisos y navegación por pestañas. |
| [`TransferServer.java`](app/src/main/java/com/fotospixel/transfer/TransferServer.java) | Servidor HTTP/1.1 embebido, sesiones, subidas por bloques, descargas con rango y API REST `/api/*`. |
| [`TransferService.java`](app/src/main/java/com/fotospixel/transfer/TransferService.java) | Servicio en primer plano (`dataSync`), Live Updates y gestión de los canales de notificación. |
| [`TransferActionReceiver.java`](app/src/main/java/com/fotospixel/transfer/TransferActionReceiver.java) | `BroadcastReceiver` para detener o reiniciar el servidor desde la notificación en todas las versiones soportadas. |
| [`TransferStats.java`](app/src/main/java/com/fotospixel/transfer/TransferStats.java) | Estado global del servidor, métricas de velocidad, transferencias activas e historial. |
| [`WebUi.java`](app/src/main/java/com/fotospixel/transfer/WebUi.java) | SPA HTML/CSS/JS principal del portal web con subida paralela, tema dinámico y navegación por secciones. |
| [`WebUiExplorer.java`](app/src/main/java/com/fotospixel/transfer/WebUiExplorer.java) | Módulo web del explorador multipestaña estilo MiXplorer, editor de texto, visor y operaciones de archivo. |
| [`WebUiStorage.java`](app/src/main/java/com/fotospixel/transfer/WebUiStorage.java) | Módulo web del analizador DaisyDisk/SquirrelDisk con gráfico Sunburst SVG y cesta de limpieza. |
| [`FileOps.java`](app/src/main/java/com/fotospixel/transfer/FileOps.java) | Operaciones de copia, movimiento, borrado recursivo, búsqueda, categorías y hashes MD5/SHA-256. |
| [`FileOpMonitor.java`](app/src/main/java/com/fotospixel/transfer/FileOpMonitor.java) | Seguimiento de progreso para operaciones largas de copia, movimiento y eliminación de archivos/carpetas. |
| [`FolderZipper.java`](app/src/main/java/com/fotospixel/transfer/FolderZipper.java) | Compresión, listado y extracción segura de archivos `.zip` y `.tar.gz`. |
| [`StorageVolumes.java`](app/src/main/java/com/fotospixel/transfer/StorageVolumes.java) | Detección de almacenamiento interno, tarjetas microSD y unidades USB OTG / USB-C (`@ext/<volId>`). |
| [`StorageAnalyzer.java`](app/src/main/java/com/fotospixel/transfer/StorageAnalyzer.java) | Escáner jerárquico de tamaños en disco para alimentar el gráfico Sunburst en App y Web. |
| [`SunburstChartView.java`](app/src/main/java/com/fotospixel/transfer/SunburstChartView.java) | Vista personalizada en `Canvas` para el gráfico radial concéntrico de almacenamiento. |
| [`SpeedGraphView.java`](app/src/main/java/com/fotospixel/transfer/SpeedGraphView.java) | Gráfica en tiempo real de velocidad de subida y descarga en la app nativa. |
| [`QrCodeGenerator.java`](app/src/main/java/com/fotospixel/transfer/QrCodeGenerator.java) | Generador de códigos QR en Java puro para compartir la URL del servidor LAN. |
| [`OsDesignSystem.java`](app/src/main/java/com/fotospixel/transfer/OsDesignSystem.java) | Detección de sistema/OEM y definición de los 11 lenguajes de diseño (claro y oscuro). |
| [`OsSkinRenderer.java`](app/src/main/java/com/fotospixel/transfer/OsSkinRenderer.java) | Aplicación de superficies, radios, elevaciones, barras de navegación y controles nativos por capa. |
| [`ThemeTokens.java`](app/src/main/java/com/fotospixel/transfer/ThemeTokens.java) | Tokens de color y tipografía compartidos entre la interfaz Android y las variables CSS del portal web. |

---

## Requisitos y compilación

### Requisitos previos
- **JDK 11 o superior** (el script usa por defecto OpenJDK 17 en macOS mediante `JAVA_HOME`, configurable con variable de entorno).
- **Android SDK** con:
  - `platforms/android-35`
  - `build-tools/35.0.0` (`aapt2`, `d8`, `zipalign`, `apksigner`)
- Herramienta CLI `zip` y, opcionalmente, `adb` para instalar y abrir la app automáticamente por USB.

### Compilar el APK

```bash
# Opcional: ajusta las rutas si tu JDK o Android SDK están en otra ubicación
export JAVA_HOME="/ruta/a/tu/jdk"
export ANDROID_SDK="$HOME/Library/Android/sdk"   # En Linux suele ser $HOME/Android/Sdk

chmod +x build.sh
./build.sh
```

El script [`build.sh`](build.sh) ejecuta los 6 pasos automáticamente:
1. Compila recursos con `aapt2`.
2. Compila todo el código Java (`-source 11 -target 11`).
3. Genera el bytecode DEX con `d8 --min-api 26`.
4. Empaqueta y alinea el APK con `zip` y `zipalign`.
5. Genera `debug.keystore` si no existe y firma `build/fotospixel.apk` con `apksigner`.
6. Si detecta un dispositivo conectado por `adb`, instala el APK, concede permisos de almacenamiento/notificaciones y lanza la aplicación.

---

## Uso rápido

1. Conecta el teléfono Android y tu ordenador o tablet a la misma red Wi-Fi (o activa el punto de acceso del teléfono).
2. Abre **Expressive Web Files** en el teléfono, concede acceso completo a los archivos y pulsa **Iniciar servidor** en la pestaña **Servidor**.
3. Escanea el código QR desde otro dispositivo o introduce la dirección mostrada (por ejemplo, `http://192.168.1.50:8080`) en el navegador.
4. Gestiona archivos, sube carpetas completas, monta tu tarjeta SD o USB OTG o analiza qué ocupa más espacio desde **Almacenamiento**.

---

## Capturas de pantalla

> Puedes añadir aquí capturas de la app nativa (en distintas capas como Nothing OS, One UI o Material 3 Expressive) y del portal web antes o después de publicar el repositorio.

<!-- Ejemplo:
| Servidor | Archivos | Almacenamiento (Sunburst) | Portal Web |
| :---: | :---: | :---: | :---: |
| ![Servidor](docs/server.png) | ![Archivos](docs/files.png) | ![Almacenamiento](docs/storage.png) | ![Web](docs/web.png) |
-->

---

## Resumen de la API HTTP integrada

| Categoría | Endpoints disponibles |
| :--- | :--- |
| **Autenticación y estado** | `GET /api/auth` · `POST /api/login` · `POST /api/logout` · `GET /api/status` · `POST /api/cancel` |
| **Volúmenes y almacenamiento** | `GET /api/volumes` · `GET /api/analyze` |
| **Explorador y edición** | `GET /api/list` · `GET /api/search` · `GET /api/info` · `GET /api/text` · `POST /api/text` · `POST /api/mkdir` · `POST /api/touch` · `POST /api/rename` · `POST /api/copy` · `POST /api/move` · `DELETE /api/file` · `POST /api/delete_batch` |
| **Transferencias y archivos comprimidos** | `POST\|PUT /api/upload` · `POST /api/upload_init` · `POST\|PUT /api/upload_chunk` · `POST /api/upload_finish` · `GET\|HEAD /api/download` · `GET /api/zip` · `POST /api/zip_create` · `GET /api/archive_list` · `POST /api/extract` |

---

## Preparación para publicar en GitHub

Antes de hacer `git push` a un repositorio público, asegúrate de crear un `.gitignore` en la raíz del proyecto para no subir artefactos generados ni claves locales de firma:

```gitignore
build/
*.apk
*.keystore
*.jks
.DS_Store
.idea/
*.iml
```

---

## Licencia

Añade un archivo `LICENSE` en la raíz del repositorio con la licencia bajo la que quieras distribuir el proyecto (por ejemplo, MIT o Apache-2.0).
