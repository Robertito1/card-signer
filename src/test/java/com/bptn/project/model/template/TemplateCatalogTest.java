package com.bptn.project.model.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bptn.project.exception.ValidationException;
import org.junit.jupiter.api.Test;

public class TemplateCatalogTest {

    @Test
    public void catalogOffersThreeTemplates() {
        assertEquals(3, TemplateCatalog.all().size());
        assertTrue(TemplateCatalog.get(1) instanceof BirthdayCardTemplate);
        assertTrue(TemplateCatalog.get(2) instanceof BalloonCardTemplate);
        assertTrue(TemplateCatalog.get(3) instanceof BasicCardTemplate);
    }

    @Test
    public void getRejectsOutOfRange() {
        assertThrows(ValidationException.class, () -> TemplateCatalog.get(0));
        assertThrows(ValidationException.class, () -> TemplateCatalog.get(4));
    }

    @Test
    public void keyRoundTripsThroughCatalog() {
        for (CardTemplate t : TemplateCatalog.all()) {
            assertEquals(TemplateCatalog.keyOf(t),
                TemplateCatalog.keyOf(TemplateCatalog.byKey(TemplateCatalog.keyOf(t))));
        }
    }

    @Test
    public void byKeyRejectsUnknown() {
        assertThrows(ValidationException.class, () -> TemplateCatalog.byKey("NoSuchTemplate"));
    }

    @Test
    public void templatesHaveSensibleProperties() {
        for (CardTemplate t : TemplateCatalog.all()) {
            assertFalse(t.getName().isBlank());
            assertFalse(t.getBanner().isBlank());
            assertFalse(t.decorationLine().isBlank());
            assertTrue(t.getWidth() >= CardTemplate.MIN_WIDTH);
        }
        assertEquals('*', TemplateCatalog.get(1).getBorderChar());
        assertEquals('#', TemplateCatalog.get(2).getBorderChar());
        assertEquals('=', TemplateCatalog.get(3).getBorderChar());
    }

    @Test
    public void templateRejectsNarrowWidth() {
        assertThrows(IllegalArgumentException.class,
            () -> new CardTemplate("Tiny", '*', 10, "B") {
                @Override
                public String decorationLine() {
                    return "";
                }
            });
    }
}
