package de.witchcafe.ui.impressum;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Inhalt des Impressums. Zwei Wege, der erste gewinnt:
 *
 * <ol>
 * <li>eigene Datei {@code src/main/resources/witch-ui/impressum.md} in der App (freier Text, Markdown)</li>
 * <li>Properties, daraus wird ein Impressum nach § 5 DDG / § 18 MStV gebaut:
 * <pre>
 * witch-ui.impressum.name=Vorname Nachname
 * witch-ui.impressum.address=Straße 1|12345 Ort        # Zeilen mit | trennen
 * witch-ui.impressum.email=kontakt@example.org
 * witch-ui.impressum.phone=+49 ...                     # optional
 * witch-ui.impressum.responsible=Vorname Nachname      # § 18 MStV, Default = name
 * witch-ui.impressum.text=...                          # optional, Markdown, zusaetzlicher Text
 * witch-ui.impressum.disclaimer=false                  # Haftungsausschluss weglassen (Default true)
 * </pre>
 * Unter die Angaben kommt der Haftungsausschluss aus {@value #DISCLAIMER} (in witch-ui; eine
 * Datei gleichen Namens in der App ersetzt ihn).
 * Auf dem Server gehen auch Umgebungsvariablen ({@code WITCH_UI_IMPRESSUM_NAME} ...),
 * so lassen sich die Angaben fuer alle Apps an einer Stelle pflegen.</li>
 * </ol>
 *
 * Abschalten mit {@code witch-ui.impressum.enabled=false}.
 */
@Component
public class Impressum {

    /** Eigene Impressum-Datei der App im Classpath. */
    public static final String RESOURCE = "witch-ui/impressum.md";

    /** Haftungsausschluss unter dem aus Properties gebauten Impressum. */
    public static final String DISCLAIMER = "witch-ui/impressum-haftung.md";

    private final boolean enabled;
    private final String name;
    private final String address;
    private final String email;
    private final String phone;
    private final String responsible;
    private final String text;
    private final boolean disclaimer;

    public Impressum(@Value("${witch-ui.impressum.enabled:true}") boolean enabled,
                     @Value("${witch-ui.impressum.name:}") String name,
                     @Value("${witch-ui.impressum.address:}") String address,
                     @Value("${witch-ui.impressum.email:}") String email,
                     @Value("${witch-ui.impressum.phone:}") String phone,
                     @Value("${witch-ui.impressum.responsible:}") String responsible,
                     @Value("${witch-ui.impressum.text:}") String text,
                     @Value("${witch-ui.impressum.disclaimer:true}") boolean disclaimer) {
        this.enabled = enabled;
        this.name = name.strip();
        this.address = address.strip();
        this.email = email.strip();
        this.phone = phone.strip();
        this.responsible = responsible.strip();
        this.text = text.strip();
        this.disclaimer = disclaimer;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** @return Impressum als Markdown, oder null, wenn weder Datei noch Name hinterlegt sind */
    public @Nullable String markdown() {
        String file = read(RESOURCE);
        if (file != null) {
            return file;
        }
        return name.isEmpty() ? null : fromProperties();
    }

    private static @Nullable String read(String path) {
        var resource = new ClassPathResource(path);
        if (!resource.exists()) {
            return null;
        }
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    String fromProperties() {
        List<String> postal = new ArrayList<>();
        postal.add(name);
        postal.addAll(lines(address));

        var md = new StringBuilder("# Impressum\n\n");
        md.append("## Angaben gemäß § 5 DDG\n\n").append(block(postal)).append("\n\n");

        List<String> contact = new ArrayList<>();
        if (!email.isEmpty()) {
            contact.add("E-Mail: [" + email + "](mailto:" + email + ")");
        }
        if (!phone.isEmpty()) {
            contact.add("Telefon: " + phone);
        }
        if (!contact.isEmpty()) {
            md.append("## Kontakt\n\n").append(block(contact)).append("\n\n");
        }

        List<String> responsiblePerson = new ArrayList<>();
        responsiblePerson.add(responsible.isEmpty() ? name : responsible);
        responsiblePerson.addAll(lines(address));
        md.append("## Verantwortlich für den Inhalt nach § 18 Abs. 2 MStV\n\n")
          .append(block(responsiblePerson)).append("\n\n");

        if (!text.isEmpty()) {
            md.append(text).append("\n\n");
        }
        String disclaimerText = disclaimer ? read(DISCLAIMER) : null;
        if (disclaimerText != null) {
            md.append(disclaimerText);
        }
        return md.toString();
    }

    private static List<String> lines(String value) {
        return Arrays.stream(value.split("\\||\\R"))
                .map(String::strip)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /** Zeilen mit Markdown-Zeilenumbruch (zwei Leerzeichen) verbinden. */
    private static String block(List<String> lines) {
        return String.join("  \n", lines);
    }
}
