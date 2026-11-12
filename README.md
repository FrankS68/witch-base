# witch-base

Basis für neue WitchCafe-Projekte: Vaadin 25 + Spring Boot 4 + Login über [witch-auth](https://github.com/FrankS68/witch-auth) (OAuth2: Google, GitHub, Facebook, Microsoft …).

Enthalten ist alles, was jedes Projekt braucht, aber keine fachlichen Entities, Services oder Views:

```
src/main/java/de/witchcafe/app
├── Application.java          Spring-Boot-Start, scannt de.witchcafe (App + witch-auth)
├── base/ui
│   ├── MainLayout.java       AppLayout mit Menü (aus @Menu), Login-Link / Benutzermenü
│   ├── ViewToolbar.java      Kopfzeile für Views
│   └── HomeView.java         Platzhalter-Startseite ("/")
└── admin
    ├── UserAdminView.java            Benutzerverwaltung (Rolle ADMIN)
    └── ProviderCredentialsView.java  Status der OAuth-Provider (Rolle ADMIN)
```

## Neues Projekt daraus machen

1. Ordner kopieren (ohne `target/`, `node_modules/`, `.git/`) und umbenennen.
2. In `pom.xml` die `artifactId` (und ggf. `groupId`) anpassen.
3. In `application.properties` `witch-auth.app-name` und `witch-auth.app-icon` setzen.
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
