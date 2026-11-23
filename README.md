# witch-base

Basis für neue WitchCafe-Projekte: Vaadin 25 + Spring Boot 4 + Login über [witch-auth](https://github.com/FrankS68/witch-auth) (OAuth2: Google, GitHub, Facebook, Microsoft …).

Das Repository enthält drei Maven-Module:

```
witch-parent/   Parent-POM fuer alle Apps: Versionen (Java, Spring Boot, Vaadin, witch-auth, witch-ui),
                Standard-Abhaengigkeiten, Plugin-Konfiguration, Repositories
witch-ui/       Bibliothek mit der gemeinsamen Oberflaeche (Paket de.witchcafe.ui)
  ├── MainLayout                     AppLayout mit Menü (aus @Menu), Login-Link / Benutzermenü
  ├── ViewToolbar                    Kopfzeile für Views
  ├── Responsive                     Helfer fürs Smartphone (Grid, Dialog, FormLayout, Breiten)
  ├── help/                          Hilfesystem: Markdown-Seiten unter witch-help/, Route hilfe, @Help
  ├── impressum/                     Impressum (Route impressum), aus Properties oder eigener Datei
  └── admin/
      ├── UserAdminView              Benutzerverwaltung (Rolle ADMIN)
      └── ProviderCredentialsView    Status der OAuth-Provider (Rolle ADMIN)
template/       Vorlage fuer neue Apps (Application, HomeView, styles.css)
```

`./mvnw install` im Wurzelverzeichnis baut alles und installiert `witch-parent` und `witch-ui`
ins lokale Maven-Repository.

### Eine App auf witch-parent aufsetzen

```xml
<parent>
    <groupId>de.witchcafe</groupId>
    <artifactId>witch-parent</artifactId>
    <version>1.1.0</version>
    <relativePath/>
</parent>

<dependencies>
    <dependency>
        <groupId>de.witchcafe</groupId>
        <artifactId>witch-ui</artifactId>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
        <plugin>
            <groupId>com.vaadin</groupId>
            <artifactId>vaadin-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>

<!-- damit Maven witch-parent selbst findet -->
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/FrankS68/witch-auth</url>
        <snapshots><enabled>true</enabled></snapshots>
    </repository>
</repositories>
```

Soll die App auch in sozialen Netzwerken lesen und posten, statt `witch-parent` den Parent
`de.witchcafe:witch-social-parent` aus [witch-social](https://github.com/FrankS68/witch-social)
nehmen und die Netzwerke als Module dazunehmen (`witch-bluesky`, `witch-mastodon`, `witch-meta`,
`witch-pinterest`).

Ein Upgrade von Vaadin, Spring Boot oder witch-auth passiert dann nur noch in `witch-parent`
(neue Version) und in jeder App durch Hochziehen der Parent-Version. Feste Versionen statt
`-SNAPSHOT`, damit sich nichts unbemerkt ändert.

### witch-ui konfigurieren (application.properties)

```properties
witch-ui.title=Meine App              # Kopfzeile; {name} = angemeldeter Benutzer (Default "Moin {name}")
witch-ui.admin.users.enabled=false    # eigene Benutzerverwaltung statt UserAdminView
witch-ui.admin.providers.enabled=false
witch-ui.impressum.enabled=false      # Impressum aus (Default an)
witch-ui.help.enabled=false           # Hilfe aus (Default an, aber nur wenn es Hilfeseiten gibt)
```

### Impressum

Jede App bekommt unter `impressum` eine Seite, ohne Anmeldung erreichbar und unten im Menü verlinkt.
Inhalt entweder aus einer eigenen Datei `src/main/resources/witch-ui/impressum.md` (Markdown) oder aus
Properties, daraus entsteht ein Impressum nach § 5 DDG und § 18 Abs. 2 MStV:

```properties
witch-ui.impressum.name=Vorname Nachname
witch-ui.impressum.address=Straße 1|12345 Ort     # Zeilen mit | trennen
witch-ui.impressum.email=kontakt@example.org
witch-ui.impressum.phone=+49 ...                  # optional
witch-ui.impressum.responsible=...                # optional, Default = name
witch-ui.impressum.text=...                       # optional, zusätzlicher Text (Markdown)
witch-ui.impressum.disclaimer=false               # Haftungsausschluss weglassen (Default an)
```

Unter die Angaben setzt witch-ui einen Haftungsausschluss (Inhalte, Links, Urheberrecht) aus
`witch-ui/impressum-haftung.md`; eine Datei gleichen Namens in der App ersetzt ihn.

Auf dem Server gehen auch Umgebungsvariablen (`WITCH_UI_IMPRESSUM_NAME`, `WITCH_UI_IMPRESSUM_ADDRESS` …),
z. B. in einer gemeinsamen `.env` für alle Docker-Apps.

### Hilfe

Hilfeseiten sind Markdown-Dateien unter `src/main/resources/witch-help/` – in der App und in jeder
Bibliothek (witch-social, witch-billing …), alle werden eingesammelt:

```
witch-help/10-erste-schritte.md   -> hilfe/erste-schritte   (Zahl = Reihenfolge)
witch-help/20-quiz-spielen.md     -> hilfe/quiz-spielen
witch-help/faq.md                 -> hilfe/faq              (ohne Zahl ans Ende)
```

Die erste Überschrift `# …` ist der Titel im Inhaltsverzeichnis. Links zwischen Seiten:
`[Quiz spielen](hilfe/quiz-spielen)`. Sobald es Seiten gibt, erscheinen unten im Menü der Link „Hilfe“
und rechts in der Kopfzeile ein `?`-Knopf. Mit `@Help("quiz-spielen")` an einer View öffnet der Knopf
dort direkt die passende Seite.

Die App muss `de.witchcafe` scannen (`@ComponentScan`, `@EnableVaadin`, `vaadin.allowed-packages`),
siehe `template/.../Application.java`.

### Smartphone (Responsive)

`MainLayout` lädt `assets/witch-ui/witch-ui.css`. Unter 600px Fensterbreite gilt die Handy-Ansicht
(`Responsive.MOBILE_MAX_WIDTH`, dieselbe Grenze steht im CSS). Statt fester Breiten in den Views:

```java
Responsive.maxWidth(suchfeld, "24em");                 // statt setWidth("24em")
Responsive.wrappingRow(zeile);                         // Eingabezeile bricht um, Felder passen sich an
Responsive.fit(filterBox);                             // feste Breite, aber nie breiter als der Platz
Responsive.dialog(dialog, "40em");                     // statt dialog.setWidth("40em")
Responsive.twoColumns(formLayout);                     // 1 Spalte, ab 32em Formularbreite 2
Responsive.hideOnMobile(grid, datumSpalte, autorSpalte);
Responsive.mobileCardColumn(grid, new ComponentRenderer<>(item -> karte(item)));
Responsive.hiddenOnMobile(button);                     // per CSS ausblenden
Responsive.onMobileChange(view, mobile -> ...);        // eigene Umschaltung, auch beim Drehen
```

CSS-Klassen für eigene Styles: `witch-hide-mobile`, `witch-mobile-only`, `witch-fit`.

## Neues Projekt daraus machen

1. Ordner `template/` kopieren (ohne `target/`, `node_modules/`) und umbenennen.
2. In `pom.xml` die `artifactId` anpassen und im `<parent>` `<relativePath/>` leer setzen;
   den `github`-Repository-Eintrag von oben ergänzen.
3. In `application.properties` `witch-auth.app-name`, `witch-auth.app-icon` und `witch-ui.title` setzen.
4. Eigene Features als Pakete unter `de.witchcafe.app` anlegen (z.B. `de.witchcafe.app.quiz`)
   mit Entity, Repository, Service und `ui/`-Views. Views mit `@Menu` erscheinen automatisch im Menü.
5. `HomeView` ersetzen oder anpassen.

## OAuth-Zugangsdaten

Secrets gehören nicht ins Repository. Zwei Wege:

- **Lokal:** `application-local.properties.example` nach `application-local.properties` kopieren
  und ausfüllen. Die Datei ist in `.gitignore` eingetragen und wird beim Start aus dem Arbeitsverzeichnis gelesen.
- **Server/Docker:** Umgebungsvariablen, z.B.
  `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID`, `..._CLIENT_SECRET`, `FIRST_ADMIN_EMAIL`.

Mindestens ein Provider muss konfiguriert sein. Der erste Login mit `witch-auth.first-admin-email` wird Admin.

## Starten

```bash
./mvnw
```

Für den Zugriff auf witch-auth aus GitHub Packages braucht `~/.m2/settings.xml` einen Server `github` mit Token (`read:packages`).

## Eclipse

Die `pom.xml` enthält schon die nötigen m2e-Workarounds (`maven-jar-plugin` 3.4.2, Vaadin-Frontend-Goals werden im Eclipse-Build übersprungen).

## Lizenz

[Apache License 2.0](LICENSE) – Copyright 2026 Frank Segieth.
