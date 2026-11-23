package de.witchcafe.ui;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.startup.ApplicationRouteRegistry;

import de.witchcafe.ui.help.HelpPages;
import de.witchcafe.ui.help.HelpView;
import de.witchcafe.ui.impressum.Impressum;
import de.witchcafe.ui.impressum.ImpressumView;

/**
 * Entfernt Impressum und Hilfe, wenn sie abgeschaltet sind:
 *
 * <pre>
 * witch-ui.impressum.enabled=false   # z. B. wenn die App ein eigenes Impressum hat
 * witch-ui.help.enabled=false        # Hilfe aus (ist auch aus, wenn es keine witch-help/*.md gibt)
 * </pre>
 */
@Configuration
public class PageViewsConfig {

    @Bean
    VaadinServiceInitListener witchUiPageViews(Impressum impressum, HelpPages helpPages) {
        return event -> {
            RouteConfiguration routes = RouteConfiguration.forRegistry(
                    ApplicationRouteRegistry.getInstance(event.getSource().getContext()));
            if (!impressum.isEnabled()) {
                routes.removeRoute(ImpressumView.class);
            }
            if (!helpPages.isEnabled()) {
                routes.removeRoute(HelpView.class);
            }
        };
    }
}
