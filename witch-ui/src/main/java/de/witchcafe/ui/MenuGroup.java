package de.witchcafe.ui;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ordnet eine {@code @Menu}-View einer Gruppe im Menue des {@link MainLayout} zu.
 *
 * Ohne Annotation entscheidet {@link MainLayout} selbst: Views aus {@code de.witchcafe.social}
 * landen unter {@link #SOCIAL}, aus {@code de.witchcafe.billing} unter {@link #BILLING},
 * Routen unter {@code admin/} unter {@link #ADMIN}, alles andere unter {@link #APP}.
 *
 * Reihenfolge im Menue: Anwendung, Social, Billing, eigene Gruppen, Admin.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MenuGroup {

    String APP = "Anwendung";
    String SOCIAL = "Social";
    String BILLING = "Billing";
    String ADMIN = "Admin";

    /** Name der Gruppe, z.B. {@link #ADMIN} oder ein eigener Name. */
    String value();
}
