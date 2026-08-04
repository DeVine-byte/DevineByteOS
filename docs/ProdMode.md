# DevineByteOS - Production Changes Checklist
Run `grep "TODO: PROD"` before every release. Zero TODOs allowed.

## 1. CLI - Argument Parsing
**DEV:** Positional args + hardcoded DSL path. `gradle :compiler-cli:run`
**PROD:** Use `picocli`. Fat jar: `dbos compile --dsl <path> --tenant <id> --version <semver> --out <dir> [--strict] [--multi-tenant]`
**File:** `compiler-cli/Cli.java`
**STATUS:** TODO
**TODO: PROD** Add picocli. Validate semver. `--strict` sets `multiTenant=false`

## 2. Logging - Remove All Stdout
**DEV:** `System.out.println("[INFO]")` `=== TOKEN DUMP ===` `=== AST DUMP ===`
**PROD:** SLF4J + Logback JSON. 1 log line per phase.
Example: `log.info("PKG_001", kv("tenant","acme"), kv("path",dbpkg), kv("sha256",hash), kv("duration_ms", 151))`
**Files:** `compiler-*/*`, `runtime-*/*`
**STATUS:** TODO
**TODO: PROD** Add `logback-spring.xml` with JSON layout. Gate debug dumps behind `--debug`.

## 3. Runtime - Security: Remove Skip-Verify
**DEV:** `--skip-verify` flag bypasses SHA256
**PROD:** Always verify `manifest.sha256` + `manifest.signature` Ed25519. Remove flag.
**Files:** `runtime-main/Main.java`, `runtime-bootstrap/DbpkgVerifier.java`
**STATUS:** TODO
**TODO: PROD** Add public key pinning. Fail on `DBRT004: SignatureInvalid`

## 4. Runtime - Dbpkg Location
**DEV:** Path passed via CLI `--dbpkg /home/...`
**PROD:** Read from `DBPKG_ROOT=/var/lib/devinebyte/tenants/{tenant}/current.dbpkg` or Tenant Registry gRPC
**File:** `runtime-bootstrap/BootstrapConfig.java` [new]
**STATUS:** TODO
**TODO: PROD** Add config. Support hot-swap via symlink.

## 5. Build - Distribution
**DEV:** `gradle :runtime-main:run`
**PROD:** `application` plugin. `dbos` binary in `/usr/bin`. `distZip` + `deb` package
**File:** `runtime-main/build.gradle`, `compiler-cli/build.gradle`
**STATUS:** TODO
**TODO: PROD** No gradle in prod. Add systemd unit.

## 6. TenantContext - Source of Truth
**DEV:** From `module_graph.json` fallback
**PROD:** Source of truth = `manifest.json.enabledModules`. If missing, fallback to `module_graph.json.enabled=true`
**Files:** `runtime-main/Main.java`, `runtime-bootstrap/ManifestReader.java`, `Manifest` record
**STATUS:** PARTIAL
**TODO: PROD** Add `enabledModules: List<String>` to `Manifest`. Works in both multi and single tenant.

## 7. Packaging - Versioned Filename
**DEV:** `tenant-acme-v1.0.0.dbpkg` hardcoded
**PROD:** `tenant-{tenantId}-v{manifest.version}.dbpkg`. Example: `tenant-acme-v2.0.0.dbpkg`
**File:** `compiler-packaging/builder/PackageBuilder.java`
**STATUS:** TODO
**TODO: PROD** Use `manifest.version()`. Validate semver. Support rollback.

## 8. Packaging - Streaming Hash
**DEV:** Hash computed after writing zip to disk = 2x I/O
**PROD:** `DigestOutputStream` while writing. Write `sha256` to `manifest.json` before closing
**File:** `compiler-packaging/builder/PackageBuilder.java`
**STATUS:** TODO
**TODO: PROD**

## 9. Generator - Output Path
**DEV:** `build/generated`
**PROD:** `--out /tmp/devinebyte-build/{tenant}/{version}` from CLI
**File:** `compiler-generator/phase/GeneratorPhase.java`
**STATUS:** TODO
**TODO: PROD**

## 10. ModuleGraph - Dependency Validation
**DEV:** No cycle check. `topologicalOrder` wrong in report: `[inventory, sales]`
**PROD:** Fail at packaging if cycle detected. `topologicalOrder` must be correct for runtime loader
**File:** `compiler-packaging/phase/PackagingPhase.java`
**STATUS:** TODO
**TODO: PROD** Add Kahn's algorithm. Error: `PKG_004: DependencyCycle`

## 11. Manifest Schema v2
**DEV:** `schemaVersion, tenantId, version, builtAt, builtBy, sha256, signature, multiTenant`
**PROD:** Add `enabledModules`, `minRuntimeVersion`, `dependencies: Map<String, String>`, `features: Map<String, Boolean>`
**File:** `runtime-bootstrap/ManifestReader.java`
**STATUS:** TODO
**TODO: PROD** Version schema. Add migration from v1 -> v2.

## 12. Multi-Tenant / Single-Tenant Enforcement
**DEV:** CLI flag ignored at runtime
**PROD:** If `manifest.multiTenant=false` and `--strict` then enforce `runtime.tenantId == manifest.tenantId` else `DBRT007: TenantMismatch`
If `manifest.multiTenant=true` then allow any `runtime.tenantId` from Registry
**File:** `runtime-bootstrap/RuntimeBootstrapper.java`
**STATUS:** PARTIAL
**TODO: PROD** Add the DBRT007 check. Move quota/feature validation to Registry.

## 13. Contracts - Real JSON Schema
**DEV:** `{}` placeholder
**PROD:** Generate JSON Schema 2020-12 with `$id`, `$defs`, `required`, `type`, validation rules from DBSL
**Files:** `compiler-contracts/generator/*SchemaGenerator.java`
**STATUS:** TODO
**TODO: PROD**

## 14. tenant_config.json - Optional Runtime Override [REMOVED REQUIREMENT]
**DEV:** File not generated
**PROD:** Optional. Used only for multi-tenant templates. If present, it overrides `manifest.enabledModules`
**File:** `compiler-generator/phase/BootstrapGeneratorPhase.java`
**STATUS:** TODO
**TODO: PROD** No longer required for `--strict`. Only for SaaS multi-tenant.

## 15. Error Codes - Central Registry
**DEV:** Ad-hoc strings: `DBRT003`, `PKG001`
**PROD:** `ErrorCode` enum with code, httpStatus, i18n message, remediation
**File:** `runtime-core/diagnostics/ErrorCodes.java` [new]
**STATUS:** TODO
**TODO: PROD** Map to gRPC status codes.

## 16. DBSL Language v1.0.0
**DEV:** `module, entity, event, workflow, String, Int`
**PROD:** Add `service, policy, permission, relation, comment //`, validation DSL `@NotNull`
**File:** `docs/DBSL_SPEC.md`, `compiler-dsl/grammar/DBSL.g4`
**STATUS:** TODO
**TODO: PROD**

## 17. Observability
**DEV:** None
**PROD:** OpenTelemetry traces for each compile phase + runtime boot. Metrics: `dbos_boot_duration_seconds`, `dbos_module_count`
**File:** `runtime-core/observability/*`
**STATUS:** TODO
**TODO: PROD**
