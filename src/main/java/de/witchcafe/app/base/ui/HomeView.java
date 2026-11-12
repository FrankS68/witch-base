package de.witchcafe.app.base.ui;

import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Startseite der Basis-Anwendung. In einem neuen Projekt durch die eigene
 * Startseite ersetzen.
 */
@Route("")
@PageTitle("Start")
@Menu(order = 0, icon = "vaadin:home", title = "Start")
@AnonymousAllowed
public class HomeView extends Main {

    public HomeView() {
        addClassName(LumoUtility.Padding.MEDIUM);
        add(new ViewToolbar("Start"));
        add(new Paragraph("Willkommen! Hier kommen die Views deiner Anwendung hin."));
    }
}
