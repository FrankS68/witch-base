package de.witchcafe.ui.help;

import java.io.Serializable;

/**
 * Eine Hilfeseite aus {@code witch-help/*.md}.
 *
 * @param id       Teil der URL ({@code hilfe/<id>}), aus dem Dateinamen ohne Nummer und {@code .md}
 * @param title    erste Ueberschrift ({@code # ...}) der Datei, sonst die id
 * @param order    Nummer vor dem Dateinamen ({@code 10-erste-schritte.md} -> 10), sonst 1000
 * @param markdown Inhalt der Datei
 */
public record HelpPage(String id, String title, int order, String markdown) implements Serializable {

    /** URL der Seite relativ zur App, z. B. {@code hilfe/erste-schritte}. */
    public String path() {
        return HelpView.ROUTE + "/" + id;
    }
}
