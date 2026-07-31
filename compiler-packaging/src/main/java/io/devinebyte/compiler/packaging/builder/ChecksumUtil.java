package io.devinebyte.compiler.packaging.builder;

import java.nio.file.Path;
import java.security.MessageDigest;

public final class ChecksumUtil {
    private ChecksumUtil() {}

    public static String sha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Checksum failed", e);
        }
    }

    public static String sha256(Path path) {
        try {
            return sha256(java.nio.file.Files.readAllBytes(path));
        } catch (Exception e) {
            throw new RuntimeException("Checksum failed", e);
        }
    }
}
