package com.fotospixel.transfer;

/**
 * Automatic browser-locale localization JS module for WebUi (es, en, de, fr, ja, zh).
 */
public final class WebUiI18n {

    private WebUiI18n() {}

    public static final String I18N_JS =
        "const WEB_LANG = (() => {\n" +
        "  const l = ((navigator.language || 'es') + '').toLowerCase().slice(0, 2);\n" +
        "  return ['es','en','de','fr','ja','zh'].includes(l) ? l : 'es';\n" +
        "})();\n" +
        "document.documentElement.lang = WEB_LANG;\n" +
        "const TR_MAP = {\n" +
        "  'Servidor': {en:'Server',de:'Server',fr:'Serveur',ja:'サーバー',zh:'服务器'},\n" +
        "  'Archivos': {en:'Files',de:'Dateien',fr:'Fichiers',ja:'ファイル',zh:'文件'},\n" +
        "  'Almacenamiento': {en:'Storage',de:'Speicher',fr:'Stockage',ja:'ストレージ',zh:'存储'},\n" +
        "  'Ajustes': {en:'Settings',de:'Einstellungen',fr:'Paramètres',ja:'設定',zh:'设置'},\n" +
        "  'En línea': {en:'Online',de:'Online',fr:'En ligne',ja:'オンライン',zh:'在线'},\n" +
        "  'Detenido': {en:'Stopped',de:'Gestoppt',fr:'Arrêté',ja:'停止中',zh:'已停止'},\n" +
        "  'Velocidad en vivo': {en:'Live speed',de:'Live-Geschwindigkeit',fr:'Débit en direct',ja:'ライブ速度',zh:'实时速度'},\n" +
        "  'Subida': {en:'Upload',de:'Upload',fr:'Envoi',ja:'アップロード',zh:'上传'},\n" +
        "  'Descarga': {en:'Download',de:'Download',fr:'Téléchargement',ja:'ダウンロード',zh:'下载'},\n" +
        "  'Transferencias activas': {en:'Active transfers',de:'Aktive Übertragungen',fr:'Transferts actifs',ja:'実行中の転送',zh:'正在传输'},\n" +
        "  'Detener todo': {en:'Stop all',de:'Alle stoppen',fr:'Tout arrêter',ja:'すべて停止',zh:'全部停止'},\n" +
        "  'Enviar archivos o carpetas': {en:'Send files or folders',de:'Dateien oder Ordner senden',fr:'Envoyer fichiers ou dossiers',ja:'ファイルまたはフォルダを送信',zh:'发送文件或文件夹'},\n" +
        "  'Subir archivos': {en:'Upload files',de:'Dateien hochladen',fr:'Envoyer des fichiers',ja:'ファイルを送信',zh:'上传文件'},\n" +
        "  'Subir carpeta': {en:'Upload folder',de:'Ordner hochladen',fr:'Envoyer un dossier',ja:'フォルダを送信',zh:'上传文件夹'},\n" +
        "  'Nueva carpeta': {en:'New folder',de:'Neuer Ordner',fr:'Nouveau dossier',ja:'新規フォルダ',zh:'新建文件夹'},\n" +
        "  'Historial reciente': {en:'Recent history',de:'Letzter Verlauf',fr:'Historique récent',ja:'最近の履歴',zh:'最近记录'},\n" +
        "  'Explorador de archivos': {en:'File explorer',de:'Datei-Explorer',fr:'Explorateur de fichiers',ja:'ファイルエクスプローラー',zh:'文件管理器'},\n" +
        "  'Descargar ZIP': {en:'Download ZIP',de:'ZIP herunterladen',fr:'Télécharger ZIP',ja:'ZIPダウンロード',zh:'下载 ZIP'},\n" +
        "  'Descargar': {en:'Download',de:'Download',fr:'Télécharger',ja:'ダウンロード',zh:'下载'},\n" +
        "  'Copiar': {en:'Copy',de:'Kopieren',fr:'Copier',ja:'コピー',zh:'复制'},\n" +
        "  'Cortar': {en:'Cut',de:'Ausschneiden',fr:'Couper',ja:'切り取り',zh:'剪切'},\n" +
        "  'Eliminar': {en:'Delete',de:'Löschen',fr:'Supprimer',ja:'削除',zh:'删除'},\n" +
        "  'Pegar aquí': {en:'Paste here',de:'Hier einfügen',fr:'Coller ici',ja:'ここに貼り付け',zh:'粘贴到此处'},\n" +
        "  'Limpiar': {en:'Clear',de:'Leeren',fr:'Effacer',ja:'クリア',zh:'清除'},\n" +
        "  'Seleccionar todo': {en:'Select all',de:'Alle auswählen',fr:'Tout sélectionner',ja:'すべて選択',zh:'全选'},\n" +
        "  'Subcarpetas': {en:'Subfolders',de:'Unterordner',fr:'Sous-dossiers',ja:'サブフォルダ',zh:'子文件夹'},\n" +
        "  'Cuadrícula': {en:'Grid',de:'Raster',fr:'Grille',ja:'グリッド',zh:'网格'},\n" +
        "  'Lista': {en:'List',de:'Liste',fr:'Liste',ja:'リスト',zh:'列表'},\n" +
        "  'Carpeta': {en:'Folder',de:'Ordner',fr:'Dossier',ja:'フォルダ',zh:'文件夹'},\n" +
        "  'Archivo': {en:'File',de:'Datei',fr:'Fichier',ja:'ファイル',zh:'文件'},\n" +
        "  'Cerrar': {en:'Close',de:'Schließen',fr:'Fermer',ja:'閉じる',zh:'关闭'},\n" +
        "  'Aceptar': {en:'OK',de:'OK',fr:'OK',ja:'OK',zh:'确定'},\n" +
        "  'Guardar': {en:'Save',de:'Speichern',fr:'Enregistrer',ja:'保存',zh:'保存'},\n" +
        "  'Extraer': {en:'Extract',de:'Entpacken',fr:'Extraire',ja:'展開',zh:'解压'},\n" +
        "  'Dispositivo y almacenamiento': {en:'Device & storage',de:'Gerät & Speicher',fr:'Appareil & stockage',ja:'デバイスとストレージ',zh:'设备与存储'},\n" +
        "  'Rendimiento de subida simultánea': {en:'Parallel upload performance',de:'Parallele Upload-Leistung',fr:'Performance d\\'envoi parallèle',ja:'並列アップロード性能',zh:'并发上传性能'},\n" +
        "  'Sesión y credenciales': {en:'Session & credentials',de:'Sitzung & Zugangsdaten',fr:'Session & identifiants',ja:'セッションと認証',zh:'会话与凭据'},\n" +
        "  'Cerrar sesión': {en:'Sign out',de:'Abmelden',fr:'Se déconnecter',ja:'ログアウト',zh:'退出登录'},\n" +
        "  'Iniciar sesión': {en:'Sign in',de:'Anmelden',fr:'Se connecter',ja:'ログイン',zh:'登录'},\n" +
        "  'Usuario': {en:'Username',de:'Benutzername',fr:'Utilisateur',ja:'ユーザー名',zh:'用户名'},\n" +
        "  'Contraseña': {en:'Password',de:'Passwort',fr:'Mot de passe',ja:'パスワード',zh:'密码'},\n" +
        "  'Permite la descarga de varios archivos': {en:'Allow multiple file downloads',de:'Mehrere Downloads erlauben',fr:'Autoriser les téléchargements multiples',ja:'複数ファイルのダウンロードを許可',zh:'允许同时下载多个文件'},\n" +
        "  'Analizador visual de almacenamiento': {en:'Visual storage analyzer',de:'Visueller Speicher-Analyzer',fr:'Analyseur visuel de stockage',ja:'ストレージ可視化アナライザー',zh:'可视化存储分析器'},\n" +
        "  'Liberar espacio': {en:'Free up space',de:'Speicher freigeben',fr:'Libérer de l\\'espace',ja:'空き容量を増やす',zh:'释放空间'},\n" +
        "  'Nombre': {en:'Name',de:'Name',fr:'Nom',ja:'名前',zh:'名称'},\n" +
        "  'Recientes': {en:'Recent',de:'Neueste',fr:'Récents',ja:'最近',zh:'最近'},\n" +
        "  'Tamaño': {en:'Size',de:'Größe',fr:'Taille',ja:'サイズ',zh:'大小'},\n" +
        "  'Tipo': {en:'Type',de:'Typ',fr:'Type',ja:'種類',zh:'类型'}\n" +
        "};\n" +
        "function trWeb(s) {\n" +
        "  if (!s || WEB_LANG === 'es') return s;\n" +
        "  const t = s.trim();\n" +
        "  if (TR_MAP[t] && TR_MAP[t][WEB_LANG]) return s.replace(t, TR_MAP[t][WEB_LANG]);\n" +
        "  if (t.startsWith('Total: ')) return {en:'Total: ',de:'Gesamt: ',fr:'Total : ',ja:'合計: ',zh:'总计: '}[WEB_LANG] + t.slice(7);\n" +
        "  if (t.endsWith(' seleccionados') || t.endsWith(' seleccionado')) {\n" +
        "    const n = t.split(' ')[0];\n" +
        "    return n + {en:' selected',de:' ausgewählt',fr:' sélectionné(s)',ja:' 件選択',zh:' 项已选'}[WEB_LANG];\n" +
        "  }\n" +
        "  return s;\n" +
        "}\n" +
        "function applyWebI18n(root) {\n" +
        "  if (WEB_LANG === 'es' || !root) return;\n" +
        "  const walk = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, null);\n" +
        "  const nodes = [];\n" +
        "  let cur; while ((cur = walk.nextNode())) nodes.push(cur);\n" +
        "  for (const n of nodes) {\n" +
        "    if (!n.parentElement || n.parentElement.tagName === 'SCRIPT' || n.parentElement.tagName === 'STYLE') continue;\n" +
        "    const raw = n.nodeValue;\n" +
        "    if (!raw || !raw.trim()) continue;\n" +
        "    const out = trWeb(raw);\n" +
        "    if (out !== raw) n.nodeValue = out;\n" +
        "  }\n" +
        "  if (root.querySelectorAll) {\n" +
        "    for (const el of root.querySelectorAll('[placeholder],[title]')) {\n" +
        "      if (el.placeholder) { const np = trWeb(el.placeholder); if (np !== el.placeholder) el.placeholder = np; }\n" +
        "      if (el.title) { const nt = trWeb(el.title); if (nt !== el.title) el.title = nt; }\n" +
        "    }\n" +
        "  }\n" +
        "}\n" +
        "if (WEB_LANG !== 'es') {\n" +
        "  applyWebI18n(document.body);\n" +
        "  let i18nTimer = null;\n" +
        "  const obs = new MutationObserver(() => {\n" +
        "    clearTimeout(i18nTimer);\n" +
        "    i18nTimer = setTimeout(() => applyWebI18n(document.body), 40);\n" +
        "  });\n" +
        "  obs.observe(document.body, { childList: true, subtree: true });\n" +
        "}\n";
}
