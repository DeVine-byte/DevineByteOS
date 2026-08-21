package io.devinebyte.runtime.workflow.engine;

import io.devinebyte.runtime.event.core.EventStore;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileWorkflowInstanceRepository extends WorkflowInstanceRepository {
    private final Path baseDir;

    public FileWorkflowInstanceRepository(EventStore store, Path baseDir) {
        super(store);
        this.baseDir = baseDir.resolve("workflows");
        try { Files.createDirectories(this.baseDir); } catch (IOException e) { throw new RuntimeException(e); }
    }
}
