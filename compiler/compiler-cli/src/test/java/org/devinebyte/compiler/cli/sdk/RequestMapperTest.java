package org.devinebyte.compiler.cli.sdk; // Fixed: org not com
import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;

import org.devinebyte.compiler.cli.options.CliOptions; // Fixed: org not com
import org.devinebyte.compiler.api.request.CompilerRequest; // Fixed: org.devinebyte.compiler.api.*
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestMapperTest {

    @Test
    void shouldMapCliOptionsToCompilerRequest() {

        
        CliOptions options = new CliOptions(
            Path.of("src"), 
            Path.of("build"), 
            true, 
            Map.of() // flags/props
        );

        RequestMapper mapper = new RequestMapper();

        CompilerRequest request = mapper.compileRequest(options);

        assertEquals(Path.of("src"), request.input());
        assertEquals(Path.of("build"), request.output());
        // assertEquals(true, request.incremental()); // Add if field exists
    }
}
