package de.witchcafe.ui.help;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Kontexthilfe: Der Hilfe-Knopf ({@code ?}) in der Kopfzeile des {@code MainLayout} oeffnet
 * auf dieser View die angegebene Hilfeseite statt der Uebersicht.
 *
 * <pre>
 * &#64;Route("quiz")
 * &#64;Help("quiz-spielen")      // witch-help/20-quiz-spielen.md
 * public class QuizView extends Main { ... }
 * </pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Help {

    /** id der Hilfeseite (Dateiname ohne Nummer und {@code .md}). */
    String value();
}
