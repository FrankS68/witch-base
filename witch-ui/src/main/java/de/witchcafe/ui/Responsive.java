package de.witchcafe.ui;

import java.io.Serializable;
import java.util.IdentityHashMap;
import java.util.Map;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.shared.Registration;

import org.jspecify.annotations.Nullable;

/**
 * Helfer fuer Oberflaechen, die auch auf dem Smartphone laufen sollen.
 * <p>
 * Die Grenze Handy/Desktop ist {@link #MOBILE_MAX_WIDTH}; dieselbe Grenze steht in
 * {@code witch-ui.css}. Dort gibt es dazu die CSS-Klassen {@code witch-hide-mobile},
 * {@code witch-mobile-only} und {@code witch-fit}.
 */
public final class Responsive {

    /** Unterhalb dieser Fensterbreite (in px) gilt die Handy-Ansicht. */
    public static final int MOBILE_MAX_WIDTH = 600;

    /** Ab dieser Breite des Formulars zeigt {@link #twoColumns(FormLayout)} zwei Spalten. */
    public static final String FORM_TWO_COLUMNS_FROM = "32em";

    private Responsive() {
    }

    /**
     * @return true, wenn das Browserfenster der aktuellen UI schmaler als
     *         {@link #MOBILE_MAX_WIDTH} ist (Stand beim Laden der Seite)
     */
    public static boolean isMobile() {
        UI ui = UI.getCurrent();
        return ui != null && isMobile(ui.getPage().getExtendedClientDetails().getWindowInnerWidth());
    }

    private static boolean isMobile(int windowWidth) {
        return windowWidth > 0 && windowWidth < MOBILE_MAX_WIDTH;
    }

    /**
     * Ruft {@code listener} auf, sobald {@code component} angezeigt wird, und danach bei
     * jedem Wechsel zwischen Handy- und Desktop-Breite (z. B. Drehen des Smartphones).
     *
     * @param listener bekommt {@code true} fuer Handy-Breite
     * @return Registration zum Abmelden
     */
    public static Registration onMobileChange(Component component, SerializableConsumer<Boolean> listener) {
        var state = new MobileState(listener);

        Registration attach = component.addAttachListener((AttachEvent e) -> state.attach(e.getUI()));
        Registration detach = component.addDetachListener(e -> state.detach());
        component.getUI().ifPresent(state::attach);

        return () -> {
            attach.remove();
            detach.remove();
            state.detach();
        };
    }

    /** Blendet die angegebenen Spalten auf Handy-Breite aus. */
    public static Registration hideOnMobile(Grid<?> grid, Grid.Column<?>... columns) {
        return onMobileChange(grid, mobile -> {
            for (var column : columns) {
                column.setVisible(!mobile);
            }
            grid.recalculateColumnWidths();
        });
    }

    /**
     * Fuegt eine Spalte hinzu, die nur auf Handy-Breite sichtbar ist und dort alle
     * anderen Spalten ersetzt, typischerweise eine kompakte Karte pro Zeile
     * ({@code new ComponentRenderer<>(item -> ...)}). Auf Desktop-Breite sind die
     * vorher sichtbaren Spalten wieder da.
     * <p>
     * Nicht zusammen mit {@link #hideOnMobile(Grid, Grid.Column...)} am selben Grid verwenden.
     */
    public static <T> Grid.Column<T> mobileCardColumn(Grid<T> grid, Renderer<T> renderer) {
        Grid.Column<T> card = grid.addColumn(renderer);
        card.setVisible(false);
        Map<Grid.Column<T>, Boolean> desktopVisibility = new IdentityHashMap<>();
        onMobileChange(grid, mobile -> {
            if (mobile) {
                desktopVisibility.clear();
                for (var column : grid.getColumns()) {
                    if (column != card) {
                        desktopVisibility.put(column, column.isVisible());
                        column.setVisible(false);
                    }
                }
            } else {
                desktopVisibility.forEach(Grid.Column::setVisible);
            }
            card.setVisible(mobile);
            grid.recalculateColumnWidths();
        });
        return card;
    }

    /**
     * Zeile (z. B. Eingabefelder mit Button), die bei wenig Platz umbricht. Alle Kinder
     * behalten ihre feste Breite, werden aber nie breiter als die Zeile ({@link #fit}).
     * Spaeter hinzugefuegte Kinder bei Bedarf selbst mit {@link #fit} versehen.
     */
    public static HorizontalLayout wrappingRow(HorizontalLayout row) {
        row.setWrap(true);
        row.getChildren()
                .filter(HasStyle.class::isInstance)
                .forEach(c -> fit((HasStyle) c));
        return row;
    }

    /**
     * Feste Breite behalten, aber nie breiter als der verfuegbare Platz
     * (CSS-Klasse {@code witch-fit}), z. B. fuer ein Feld mit {@code setWidth("28em")}.
     */
    public static void fit(HasStyle... components) {
        for (var component : components) {
            component.addClassName("witch-fit");
        }
    }

    /**
     * Ersatz fuer eine feste Breite: volle Breite, aber hoechstens {@code maxWidth}.
     * Statt {@code field.setWidth("24em")} also {@code Responsive.maxWidth(field, "24em")}.
     */
    public static <C extends HasSize> C maxWidth(C component, String maxWidth) {
        component.setWidthFull();
        component.setMaxWidth(maxWidth);
        return component;
    }

    /**
     * Dialog, der auf dem Desktop hoechstens {@code maxWidth} breit ist und auf dem Handy
     * die ganze Breite nutzt. Ersetzt {@code dialog.setWidth("40em")}.
     */
    public static Dialog dialog(Dialog dialog, String maxWidth) {
        dialog.setWidth("100%");
        dialog.setMaxWidth(maxWidth);
        return dialog;
    }

    /**
     * Formular einspaltig bei wenig Platz, zweispaltig ab {@link #FORM_TWO_COLUMNS_FROM}
     * (gemessen an der Breite des Formulars, nicht des Fensters).
     */
    public static FormLayout twoColumns(FormLayout form) {
        form.setResponsiveSteps(
                new ResponsiveStep("0", 1),
                new ResponsiveStep(FORM_TWO_COLUMNS_FROM, 2));
        return form;
    }

    /** Formular immer einspaltig, auf dem Desktop aber hoechstens {@code maxWidth} breit. */
    public static FormLayout oneColumn(FormLayout form, String maxWidth) {
        form.setResponsiveSteps(new ResponsiveStep("0", 1));
        return maxWidth(form, maxWidth);
    }

    /** Blendet die Komponente auf Handy-Breite aus (per CSS, ohne Server-Roundtrip). */
    public static <C extends Component & HasStyle> C hiddenOnMobile(C component) {
        component.addClassName("witch-hide-mobile");
        return component;
    }

    /** Zeigt die Komponente nur auf Handy-Breite (per CSS). */
    public static <C extends Component & HasStyle> C mobileOnly(C component) {
        component.addClassName("witch-mobile-only");
        return component;
    }

    /** Merkt sich pro Komponente, ob gerade Handy-Breite gilt, und meldet nur Wechsel. */
    private static final class MobileState implements Serializable {
        private final SerializableConsumer<Boolean> listener;
        private @Nullable Boolean mobile;
        private @Nullable Registration resize;

        MobileState(SerializableConsumer<Boolean> listener) {
            this.listener = listener;
        }

        void update(boolean nowMobile) {
            if (mobile == null || mobile != nowMobile) {
                mobile = nowMobile;
                listener.accept(nowMobile);
            }
        }

        void attach(UI ui) {
            detach();
            var details = ui.getPage().getExtendedClientDetails();
            if (details.getWindowInnerWidth() > 0) {
                update(isMobile(details.getWindowInnerWidth()));
            } else {
                update(false);
                details.refresh(d -> update(isMobile(d.getWindowInnerWidth())));
            }
            resize = ui.getPage().addBrowserWindowResizeListener(e -> update(isMobile(e.getWidth())));
        }

        void detach() {
            if (resize != null) {
                resize.remove();
                resize = null;
            }
        }
    }
}
