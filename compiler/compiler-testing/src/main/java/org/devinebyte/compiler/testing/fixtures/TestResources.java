package org.devinebyte.compiler.testing;

import java.net.URL;

public final class TestResources {

    private TestResources() {
    }

    public static URL resource(String name) {

        return TestResources.class
                .getClassLoader()
                .getResource(name);

    }

}
