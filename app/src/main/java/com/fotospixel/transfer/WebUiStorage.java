package com.fotospixel.transfer;

/**
 * Web UI assets for the M3 Expressive "Almacenamiento" section (DaisyDisk / SquirrelDisk
 * interactive SVG Sunburst analyzer + "Liberar espacio" collector).
 */
final class WebUiStorage {

    private WebUiStorage() {}

    static final String STORAGE_CSS =
        "  .st-shell { display: grid; grid-template-columns: minmax(290px, 410px) minmax(0, 1fr); gap: 18px; align-items: start; }\n" +
        "  @media (max-width: 860px) { .st-shell { grid-template-columns: 1fr; } }\n" +
        "  .st-chart-box { position: relative; display: flex; flex-direction: column; align-items: center; padding: 8px 0 4px; }\n" +
        "  .st-svg { width: 100%; max-width: 340px; height: auto; display: block; user-select: none; }\n" +
        "  .st-arc { cursor: pointer; transition: opacity 0.16s ease, transform 0.16s ease; }\n" +
        "  .st-arc:hover { opacity: 0.84; }\n" +
        "  .st-hub { cursor: pointer; }\n" +
        "  .st-hover-bar {\n" +
        "    margin-top: 10px; width: 100%; min-height: 40px; border-radius: 16px;\n" +
        "    background: var(--md-sys-color-surface-container-high); padding: 9px 14px;\n" +
        "    display: flex; align-items: center; justify-content: space-between; gap: 10px;\n" +
        "    font-size: 12px; color: var(--md-sys-color-on-surface-variant);\n" +
        "  }\n" +
        "  .st-crumbs { display: flex; flex-wrap: wrap; align-items: center; gap: 6px; margin-bottom: 12px; }\n" +
        "  .st-crumb {\n" +
        "    background: var(--md-sys-color-surface-container-high); color: var(--md-sys-color-on-surface);\n" +
        "    border-radius: 999px; padding: 6px 12px; font-size: 12px; font-weight: 700; cursor: pointer;\n" +
        "  }\n" +
        "  .st-crumb.active { background: var(--md-sys-color-primary-container); color: var(--md-sys-color-on-primary-container); }\n" +
        "  .st-collector {\n" +
        "    background: var(--md-sys-color-error-container); color: var(--md-sys-color-on-error-container);\n" +
        "    border-radius: 22px; padding: 14px 16px; margin-bottom: 14px;\n" +
        "    display: none; flex-direction: column; gap: 10px;\n" +
        "  }\n" +
        "  .st-collector.open { display: flex; }\n" +
        "  .st-col-tags { display: flex; flex-wrap: wrap; gap: 6px; }\n" +
        "  .st-col-tag {\n" +
        "    display: inline-flex; align-items: center; gap: 6px; padding: 5px 10px; border-radius: 999px;\n" +
        "    background: rgba(0,0,0,0.14); font-size: 11px; font-weight: 700; cursor: pointer;\n" +
        "  }\n" +
        "  .st-row {\n" +
        "    display: grid; grid-template-columns: 12px minmax(0, 1fr) auto auto; align-items: center; gap: 10px;\n" +
        "    background: var(--md-sys-color-surface-container-high); border-radius: 18px;\n" +
        "    padding: 10px 12px; margin-bottom: 8px; transition: background-color 0.16s;\n" +
        "  }\n" +
        "  .st-row:hover { background: var(--md-sys-color-surface-container-highest); }\n" +
        "  .st-row.marked { outline: 2px solid var(--md-sys-color-error); }\n" +
        "  .st-dot { width: 12px; height: 12px; border-radius: 999px; flex-shrink: 0; }\n" +
        "  .st-row-main { min-width: 0; cursor: pointer; }\n" +
        "  .st-row-name { font-weight: 700; font-size: 13px; color: var(--md-sys-color-on-surface); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }\n" +
        "  .st-row-sub { font-size: 11px; color: var(--md-sys-color-on-surface-variant); margin-top: 2px; }\n" +
        "  .st-mini-bar { height: 5px; border-radius: 999px; background: var(--md-sys-color-surface-container-lowest); overflow: hidden; margin-top: 5px; }\n" +
        "  .st-mini-fill { height: 100%; border-radius: 999px; transition: width 0.25s ease; }\n" +
        "  .st-row-size { font-weight: 700; font-size: 12.5px; color: var(--md-sys-color-on-surface); font-variant-numeric: tabular-nums; white-space: nowrap; }\n" +
        "  .st-row-acts { display: flex; gap: 6px; align-items: center; }\n" +
        "  .st-ico-btn {\n" +
        "    width: 34px; height: 34px; border-radius: 999px; padding: 0; display: inline-flex;\n" +
        "    align-items: center; justify-content: center; cursor: pointer;\n" +
        "    background: var(--md-sys-color-surface-container-lowest); color: var(--md-sys-color-on-surface);\n" +
        "  }\n" +
        "  .st-ico-btn.collected { background: var(--md-sys-color-error); color: var(--md-sys-color-on-error); }\n" +
        "  @media (max-width: 640px) {\n" +
        "    .floating-nav { max-width: calc(100vw - 16px); gap: 3px; padding: 5px; }\n" +
        "    .nav-pill { padding: 8px 10px; font-size: 11px; gap: 4px; white-space: nowrap; }\n" +
        "  }\n";

    static final String SECTION_HTML =
        "  <div id=\"tabStorage\" style=\"display:none\">\n" +
        "    <div class=\"card\">\n" +
        "      <div style=\"display:flex;flex-wrap:wrap;align-items:center;justify-content:space-between;gap:12px;margin-bottom:10px\">\n" +
        "        <div>\n" +
        "          <div class=\"card-title\" style=\"margin-bottom:2px\"><span class=\"title-ico\">" + WebUi.SVG_STORAGE + " Analizador de almacenamiento</span></div>\n" +
        "          <div class=\"sub\" id=\"stVolSummary\">Analiza qué carpetas ocupan más espacio y libera lo que no necesites.</div>\n" +
        "        </div>\n" +
        "        <div style=\"display:flex;gap:8px;align-items:center\">\n" +
        "          <button class=\"btn btn-primary btn-sm\" id=\"stRescanBtn\" onclick=\"runStorageAnalyze(stBaseDir)\">" + WebUi.SVG_REFRESH + " Analizar</button>\n" +
        "          <button class=\"btn btn-tonal btn-sm\" id=\"stOpenFilesBtn\" onclick=\"openStorageFocusInExplorer()\">" + WebUi.SVG_FOLDER + " Ver en Archivos</button>\n" +
        "        </div>\n" +
        "      </div>\n" +
        "      <div class=\"chips\" id=\"stVolChips\" style=\"margin-bottom:8px\"></div>\n" +
        "      <div class=\"storage-bar-bg\"><div class=\"storage-bar-fill\" id=\"stVolBar\"></div></div>\n" +
        "    </div>\n" +
        "\n" +
        "    <div class=\"st-collector\" id=\"stCollectorCard\">\n" +
        "      <div style=\"display:flex;flex-wrap:wrap;align-items:center;justify-content:space-between;gap:10px\">\n" +
        "        <div>\n" +
        "          <div style=\"font-weight:700;font-size:15px\" id=\"stCollectorTitle\">0 elementos marcados · 0 B liberables</div>\n" +
        "          <div style=\"font-size:12px;opacity:0.86\">Cola segura de SquirrelDisk: revisa y borra en un toque con progreso en vivo.</div>\n" +
        "        </div>\n" +
        "        <div style=\"display:flex;gap:8px;flex-wrap:wrap\">\n" +
        "          <button class=\"btn btn-tonal btn-sm\" onclick=\"clearStorageCollector()\">Vaciar</button>\n" +
        "          <button class=\"btn btn-danger btn-sm\" id=\"stPurgeBtn\" onclick=\"purgeStorageCollector()\">" + WebUi.SVG_STOP + " Liberar espacio</button>\n" +
        "        </div>\n" +
        "      </div>\n" +
        "      <div class=\"st-col-tags\" id=\"stCollectorTags\"></div>\n" +
        "    </div>\n" +
        "\n" +
        "    <div class=\"st-shell\">\n" +
        "      <div class=\"card\">\n" +
        "        <div class=\"st-crumbs\" id=\"stBreadcrumbs\"></div>\n" +
        "        <div class=\"st-chart-box\">\n" +
        "          <svg class=\"st-svg\" id=\"stSunburstSvg\" viewBox=\"0 0 320 320\" role=\"img\" aria-label=\"Mapa circular de almacenamiento\"></svg>\n" +
        "          <div class=\"st-hover-bar\" id=\"stHoverBar\">\n" +
        "            <span>Toca un sector para profundizar o el centro para subir</span>\n" +
        "            <strong id=\"stHoverPct\"></strong>\n" +
        "          </div>\n" +
        "        </div>\n" +
        "      </div>\n" +
        "\n" +
        "      <div class=\"card\">\n" +
        "        <div style=\"display:flex;align-items:center;justify-content:space-between;gap:8px;margin-bottom:10px\">\n" +
        "          <div class=\"card-title\" style=\"margin-bottom:0\"><span class=\"title-ico\">" + WebUi.SVG_FOLDER + " <span id=\"stListHeading\">Elementos más pesados</span></span></div>\n" +
        "          <span class=\"badge b-folder\" id=\"stFocusCounts\">--</span>\n" +
        "        </div>\n" +
        "        <div id=\"stListWrap\"><div class=\"empty\">Pulsa Analizar o abre Almacenamiento para escanear.</div></div>\n" +
        "      </div>\n" +
        "    </div>\n" +
        "  </div>\n";

    static final String STORAGE_JS =
        "\n// --- Almacenamiento (DaisyDisk / SquirrelDisk Sunburst) ---\n" +
        "let stBaseDir = '';\n" +
        "let stRootTree = null;\n" +
        "let stFocusNode = null;\n" +
        "let stNavStack = [];\n" +
        "let stCollector = new Map(); // path -> { name, path, size, isDir }\n" +
        "let stScanning = false;\n" +
        "\n" +
        "function stPaletteColor(idx) {\n" +
        "  const cs = getComputedStyle(document.documentElement);\n" +
        "  const p = cs.getPropertyValue('--md-sys-color-primary').trim() || '#6750A4';\n" +
        "  const t = cs.getPropertyValue('--md-sys-color-tertiary').trim() || '#7D5260';\n" +
        "  const s = cs.getPropertyValue('--md-sys-color-secondary').trim() || '#625B71';\n" +
        "  const pal = [p, t, '#E59500', s, '#178D6D', '#5A5FE0', '#D84B6E', '#0E86B4'];\n" +
        "  return pal[Math.abs(idx || 0) % pal.length];\n" +
        "}\n" +
        "\n" +
        "async function loadStorageVolumesChips() {\n" +
        "  try {\n" +
        "    const r = await fetch('/api/volumes');\n" +
        "    const d = await r.json();\n" +
        "    const wrap = document.getElementById('stVolChips');\n" +
        "    if (!wrap || !d.ok || !Array.isArray(d.volumes)) return;\n" +
        "    wrap.innerHTML = d.volumes.map(v => {\n" +
        "      const p = v.path || '';\n" +
        "      const active = (stBaseDir === p) || (!stBaseDir && !p);\n" +
        "      const freeTxt = fmtBytes(v.freeBytes || 0);\n" +
        "      return '<span class=\"chip' + (active ? ' active' : '') + '\" data-vol=\"' + esc(p) + '\">' +\n" +
        "        esc(v.label) + ' · ' + freeTxt + ' libres</span>';\n" +
        "    }).join('');\n" +
        "    wrap.querySelectorAll('[data-vol]').forEach(el => {\n" +
        "      el.onclick = () => selectStorageVolume(el.getAttribute('data-vol') || '');\n" +
        "    });\n" +
        "  } catch (e) {}\n" +
        "}\n" +
        "\n" +
        "function selectStorageVolume(path) {\n" +
        "  stBaseDir = path || '';\n" +
        "  runStorageAnalyze(stBaseDir);\n" +
        "}\n" +
        "\n" +
        "async function runStorageAnalyze(dirPath, keepStack) {\n" +
        "  if (stScanning) return;\n" +
        "  stScanning = true;\n" +
        "  const btn = document.getElementById('stRescanBtn');\n" +
        "  if (btn) btn.disabled = true;\n" +
        "  document.getElementById('stListWrap').innerHTML = '<div class=\"empty\">Escaneando carpetas y tamaños...</div>';\n" +
        "  loadStorageVolumesChips();\n" +
        "  try {\n" +
        "    const r = await fetch('/api/analyze?dir=' + encodeURIComponent(dirPath || '') + '&depth=3');\n" +
        "    const d = await r.json();\n" +
        "    if (!r.ok || !d.ok || !d.root) {\n" +
        "      document.getElementById('stListWrap').innerHTML = '<div class=\"empty\">' + esc((d && d.error) || 'No se pudo analizar') + '</div>';\n" +
        "      return;\n" +
        "    }\n" +
        "    const used = Math.max(0, (d.totalBytes || 0) - (d.freeBytes || 0));\n" +
        "    const pct = d.totalBytes > 0 ? Math.round((used / d.totalBytes) * 100) : 0;\n" +
        "    document.getElementById('stVolSummary').textContent = (d.volumeLabel || 'Almacenamiento') + ': ' + fmtBytes(used) + ' usados · ' + fmtBytes(d.freeBytes || 0) + ' libres de ' + fmtBytes(d.totalBytes || 0) + ' (' + pct + '%)';\n" +
        "    document.getElementById('stVolBar').style.width = pct + '%';\n" +
        "    if (!keepStack) {\n" +
        "      stRootTree = d.root;\n" +
        "      stFocusNode = d.root;\n" +
        "      stNavStack = [d.root];\n" +
        "    } else {\n" +
        "      stFocusNode = d.root;\n" +
        "      if (stNavStack.length === 0) stNavStack = [d.root];\n" +
        "      else stNavStack[stNavStack.length - 1] = d.root;\n" +
        "    }\n" +
        "    renderStorageView();\n" +
        "  } catch (e) {\n" +
        "    document.getElementById('stListWrap').innerHTML = '<div class=\"empty\">Error de conexión al analizar almacenamiento.</div>';\n" +
        "  } finally {\n" +
        "    stScanning = false;\n" +
        "    if (btn) btn.disabled = false;\n" +
        "  }\n" +
        "}\n" +
        "\n" +
        "function focusStorageNode(node) {\n" +
        "  if (!node) return;\n" +
        "  if (node.isDir && node.hasMore && (!node.children || node.children.length === 0)) {\n" +
        "    stNavStack.push(node);\n" +
        "    runStorageAnalyze(node.path, true);\n" +
        "    return;\n" +
        "  }\n" +
        "  const existingIdx = stNavStack.indexOf(node);\n" +
        "  if (existingIdx >= 0) {\n" +
        "    stNavStack = stNavStack.slice(0, existingIdx + 1);\n" +
        "  } else {\n" +
        "    stNavStack.push(node);\n" +
        "  }\n" +
        "  stFocusNode = node;\n" +
        "  renderStorageView();\n" +
        "}\n" +
        "\n" +
        "function zoomOutStorageNode() {\n" +
        "  if (stNavStack.length > 1) {\n" +
        "    stNavStack.pop();\n" +
        "    stFocusNode = stNavStack[stNavStack.length - 1];\n" +
        "    renderStorageView();\n" +
        "  }\n" +
        "}\n" +
        "\n" +
        "function jumpStorageBreadcrumb(idx) {\n" +
        "  if (idx < 0 || idx >= stNavStack.length) return;\n" +
        "  const target = stNavStack[idx];\n" +
        "  stNavStack = stNavStack.slice(0, idx + 1);\n" +
        "  if (target && (!target.children || target.children.length === 0) && target.hasMore) {\n" +
        "    runStorageAnalyze(target.path, true);\n" +
        "    return;\n" +
        "  }\n" +
        "  stFocusNode = target;\n" +
        "  renderStorageView();\n" +
        "}\n" +
        "\n" +
        "function openStorageFocusInExplorer() {\n" +
        "  if (!stFocusNode) { switchTab('files'); return; }\n" +
        "  currentDir = stFocusNode.path || '';\n" +
        "  switchTab('files');\n" +
        "}\n" +
        "\n" +
        "function toggleStorageCollect(node) {\n" +
        "  if (!node || !node.path || node.path === stBaseDir) return;\n" +
        "  if (stCollector.has(node.path)) {\n" +
        "    stCollector.delete(node.path);\n" +
        "  } else {\n" +
        "    stCollector.set(node.path, { name: node.name, path: node.path, size: node.size || 0, isDir: !!node.isDir });\n" +
        "  }\n" +
        "  renderStorageCollector();\n" +
        "  renderStorageView();\n" +
        "}\n" +
        "\n" +
        "function clearStorageCollector() {\n" +
        "  stCollector.clear();\n" +
        "  renderStorageCollector();\n" +
        "  renderStorageView();\n" +
        "}\n" +
        "\n" +
        "function renderStorageCollector() {\n" +
        "  const card = document.getElementById('stCollectorCard');\n" +
        "  if (!card) return;\n" +
        "  const items = Array.from(stCollector.values());\n" +
        "  card.classList.toggle('open', items.length > 0);\n" +
        "  if (items.length === 0) return;\n" +
        "  let total = 0;\n" +
        "  items.forEach(it => total += (it.size || 0));\n" +
        "  document.getElementById('stCollectorTitle').textContent = items.length + (items.length === 1 ? ' elemento marcado' : ' elementos marcados') + ' · ' + fmtBytes(total) + ' liberables';\n" +
        "  document.getElementById('stCollectorTags').innerHTML = items.map(it =>\n" +
        "    '<span class=\"st-col-tag\" data-colpath=\"' + esc(it.path) + '\">' +\n" +
        "    esc(it.name) + ' (' + fmtBytes(it.size) + ') &times;</span>'\n" +
        "  ).join('');\n" +
        "  document.querySelectorAll('#stCollectorTags [data-colpath]').forEach(el => {\n" +
        "    el.onclick = () => {\n" +
        "      stCollector.delete(el.getAttribute('data-colpath') || '');\n" +
        "      renderStorageCollector();\n" +
        "      renderStorageView();\n" +
        "    };\n" +
        "  });\n" +
        "}\n" +
        "\n" +
        "async function purgeStorageCollector() {\n" +
        "  const paths = Array.from(stCollector.keys());\n" +
        "  if (paths.length === 0) return;\n" +
        "  let total = 0;\n" +
        "  stCollector.forEach(it => total += (it.size || 0));\n" +
        "  if (!confirm('¿Eliminar definitivamente ' + paths.length + ' elementos (' + fmtBytes(total) + ')?')) return;\n" +
        "  const btn = document.getElementById('stPurgeBtn');\n" +
        "  if (btn) btn.disabled = true;\n" +
        "  try {\n" +
        "    const r = await fetch('/api/delete_batch', {\n" +
        "      method: 'POST',\n" +
        "      headers: { 'Content-Type': 'application/json' },\n" +
        "      body: JSON.stringify({ paths })\n" +
        "    });\n" +
        "    const d = await r.json();\n" +
        "    if (r.ok && d.ok) {\n" +
        "      stCollector.clear();\n" +
        "      renderStorageCollector();\n" +
        "      runStorageAnalyze(stBaseDir);\n" +
        "    } else {\n" +
        "      alert((d && d.error) || 'No se pudieron eliminar algunos archivos');\n" +
        "    }\n" +
        "  } catch (e) {\n" +
        "    alert('Error de conexión al eliminar');\n" +
        "  } finally {\n" +
        "    if (btn) btn.disabled = false;\n" +
        "  }\n" +
        "}\n" +
        "\n" +
        "function stPolar(cx, cy, r, deg) {\n" +
        "  const rad = (deg - 90) * Math.PI / 180;\n" +
        "  return { x: cx + r * Math.cos(rad), y: cy + r * Math.sin(rad) };\n" +
        "}\n" +
        "\n" +
        "function stArcPath(cx, cy, rIn, rOut, a0, sweep) {\n" +
        "  const a1 = a0 + Math.min(359.7, sweep);\n" +
        "  const p1 = stPolar(cx, cy, rOut, a0);\n" +
        "  const p2 = stPolar(cx, cy, rOut, a1);\n" +
        "  const p3 = stPolar(cx, cy, rIn, a1);\n" +
        "  const p4 = stPolar(cx, cy, rIn, a0);\n" +
        "  const large = (a1 - a0) > 180 ? 1 : 0;\n" +
        "  return 'M ' + p1.x.toFixed(2) + ' ' + p1.y.toFixed(2) +\n" +
        "         ' A ' + rOut + ' ' + rOut + ' 0 ' + large + ' 1 ' + p2.x.toFixed(2) + ' ' + p2.y.toFixed(2) +\n" +
        "         ' L ' + p3.x.toFixed(2) + ' ' + p3.y.toFixed(2) +\n" +
        "         ' A ' + rIn + ' ' + rIn + ' 0 ' + large + ' 0 ' + p4.x.toFixed(2) + ' ' + p4.y.toFixed(2) + ' Z';\n" +
        "}\n" +
        "\n" +
        "function renderStorageView() {\n" +
        "  if (!stFocusNode) return;\n" +
        "  const crumbsEl = document.getElementById('stBreadcrumbs');\n" +
        "  crumbsEl.innerHTML = stNavStack.map((n, i) => {\n" +
        "    const last = (i === stNavStack.length - 1);\n" +
        "    return '<span class=\"st-crumb' + (last ? ' active' : '') + '\" onclick=\"jumpStorageBreadcrumb(' + i + ')\">' + esc(i === 0 ? 'Raíz' : n.name) + '</span>';\n" +
        "  }).join('');\n" +
        "\n" +
        "  document.getElementById('stListHeading').textContent = (stNavStack.length <= 1 ? 'Carpetas principales' : stFocusNode.name);\n" +
        "  document.getElementById('stFocusCounts').textContent = (stFocusNode.dirCount || 0) + ' carpetas · ' + (stFocusNode.fileCount || 0) + ' archivos';\n" +
        "\n" +
        "  renderStorageSunburst();\n" +
        "  renderStorageBreakdownList();\n" +
        "}\n" +
        "\n" +
        "function renderStorageSunburst() {\n" +
        "  const svg = document.getElementById('stSunburstSvg');\n" +
        "  if (!svg || !stFocusNode) return;\n" +
        "  const cx = 160, cy = 160;\n" +
        "  const rings = [[54, 86], [90, 121], [125, 153]];\n" +
        "  let html = '';\n" +
        "  rings.forEach(rg => {\n" +
        "    const rm = (rg[0] + rg[1]) / 2;\n" +
        "    html += '<circle cx=\"160\" cy=\"160\" r=\"' + rm + '\" fill=\"none\" stroke=\"var(--md-sys-color-surface-container-high)\" stroke-width=\"' + (rg[1] - rg[0]) + '\" opacity=\"0.55\"/>';\n" +
        "  });\n" +
        "\n" +
        "  const nodeMap = new Map();\n" +
        "  let idSeq = 0;\n" +
        "  function addRings(parent, depth, startDeg, sweepDeg, branchColor) {\n" +
        "    if (depth > 3 || !parent.children || parent.size <= 0 || sweepDeg < 1.8) return;\n" +
        "    const rg = rings[depth - 1];\n" +
        "    let cur = startDeg;\n" +
        "    const alphas = [1, 0.78, 0.56];\n" +
        "    for (let i = 0; i < parent.children.length; i++) {\n" +
        "      const ch = parent.children[i];\n" +
        "      if (!ch || ch.size <= 0) continue;\n" +
        "      const rawSweep = (ch.size / parent.size) * sweepDeg;\n" +
        "      const col = depth === 1 ? stPaletteColor(ch.colorIndex) : branchColor;\n" +
        "      if (rawSweep >= 1.35) {\n" +
        "        const gap = depth === 1 ? 1.1 : 0.6;\n" +
        "        const visSweep = rawSweep > gap * 2 ? rawSweep - gap : rawSweep * 0.85;\n" +
        "        const visStart = cur + (rawSweep - visSweep) * 0.5;\n" +
        "        const nid = 's_' + (++idSeq);\n" +
        "        nodeMap.set(nid, ch);\n" +
        "        const marked = ch.path && stCollector.has(ch.path);\n" +
        "        const dPath = stArcPath(cx, cy, rg[0], rg[1], visStart, visSweep);\n" +
        "        html += '<path class=\"st-arc\" data-nid=\"' + nid + '\" d=\"' + dPath + '\" fill=\"' + (marked ? 'var(--md-sys-color-error)' : col) + '\" fill-opacity=\"' + alphas[depth - 1] + '\"' + (marked ? ' stroke=\"var(--md-sys-color-on-error-container)\" stroke-width=\"1.6\"' : '') + '/>';\n" +
        "        if (ch.isDir && ch.children && ch.children.length > 0 && depth < 3) {\n" +
        "          addRings(ch, depth + 1, visStart, visSweep, col);\n" +
        "        }\n" +
        "      }\n" +
        "      cur += rawSweep;\n" +
        "    }\n" +
        "  }\n" +
        "  addRings(stFocusNode, 1, 0, 360, stPaletteColor(0));\n" +
        "\n" +
        "  const canUp = stNavStack.length > 1;\n" +
        "  const focusLabel = (stNavStack.length <= 1 ? 'Total analizado' : stFocusNode.name || '');\n" +
        "  const shortLabel = focusLabel.length > 13 ? focusLabel.slice(0, 12) + '...' : focusLabel;\n" +
        "  html += '<g class=\"st-hub\" id=\"stHubGroup\">' +\n" +
        "    '<circle cx=\"160\" cy=\"160\" r=\"48\" fill=\"var(--md-sys-color-surface-container-lowest)\" stroke=\"var(--md-sys-color-outline-variant)\" stroke-width=\"1.5\"/>' +\n" +
        "    '<text x=\"160\" y=\"' + (canUp ? '151' : '156') + '\" text-anchor=\"middle\" font-size=\"11\" font-weight=\"700\" fill=\"var(--md-sys-color-on-surface)\">' + esc(shortLabel) + '</text>' +\n" +
        "    '<text x=\"160\" y=\"' + (canUp ? '168' : '174') + '\" text-anchor=\"middle\" font-size=\"13\" font-weight=\"700\" fill=\"var(--md-sys-color-primary)\">' + esc(fmtBytes(stFocusNode.size || 0)) + '</text>' +\n" +
        "    (canUp ? '<text x=\"160\" y=\"184\" text-anchor=\"middle\" font-size=\"10\" font-weight=\"700\" fill=\"var(--md-sys-color-on-surface-variant)\">&uarr; Subir</text>' : '') +\n" +
        "    '</g>';\n" +
        "  svg.innerHTML = html;\n" +
        "\n" +
        "  const hubEl = document.getElementById('stHubGroup');\n" +
        "  if (hubEl) hubEl.onclick = () => zoomOutStorageNode();\n" +
        "  svg.querySelectorAll('.st-arc').forEach(el => {\n" +
        "    const node = nodeMap.get(el.getAttribute('data-nid'));\n" +
        "    if (!node) return;\n" +
        "    el.onmouseenter = () => {\n" +
        "      const pct = stFocusNode.size > 0 ? ((node.size / stFocusNode.size) * 100).toFixed(1) : '0.0';\n" +
        "      document.getElementById('stHoverBar').innerHTML = '<span style=\"overflow:hidden;text-overflow:ellipsis;white-space:nowrap\"><strong>' + esc(node.name) + '</strong> &middot; ' + fmtBytes(node.size) + '</span><strong>' + pct + '%</strong>';\n" +
        "    };\n" +
        "    el.onclick = () => {\n" +
        "      if (node.isDir) focusStorageNode(node);\n" +
        "      else toggleStorageCollect(node);\n" +
        "    };\n" +
        "  });\n" +
        "}\n" +
        "\n" +
        "function renderStorageBreakdownList() {\n" +
        "  const wrap = document.getElementById('stListWrap');\n" +
        "  if (!wrap || !stFocusNode) return;\n" +
        "  const list = stFocusNode.children || [];\n" +
        "  if (list.length === 0 && !(stFocusNode.otherCount > 0)) {\n" +
        "    wrap.innerHTML = '<div class=\"empty\">Esta carpeta está vacía.</div>';\n" +
        "    return;\n" +
        "  }\n" +
        "  const total = Math.max(1, stFocusNode.size || 0);\n" +
        "  const rowsEl = document.createElement('div');\n" +
        "  list.forEach(ch => {\n" +
        "    const pct = ((ch.size || 0) / total) * 100;\n" +
        "    const col = stPaletteColor(ch.colorIndex);\n" +
        "    const marked = ch.path && stCollector.has(ch.path);\n" +
        "    const row = document.createElement('div');\n" +
        "    row.className = 'st-row' + (marked ? ' marked' : '');\n" +
        "    const sub = ch.isDir\n" +
        "      ? ((ch.dirCount || 0) + ' carpetas &middot; ' + (ch.fileCount || 0) + ' archivos &middot; ' + pct.toFixed(1) + '%')\n" +
        "      : ('Archivo &middot; ' + pct.toFixed(1) + '%');\n" +
        "    row.innerHTML =\n" +
        "      '<span class=\"st-dot\" style=\"background:' + col + '\"></span>' +\n" +
        "      '<div class=\"st-row-main\">' +\n" +
        "        '<div class=\"st-row-name\">' + (ch.isDir ? '&#128193; ' : '') + esc(ch.name) + '</div>' +\n" +
        "        '<div class=\"st-row-sub\">' + sub + '</div>' +\n" +
        "        '<div class=\"st-mini-bar\"><div class=\"st-mini-fill\" style=\"width:' + Math.min(100, pct).toFixed(1) + '%;background:' + col + '\"></div></div>' +\n" +
        "      '</div>' +\n" +
        "      '<div class=\"st-row-size\">' + fmtBytes(ch.size || 0) + '</div>' +\n" +
        "      '<div class=\"st-row-acts\">' +\n" +
        "        '<button class=\"st-ico-btn' + (marked ? ' collected' : '') + '\" title=\"Marcar para liberar espacio\">' + (marked ? '&#10003;' : '&#128465;') + '</button>' +\n" +
        "      '</div>';\n" +
        "    row.querySelector('.st-row-main').onclick = () => {\n" +
        "      if (ch.isDir) focusStorageNode(ch);\n" +
        "      else toggleStorageCollect(ch);\n" +
        "    };\n" +
        "    row.querySelector('.st-ico-btn').onclick = e => { e.stopPropagation(); toggleStorageCollect(ch); };\n" +
        "    rowsEl.appendChild(row);\n" +
        "  });\n" +
        "  wrap.innerHTML = '';\n" +
        "  wrap.appendChild(rowsEl);\n" +
        "  if (stFocusNode.otherCount > 0) {\n" +
        "    const tail = document.createElement('div');\n" +
        "    tail.className = 'empty';\n" +
        "    tail.textContent = '+ ' + stFocusNode.otherCount + ' elementos pequeños (' + fmtBytes(stFocusNode.otherSize || 0) + ')';\n" +
        "    wrap.appendChild(tail);\n" +
        "  }\n" +
        "}\n";
}
