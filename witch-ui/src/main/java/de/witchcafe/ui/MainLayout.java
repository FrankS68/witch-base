package de.witchcafe.ui;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;

import de.witchcafe.auth.AppUserInfo;
import de.witchcafe.auth.CurrentUser;
import de.witchcafe.auth.oauth2.OAuthLoginView;
import de.witchcafe.ui.help.Help;
import de.witchcafe.ui.help.HelpPages;
import de.witchcafe.ui.help.HelpView;
import de.witchcafe.ui.impressum.Impressum;
import de.witchcafe.ui.impressum.ImpressumView;

/**
 * Gemeinsames App-Layout: Kopfzeile, Menue aus allen {@code @Menu}-Views (nach
 * Zugriffsrechten gefiltert, gruppiert nach {@link MenuGroup}) und unten im Drawer
 * Login-Link bzw. Benutzermenue.
 *
 * Der DrawerToggle sitzt nur hier neben dem Anwendungstitel, nicht im {@link ViewToolbar}.
 *
 * Titel in der Kopfzeile ueber {@code witch-ui.title} (application.properties);
 * {@code {name}} wird durch den Namen des angemeldeten Benutzers bzw. "Gast" ersetzt.
 *
 * Laedt {@code witch-ui.css} mit den Regeln fuer schmale Bildschirme, siehe {@link Responsive}.
 *
 * Ganz unten im Drawer stehen die Links "Hilfe" und "Impressum" (sofern eingeschaltet, siehe
 * {@link HelpPages} und {@link Impressum}); bei eingeschalteter Hilfe zusaetzlich ein
 * {@code ?}-Knopf rechts in der Kopfzeile, der die zur View passende Seite oeffnet ({@link Help}).
 *
 * Die {@link ViewToolbar} der aktuellen View (Seitentitel und Aktionen) wandert nach jedem
 * Seitenwechsel in die Kopfzeile rechts neben den Anwendungstitel, damit oben kein Platz
 * verschenkt wird. Auf dem Handy bricht sie dort in eine zweite Zeile um. Abschalten mit
 * {@code witch-ui.toolbar-in-header=false}.
 */
@Layout
@StyleSheet("assets/witch-ui/witch-ui.css")
@AnonymousAllowed
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private final AuthenticationContext authenticationContext;
    private final HelpPages helpPages;
    private final Impressum impressum;
    private @Nullable Button helpButton;
    private String helpTarget = HelpView.ROUTE;
    private final boolean toolbarInHeader;
    /** Platz in der Kopfzeile fuer die ViewToolbar der aktuellen View. */
    private final Div viewBar = new Div();
    /** View, deren Toolbar gerade in {@link #viewBar} steht. */
    private @Nullable Component viewBarOwner;

    public MainLayout(CurrentUser currentUser, AuthenticationContext authenticationContext,
                      HelpPages helpPages, Impressum impressum,
                      @Value("${witch-ui.title:Moin {name}}") String title,
                      @Value("${witch-ui.toolbar-in-header:true}") boolean toolbarInHeader) {
        this.toolbarInHeader = toolbarInHeader;
        this.authenticationContext = authenticationContext;
        this.helpPages = helpPages;
        this.impressum = impressum;

        // 1. Navbar Bereich (oben)
        createHeader(currentUser, title);

        // 2. Drawer Bereich (seitlich / mobil einklappbar)
        createDrawer();
        addToDrawer(currentUser.get().map(this::createUserMenu).orElseGet(this::createLoginLink));
        createFooter();
    }

    private void createHeader(CurrentUser currentUser, String title) {
        String name = currentUser.get().map(AppUserInfo::getFullName).orElse("Gast");
        H1 logo = new H1(title.replace("{name}", name));
        // witch-title: auf dem Handy kleiner und mit "…" gekuerzt statt ueberzulaufen
        logo.addClassName("witch-title");

        // Der DrawerToggle ist der "Hamburger"-Button für Mobilgeräte
        // addToNavbar(true, ...) bedeutet: optimiert für Touch/Mobil
        addToNavbar(true, new DrawerToggle(), logo);

        // Platz fuer Titel und Aktionen der aktuellen View (siehe afterNavigation)
        viewBar.addClassName("witch-view-bar");
        addToNavbar(true, viewBar);

        if (helpPages.isEnabled()) {
            helpButton = new Button(new Icon(VaadinIcon.QUESTION_CIRCLE_O),
                    e -> UI.getCurrent().navigate(helpTarget));
            helpButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ICON);
            helpButton.addClassName("witch-help-button");
            helpButton.setAriaLabel("Hilfe");
            helpButton.setTooltipText("Hilfe");
            addToNavbar(true, helpButton);
        }
    }

    /**
     * Nach jedem Seitenwechsel: ViewToolbar der View in die Kopfzeile holen und den Hilfe-Knopf
     * auf die Seite aus {@link Help} der aktuellen View setzen (sonst Hilfe-Startseite).
     */
    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        Component view = getContent();
        if (toolbarInHeader) {
            moveToolbarToHeader(view);
        }
        if (helpButton == null) {
            return;
        }
        Help help = view == null ? null : view.getClass().getAnnotation(Help.class);
        helpTarget = help == null ? HelpView.ROUTE
                : helpPages.find(help.value()).map(p -> p.path()).orElse(HelpView.ROUTE);
    }

    /**
     * Holt die erste {@link ViewToolbar} der View in die Kopfzeile. Bleibt dieselbe View-Instanz
     * (z. B. nur ein anderer URL-Parameter), bleibt auch ihre Toolbar stehen.
     */
    private void moveToolbarToHeader(@Nullable Component view) {
        if (view != null && view == viewBarOwner) {
            return;
        }
        viewBar.removeAll();
        viewBarOwner = null;
        findToolbar(view).ifPresent(toolbar -> {
            viewBar.add(toolbar);
            viewBarOwner = view;
        });
    }

    /** Sucht die erste ViewToolbar in den obersten Ebenen der View (Breitensuche, max. 3 Ebenen). */
    static Optional<ViewToolbar> findToolbar(@Nullable Component view) {
        if (view == null) {
            return Optional.empty();
        }
        Deque<Component> level = new ArrayDeque<>(List.of(view));
        for (int depth = 0; depth < 3 && !level.isEmpty(); depth++) {
            Deque<Component> next = new ArrayDeque<>();
            for (Component c : level) {
                if (c instanceof ViewToolbar toolbar) {
                    return Optional.of(toolbar);
                }
                c.getChildren().forEach(next::add);
            }
            level = next;
        }
        return Optional.empty();
    }

    /** Links "Hilfe" und "Impressum" unten im Drawer. */
    private void createFooter() {
        var footer = new Div();
        footer.addClassName("witch-drawer-footer");
        if (helpPages.isEnabled()) {
            footer.add(new Anchor(HelpView.ROUTE, "Hilfe"));
        }
        if (impressum.isEnabled()) {
            footer.add(new Anchor(ImpressumView.ROUTE, "Impressum"));
        }
        if (footer.getComponentCount() > 0) {
            addToDrawer(footer);
        }
    }

    private void createDrawer() {
        // Menue nach Gruppen (Anwendung, Social, Billing, ..., Admin), je Gruppe eine SideNav
        var menu = new VerticalLayout();
        menu.setPadding(false);
        menu.setSpacing(true);
        menu.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
        var groups = groupMenuEntries(MenuConfiguration.getMenuEntries());
        boolean withLabels = groups.size() > 1;
        groups.forEach((group, entries) -> menu.add(createSideNav(withLabels ? group : null, entries)));

        // Scroller sorgt dafür, dass das Menü scrollbar ist, wenn es zu viele Items sind
        Scroller scroller = new Scroller(menu);
        scroller.setClassName(LumoUtility.Padding.SMALL);

        addToDrawer(scroller);
    }

    /**
     * Teilt die (bereits nach Rechten gefilterten und sortierten) Menueeintraege in Gruppen auf.
     * Leere Gruppen fallen weg; Admin steht immer am Ende.
     */
    static Map<String, List<MenuEntry>> groupMenuEntries(List<MenuEntry> entries) {
        Map<String, List<MenuEntry>> groups = new LinkedHashMap<>();
        for (String group : List.of(MenuGroup.APP, MenuGroup.SOCIAL, MenuGroup.BILLING)) {
            groups.put(group, new ArrayList<>());
        }
        entries.forEach(entry -> groups.computeIfAbsent(groupOf(entry), g -> new ArrayList<>()).add(entry));
        List<MenuEntry> admin = groups.remove(MenuGroup.ADMIN);
        if (admin != null) {
            groups.put(MenuGroup.ADMIN, admin);
        }
        groups.values().removeIf(List::isEmpty);
        return groups;
    }

    static String groupOf(MenuEntry entry) {
        Class<?> view = entry.menuClass();
        if (view != null) {
            MenuGroup annotation = view.getAnnotation(MenuGroup.class);
            if (annotation != null) {
                return annotation.value();
            }
            String pkg = view.getPackageName();
            if (pkg.startsWith("de.witchcafe.social")) {
                return MenuGroup.SOCIAL;
            }
            if (pkg.startsWith("de.witchcafe.billing")) {
                return MenuGroup.BILLING;
            }
        }
        String path = entry.path() == null ? "" : entry.path().replaceFirst("^/+", "");
        if (path.equals("admin") || path.startsWith("admin/")) {
            return MenuGroup.ADMIN;
        }
        return MenuGroup.APP;
    }

    private SideNav createSideNav(String label, List<MenuEntry> entries) {
        var nav = new SideNav();
        if (label != null) {
            nav.setLabel(label);
            nav.setCollapsible(true);
        }
        entries.forEach(entry -> nav.addItem(createSideNavItem(entry)));
        return nav;
    }

	private SideNavItem createSideNavItem(MenuEntry menuEntry) {
	    if (menuEntry.icon() != null) {
	        return new SideNavItem(menuEntry.title(), menuEntry.path(), new Icon(menuEntry.icon()));
	    } else {
	        return new SideNavItem(menuEntry.title(), menuEntry.path());
	    }
	}

    private Component createUserMenu(AppUserInfo user) {
        var avatar = new Avatar(user.getFullName(), user.getPictureUrl());
        avatar.addThemeVariants(AvatarVariant.LUMO_XSMALL);
        avatar.addClassNames(LumoUtility.Margin.Right.SMALL);
        avatar.setColorIndex(5);

        var userMenu = new MenuBar();
        userMenu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        userMenu.addClassNames(LumoUtility.Margin.MEDIUM);

        var userMenuItem = userMenu.addItem(avatar);
        userMenuItem.add(user.getFullName());
        if (user.getProfileUrl() != null) {
            userMenuItem.getSubMenu().addItem("Profil anzeigen",
                    event -> UI.getCurrent().getPage().open(user.getProfileUrl()));
        }
        userMenuItem.getSubMenu().addItem("Abmelden", event -> authenticationContext.logout());

        return userMenu;
    }

    private Component createLoginLink() {
        // router-ignore: /login soll vom Server (Spring Security) ausgeliefert werden
        var login = new Anchor("/" + OAuthLoginView.LOGIN_PATH, "Anmelden");
        login.getElement().setAttribute("router-ignore", true);
        login.addClassNames(LumoUtility.Margin.MEDIUM);
        return login;
    }
}
