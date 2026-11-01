package com.bptn.project;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * Smoke test: the app boots, shows the menu, and exits cleanly.
 */
public class AppTest {

    @Test
    public void mainRunsMenuAndExits() {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream("9\n".getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(buffer));
            App.main(new String[0]);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
        String out = buffer.toString();
        assertTrue(out.contains("Local Complementary/Birthday Card & Message Signer"));
        assertTrue(out.contains("Goodbye!"));
    }

    @Test
    public void appCanBeInstantiated() {
        new App();
    }

    @Test
    public void mainExitsCleanlyOnEof() {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        try {
            System.setIn(new ByteArrayInputStream(new byte[0]));
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            App.main(new String[0]);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }
}
