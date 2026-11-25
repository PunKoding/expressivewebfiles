package com.fotospixel.transfer;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Automatic OS-locale localization supporting Spanish (es), English (en),
 * German (de), French (fr), Japanese (ja), and Chinese (zh).
 */
public final class I18n {

    public enum Lang {
        ES(0, "es"),
        EN(1, "en"),
        DE(2, "de"),
        FR(3, "fr"),
        JA(4, "ja"),
        ZH(5, "zh");

        public final int idx;
        public final String code;

        Lang(int idx, String code) {
            this.idx = idx;
            this.code = code;
        }
    }

    private static final Map<String, String[]> EXACT = new HashMap<>();
    private static final Pattern PAT_STORAGE_FMT = Pattern.compile("^(\\d+) carp · (\\d+) arch · ([0-9.,]+%)$");
    private static final Pattern PAT_STORAGE_INT = Pattern.compile("^Almacenamiento interno: (.+) usados · (.+) libres \\((\\d+%)\\)$");
    private static final Pattern PAT_LIBRES_DE = Pattern.compile("^Almacenamiento interno: (.+) libres de (.+) \\((\\d+% usado)\\)$");
    private static final Pattern PAT_TAIL_MORE = Pattern.compile("^\\+ (\\d+) elementos menores \\((.+)\\)$");

    static {
        // Navigation tabs & header status
        p("Servidor", "Server", "Server", "Serveur", "サーバー", "服务器");
        p("Archivos", "Files", "Dateien", "Fichiers", "ファイル", "文件");
        p("Almacenamiento", "Storage", "Speicher", "Stockage", "ストレージ", "存储");
        p("Ajustes", "Settings", "Einstellungen", "Paramètres", "設定", "设置");
        p("En línea", "Online", "Online", "En ligne", "オンライン", "在线");
        p("Detenido", "Stopped", "Gestoppt", "Arrêté", "停止中", "已停止");

        // Server tab & permissions
        p("Velocidad en vivo", "Live speed", "Live-Geschwindigkeit", "Débit en direct", "ライブ速度", "实时速度");
        p("Subida", "Upload", "Upload", "Envoi", "アップロード", "上传");
        p("Descarga", "Download", "Download", "Téléchargement", "ダウンロード", "下载");
        p("Total: 0 B", "Total: 0 B", "Gesamt: 0 B", "Total : 0 B", "合計: 0 B", "总计: 0 B");
        p("Acceso desde navegador", "Browser access", "Browserzugriff", "Accès navigateur", "ブラウザアクセス", "浏览器访问");
        p("Conecta otro dispositivo", "Connect another device", "Anderes Gerät verbinden", "Connecter un autre appareil", "別のデバイスを接続", "连接其他设备");
        p("Toca el código para mostrarlo en pantalla completa.", "Tap QR code for fullscreen view.", "Tippen für Vollbild-QR-Code.", "Touchez le code pour l'agrandir.", "タップして全画面QRを表示", "点击二维码全屏显示");
        p("Copiar URL", "Copy URL", "URL kopieren", "Copier l'URL", "URLをコピー", "复制网址");
        p("Ampliar QR", "Enlarge QR", "QR vergrößern", "Agrandir QR", "QR拡大", "放大二维码");
        p("Detener servidor", "Stop server", "Server stoppen", "Arrêter le serveur", "サーバー停止", "停止服务器");
        p("Iniciar servidor", "Start server", "Server starten", "Démarrer le serveur", "サーバー開始", "启动服务器");
        p("Detener", "Stop", "Stopp", "Arrêter", "停止", "停止");
        p("Iniciar", "Start", "Start", "Démarrer", "開始", "启动");
        p("Reiniciar", "Restart", "Neustart", "Redémarrer", "再起動", "重启");
        p("Transferencias activas", "Active transfers", "Aktive Übertragungen", "Transferts actifs", "実行中の転送", "正在传输");
        p("0 activas", "0 active", "0 aktiv", "0 actif", "0 件", "0 个活跃");
        p("Detener todo", "Stop all", "Alle stoppen", "Tout arrêter", "すべて停止", "全部停止");
        p("Esperando transferencias desde el portal web...", "Waiting for transfers from the web portal...", "Warte auf Übertragungen vom Webportal...", "En attente de transferts depuis le portail web...", "Webポータルからの転送を待機中...", "等待来自网页端的文件传输...");
        p("Historial reciente", "Recent history", "Letzter Verlauf", "Historique récent", "最近の履歴", "最近记录");
        p("Ninguna transferencia completada todavía.", "No completed transfers yet.", "Noch keine Übertragungen abgeschlossen.", "Aucun transfert terminé pour le moment.", "完了した転送はまだありません。", "暂无已完成的传输。");
        p("Permiso de almacenamiento", "Storage permission", "Speicherberechtigung", "Autorisation de stockage", "ストレージ権限", "存储权限");
        p("Concede acceso completo para enviar y guardar archivos en cualquier carpeta y medios USB/SD.", "Grant full access to send and save files in any folder and USB/SD media.", "Vollzugriff erlauben, um Dateien in beliebigen Ordnern und USB/SD zu speichern.", "Accordez l'accès complet pour gérer les fichiers et supports USB/SD.", "すべてのフォルダとUSB/SDメディアにアクセスするためフル権限を許可してください。", "请授予完全存储权限，以便在任意文件夹及 USB/SD 设备中读写文件。");
        p("Conceder acceso", "Grant access", "Zugriff gewähren", "Autoriser l'accès", "アクセスを許可", "授予权限");
        p("Live Updates Android 16/17", "Android 16/17 Live Updates", "Android 16/17 Live Updates", "Mises à jour en direct Android 16/17", "Android 16/17 ライブアップデート", "Android 16/17 实时更新");
        p("Activa la notificación promocionada para ver el progreso en vivo desde la barra de estado y bloqueo.", "Enable promoted ongoing notifications for live progress on the status and lock screen.", "Erweiterte Live-Benachrichtigungen für Status- und Sperrbildschirm aktivieren.", "Activez les notifications en direct sur l'écran de verrouillage et la barre d'état.", "ステータスバーやロック画面で進行状況をリアルタイム表示します。", "开启实时通知以在状态栏和锁屏界面查看传输进度。");
        p("Activar Live Updates", "Enable Live Updates", "Live Updates aktivieren", "Activer Live Updates", "Live Updatesを有効化", "启用实时更新");
        p("Código QR del portal web", "Web portal QR code", "Webportal-QR-Code", "Code QR du portail web", "Webポータル QRコード", "网页端二维码");
        p("Escanea desde el navegador para abrir directamente", "Scan from a browser to open directly", "Im Browser scannen zum direkten Öffnen", "Scannez depuis un navigateur pour ouvrir directement", "ブラウザからスキャンして直接開く", "使用浏览器扫码直接打开");

        // Explorer (Archivos)
        p("Marcadores", "Bookmarks", "Lesezeichen", "Signets", "ブックマーク", "书签");
        p("Buscar", "Search", "Suchen", "Rechercher", "検索", "搜索");
        p("Ordenar", "Sort", "Sortieren", "Trier", "並び替え", "排序");
        p("Seleccionar", "Select", "Auswählen", "Sélectionner", "選択", "选择");
        p("Actualizar", "Refresh", "Aktualisieren", "Actualiser", "更新", "刷新");
        p("+ Nuevo", "+ New", "+ Neu", "+ Nouveau", "+ 新規", "+ 新建");
        p("Raíz", "Root", "Stamm", "Racine", "ルート", "根目录");
        p("Carpeta vacía", "Empty folder", "Leerer Ordner", "Dossier vide", "空のフォルダ", "空文件夹");
        p("Vacía", "Empty", "Leer", "Vide", "空", "空");
        p("Pegar aquí", "Paste here", "Hier einfügen", "Coller ici", "ここに貼り付け", "粘贴到此处");
        p("Pegar", "Paste", "Einfügen", "Coller", "貼り付け", "粘贴");
        p("Limpiar", "Clear", "Leeren", "Effacer", "クリア", "清除");
        p("Copiar", "Copy", "Kopieren", "Copier", "コピー", "复制");
        p("Cortar", "Cut", "Ausschneiden", "Couper", "切り取り", "剪切");
        p("Cortar (Mover)", "Cut (Move)", "Ausschneiden (Verschieben)", "Couper (Déplacer)", "切り取り（移動）", "剪切（移动）");
        p("Eliminar", "Delete", "Löschen", "Supprimer", "削除", "删除");
        p("Renombrar", "Rename", "Umbenennen", "Renommer", "名前変更", "重命名");
        p("Propiedades", "Properties", "Eigenschaften", "Propriétés", "プロパティ", "属性");
        p("Propiedades y hashes", "Properties & hashes", "Eigenschaften & Hashes", "Propriétés & empreintes", "プロパティとハッシュ", "属性与校验");
        p("Propiedades y MD5 / SHA-256", "Properties & MD5 / SHA-256", "Eigenschaften & MD5 / SHA-256", "Propriétés & MD5 / SHA-256", "プロパティ & MD5 / SHA-256", "属性与 MD5 / SHA-256");
        p("Copiar MD5 y SHA-256", "Copy MD5 & SHA-256", "MD5 & SHA-256 kopieren", "Copier MD5 et SHA-256", "MD5とSHA-256をコピー", "复制 MD5 和 SHA-256");
        p("Copiar ruta", "Copy path", "Pfad kopieren", "Copier le chemin", "パスをコピー", "复制路径");
        p("Nueva carpeta", "New folder", "Neuer Ordner", "Nouveau dossier", "新規フォルダ", "新建文件夹");
        p("Nuevo archivo de texto", "New text file", "Neue Textdatei", "Nouveau fichier texte", "新規テキストファイル", "新建文本文件");
        p("Nombre de la carpeta", "Folder name", "Ordnername", "Nom du dossier", "フォルダ名", "文件夹名称");
        p("Nombre", "Name", "Name", "Nom", "名前", "名称");
        p("Nombre (A-Z)", "Name (A-Z)", "Name (A-Z)", "Nom (A-Z)", "名前 (A-Z)", "名称 (A-Z)");
        p("Fecha", "Date", "Datum", "Date", "日付", "日期");
        p("Tamaño", "Size", "Größe", "Taille", "サイズ", "大小");
        p("Tipo", "Type", "Typ", "Type", "種類", "类型");
        p("Ordenar elementos", "Sort items", "Elemente sortieren", "Trier les éléments", "並び替え", "排序方式");
        p("Elige cómo ordenar carpetas y archivos.", "Choose how to sort folders and files.", "Wähle die Sortierung für Ordner und Dateien.", "Choisissez comment trier dossiers et fichiers.", "フォルダとファイルの並び順を選択します。", "选择文件和文件夹的排序方式。");
        p("Acciones en carpeta", "Folder actions", "Ordneraktionen", "Actions du dossier", "フォルダ操作", "文件夹操作");
        p("Marcadores y medios", "Bookmarks & media", "Lesezeichen & Medien", "Signets & supports", "ブックマークとメディア", "书签与存储设备");
        p("Unidades externas y accesos MiXplorer", "External drives & shortcuts", "Externe Laufwerke & Verknüpfungen", "Lecteurs externes et raccourcis", "外部ドライブとショートカット", "外部存储与快捷方式");
        p("BIBLIOTECAS POR TIPO", "SMART LIBRARIES", "BIBLIOTHEKEN NACH TYP", "BIBLIOTHÈQUES PAR TYPE", "カテゴリ別ライブラリ", "分类文件库");
        p("ACCESOS RÁPIDOS", "QUICK ACCESS", "SCHNELLZUGRIFF", "ACCÈS RAPIDES", "クイックアクセス", "快捷入口");
        p("UNIDADES Y MEDIOS EXTERNOS", "EXTERNAL DRIVES & MEDIA", "EXTERNE LAUFWERKE", "SUPPORTS EXTERNES", "外部ストレージ", "外部存储设备");
        p("Vista de carpeta actual", "Current folder view", "Aktueller Ordner", "Dossier actuel", "現在のフォルダ", "当前文件夹视图");
        p("Imágenes", "Images", "Bilder", "Images", "画像", "图片");
        p("Vídeos", "Videos", "Videos", "Vidéos", "動画", "视频");
        p("Audio y música", "Audio & music", "Audio & Musik", "Audio & musique", "オーディオと音楽", "音频与音乐");
        p("Audio", "Audio", "Audio", "Audio", "オーディオ", "音频");
        p("Documentos", "Documents", "Dokumente", "Documents", "ドキュメント", "文档");
        p("Archivos ZIP / TAR.GZ / APK", "ZIP / TAR.GZ / APK archives", "ZIP / TAR.GZ / APK-Archive", "Archives ZIP / TAR.GZ / APK", "ZIP / TAR.GZ / APK 書庫", "压缩包与安装包");
        p("Comprimidos", "Archives", "Archive", "Archives", "アーカイブ", "压缩包");
        p("Recientes (7 días)", "Recent (7 days)", "Neueste (7 Tage)", "Récents (7 jours)", "最近 (7日)", "最近 (7天)");
        p("Cámara (DCIM/Camera)", "Camera (DCIM/Camera)", "Kamera (DCIM/Camera)", "Appareil photo (DCIM/Camera)", "カメラ (DCIM/Camera)", "相机 (DCIM/Camera)");
        p("Limpiar búsqueda", "Clear search", "Suche leeren", "Effacer la recherche", "検索をクリア", "清除搜索");
        p("Empaquetar", "Pack", "Packen", "Compresser", "圧縮", "打包");
        p("Empaquetar (ZIP / TAR.GZ)", "Pack (ZIP / TAR.GZ)", "Packen (ZIP / TAR.GZ)", "Compresser (ZIP / TAR.GZ)", "圧縮 (ZIP / TAR.GZ)", "打包 (ZIP / TAR.GZ)");
        p("Empaquetar carpeta (ZIP / TAR.GZ)", "Pack folder (ZIP / TAR.GZ)", "Ordner packen (ZIP / TAR.GZ)", "Compresser le dossier (ZIP / TAR.GZ)", "フォルダを圧縮 (ZIP / TAR.GZ)", "打包文件夹 (ZIP / TAR.GZ)");
        p("Comprimir ZIP", "Compress ZIP", "Als ZIP packen", "Compresser en ZIP", "ZIPで圧縮", "压缩为 ZIP");
        p("Comprimir como .ZIP", "Compress as .ZIP", "Als .ZIP komprimieren", "Compresser en .ZIP", ".ZIP形式で圧縮", "打包为 .ZIP");
        p("Empaquetar como .TAR.GZ", "Pack as .TAR.GZ", "Als .TAR.GZ packen", "Compresser en .TAR.GZ", ".TAR.GZ形式で圧縮", "打包为 .TAR.GZ");
        p("Extraer", "Extract", "Entpacken", "Extraire", "展開", "解压");
        p("Extraer todo", "Extract all", "Alle entpacken", "Tout extraire", "すべて展開", "全部解压");
        p("Abrir visor / editor de texto", "Open text viewer / editor", "Texteditor öffnen", "Ouvrir l'éditeur de texte", "テキストエディタを開く", "打开文本编辑器");
        p("Editor de texto UTF-8 (máx. 512 KB)", "UTF-8 text editor (max 512 KB)", "UTF-8-Texteditor (max. 512 KB)", "Éditeur texte UTF-8 (max 512 Ko)", "UTF-8テキストエディタ (最大512KB)", "UTF-8 文本编辑器 (最大 512 KB)");
        p("Guardar", "Save", "Speichern", "Enregistrer", "保存", "保存");
        p("Crear", "Create", "Erstellen", "Créer", "作成", "创建");
        p("Crear y abrir", "Create & open", "Erstellen & öffnen", "Créer et ouvrir", "作成して開く", "创建并打开");
        p("Cancelar", "Cancel", "Abbrechen", "Annuler", "キャンセル", "取消");
        p("Cerrar", "Close", "Schließen", "Fermer", "閉じる", "关闭");
        p("Fijar marcador rápido", "Pin bookmark", "Lesezeichen anheften", "Épingler le signet", "ブックマークに固定", "固定书签");
        p("Buscar en archivos", "Search files", "Dateien durchsuchen", "Rechercher dans les fichiers", "ファイルを検索", "搜索文件");
        p("Nombre de archivo o carpeta...", "File or folder name...", "Datei- oder Ordnername...", "Nom du fichier ou dossier...", "ファイルまたはフォルダ名...", "文件或文件夹名称...");
        p("Todo", "All", "Alle", "Tout", "すべて", "全选");

        // Storage (Almacenamiento)
        p("Carpetas por tamaño", "Folders by size", "Ordner nach Größe", "Dossiers par taille", "フォルダ別容量", "文件夹占用排行");
        p("Carpetas más pesadas", "Largest folders", "Größte Ordner", "Dossiers les plus lourds", "最大フォルダ", "最大文件夹");
        p("Toca un sector para profundizar · Centro para subir · Mantén pulsado para marcar",
          "Tap a slice to drill down · Center to go up · Long-press to mark",
          "Tippen zum Öffnen · Mitte für hoch · Gedrückt halten zum Markieren",
          "Touchez un secteur pour ouvrir · Centre pour remonter · Appui long pour marquer",
          "セクターをタップで開く・中央で戻る・長押しで削除マーク",
          "点击扇区深入查看 · 点击中心返回上级 · 长按标记待删");
        p("Mantén pulsado cualquier archivo o carpeta para marcarlo · Toca «Ver lista» para revisar o retirar elementos.",
          "Long-press any file or folder to mark it · Tap «View list» to review or remove items.",
          "Datei oder Ordner gedrückt halten zum Markieren · «Liste ansehen» zum Prüfen oder Entfernen.",
          "Appui long sur un élément pour le marquer · Touchez « Voir la liste » pour vérifier ou retirer.",
          "ファイルやフォルダを長押しでマーク・「リスト表示」で確認や解除ができます。",
          "长按任意文件或文件夹即可标记 · 点击「查看列表」可筛选或取消标记。");
        p("Ver lista", "View list", "Liste ansehen", "Voir la liste", "リスト表示", "查看列表");
        p("Vaciar", "Clear", "Leeren", "Vider", "空にする", "清空");
        p("Eliminar todo", "Delete all", "Alle löschen", "Tout supprimer", "すべて削除", "全部删除");
        p("Eliminar todos", "Delete all", "Alle löschen", "Tout supprimer", "すべて削除", "全部删除");
        p("Eliminar ahora", "Delete now", "Jetzt löschen", "Supprimer maintenant", "今すぐ削除", "立即删除");
        p("Liberar espacio", "Free up space", "Speicher freigeben", "Libérer de l'espace", "空き容量を増やす", "释放空间");
        p("Quitar", "Remove", "Entfernen", "Retirer", "除外", "移除");
        p("Elementos marcados para eliminar", "Marked items for deletion", "Zum Löschen markierte Elemente", "Éléments marqués à supprimer", "削除マーク済み項目", "已标记待删除项目");
        p("Retira los que quieras conservar o elimínalos todos de golpe.",
          "Remove items you want to keep or delete them all at once.",
          "Elemente entfernen, die behalten werden sollen, oder alle löschen.",
          "Retirez ceux à conserver ou supprimez-les tous d'un coup.",
          "残したい項目を外すか、まとめてすべて削除します。",
          "可移除想要保留的项目，或一键全部删除。");
        p("Explorar en Sunburst", "Explore in Sunburst", "Im Sunburst öffnen", "Explorer dans Sunburst", "Sunburstで開く", "在旭日图中查看");
        p("Marcar para Liberar espacio", "Mark to free space", "Zum Freigeben markieren", "Marquer pour libérer", "削除リストに追加", "标记为释放空间");
        p("Quitar de Liberar espacio", "Unmark from deletion", "Markierung aufheben", "Retirer de la liste", "削除リストから外す", "取消删除标记");
        p("Abrir en Archivos", "Open in Files", "In Dateien öffnen", "Ouvrir dans Fichiers", "ファイルで開く", "在文件管理器中打开");
        p("Vacío", "Empty", "Leer", "Vide", "空", "空");
        p("Subir", "Up", "Hoch", "Haut", "上へ", "上级");

        // Settings & Updater (Ajustes)
        p("Control del servidor", "Server control", "Serversteuerung", "Contrôle du serveur", "サーバー制御", "服务器控制");
        p("Servidor detenido", "Server stopped", "Server gestoppt", "Serveur arrêté", "サーバー停止中", "服务器已停止");
        p("Autenticación del portal web", "Web portal authentication", "Webportal-Authentifizierung", "Authentification du portail web", "Webポータル認証", "网页端登录认证");
        p("Define el usuario y contraseña para entrar en la web desde el navegador. Si dejas el usuario vacío, el portal será abierto sin contraseña.",
          "Set username and password for browser access. Leave username empty for passwordless access.",
          "Benutzername und Passwort für den Browser festlegen. Leerer Benutzername öffnet den Zugang ohne Passwort.",
          "Définissez l'identifiant et le mot de passe du portail web. Laissez l'identifiant vide pour un accès libre.",
          "ブラウザ接続用のユーザー名とパスワードを設定します。空欄にするとパスワードなしでアクセスできます。",
          "设置网页登录用户名和密码。若用户名为空，则无需密码即可访问。");
        p("Usuario", "Username", "Benutzername", "Utilisateur", "ユーザー名", "用户名");
        p("Contraseña", "Password", "Passwort", "Mot de passe", "パスワード", "密码");
        p("Guardar y reiniciar servidor", "Save & restart server", "Speichern & Server neu starten", "Enregistrer et redémarrer", "保存してサーバー再起動", "保存并重启服务器");
        p("Almacenamiento y permisos", "Storage & permissions", "Speicher & Berechtigungen", "Stockage et autorisations", "ストレージと権限", "存储与权限");
        p("Diseño nativo del sistema", "Native system design", "Natives Systemdesign", "Design natif du système", "システムネイティブデザイン", "系统原生外观");
        p("Elegir capa / lenguaje visual", "Choose OS skin / design", "Design-Ebene wählen", "Choisir le style visuel", "外観テーマを選択", "选择系统外观风格");
        p("Lenguaje de diseño", "Design language", "Designsprache", "Langage visuel", "デザイン言語", "界面设计语言");
        p("Color Material You (Monet)", "Material You (Monet) color", "Material You (Monet) Farbe", "Couleur Material You (Monet)", "Material You (Monet) カラー", "Material You (Monet) 配色");
        p("Usa el color dinámico del sistema o elige una paleta personalizada.",
          "Use the system dynamic color or choose a custom palette.",
          "Dynamische Systemfarbe nutzen oder eigene Palette wählen.",
          "Utilisez la couleur dynamique du système ou une palette personnalisée.",
          "システムのダイナミックカラーまたはカスタムパレットを選択します。",
          "使用系统动态取色或选择自定义调色板。");
        p("Acerca de", "About", "Über", "À propos", "このアプリについて", "关于");
        p("Buscar actualizaciones", "Check for updates", "Nach Updates suchen", "Rechercher des mises à jour", "アップデートを確認", "检查更新");
        p("Comprobando GitHub...", "Checking GitHub...", "Prüfe GitHub...", "Vérification GitHub...", "GitHubを確認中...", "正在检查 GitHub...");
        p("Auto-actualizar: Activado", "Auto-check updates: On", "Auto-Update: Ein", "Mises à jour auto : Activées", "自動更新チェック: オン", "自动检查更新：开启");
        p("Auto-actualizar: Desactivado", "Auto-check updates: Off", "Auto-Update: Aus", "Mises à jour auto : Désactivées", "自動更新チェック: オフ", "自动检查更新：关闭");
        p("Descargar e instalar", "Download & install", "Herunterladen & installieren", "Télécharger et installer", "ダウンロードしてインストール", "下载并安装");
        p("Más tarde", "Later", "Später", "Plus tard", "あとで", "稍后");
        p("Descargando...", "Downloading...", "Wird heruntergeladen...", "Téléchargement...", "ダウンロード中...", "正在下载...");
        p("Instalar ahora", "Install now", "Jetzt installieren", "Installer maintenant", "今すぐインストール", "立即安装");
        p("Reintentar", "Retry", "Erneut versuchen", "Réessayer", "再試行", "重试");
        p("Cancela descarga", "Cancel download", "Download abbrechen", "Annuler le téléchargement", "ダウンロードを中止", "取消下载");

        // Notification actions & status
        p("Apagar servidor", "Stop server", "Server stoppen", "Arrêter le serveur", "サーバー停止", "关闭服务器");
        p("Detener transferencia", "Stop transfer", "Übertragung stoppen", "Arrêter le transfert", "転送を停止", "停止传输");
        p("Cancelar borrado", "Cancel delete", "Löschen abbrechen", "Annuler la suppression", "削除をキャンセル", "取消删除");
        p("Expressive Web Files · En línea", "Expressive Web Files · Online", "Expressive Web Files · Online", "Expressive Web Files · En ligne", "Expressive Web Files · オンライン", "Expressive Web Files · 在线");
        p("Expressive Web Files · Servidor activo", "Expressive Web Files · Server active", "Expressive Web Files · Server aktiv", "Expressive Web Files · Serveur actif", "Expressive Web Files · サーバー稼働中", "Expressive Web Files · 服务器运行中");
        p("Listo para recibir carpetas y archivos.", "Ready to receive folders and files.", "Bereit für Dateien und Ordner.", "Prêt à recevoir dossiers et fichiers.", "ファイルやフォルダの受信準備ができています。", "已就绪，可接收文件夹和文件。");
        p("Actualización instalada correctamente", "Update installed successfully", "Update erfolgreich installiert", "Mise à jour installée avec succès", "アップデートが正常にインストールされました", "更新安装成功");
    }

    private static void p(String es, String en, String de, String fr, String ja, String zh) {
        EXACT.put(es, new String[]{en, de, fr, ja, zh});
    }

    public static Lang detect() {
        String code = Locale.getDefault().getLanguage();
        if (code == null) return Lang.ES;
        String l = code.toLowerCase(Locale.US);
        if (l.startsWith("en")) return Lang.EN;
        if (l.startsWith("de")) return Lang.DE;
        if (l.startsWith("fr")) return Lang.FR;
        if (l.startsWith("ja")) return Lang.JA;
        if (l.startsWith("zh")) return Lang.ZH;
        return Lang.ES;
    }

    public static String tr(String text) {
        if (text == null || text.isEmpty()) return text;
        Lang lang = detect();
        if (lang == Lang.ES) return text;
        int i = lang.idx - 1;

        String[] hit = EXACT.get(text);
        if (hit != null) {
            return hit[i];
        }

        if (text.contains("\n")) {
            String[] lines = text.split("\n", -1);
            StringBuilder out = new StringBuilder(text.length() + 16);
            for (int k = 0; k < lines.length; k++) {
                if (k > 0) out.append('\n');
                out.append(tr(lines[k]));
            }
            return out.toString();
        }

        // Dynamic patterns
        if (text.startsWith("Total: ")) {
            String[] lbl = {"Total: ", "Gesamt: ", "Total : ", "合計: ", "总计: "};
            return lbl[i] + text.substring(7);
        }
        if (text.startsWith("IP alternativa (toca): ")) {
            String[] lbl = {"Alt IP (tap): ", "Alt.-IP (tippen): ", "IP alt. (toucher) : ", "代替IP (タップ): ", "备用 IP (点击): "};
            return lbl[i] + text.substring("IP alternativa (toca): ".length());
        }
        if (text.startsWith("IP alternativa: ")) {
            String[] lbl = {"Alt IP: ", "Alt.-IP: ", "IP alt. : ", "代替IP: ", "备用 IP: "};
            return lbl[i] + text.substring("IP alternativa: ".length());
        }
        if (text.startsWith("Activo en ")) {
            String[] lbl = {"Active at ", "Aktiv unter ", "Actif sur ", "稼働中: ", "运行于 "};
            return lbl[i] + text.substring("Activo en ".length());
        }
        if (text.startsWith("Detectado: ")) {
            String[] lbl = {"Detected: ", "Erkannt: ", "Détecté : ", "検出: ", "检测到: "};
            return lbl[i] + text.substring("Detectado: ".length());
        }
        if (text.startsWith("Activo: ")) {
            String[] lbl = {"Active: ", "Aktiv: ", "Actif : ", "現在: ", "当前: "};
            return lbl[i] + text.substring("Activo: ".length());
        }
        if (text.startsWith("Color Monet: ")) {
            String[] lbl = {"Monet color: ", "Monet-Farbe: ", "Couleur Monet : ", "Monetカラー: ", "Monet 配色: "};
            return lbl[i] + text.substring("Color Monet: ".length());
        }
        if (text.startsWith("Automático (")) {
            String[] lbl = {"Automatic (", "Automatisch (", "Automatique (", "自動 (", "自动 ("};
            return lbl[i] + text.substring("Automático (".length());
        }
        if (text.startsWith("Comprobación automática: ")) {
            String rest = text.substring("Comprobación automática: ".length());
            String[] on = {"Enabled", "Aktiviert", "Activée", "オン", "已开启"};
            String[] off = {"Disabled", "Deaktiviert", "Désactivée", "オフ", "已关闭"};
            rest = rest.replace("Activada", on[i]).replace("Desactivada", off[i]);
            String[] lbl = {"Auto-check: ", "Auto-Prüfung: ", "Vérif. auto : ", "自動確認: ", "自动检查: "};
            return lbl[i] + rest;
        }
        if (text.startsWith("Nueva versión disponible: ")) {
            String[] lbl = {"New version available: ", "Neue Version verfügbar: ", "Nouvelle version disponible : ", "新しいバージョン: ", "发现新版本: "};
            return lbl[i] + text.substring("Nueva versión disponible: ".length());
        }
        if (text.startsWith("Al día · Última versión ")) {
            String[] lbl = {"Up to date · Latest ", "Aktuell · Neueste ", "À jour · Dernière ", "最新です · ", "已是最新 · "};
            return lbl[i] + text.substring("Al día · Última versión ".length());
        }
        if (text.startsWith("Actualización ")) {
            String[] lbl = {"Update ", "Update ", "Mise à jour ", "アップデート ", "更新 "};
            return lbl[i] + text.substring("Actualización ".length());
        }
        if (text.startsWith("Instalada: ")) {
            String[] lbl = {"Installed: ", "Installiert: ", "Installée : ", "インストール済み: ", "当前版本: "};
            return lbl[i] + text.substring("Instalada: ".length());
        }
        if (text.startsWith("Portal Web · ")) {
            String[] lbl = {"Web Portal · ", "Webportal · ", "Portail Web · ", "Webポータル · ", "网页端 · "};
            return lbl[i] + text.substring("Portal Web · ".length());
        }
        if (text.startsWith("Portal Web: ")) {
            String[] lbl = {"Web Portal: ", "Webportal: ", "Portail Web : ", "Webポータル: ", "网页端: "};
            return lbl[i] + text.substring("Portal Web: ".length());
        }
        if (text.startsWith("Subiendo: ")) {
            String[] lbl = {"Uploading: ", "Senden: ", "Envoi : ", "送信中: ", "正在上传: "};
            return lbl[i] + text.substring("Subiendo: ".length());
        }
        if (text.startsWith("Descargando: ")) {
            String[] lbl = {"Downloading: ", "Laden: ", "Téléchargement : ", "受信中: ", "正在下载: "};
            return lbl[i] + text.substring("Descargando: ".length());
        }
        if (text.startsWith("Eliminando: ")) {
            String[] lbl = {"Deleting: ", "Lösche: ", "Suppression : ", "削除中: ", "正在删除: "};
            return lbl[i] + text.substring("Eliminando: ".length());
        }
        if (text.startsWith("Borrando: ") || text.startsWith("Borrando ahora: ")) {
            int colon = text.indexOf(": ");
            String[] lbl = {"Deleting: ", "Lösche: ", "Suppression : ", "削除中: ", "正在删除: "};
            return lbl[i] + text.substring(colon + 2);
        }
        if (text.startsWith("Almacenamiento completo: ")) {
            String[] granted = {"Granted", "Erteilt", "Accordé", "許可済み", "已授权"};
            String[] pending = {"Pending", "Ausstehend", "En attente", "保留", "待授权"};
            String[] liveOn = {"Enabled", "Aktiv", "Activé", "有効", "已开启"};
            String[] off = {"Pending", "Ausstehend", "En attente", "保留", "待授权"};
            String s = text.replace("Almacenamiento completo: ", new String[]{"Full storage: ", "Vollzugriff: ", "Stockage complet : ", "全ストレージ権限: ", "完全存储权限: "}[i]);
            s = s.replace("Concedido", granted[i]).replace("Pendiente", pending[i]);
            s = s.replace("Notificación en vivo: Activa", new String[]{"Live notification: Active", "Live-Mitteilung: Aktiv", "Notification en direct : Active", "ライブ通知: 有効", "实时通知: 已开启"}[i]);
            s = s.replace("Activado", liveOn[i]);
            if (s.contains("Pendiente")) s = s.replace("Pendiente", off[i]);
            return s;
        }
        Matcher mInt = PAT_STORAGE_INT.matcher(text);
        if (mInt.matches()) {
            String[] tpl = {
                    "Internal storage: %s used · %s free (%s)",
                    "Interner Speicher: %s belegt · %s frei (%s)",
                    "Stockage interne : %s utilisés · %s libres (%s)",
                    "内部ストレージ: %s 使用 · %s 空き (%s)",
                    "内部存储: 已用 %s · 可用 %s (%s)"
            };
            return String.format(Locale.US, tpl[i], mInt.group(1), mInt.group(2), mInt.group(3));
        }
        Matcher mLib = PAT_LIBRES_DE.matcher(text);
        if (mLib.matches()) {
            String[] tpl = {
                    "Internal storage: %s free of %s (%s)",
                    "Interner Speicher: %s frei von %s (%s)",
                    "Stockage interne : %s libres sur %s (%s)",
                    "内部ストレージ: %s 空き / %s (%s)",
                    "内部存储: %s 可用 / 共 %s (%s)"
            };
            return String.format(Locale.US, tpl[i], mLib.group(1), mLib.group(2), mLib.group(3));
        }
        Matcher mFmt = PAT_STORAGE_FMT.matcher(text);
        if (mFmt.matches()) {
            String[] tpl = {
                    "%s folders · %s files · %s",
                    "%s Ordner · %s Dateien · %s",
                    "%s dossiers · %s fichiers · %s",
                    "%s フォルダ · %s ファイル · %s",
                    "%s 个文件夹 · %s 个文件 · %s"
            };
            return String.format(Locale.US, tpl[i], mFmt.group(1), mFmt.group(2), mFmt.group(3));
        }
        Matcher mTail = PAT_TAIL_MORE.matcher(text);
        if (mTail.matches()) {
            String[] tpl = {
                    "+ %s smaller items (%s)",
                    "+ %s kleinere Elemente (%s)",
                    "+ %s petits éléments (%s)",
                    "+ %s 件の小項目 (%s)",
                    "+ %s 个较小项目 (%s)"
            };
            return String.format(Locale.US, tpl[i], mTail.group(1), mTail.group(2));
        }
        if (text.startsWith("Archivo · ")) {
            return new String[]{"File · ", "Datei · ", "Fichier · ", "ファイル · ", "文件 · "}[i] + text.substring("Archivo · ".length());
        }
        if (text.contains(" · ") && (text.contains(" marcado") || text.contains(" libres"))) {
            String s = text;
            s = s.replace(" elementos", new String[]{" items", " Elemente", " éléments", " 件", " 项"}[i]);
            s = s.replace(" elemento", new String[]{" item", " Element", " élément", " 件", " 项"}[i]);
            s = s.replace(" marcados", new String[]{" marked", " markiert", " marqués", " 選択中", " 已标记"}[i]);
            s = s.replace(" libres)", new String[]{" free)", " frei)", " libres)", " 空き)", " 可用)"}[i]);
            return s;
        }
        if (text.endsWith(" activas") || text.endsWith(" activa")) {
            return text.replace(" activas", new String[]{" active", " aktiv", " actifs", " 件", " 个活跃"}[i])
                    .replace(" activa", new String[]{" active", " aktiv", " actif", " 件", " 个活跃"}[i]);
        }
        if (text.contains(" carpetas") || text.contains(" archivos") || text.contains(" carpeta") || text.contains(" archivo")) {
            return text.replace(" carpetas", new String[]{" folders", " Ordner", " dossiers", " フォルダ", " 个文件夹"}[i])
                    .replace(" carpeta", new String[]{" folder", " Ordner", " dossier", " フォルダ", " 个文件夹"}[i])
                    .replace(" archivos", new String[]{" files", " Dateien", " fichiers", " ファイル", " 个文件"}[i])
                    .replace(" archivo", new String[]{" file", " Datei", " fichier", " ファイル", " 个文件"}[i]);
        }
        return text;
    }

    public static void translateViewTree(View root) {
        if (root == null || detect() == Lang.ES) return;
        if (root instanceof TextView && !(root instanceof EditText)) {
            TextView tv = (TextView) root;
            CharSequence cur = tv.getText();
            if (cur != null && cur.length() > 0) {
                String orig = cur.toString();
                String mapped = tr(orig);
                if (!orig.equals(mapped)) {
                    tv.setText(mapped);
                }
            }
            CharSequence hint = tv.getHint();
            if (hint != null && hint.length() > 0) {
                String oh = hint.toString();
                String mh = tr(oh);
                if (!oh.equals(mh)) tv.setHint(mh);
            }
        } else if (root instanceof EditText) {
            EditText et = (EditText) root;
            CharSequence hint = et.getHint();
            if (hint != null && hint.length() > 0) {
                String oh = hint.toString();
                String mh = tr(oh);
                if (!oh.equals(mh)) et.setHint(mh);
            }
        }
        CharSequence cd = root.getContentDescription();
        if (cd != null && cd.length() > 0) {
            String ocd = cd.toString();
            String mcd = tr(ocd);
            if (!ocd.equals(mcd)) root.setContentDescription(mcd);
        }
        if (root instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) root;
            int n = vg.getChildCount();
            for (int i = 0; i < n; i++) {
                translateViewTree(vg.getChildAt(i));
            }
        }
    }
}
