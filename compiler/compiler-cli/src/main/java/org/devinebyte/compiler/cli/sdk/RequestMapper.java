package org.devinebyte.compiler.cli.sdk;

import org.devinebyte.sdk.Request;
import org.devinebyte.sdk.Session;
import org.devinebyte.compiler.cli.options.CliOptions;

public class RequestMapper {
    
    public Request compileRequest(Session session, CliOptions options) {
        return Request.builder()
                .source(options.getInput()) // was getSourceFile()
                .build();
    }
}
