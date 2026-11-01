package com.bptn.project.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

public class ConsoleInputTest {

    @Test
    public void readsLinesAndPrintsPrompt() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buf);
        ConsoleInput input = new ConsoleInput(
            new ByteArrayInputStream("hello\nworld\n".getBytes(StandardCharsets.UTF_8)), out);
        assertEquals("hello", input.readLine("Say: "));
        assertEquals("world", input.readLine("Again: "));
        assertTrue(buf.toString().contains("Say: "));
        assertTrue(buf.toString().contains("Again: "));
    }

    @Test
    public void returnsNullAtEof() {
        ConsoleInput input = new ConsoleInput(
            new ByteArrayInputStream(new byte[0]),
            new PrintStream(new ByteArrayOutputStream()));
        assertNull(input.readLine("Say: "));
    }
}
