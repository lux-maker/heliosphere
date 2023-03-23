# heliosphere

TODOS:
* ~~think about how keys/hashMaps can be stored effectively -> Class HashedPasswordInfo?~~
* add key password definition at first start
* take care of todos in existing code
* make sure that all activities can only be accessed via the EnterPasswordActivity (not the case right now when reopening the application after a pause)
* add "back" buttons
* Messagesplitting for messages too long to fit into a single QR-code (-> decode information about message splitting within the first QR-code ("message protocol"))
* check einbauen der prüft ob verwendetes passwort sicher ist -> entropie
* ~~hash application access password~~
* ~~add functions for encryption and decryption of data~~ (implemented in HelperFunctionsCrypto)



Implementierungsideen:
* "Selbstzerstörungsmechanismus" bei mehrmaliger falscher Passworteingabe
* erneute*r generierung und Austausch der Schlüssel nach bestimmter Zeit / bestimmter Anzahl an gesendeten Nachrihten
* Signature für Nachrichten und Schlüssel ( -> Recherche)
