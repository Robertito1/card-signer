package com.bptn.project.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bptn.project.model.Card;
import com.bptn.project.model.Signature;
import com.bptn.project.model.template.BalloonCardTemplate;
import com.bptn.project.model.template.BirthdayCardTemplate;
import com.bptn.project.model.template.CardTemplate;
import java.util.List;
import org.junit.jupiter.api.Test;

public class AsciiCardRendererTest {

    private final AsciiCardRenderer renderer = new AsciiCardRenderer();

    private Card sampleCard() {
        return new Card("Alice", 30, "Birthday", new BirthdayCardTemplate());
    }

    @Test
    public void everyLineMatchesTemplateWidth() {
        Card card = sampleCard();
        card.addSignature(new Signature("Bob", "A wish that is definitely long enough to "
            + "wrap onto a second line inside the card"));
        for (String line : renderer.render(card).split("\n")) {
            assertEquals(card.getTemplate().getWidth(), line.length());
        }
    }

    @Test
    public void middleLinesAreBordered() {
        Card card = sampleCard();
        String[] lines = renderer.render(card).split("\n");
        for (int i = 1; i < lines.length - 1; i++) {
            assertTrue(lines[i].startsWith("*"));
            assertTrue(lines[i].endsWith("*"));
        }
    }

    @Test
    public void showsCardDetails() {
        String out = renderer.render(sampleCard());
        assertTrue(out.contains("HAPPY BIRTHDAY!"));
        assertTrue(out.contains("To: Alice"));
        assertTrue(out.contains("Turning 30 (Birthday)"));
    }

    @Test
    public void emptyCardShowsPlaceholder() {
        assertTrue(renderer.render(sampleCard()).contains("(no signatures yet)"));
    }

    @Test
    public void signaturesAreListedAndWrapped() {
        Card card = sampleCard();
        card.addSignature(new Signature("Bob", "short wish"));
        card.addSignature(new Signature("Caroline", "wishing you the very best on this "
            + "wonderful day and many more happy years to come"));
        String out = renderer.render(card);
        assertTrue(out.contains("- Bob: short wish"));
        assertTrue(out.contains("- Caroline: wishing you the very best"));
        assertTrue(out.contains("years to come"));
        for (String line : out.split("\n")) {
            assertEquals(card.getTemplate().getWidth(), line.length());
        }
    }

    @Test
    public void overlongWordIsHardSplit() {
        Card card = sampleCard();
        card.addSignature(new Signature("Bob", "x".repeat(80)));
        for (String line : renderer.render(card).split("\n")) {
            assertEquals(card.getTemplate().getWidth(), line.length());
        }
    }

    @Test
    public void overlongRecipientNameIsWrapped() {
        Card card = new Card("A".repeat(40), 30, "Birthday", new BirthdayCardTemplate());
        for (String line : renderer.render(card).split("\n")) {
            assertEquals(card.getTemplate().getWidth(), line.length());
        }
    }

    @Test
    public void differentTemplatesUseDifferentBorders() {
        Card balloon = new Card("Al", 5, "Party", new BalloonCardTemplate());
        String out = renderer.render(balloon);
        assertTrue(out.startsWith("#"));
        assertTrue(out.contains("LET'S CELEBRATE!"));
    }

    @Test
    public void wrapBreaksOnWordBoundaries() {
        assertEquals(List.of("hello world"), AsciiCardRenderer.wrap("hello world", 11));
        assertEquals(List.of("hello", "world"), AsciiCardRenderer.wrap("hello world", 5));
        assertEquals(List.of("ab", "cd"), AsciiCardRenderer.wrap("abcd", 2));
        assertEquals(List.of(""), AsciiCardRenderer.wrap("", 5));
        assertEquals(List.of("one"), AsciiCardRenderer.wrap("  one  ", 10));
    }

    @Test
    public void centerPadsBothSides() {
        assertEquals(" hi ", AsciiCardRenderer.center("hi", 4));
        assertEquals(" hi  ", AsciiCardRenderer.center("hi", 5));
        assertEquals("toolong", AsciiCardRenderer.center("toolongishere", 7));
    }

    @Test
    public void renderedExampleLooksLikeACard() {
        CardTemplate t = new BirthdayCardTemplate();
        Card card = new Card("Alice", 30, "Birthday", t);
        card.addSignature(new Signature("Bob", "Best wishes"));
        String[] lines = renderer.render(card).split("\n");
        assertEquals(String.valueOf(t.getBorderChar()).repeat(t.getWidth()), lines[0]);
        assertEquals(lines[0], lines[lines.length - 1]);
    }
}
