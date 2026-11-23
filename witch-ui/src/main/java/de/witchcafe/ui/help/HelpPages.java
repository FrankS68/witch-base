package de.witchcafe.ui.help;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * Sammelt die Hilfeseiten aller Jars und der App: Markdown-Dateien unter
 * {@code src/main/resources/witch-help/}.
 *
 * <pre>
 * witch-help/10-erste-schritte.md   -> hilfe/erste-schritte, an 1. Stelle
 * witch-help/20-quiz-spielen.md     -> hilfe/quiz-spielen
 * witch-help/faq.md                 -> hilfe/faq, ohne Nummer ans Ende
 * </pre>
 *
 * Abschalten mit {@code witch-ui.help.enabled=false}. Ohne Hilfeseiten ist die Hilfe
 * ebenfalls aus (kein Knopf, keine Route).
 */
@Component
public class HelpPages {

    /** Ordner im Classpath, aus dem die Hilfeseiten gelesen werden. */
    public static final String LOCATION = "witch-help";

    private static final Logger log = LoggerFactory.getLogger(HelpPages.class);
    private static final Pattern FILE_NAME = Pattern.compile("(?:(\\d+)[-_ ])?(.+)\\.md");
    private static final Pattern HEADING = Pattern.compile("(?m)^#\\s+(.+?)\\s*#*\\s*$");

    private final boolean enabled;
    private final Map<String, HelpPage> pages;

    public HelpPages(@Value("${witch-ui.help.enabled:true}") boolean enabled) {
        this.pages = enabled ? load() : Map.of();
        this.enabled = enabled && !pages.isEmpty();
    }

    /** true, wenn die Hilfe eingeschaltet ist und es mindestens eine Seite gibt. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Alle Seiten in Anzeigereihenfolge. */
    public List<HelpPage> all() {
        return List.copyOf(pages.values());
    }

    public Optional<HelpPage> find(@Nullable String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(pages.get(id));
    }

    /** Erste Seite (Startseite der Hilfe). */
    public Optional<HelpPage> first() {
        return pages.values().stream().findFirst();
    }

    private static Map<String, HelpPage> load() {
        Resource[] resources;
        try {
            resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:" + LOCATION + "/*.md");
        } catch (IOException e) {
            log.warn("Hilfeseiten konnten nicht gelesen werden", e);
            return Map.of();
        }
        List<HelpPage> found = new ArrayList<>();
        for (Resource resource : resources) {
            HelpPage page = read(resource);
            if (page != null) {
                found.add(page);
            }
        }
        found.sort(Comparator.comparingInt(HelpPage::order).thenComparing(HelpPage::id));

        Map<String, HelpPage> result = new LinkedHashMap<>();
        for (HelpPage page : found) {
            if (result.putIfAbsent(page.id(), page) != null) {
                log.warn("Hilfeseite '{}' gibt es mehrfach, nur die erste wird angezeigt", page.id());
            }
        }
        log.info("{} Hilfeseite(n) geladen: {}", result.size(), result.keySet());
        return result;
    }

    static @Nullable HelpPage read(Resource resource) {
        String fileName = resource.getFilename();
        if (fileName == null) {
            return null;
        }
        Matcher name = FILE_NAME.matcher(fileName);
        if (!name.matches()) {
            return null;
        }
        try {
            String markdown = resource.getContentAsString(StandardCharsets.UTF_8);
            return page(name.group(1), name.group(2), markdown);
        } catch (IOException e) {
            log.warn("Hilfeseite {} nicht lesbar", fileName, e);
            return null;
        }
    }

    static HelpPage page(@Nullable String number, String id, String markdown) {
        int order = number == null ? 1000 : Integer.parseInt(number);
        Matcher heading = HEADING.matcher(markdown);
        String title = heading.find() ? heading.group(1) : id;
        return new HelpPage(id, title, order, markdown);
    }
}
