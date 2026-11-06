# NSE Quant — Android companion

Android app for **NSE Quant Terminal**. It shows your desktop app's live analysis on your phone and alerts you while you're away from the Mac.

The Mac app stays in charge of everything: the single Shoonya session (Shoonya allows only one live connection per login), MongoDB, and all of the analysis. The phone reads what the Mac has computed through a small **read-only, key-protected API**. It can never place orders.

## Screens
| Tab | What it shows |
|---|---|
| **Home** | The 15-minute direction call for NIFTY and BANKNIFTY (with the writing idea, nearest support / resistance and today's hit rate) and level breaks. Market news (high and medium impact, tap to open). Live BANKNIFTY / NIFTY / VIX, open P&L, RED count and setup flag. The **Market monitor** switch. Every active alert. Your positions with EXIT / WATCH / BOOK checks and leg P&L. Global markets with the read for India's open. The indices Shoonya streams. A one-line FII/DII summary. |
| **BANKNIFTY** | Index with breadth (banks up/down, % of weight up), lifters and draggers. Heatmap sized by weight. Points each bank adds to the index. Alerts. Every bank with weight, LTP, % change, points, VWAP gap, relative volume and a sparkline. |
| **Chain** | NIFTY / BANKNIFTY: ATM IV, straddle, ±1σ, PCR (OI and change in OI), max pain, OI support and resistance, fresh writing. A mirrored chain (±15 strikes) with OI bars, change in OI, ITM shading, strike PCR and the spot divider. |
| **Risk** | Writer risk monitor for both indices: threat matrix, colour-coded alerts with the exact signals, prohibitions, safe zones, and guidance for your legs. |
| **FII/DII** | Evening squeeze-loading setup (conditions, track record, exposed side). Positioning read and writing posture. Strike zones. All signals. Recent sessions. |

**Home-screen widget (BANKNIFTY):** long-press the home screen → Widgets → NSE Quant → BANKNIFTY.
* **Shows:** live price; change in points and %; today's line against the previous close (dotted), green above and red below; day high/low and where price sits in the range; banks up/down and % of weight up; the biggest lifter and dragger in index points.
* **Sizes:** at 4×2 or larger you get the full view; shrink it to 2×1 for price and change only.
* **Updates:** every ~15 s while **Market monitor** is on, every 30 minutes otherwise (Android's minimum for widgets), and immediately when you tap ⟳. Tap the widget to open the app on the BANKNIFTY tab.

**Status bar and lock screen:** with the monitor on, the live price is drawn into the notification's status-bar icon (e.g. `57.8K / +0.62`), so it stays visible next to the clock, including on the lock screen.
* Choose in Settings: BANKNIFTY or NIFTY; price + %, % only, or points.
* The ticker notification also shows on the lock screen. If your phone hides sensitive content there, the lock-screen version shows prices only, never P&L.

**Market monitor** (a foreground service) does two things:
* It keeps an ongoing notification like the Mac's menu-bar ticker, refreshed every 5 s, for example `BNF 57,813.15 ▲39.5 (+0.07%)`, with NIFTY, VIX, P&L, ⚠ RED count and ⚑ setup flag.
* Every 30 s it checks the Mac's alerts and raises a **high-priority notification** for each new one: RED risk alerts, **support / resistance breaks** on NIFTY and BANKNIFTY, **major global moves** (several at once arrive as one summary), the evening setup, BANKNIFTY breadth warnings, EXIT on your positions, and HIGH-impact market news (tap to open the article). Each alert notifies once.
* The **15-minute direction calls** arrive on their own "Direction calls" channel: one notification per index, replaced each time, with sound only when the direction changes. The ticker notification also shows the current calls (e.g. `Dir N ▲ BNF ↔`) and the global read.

## Set up
1. **On the Mac:** run NSE Quant Terminal (the .app or `python app.py`). Open **Sync ▾ → Connect phone (Android app)…**, which shows the server address (e.g. `http://192.168.1.10:8787`) and the API key.
2. **Install the APK on the phone:** copy `dist/NSE-Quant-1.0.0-debug.apk` to the phone and open it (allow "install unknown apps" for your file manager). Or, with USB debugging on:
   ```bash
   ~/Library/Android/sdk/platform-tools/adb install -r dist/NSE-Quant-1.0.0-debug.apk
   ```
3. **Connect:** open **NSE Quant** → **Settings**, enter the address and key, tap **Save & test** (you should see "✅ Connected"), then **Done**.
4. **Monitor:** on Home, turn on **Market monitor** and allow notifications.

**Network:**
* Phone and Mac need to be on the same Wi-Fi.
* Office or hotel networks often isolate devices. Use **Tailscale** there: install it on both, then use the Mac's `100.x.x.x` address. It also lets you connect from anywhere.
* The API is plain HTTP inside your own network or Tailscale, so keep the key private.

## Build
Requirements: JDK 17 and the Android SDK (`platforms;android-35`, `build-tools;35.0.0`). `local.properties` points at the SDK.

```bash
./gradlew assembleDebug          # → app/build/outputs/apk/debug/app-debug.apk
```

Or open the folder in Android Studio.

**Stack:**
* Kotlin 2.0, Jetpack Compose (Material 3) and OkHttp; minSdk 26 (Android 8.0), targetSdk 35.
* No Google services and no analytics.
* Settings live in private app storage.

## API (served by the Mac app, `api_server.py`)
Every request is `GET` with `Authorization: Bearer <key>`:

| Endpoint | Returns |
|---|---|
| `/api/health` | app, Shoonya state, market status |
| `/api/ticker` | BANKNIFTY / NIFTY / VIX, P&L, alert counts, setup flag |
| `/api/overview` | indices Shoonya streams, VIX, FII/DII headline |
| `/api/banknifty` | the BANKNIFTY monitor |
| `/api/chain?und=NIFTY\|BANKNIFTY&n=15` | chain summary + rows around ATM |
| `/api/risk` | writer risk monitor |
| `/api/fii` | FII/DII read, setup, zones, signals, recent sessions |
| `/api/positions` | open positions, today's P&L, margin |
| `/api/news` | relevant headlines with impact, tags and tone |
| `/api/signals` | latest direction call per index, today's calls with grades, key levels, level breaks |
| `/api/global` | world markets, the read for India's open, global alerts |
| `/api/alerts` | active alerts with stable ids, including HIGH-impact news, level breaks, global moves and direction calls |

Read-only analytics, not investment advice.
