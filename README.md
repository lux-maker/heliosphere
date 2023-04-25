# heliosphere

### TODOS:

* ~~think about how keys/hashMaps can be stored effectively -> Class HashedPasswordInfo?~~
* ~~add key password definition at first start~~
* take care of todos in existing code
* make sure that all activities can only be accessed via the EnterPasswordActivity (not the case right now when reopening the application after a pause)
* ~~add "back" buttons~~
* Messagesplitting for messages too long to fit into a single QR-code 
* (-> decode information about message splitting within the first QR-code ("message protocol"))
* check einbauen der prüft ob verwendetes passwort sicher ist -> entropie
* ~~hash application access password~~
* ~~add functions for encryption and decryption of data~~ (implemented in HelperFunctionsCrypto)



### Implementierungsideen:
* "Selbstzerstörungsmechanismus" bei mehrmaliger falscher Passworteingabe
* erneute*r generierung und Austausch der Schlüssel nach bestimmter Zeit / bestimmter Anzahl an gesendeten Nachrihten (eventuell kann schlüssel angehängt werden an nachricht ohne das der nutzer in irgendeiner weise tätig werden muss)
* Signature für Nachrichten und Schlüssel ( -> Recherche)
* check bei app start einbauen ob irgendeine art der kommunikation möglich ist (wifi, bluetooth, usb, nfc) -> wenn ja, dann alles löschen und beleidigungn anzeigen

### Workflow:
1. passworteingabe: 
- -> notwendig für starten der App (wichtig)
- -> bei erstmaligem öffnen (Passwortvergabe): Sicherheit des eingegeben Passworts checken und ggf. rejecten
2. Wenn Passwort richtig: Menü
- -> button 1: Scan Message and Description
- -> Button 2: Compiler Message and encrypt
- -> Button 3: Scan foreign public key
- -> Button 4: Show Own Public key

### Schlüsselprobleme: 
* Speicherung von keys:echerche! Was sind vorhergesehen Workflows? Wie kann sicher gestellt werden, dass von nirgendwo außerhalb der App auf die Speicherorte der Schlüssel zugegriffen werden kann? 
* Footprint von public keys? 
* Über manuellen Abgleich oder über Signatur? 
-> eventuell erst für spätere Visionen
Für jetzige Version: Möglichkeit, eigenen public key und public key von anderen Personen im Klartext anzeigen zu können. 

## QR Code Basisc
* Der QR Code enthält den kodierten Inhalt zusammen mit Redundanzen und Check-Summen, um die Fehleranfälligkeit zu minimieren
    * Level H: 30% Beschädigung möglich
    * Level Q: 25% Beschädigung möglich
    * Level M: 15% Beschädigung möglich
    * Level L: 7% Beschädigung möglich (Standard Einstellung)
* maximale Anzahl Elemente (quadrate) ist 177x177
    -> bei Fehlerlevel L entspricht das 23.648 Bit (7089 Dezimalziffern, 4296 alphanumerische Zeichen)
    -> 177x177 elemente sehen auf dem bildschirm so aus:
    ![Alt text](https://github.com/lux-maker/heliosphere/blob/master/Screenshot%20from%202023-04-25%2013-32-25.png?raw=true "Title")

## Scan Protokoll:
Beim scannen wird im ersten QR Code angezeigt, wie viele QR Codes noch folgen werden und in welche Reihenfolge diese zusammengesetzt sind. 
* Überlegen, wie die Anzahl der enthaltenen QR Codes kodiert wird

Variante 1.0: 
* Jedem chiffrat wird pro QR Code  eine zweistellige Nummer vorangestellt. (PositionNumber)
* Diese Nummer enhält infos über Gesammtanzahl und die jeweiligen Position des einzelnen QR-Codes 

| PositionNumber | absolute Anzahl(P), Position (p) |
| ------------- | ------------- |
| 01  | 1,1  |
| 02  | 2,1  |
| 03  | 2,2  |
| 04  | 3,1  |
| 05  | 3,2  |
| 06  | 3,3  |
| 07  | 4,1  |
| 08  | 4,2  |
| ... | ...  |
| 89  | 13,11  |
| 90  | 13,12  |
| 91  | 13,13  |
| 99 | public key |

* Damit sind pro Nachricht max 13 QR Codes möglich. Ich denke das reicht. 
* Die Zahl 99 ist frei zur Belegung. Ich schlage vor, wir nutzen diese Zahl um anzuzeigen dieser QR-Code enhält einen public key für den Schlüsselaustausch
* Iterativer Berechnungsweg:
```
public static int[] getQRPosition(int PositionNumber) {
  	int rest = 0;
	int P = 0;
        for(;  PositionNumber >= (rest +1) ; P++){
		rest= rest + P; // (lux) ich denke hier müsste das abbruchkriterium leicht verändert werden damit die letzte erhöhung von rest nicht statt findet, oder analytischer prozess siehe unten?
        } 
	int[] QRPostion = {P,PositionNumber - rest}; //[absolute Anzahl(P), Position (p)]
	return QRPosition;
	
}
```

* analytischer Berechnungsweg:
```
public static int[] getQRPosition(int PositionNumber) {
	// gaußsche Summenformel: 1 + 2 + 3 + ... + n = ((n+1)*n) / 2 = PNfull
	// PNfull ist die Positionsnummer jeweils bei vollen "Reihen"
	// n ist die volle Reihe und P = ceil(n)
	// p ist die differenz zwischen PositionsNummer und der positionsnummer der niedrigeren vollen "Reihe"
	
	int P = ceil(-0.5 + sqrt(0.25 + PositionNumber * 2)); //(paule) muss hier nicht PositionNumber anstelle von PNfull verwendet werden? (lux) jap
	int p = (((P+1)*P) / 2) - PositionNumber;
	
	return {P,p};
}

public static int computeQRPosition(int[] positionTuple)
{
	int P = positionTuple[0];
	int p = positionTUple[1];
	
	return ((P-1)+1)*(P-1) / 2 + p;
}

```
## Konzept: fatal Error / Critical Vulnerability Detection (CVD)
* Sicherheitskonzept 
* Wir können in unserer Codes immer mal Sanity checks einbauen. 
* Diese können einen fatal Error triggern. 
* fatal Error löscht / überschreibt alle Daten sofort und schließt die App. 
* Wir gehen davon aus, dass ein fatal Error nur eintritt, wenn eine Person von außen versucht die App zu manipulieren = Angriff. 
* Da sich für das offline Handy die externen Faktoren nie ändern werden, können wir uns sicher sein, dass die fatal Errors nur zu besorgniserweckenden Aktionen passieren. 
* sollten wir auf jeden einbauen, würde es aber unter anderm namen laufen lassen (sowas wie "Critical Vulnerability Detection") weil fatal Error meiner Aufassung nach einen Fehler im Programm beschreibt, während das Löschen aller Daten in bestimmten Situationen eine einwandfreie Funktionalität darstellt (ist vielleicht Haarspalterei aber sonst kommen wir eventuell in der kommunikation durcheinander ;) )
