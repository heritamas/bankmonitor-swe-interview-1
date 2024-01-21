# Visszafelé kompatibilis  Spring Boot applikáció refaktorálása


## A feladat

Egy Spring Boot applikáció refaktorálása, úgy, hogy a régi kliensek működése ne változzon. Emellett 
szeretnénk, ha a kód minél jobban követné a Spring Boot ajánlásait, és a lehető legtöbb lehetőséget
biztosítaná a későbbi továbbfejlesztéshez.

## Kompatibilitás

Az egyik legfontosabb kikötés a régi kliensek működésének megőrzése volt. Nem tudjuk, hogy pontosan 
milyen viselkedésre számítanak a kliensek, de a következőket feltételezzük:

 * A kliensek a `/transactions/*` végponton keresztül kommunikálnak a szerverrel.
 * Ismerik a `Transaction` osztályt, és a annak (néha különös) viselkedését.
 * A `Transaction` osztályban a `data` mezőben egy JSON objektummá deszerializálható adat van.

Mivel ezen túlmenően további információval nem rendelkezünk, ezért a létező viselkedést a 
lehető legnagyobb mértékben megpróbáljuk megőrizni.

## Változtatások 

### `Transaction` osztály

Az eredeti `Transaction` osztályt csak minimálisan változtathatjuk meg, és gondosan vigyázunk rá hogy a 
viselkedése ne változzon. Az osztály kapott pár "szokásos" annotációt, illetve rendezve lett a 
JSON tartalmat feldolgozó metódusok szerkezete.

###  `Controller` osztályok

A régi `TransactionController` osztályt `@Deprecated` státuszt kapott, és két új osztályban
valósítjuk meg a __REST__ API-t. A `TransactionControllerV1` osztály a régi kliensek számára nyújt
kompatibilitást, a `TransactionControllerV2` osztály pedig a későbbi kliensek számára - ez utóbbi 
más domain és DTO osztályokat használ. A régi `TransactionController` controller egy egyszerű
_redirect_ az új `TransactionControllerV1` osztályra, ami a `/api/v1/transactions` végponton üzemel.

Az új kliensek számára a `TransactionControllerV2` osztály a `/api/v2/transactions` végponton működik.

###  `Service` osztály

A `TransactionService` az új klienseket szolgálja ki, és az egyik legföbb feladata, hogy a régi 
kliensek felé a kompatibilitást biztosítsa. A szolgáltatása lényegében egy absztakció a data store 
és a business entity-k között.

### Új `Entity` osztályok

Két új `Enitity` osztályt vezettünk be, a `TransactionData` és a `TransactionV2` osztályokat. A `TransactionData`
lényegében a régi `Transaction` osztály `data` mezőjének tartalmát reprezentálja, míg a `TransactionV2`
egyesíti a `TransactionData` és a `Transaction` osztályokat. A service rétegben levő szinkronizáció
biztosítja, hogy a `TransactionData` és a `Transaction` osztály `data` mezője  közötti konverzió mindig 
konzisztens legyen. Figyelni kell a szinkronizálás irányára. Az data store-ból történő lekérdezés során
a `data` mezőt tekintjük irányadónak, hiszen számítani lehet rá, hogy egy régi kliens az új adatmodell
ismerete nélkül módosította. Egy új `TransactionV2` entitás létrehozásakor természetesen a `TransactionData`
típusú mezőt tartalma az irányadó, hiszen egy ilyen entitás létrhozására csak az új, a modellt imserő
kliensek képesek.

### `DTO` osztályok

A __REST__ endpointokon `DTO` jellegű osztályok írják le a kliensek és a szerver közötti kommunikáció 
mikénjét. A `DTO`-k és az entity-k közötti konverziót a `Conversions` komponens végzi, egy megfelelően
konfigurált `ObjectMapper` segítségével. 

### JPA mapping

A `Transaction` és a `TransactionV2` entitások leképezésének kiindulópontja egyaránt a `transaction` tábla.
Ugyanakkor a `TransactionV2` entitás `@OneToOne` csatolva van a `TransactionData` entitáshoz, ami a viszont az
új `transactiondata` táblába képződik le. Ez a megoldás, bár nem igazán JPA konform, a régi kliensek 
számára teljes transzparenciát biztosít, hiszen a `transaction` táblában a `data` mezőben továbbra is csak 
olyan adat, és olyan módon van tárolva, ahogy azt a régi kliensek elvárnák. Az új kliensek számára pedig a 
megfelelő `Service` réteg biztosítja a megfelelő konverziót és szinkronizációt.

### Hibakezelés

A __REST__ egy request-reply protokoll. Akármi is történik a szerver oldalon, a kliens mindig egy választ kap.
Ez kézenfekvővé teszi, hogy az értelmes eredmények mellett a hibákat is értékek reprezentálják, az általában szokásos
kivételek helyett. A `Vavr`, `Scala` nyelvből kölcsönzött `Try` és `Either` osztályokat használjuk erre a célra.
A service réteg általában `Either<TranactionError, TransactionV2`-t ad vissza, ami egyaránt képes reprezentálni
a sikeres végrehajtást és a hibát. Ezt a típust a controller-ben alakítjuk át, a hagyomásnyos módon `ResponseEntity`-ké,
illetve Exception-ökké. Az `Exception`-öket végül egy `ControllerAdvice` osztályban képezzük le értelmezhető
hibaüzenetekké.

### Tesztelés

A végpont teszteléshez a `Spring Boot` által biztosított `MockMvc` keretrendszert használjuk. A többi teszthez a
teljes kontextust betöltő `@SpringBootTest` annotációt használjuk. Külön teszteket kapott az API kompatibilitásának
ellenőrzése.


## A megoldás kritikája

### Szinkronizáció

Nem elegáns (bár értelmes) hogy a `Transaction` és a `TransactionV2` osztály kapcsolata nincs kifejzeve a JPA
mappingek szintjén. Az adattartalom konzisztenciáját csak a service réteg biztosítja. Alternatív megoldás lehetne,
ha a `TransactionV2` leszármazna a `Transaction` osztályból, és a JPA-ban szokásos `JOINED` öröklségi stratégiát
alkalmaznánk. Előnyként az egyszerűbb perzisztencia jelentkezik. Ami ebben az esetben problémát jelenthetne az az, 
hogy a `Transaction` osztály `data` mezőjének  konzisztenciáját a többi mezővel még ebben az esetben sem lehetne 
csak a JPA mappingek szintjén biztosítani.

### Spring Boot konvenciók

Igazából nincs tapasztalatom a Spring Boot-tal, így a megoldásom javarészt a JavaEE világban megszokott módszereket
tükrözi. A tesztelést különösen gyengének érzem - szinte az összes tesztem a teljes kontextust betölti, nem 
élek a mock-olás lehetőségével.

### Deszerializáció

Kissé furcsának találtam, hogy a `Transaction` osztály `data` mezőjének tartalma egy JSON objektum szöveges 
reprezentációja, amit ráadásul a response osztályban meg is jelenítünk. Mivel ez a viselkedés adott, így nem
igazán lehet rajta változtatni. Annyit tudtam kísérletképpen tenni, hogy a `TransactionV2` `DTO`-jában a `data`
mező speciálisan szerializálódik és deszerializálódik (ld. a vonatkozó annotációkat). A `/api/v2/transactions`
endpointon így a válasz `DTO` egy teljes egészében értelmezhető JSON objektum, a `data` mező tartalma "data" 
kulcs alatt mint JSON objektum jelenik meg. Talán ez könnyebben kezelhető a kliensek számára.