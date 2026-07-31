# DevineByteOS - Production Changes Checklist
`grep TODO: PROD` before every release

## 1. CLI - Hardcoded DSL Path
**DEV:** `Path dslPath = Path.of("acme.dbdsl");`
**PROD:** Accept `--dsl /path/to/tenant.dsl --tenant acme --version 1.0.0 --out /var/lib/devinebyte/build`
**File:** `compiler-cli/Cli.java`
**STATUS:** TODO
**TODO: PROD** Add picocli for arg parsing. Add `--multi-tenant` flag to set manifest.multiTenant

## 2. CLI/Runtime - System.out.println Diagnostics
**DEV:** `System.out.println("[INFO] Compiling...")` `=== TOKEN DUMP ===` `=== AST DUMP ===`
**PROD:** Remove ALL stdout. Use structured logger: SLF4J + Logback JSON.
Reason: When compiling 100 tenants in parallel, stdout will be interleaved and useless.
Keep only: `log.info("PKG_001", kv("tenant", tenantId), kv("path", dbpkgPath), kv("sha256", hash))`
**Files:** `compiler-cli/*`, `compiler-core/*`, all `System.out.println`
**STATUS:** TODO
**TODO: PROD** Add `logback-spring.xml` with JSON layout

## 3. Runtime - Checksum Skip Flag ✅ DONE FOR DEV
**DEV:** `new DbpkgVerifier(true)` when `--skip-verify` flag present in `Main.java`
**PROD:** `new DbpkgVerifier()` Always verify SHA256 + Signature. Remove `--skip-verify` flag.
**File:** `runtime-main/Main.java`, `runtime-bootstrap/DbpkgVerifier.java`
**STATUS:** TODO
**TODO: PROD** Remove flag parsing. Add Ed25519 signature verification.

## 4. Runtime - Hardcoded Dbpkg Path
**DEV:** `Path.of(args)` passed from CLI / test
**PROD:** Read from `DBPKG_ROOT=/var/lib/devinebyte/tenants/{tenant}/current.dbpkg` or Tenant Registry DB
**File:** `runtime-bootstrap/BootstrapConfig.java` [new]
**STATUS:** TODO
**TODO: PROD**

## 5. Build - Gradle CLI Tasks
**DEV:** `gradle :runtime-main:run --args="..."`
**PROD:** Remove. Use `dbos run --dbpkg <path> --tenant <id>` fat jar. Don't expose gradle to ops.
**Files:** `runtime-main/build.gradle` add `application` plugin + `distZip`
**STATUS:** TODO

## 6. TenantContext Modules ✅ PARTIAL
**DEV:** `Set.of("SALES", "INVENTORY")` hardcoded in tests. `Set.of()` in Main.java
**PROD:** Read from `manifest.json.enabledModules` or tenant DB at bootstrap time
**File:** `runtime-main/Main.java`, `runtime-bootstrap/ManifestReader.java`
**STATUS:** PARTIAL - `ManifestReader` now parses manifest but doesn't read `enabledModules` yet
**TODO: PROD** Add `enabledModules: [String]` to `Manifest` record and `manifest.json`

## 7. Test/Debug Dumps
**DEV:** `System.out.println("✅ Booted tenant:...")`
**PROD:** Gate behind `--debug` flag. Default OFF. Use structured logs.
**File:** `runtime-main/Main.java`
**STATUS:** TODO

## 8. Generator - Hardcoded Output Path
**DEV:** `Path base = Path.of("build/generated");`
**PROD:** Use `--out` arg from CLI. Default: `/tmp/devinebyte-build/{tenant}/{version}`
**File:** `compiler-generator/phase/GeneratorPhase.java`
**STATUS:** TODO

## 9. Packaging - In-Memory Hash
**DEV:** Hash computed after zip is written to disk
**PROD:** Stream hash while writing to avoid double I/O. Use `DigestOutputStream`
**File:** `compiler-packaging/builder/PackageBuilder.java`
**STATUS:** TODO

## 10. Contracts - Placeholder Schemas
**DEV:** `contracts/*.json` are 3 bytes `{}` placeholder
**PROD:** Generate real JSON Schema v2020-12 from `EntitySchema`, `EventSchema` with `$id`, `$defs`, validation rules
**Files:** `compiler-contracts/generator/*SchemaGenerator.java`
**STATUS:** TODO

## 11. ModuleGraph - No Dependency Validation
**DEV:** `dependsOn: []` always empty in `ModuleDefinition`
**PROD:** Validate no cycles in `module_graph.json` at packaging time. Fail fast.
**File:** `compiler-packaging/phase/PackagingPhase.java`
**STATUS:** TODO

## 12. Runtime - Multi-tenant Bootstrap ✅ DONE FOR DEV
**DEV:** `new TenantContext(tenantArg, TenantLifecycle.ACTIVE, Set.of())` from CLI args
**PROD:** Load TenantContext from Registry + DB. Enforce isolation, quotas, feature flags.
**File:** `runtime-main/Main.java`
**STATUS:** DONE DEV - `RuntimeBootstrapper` now supports `manifest.multiTenant` flag
Logic: If `multiTenant=false` then enforce `manifest.tenantId == runtime.tenantId`. Else allow template.
Error Code: `DBRT007` Tenant Mismatch
**TODO: PROD** Move tenant validation to DB/Registry layer

## 13. Manifest Schema ✅ NEW
**DEV:** `Manifest` record added in `runtime-bootstrap/ManifestReader.java`
Fields: `schemaVersion, tenantId, version, builtAt, builtBy, sha256, signature, multiTenant`
**PROD:** Add `enabledModules`, `minRuntimeVersion`, `dependencies`
**File:** `runtime-bootstrap/ManifestReader.java`
**STATUS:** DONE DEV
**TODO: PROD** Version the schema and add migration logic

## 14. DBSL Language Spec ✅ NEW
**DEV:** Keywords: `module, enabled, entity, event, workflow, step, kpi`
Types: `String, Number`
**PROD:** Add `service, policy, permission, relation`. Add comments. Add validation DSL
**File:** `docs/DBSL_SPEC.md`
**STATUS:** DONE DEV - v1.0.0-alpha spec written
**TODO: PROD** Implement parser for new keywords

## 15. Error Codes Registry ✅ NEW
**DEV:** Ad-hoc codes: `DBSL001-004, GAP001, RISK001, CTRT001, WF001, PKG001-003, DBRT001-008`
**PROD:** Central `ErrorCode` enum with i18n messages. Map to HTTP 400/500
**File:** `runtime-core/diagnostics/ErrorCodes.java` [new]
**STATUS:** TODO
