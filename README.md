# heliosphere

TODOS:

* ~~think about how keys/hashMaps can be stored effectively -> Class HashedPasswordInfo?~~
* add key password definition at first start
* take care of todos in existing code
* make sure that all activities can only be accessed via the EnterPasswordActivity (not the case right now when reopening the application after a pause)
* add "back" buttons
* Messagesplitting for messages too long to fit into a single QR-code 
* (-> decode information about message splitting within the first QR-code ("message protocol"))
* check einbauen der prüft ob verwendetes passwort sicher ist -> entropie
* ~~hash application access password~~
* ~~add functions for encryption and decryption of data~~ (implemented in HelperFunctionsCrypto)



Implementierungsideen:
* "Selbstzerstörungsmechanismus" bei mehrmaliger falscher Passworteingabe
* erneute*r generierung und Austausch der Schlüssel nach bestimmter Zeit / bestimmter Anzahl an gesendeten Nachrihten
* Signature für Nachrichten und Schlüssel ( -> Recherche)
* check bei app start einbauen ob irgendeine art der kommunikation möglich ist (wifi, bluetooth, usb, nfc) -> wenn ja, dann alles löschen und beleidigungn anzeigen

Workflow:
	1.	passworteingabe: 
       -> notwendig für starten der App (wichtig)
	2.	Wenn Passwort richtig: Menü
      -> button 1: Scan Message and Description
      -> Button 2: Compiler Message and encrypt
      -> Button 3: Scan foreign public key
      -> Button 4: Show Own Public key

Schlüsselprobleme: 
* Speicherung von keys:echerche! Was sind vorhergesehen Workflows? Wie kann sicher gestellt werden, dass von nirgendwo außerhalb der App auf die Speicherorte der Schlüssel zugegriffen werden kann? 
* Footprint von public keys? 
* Über manuellen Abgleich oder über Signatur? 
-> eventuell erst für spätere Visionen
Für jetzige Version: Möglichkeit, eigenen public key und public key von anderen Personen im Klartext anzeigen zu können. 

Scan Protokoll:
Beim scannen wird im ersten QR Code angezeigt, wie viele QR Codes noch folgen werden und in welche Reihenfolge diese zusammengesetzt sind. 
* Überlegen, wie die Anzahl der enthaltenen QR Codes kodiert wird
