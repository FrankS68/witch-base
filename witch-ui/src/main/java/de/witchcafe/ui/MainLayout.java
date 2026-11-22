package de.witchcafe.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;

import org.springframework.beans.factory.annotation.Value;

import de.witchcafe.auth.AppUserInfo;
import de.witchcafe.auth.CurrentUser;
import de.witchcafe.auth.oauth2.OAuthLoginView;

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
 */
@Layout
@StyleSheet("assets/witch-ui/witch-ui.css")
@AnonymousAllowed
public class MainLayout extends AppLayout {

    private final AuthenticationContext authenticationContext;

    public MainLayout(CurrentUser currentUser, AuthenticationContext authenticationContext,
                      @Value("${witch-ui.title:Moin {name}}") String title) {
        this.authenticationContext = authenticationContext;

        // 1. Navbar Bereich (oben)
        createHeader(currentUser, title);

        // 2. Drawer Bereich (seitlich / mobil einklappbar)
        createDrawer();
        addToDrawer(currentUser.get().map(this::createUserMenu).orElseGet(this::createLoginLink));
    }

    private void createHeader(CurrentUser currentUser, String title) {
        String name = currentUser.get().map(AppUserInfo::getFullName).orElse("Gast");
        H1 logo = new H1(title.replace("{name}", name));
        // witch-title: auf dem Handy kleiner und mit "…" gekuerzt statt ueberzulaufen
        logo.addClassName("witch-title");

        // Der DrawerToggle ist der "Hamburger"-Button für Mobilgeräte
        // addToNavbar(true, ...) bedeutet: optimiert für Touch/Mobil
        addToNavbar(true, new DrawerToggle(), logo);
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
