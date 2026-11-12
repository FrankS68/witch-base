package de.witchcafe.app.admin;

import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.witchcafe.auth.AppRoles;
import de.witchcafe.auth.ui.ProviderCredentialsPanel;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * Bindet das wiederverwendbare ProviderCredentialsPanel aus witch-auth in das
 * App-Layout ein.
 */
@Route("admin/oauth-providers")
@PageTitle("OAuth-Provider")
@Menu(order = 100, icon = "vaadin:key", title = "OAuth-Provider")
@RolesAllowed(AppRoles.ADMIN)
public class ProviderCredentialsView extends Main {

    public ProviderCredentialsView(ClientRegistrationRepository repository) {
        addClassName(LumoUtility.Padding.MEDIUM);
        add(new ProviderCredentialsPanel(repository));
    }
}
