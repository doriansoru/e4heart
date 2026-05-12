# e4heart (Native Wear OS)

Applicazione nativa per Wear OS (TicWatch 3) sviluppata in Kotlin e Jetpack Compose per il monitoraggio della frequenza cardiaca in tempo reale.

## Funzionalità
- Monitoraggio continuo del battito cardiaco.
- Interfaccia ottimizzata per schermi circolari.
- **Internazionalizzazione**: Supporto completo per Italiano e Inglese (UI, Notifiche, Tile e Quadrante).
- Slider di precisione (step di 1 BPM) per la soglia di vibrazione.
- Persistenza della soglia (salvata all'uscita).
- Avvisi intelligenti via vibrazione: allarme immediato al superamento della soglia e promemoria ogni 30 secondi se il battito non scende (ottimizzato per PEM pacing).

## Sviluppo
Per compilare e avviare l'app:
```bash
make run
```

## Requisiti
- Android SDK (API 34)
- Qualsiasi orologio Wear OS con sensore cardiaco (testato su TicWatch 3, Pixel Watch, Galaxy Watch).

## ⚠️ Disclaimer Medico
Questa applicazione **non è un dispositivo medico**. I dati forniti sono solo a scopo informativo e di supporto al monitoraggio personale (es. PEM pacing). L'app non deve essere utilizzata per diagnosticare, trattare o prevenire alcuna patologia. Consulta sempre un medico professionista per decisioni riguardanti la tua salute.

## Licenza
Distribuito sotto licenza **GNU GPLv3**. Vedi il file `LICENSE` per i dettagli.

## Note sullo sviluppo
Questo progetto è assistito da **Gemini CLI**, che ha curato l'implementazione dell'internazionalizzazione, l'ottimizzazione del codice per la compatibilità Wear OS estesa e l'aggiornamento della documentazione.
