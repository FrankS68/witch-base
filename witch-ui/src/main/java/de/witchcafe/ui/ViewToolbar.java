package de.witchcafe.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.jspecify.annotations.Nullable;

/**
 * Kopfzeile einer View: Seitentitel links, Aktionen rechts.
 *
 * Ohne eigenen DrawerToggle - den gibt es nur einmal im {@link MainLayout} neben dem Anwendungstitel.
 */
public final class ViewToolbar extends Composite<HorizontalLayout> {

    public ViewToolbar(@Nullable String viewTitle, Component... components) {
        var layout = getContent();
        layout.setPadding(true);
        layout.setWrap(true);
        layout.setWidthFull();
        layout.addClassName(LumoUtility.Border.BOTTOM);
        layout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        var title = new H1(viewTitle);
        title.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.NONE, LumoUtility.FontWeight.LIGHT);

        layout.add(title);
        layout.setFlexGrow(1, title);

        if (components.length > 0) {
            var actions = new HorizontalLayout(components);
            actions.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
            layout.add(actions);
        }
    }

    public static Component group(Component... components) {
        var group = new HorizontalLayout(components);
        group.setWrap(true);
        return group;
    }
}
