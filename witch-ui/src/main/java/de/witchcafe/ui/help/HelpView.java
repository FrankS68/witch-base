package de.witchcafe.ui.help;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import org.jspecify.annotations.Nullable;

/**
 * Hilfe: links (auf dem Handy oben) das Inhaltsverzeichnis aller {@link HelpPages},
 * rechts die gewaehlte Seite als Markdown.
 *
 * <ul>
 * <li>{@code hilfe} - erste Seite</li>
 * <li>{@code hilfe/<id>} - Seite {@code witch-help/[nn-]<id>.md}</li>
 * </ul>
 *
 * Links zwischen Hilfeseiten im Markdown: {@code [Quiz spielen](hilfe/quiz-spielen)}.
 */
@Route(HelpView.ROUTE)
@AnonymousAllowed
public class HelpView extends Main implements HasUrlParameter<String>, HasDynamicTitle {

    public static final String ROUTE = "hilfe";

    private final HelpPages pages;
    private final SideNav toc = new SideNav();
    private final Div content = new Div();
    private String title = "Hilfe";

    public HelpView(HelpPages pages) {
        this.pages = pages;
        addClassName("witch-help");

        toc.setLabel("Hilfe");
        toc.setCollapsible(true);
        toc.addClassName("witch-help-toc");
        pages.all().forEach(page -> toc.addItem(new SideNavItem(page.title(), page.path())));

        content.addClassName("witch-help-content");
        add(toc, content);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter @Nullable String id) {
        content.removeAll();
        var page = id == null ? pages.first() : pages.find(id);
        if (page.isPresent()) {
            title = "Hilfe: " + page.get().title();
            content.add(new Markdown(page.get().markdown()));
        } else {
            title = "Hilfe";
            content.add(new Paragraph("Diese Hilfeseite gibt es nicht. Bitte links eine Seite auswählen."));
        }
    }

    @Override
    public String getPageTitle() {
        return title;
    }
}
