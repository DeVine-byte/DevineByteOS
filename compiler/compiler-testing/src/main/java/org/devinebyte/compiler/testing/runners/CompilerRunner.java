package org.devinebyte.compiler.testing.runners;

import com.devinebyte.compiler.sdk.CompilerSDK;
import com.devinebyte.compiler.sdk.Result;
import com.devinebyte.compiler.sdk.Session;
import com.devinebyte.compiler.sdk.Request;

public final class CompilerRunner {

    public Result run(Session session,
                      Request request) {

        return session.compile(request);

    }

}
