package io.devinebyte.runtime.event.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.devinebyte.runtime.core.context.TenantContext;
import io.devinebyte.runtime.event.core.EventStore;
import io.devinebyte.runtime.event.diagnostics.EventDiagnostics;
import io.devinebyte.runtime.event.model.DomainEvent;
import io.devinebyte.runtime.event.model.StoredEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public final class FileEventStore implements EventStore {
    private final Path basePath;
    private final ObjectMapper mapper;
    private final EventDiagnostics diagnostics;

    public FileEventStore(Path basePath, ObjectMapper mapper, EventDiagnostics diagnostics) {
        this.mapper = mapper;
        this.diagnostics = diagnostics;
        this.basePath = basePath;
    }

    @Override
    public synchronized StoredEvent append(TenantContext ctx, DomainEvent event) {
        Path tenantDir = basePath.resolve(ctx.tenantId());
        Path eventFile = tenantDir.resolve("events.log");
        try {
            Files.createDirectories(tenantDir);
            long nextSeq = getLastSequence(ctx) + 1;
            String prevHash = nextSeq == 1? "0" : readLastHash(eventFile);
            String payload = mapper.writeValueAsString(event);
            String currentHash = sha256(prevHash + payload + nextSeq);

            StoredEvent stored = new StoredEvent(nextSeq, event, prevHash, currentHash, event.occurredAt());
            String line = mapper.writeValueAsString(stored) + "\n";

            try (BufferedWriter w = Files.newBufferedWriter(eventFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                w.write(line);
            }
            return stored;
        } catch (IOException e) {
            diagnostics.add(ctx, "DBRT010", "EventAppendFailed", e.getMessage());
            throw new IllegalStateException("DBRT010: " + e.getMessage());
        }
    }

    @Override
    public List<StoredEvent> readStream(TenantContext ctx, long fromSequence) {
        Path eventFile = basePath.resolve(ctx.tenantId()).resolve("events.log");
        List<StoredEvent> out = new ArrayList<>();
        if (!Files.exists(eventFile)) return out;
        try (var reader = Files.newBufferedReader(eventFile)) {
            String line;
            String expectedPrevHash = "0";
            while ((line = reader.readLine())!= null) {
                StoredEvent e = mapper.readValue(line, StoredEvent.class);
                if (e.sequence() >= fromSequence) {
                    String payload = mapper.writeValueAsString(e.event());
                    String recomputed = sha256(e.previousHash() + payload + e.sequence());
                    if (!recomputed.equals(e.currentHash()) ||!e.previousHash().equals(expectedPrevHash)) {
                        diagnostics.add(ctx, "DBRT011", "HashChainCorrupted", "seq=" + e.sequence());
                        throw new IllegalStateException("DBRT011");
                    }
                    out.add(e);
                    expectedPrevHash = e.currentHash();
                }
            }
        } catch (IOException e) {
            diagnostics.add(ctx, "DBRT011", "EventReadFailed", e.getMessage());
            throw new IllegalStateException("DBRT011");
        }
        return out;
    }

    @Override
    public long getLastSequence(TenantContext ctx) {
        Path eventFile = basePath.resolve(ctx.tenantId()).resolve("events.log");
        if (!Files.exists(eventFile)) return 0L;
        try {
            return Files.lines(eventFile).count();
        } catch (IOException e) {
            return 0L;
        }
    }

    private String sha256(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("DBRT012");
        }
    }

    private String readLastHash(Path file) throws IOException {
        String lastLine = null;
        try (var reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine())!= null) lastLine = line;
        }
        if (lastLine == null) return "0";
        StoredEvent last = mapper.readValue(lastLine, StoredEvent.class);
        return last.currentHash();
    }
}
