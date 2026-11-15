package de.witchcafe.ui.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.startup.ApplicationRouteRegistry;

/**
 * Erlaubt Apps, die Standard-Admin-Views von witch-ui abzuschalten, z.B. weil sie
 * eine eigene Benutzerverwaltung mit zusaetzlichen Rollen mitbringen:
 *
 * <pre>
 * witch-ui.admin.users.enabled=false       # UserAdminView (admin/users)
 * witch-ui.admin.providers.enabled=false   # ProviderCredentialsView (admin/oauth-providers)
 * </pre>
 *
 * Abgeschaltete Views verschwinden auch aus dem Menue.
 */
@Configuration
public class AdminViewsConfig {

    @Bean
    VaadinServiceInitListener witchUiAdminViews(
            @Value("${witch-ui.admin.users.enabled:true}") boolean usersEnabled,
            @Value("${witch-ui.admin.providers.enabled:true}") boolean providersEnabled) {
        return event -> {
            RouteConfiguration routes = RouteConfiguration.forRegistry(
                    ApplicationRouteRegistry.getInstance(event.getSource().getContext()));
            if (!usersEnabled) {
                routes.removeRoute(UserAdminView.class);
            }
            if (!providersEnabled) {
                routes.removeRoute(ProviderCredentialsView.class);
            }
        };
    }
}
