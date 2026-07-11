package org.devinebyte.compiler.cli.sdk;

import org.devinebyte.compiler.cli.options.CliOptions;
import org.devinebyte.sdk.Session;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SessionFactoryTest {

    @Test
    void createsSessionFromCliOptions() {

        CliOptions options = new CliOptions();

        options.setInput(Path.of("project/src"));
        options.setOutput(Path.of("build"));
        options.setIncremental(true);
        options.setOptimize(true);

        SessionFactory factory = new SessionFactory();

        Session session = factory.create(options);

        assertNotNull(session);

        assertEquals(
                options.getInput().toAbsolutePath().getParent(),
                session.getProjectRoot()
        );

        assertEquals(
                options.getInput(),
                session.getSourceDirectory()
        );

        assertEquals(
                options.getOutput(),
                session.getOutputDirectory()
        );

        assertTrue(session.isIncremental());
        assertTrue(session.isOptimize());

        assertNotNull(session.getContext());
    }

    @Test
    void usesCurrentDirectoryWhenInputHasNoParent() {

        CliOptions options = new CliOptions();

        options.setInput(Path.of("project.db"));
        options.setOutput(Path.of("out"));

        SessionFactory factory = new SessionFactory();

        Session session = factory.create(options);

        assertEquals(Path.of("."), session.getProjectRoot());
    }

    @Test
    void disablesOptionalFlagsByDefault() {

        CliOptions options = new CliOptions();

        options.setInput(Path.of("src"));
        options.setOutput(Path.of("build"));

        Session session = new SessionFactory().create(options);

        assertFalse(session.isIncremental());
        assertFalse(session.isOptimize());
    }
}
