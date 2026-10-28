package com.bptn.project.render;

import com.bptn.project.model.Card;
import com.bptn.project.model.Signature;
import com.bptn.project.model.template.CardTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders a card as ASCII text inside a border defined by its template.
 * Every emitted line is exactly the template width.
 */
public class AsciiCardRenderer implements CardRenderer  {

    private static final int SIGNATURE_PREFIX = 4; // "  - " / "    "

    public String render(Card card) {
        CardTemplate template = card.getTemplate();
        int inner = template.getWidth() - 2;
        StringBuilder out = new StringBuilder();
        String border = String.valueOf(template.getBorderChar()).repeat(template.getWidth());

        out.append(border).append('\n');
        line(out, template, "");
        line(out, template, center(template.getBanner(), inner));
        line(out, template, center(template.decorationLine(), inner));
        line(out, template, "");
        for (String text : wrap("To: " + card.getRecipientName(), inner - 1)) {
            line(out, template, " " + text);
        }
        for (String text : wrap(
            "Turning " + card.getAge() + "  (" + card.getOccasion() + ")", inner - 1)) {
            line(out, template, " " + text);
        }
        line(out, template, "");
        line(out, template, " Wishes:");
        if (card.getSignatures().isEmpty()) {
            line(out, template, "   (no signatures yet)");
        } else {
            for (Signature signature : card.getSignatures()) {
                renderSignature(out, template, signature, inner);
            }
        }
        line(out, template, "");
        out.append(border).append('\n');
        return out.toString();
    }

    private void renderSignature(
        StringBuilder out, CardTemplate template, Signature signature, int inner) {
        String text = signature.getSignerName() + ": " + signature.getWish();
        List<String> wrapped = wrap(text, inner - SIGNATURE_PREFIX);
        for (int i = 0; i < wrapped.size(); i++) {
            String prefix = i == 0 ? "  - " : "    ";
            line(out, template, prefix + wrapped.get(i));
        }
    }

    /** Emits one bordered line; content must already fit the inner width. */
    private void line(StringBuilder out, CardTemplate template, String content) {
        int inner = template.getWidth() - 2;
        String padded = content.length() >= inner
            ? content.substring(0, inner)
            : content + " ".repeat(inner - content.length());
        out.append(template.getBorderChar()).append(padded)
            .append(template.getBorderChar()).append('\n');
    }

    /** Centers text within {@code width}; overlong text is truncated. */
    static String center(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        int left = (width - text.length()) / 2;
        return " ".repeat(left) + text + " ".repeat(width - text.length() - left);
    }

    /**
     * Word-wraps text so each returned line is at most {@code width}
     * characters. Words longer than the width are hard-split.
     */
    static List<String> wrap(String text, int width) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : text.split("\\s+")) {
            if (word.isEmpty()) {
                continue;
            }
            while (word.length() > width) {
                if (current.length() > 0) {
                    lines.add(current.toString());
                    current.setLength(0);
                }
                lines.add(word.substring(0, width));
                word = word.substring(width);
            }
            if (current.length() == 0) {
                current.append(word);
            } else if (current.length() + 1 + word.length() <= width) {
                current.append(' ').append(word);
            } else {
                lines.add(current.toString());
                current = new StringBuilder(word);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        if (lines.isEmpty()) {
            lines.add("");
        }
        return lines;
    }
}

