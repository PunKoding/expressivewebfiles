package com.fotospixel.transfer;

public final class WebUi {

    private WebUi() {
    }

    private static final String SVG_BRAND = "<svg class=\"ico\" viewBox=\"0 0 960 960\"><path d=\"M264,664 V320 L204,380 Q192,392 175,392 Q158,392 146,380 Q134,368 134,351 Q134,334 146,322 L272,196 Q284,184 300,184 Q316,184 328,196 L454,322 Q466,334 466,351 Q466,368 454,380 Q442,392 425,392 Q408,392 396,380 L336,320 V664 Q336,679 325,690 Q315,700 300,700 Q285,700 275,690 Q264,679 264,664 Z M624,296 V640 L564,580 Q552,568 535,568 Q518,568 506,580 Q494,592 494,609 Q494,626 506,638 L632,764 Q644,776 660,776 Q676,776 688,764 L814,638 Q826,626 826,609 Q826,592 814,580 Q802,568 785,568 Q768,568 756,580 L696,640 V296 Q696,281 685,270 Q675,260 660,260 Q645,260 635,270 Q624,281 624,296 Z\"/></svg>";
    private static final String FAVICON_URI = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 108 108'%3E%3Crect width='108' height='108' rx='36' fill='%23F6F1FF'/%3E%3Cpath fill='%23D9CEFF' d='M54,19 C62,19 68,12 78,12 C89,12 96,19 96,30 C96,40 89,46 89,54 C89,62 96,68 96,78 C96,89 89,96 78,96 C68,96 62,89 54,89 C46,89 40,96 30,96 C19,96 12,89 12,78 C12,68 19,62 19,54 C19,46 12,40 12,30 C12,19 19,12 30,12 C40,12 46,19 54,19 Z'/%3E%3Cpath fill='none' stroke='%233A2585' stroke-width='7' stroke-linecap='round' stroke-linejoin='round' d='M41,70 V34 M30,45 L41,34 L52,45'/%3E%3Cpath fill='none' stroke='%239B3460' stroke-width='7' stroke-linecap='round' stroke-linejoin='round' d='M67,38 V74 M56,63 L67,74 L78,63'/%3E%3C/svg%3E";
    static final String SVG_PHOTO = "<svg class=\"ico\" viewBox=\"0 0 960 960\"><path d=\"M200,840 Q167,840 144,816 Q120,793 120,760 V200 Q120,167 144,144 Q167,120 200,120 H760 Q793,120 816,144 Q840,167 840,200 V760 Q840,793 816,816 Q793,840 760,840 H200 Z M200,760 H760 V200 H200 V760 Z M260,680 H700 L562,496 L450,640 L370,536 L260,680 Z M310,360 A50,50 0 1,0 410,360 A50,50 0 1,0 310,360 Z\"/></svg>";
    static final String SVG_UPLOAD = "<svg class=\"ico\" viewBox=\"0 0 960 960\"><path d=\"M440,640 V314 L314,440 Q302,452 285,452 Q268,452 256,440 Q244,428 244,411 Q244,394 256,382 L452,186 Q464,174 480,174 Q496,174 508,186 L704,382 Q716,394 716,411 Q716,428 704,440 Q692,452 675,452 Q658,452 646,440 L520,314 V640 Q520,657 508,668 Q497,680 480,680 Q463,680 452,668 Q440,657 440,640 Z M240,800 Q207,800 184,776 Q160,753 160,720 V640 Q160,623 172,612 Q183,600 200,600 Q217,600 228,612 Q240,623 240,640 V720 H720 V640 Q720,623 732,612 Q743,600 760,600 Q777,600 788,612 Q800,623 800,640 V720 Q800,753 776,776 Q753,800 720,800 H240 Z\"/></svg>";
    static final String SVG_DOWNLOAD = "<svg class=\"ico\" viewBox=\"0 0 960 960\"><path d=\"M480,640 Q464,640 452,628 L256,432 Q244,420 244,403 Q244,386 256,374 Q268,362 285,362 Q302,362 314,374 L440,500 V174 Q440,157 452,146 Q463,134 480,134 Q497,134 508,146 Q520,157 520,174 V500 L646,374 Q658,362 675,362 Q692,362 704,374 Q716,386 716,403 Q716,420 704,432 L508,628 Q496,640 480,640 Z M240,800 Q207,800 184,776 Q160,753 160,720 V640 Q160,623 172,612 Q183,600 200,600 Q217,600 228,612 Q240,623 240,640 V720 H720 V640 Q720,623 732,612 Q743,600 760,600 Q777,600 788,612 Q800,623 800,640 V720 Q800,753 776,776 Q753,800 720,800 H240 Z\"/></svg>";
    static final String SVG_FOLDER = "<svg class=\"ico\" viewBox=\"0 0 960 960\"><path d=\"M160,800 Q127,800 104,776 Q80,753 80,720 V240 Q80,207 104,184 Q127,160 160,160 H367 Q383,160 398,166 Q413,172 424,183 L480,240 H800 Q833,240 856,264 Q880,287 880,320 V720 Q880,753 856,776 Q833,800 800,800 H160 Z M160,320 V720 H800 V320 H447 L367,240 H160 V320 Z\"/></svg>";
    static final String SVG_STOP = "<svg class=\"ico\" viewBox=\"0 0 960 960\"><path fill-rule=\"evenodd\" d=\"M480,880 Q397,880 324,849 Q251,817 197,763 Q143,709 111,636 Q80,563 80,480 Q80,397 111,324 Q143,251 197,197 Q251,143 324,111 Q397,80 480,80 Q563,80 636,111 Q709,143 763,197 Q817,251 849,324 Q880,397 880,480 Q880,563 849,636 Q817,709 763,763 Q709,817 636,849 Q563,880 480,880 Z M480,800 Q614,800 707,707 Q800,614 800,480 Q800,346 707,253 Q614,160 480,160 Q346,160 253,253 Q160,346 160,480 Q160,614 253,707 Q346,800 480,800 Z M360,320 H600 Q640,320 640,360 V600 Q640,640 600,640 H360 Q320,640 320,600 V360 Q320,320 360,320 Z\"/></svg>";
    private static final String SVG_DNS = "<svg class=\"ico\" viewBox=\"0 0 960 960\"><path fill-rule=\"evenodd\" d=\"M200,140 H760 Q800,140 820,160 Q840,180 840,220 V360 Q840,400 820,420 Q800,440 760,440 H200 Q160,440 140,420 Q120,400 120,360 V220 Q120,180 140,160 Q160,140 200,140 Z M200,220 V360 H760 V220 H200 Z M260,290 A40,40 0 1,0 340,290 A40,40 0 1,0 260,290 Z M200,520 H760 Q800,520 820,540 Q840,560 840,600 V740 Q840,780 820,800 Q800,820 760,820 H200 Q160,820 140,800 Q120,780 120,740 V600 Q120,560 140,540 Q160,520 200,520 Z M200,600 V740 H760 V600 H200 Z M260,670 A40,40 0 1,0 340,670 A40,40 0 1,0 260,670 Z\"/></svg>";
    static final String SVG_TUNE = "<svg class=\"ico\" viewBox=\"0 0 960 960\"><path d=\"M440,840 V600 H520 V680 H840 V760 H520 V840 H440 Z M120,760 V680 H360 V760 H120 Z M280,600 V520 H120 V440 H280 V360 H360 V600 H280 Z M440,520 V440 H840 V520 H440 Z M600,360 V120 H680 V200 H840 V280 H680 V360 H600 Z M120,280 V200 H520 V280 H120 Z\"/></svg>";
    static final String SVG_REFRESH = "<svg class=\"ico\" viewBox=\"0 0 960 960\"><path d=\"M480,800 Q346,800 253,707 Q160,614 160,480 Q160,346 253,253 Q346,160 480,160 Q549,160 611,189 Q673,217 720,268 V200 Q720,183 732,172 Q743,160 760,160 Q777,160 788,172 Q800,183 800,200 V400 Q800,417 788,428 Q777,440 760,440 H560 Q543,440 532,428 Q520,417 520,400 Q520,383 532,372 Q543,360 560,360 H686 Q646,304 590,272 Q534,240 480,240 Q380,240 310,310 Q240,380 240,480 Q240,580 310,650 Q380,720 480,720 Q560,720 624,673 Q688,626 710,552 Q715,536 729,528 Q743,520 759,524 Q775,529 784,543 Q792,557 788,574 Q759,676 673,738 Q587,800 480,800 Z\"/></svg>";
    private static final String SVG_KEY = "<svg class=\"ico\" viewBox=\"0 0 960 960\"><path fill-rule=\"evenodd\" d=\"M280,720 Q180,720 110,650 Q40,580 40,480 Q40,380 110,310 Q180,240 280,240 Q346,240 403,274 Q460,308 492,364 Q498,374 504,387 Q510,400 513,400 H840 Q873,400 896,424 Q920,447 920,480 V560 Q920,577 908,588 Q897,600 880,600 H840 V640 Q840,657 828,668 Q817,680 800,680 H720 Q703,680 692,668 Q680,657 680,640 V560 H513 Q482,633 419,676 Q356,720 280,720 Z M280,640 Q357,640 403,581 Q450,522 440,480 H840 V480 H440 Q426,423 383,372 Q340,320 280,320 Q214,320 167,367 Q120,414 120,480 Q120,546 167,593 Q214,640 280,640 Z M220,480 A60,60 0 1,0 340,480 A60,60 0 1,0 220,480 Z\"/></svg>";
    static final String SVG_STORAGE = "<svg class=\"ico\" viewBox=\"0 0 960 960\"><path fill-rule=\"evenodd\" d=\"M200,800 Q150,800 115,765 Q80,730 80,680 V520 Q80,502 88,484 L182,250 Q196,214 226,187 Q256,160 295,160 H665 Q704,160 734,187 Q764,214 778,250 L872,484 Q880,502 880,520 V680 Q880,730 845,765 Q810,800 760,800 H200 Z M245,280 L176,450 H784 L715,280 Q707,260 691,250 Q675,240 665,240 H295 Q285,240 269,250 Q253,260 245,280 Z M160,520 V680 Q160,697 172,708 Q183,720 200,720 H760 Q777,720 788,708 Q800,697 800,680 V520 H160 Z M620,620 A45,45 0 1,0 710,620 A45,45 0 1,0 620,620 Z\"/></svg>";
    static final String SVG_ZIP = "<svg class=\"ico\" viewBox=\"0 0 960 960\"><path d=\"M160,800 Q127,800 104,776 Q80,753 80,720 V240 Q80,207 104,184 Q127,160 160,160 H367 Q383,160 398,166 Q413,172 424,183 L480,240 H800 Q833,240 856,264 Q880,287 880,320 V720 Q880,753 856,776 Q833,800 800,800 H160 Z M160,320 V720 H800 V320 H447 L367,240 H160 V320 Z M560,340 H640 V420 H560 Z M640,420 H720 V500 H640 Z M560,500 H640 V580 H560 Z M640,580 H720 V660 H640 Z M560,660 H640 V720 H560 Z\"/></svg>";
    static final String SVG_SEARCH = "<svg class=\"ico\" viewBox=\"0 0 960 960\"><path d=\"M772,828 L518,577 Q486,598 450,609 Q414,620 380,620 Q280,620 210,550 Q140,480 140,380 Q140,280 210,210 Q280,140 380,140 Q480,140 550,210 Q620,280 620,380 Q620,414 609,450 Q598,486 577,518 L828,772 Q854,798 828,828 Q798,854 772,828 Z M380,540 Q447,540 493,493 Q540,447 540,380 Q540,313 493,267 Q447,220 380,220 Q313,220 267,267 Q220,313 220,380 Q220,447 267,493 Q313,540 380,540 Z\"/></svg>";
    static final String SVG_ADD = "<svg class=\"ico\" viewBox=\"0 0 960 960\"><path d=\"M440,520 H240 Q223,520 212,508 Q200,497 200,480 Q200,463 212,452 Q223,440 240,440 H440 V240 Q440,223 452,212 Q463,200 480,200 Q497,200 508,212 Q520,223 520,240 V440 H720 Q737,440 748,452 Q760,463 760,480 Q760,497 748,508 Q737,520 720,520 H520 V720 Q520,737 508,748 Q497,760 480,760 Q463,760 452,748 Q440,737 440,720 V520 Z\"/></svg>";
    static final String SVG_MENU = "<svg class=\"ico\" viewBox=\"0 0 960 960\"><path d=\"M160,720 Q143,720 132,708 Q120,697 120,680 Q120,663 132,652 Q143,640 160,640 H800 Q817,640 828,652 Q840,663 840,680 Q840,697 828,708 Q817,720 800,720 H160 Z M160,520 Q143,520 132,508 Q120,497 120,480 Q120,463 132,452 Q143,440 160,440 H800 Q817,440 828,452 Q840,463 840,480 Q840,497 828,508 Q817,520 800,520 H160 Z M160,320 Q143,320 132,308 Q120,297 120,280 Q120,263 132,252 Q143,240 160,240 H800 Q817,240 828,252 Q840,263 840,280 Q840,297 828,308 Q817,320 800,320 H160 Z\"/></svg>";
    static final String SVG_MORE = "<svg class=\"ico\" viewBox=\"0 0 960 960\"><path d=\"M480,320 Q447,320 423,297 Q400,273 400,240 Q400,207 423,183 Q447,160 480,160 Q513,160 537,183 Q560,207 560,240 Q560,273 537,297 Q513,320 480,320 Z M480,560 Q447,560 423,537 Q400,513 400,480 Q400,447 423,423 Q447,400 480,400 Q513,400 537,423 Q560,447 560,480 Q560,513 537,537 Q513,560 480,560 Z M480,800 Q447,800 423,777 Q400,753 400,720 Q400,687 423,663 Q447,640 480,640 Q513,640 537,663 Q560,687 560,720 Q560,753 537,777 Q513,800 480,800 Z\"/></svg>";
    static final String SVG_CHECK = "<svg class=\"ico\" viewBox=\"0 0 960 960\"><path d=\"M382,684 Q366,684 352,672 L182,502 Q170,490 170,474 Q170,458 182,446 Q194,434 210,434 Q226,434 238,446 L382,590 L722,250 Q734,238 750,238 Q766,238 778,250 Q790,262 790,278 Q790,294 778,306 L412,672 Q398,684 382,684 Z\"/></svg>";

    private static final String HTML_MARKUP =
        "<!DOCTYPE html>\n" +
        "<html lang=\"es\">\n" +
        "<head>\n" +
        "<meta charset=\"UTF-8\">\n" +
        "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
        "<meta name=\"theme-color\" id=\"themeMeta\" content=\"#111318\">\n" +
        "<link rel=\"icon\" type=\"image/svg+xml\" href=\"" + FAVICON_URI + "\">\n" +
        "<title>Expressive Web Files</title>\n" +
        "<style>\n" +
        "  :root {\n" +
        "    --md-sys-color-surface: #111318;\n" +
        "    --md-sys-color-surface-dim: #111318;\n" +
        "    --md-sys-color-surface-bright: #37393E;\n" +
        "    --md-sys-color-surface-container-lowest: #0C0E13;\n" +
        "    --md-sys-color-surface-container-low: #191C20;\n" +
        "    --md-sys-color-surface-container: #1D2024;\n" +
        "    --md-sys-color-surface-container-high: #282A2F;\n" +
        "    --md-sys-color-surface-container-highest: #33353A;\n" +
        "    --md-sys-color-primary: #ADC6FF;\n" +
        "    --md-sys-color-on-primary: #102F60;\n" +
        "    --md-sys-color-primary-container: #2B4678;\n" +
        "    --md-sys-color-on-primary-container: #D8E2FF;\n" +
        "    --md-sys-color-secondary: #BFC6DC;\n" +
        "    --md-sys-color-on-secondary: #293041;\n" +
        "    --md-sys-color-secondary-container: #3F4759;\n" +
        "    --md-sys-color-on-secondary-container: #DBE2F9;\n" +
        "    --md-sys-color-tertiary: #DEBCDF;\n" +
        "    --md-sys-color-on-tertiary: #402843;\n" +
        "    --md-sys-color-tertiary-container: #583E5B;\n" +
        "    --md-sys-color-on-tertiary-container: #FBD7FC;\n" +
        "    --md-sys-color-on-surface: #E2E2E9;\n" +
        "    --md-sys-color-on-surface-variant: #C4C6D0;\n" +
        "    --md-sys-color-outline: #8E9099;\n" +
        "    --md-sys-color-outline-variant: #44474F;\n" +
        "    --md-sys-color-error: #FFB4AB;\n" +
        "    --md-sys-color-on-error: #690005;\n" +
        "    --md-sys-color-error-container: #93000A;\n" +
        "    --md-sys-color-on-error-container: #FFDAD6;\n" +
        "    --ui-font: 'Google Sans Flex', 'Google Sans', 'Plus Jakarta Sans', 'Inter', system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif;\n" +
        "    --ui-radius-card: 28px;\n" +
        "    --ui-radius-hero: 32px;\n" +
        "    --ui-radius-btn: 999px;\n" +
        "    --ui-card-border: none;\n" +
        "    --ui-card-shadow: none;\n" +
        "    --ui-nav-shadow: 0 14px 36px rgba(0,0,0,0.48);\n" +
        "    --ui-ink: rgba(255,255,255,0.28);\n" +
        "    --ui-btn-transform: none;\n" +
        "    --ui-hero-bg: var(--md-sys-color-surface-container);\n" +
        "  }\n" +
        "  * { box-sizing: border-box; -webkit-tap-highlight-color: transparent; }\n" +
        "  body {\n" +
        "    margin: 0;\n" +
        "    font-family: var(--ui-font);\n" +
        "    background: var(--md-sys-color-surface);\n" +
        "    color: var(--md-sys-color-on-surface);\n" +
        "    line-height: 1.48;\n" +
        "    transition: background-color 0.35s ease, color 0.35s ease;\n" +
        "  }\n" +
        "  .ico { width: 18px; height: 18px; fill: currentColor; vertical-align: -3px; flex-shrink: 0; }\n" +
        "  .ico-lg { width: 24px; height: 24px; }\n" +
        "  .wrap { max-width: 980px; margin: 0 auto; padding: 24px 18px 128px; }\n" +
        "  .header {\n" +
        "    display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between;\n" +
        "    gap: 14px; margin-bottom: 20px;\n" +
        "  }\n" +
        "  .brand { display: flex; align-items: center; gap: 14px; }\n" +
        "  .logo {\n" +
        "    width: 48px; height: 48px; border-radius: 20px;\n" +
        "    background: var(--md-sys-color-primary-container);\n" +
        "    color: var(--md-sys-color-on-primary-container);\n" +
        "    display: flex; align-items: center; justify-content: center;\n" +
        "    transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);\n" +
        "  }\n" +
        "  .logo:hover { transform: scale(1.06) rotate(-3deg); }\n" +
        "  h1 {\n" +
        "    margin: 0; font-size: clamp(1.35rem, 2.5vw, 1.9rem); font-weight: 700;\n" +
        "    letter-spacing: -0.02em; color: var(--md-sys-color-on-surface); line-height: 1.15;\n" +
        "  }\n" +
        "  .sub { color: var(--md-sys-color-on-surface-variant); font-size: 13px; }\n" +
        "  .pill {\n" +
        "    display: inline-flex; align-items: center; gap: 8px; padding: 9px 16px;\n" +
        "    border-radius: 999px; background: var(--md-sys-color-secondary-container);\n" +
        "    color: var(--md-sys-color-on-secondary-container);\n" +
        "    font-size: 13px; font-weight: 700;\n" +
        "  }\n" +
        "  .pill.offline {\n" +
        "    background: var(--md-sys-color-error-container);\n" +
        "    color: var(--md-sys-color-on-error-container);\n" +
        "  }\n" +
        "  .dot {\n" +
        "    width: 9px; height: 9px; border-radius: 50%; background: var(--md-sys-color-primary);\n" +
        "    animation: pulse 1.5s ease-in-out infinite;\n" +
        "  }\n" +
        "  .pill.offline .dot { background: var(--md-sys-color-error); }\n" +
        "  @keyframes pulse {\n" +
        "    0%, 100% { transform: scale(1); opacity: 1; }\n" +
        "    50% { transform: scale(1.42); opacity: 0.45; }\n" +
        "  }\n" +
        "  @keyframes fadeUp {\n" +
        "    from { opacity: 0; transform: translateY(14px) scale(0.985); }\n" +
        "    to { opacity: 1; transform: translateY(0) scale(1); }\n" +
        "  }\n" +
        "  @keyframes txEnter {\n" +
        "    from { opacity: 0; transform: translateY(14px) scale(0.96); }\n" +
        "    to { opacity: 1; transform: translateY(0) scale(1); }\n" +
        "  }\n" +
        "  @keyframes shimmer {\n" +
        "    0% { background-position: 200% 0; }\n" +
        "    100% { background-position: -200% 0; }\n" +
        "  }\n" +
        "  .m3-ink {\n" +
        "    position: absolute; border-radius: 50%; pointer-events: none;\n" +
        "    background: radial-gradient(circle, var(--ui-ink) 0%, transparent 72%);\n" +
        "    transform: scale(0); animation: inkPop 0.55s cubic-bezier(0.22, 1, 0.36, 1) forwards;\n" +
        "  }\n" +
        "  @keyframes inkPop {\n" +
        "    to { transform: scale(2.8); opacity: 0; }\n" +
        "  }\n" +
        "  .card {\n" +
        "    background: var(--md-sys-color-surface-container-low);\n" +
        "    border-radius: var(--ui-radius-card); padding: 22px; margin-bottom: 16px;\n" +
        "    border: var(--ui-card-border); box-shadow: var(--ui-card-shadow);\n" +
        "    animation: fadeUp 0.32s cubic-bezier(0.22, 1, 0.36, 1);\n" +
        "    transition: background-color 0.3s ease;\n" +
        "  }\n" +
        "  .card-hero {\n" +
        "    background: var(--ui-hero-bg);\n" +
        "    border-radius: var(--ui-radius-hero);\n" +
        "  }\n" +
        "  .card-title {\n" +
        "    font-size: 17px; font-weight: 700; letter-spacing: -0.01em;\n" +
        "    color: var(--md-sys-color-on-surface); margin: 0 0 14px;\n" +
        "    display: flex; align-items: center; justify-content: space-between; gap: 10px; flex-wrap: wrap;\n" +
        "  }\n" +
        "  .title-ico { display: inline-flex; align-items: center; gap: 9px; }\n" +
        "  .title-ico .ico { color: var(--md-sys-color-primary); width: 20px; height: 20px; }\n" +
        "  .tonal-pill {\n" +
        "    display: inline-flex; align-items: center; padding: 5px 12px; border-radius: 999px;\n" +
        "    background: var(--md-sys-color-secondary-container);\n" +
        "    color: var(--md-sys-color-on-secondary-container);\n" +
        "    font-size: 12px; font-weight: 700;\n" +
        "  }\n" +
        "  .speed-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 12px; margin-bottom: 14px; }\n" +
        "  .speed-box {\n" +
        "    border-radius: 24px; padding: 18px 20px;\n" +
        "    transition: transform 0.28s cubic-bezier(0.34, 1.56, 0.64, 1), background-color 0.3s ease;\n" +
        "  }\n" +
        "  .speed-box:hover { transform: translateY(-2px); }\n" +
        "  .speed-box-up {\n" +
        "    background: var(--md-sys-color-primary-container);\n" +
        "    color: var(--md-sys-color-on-primary-container);\n" +
        "  }\n" +
        "  .speed-box-down {\n" +
        "    background: var(--md-sys-color-tertiary-container);\n" +
        "    color: var(--md-sys-color-on-tertiary-container);\n" +
        "  }\n" +
        "  .speed-label { font-size: 13px; font-weight: 700; opacity: 0.9; display: flex; align-items: center; gap: 6px; }\n" +
        "  .speed-val { font-size: clamp(1.7rem, 3vw, 2.05rem); font-weight: 700; letter-spacing: -0.02em; margin: 6px 0 2px; font-variant-numeric: tabular-nums; }\n" +
        "  .speed-total { font-size: 12px; opacity: 0.82; font-variant-numeric: tabular-nums; }\n" +
        "  canvas {\n" +
        "    width: 100%; height: 108px; display: block; border-radius: 22px;\n" +
        "    background: var(--md-sys-color-surface-container-lowest);\n" +
        "  }\n" +
        "  .btn, .chip, .nav-pill {\n" +
        "    position: relative; overflow: hidden; border: none;\n" +
        "    transition: transform 0.26s cubic-bezier(0.34, 1.56, 0.64, 1), filter 0.18s, background-color 0.2s;\n" +
        "  }\n" +
        "  .btn {\n" +
        "    display: inline-flex; align-items: center; justify-content: center; gap: 8px;\n" +
        "    background: var(--md-sys-color-surface-container-high); color: var(--md-sys-color-on-surface);\n" +
        "    border-radius: var(--ui-radius-btn); padding: 11px 20px; font-size: 14px; font-weight: 700; cursor: pointer;\n" +
        "    text-decoration: none; text-transform: var(--ui-btn-transform);\n" +
        "  }\n" +
        "  .btn:hover { filter: brightness(1.08); transform: translateY(-1px); }\n" +
        "  .btn:active, .chip:active, .nav-pill:active { transform: scale(0.93); }\n" +
        "  .btn-primary { background: var(--md-sys-color-primary); color: var(--md-sys-color-on-primary); }\n" +
        "  .btn-tonal { background: var(--md-sys-color-secondary-container); color: var(--md-sys-color-on-secondary-container); }\n" +
        "  .btn-danger { background: var(--md-sys-color-error-container); color: var(--md-sys-color-on-error-container); }\n" +
        "  .btn-sm { padding: 7px 14px; font-size: 12px; }\n" +
        "  .storage-bar-bg { height: 10px; background: var(--md-sys-color-surface-container-highest); border-radius: 999px; overflow: hidden; margin-top: 10px; }\n" +
        "  .storage-bar-fill { height: 100%; background: var(--md-sys-color-primary); width: 0%; transition: width 0.4s; border-radius: 999px; }\n" +
        "  .tx-item {\n" +
        "    position: relative; overflow: hidden;\n" +
        "    background: var(--md-sys-color-surface-container-high); border-radius: 22px;\n" +
        "    padding: 15px 17px; margin-bottom: 10px;\n" +
        "    animation: txEnter 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);\n" +
        "  }\n" +
        "  .tx-top { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 8px; margin-bottom: 8px; }\n" +
        "  .tx-name { font-weight: 700; font-size: 14px; word-break: break-all; color: var(--md-sys-color-on-surface); }\n" +
        "  .badge {\n" +
        "    display: inline-flex; align-items: center; gap: 5px; padding: 4px 11px; border-radius: 999px; font-size: 11px; font-weight: 700;\n" +
        "  }\n" +
        "  .b-up { background: var(--md-sys-color-primary-container); color: var(--md-sys-color-on-primary-container); }\n" +
        "  .b-down { background: var(--md-sys-color-tertiary-container); color: var(--md-sys-color-on-tertiary-container); }\n" +
        "  .b-folder { background: var(--md-sys-color-surface-container-highest); color: var(--md-sys-color-on-surface-variant); font-family: ui-monospace, monospace; }\n" +
        "  .b-cancel { background: var(--md-sys-color-error-container); color: var(--md-sys-color-on-error-container); }\n" +
        "  .prog-bg { height: 8px; background: var(--md-sys-color-surface-container-lowest); border-radius: 999px; overflow: hidden; margin: 8px 0; }\n" +
        "  .prog-fill {\n" +
        "    height: 100%; border-radius: 999px; background-size: 200% 100%;\n" +
        "    transition: width 0.35s cubic-bezier(0.22, 1, 0.36, 1);\n" +
        "    animation: shimmer 1.8s linear infinite;\n" +
        "  }\n" +
        "  .prog-up { background: var(--md-sys-color-primary); }\n" +
        "  .prog-down { background: var(--md-sys-color-tertiary); }\n" +
        "  .tx-meta { display: flex; justify-content: space-between; font-size: 12px; color: var(--md-sys-color-on-surface-variant); font-variant-numeric: tabular-nums; }\n" +
        "  .batch-banner {\n" +
        "    background: var(--md-sys-color-secondary-container); color: var(--md-sys-color-on-secondary-container);\n" +
        "    border-radius: 20px; padding: 14px 16px; margin-bottom: 12px;\n" +
        "    animation: txEnter 0.28s cubic-bezier(0.34, 1.56, 0.64, 1);\n" +
        "  }\n" +
        "  .batch-top { display: flex; flex-wrap: wrap; justify-content: space-between; gap: 8px; font-size: 13px; font-weight: 700; }\n" +
        "  .tx-speed-up { color: var(--md-sys-color-primary); }\n" +
        "  .tx-speed-down { color: var(--md-sys-color-tertiary); }\n" +
        "  .dropzone {\n" +
        "    position: relative; overflow: hidden;\n" +
        "    border: 2px dashed var(--md-sys-color-outline-variant); border-radius: 26px; padding: 30px 20px; text-align: center;\n" +
        "    background: var(--md-sys-color-surface-container-high);\n" +
        "    transition: border-color 0.2s, background-color 0.2s, transform 0.22s;\n" +
        "  }\n" +
        "  .dropzone.over {\n" +
        "    border-color: var(--md-sys-color-primary);\n" +
        "    background: var(--md-sys-color-primary-container);\n" +
        "    transform: scale(1.01);\n" +
        "  }\n" +
        "  .controls { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; margin-bottom: 14px; }\n" +
        "  select, input[type=text], input[type=password] {\n" +
        "    background: var(--md-sys-color-surface-container-high); color: var(--md-sys-color-on-surface);\n" +
        "    border: 1px solid var(--md-sys-color-outline-variant);\n" +
        "    border-radius: 16px; padding: 12px 15px; font-size: 14px; outline: none;\n" +
        "    transition: border-color 0.2s, background-color 0.2s;\n" +
        "  }\n" +
        "  select:focus, input:focus { border-color: var(--md-sys-color-primary); }\n" +
        "  .chips { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 14px; }\n" +
        "  .chip {\n" +
        "    display: inline-flex; align-items: center; gap: 6px;\n" +
        "    background: var(--md-sys-color-surface-container-high); color: var(--md-sys-color-on-surface);\n" +
        "    padding: 8px 15px; border-radius: 999px; font-size: 13px; font-weight: 700; cursor: pointer;\n" +
        "  }\n" +
        "  .chip.active, .chip:hover {\n" +
        "    background: var(--md-sys-color-secondary-container);\n" +
        "    color: var(--md-sys-color-on-secondary-container);\n" +
        "  }\n" +
        "  #fileTableWrap, #recentList { overflow-x: auto; }\n" +
        "  table { width: 100%; border-collapse: separate; border-spacing: 0 6px; font-size: 13px; }\n" +
        "  th { text-align: left; padding: 6px 12px; color: var(--md-sys-color-on-surface-variant); font-weight: 700; font-size: 12px; }\n" +
        "  td { padding: 11px 12px; background: var(--md-sys-color-surface-container-high); }\n" +
        "  tr td:first-child { border-radius: 16px 0 0 16px; }\n" +
        "  tr td:last-child { border-radius: 0 16px 16px 0; }\n" +
        "  tr td:only-child { border-radius: 16px; }\n" +
        "  .file-row { cursor: pointer; }\n" +
        "  .file-row:hover td { filter: brightness(1.07); }\n" +
        "  .thumb { width: 42px; height: 42px; object-fit: cover; border-radius: 12px; vertical-align: middle; margin-right: 12px; background: var(--md-sys-color-surface-container-lowest); }\n" +
        "  .exp-bar { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; margin-bottom: 12px; }\n" +
        "  .exp-bar input[type=text] { flex: 1 1 200px; min-width: 160px; padding: 10px 14px; }\n" +
        "  .exp-bar select { padding: 10px 14px; }\n" +
        "  .breadcrumbs { display: flex; flex-wrap: wrap; gap: 6px; align-items: center; margin-bottom: 12px; }\n" +
        "  .crumb {\n" +
        "    background: var(--md-sys-color-surface-container-high); color: var(--md-sys-color-on-surface);\n" +
        "    padding: 6px 12px; border-radius: 999px; font-size: 12px; font-weight: 700; cursor: pointer;\n" +
        "  }\n" +
        "  .crumb.cur { background: var(--md-sys-color-primary-container); color: var(--md-sys-color-on-primary-container); }\n" +
        "  .exp-stats { display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 8px; margin-bottom: 10px; font-size: 12px; color: var(--md-sys-color-on-surface-variant); }\n" +
        "  .exp-notice { background: var(--md-sys-color-primary-container); color: var(--md-sys-color-on-primary-container); border-radius: 16px; padding: 10px 14px; font-size: 13px; font-weight: 700; margin-bottom: 12px; }\n" +
        "  .file-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(215px, 1fr)); gap: 12px; }\n" +
        "  .grid-card {\n" +
        "    background: var(--md-sys-color-surface-container-high); border-radius: 22px; padding: 14px;\n" +
        "    display: flex; flex-direction: column; gap: 8px; transition: transform 0.2s;\n" +
        "  }\n" +
        "  .grid-card:hover { transform: translateY(-2px); }\n" +
        "  .grid-preview { width: 100%; height: 118px; border-radius: 16px; background: var(--md-sys-color-surface-container-lowest); display: flex; align-items: center; justify-content: center; overflow: hidden; cursor: pointer; }\n" +
        "  .grid-preview img { width: 100%; height: 100%; object-fit: cover; }\n" +
        "  .grid-name { font-weight: 700; font-size: 13px; word-break: break-word; color: var(--md-sys-color-on-surface); }\n" +
        "  .grid-meta { font-size: 11px; color: var(--md-sys-color-on-surface-variant); }\n" +
        "  .grid-actions { display: flex; flex-wrap: wrap; gap: 6px; margin-top: auto; }\n" +
        "  .empty { color: var(--md-sys-color-on-surface-variant); font-size: 13px; text-align: center; padding: 14px; }\n" +
        "  .floating-nav {\n" +
        "    position: fixed; bottom: 18px; left: 50%; transform: translateX(-50%); z-index: 40;\n" +
        "    display: flex; gap: 6px; padding: 7px; border-radius: 999px;\n" +
        "    background: var(--md-sys-color-surface-container-highest);\n" +
        "    border: 1px solid var(--md-sys-color-outline-variant);\n" +
        "    border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 45%, transparent);\n" +
        "    box-shadow: var(--ui-nav-shadow);\n" +
        "  }\n" +
        "  .nav-pill {\n" +
        "    display: inline-flex; align-items: center; gap: 7px;\n" +
        "    background: transparent; color: var(--md-sys-color-on-surface-variant); padding: 10px 18px;\n" +
        "    border-radius: 999px; font-size: 13px; font-weight: 700; cursor: pointer;\n" +
        "  }\n" +
        "  .nav-pill.active {\n" +
        "    background: var(--md-sys-color-primary-container); color: var(--md-sys-color-on-primary-container);\n" +
        "  }\n" +
        WebUiExplorer.EXTRA_CSS +
        WebUiStorage.STORAGE_CSS +
        "  .login-wrap {\n" +
        "    min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 20px;\n" +
        "  }\n" +
        "  .login-card {\n" +
        "    width: 100%; max-width: 410px; background: var(--md-sys-color-surface-container-low);\n" +
        "    border-radius: 32px; padding: 30px 26px; box-shadow: 0 22px 50px rgba(0,0,0,0.42);\n" +
        "    animation: fadeUp 0.35s cubic-bezier(0.22, 1, 0.36, 1);\n" +
        "  }\n" +
        "  .field { display: flex; flex-direction: column; gap: 6px; margin-bottom: 14px; }\n" +
        "  .field label { font-size: 12px; color: var(--md-sys-color-on-surface-variant); font-weight: 700; padding-left: 4px; }\n" +
        "  .modal {\n" +
        "    display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.88); z-index: 60;\n" +
        "    align-items: center; justify-content: center; padding: 20px;\n" +
        "  }\n" +
        "  .modal.open { display: flex; }\n" +
        "  .modal img { max-width: 92vw; max-height: 85vh; border-radius: 22px; }\n" +
        "</style>\n" +
        "</head>\n" +
        "<body>\n" +
        "\n" +
        "<div class=\"login-wrap\" id=\"loginView\" style=\"display:none\">\n" +
        "  <div class=\"login-card\">\n" +
        "    <div style=\"display:flex;align-items:center;gap:14px;margin-bottom:22px\">\n" +
        "      <div class=\"logo\">" + SVG_BRAND.replace("class=\"ico\"", "class=\"ico ico-lg\"") + "</div>\n" +
        "      <div>\n" +
        "        <h1 style=\"font-size:22px\">Expressive Web Files</h1>\n" +
        "        <div class=\"sub\">Inicia sesión para transferir archivos</div>\n" +
        "      </div>\n" +
        "    </div>\n" +
        "    <div class=\"field\">\n" +
        "      <label for=\"loginUser\">Usuario</label>\n" +
        "      <input type=\"text\" id=\"loginUser\" placeholder=\"Usuario\" autocomplete=\"username\">\n" +
        "    </div>\n" +
        "    <div class=\"field\">\n" +
        "      <label for=\"loginPass\">Contraseña</label>\n" +
        "      <input type=\"password\" id=\"loginPass\" placeholder=\"Contraseña\" autocomplete=\"current-password\">\n" +
        "    </div>\n" +
        "    <div id=\"loginErr\" style=\"color:var(--md-sys-color-error);font-size:13px;min-height:20px;margin-bottom:10px\"></div>\n" +
        "    <button class=\"btn btn-primary\" style=\"width:100%;padding:14px\" onclick=\"doLogin()\">" + SVG_KEY + " Iniciar sesión</button>\n" +
        "  </div>\n" +
        "</div>\n" +
        "\n" +
        "<div class=\"wrap\" id=\"appView\" style=\"display:none\">\n" +
        "  <div class=\"header\">\n" +
        "    <div class=\"brand\">\n" +
        "      <div class=\"logo\">" + SVG_BRAND.replace("class=\"ico\"", "class=\"ico ico-lg\"") + "</div>\n" +
        "      <div>\n" +
        "        <h1>Expressive Web Files</h1>\n" +
        "        <div class=\"sub\" id=\"devInfo\">Conectando con el teléfono...</div>\n" +
        "      </div>\n" +
        "    </div>\n" +
        "    <div class=\"pill\" id=\"connPill\"><span class=\"dot\"></span><span id=\"connLabel\">En línea</span></div>\n" +
        "  </div>\n" +
        "\n" +
        "  <div id=\"tabServer\">\n" +
        "    <div class=\"card card-hero\">\n" +
        "      <div class=\"card-title\">\n" +
        "        <span class=\"title-ico\">" + SVG_DNS + " Velocidad en vivo</span>\n" +
        "        <span class=\"tonal-pill\" id=\"concLabel\">4 hilos</span>\n" +
        "      </div>\n" +
        "      <div class=\"speed-row\">\n" +
        "        <div class=\"speed-box speed-box-up\">\n" +
        "          <div class=\"speed-label\">" + SVG_UPLOAD + " Subida</div>\n" +
        "          <div class=\"speed-val\" id=\"upSpeed\">0 B/s</div>\n" +
        "          <div class=\"speed-total\" id=\"upTotal\">Total: 0 B</div>\n" +
        "        </div>\n" +
        "        <div class=\"speed-box speed-box-down\">\n" +
        "          <div class=\"speed-label\">" + SVG_DOWNLOAD + " Descarga</div>\n" +
        "          <div class=\"speed-val\" id=\"downSpeed\">0 B/s</div>\n" +
        "          <div class=\"speed-total\" id=\"downTotal\">Total: 0 B</div>\n" +
        "        </div>\n" +
        "      </div>\n" +
        "      <canvas id=\"speedChart\" width=\"640\" height=\"108\"></canvas>\n" +
        "    </div>\n" +
        "\n" +
        "    <div class=\"card\">\n" +
        "      <div class=\"card-title\">\n" +
        "        <span class=\"title-ico\">" + SVG_UPLOAD + " Transferencias activas</span>\n" +
        "        <div style=\"display:flex;align-items:center;gap:8px\">\n" +
        "          <span class=\"tonal-pill\" id=\"activeCount\">0 activas</span>\n" +
        "          <button class=\"btn btn-sm btn-danger\" id=\"stopAllBtn\" style=\"display:none\" onclick=\"stopAllTransfers()\">" + SVG_STOP + " Detener todo</button>\n" +
        "        </div>\n" +
        "      </div>\n" +
        "      <div id=\"batchBanner\" class=\"batch-banner\" style=\"display:none\">\n" +
        "        <div class=\"batch-top\">\n" +
        "          <span id=\"batchTitle\" style=\"display:inline-flex;align-items:center;gap:6px\">" + SVG_FOLDER + " Estructura de carpeta</span>\n" +
        "          <span id=\"batchCount\" style=\"font-variant-numeric:tabular-nums\">0 / 0 archivos</span>\n" +
        "        </div>\n" +
        "        <div class=\"prog-bg\" style=\"margin:8px 0 4px\"><div class=\"prog-fill prog-up\" id=\"batchBar\" style=\"width:0%\"></div></div>\n" +
        "        <div id=\"batchSub\" style=\"font-size:12px;opacity:0.85\">Conservando jerarquía de subcarpetas</div>\n" +
        "      </div>\n" +
        "      <div id=\"activeList\"><div class=\"empty\">No hay transferencias activas en este momento.</div></div>\n" +
        "    </div>\n" +
        "\n" +
        "    <div class=\"card\">\n" +
        "      <div class=\"card-title\"><span class=\"title-ico\">" + SVG_PHOTO + " Enviar fotos, archivos o carpetas al teléfono</span></div>\n" +
        "      <div class=\"controls\">\n" +
        "        <label class=\"sub\" style=\"font-weight:700\">Carpeta de destino:</label>\n" +
        "        <select id=\"targetDir\" onchange=\"onTargetDirChange()\">\n" +
        "          <option value=\"DCIM/Camera\">/DCIM/Camera (Galería / Cámara)</option>\n" +
        "          <option value=\"Pictures\">/Pictures (Imágenes)</option>\n" +
        "          <option value=\"Download\">/Download (Descargas)</option>\n" +
        "          <option value=\"_current_\">Usar carpeta abierta en el explorador</option>\n" +
        "          <option value=\"_custom_\">Carpeta personalizada...</option>\n" +
        "        </select>\n" +
        "        <input type=\"text\" id=\"customDir\" placeholder=\"Ej: Pictures/Vacaciones\" style=\"display:none;min-width:200px\">\n" +
        "        <button class=\"btn btn-tonal btn-sm\" onclick=\"createSubfolder()\">" + SVG_FOLDER + " Nueva carpeta</button>\n" +
        "      </div>\n" +
        "      <div class=\"dropzone\" id=\"dropzone\">\n" +
        "        <div style=\"font-size:18px;font-weight:700;margin-bottom:6px;letter-spacing:-0.01em\">Arrastra aquí carpetas enteras, fotos o archivos</div>\n" +
        "        <div class=\"sub\" style=\"margin-bottom:18px\">Se conserva exactamente la estructura completa de carpetas y subcarpetas del equipo</div>\n" +
        "        <div style=\"display:flex;gap:10px;justify-content:center;flex-wrap:wrap\">\n" +
        "          <button class=\"btn btn-primary\" onclick=\"document.getElementById('imgInput').click()\">" + SVG_PHOTO + " Seleccionar fotos / vídeos</button>\n" +
        "          <button class=\"btn btn-primary\" onclick=\"document.getElementById('folderInput').click()\">" + SVG_FOLDER + " Subir carpeta entera</button>\n" +
        "          <button class=\"btn btn-tonal\" onclick=\"document.getElementById('allInput').click()\">" + SVG_UPLOAD + " Archivos sueltos</button>\n" +
        "        </div>\n" +
        "        <input type=\"file\" id=\"imgInput\" accept=\"image/*,video/*\" multiple hidden>\n" +
        "        <input type=\"file\" id=\"allInput\" multiple hidden>\n" +
        "        <input type=\"file\" id=\"folderInput\" webkitdirectory directory multiple hidden>\n" +
        "      </div>\n" +
        "    </div>\n" +
        "\n" +
        "    <div class=\"card\">\n" +
        "      <div class=\"card-title\"><span class=\"title-ico\">" + SVG_STORAGE + " Historial reciente</span></div>\n" +
        "      <div id=\"recentList\"><div class=\"empty\">Todavía no se ha completado ninguna transferencia en esta sesión.</div></div>\n" +
        "    </div>\n" +
        "  </div>\n" +
        "\n" +
        WebUiExplorer.SECTION_HTML +
        "\n" +
        WebUiStorage.SECTION_HTML +
        "\n" +
        "  <div id=\"tabSettings\" style=\"display:none\">\n" +
        "    <div class=\"card\">\n" +
        "      <div class=\"card-title\"><span class=\"title-ico\">" + SVG_STORAGE + " Dispositivo y almacenamiento</span></div>\n" +
        "      <div style=\"font-size:17px;font-weight:700;margin-bottom:6px;color:var(--md-sys-color-primary)\" id=\"settingsUrl\">http://...</div>\n" +
        "      <div class=\"sub\" id=\"storageText\">Almacenamiento interno: calculando...</div>\n" +
        "      <div class=\"storage-bar-bg\"><div class=\"storage-bar-fill\" id=\"storageBar\"></div></div>\n" +
        "    </div>\n" +
        "\n" +
        "    <div class=\"card\">\n" +
        "      <div class=\"card-title\"><span class=\"title-ico\">" + SVG_TUNE + " Rendimiento de subida simultánea</span></div>\n" +
        "      <div class=\"sub\" style=\"margin-bottom:14px\">Los archivos grandes (&ge; 8 MB) se dividen en bloques paralelos de 4 MB para aprovechar todo el Wi-Fi.</div>\n" +
        "      <div class=\"chips\" id=\"concChips\">\n" +
        "        <span class=\"chip\" onclick=\"setConcurrency(1)\">1 hilo</span>\n" +
        "        <span class=\"chip\" onclick=\"setConcurrency(2)\">2 hilos</span>\n" +
        "        <span class=\"chip\" onclick=\"setConcurrency(4)\">4 hilos (Recomendado)</span>\n" +
        "        <span class=\"chip\" onclick=\"setConcurrency(6)\">6 hilos</span>\n" +
        "      </div>\n" +
        "    </div>\n" +
        "\n" +
        "    <div class=\"card\">\n" +
        "      <div class=\"card-title\"><span class=\"title-ico\">" + SVG_KEY + " Sesión y credenciales</span></div>\n" +
        "      <div class=\"sub\" id=\"authInfo\" style=\"margin-bottom:12px\">Comprobando sesión...</div>\n" +
        "      <div class=\"sub\" style=\"margin-bottom:16px\">Puedes cambiar el usuario y la contraseña desde la pestaña <b>Ajustes</b> de la app en tu teléfono.</div>\n" +
        "      <button class=\"btn btn-danger\" id=\"logoutBtn\" onclick=\"doLogout()\">" + SVG_STOP + " Cerrar sesión</button>\n" +
        "    </div>\n" +
        "    <div class=\"sub\" id=\"webVersionFooter\" style=\"text-align:center;margin:10px 0 18px;opacity:.75\">Expressive Web Files</div>\n" +
        "  </div>\n" +
        "\n" +
        "  <div class=\"floating-nav\">\n" +
        "    <button class=\"nav-pill active\" id=\"navServer\" onclick=\"switchTab('server')\">" + SVG_DNS + " Servidor</button>\n" +
        "    <button class=\"nav-pill\" id=\"navFiles\" onclick=\"switchTab('files')\">" + SVG_FOLDER + " Archivos</button>\n" +
        "    <button class=\"nav-pill\" id=\"navStorage\" onclick=\"switchTab('storage')\">" + SVG_STORAGE + " Almacenamiento</button>\n" +
        "    <button class=\"nav-pill\" id=\"navSettings\" onclick=\"switchTab('settings')\">" + SVG_TUNE + " Ajustes</button>\n" +
        "  </div>\n" +
        "</div>\n" +
        "\n" +
        WebUiExplorer.MODALS_HTML +
        "<div class=\"modal\" id=\"previewModal\" onclick=\"this.classList.remove('open')\">\n" +
        "  <img id=\"previewImg\" src=\"\" alt=\"Vista previa\">\n" +
        "</div>\n\n";

    private static final String HTML_SCRIPT =
        "<script>\n" +
        "let currentDir = 'DCIM/Camera';\n" +
        "let currentEntries = [];\n" +
        "let explorerViewMode = localStorage.getItem('fp_exp_view') || 'list';\n" +
        "let pollTimer = null;\n" +
        "let liveTheme = null;\n" +
        "let concurrency = parseInt(localStorage.getItem('fp_concurrency') || '4', 10);\n" +
        "if (![1,2,4,6].includes(concurrency)) concurrency = 4;\n" +
        "const uploadQueue = [];\n" +
        "let activeWorkers = 0;\n" +
        "const activeXhrs = new Map();\n" +
        "const cancelledGroups = new Set();\n" +
        "const batchStats = { active: false, roots: new Set(), dirs: new Set(), total: 0, done: 0 };\n" +
        "\n" +
        "function updateBatchBanner() {\n" +
        "  const el = document.getElementById('batchBanner');\n" +
        "  if (!el) return;\n" +
        "  if (!batchStats.active || batchStats.total === 0) {\n" +
        "    el.style.display = 'none';\n" +
        "    return;\n" +
        "  }\n" +
        "  el.style.display = 'block';\n" +
        "  const rootList = Array.from(batchStats.roots);\n" +
        "  const label = rootList.length > 0 ? rootList.slice(0, 2).join(', ') + (rootList.length > 2 ? ' (+' + (rootList.length - 2) + ')' : '') : 'Lote de archivos';\n" +
        "  document.getElementById('batchTitle').textContent = label;\n" +
        "  document.getElementById('batchCount').textContent = batchStats.done + ' / ' + batchStats.total + ' archivos';\n" +
        "  const pct = Math.min(100, Math.round((batchStats.done / Math.max(1, batchStats.total)) * 100));\n" +
        "  document.getElementById('batchBar').style.width = pct + '%';\n" +
        "  const dirCount = batchStats.dirs.size;\n" +
        "  document.getElementById('batchSub').textContent = dirCount > 0\n" +
        "    ? ('Replicando ' + dirCount + (dirCount === 1 ? ' carpeta' : ' carpetas') + ' con su jerarquía original')\n" +
        "    : 'Subiendo archivos al teléfono';\n" +
        "}\n" +
        "function resetBatchStats() {\n" +
        "  batchStats.active = false;\n" +
        "  batchStats.roots.clear();\n" +
        "  batchStats.dirs.clear();\n" +
        "  batchStats.total = 0;\n" +
        "  batchStats.done = 0;\n" +
        "  updateBatchBanner();\n" +
        "}\n" +
        "\n" +
        "let dispUp = 0, tgtUp = 0;\n" +
        "let dispDown = 0, tgtDown = 0;\n" +
        "const txAnimSpeed = new Map();\n" +
        "\n" +
        "function applyTheme(t) {\n" +
        "  if (!t || typeof t !== 'object') return;\n" +
        "  liveTheme = t;\n" +
        "  const map = {\n" +
        "    surface: '--md-sys-color-surface',\n" +
        "    surfaceDim: '--md-sys-color-surface-dim',\n" +
        "    surfaceBright: '--md-sys-color-surface-bright',\n" +
        "    surfaceContainerLowest: '--md-sys-color-surface-container-lowest',\n" +
        "    surfaceContainerLow: '--md-sys-color-surface-container-low',\n" +
        "    surfaceContainer: '--md-sys-color-surface-container',\n" +
        "    surfaceContainerHigh: '--md-sys-color-surface-container-high',\n" +
        "    surfaceContainerHighest: '--md-sys-color-surface-container-highest',\n" +
        "    primary: '--md-sys-color-primary',\n" +
        "    onPrimary: '--md-sys-color-on-primary',\n" +
        "    primaryContainer: '--md-sys-color-primary-container',\n" +
        "    onPrimaryContainer: '--md-sys-color-on-primary-container',\n" +
        "    secondary: '--md-sys-color-secondary',\n" +
        "    onSecondary: '--md-sys-color-on-secondary',\n" +
        "    secondaryContainer: '--md-sys-color-secondary-container',\n" +
        "    onSecondaryContainer: '--md-sys-color-on-secondary-container',\n" +
        "    tertiary: '--md-sys-color-tertiary',\n" +
        "    onTertiary: '--md-sys-color-on-tertiary',\n" +
        "    tertiaryContainer: '--md-sys-color-tertiary-container',\n" +
        "    onTertiaryContainer: '--md-sys-color-on-tertiary-container',\n" +
        "    onSurface: '--md-sys-color-on-surface',\n" +
        "    onSurfaceVariant: '--md-sys-color-on-surface-variant',\n" +
        "    outline: '--md-sys-color-outline',\n" +
        "    outlineVariant: '--md-sys-color-outline-variant',\n" +
        "    error: '--md-sys-color-error',\n" +
        "    onError: '--md-sys-color-on-error',\n" +
        "    errorContainer: '--md-sys-color-error-container',\n" +
        "    onErrorContainer: '--md-sys-color-on-error-container'\n" +
        "  };\n" +
        "  const rootStyle = document.documentElement.style;\n" +
        "  for (const k in map) {\n" +
        "    if (t[k]) rootStyle.setProperty(map[k], t[k]);\n" +
        "  }\n" +
        "  if (t.cardRadiusPx) rootStyle.setProperty('--ui-radius-card', t.cardRadiusPx + 'px');\n" +
        "  if (t.heroRadiusPx) rootStyle.setProperty('--ui-radius-hero', t.heroRadiusPx + 'px');\n" +
        "  if (t.btnRadiusPx) rootStyle.setProperty('--ui-radius-btn', t.btnRadiusPx + 'px');\n" +
        "  if (t.fontFamily) rootStyle.setProperty('--ui-font', t.fontFamily);\n" +
        "  if (t.cardBorder) rootStyle.setProperty('--ui-card-border', t.cardBorder);\n" +
        "  if (t.cardShadow) rootStyle.setProperty('--ui-card-shadow', t.cardShadow);\n" +
        "  if (typeof t.uppercaseBtns === 'boolean') rootStyle.setProperty('--ui-btn-transform', t.uppercaseBtns ? 'uppercase' : 'none');\n" +
        "  if (t.heroGradient && t.heroGradient !== 'none') rootStyle.setProperty('--ui-hero-bg', t.heroGradient);\n" +
        "  else rootStyle.setProperty('--ui-hero-bg', 'var(--md-sys-color-surface-container)');\n" +
        "  if (typeof t.isLight === 'boolean') {\n" +
        "    rootStyle.setProperty('--ui-nav-shadow', t.isLight ? '0 8px 24px rgba(0,0,0,0.14)' : '0 14px 36px rgba(0,0,0,0.48)');\n" +
        "    rootStyle.setProperty('--ui-ink', t.isLight ? 'rgba(0,0,0,0.12)' : 'rgba(255,255,255,0.28)');\n" +
        "  }\n" +
        "  const meta = document.getElementById('themeMeta');\n" +
        "  if (meta && t.surface) meta.setAttribute('content', t.surface);\n" +
        "}\n" +
        "\n" +
        "function hexToRgba(hex, a) {\n" +
        "  const clean = String(hex || '#ADC6FF').replace('#', '');\n" +
        "  const n = parseInt(clean.length === 6 ? clean : 'ADC6FF', 16);\n" +
        "  const r = (n >> 16) & 255, g = (n >> 8) & 255, b = n & 255;\n" +
        "  return 'rgba(' + r + ',' + g + ',' + b + ',' + a + ')';\n" +
        "}\n" +
        "\n" +
        "function fmtBytes(b) {\n" +
        "  if (b < 0) return '—';\n" +
        "  if (b < 1024) return Math.round(b) + ' B';\n" +
        "  if (b < 1048576) return (b / 1024).toFixed(1) + ' KB';\n" +
        "  if (b < 1073741824) return (b / 1048576).toFixed(2) + ' MB';\n" +
        "  return (b / 1073741824).toFixed(2) + ' GB';\n" +
        "}\n" +
        "function fmtSpeed(b) { return fmtBytes(b) + '/s'; }\n" +
        "function esc(s) {\n" +
        "  return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/\"/g,'&quot;');\n" +
        "}\n" +
        "function encJs(s) { return encodeURIComponent(s).replace(/'/g, '%27'); }\n" +
        "\n" +
        "document.addEventListener('pointerdown', (e) => {\n" +
        "  const target = e.target.closest('.btn, .chip, .nav-pill, .dropzone');\n" +
        "  if (!target) return;\n" +
        "  const rect = target.getBoundingClientRect();\n" +
        "  const size = Math.max(rect.width, rect.height);\n" +
        "  const ink = document.createElement('span');\n" +
        "  ink.className = 'm3-ink';\n" +
        "  ink.style.width = ink.style.height = size + 'px';\n" +
        "  ink.style.left = (e.clientX - rect.left - size / 2) + 'px';\n" +
        "  ink.style.top = (e.clientY - rect.top - size / 2) + 'px';\n" +
        "  target.appendChild(ink);\n" +
        "  setTimeout(() => ink.remove(), 580);\n" +
        "});\n" +
        "\n" +
        "function animLoop() {\n" +
        "  dispUp += (tgtUp - dispUp) * 0.24;\n" +
        "  dispDown += (tgtDown - dispDown) * 0.24;\n" +
        "  if (Math.abs(tgtUp - dispUp) < 1) dispUp = tgtUp;\n" +
        "  if (Math.abs(tgtDown - dispDown) < 1) dispDown = tgtDown;\n" +
        "  const upEl = document.getElementById('upSpeed');\n" +
        "  const downEl = document.getElementById('downSpeed');\n" +
        "  if (upEl) upEl.textContent = fmtSpeed(dispUp);\n" +
        "  if (downEl) downEl.textContent = fmtSpeed(dispDown);\n" +
        "  for (const [id, st] of txAnimSpeed.entries()) {\n" +
        "    st.cur += (st.tgt - st.cur) * 0.28;\n" +
        "    if (Math.abs(st.tgt - st.cur) < 1) st.cur = st.tgt;\n" +
        "    const el = document.querySelector('[data-tx-id=\"' + id + '\"] .tx-speed');\n" +
        "    if (el) el.textContent = fmtSpeed(st.cur);\n" +
        "  }\n" +
        "  requestAnimationFrame(animLoop);\n" +
        "}\n" +
        "requestAnimationFrame(animLoop);\n" +
        "\n" +
        "function switchTab(tab) {\n" +
        "  document.getElementById('tabServer').style.display = (tab === 'server') ? 'block' : 'none';\n" +
        "  document.getElementById('tabFiles').style.display = (tab === 'files') ? 'block' : 'none';\n" +
        "  document.getElementById('tabStorage').style.display = (tab === 'storage') ? 'block' : 'none';\n" +
        "  document.getElementById('tabSettings').style.display = (tab === 'settings') ? 'block' : 'none';\n" +
        "  document.getElementById('navServer').classList.toggle('active', tab === 'server');\n" +
        "  document.getElementById('navFiles').classList.toggle('active', tab === 'files');\n" +
        "  document.getElementById('navStorage').classList.toggle('active', tab === 'storage');\n" +
        "  document.getElementById('navSettings').classList.toggle('active', tab === 'settings');\n" +
        "  window.scrollTo({ top: 0, behavior: 'smooth' });\n" +
        "  if (tab === 'files') { browse(currentDir); }\n" +
        "  if (tab === 'storage') { loadStorageVolumesChips(); if (!stRootTree) runStorageAnalyze(stBaseDir); }\n" +
        "}\n" +
        "\n" +
        "function setConcurrency(n) {\n" +
        "  concurrency = n;\n" +
        "  localStorage.setItem('fp_concurrency', String(n));\n" +
        "  renderConcurrency();\n" +
        "  pumpQueue();\n" +
        "}\n" +
        "\n" +
        "function renderConcurrency() {\n" +
        "  document.getElementById('concLabel').textContent = concurrency + (concurrency === 1 ? ' hilo' : ' hilos');\n" +
        "  const chips = document.querySelectorAll('#concChips .chip');\n" +
        "  const vals = [1, 2, 4, 6];\n" +
        "  chips.forEach((c, idx) => c.classList.toggle('active', vals[idx] === concurrency));\n" +
        "}\n" +
        "\n" +
        "async function checkAuth() {\n" +
        "  try {\n" +
        "    const r = await fetch('/api/auth');\n" +
        "    const a = await r.json();\n" +
        "    if (a.theme) applyTheme(a.theme);\n" +
        "    if (a.authEnabled && !a.authenticated) {\n" +
        "      showLogin();\n" +
        "    } else {\n" +
        "      showApp(a);\n" +
        "    }\n" +
        "  } catch (e) {\n" +
        "    showLogin();\n" +
        "  }\n" +
        "}\n" +
        "\n" +
        "function showLogin() {\n" +
        "  if (pollTimer) { clearInterval(pollTimer); pollTimer = null; }\n" +
        "  document.getElementById('appView').style.display = 'none';\n" +
        "  document.getElementById('loginView').style.display = 'flex';\n" +
        "  document.getElementById('loginErr').textContent = '';\n" +
        "}\n" +
        "\n" +
        "function showApp(auth) {\n" +
        "  document.getElementById('loginView').style.display = 'none';\n" +
        "  document.getElementById('appView').style.display = 'block';\n" +
        "  const authInfo = document.getElementById('authInfo');\n" +
        "  const logoutBtn = document.getElementById('logoutBtn');\n" +
        "  if (auth && auth.authEnabled) {\n" +
        "    authInfo.textContent = 'Sesión iniciada como: ' + auth.user;\n" +
        "    logoutBtn.style.display = 'inline-flex';\n" +
        "  } else {\n" +
        "    authInfo.textContent = 'Portal abierto sin contraseña.';\n" +
        "    logoutBtn.style.display = 'none';\n" +
        "  }\n" +
        "  renderConcurrency();\n" +
        "  browse(currentDir);\n" +
        "  pollStatus();\n" +
        "  if (pollTimer) clearInterval(pollTimer);\n" +
        "  pollTimer = setInterval(pollStatus, 500);\n" +
        "}\n" +
        "\n" +
        "async function doLogin() {\n" +
        "  const user = document.getElementById('loginUser').value.trim();\n" +
        "  const pass = document.getElementById('loginPass').value;\n" +
        "  const errEl = document.getElementById('loginErr');\n" +
        "  errEl.textContent = '';\n" +
        "  try {\n" +
        "    const r = await fetch('/api/login', {\n" +
        "      method: 'POST',\n" +
        "      headers: { 'Content-Type': 'application/json' },\n" +
        "      body: JSON.stringify({ user, pass })\n" +
        "    });\n" +
        "    const data = await r.json();\n" +
        "    if (r.ok && data.ok) {\n" +
        "      checkAuth();\n" +
        "    } else {\n" +
        "      errEl.textContent = data.error || 'Credenciales incorrectas';\n" +
        "    }\n" +
        "  } catch (e) {\n" +
        "    errEl.textContent = 'Error de conexión con el teléfono';\n" +
        "  }\n" +
        "}\n" +
        "\n" +
        "async function doLogout() {\n" +
        "  await fetch('/api/logout', { method: 'POST' });\n" +
        "  checkAuth();\n" +
        "}\n" +
        "\n" +
        "['loginUser', 'loginPass'].forEach(id => {\n" +
        "  document.getElementById(id).addEventListener('keydown', e => { if (e.key === 'Enter') doLogin(); });\n" +
        "});\n" +
        "\n" +
        "function onTargetDirChange() {\n" +
        "  const sel = document.getElementById('targetDir').value;\n" +
        "  document.getElementById('customDir').style.display = (sel === '_custom_') ? 'inline-block' : 'none';\n" +
        "}\n" +
        "\n" +
        "function resolveUploadDir() {\n" +
        "  const sel = document.getElementById('targetDir').value;\n" +
        "  if (sel === '_current_') return currentDir;\n" +
        "  if (sel === '_custom_') return document.getElementById('customDir').value.replace(/^\\/+|\\/+$/g, '');\n" +
        "  return sel;\n" +
        "}\n" +
        "\n" +
        "async function createSubfolder() {\n" +
        "  const base = resolveUploadDir();\n" +
        "  const name = prompt('Nombre de la nueva carpeta dentro de /' + (base || '') + ':');\n" +
        "  if (!name || !name.trim()) return;\n" +
        "  const path = (base ? base + '/' : '') + name.trim().replace(/^\\/+|\\/+$/g, '');\n" +
        "  const r = await fetch('/api/mkdir?dir=' + encodeURIComponent(path), { method: 'POST' });\n" +
        "  if (r.ok) {\n" +
        "    browse(path);\n" +
        "    document.getElementById('targetDir').value = '_current_';\n" +
        "    onTargetDirChange();\n" +
        "  }\n" +
        "}\n" +
        "\n" +
        "function trackXhr(groupId, xhr) {\n" +
        "  if (!activeXhrs.has(groupId)) activeXhrs.set(groupId, new Set());\n" +
        "  activeXhrs.get(groupId).add(xhr);\n" +
        "}\n" +
        "function untrackXhr(groupId, xhr) {\n" +
        "  const set = activeXhrs.get(groupId);\n" +
        "  if (set) {\n" +
        "    set.delete(xhr);\n" +
        "    if (set.size === 0) activeXhrs.delete(groupId);\n" +
        "  }\n" +
        "}\n" +
        "function abortGroup(groupId) {\n" +
        "  const set = activeXhrs.get(groupId);\n" +
        "  if (set) {\n" +
        "    for (const xhr of Array.from(set)) xhr.abort();\n" +
        "    activeXhrs.delete(groupId);\n" +
        "  }\n" +
        "}\n" +
        "\n" +
        "async function ensureDirs(dirList) {\n" +
        "  const unique = Array.from(new Set(dirList)).filter(Boolean);\n" +
        "  let idx = 0;\n" +
        "  async function mkWorker() {\n" +
        "    while (idx < unique.length) {\n" +
        "      const d = unique[idx++];\n" +
        "      try {\n" +
        "        await fetch('/api/mkdir?dir=' + encodeURIComponent(d), { method: 'POST' });\n" +
        "      } catch (ignored) {}\n" +
        "    }\n" +
        "  }\n" +
        "  const n = Math.min(4, unique.length);\n" +
        "  if (n > 0) await Promise.all(Array.from({ length: n }, () => mkWorker()));\n" +
        "}\n" +
        "\n" +
        "async function enqueueFiles(files, useRelPath) {\n" +
        "  const baseDir = resolveUploadDir();\n" +
        "  const dirsToMk = new Set();\n" +
        "  const itemsToAdd = [];\n" +
        "  for (const file of files) {\n" +
        "    let destDir = baseDir;\n" +
        "    let fileName = file.name;\n" +
        "    if (useRelPath && file.webkitRelativePath) {\n" +
        "      const parts = file.webkitRelativePath.split('/').filter(Boolean);\n" +
        "      if (parts.length > 0) fileName = parts.pop();\n" +
        "      const sub = parts.join('/');\n" +
        "      if (parts.length > 0) batchStats.roots.add(parts[0]);\n" +
        "      if (sub) {\n" +
        "        destDir = baseDir ? (baseDir + '/' + sub) : sub;\n" +
        "        batchStats.dirs.add(sub);\n" +
        "        dirsToMk.add(destDir);\n" +
        "      }\n" +
        "    }\n" +
        "    itemsToAdd.push({ file, destDir, fileName });\n" +
        "  }\n" +
        "  if (itemsToAdd.length === 0) return;\n" +
        "  batchStats.active = true;\n" +
        "  batchStats.total += itemsToAdd.length;\n" +
        "  updateBatchBanner();\n" +
        "  if (dirsToMk.size > 0) await ensureDirs(Array.from(dirsToMk));\n" +
        "  for (const it of itemsToAdd) uploadQueue.push(it);\n" +
        "  pumpQueue();\n" +
        "}\n" +
        "\n" +
        "function pumpQueue() {\n" +
        "  while (activeWorkers < concurrency && uploadQueue.length > 0) {\n" +
        "    const item = uploadQueue.shift();\n" +
        "    activeWorkers++;\n" +
        "    const p = (item.file.size >= 8 * 1024 * 1024) ? uploadChunked(item) : uploadSingle(item);\n" +
        "    p.finally(() => {\n" +
        "      activeWorkers--;\n" +
        "      if (batchStats.active) {\n" +
        "        batchStats.done++;\n" +
        "        updateBatchBanner();\n" +
        "      }\n" +
        "      if (uploadQueue.length > 0) {\n" +
        "        pumpQueue();\n" +
        "      } else if (activeWorkers === 0) {\n" +
        "        resetBatchStats();\n" +
        "        browse(currentDir);\n" +
        "      }\n" +
        "    });\n" +
        "  }\n" +
        "}\n" +
        "\n" +
        "function uploadSingle(item) {\n" +
        "  return new Promise((resolve) => {\n" +
        "    const key = 'name:' + item.fileName;\n" +
        "    const xhr = new XMLHttpRequest();\n" +
        "    trackXhr(key, xhr);\n" +
        "    const url = '/api/upload?dir=' + encodeURIComponent(item.destDir) + '&name=' + encodeURIComponent(item.fileName);\n" +
        "    xhr.open('POST', url, true);\n" +
        "    xhr.setRequestHeader('Content-Type', item.file.type || 'application/octet-stream');\n" +
        "    const done = () => { untrackXhr(key, xhr); resolve(); };\n" +
        "    xhr.onload = () => { if (xhr.status === 401) showLogin(); done(); };\n" +
        "    xhr.onerror = done;\n" +
        "    xhr.onabort = done;\n" +
        "    xhr.send(item.file);\n" +
        "  });\n" +
        "}\n" +
        "\n" +
        "async function uploadChunked(item) {\n" +
        "  const initRes = await fetch('/api/upload_init?dir=' + encodeURIComponent(item.destDir) +\n" +
        "    '&name=' + encodeURIComponent(item.fileName) + '&total=' + item.file.size, { method: 'POST' });\n" +
        "  if (initRes.status === 401) { showLogin(); return; }\n" +
        "  if (!initRes.ok) return;\n" +
        "  const initData = await initRes.json();\n" +
        "  const txId = initData.id;\n" +
        "  const chunkSize = 4 * 1024 * 1024;\n" +
        "  const offsets = [];\n" +
        "  for (let off = 0; off < item.file.size; off += chunkSize) offsets.push(off);\n" +
        "\n" +
        "  let nextIdx = 0;\n" +
        "  let failed = false;\n" +
        "  const workerCount = Math.min(concurrency, offsets.length);\n" +
        "\n" +
        "  async function chunkWorker() {\n" +
        "    while (nextIdx < offsets.length && !failed && !cancelledGroups.has(txId)) {\n" +
        "      const off = offsets[nextIdx++];\n" +
        "      const slice = item.file.slice(off, Math.min(item.file.size, off + chunkSize));\n" +
        "      const ok = await new Promise((res) => {\n" +
        "        const xhr = new XMLHttpRequest();\n" +
        "        trackXhr(txId, xhr);\n" +
        "        xhr.open('POST', '/api/upload_chunk?id=' + encodeURIComponent(txId) + '&offset=' + off, true);\n" +
        "        xhr.setRequestHeader('Content-Type', 'application/octet-stream');\n" +
        "        xhr.onload = () => { untrackXhr(txId, xhr); res(xhr.status === 200); };\n" +
        "        xhr.onerror = () => { untrackXhr(txId, xhr); res(false); };\n" +
        "        xhr.onabort = () => { untrackXhr(txId, xhr); res(false); };\n" +
        "        xhr.send(slice);\n" +
        "      });\n" +
        "      if (!ok) { failed = true; abortGroup(txId); break; }\n" +
        "    }\n" +
        "  }\n" +
        "\n" +
        "  await Promise.all(Array.from({ length: workerCount }, () => chunkWorker()));\n" +
        "  if (!failed && !cancelledGroups.has(txId)) {\n" +
        "    await fetch('/api/upload_finish?id=' + encodeURIComponent(txId), { method: 'POST' });\n" +
        "  } else {\n" +
        "    await fetch('/api/cancel?id=' + encodeURIComponent(txId), { method: 'POST' });\n" +
        "    cancelledGroups.delete(txId);\n" +
        "  }\n" +
        "}\n" +
        "\n" +
        "async function stopTransfer(id, fileName) {\n" +
        "  cancelledGroups.add(id);\n" +
        "  for (const key of [id, 'name:' + fileName]) {\n" +
        "    const set = activeXhrs.get(key);\n" +
        "    if (set) {\n" +
        "      for (const xhr of Array.from(set)) xhr.abort();\n" +
        "      activeXhrs.delete(key);\n" +
        "    }\n" +
        "  }\n" +
        "  await fetch('/api/cancel?id=' + encodeURIComponent(id), { method: 'POST' });\n" +
        "  pollStatus();\n" +
        "}\n" +
        "\n" +
        "async function stopAllTransfers() {\n" +
        "  uploadQueue.length = 0;\n" +
        "  for (const set of activeXhrs.values()) {\n" +
        "    for (const xhr of Array.from(set)) xhr.abort();\n" +
        "  }\n" +
        "  activeXhrs.clear();\n" +
        "  resetBatchStats();\n" +
        "  await fetch('/api/cancel?id=all', { method: 'POST' });\n" +
        "  pollStatus();\n" +
        "}\n" +
        "\n" +
        "document.getElementById('imgInput').addEventListener('change', e => { enqueueFiles(e.target.files, false); e.target.value = ''; });\n" +
        "document.getElementById('allInput').addEventListener('change', e => { enqueueFiles(e.target.files, false); e.target.value = ''; });\n" +
        "document.getElementById('folderInput').addEventListener('change', e => { enqueueFiles(e.target.files, true); e.target.value = ''; });\n" +
        "\n" +
        "const dz = document.getElementById('dropzone');\n" +
        "dz.addEventListener('dragover', e => { e.preventDefault(); dz.classList.add('over'); });\n" +
        "dz.addEventListener('dragleave', () => dz.classList.remove('over'));\n" +
        "dz.addEventListener('drop', async e => {\n" +
        "  e.preventDefault();\n" +
        "  dz.classList.remove('over');\n" +
        "  const rawItems = e.dataTransfer.items;\n" +
        "  const entries = [];\n" +
        "  if (rawItems && rawItems.length) {\n" +
        "    for (let i = 0; i < rawItems.length; i++) {\n" +
        "      const it = rawItems[i];\n" +
        "      if (it.webkitGetAsEntry) {\n" +
        "        const en = it.webkitGetAsEntry();\n" +
        "        if (en) entries.push(en);\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "  if (entries.length > 0) {\n" +
        "    const baseDir = resolveUploadDir();\n" +
        "    const dirsToCreate = new Set();\n" +
        "    const collectedFiles = [];\n" +
        "    async function walk(entry, destDir, relSub) {\n" +
        "      if (entry.isFile) {\n" +
        "        await new Promise(res => entry.file(f => {\n" +
        "          collectedFiles.push({ file: f, destDir, fileName: f.name });\n" +
        "          res();\n" +
        "        }, () => res()));\n" +
        "      } else if (entry.isDirectory) {\n" +
        "        const nextDest = destDir ? (destDir + '/' + entry.name) : entry.name;\n" +
        "        const nextRel = relSub ? (relSub + '/' + entry.name) : entry.name;\n" +
        "        dirsToCreate.add(nextDest);\n" +
        "        batchStats.dirs.add(nextRel);\n" +
        "        const reader = entry.createReader();\n" +
        "        while (true) {\n" +
        "          const batch = await new Promise(res => reader.readEntries(res, () => res([])));\n" +
        "          if (!batch || batch.length === 0) break;\n" +
        "          for (const child of batch) await walk(child, nextDest, nextRel);\n" +
        "        }\n" +
        "      }\n" +
        "    }\n" +
        "    for (const en of entries) {\n" +
        "      if (en.isDirectory) batchStats.roots.add(en.name);\n" +
        "      await walk(en, baseDir, '');\n" +
        "    }\n" +
        "    if (dirsToCreate.size > 0) await ensureDirs(Array.from(dirsToCreate));\n" +
        "    if (collectedFiles.length > 0) {\n" +
        "      batchStats.active = true;\n" +
        "      batchStats.total += collectedFiles.length;\n" +
        "      updateBatchBanner();\n" +
        "      for (const item of collectedFiles) uploadQueue.push(item);\n" +
        "      pumpQueue();\n" +
        "    } else if (dirsToCreate.size > 0) {\n" +
        "      resetBatchStats();\n" +
        "      browse(currentDir);\n" +
        "    } else {\n" +
        "      resetBatchStats();\n" +
        "    }\n" +
        "  } else {\n" +
        "    enqueueFiles(e.dataTransfer.files, false);\n" +
        "  }\n" +
        "});\n" +
        "\n" +
        "function drawChart(upHist, downHist) {\n" +
        "  const canvas = document.getElementById('speedChart');\n" +
        "  const ctx = canvas.getContext('2d');\n" +
        "  const w = canvas.width, h = canvas.height;\n" +
        "  ctx.clearRect(0, 0, w, h);\n" +
        "  const gridCol = liveTheme ? hexToRgba(liveTheme.outlineVariant, 0.42) : 'rgba(68,71,79,0.42)';\n" +
        "  ctx.strokeStyle = gridCol;\n" +
        "  ctx.lineWidth = 1;\n" +
        "  [0.26, 0.58, 0.90].forEach(r => {\n" +
        "    const gy = Math.round((h - 16) * r + 8);\n" +
        "    ctx.beginPath(); ctx.moveTo(0, gy); ctx.lineTo(w, gy); ctx.stroke();\n" +
        "  });\n" +
        "  const maxVal = Math.max(1024 * 100, ...upHist, ...downHist);\n" +
        "  function plot(arr, hexCol) {\n" +
        "    if (!arr || arr.length < 2) return;\n" +
        "    const pts = arr.map((v, i) => ({ x: (i / (arr.length - 1)) * w, y: h - 10 - (v / maxVal) * (h - 22) }));\n" +
        "    ctx.beginPath();\n" +
        "    ctx.moveTo(pts[0].x, pts[0].y);\n" +
        "    for (let i = 0; i < pts.length - 1; i++) {\n" +
        "      const mx = (pts[i].x + pts[i + 1].x) * 0.5;\n" +
        "      ctx.bezierCurveTo(mx, pts[i].y, mx, pts[i + 1].y, pts[i + 1].x, pts[i + 1].y);\n" +
        "    }\n" +
        "    ctx.strokeStyle = hexCol;\n" +
        "    ctx.lineWidth = 2.8;\n" +
        "    ctx.stroke();\n" +
        "    ctx.lineTo(pts[pts.length - 1].x, h - 10);\n" +
        "    ctx.lineTo(pts[0].x, h - 10);\n" +
        "    ctx.closePath();\n" +
        "    const g = ctx.createLinearGradient(0, 0, 0, h);\n" +
        "    g.addColorStop(0, hexToRgba(hexCol, 0.28));\n" +
        "    g.addColorStop(1, 'rgba(0,0,0,0)');\n" +
        "    ctx.fillStyle = g;\n" +
        "    ctx.fill();\n" +
        "  }\n" +
        "  const cUp = liveTheme ? liveTheme.primary : '#ADC6FF';\n" +
        "  const cDown = liveTheme ? liveTheme.tertiary : '#DEBCDF';\n" +
        "  plot(downHist, cDown);\n" +
        "  plot(upHist, cUp);\n" +
        "}\n" +
        "\n" +
        "function syncActiveTransfers(actives) {\n" +
        "  const activeEl = document.getElementById('activeList');\n" +
        "  if (actives.length === 0) {\n" +
        "    txAnimSpeed.clear();\n" +
        "    if (!activeEl.querySelector('.empty')) {\n" +
        "      activeEl.innerHTML = '<div class=\"empty\">No hay transferencias activas en este momento.</div>';\n" +
        "    }\n" +
        "    return;\n" +
        "  }\n" +
        "  const emptyEl = activeEl.querySelector('.empty');\n" +
        "  if (emptyEl) emptyEl.remove();\n" +
        "\n" +
        "  const liveIds = new Set(actives.map(t => t.id));\n" +
        "  for (const row of Array.from(activeEl.querySelectorAll('.tx-item'))) {\n" +
        "    const id = row.getAttribute('data-tx-id');\n" +
        "    if (!liveIds.has(id)) {\n" +
        "      row.remove();\n" +
        "      txAnimSpeed.delete(id);\n" +
        "    }\n" +
        "  }\n" +
        "\n" +
        "  for (const tx of actives) {\n" +
        "    const isUp = tx.direction === 'UPLOAD';\n" +
        "    const pctTx = tx.totalBytes > 0 ? Math.min(100, Math.round((tx.bytesTransferred / tx.totalBytes) * 100)) : 100;\n" +
        "    let anim = txAnimSpeed.get(tx.id);\n" +
        "    if (!anim) { anim = { cur: tx.speedBps, tgt: tx.speedBps }; txAnimSpeed.set(tx.id, anim); }\n" +
        "    else { anim.tgt = tx.speedBps; }\n" +
        "\n" +
        "    let row = activeEl.querySelector('[data-tx-id=\"' + tx.id + '\"]');\n" +
        "    if (!row) {\n" +
        "      row = document.createElement('div');\n" +
        "      row.className = 'tx-item';\n" +
        "      row.setAttribute('data-tx-id', tx.id);\n" +
        "      row.innerHTML =\n" +
        "        '<div class=\"tx-top\">' +\n" +
        "          '<span class=\"tx-name\">' + esc(tx.fileName) + '</span>' +\n" +
        "          '<span style=\"display:flex;gap:6px;flex-wrap:wrap;align-items:center\">' +\n" +
        "            '<span class=\"badge ' + (isUp ? 'b-up' : 'b-down') + '\">' + (isUp ? '↑ Subiendo' : '↓ Descargando') + '</span>' +\n" +
        "            '<span class=\"badge b-folder\">' + esc(tx.folder) + '</span>' +\n" +
        "            '<button class=\"btn btn-sm btn-danger\" onclick=\"stopTransfer(decodeURIComponent(\\'' + encJs(tx.id) + '\\'), decodeURIComponent(\\'' + encJs(tx.fileName) + '\\'))\">Detener</button>' +\n" +
        "          '</span>' +\n" +
        "        '</div>' +\n" +
        "        '<div class=\"prog-bg\"><div class=\"prog-fill ' + (isUp ? 'prog-up' : 'prog-down') + '\" style=\"width:' + pctTx + '%\"></div></div>' +\n" +
        "        '<div class=\"tx-meta\">' +\n" +
        "          '<span class=\"tx-bytes\"></span>' +\n" +
        "          '<span class=\"' + (isUp ? 'tx-speed-up' : 'tx-speed-down') + '\"><b class=\"tx-speed\">' + fmtSpeed(tx.speedBps) + '</b></span>' +\n" +
        "        '</div>';\n" +
        "      activeEl.appendChild(row);\n" +
        "    }\n" +
        "    row.querySelector('.prog-fill').style.width = pctTx + '%';\n" +
        "    row.querySelector('.tx-bytes').textContent =\n" +
        "      fmtBytes(tx.bytesTransferred) + (tx.totalBytes > 0 ? ' / ' + fmtBytes(tx.totalBytes) + ' (' + pctTx + '%)' : '');\n" +
        "  }\n" +
        "}\n" +
        "\n" +
        "async function pollStatus() {\n" +
        "  try {\n" +
        "    const r = await fetch('/api/status');\n" +
        "    if (r.status === 401) { showLogin(); return; }\n" +
        "    if (!r.ok) throw new Error('HTTP ' + r.status);\n" +
        "    const s = await r.json();\n" +
        "    if (s.theme) applyTheme(s.theme);\n" +
        "    const urlStr = 'http://' + s.ip + ':' + s.port;\n" +
        "    document.getElementById('devInfo').textContent = s.deviceModel + ' · ' + urlStr;\n" +
        "    document.getElementById('settingsUrl').textContent = urlStr;\n" +
        "    if (s.appVersion) document.getElementById('webVersionFooter').textContent = 'Expressive Web Files · v' + s.appVersion + ' (build ' + (s.appVersionCode || '') + ')';\n" +
        "    const pill = document.getElementById('connPill');\n" +
        "    pill.classList.toggle('offline', !s.serverRunning);\n" +
        "    document.getElementById('connLabel').textContent = s.serverRunning ? 'En línea' : 'Detenido';\n" +
        "    tgtUp = s.uploadSpeedBps;\n" +
        "    tgtDown = s.downloadSpeedBps;\n" +
        "    document.getElementById('upTotal').textContent = 'Total: ' + fmtBytes(s.totalUploadBytes);\n" +
        "    document.getElementById('downTotal').textContent = 'Total: ' + fmtBytes(s.totalDownloadBytes);\n" +
        "    const used = Math.max(0, s.storageTotalBytes - s.storageFreeBytes);\n" +
        "    const pct = s.storageTotalBytes > 0 ? Math.round((used / s.storageTotalBytes) * 100) : 0;\n" +
        "    document.getElementById('storageText').textContent = 'Almacenamiento interno: ' + fmtBytes(s.storageFreeBytes) + ' libres de ' + fmtBytes(s.storageTotalBytes) + ' (' + pct + '% usado)';\n" +
        "    document.getElementById('storageBar').style.width = pct + '%';\n" +
        "    drawChart(s.uploadHistory || [], s.downloadHistory || []);\n" +
        "\n" +
        "    const actives = s.activeTransfers || [];\n" +
        "    const queued = uploadQueue.length;\n" +
        "    document.getElementById('activeCount').textContent = actives.length + (actives.length === 1 ? ' activa' : ' activas') + (queued > 0 ? ' · ' + queued + ' en cola' : '');\n" +
        "    document.getElementById('stopAllBtn').style.display = (actives.length > 0 || queued > 0) ? 'inline-flex' : 'none';\n" +
        "    syncActiveTransfers(actives);\n" +
        "\n" +
        "    const recents = s.recentTransfers || [];\n" +
        "    const recEl = document.getElementById('recentList');\n" +
        "    if (recents.length === 0) {\n" +
        "      recEl.innerHTML = '<div class=\"empty\">Todavía no se ha completado ninguna transferencia en esta sesión.</div>';\n" +
        "    } else {\n" +
        "      recEl.innerHTML = '<table><thead><tr><th>Tipo</th><th>Archivo</th><th>Carpeta</th><th>Tamaño</th><th>Velocidad</th><th>Estado</th></tr></thead><tbody>' +\n" +
        "        recents.slice(0, 20).map(r => {\n" +
        "          const isUp = r.direction === 'UPLOAD';\n" +
        "          const stBadge = r.cancelled ? '<span class=\"badge b-cancel\">Detenida</span>' : (r.ok ? '<span class=\"badge b-up\">OK</span>' : '<span class=\"badge b-cancel\">Error</span>');\n" +
        "          return '<tr>' +\n" +
        "            '<td><span class=\"badge ' + (isUp ? 'b-up' : 'b-down') + '\">' + (isUp ? '↑ Subida' : '↓ Descarga') + '</span></td>' +\n" +
        "            '<td style=\"font-weight:700\">' + esc(r.fileName) + '</td>' +\n" +
        "            '<td><span class=\"badge b-folder\">' + esc(r.folder) + '</span></td>' +\n" +
        "            '<td>' + fmtBytes(r.bytes) + '</td>' +\n" +
        "            '<td>' + fmtSpeed(r.avgSpeedBps) + '</td>' +\n" +
        "            '<td>' + stBadge + '</td>' +\n" +
        "          '</tr>';\n" +
        "        }).join('') + '</tbody></table>';\n" +
        "    }\n" +
        "  } catch (err) {\n" +
        "    document.getElementById('connPill').classList.add('offline');\n" +
        "    document.getElementById('connLabel').textContent = 'Reconectando';\n" +
        "  }\n" +
        "}\n" +
        "\n" +
        "async function delFile(path) {\n" +
        "  if (!confirm('¿Eliminar /' + path + '?')) return;\n" +
        "  await fetch('/api/file?path=' + encodeURIComponent(path), { method: 'DELETE' });\n" +
        "  browse(currentDir);\n" +
        "}\n" +
        "\n" +
        "function openPreview(url) {\n" +
        "  document.getElementById('previewImg').src = url;\n" +
        "  document.getElementById('previewModal').classList.add('open');\n" +
        "}\n";

    private static final String HTML_TAIL =
        "\ncheckAuth();\n" +
        "</script>\n" +
        "</body>\n" +
        "</html>\n";

    public static final String HTML = new StringBuilder(
            HTML_MARKUP.length() + HTML_SCRIPT.length()
            + WebUiExplorer.EXPLORER_JS_1.length()
            + WebUiExplorer.EXPLORER_JS_2.length()
            + WebUiStorage.STORAGE_JS.length()
            + HTML_TAIL.length())
            .append(HTML_MARKUP)
            .append(HTML_SCRIPT)
            .append(WebUiExplorer.EXPLORER_JS_1)
            .append(WebUiExplorer.EXPLORER_JS_2)
            .append(WebUiStorage.STORAGE_JS)
            .append(HTML_TAIL)
            .toString();
}
