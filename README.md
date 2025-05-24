In questo progetto la mappa non va per via dell'assenza di un API specifico di google. Questo API non è stato aggiunto perchè nel sito richiedeva dei dati della carta di credito e per motivi personali ho preferito fare a meno.
La parte di codice relativa alla mappa è ancora presente, nei post è possibile mettere le cordinate di dove è stata scattata la foto (funziona automatica dopo aver aderito ai permessi per la geocalizzazione).
Istruzioni su come modificare l'IP del progetto per farlo andare quando lo correggerà.
1)Ricavare il proprio Ip dal cmd dei comandi con il comando ipconfig e copiare l'indirizzo IPv4
2): Modifica l'IP nell'app Android
    -Per prima cosa bisogna andare in questo file app/src/main/java/com/example/triptales/di/NetworkModule.kt
    -Trovare questa riga e modificare l'IP con quello ricavato in precedenza  const val BASE_URL = "http://172.20.10.8:8000/"
3)Aggiorna la configurazione di rete
  -Per prima cosa bisogna andare in questo file app/src/main/res/xml/network_security_config.xml
  -Trovare questa riga <domain includeSubdomains="false">172.20.10.8</domain> sostituire l'Ip con quello trovato in precedenza
4)Aggiorna le impostazioni Django
 -Aprire questo file backend_triptales/settings.py
 -Trovare questa sezione CORS_ALLOWED_ORIGINS = [
    "http://172.20.10.8:8000",
    "https://172.20.10.8:8000",

]
-modificare l'Ip con quello ricavato in precedenza o aggiungere due righe come queste ma con il nuovo IP in caso si voglia mantenere l'IP vecchio
-da fare questa modifica anche in CSRF_TRUSTED_ORIGIN che si trova nello stesso file, consiglio di fare ctrl+f per una ricerca più rapida.
5)Testare
 -Usare il comando python manage.py runserver 192.168.1.100:8000 (con l'ip ricavato in precedenza) nel terminale per avviare il back-end. 
Con ngrok è possibile avviare una connesione tra app e server anche se in reti diverse.
