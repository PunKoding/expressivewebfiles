package com.fotospixel.transfer;

public final class WebUiExplorer {

    private WebUiExplorer() {
    }

    public static final String EXTRA_CSS =
        "  .exp-topbar { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; margin-bottom: 10px; }\n" +
        "  .exp-tabs { display: flex; flex-wrap: wrap; gap: 6px; align-items: center; flex: 1; min-width: 0; }\n" +
        "  .exp-tab {\n" +
        "    display: inline-flex; align-items: center; gap: 6px; padding: 7px 13px; border-radius: 999px;\n" +
        "    background: var(--md-sys-color-surface-container-high); color: var(--md-sys-color-on-surface-variant);\n" +
        "    font-size: 12px; font-weight: 700; cursor: pointer;\n" +
        "  }\n" +
        "  .exp-tab.active {\n" +
        "    background: var(--md-sys-color-primary-container); color: var(--md-sys-color-on-primary-container);\n" +
        "  }\n" +
        "  .exp-tab-x { opacity: 0.7; padding: 0 3px; font-weight: 800; }\n" +
        "  .exp-tab-x:hover { opacity: 1; }\n" +
        "  .exp-drawer {\n" +
        "    display: none; flex-direction: column; gap: 10px; padding: 14px; margin-bottom: 12px;\n" +
        "    border-radius: 22px; background: var(--md-sys-color-surface-container);\n" +
        "  }\n" +
        "  .exp-drawer.open { display: flex; }\n" +
        "  .exp-drawer-lbl {\n" +
        "    font-size: 11px; font-weight: 800; letter-spacing: 0.06em;\n" +
        "    color: var(--md-sys-color-on-surface-variant); text-transform: uppercase;\n" +
        "  }\n" +
        "  .sel-bar, .clip-bar {\n" +
        "    display: none; flex-wrap: wrap; gap: 8px; align-items: center; justify-content: space-between;\n" +
        "    border-radius: 18px; padding: 11px 14px; margin-bottom: 12px;\n" +
        "  }\n" +
        "  .sel-bar { background: var(--md-sys-color-primary-container); color: var(--md-sys-color-on-primary-container); }\n" +
        "  .clip-bar { background: var(--md-sys-color-tertiary-container); color: var(--md-sys-color-on-tertiary-container); }\n" +
        "  .chk { width: 17px; height: 17px; accent-color: var(--md-sys-color-primary); cursor: pointer; vertical-align: middle; }\n" +
        "  .row-cell { display: flex; align-items: center; gap: 10px; cursor: pointer; min-width: 0; }\n" +
        "  .row-ico-badge {\n" +
        "    width: 36px; height: 36px; border-radius: 13px; flex-shrink: 0;\n" +
        "    display: inline-flex; align-items: center; justify-content: center;\n" +
        "    background: var(--md-sys-color-primary-container); color: var(--md-sys-color-on-primary-container);\n" +
        "  }\n" +
        "  .row-ico-badge.arch { background: var(--md-sys-color-tertiary-container); color: var(--md-sys-color-on-tertiary-container); }\n" +
        "  .row-ico-badge.file { background: var(--md-sys-color-surface-container-highest); color: var(--md-sys-color-on-surface-variant); }\n" +
        "  .row-name-wrap { flex: 1; min-width: 0; }\n" +
        "  .row-name { font-weight: 700; font-size: 14px; word-break: break-word; line-height: 1.3; }\n" +
        "  .file-sub-mob { display: none; font-size: 11px; color: var(--md-sys-color-on-surface-variant); margin-top: 2px; }\n" +
        "  .row-acts { display: flex; gap: 6px; align-items: center; justify-content: flex-end; flex-wrap: wrap; }\n" +
        "  .btn-ico-round {\n" +
        "    width: 36px; height: 36px; min-width: 36px; padding: 0; border-radius: 999px;\n" +
        "    display: inline-flex; align-items: center; justify-content: center; flex-shrink: 0;\n" +
        "  }\n" +
        "  .btn-resp { display: inline-flex; align-items: center; gap: 6px; }\n" +
        "  .mob-only { display: none !important; }\n" +
        "  @media (max-width: 720px) {\n" +
        "    .btn-resp .btn-lbl { display: none; }\n" +
        "    .btn-resp {\n" +
        "      width: 34px !important; height: 34px !important; min-width: 34px !important;\n" +
        "      padding: 0 !important; border-radius: 999px !important; justify-content: center; gap: 0; flex-shrink: 0;\n" +
        "    }\n" +
        "    .desk-only { display: none !important; }\n" +
        "    .mob-only { display: inline-flex !important; }\n" +
        "    th.col-meta, th.col-date, td.col-meta, td.col-date { display: none; }\n" +
        "    .file-sub-mob { display: block; }\n" +
        "    #fileTableWrap table td { padding: 9px 6px; }\n" +
        "    .exp-bar > input#explorerSearch { width: 100%; flex-basis: 100%; }\n" +
        "  }\n" +
        "  .modal-box {\n" +
        "    width: 100%; max-width: 720px; max-height: 88vh; overflow: auto;\n" +
        "    background: var(--md-sys-color-surface-container-low); color: var(--md-sys-color-on-surface);\n" +
        "    border-radius: 28px; padding: 22px; box-shadow: 0 24px 60px rgba(0,0,0,0.65);\n" +
        "  }\n" +
        "  .modal-head { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-bottom: 14px; }\n" +
        "  .modal-head h2 { margin: 0; font-size: 18px; word-break: break-word; }\n" +
        "  textarea.code-ed {\n" +
        "    width: 100%; min-height: 320px; border-radius: 18px; padding: 14px;\n" +
        "    background: var(--md-sys-color-surface-container-lowest); color: var(--md-sys-color-on-surface);\n" +
        "    border: 1px solid var(--md-sys-color-outline-variant);\n" +
        "    font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-size: 13px; line-height: 1.5; resize: vertical;\n" +
        "  }\n" +
        "  .kv-grid { display: grid; grid-template-columns: 120px 1fr; gap: 8px 12px; font-size: 13px; }\n" +
        "  .kv-k { color: var(--md-sys-color-on-surface-variant); font-weight: 700; }\n" +
        "  .kv-v { word-break: break-all; font-family: ui-monospace, monospace; }\n" +
        "  .act-sheet-list { display: flex; flex-direction: column; gap: 8px; }\n" +
        "  .act-sheet-list .btn { width: 100%; justify-content: flex-start; padding: 11px 16px; }\n";

    public static final String SECTION_HTML =
        "  <div id=\"tabFiles\" style=\"display:none\">\n" +
        "    <div class=\"card\">\n" +
        "      <div class=\"exp-topbar\">\n" +
        "        <button class=\"btn btn-sm btn-tonal btn-resp\" id=\"drawerToggleBtn\" title=\"Marcadores y bibliotecas\" onclick=\"toggleBookmarksDrawer()\">" + WebUi.SVG_MENU + "<span class=\"btn-lbl\">Marcadores</span></button>\n" +
        "        <div class=\"exp-tabs\" id=\"expTabsBar\"></div>\n" +
        "        <div style=\"display:flex;gap:6px;align-items:center;flex-wrap:wrap\">\n" +
        "          <button class=\"btn btn-sm btn-primary btn-resp\" id=\"curZipDlBtn\" title=\"Descargar ZIP\" style=\"display:none\" onclick=\"downloadZip(currentDir)\">" + WebUi.SVG_ZIP + "<span class=\"btn-lbl\">Descargar ZIP</span></button>\n" +
        "          <button class=\"btn btn-sm btn-tonal btn-resp\" id=\"curZipCreateBtn\" title=\"Guardar ZIP en el teléfono\" style=\"display:none\" onclick=\"createZipOnDevice(currentDir, 'zip')\">" + WebUi.SVG_DOWNLOAD + "<span class=\"btn-lbl\">Guardar ZIP</span></button>\n" +
        "          <button class=\"btn btn-sm btn-tonal desk-only\" id=\"curTarCreateBtn\" style=\"display:none\" onclick=\"createZipOnDevice(currentDir, 'targz')\">TAR.GZ</button>\n" +
        "        </div>\n" +
        "      </div>\n" +
        "\n" +
        "      <div class=\"exp-drawer\" id=\"bookmarksDrawer\">\n" +
        "        <div class=\"exp-drawer-lbl\">Unidades y medios externos</div>\n" +
        "        <div class=\"chips\" id=\"volumesBar\">\n" +
        "          <span class=\"chip\" onclick=\"browse('');closeBookmarksDrawer()\">" + WebUi.SVG_STORAGE + " Almacenamiento interno</span>\n" +
        "        </div>\n" +
        "        <div class=\"exp-drawer-lbl\">Accesos rápidos</div>\n" +
        "        <div class=\"chips\" id=\"bookmarksBar\">\n" +
        "          <span class=\"chip\" onclick=\"browse('');closeBookmarksDrawer()\">" + WebUi.SVG_STORAGE + " Raíz</span>\n" +
        "          <span class=\"chip\" onclick=\"browse('DCIM/Camera');closeBookmarksDrawer()\">" + WebUi.SVG_PHOTO + " DCIM/Camera</span>\n" +
        "          <span class=\"chip\" onclick=\"browse('Pictures');closeBookmarksDrawer()\">" + WebUi.SVG_PHOTO + " Pictures</span>\n" +
        "          <span class=\"chip\" onclick=\"browse('Download');closeBookmarksDrawer()\">" + WebUi.SVG_DOWNLOAD + " Download</span>\n" +
        "          <span class=\"chip\" onclick=\"browse('Documents');closeBookmarksDrawer()\">" + WebUi.SVG_FOLDER + " Documents</span>\n" +
        "          <span class=\"chip\" onclick=\"pinCurrentFolder()\">+ Fijar actual</span>\n" +
        "        </div>\n" +
        "        <div class=\"chips\" id=\"customBookmarks\"></div>\n" +
        "        <div class=\"exp-drawer-lbl\">Bibliotecas inteligentes</div>\n" +
        "        <div class=\"chips\">\n" +
        "          <span class=\"chip\" onclick=\"browse(currentDir);closeBookmarksDrawer()\">Vista carpeta</span>\n" +
        "          <span class=\"chip\" onclick=\"runCategory('image');closeBookmarksDrawer()\">Imágenes</span>\n" +
        "          <span class=\"chip\" onclick=\"runCategory('video');closeBookmarksDrawer()\">Vídeos</span>\n" +
        "          <span class=\"chip\" onclick=\"runCategory('audio');closeBookmarksDrawer()\">Audio</span>\n" +
        "          <span class=\"chip\" onclick=\"runCategory('doc');closeBookmarksDrawer()\">Documentos</span>\n" +
        "          <span class=\"chip\" onclick=\"runCategory('archive');closeBookmarksDrawer()\">ZIP / Comprimidos</span>\n" +
        "          <span class=\"chip\" onclick=\"runCategory('recent');closeBookmarksDrawer()\">Recientes (7d)</span>\n" +
        "        </div>\n" +
        "      </div>\n" +
        "\n" +
        "      <div class=\"chips\" id=\"extVolumesBanner\" style=\"display:none;margin-bottom:8px\"></div>\n" +
        "\n" +
        "      <div style=\"display:flex;align-items:center;justify-content:space-between;gap:8px;flex-wrap:wrap;margin-bottom:8px\">\n" +
        "        <div class=\"breadcrumbs\" id=\"breadcrumbBar\" style=\"margin:0;flex:1;min-width:180px\"></div>\n" +
        "        <span class=\"tonal-pill desk-only\" id=\"currentPathLabel\">/</span>\n" +
        "      </div>\n" +
        "\n" +
        "      <div class=\"exp-bar\">\n" +
        "        <input type=\"text\" id=\"explorerSearch\" placeholder=\"Filtrar aquí o buscar en subcarpetas...\" oninput=\"renderExplorer()\">\n" +
        "        <button class=\"btn btn-sm btn-tonal btn-resp\" title=\"Buscar en subcarpetas\" onclick=\"runDeepSearch()\">" + WebUi.SVG_SEARCH + "<span class=\"btn-lbl\">Subcarpetas</span></button>\n" +
        "        <select id=\"explorerSort\" onchange=\"renderExplorer()\">\n" +
        "          <option value=\"name\">Nombre</option>\n" +
        "          <option value=\"date\">Recientes</option>\n" +
        "          <option value=\"size\">Tamaño</option>\n" +
        "          <option value=\"type\">Tipo</option>\n" +
        "        </select>\n" +
        "        <button class=\"btn btn-sm btn-tonal btn-resp\" id=\"viewToggleBtn\" title=\"Cambiar vista\" onclick=\"toggleViewMode()\">" + WebUi.SVG_TUNE + "<span class=\"btn-lbl\">Cuadrícula</span></button>\n" +
        "        <button class=\"btn btn-sm btn-tonal btn-resp\" title=\"Nueva carpeta\" onclick=\"createSubfolderFromExplorer()\">" + WebUi.SVG_FOLDER + "<span class=\"btn-lbl\">Carpeta</span></button>\n" +
        "        <button class=\"btn btn-sm btn-primary btn-resp\" title=\"Nuevo archivo\" onclick=\"createNewFile()\">" + WebUi.SVG_ADD + "<span class=\"btn-lbl\">Archivo</span></button>\n" +
        "        <button class=\"btn btn-sm btn-tonal btn-ico-round\" title=\"Actualizar\" onclick=\"browse(currentDir)\">" + WebUi.SVG_REFRESH + "</button>\n" +
        "      </div>\n" +
        "\n" +
        "      <div class=\"clip-bar\" id=\"clipboardBar\">\n" +
        "        <span id=\"clipSummary\" style=\"font-weight:700;font-size:13px\"></span>\n" +
        "        <div style=\"display:flex;gap:8px;flex-wrap:wrap\">\n" +
        "          <button class=\"btn btn-sm btn-primary\" onclick=\"pasteClipboard()\">Pegar aquí</button>\n" +
        "          <button class=\"btn btn-sm\" onclick=\"clearClipboard()\">Limpiar</button>\n" +
        "        </div>\n" +
        "      </div>\n" +
        "\n" +
        "      <div class=\"sel-bar\" id=\"selectionBar\">\n" +
        "        <span id=\"selSummary\" style=\"font-weight:700;font-size:13px\"></span>\n" +
        "        <div style=\"display:flex;gap:6px;flex-wrap:wrap\">\n" +
        "          <button class=\"btn btn-sm btn-primary btn-resp\" title=\"Descargar archivos sin ZIP\" onclick=\"downloadSelected()\">" + WebUi.SVG_DOWNLOAD + "<span class=\"btn-lbl\">Descargar</span></button>\n" +
        "          <button class=\"btn btn-sm\" onclick=\"copyCutSelected('copy')\">Copiar</button>\n" +
        "          <button class=\"btn btn-sm\" onclick=\"copyCutSelected('cut')\">Cortar</button>\n" +
        "          <button class=\"btn btn-sm btn-resp\" title=\"Empaquetar ZIP\" onclick=\"packSelected('zip')\">" + WebUi.SVG_ZIP + "<span class=\"btn-lbl\">ZIP</span></button>\n" +
        "          <button class=\"btn btn-sm\" onclick=\"packSelected('targz')\">TAR.GZ</button>\n" +
        "          <button class=\"btn btn-sm btn-danger btn-resp\" title=\"Eliminar selección\" onclick=\"deleteSelected()\">" + WebUi.SVG_STOP + "<span class=\"btn-lbl\">Eliminar</span></button>\n" +
        "          <button class=\"btn btn-sm\" onclick=\"clearSelection()\">✕</button>\n" +
        "        </div>\n" +
        "      </div>\n" +
        "\n" +
        "      <div class=\"exp-notice\" id=\"explorerNotice\" style=\"display:none\"></div>\n" +
        "      <div class=\"exp-stats\">\n" +
        "        <span id=\"explorerStats\">Cargando contenido...</span>\n" +
        "        <button class=\"btn btn-sm btn-resp\" title=\"Seleccionar todo\" onclick=\"toggleSelectAll()\">" + WebUi.SVG_CHECK + "<span class=\"btn-lbl\">Seleccionar todo</span></button>\n" +
        "      </div>\n" +
        "      <div id=\"fileTableWrap\"></div>\n" +
        "    </div>\n" +
        "  </div>\n";

    public static final String MODALS_HTML =
        "<div class=\"modal\" id=\"mediaModal\" onclick=\"if(event.target===this)closeMediaModal()\">\n" +
        "  <div class=\"modal-box\">\n" +
        "    <div class=\"modal-head\">\n" +
        "      <h2 id=\"mediaTitle\">Vista previa</h2>\n" +
        "      <div style=\"display:flex;gap:8px\">\n" +
        "        <a class=\"btn btn-sm btn-primary\" id=\"mediaDlLink\" href=\"#\" download>Descargar</a>\n" +
        "        <button class=\"btn btn-sm\" onclick=\"closeMediaModal()\">Cerrar</button>\n" +
        "      </div>\n" +
        "    </div>\n" +
        "    <div id=\"mediaBody\" style=\"text-align:center\"></div>\n" +
        "  </div>\n" +
        "</div>\n" +
        "\n" +
        "<div class=\"modal\" id=\"editorModal\" onclick=\"if(event.target===this)this.classList.remove('open')\">\n" +
        "  <div class=\"modal-box\">\n" +
        "    <div class=\"modal-head\">\n" +
        "      <h2 id=\"edTitle\">Editor de texto</h2>\n" +
        "      <div style=\"display:flex;gap:8px\">\n" +
        "        <button class=\"btn btn-sm btn-primary\" onclick=\"saveEditorFile()\">Guardar</button>\n" +
        "        <button class=\"btn btn-sm\" onclick=\"document.getElementById('editorModal').classList.remove('open')\">Cerrar</button>\n" +
        "      </div>\n" +
        "    </div>\n" +
        "    <textarea id=\"edTextarea\" class=\"code-ed\"></textarea>\n" +
        "  </div>\n" +
        "</div>\n" +
        "\n" +
        "<div class=\"modal\" id=\"infoModal\" onclick=\"if(event.target===this)this.classList.remove('open')\">\n" +
        "  <div class=\"modal-box\" style=\"max-width:560px\">\n" +
        "    <div class=\"modal-head\">\n" +
        "      <h2 id=\"infoTitle\">Propiedades y Hashes</h2>\n" +
        "      <button class=\"btn btn-sm\" onclick=\"document.getElementById('infoModal').classList.remove('open')\">Cerrar</button>\n" +
        "    </div>\n" +
        "    <div id=\"infoBody\" class=\"kv-grid\"></div>\n" +
        "  </div>\n" +
        "</div>\n" +
        "\n" +
        "<div class=\"modal\" id=\"archiveModal\" onclick=\"if(event.target===this)this.classList.remove('open')\">\n" +
        "  <div class=\"modal-box\">\n" +
        "    <div class=\"modal-head\">\n" +
        "      <h2 id=\"archTitle\">Contenido del archivo</h2>\n" +
        "      <div style=\"display:flex;gap:8px\">\n" +
        "        <button class=\"btn btn-sm btn-primary\" id=\"archExtractBtn\">Extraer</button>\n" +
        "        <button class=\"btn btn-sm\" onclick=\"document.getElementById('archiveModal').classList.remove('open')\">Cerrar</button>\n" +
        "      </div>\n" +
        "    </div>\n" +
        "    <div id=\"archBody\" style=\"overflow:auto\"></div>\n" +
        "  </div>\n" +
        "</div>\n" +
        "\n" +
        "<div class=\"modal\" id=\"itemActionsModal\" onclick=\"if(event.target===this)this.classList.remove('open')\">\n" +
        "  <div class=\"modal-box\" style=\"max-width:420px\">\n" +
        "    <div class=\"modal-head\">\n" +
        "      <h2 id=\"actTitle\">Opciones</h2>\n" +
        "      <button class=\"btn btn-sm\" onclick=\"document.getElementById('itemActionsModal').classList.remove('open')\">Cerrar</button>\n" +
        "    </div>\n" +
        "    <div id=\"actList\" class=\"act-sheet-list\"></div>\n" +
        "  </div>\n" +
        "</div>\n";

    public static final String EXPLORER_JS_1 =
        "let expTabs = [{ id: 1, dir: 'DCIM/Camera', label: 'Camera', category: '' }];\n" +
        "let activeTabId = 1;\n" +
        "let expNextTabId = 2;\n" +
        "let activeCategory = '';\n" +
        "let isSearchResultMode = false;\n" +
        "let mountedVolumes = [];\n" +
        "const selectedPaths = new Set();\n" +
        "const expClipboard = { mode: '', paths: [] };\n" +
        "let currentEditPath = '';\n" +
        "\n" +
        "function isVolumeRootPath(dir) { return !dir || /^@ext\\/[^/]+$/.test(dir); }\n" +
        "function getParentDirPath(dir) {\n" +
        "  if (!dir || isVolumeRootPath(dir)) return '';\n" +
        "  return dir.includes('/') ? dir.substring(0, dir.lastIndexOf('/')) : '';\n" +
        "}\n" +
        "function volumeLabel(dir) {\n" +
        "  if (!dir) return 'Raíz';\n" +
        "  if (dir.startsWith('@ext/')) {\n" +
        "    const parts = dir.split('/');\n" +
        "    if (parts.length === 2) {\n" +
        "      const v = mountedVolumes.find(x => x.path === dir);\n" +
        "      return v ? v.label : parts[1];\n" +
        "    }\n" +
        "    return parts[parts.length - 1];\n" +
        "  }\n" +
        "  return dir.split('/').pop();\n" +
        "}\n" +
        "async function loadVolumes() {\n" +
        "  try {\n" +
        "    const r = await fetch('/api/volumes');\n" +
        "    if (!r.ok) return;\n" +
        "    const d = await r.json();\n" +
        "    mountedVolumes = d.volumes || [];\n" +
        "    const vb = document.getElementById('volumesBar');\n" +
        "    if (vb) {\n" +
        "      vb.innerHTML = mountedVolumes.map(v => {\n" +
        "        const sp = v.freeBytes > 0 ? (' · ' + fmtBytes(v.freeBytes) + ' libres') : '';\n" +
        "        return '<span class=\"chip\" onclick=\"browse(decodeURIComponent(\\'' + encJs(v.path) + '\\'));closeBookmarksDrawer()\">' + WebUi_SVG_STORAGE() + ' ' + esc(v.label) + sp + '</span>';\n" +
        "      }).join('');\n" +
        "    }\n" +
        "    const ext = mountedVolumes.filter(v => v.kind !== 'internal');\n" +
        "    const bn = document.getElementById('extVolumesBanner');\n" +
        "    if (bn) {\n" +
        "      if (ext.length > 0) {\n" +
        "        bn.style.display = 'flex';\n" +
        "        bn.innerHTML = '<span class=\"chip\" onclick=\"browse(\\'\\')\">' + WebUi_SVG_STORAGE() + ' Interno</span>' + ext.map(v => {\n" +
        "          const tag = v.kind === 'usb' ? 'USB' : 'SD';\n" +
        "          return '<span class=\"chip\" onclick=\"browse(decodeURIComponent(\\'' + encJs(v.path) + '\\'))\">' + WebUi_SVG_STORAGE() + ' ' + tag + ': ' + esc(v.label) + '</span>';\n" +
        "        }).join('');\n" +
        "      } else { bn.style.display = 'none'; bn.innerHTML = ''; }\n" +
        "    }\n" +
        "  } catch (ignored) {}\n" +
        "}\n" +
        "function toggleBookmarksDrawer() {\n" +
        "  const d = document.getElementById('bookmarksDrawer');\n" +
        "  if (d) d.classList.toggle('open');\n" +
        "}\n" +
        "function closeBookmarksDrawer() {\n" +
        "  const d = document.getElementById('bookmarksDrawer');\n" +
        "  if (d) d.classList.remove('open');\n" +
        "}\n" +
        "function fmtDate(ms) {\n" +
        "  if (!ms) return '—';\n" +
        "  try { const d = new Date(ms); return d.toLocaleDateString() + ' ' + d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }); } catch (e) { return ''; }\n" +
        "}\n" +
        "function showExplorerNotice(msg) {\n" +
        "  const el = document.getElementById('explorerNotice');\n" +
        "  if (!el) return;\n" +
        "  el.textContent = msg; el.style.display = 'block';\n" +
        "  setTimeout(() => { el.style.display = 'none'; }, 5000);\n" +
        "}\n" +
        "function renderExpTabs() {\n" +
        "  const bar = document.getElementById('expTabsBar');\n" +
        "  if (!bar) return;\n" +
        "  let h = '';\n" +
        "  for (const t of expTabs) {\n" +
        "    const cls = (t.id === activeTabId) ? 'exp-tab active' : 'exp-tab';\n" +
        "    const closeBtn = expTabs.length > 1 ? '<span class=\"exp-tab-x\" onclick=\"closeExpTab(' + t.id + ', event)\">×</span>' : '';\n" +
        "    h += '<span class=\"' + cls + '\" onclick=\"selectExpTab(' + t.id + ')\">' + esc(t.label) + closeBtn + '</span>';\n" +
        "  }\n" +
        "  h += '<button class=\"btn btn-sm btn-tonal btn-ico-round\" title=\"Nueva pestaña\" onclick=\"openExpTab(currentDir)\">' + WebUi_SVG_ADD() + '</button>';\n" +
        "  bar.innerHTML = h;\n" +
        "}\n" +
        "function selectExpTab(id) {\n" +
        "  const t = expTabs.find(x => x.id === id);\n" +
        "  if (!t) return;\n" +
        "  activeTabId = id;\n" +
        "  if (t.category) runCategory(t.category); else browse(t.dir);\n" +
        "}\n" +
        "function openExpTab(dir) {\n" +
        "  const clean = (dir || '').replace(/^\\/+|\\/+$/g, '');\n" +
        "  const lbl = volumeLabel(clean);\n" +
        "  const id = expNextTabId++;\n" +
        "  expTabs.push({ id, dir: clean, label: lbl, category: '' });\n" +
        "  activeTabId = id;\n" +
        "  browse(clean);\n" +
        "}\n" +
        "function closeExpTab(id, ev) {\n" +
        "  if (ev) ev.stopPropagation();\n" +
        "  if (expTabs.length <= 1) return;\n" +
        "  expTabs = expTabs.filter(t => t.id !== id);\n" +
        "  if (activeTabId === id) { selectExpTab(expTabs[expTabs.length - 1].id); } else { renderExpTabs(); }\n" +
        "}\n" +
        "function loadCustomBookmarks() {\n" +
        "  try { return JSON.parse(localStorage.getItem('fp_bookmarks') || '[]'); } catch (e) { return []; }\n" +
        "}\n" +
        "function renderCustomBookmarks() {\n" +
        "  const el = document.getElementById('customBookmarks');\n" +
        "  if (!el) return;\n" +
        "  const list = loadCustomBookmarks();\n" +
        "  el.style.display = list.length ? 'flex' : 'none';\n" +
        "  el.innerHTML = list.map(p => '<span class=\"chip\" onclick=\"browse(decodeURIComponent(\\'' + encJs(p) + '\\'));closeBookmarksDrawer()\">★ /' + esc(p) + ' <b onclick=\"unpinBookmark(decodeURIComponent(\\'' + encJs(p) + '\\'), event)\" style=\"margin-left:4px\">×</b></span>').join('');\n" +
        "}\n" +
        "function pinCurrentFolder() {\n" +
        "  if (!currentDir) return;\n" +
        "  const list = loadCustomBookmarks();\n" +
        "  if (!list.includes(currentDir)) { list.push(currentDir); localStorage.setItem('fp_bookmarks', JSON.stringify(list)); }\n" +
        "  renderCustomBookmarks();\n" +
        "  showExplorerNotice('Carpeta fijada en marcadores: /' + currentDir);\n" +
        "}\n" +
        "function unpinBookmark(p, ev) {\n" +
        "  if (ev) ev.stopPropagation();\n" +
        "  const list = loadCustomBookmarks().filter(x => x !== p);\n" +
        "  localStorage.setItem('fp_bookmarks', JSON.stringify(list));\n" +
        "  renderCustomBookmarks();\n" +
        "}\n" +
        "function toggleViewMode() {\n" +
        "  explorerViewMode = (explorerViewMode === 'list') ? 'grid' : 'list';\n" +
        "  localStorage.setItem('fp_exp_view', explorerViewMode);\n" +
        "  const lbl = document.querySelector('#viewToggleBtn .btn-lbl');\n" +
        "  if (lbl) lbl.textContent = explorerViewMode === 'list' ? 'Cuadrícula' : 'Lista';\n" +
        "  renderExplorer();\n" +
        "}\n" +
        "function renderBreadcrumbs() {\n" +
        "  const bar = document.getElementById('breadcrumbBar');\n" +
        "  if (!bar) return;\n" +
        "  if (currentDir && currentDir.startsWith('@ext/')) {\n" +
        "    const parts = currentDir.split('/').filter(Boolean);\n" +
        "    const volRoot = '@ext/' + (parts[1] || '');\n" +
        "    const v = mountedVolumes.find(x => x.path === volRoot);\n" +
        "    const rootName = v ? v.label : (parts[1] || 'Externo');\n" +
        "    let html = '<span class=\"crumb ' + (parts.length <= 2 ? 'cur' : '') + '\" onclick=\"browse(decodeURIComponent(\\'' + encJs(volRoot) + '\\'))\">' + esc(rootName) + '</span>';\n" +
        "    let acc = volRoot;\n" +
        "    for (let i = 2; i < parts.length; i++) {\n" +
        "      acc += '/' + parts[i];\n" +
        "      const isLast = (i === parts.length - 1);\n" +
        "      html += '<span style=\"opacity:0.45\">›</span><span class=\"crumb ' + (isLast ? 'cur' : '') + '\" onclick=\"browse(decodeURIComponent(\\'' + encJs(acc) + '\\'))\">' + esc(parts[i]) + '</span>';\n" +
        "    }\n" +
        "    bar.innerHTML = html;\n" +
        "    return;\n" +
        "  }\n" +
        "  const parts = currentDir ? currentDir.split('/').filter(Boolean) : [];\n" +
        "  let html = '<span class=\"crumb ' + (parts.length === 0 ? 'cur' : '') + '\" onclick=\"browse(\\'\\')\">Raíz</span>';\n" +
        "  let acc = '';\n" +
        "  parts.forEach((p, idx) => {\n" +
        "    acc = acc ? (acc + '/' + p) : p;\n" +
        "    const isLast = (idx === parts.length - 1);\n" +
        "    html += '<span style=\"opacity:0.45\">›</span><span class=\"crumb ' + (isLast ? 'cur' : '') + '\" onclick=\"browse(decodeURIComponent(\\'' + encJs(acc) + '\\'))\">' + esc(p) + '</span>';\n" +
        "  });\n" +
        "  bar.innerHTML = html;\n" +
        "}\n" +
        "function updateSelectionUi() {\n" +
        "  const bar = document.getElementById('selectionBar');\n" +
        "  if (bar) {\n" +
        "    bar.style.display = selectedPaths.size > 0 ? 'flex' : 'none';\n" +
        "    document.getElementById('selSummary').textContent = selectedPaths.size + (selectedPaths.size === 1 ? ' seleccionado' : ' seleccionados');\n" +
        "  }\n" +
        "  const cb = document.getElementById('clipboardBar');\n" +
        "  if (cb) {\n" +
        "    cb.style.display = expClipboard.paths.length > 0 ? 'flex' : 'none';\n" +
        "    document.getElementById('clipSummary').textContent = (expClipboard.mode === 'cut' ? 'Cortados ' : 'Copiados ') + expClipboard.paths.length;\n" +
        "  }\n" +
        "}\n" +
        "function toggleSelect(path, checked) {\n" +
        "  if (checked) selectedPaths.add(path); else selectedPaths.delete(path);\n" +
        "  updateSelectionUi();\n" +
        "}\n" +
        "function clearSelection() { selectedPaths.clear(); updateSelectionUi(); renderExplorer(); }\n" +
        "function toggleSelectAll() {\n" +
        "  const list = getFilteredSortedEntries();\n" +
        "  if (selectedPaths.size > 0 && selectedPaths.size >= list.length) { selectedPaths.clear(); }\n" +
        "  else { for (const e of list) selectedPaths.add(e.path || (currentDir ? currentDir + '/' + e.name : e.name)); }\n" +
        "  updateSelectionUi(); renderExplorer();\n" +
        "}\n";

    public static final String EXPLORER_JS_2 =
        "function WebUi_SVG_FOLDER() { return '" + WebUi.SVG_FOLDER + "'; }\n" +
        "function WebUi_SVG_ZIP() { return '" + WebUi.SVG_ZIP + "'; }\n" +
        "function WebUi_SVG_STORAGE() { return '" + WebUi.SVG_STORAGE + "'; }\n" +
        "function WebUi_SVG_MORE() { return '" + WebUi.SVG_MORE + "'; }\n" +
        "function WebUi_SVG_ADD() { return '" + WebUi.SVG_ADD + "'; }\n" +
        "function WebUi_SVG_DOWNLOAD() { return '" + WebUi.SVG_DOWNLOAD + "'; }\n" +
        "function WebUi_SVG_STOP() { return '" + WebUi.SVG_STOP + "'; }\n" +
        "function WebUi_SVG_TUNE() { return '" + WebUi.SVG_TUNE + "'; }\n" +
        "async function browse(dir) {\n" +
        "  activeCategory = ''; isSearchResultMode = false;\n" +
        "  currentDir = (dir || '').replace(/^\\/+|\\/+$/g, '');\n" +
        "  selectedPaths.clear(); updateSelectionUi();\n" +
        "  if (mountedVolumes.length === 0) await loadVolumes();\n" +
        "  const tab = expTabs.find(t => t.id === activeTabId);\n" +
        "  if (tab) { tab.dir = currentDir; tab.category = ''; tab.label = volumeLabel(currentDir); }\n" +
        "  renderExpTabs(); renderCustomBookmarks();\n" +
        "  const cp = document.getElementById('currentPathLabel'); if (cp) cp.textContent = '/' + currentDir;\n" +
        "  const showCurActions = !isVolumeRootPath(currentDir) ? 'inline-flex' : 'none';\n" +
        "  ['curZipDlBtn', 'curZipCreateBtn', 'curTarCreateBtn'].forEach(id => { const x = document.getElementById(id); if (x) x.style.display = showCurActions; });\n" +
        "  const lbl = document.querySelector('#viewToggleBtn .btn-lbl');\n" +
        "  if (lbl) lbl.textContent = explorerViewMode === 'list' ? 'Cuadrícula' : 'Lista';\n" +
        "  renderBreadcrumbs();\n" +
        "  loadVolumes();\n" +
        "  try {\n" +
        "    const r = await fetch('/api/list?dir=' + encodeURIComponent(currentDir));\n" +
        "    if (r.status === 401) { showLogin(); return; }\n" +
        "    const data = await r.json();\n" +
        "    currentEntries = data.entries || [];\n" +
        "    renderExplorer();\n" +
        "  } catch (err) { document.getElementById('fileTableWrap').innerHTML = '<div class=\"empty\">Error al listar carpeta</div>'; }\n" +
        "}\n" +
        "async function runCategory(cat) {\n" +
        "  activeCategory = cat; isSearchResultMode = true; selectedPaths.clear(); updateSelectionUi();\n" +
        "  const names = { image: 'Imágenes', video: 'Vídeos', audio: 'Audio', doc: 'Documentos', archive: 'Comprimidos', recent: 'Recientes' };\n" +
        "  const tab = expTabs.find(t => t.id === activeTabId);\n" +
        "  if (tab) { tab.category = cat; tab.label = names[cat] || cat; }\n" +
        "  renderExpTabs();\n" +
        "  const cp = document.getElementById('currentPathLabel'); if (cp) cp.textContent = 'Filtro: ' + (names[cat] || cat);\n" +
        "  const r = await fetch('/api/search?dir=' + encodeURIComponent(currentDir) + '&category=' + encodeURIComponent(cat));\n" +
        "  if (r.status === 401) { showLogin(); return; }\n" +
        "  const d = await r.json(); currentEntries = d.entries || []; renderExplorer();\n" +
        "}\n" +
        "async function runDeepSearch() {\n" +
        "  const q = (document.getElementById('explorerSearch') ? document.getElementById('explorerSearch').value : '').trim();\n" +
        "  if (!q) { browse(currentDir); return; }\n" +
        "  isSearchResultMode = true; selectedPaths.clear(); updateSelectionUi();\n" +
        "  const r = await fetch('/api/search?dir=' + encodeURIComponent(currentDir) + '&q=' + encodeURIComponent(q));\n" +
        "  if (r.status === 401) { showLogin(); return; }\n" +
        "  const d = await r.json(); currentEntries = d.entries || []; renderExplorer();\n" +
        "}\n" +
        "function getFilteredSortedEntries() {\n" +
        "  const q = (!isSearchResultMode && document.getElementById('explorerSearch')) ? document.getElementById('explorerSearch').value.trim().toLowerCase() : '';\n" +
        "  const sortMode = document.getElementById('explorerSort') ? document.getElementById('explorerSort').value : 'name';\n" +
        "  let list = currentEntries.slice();\n" +
        "  if (q) list = list.filter(e => e.name.toLowerCase().includes(q));\n" +
        "  list.sort((a, b) => {\n" +
        "    if (a.isDir !== b.isDir) return a.isDir ? -1 : 1;\n" +
        "    if (sortMode === 'date') return (b.mtime || 0) - (a.mtime || 0);\n" +
        "    if (sortMode === 'size') return (b.size || 0) - (a.size || 0);\n" +
        "    if (sortMode === 'type') return String(a.mime || '').localeCompare(String(b.mime || '')) || a.name.localeCompare(b.name);\n" +
        "    return a.name.localeCompare(b.name, undefined, { sensitivity: 'base' });\n" +
        "  });\n" +
        "  return list;\n" +
        "}\n" +
        "function isTextName(name) { return /\\.(txt|md|json|xml|csv|log|html|htm|js|css|java|py|sh|ini|cfg|conf|yaml|yml)$/i.test(name); }\n" +
        "function isArchiveName(name) { return /\\.(zip|tar\\.gz|tgz)$/i.test(name); }\n" +
        "function isMediaPreview(name) { return /\\.(mp4|mov|webm|mkv|mp3|wav|flac|m4a|aac|ogg|opus|pdf)$/i.test(name); }\n" +
        "function openItemMenu(fullPath, name, isDir, isArchive, isText) {\n" +
        "  document.getElementById('actTitle').textContent = name;\n" +
        "  const fp = decodeURIComponent(fullPath), nm = decodeURIComponent(name);\n" +
        "  const el = document.getElementById('actList');\n" +
        "  const acts = [];\n" +
        "  if (isDir) {\n" +
        "    acts.push({ lbl: 'Abrir en nueva pestaña', cls: 'btn-tonal', fn: () => openExpTab(fp) });\n" +
        "    acts.push({ lbl: 'Descargar ZIP', cls: 'btn-primary', fn: () => downloadZip(fp) });\n" +
        "    acts.push({ lbl: 'Guardar ZIP en dispositivo', cls: 'btn-tonal', fn: () => createZipOnDevice(fp, 'zip') });\n" +
        "    acts.push({ lbl: 'Empaquetar TAR.GZ en dispositivo', cls: 'btn-tonal', fn: () => createZipOnDevice(fp, 'targz') });\n" +
        "  } else {\n" +
        "    if (isArchive) {\n" +
        "      acts.push({ lbl: 'Ver contenido del archivo', cls: 'btn-primary', fn: () => inspectArchive(fp) });\n" +
        "      acts.push({ lbl: 'Extraer en subcarpeta', cls: 'btn-tonal', fn: () => extractArchive(fp) });\n" +
        "    }\n" +
        "    if (isText) acts.push({ lbl: 'Editar texto UTF-8', cls: 'btn-tonal', fn: () => openTextEditor(fp, nm) });\n" +
        "    acts.push({ lbl: 'Descargar archivo', cls: 'btn-primary', fn: () => { window.location.href = '/api/download?path=' + encodeURIComponent(fp); } });\n" +
        "  }\n" +
        "  acts.push({ lbl: 'Copiar al portapapeles', cls: '', fn: () => { expClipboard.mode = 'copy'; expClipboard.paths = [fp]; updateSelectionUi(); showExplorerNotice('Copiado: ' + nm); } });\n" +
        "  acts.push({ lbl: 'Cortar (Mover)', cls: '', fn: () => { expClipboard.mode = 'cut'; expClipboard.paths = [fp]; updateSelectionUi(); showExplorerNotice('Cortado: ' + nm); } });\n" +
        "  acts.push({ lbl: 'Propiedades y MD5 / SHA-256', cls: '', fn: () => showInfo(fp) });\n" +
        "  acts.push({ lbl: 'Renombrar', cls: '', fn: () => renameItem(fp, nm) });\n" +
        "  acts.push({ lbl: 'Eliminar', cls: 'btn-danger', fn: () => delFile(fp) });\n" +
        "  el.innerHTML = '';\n" +
        "  for (const a of acts) {\n" +
        "    const b = document.createElement('button');\n" +
        "    b.className = 'btn btn-sm ' + a.cls;\n" +
        "    b.textContent = a.lbl;\n" +
        "    b.onclick = () => { document.getElementById('itemActionsModal').classList.remove('open'); a.fn(); };\n" +
        "    el.appendChild(b);\n" +
        "  }\n" +
        "  document.getElementById('itemActionsModal').classList.add('open');\n" +
        "}\n" +
        "function renderExplorer() {\n" +
        "  const list = getFilteredSortedEntries();\n" +
        "  let dirCount = 0, fileCount = 0, totalSize = 0;\n" +
        "  for (const e of list) { if (e.isDir) dirCount++; else { fileCount++; totalSize += (e.size || 0); } }\n" +
        "  const stEl = document.getElementById('explorerStats');\n" +
        "  if (stEl) stEl.textContent = dirCount + (dirCount === 1 ? ' carpeta' : ' carpetas') + ' · ' + fileCount + (fileCount === 1 ? ' archivo' : ' archivos') + ' (' + fmtBytes(totalSize) + ')';\n" +
        "  if (explorerViewMode === 'grid') { renderGrid(list); return; }\n" +
        "  let html = '<table><thead><tr><th style=\"width:32px\">Sel</th><th>Nombre</th><th class=\"col-meta\">Detalle</th><th class=\"col-date\">Fecha</th><th style=\"text-align:right\">Acciones</th></tr></thead><tbody>';\n" +
        "  if (!isVolumeRootPath(currentDir) && !isSearchResultMode) {\n" +
        "    const parent = getParentDirPath(currentDir);\n" +
        "    html += '<tr class=\"file-row\" onclick=\"browse(decodeURIComponent(\\'' + encJs(parent) + '\\'))\"><td colspan=\"5\"><div class=\"row-cell\"><span class=\"row-ico-badge file\">' + WebUi_SVG_FOLDER() + '</span><span class=\"row-name\">.. (Carpeta superior)</span></div></td></tr>';\n" +
        "  }\n" +
        "  if (list.length === 0) { html += '<tr><td colspan=\"5\" class=\"empty\">Sin elementos que coincidan</td></tr>'; }\n" +
        "  else {\n" +
        "    for (const e of list) {\n" +
        "      const fullPath = e.path || (currentDir ? (currentDir + '/' + e.name) : e.name);\n" +
        "      const fpJs = encJs(fullPath); const nJs = encJs(e.name);\n" +
        "      const chk = '<input type=\"checkbox\" class=\"chk\" ' + (selectedPaths.has(fullPath) ? 'checked ' : '') + 'onclick=\"event.stopPropagation();toggleSelect(decodeURIComponent(\\'' + fpJs + '\\'), this.checked)\">';\n" +
        "      const subHint = (isSearchResultMode && e.dir) ? (' · /' + esc(e.dir)) : '';\n" +
        "      const isArch = !e.isDir && isArchiveName(e.name);\n" +
        "      const isTxt = !e.isDir && isTextName(e.name);\n" +
        "      if (e.isDir) {\n" +
        "        const cnt = (typeof e.itemCount === 'number') ? (e.itemCount === 0 ? 'Vacía' : e.itemCount + ' elem.') : 'Carpeta';\n" +
        "        html += '<tr class=\"file-row\"><td>' + chk + '</td>' +\n" +
        "          '<td onclick=\"browse(decodeURIComponent(\\'' + fpJs + '\\'))\"><div class=\"row-cell\"><span class=\"row-ico-badge\">' + WebUi_SVG_FOLDER() + '</span><div class=\"row-name-wrap\"><div class=\"row-name\">' + esc(e.name) + '</div><div class=\"file-sub-mob\">' + cnt + ' · ' + fmtDate(e.mtime) + subHint + '</div></div></div></td>' +\n" +
        "          '<td class=\"col-meta\">' + cnt + '</td><td class=\"col-date\">' + fmtDate(e.mtime) + '</td>' +\n" +
        "          '<td><div class=\"row-acts\">' +\n" +
        "            '<button class=\"btn btn-sm btn-primary btn-resp\" title=\"Descargar ZIP\" onclick=\"downloadZip(decodeURIComponent(\\'' + fpJs + '\\'))\">' + WebUi_SVG_ZIP() + '<span class=\"btn-lbl\">ZIP</span></button>' +\n" +
        "            '<button class=\"btn btn-sm btn-tonal desk-only\" onclick=\"createZipOnDevice(decodeURIComponent(\\'' + fpJs + '\\'), \\'zip\\')\">Guardar ZIP</button>' +\n" +
        "            '<button class=\"btn btn-sm desk-only\" onclick=\"showInfo(decodeURIComponent(\\'' + fpJs + '\\'))\">Info</button>' +\n" +
        "            '<button class=\"btn btn-sm desk-only\" onclick=\"renameItem(decodeURIComponent(\\'' + fpJs + '\\'), decodeURIComponent(\\'' + nJs + '\\'))\">Renombrar</button>' +\n" +
        "            '<button class=\"btn btn-sm btn-danger desk-only\" onclick=\"delFile(decodeURIComponent(\\'' + fpJs + '\\'))\">Eliminar</button>' +\n" +
        "            '<button class=\"btn btn-sm btn-ico-round mob-only\" title=\"Opciones\" onclick=\"openItemMenu(\\'' + fpJs + '\\', \\'' + nJs + '\\', true, false, false)\">' + WebUi_SVG_MORE() + '</button>' +\n" +
        "          '</div></td></tr>';\n" +
        "      } else {\n" +
        "        const dlUrl = '/api/download?path=' + encodeURIComponent(fullPath);\n" +
        "        const badge = e.isImage ? '<img class=\"thumb\" loading=\"lazy\" src=\"' + dlUrl + '&inline=1\">' : ('<span class=\"row-ico-badge ' + (isArch ? 'arch' : 'file') + '\">' + (isArch ? WebUi_SVG_ZIP() : WebUi_SVG_STORAGE()) + '</span>');\n" +
        "        const canPrev = e.isImage || isMediaPreview(e.name) || isTxt;\n" +
        "        const clickAttr = canPrev ? 'onclick=\"openItemPreview(decodeURIComponent(\\'' + fpJs + '\\'), decodeURIComponent(\\'' + nJs + '\\'), ' + e.isImage + ')\"' : (isArch ? 'onclick=\"inspectArchive(decodeURIComponent(\\'' + fpJs + '\\'))\"' : '');\n" +
        "        const archBtnsDesk = isArch ? ('<button class=\"btn btn-sm btn-primary desk-only\" onclick=\"inspectArchive(decodeURIComponent(\\'' + fpJs + '\\'))\">Ver</button><button class=\"btn btn-sm btn-tonal btn-resp\" title=\"Extraer\" onclick=\"extractArchive(decodeURIComponent(\\'' + fpJs + '\\'))\">' + WebUi_SVG_DOWNLOAD() + '<span class=\"btn-lbl\">Extraer</span></button>') : '';\n" +
        "        const editBtnDesk = isTxt ? '<button class=\"btn btn-sm btn-tonal desk-only\" onclick=\"openTextEditor(decodeURIComponent(\\'' + fpJs + '\\'), decodeURIComponent(\\'' + nJs + '\\'))\">Editar</button>' : '';\n" +
        "        const dlBtn = !isArch ? ('<a class=\"btn btn-sm btn-tonal btn-resp\" title=\"Descargar\" href=\"' + dlUrl + '\" download>' + WebUi_SVG_DOWNLOAD() + '<span class=\"btn-lbl\">Descargar</span></a>') : '';\n" +
        "        html += '<tr class=\"file-row\"><td>' + chk + '</td>' +\n" +
        "          '<td ' + clickAttr + '><div class=\"row-cell\">' + badge + '<div class=\"row-name-wrap\"><div class=\"row-name\">' + esc(e.name) + '</div><div class=\"file-sub-mob\">' + fmtBytes(e.size) + ' · ' + fmtDate(e.mtime) + subHint + '</div></div></div></td>' +\n" +
        "          '<td class=\"col-meta\">' + fmtBytes(e.size) + '</td><td class=\"col-date\">' + fmtDate(e.mtime) + '</td>' +\n" +
        "          '<td><div class=\"row-acts\">' + archBtnsDesk + editBtnDesk + dlBtn +\n" +
        "            '<button class=\"btn btn-sm desk-only\" onclick=\"showInfo(decodeURIComponent(\\'' + fpJs + '\\'))\">Info</button>' +\n" +
        "            '<button class=\"btn btn-sm desk-only\" onclick=\"renameItem(decodeURIComponent(\\'' + fpJs + '\\'), decodeURIComponent(\\'' + nJs + '\\'))\">Renombrar</button>' +\n" +
        "            '<button class=\"btn btn-sm btn-danger desk-only\" onclick=\"delFile(decodeURIComponent(\\'' + fpJs + '\\'))\">Eliminar</button>' +\n" +
        "            '<button class=\"btn btn-sm btn-ico-round mob-only\" title=\"Opciones\" onclick=\"openItemMenu(\\'' + fpJs + '\\', \\'' + nJs + '\\', false, ' + isArch + ', ' + isTxt + ')\">' + WebUi_SVG_MORE() + '</button>' +\n" +
        "          '</div></td></tr>';\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "  html += '</tbody></table>';\n" +
        "  document.getElementById('fileTableWrap').innerHTML = html;\n" +
        "}\n" +
        "function renderGrid(list) {\n" +
        "  let g = '<div class=\"file-grid\">';\n" +
        "  if (!isVolumeRootPath(currentDir) && !isSearchResultMode) {\n" +
        "    const parent = getParentDirPath(currentDir);\n" +
        "    g += '<div class=\"grid-card\" style=\"cursor:pointer;justify-content:center;align-items:center\" onclick=\"browse(decodeURIComponent(\\'' + encJs(parent) + '\\'))\"><b>.. Carpeta superior</b></div>';\n" +
        "  }\n" +
        "  for (const e of list) {\n" +
        "    const fullPath = e.path || (currentDir ? (currentDir + '/' + e.name) : e.name);\n" +
        "    const fpJs = encJs(fullPath); const nJs = encJs(e.name);\n" +
        "    const chk = '<input type=\"checkbox\" class=\"chk\" ' + (selectedPaths.has(fullPath) ? 'checked ' : '') + 'onclick=\"event.stopPropagation();toggleSelect(decodeURIComponent(\\'' + fpJs + '\\'), this.checked)\">';\n" +
        "    if (e.isDir) {\n" +
        "      g += '<div class=\"grid-card\"><div style=\"display:flex;justify-content:space-between;align-items:center\">' + chk + '<button class=\"btn btn-sm btn-ico-round\" onclick=\"openItemMenu(\\'' + fpJs + '\\', \\'' + nJs + '\\', true, false, false)\">' + WebUi_SVG_MORE() + '</button></div>' +\n" +
        "        '<div class=\"grid-preview\" onclick=\"browse(decodeURIComponent(\\'' + fpJs + '\\'))\">' + WebUi_SVG_FOLDER().replace('class=\"ico\"', 'class=\"ico\" style=\"width:44px;height:44px;color:var(--md-sys-color-primary)\"') + '</div>' +\n" +
        "        '<div class=\"grid-name\">' + esc(e.name) + '</div><div class=\"grid-actions\"><button class=\"btn btn-sm btn-primary btn-ico-round\" title=\"ZIP\" onclick=\"downloadZip(decodeURIComponent(\\'' + fpJs + '\\'))\">' + WebUi_SVG_ZIP() + '</button><button class=\"btn btn-sm btn-tonal btn-ico-round\" title=\"Guardar ZIP\" onclick=\"createZipOnDevice(decodeURIComponent(\\'' + fpJs + '\\'), \\'zip\\')\">' + WebUi_SVG_DOWNLOAD() + '</button></div></div>';\n" +
        "    } else {\n" +
        "      const dlUrl = '/api/download?path=' + encodeURIComponent(fullPath);\n" +
        "      const isArch = isArchiveName(e.name), isTxt = isTextName(e.name);\n" +
        "      const thumb = e.isImage ? '<img loading=\"lazy\" src=\"' + dlUrl + '&inline=1\">' : '<span style=\"font-weight:800;opacity:0.7\">' + esc(e.name.split('.').pop().toUpperCase()) + '</span>';\n" +
        "      g += '<div class=\"grid-card\"><div style=\"display:flex;justify-content:space-between;align-items:center\">' + chk + '<button class=\"btn btn-sm btn-ico-round\" onclick=\"openItemMenu(\\'' + fpJs + '\\', \\'' + nJs + '\\', false, ' + isArch + ', ' + isTxt + ')\">' + WebUi_SVG_MORE() + '</button></div>' +\n" +
        "        '<div class=\"grid-preview\" onclick=\"openItemPreview(decodeURIComponent(\\'' + fpJs + '\\'), decodeURIComponent(\\'' + nJs + '\\'), ' + e.isImage + ')\">' + thumb + '</div>' +\n" +
        "        '<div class=\"grid-name\">' + esc(e.name) + '</div><div class=\"grid-meta\">' + fmtBytes(e.size) + '</div>' +\n" +
        "        '<div class=\"grid-actions\"><a class=\"btn btn-sm btn-tonal btn-ico-round\" title=\"Descargar\" href=\"' + dlUrl + '\" download>' + WebUi_SVG_DOWNLOAD() + '</a>' +\n" +
        "        (isArch ? '<button class=\"btn btn-sm btn-primary btn-ico-round\" title=\"Extraer\" onclick=\"extractArchive(decodeURIComponent(\\'' + fpJs + '\\'))\">' + WebUi_SVG_ZIP() + '</button>' : '') + '</div></div>';\n" +
        "    }\n" +
        "  }\n" +
        "  g += '</div>';\n" +
        "  if (list.length === 0) g += '<div class=\"empty\">Sin elementos</div>';\n" +
        "  document.getElementById('fileTableWrap').innerHTML = g;\n" +
        "}\n" +
        "function downloadZip(p) { if (p) window.location.href = '/api/zip?path=' + encodeURIComponent(p); }\n" +
        "async function createZipOnDevice(p, fmt) {\n" +
        "  if (!p) return;\n" +
        "  showExplorerNotice('Comprimiendo /' + p + '...');\n" +
        "  const r = await fetch('/api/zip_create?path=' + encodeURIComponent(p) + '&format=' + (fmt || 'zip'), { method: 'POST' });\n" +
        "  const d = await r.json();\n" +
        "  if (r.ok && d.ok) { showExplorerNotice('Creado: /' + d.path + ' (' + fmtBytes(d.bytes) + ', ' + d.files + ' archivos)'); browse(currentDir); }\n" +
        "  else { showExplorerNotice('Error: ' + (d.error || 'Fallo al comprimir')); }\n" +
        "}\n" +
        "function downloadSelected() {\n" +
        "  if (selectedPaths.size === 0) return;\n" +
        "  const byPath = new Map();\n" +
        "  const list = (typeof currentEntries !== 'undefined' && Array.isArray(currentEntries)) ? currentEntries : [];\n" +
        "  for (const e of list) {\n" +
        "    const fp = e.path || (currentDir ? currentDir + '/' + e.name : e.name);\n" +
        "    byPath.set(fp, e);\n" +
        "  }\n" +
        "  const files = [];\n" +
        "  let dirCount = 0;\n" +
        "  for (const fp of selectedPaths) {\n" +
        "    const meta = byPath.get(fp);\n" +
        "    if (meta && meta.isDir) { dirCount++; } else { files.push(fp); }\n" +
        "  }\n" +
        "  if (files.length === 0) {\n" +
        "    showExplorerNotice('Solo hay carpetas seleccionadas; usa ZIP para descargar carpetas.');\n" +
        "    return;\n" +
        "  }\n" +
        "  showExplorerNotice('Descargando ' + files.length + (files.length === 1 ? ' archivo' : ' archivos') + (dirCount > 0 ? ' (' + dirCount + ' carpeta/s omitida/s; usa ZIP para carpetas)' : '') + '...');\n" +
        "  files.forEach((fp, idx) => {\n" +
        "    setTimeout(() => {\n" +
        "      const ifr = document.createElement('iframe');\n" +
        "      ifr.style.display = 'none';\n" +
        "      ifr.src = '/api/download?path=' + encodeURIComponent(fp);\n" +
        "      document.body.appendChild(ifr);\n" +
        "      setTimeout(() => ifr.remove(), 60000);\n" +
        "    }, idx * 250);\n" +
        "  });\n" +
        "}\n" +
        "async function packSelected(fmt) {\n" +
        "  if (selectedPaths.size === 0) return;\n" +
        "  showExplorerNotice('Comprimiendo ' + selectedPaths.size + ' elementos...');\n" +
        "  const r = await fetch('/api/zip_create', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ paths: Array.from(selectedPaths), format: fmt || 'zip' }) });\n" +
        "  const d = await r.json();\n" +
        "  if (r.ok && d.ok) { showExplorerNotice('Creado: /' + d.path + ' (' + fmtBytes(d.bytes) + ')'); clearSelection(); browse(currentDir); }\n" +
        "  else { showExplorerNotice('Error: ' + (d.error || 'No se pudo comprimir')); }\n" +
        "}\n" +
        "function copyCutSelected(mode) {\n" +
        "  if (selectedPaths.size === 0) return;\n" +
        "  expClipboard.mode = mode; expClipboard.paths = Array.from(selectedPaths);\n" +
        "  selectedPaths.clear(); updateSelectionUi(); renderExplorer();\n" +
        "  showExplorerNotice((mode === 'cut' ? 'Cortados ' : 'Copiados ') + expClipboard.paths.length + ' elemento(s).');\n" +
        "}\n" +
        "function clearClipboard() { expClipboard.mode = ''; expClipboard.paths = []; updateSelectionUi(); }\n" +
        "async function pasteClipboard() {\n" +
        "  if (expClipboard.paths.length === 0) return;\n" +
        "  const ep = expClipboard.mode === 'cut' ? '/api/move' : '/api/copy';\n" +
        "  const r = await fetch(ep, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ paths: expClipboard.paths, destDir: currentDir }) });\n" +
        "  const d = await r.json();\n" +
        "  if (r.ok && d.ok) { showExplorerNotice('Operación completada (' + d.count + ' elemento/s)'); clearClipboard(); browse(currentDir); }\n" +
        "  else { alert(d.error || 'Error al pegar'); }\n" +
        "}\n" +
        "async function deleteSelected() {\n" +
        "  if (selectedPaths.size === 0 || !confirm('¿Eliminar ' + selectedPaths.size + ' elementos seleccionados?')) return;\n" +
        "  const r = await fetch('/api/delete_batch', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ paths: Array.from(selectedPaths) }) });\n" +
        "  if (r.ok) { clearSelection(); browse(currentDir); }\n" +
        "}\n" +
        "async function createSubfolderFromExplorer() {\n" +
        "  const name = prompt('Nombre de la nueva carpeta dentro de /' + currentDir + ':');\n" +
        "  if (!name || !name.trim()) return;\n" +
        "  const path = (currentDir ? currentDir + '/' : '') + name.trim().replace(/^\\/+|\\/+$/g, '');\n" +
        "  const r = await fetch('/api/mkdir?dir=' + encodeURIComponent(path), { method: 'POST' });\n" +
        "  if (r.ok) browse(currentDir);\n" +
        "}\n" +
        "async function createNewFile() {\n" +
        "  const name = prompt('Nombre del nuevo archivo en /' + currentDir + ' (ej: notas.txt):');\n" +
        "  if (!name || !name.trim()) return;\n" +
        "  const r = await fetch('/api/touch?dir=' + encodeURIComponent(currentDir) + '&name=' + encodeURIComponent(name.trim()), { method: 'POST' });\n" +
        "  if (r.ok) { browse(currentDir); openTextEditor((currentDir ? currentDir + '/' : '') + name.trim(), name.trim()); }\n" +
        "  else { const d = await r.json(); alert(d.error || 'Error'); }\n" +
        "}\n" +
        "async function renameItem(fullPath, oldName) {\n" +
        "  const next = prompt('Nuevo nombre para \"' + oldName + '\":', oldName);\n" +
        "  if (!next || !next.trim() || next.trim() === oldName) return;\n" +
        "  const r = await fetch('/api/rename?path=' + encodeURIComponent(fullPath) + '&name=' + encodeURIComponent(next.trim()), { method: 'POST' });\n" +
        "  if (r.ok) browse(currentDir); else { const d = await r.json(); alert(d.error || 'No se pudo renombrar'); }\n" +
        "}\n" +
        "async function extractArchive(path) {\n" +
        "  showExplorerNotice('Extrayendo /' + path + '...');\n" +
        "  const r = await fetch('/api/extract?path=' + encodeURIComponent(path), { method: 'POST' });\n" +
        "  const d = await r.json();\n" +
        "  if (r.ok && d.ok) { showExplorerNotice('Extraído en /' + d.dest + ' (' + d.files + ' archivos)'); document.getElementById('archiveModal').classList.remove('open'); browse(currentDir); }\n" +
        "  else { showExplorerNotice('Error: ' + (d.error || 'Fallo al extraer')); }\n" +
        "}\n" +
        "async function inspectArchive(path) {\n" +
        "  const r = await fetch('/api/archive_list?path=' + encodeURIComponent(path));\n" +
        "  const d = await r.json();\n" +
        "  if (!r.ok || !d.ok) { alert(d.error || 'Error al abrir comprimido'); return; }\n" +
        "  document.getElementById('archTitle').textContent = d.name + ' (' + d.entries.length + ' entradas)';\n" +
        "  document.getElementById('archExtractBtn').onclick = () => extractArchive(path);\n" +
        "  let h = '<table><thead><tr><th>Entrada</th><th>Tamaño</th><th>Comprimido</th></tr></thead><tbody>';\n" +
        "  for (const e of d.entries) h += '<tr><td>' + esc(e.name) + '</td><td>' + (e.isDir ? 'Carpeta' : fmtBytes(e.size)) + '</td><td>' + (e.compressedSize >= 0 ? fmtBytes(e.compressedSize) : '—') + '</td></tr>';\n" +
        "  h += '</tbody></table>';\n" +
        "  document.getElementById('archBody').innerHTML = h;\n" +
        "  document.getElementById('archiveModal').classList.add('open');\n" +
        "}\n" +
        "async function showInfo(path) {\n" +
        "  const r = await fetch('/api/info?path=' + encodeURIComponent(path));\n" +
        "  const d = await r.json();\n" +
        "  if (!r.ok || !d.ok) return;\n" +
        "  document.getElementById('infoTitle').textContent = d.name;\n" +
        "  const rows = [\n" +
        "    ['Ruta', '/' + d.path],\n" +
        "    ['Tipo', d.isDir ? 'Carpeta' : d.mime],\n" +
        "    ['Tamaño', fmtBytes(d.size) + (d.isDir ? (' (' + d.fileCount + ' archivos, ' + d.dirCount + ' subcarpetas)') : '')],\n" +
        "    ['Permisos', d.perms],\n" +
        "    ['Modificado', fmtDate(d.mtime)]\n" +
        "  ];\n" +
        "  if (!d.isDir) { rows.push(['MD5', d.md5]); rows.push(['SHA-256', d.sha256]); }\n" +
        "  document.getElementById('infoBody').innerHTML = rows.map(r => '<div class=\"kv-k\">' + esc(r[0]) + '</div><div class=\"kv-v\">' + esc(r[1]) + '</div>').join('');\n" +
        "  document.getElementById('infoModal').classList.add('open');\n" +
        "}\n" +
        "async function openTextEditor(path, name) {\n" +
        "  const r = await fetch('/api/text?path=' + encodeURIComponent(path));\n" +
        "  const d = await r.json();\n" +
        "  if (!r.ok || !d.ok) { alert(d.error || 'No editable como texto'); return; }\n" +
        "  currentEditPath = path;\n" +
        "  document.getElementById('edTitle').textContent = 'Editar: ' + name;\n" +
        "  document.getElementById('edTextarea').value = d.content || '';\n" +
        "  document.getElementById('editorModal').classList.add('open');\n" +
        "}\n" +
        "async function saveEditorFile() {\n" +
        "  if (!currentEditPath) return;\n" +
        "  const content = document.getElementById('edTextarea').value;\n" +
        "  const r = await fetch('/api/text?path=' + encodeURIComponent(currentEditPath), { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ path: currentEditPath, content }) });\n" +
        "  if (r.ok) { document.getElementById('editorModal').classList.remove('open'); showExplorerNotice('Archivo guardado correctamente'); browse(currentDir); }\n" +
        "  else { const d = await r.json(); alert(d.error || 'Error al guardar'); }\n" +
        "}\n" +
        "function openItemPreview(path, name, isImg) {\n" +
        "  if (isTextName(name)) { openTextEditor(path, name); return; }\n" +
        "  const url = '/api/download?path=' + encodeURIComponent(path) + '&inline=1';\n" +
        "  const dl = '/api/download?path=' + encodeURIComponent(path);\n" +
        "  document.getElementById('mediaTitle').textContent = name;\n" +
        "  document.getElementById('mediaDlLink').href = dl;\n" +
        "  const body = document.getElementById('mediaBody');\n" +
        "  if (isImg) body.innerHTML = '<img src=\"' + url + '\" style=\"max-width:100%;max-height:72vh;border-radius:18px\">';\n" +
        "  else if (/\\.(mp4|mov|webm|mkv)$/i.test(name)) body.innerHTML = '<video controls autoplay src=\"' + url + '\" style=\"max-width:100%;max-height:72vh;border-radius:18px\"></video>';\n" +
        "  else if (/\\.(mp3|wav|flac|m4a|aac|ogg|opus)$/i.test(name)) body.innerHTML = '<audio controls autoplay src=\"' + url + '\" style=\"width:100%\"></audio>';\n" +
        "  else if (/\\.pdf$/i.test(name)) body.innerHTML = '<iframe src=\"' + url + '\" style=\"width:100%;height:70vh;border:none;border-radius:16px\"></iframe>';\n" +
        "  else body.innerHTML = '<div class=\"empty\">Usa Descargar para abrir este archivo</div>';\n" +
        "  document.getElementById('mediaModal').classList.add('open');\n" +
        "}\n" +
        "function closeMediaModal() {\n" +
        "  document.getElementById('mediaBody').innerHTML = '';\n" +
        "  document.getElementById('mediaModal').classList.remove('open');\n" +
        "}\n";
}
