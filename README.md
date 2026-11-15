# witch-base

Basis für neue WitchCafe-Projekte: Vaadin 25 + Spring Boot 4 + Login über [witch-auth](https://github.com/FrankS68/witch-auth) (OAuth2: Google, GitHub, Facebook, Microsoft …).

Das Repository enthält drei Maven-Module:

```
witch-parent/   Parent-POM fuer alle Apps: Versionen (Java, Spring Boot, Vaadin, witch-auth, witch-ui),
                Standard-Abhaengigkeiten, Plugin-Konfiguration, Repositories
witch-ui/       Bibliothek mit der gemeinsamen Oberflaeche (Paket de.witchcafe.ui)
  ├── MainLayout                     AppLayout mit Menü (aus @Menu), Login-Link / Benutzermenü
  ├── ViewToolbar                    Kopfzeile für Views
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
    <version>1.0.0</version>
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

Ein Upgrade von Vaadin, Spring Boot oder witch-auth passiert dann nur noch in `witch-parent`
(neue Version) und in jeder App durch Hochziehen der Parent-Version. Feste Versionen statt
`-SNAPSHOT`, damit sich nichts unbemerkt ändert.

### witch-ui konfigurieren (application.properties)

```properties
witch-ui.title=Meine App              # Kopfzeile; {name} = angemeldeter Benutzer (Default "Moin {name}")
witch-ui.admin.users.enabled=false    # eigene Benutzerverwaltung statt UserAdminView
witch-ui.admin.providers.enabled=false
```

Die App muss `de.witchcafe` scannen (`@ComponentScan`, `@EnableVaadin`, `vaadin.allowed-packages`),
siehe `template/.../Application.java`.

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
