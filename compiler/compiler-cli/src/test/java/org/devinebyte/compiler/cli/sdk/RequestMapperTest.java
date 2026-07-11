package org.devinebyte.compiler.cli.sdk;

import org.devinebyte.compiler.cli.options.CliOptions;
import org.devinebyte.sdk.CompilerContext;
import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Session;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class RequestMapperTest {

    @Test
    void compileRequestMapsSessionAndOptions() {

        Path input = Path.of("project.db");
        Path output = Path.of("build");

        CliOptions options = new CliOptions();
        options.setInput(input);

        CompilerContext context = mock(CompilerContext.class);

        Session session = mock(Session.class);
        when(session.getContext()).thenReturn(context);
        when(session.getOutputDirectory()).thenReturn(output);
        when(session.isIncremental()).thenReturn(true);

        RequestMapper mapper = new RequestMapper();

        Request request = mapper.compileRequest(session, options);

        assertEquals(input, request.getSourceFile());
        assertEquals(output, request.getOutputDirectory());
        assertSame(context, request.getContext());
        assertEquals(true, request.isIncremental());
    }
}
