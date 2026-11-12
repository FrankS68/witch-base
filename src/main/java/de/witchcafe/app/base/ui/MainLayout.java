package de.witchcafe.app.base.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;

import de.witchcafe.auth.AppUserInfo;
import de.witchcafe.auth.CurrentUser;
import de.witchcafe.auth.oauth2.OAuthLoginView;

@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout {

    private final AuthenticationContext authenticationContext;

    public MainLayout(CurrentUser currentUser, AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;

        // 1. Navbar Bereich (oben)
        createHeader(currentUser);

        // 2. Drawer Bereich (seitlich / mobil einklappbar)
        createDrawer();
        addToDrawer(currentUser.get().map(this::createUserMenu).orElseGet(this::createLoginLink));
    }

    private void createHeader(CurrentUser currentUser) {
        String name = currentUser.get().map(AppUserInfo::getFullName).orElse("Gast");
        H1 logo = new H1("Moin " + name);
        logo.addClassNames(
            LumoUtility.FontSize.LARGE,
            LumoUtility.Margin.MEDIUM);

        // Der DrawerToggle ist der "Hamburger"-Button für Mobilgeräte
        // addToNavbar(true, ...) bedeutet: optimiert für Touch/Mobil
        addToNavbar(true, new DrawerToggle(), logo);
    }

    private void createDrawer() {
        // Die Navigation als SideNav (neue Komponente in Vaadin 24)
        SideNav nav = createSideNav();

        // Scroller sorgt dafür, dass das Menü scrollbar ist, wenn es zu viele Items sind
        Scroller scroller = new Scroller(nav);
        scroller.setClassName(LumoUtility.Padding.SMALL);

        addToDrawer(scroller);
    }

	private SideNav createSideNav() {
	    var nav = new SideNav();
	    MenuConfiguration.getMenuEntries().forEach(entry -> nav.addItem(createSideNavItem(entry)));
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
