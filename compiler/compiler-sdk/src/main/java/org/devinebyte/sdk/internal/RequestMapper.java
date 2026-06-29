package org.devinebyte.sdk.internal;

import org.devinebyte.compiler.core.CompilationContext;
import org.devinebyte.sdk.Request;

public final class RequestMapper {

    private RequestMapper() {
    }

    public static CompilationContext map(Request request) {

        return new CompilationContext();

    }

}
