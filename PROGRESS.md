# Stato del progetto (Native)

## Obiettivi raggiunti
- [x] Transizione da Tauri a Kotlin Nativo (risoluzione crash WebView).
- [x] Implementazione UI con Jetpack Compose for Wear OS.
- [x] Internazionalizzazione completa (Italiano/Inglese) in tutti i componenti.
- [x] Conferma compatibilità estesa con ecosistema Wear OS (non solo TicWatch 3).
- [x] Lettura sensore cardio nativa.
- [x] Gestione vibrazione nativa.
- [x] Slider ad alta precisione (step 1 BPM).
- [x] Salvataggio persistente del RHR (SharedPreferences).
- [x] Makefile semplificato per build/run rapido.
- [x] Implementazione "Regola dei due minuti" per PEM pacing (allarme dopo 120s sopra soglia).
- [x] Logica di recupero (allerta attiva fino a RHR + 10).
- [x] Gestione intelligente dei picchi (spike) per evitare falsi positivi.
- [x] Vibrazioni progressive (discreta -> insistente) per allerta PEM.
- [x] Unificazione etichette App, Tile e Quadrante ("e4heart").
- [x] Ottimizzazione luminosità notturna in Ambient Mode (grigi attenuati anti-bagliore).
- [x] Sincronizzazione totale stato di allerta tra vibrazione e colori (UI + Quadrante).
- [x] Watchdog automatico per ripristino sensore cardio in caso di sospensione.
- [x] Ottimizzazione estrema consumo batteria (Hardware Batching sensore).
- [x] Architettura reattiva UI (`StateFlow`) al posto del polling.
- [x] Aggiornamento intelligente di Tile e Complication (basato su delta BPM).

## Prossimi passi
- [ ] Aggiunta di un grafico storico del battito.

---
*Documentazione e i18n aggiornati da Gemini CLI.*
