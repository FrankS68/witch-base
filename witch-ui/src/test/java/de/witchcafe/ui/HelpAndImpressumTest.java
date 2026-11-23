package de.witchcafe.ui;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import de.witchcafe.ui.help.HelpPages;
import de.witchcafe.ui.impressum.Impressum;

class HelpAndImpressumTest {

    @Test
    void helpPagesFromTestClasspathAreSortedAndTitled() {
        var pages = new HelpPages(true);
        assertThat(pages.isEnabled()).isTrue();
        assertThat(pages.all()).extracting(p -> p.id()).containsExactly("start", "zweite-seite", "faq");
        assertThat(pages.first().orElseThrow().title()).isEqualTo("Los geht's");
        assertThat(pages.find("faq").orElseThrow().title()).isEqualTo("faq");
        assertThat(pages.find("zweite-seite").orElseThrow().path()).isEqualTo("hilfe/zweite-seite");
    }

    @Test
    void helpCanBeSwitchedOff() {
        var pages = new HelpPages(false);
        assertThat(pages.isEnabled()).isFalse();
        assertThat(pages.all()).isEmpty();
    }

    @Test
    void impressumFromProperties() {
        var impressum = new Impressum(true, "Erika Muster", "Weg 1 | 12345 Ort", "e@example.org", "", "", "", true);
        String md = impressum.markdown();
        assertThat(md).contains("§ 5 DDG", "Erika Muster  \nWeg 1  \n12345 Ort",
                "mailto:e@example.org", "§ 18 Abs. 2 MStV", "## Haftung für Inhalte");
        assertThat(md).doesNotContain("Telefon");
    }

    @Test
    void disclaimerCanBeSwitchedOff() {
        var impressum = new Impressum(true, "Erika Muster", "", "", "", "", "", false);
        assertThat(impressum.markdown()).doesNotContain("Haftung");
    }

    @Test
    void impressumWithoutDataIsNull() {
        assertThat(new Impressum(true, "", "", "", "", "", "", true).markdown()).isNull();
    }
}
