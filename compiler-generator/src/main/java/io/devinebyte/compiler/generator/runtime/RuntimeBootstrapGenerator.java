package io.devinebyte.compiler.generator.runtime;

import io.devinebyte.compiler.core.context.TenantContext;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

@Singleton
public class RuntimeBootstrapGenerator {

    public byte[] generate(TenantContext tenant, Path outDir) throws IOException {
        Files.createDirectories(outDir);
        
        // Placeholder: real prod would compile this to .class
        String bootstrap = """
            package tenant.%s.bootstrap;
            public class RuntimeBootstrap {
                public static void boot() {}
            }
            """.formatted(tenant.tenantId());
            
        byte[] bytes = bootstrap.getBytes(StandardCharsets.UTF_8);
        Files.write(outDir.resolve("RuntimeBootstrap.class"), bytes);
        return bytes;
    }
}
