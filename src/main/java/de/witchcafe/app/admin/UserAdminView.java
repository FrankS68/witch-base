package de.witchcafe.app.admin;

import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.witchcafe.auth.AppRoles;
import de.witchcafe.auth.domain.UserManagementService;
import de.witchcafe.auth.ui.UserAdminPanel;
import jakarta.annotation.security.RolesAllowed;

/**
 * Bindet das wiederverwendbare UserAdminPanel aus witch-auth in das
 * App-Layout ein.
 */
@Route("admin/users")
@PageTitle("Benutzerverwaltung")
@Menu(order = 99, icon = "vaadin:users", title = "Benutzer")
@RolesAllowed(AppRoles.ADMIN)
public class UserAdminView extends Main {

    public UserAdminView(UserManagementService service) {
        addClassName(LumoUtility.Padding.MEDIUM);
        add(new UserAdminPanel(service));
    }
}
