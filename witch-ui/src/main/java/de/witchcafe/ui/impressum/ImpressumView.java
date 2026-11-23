package de.witchcafe.ui.impressum;

import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Impressum, ohne Anmeldung erreichbar unter {@code impressum}. Verlinkt unten im Menue
 * des {@code MainLayout}. Inhalt siehe {@link Impressum}.
 */
@Route(ImpressumView.ROUTE)
@PageTitle("Impressum")
@AnonymousAllowed
public class ImpressumView extends Main {

    public static final String ROUTE = "impressum";

    public ImpressumView(Impressum impressum) {
        addClassName("witch-text-page");
        String markdown = impressum.markdown();
        if (markdown != null) {
            add(new Markdown(markdown));
        } else {
            add(new Markdown("# Impressum"),
                new Paragraph("Das Impressum ist noch nicht hinterlegt "
                        + "(witch-ui.impressum.* oder " + Impressum.RESOURCE + ")."));
        }
    }
}
