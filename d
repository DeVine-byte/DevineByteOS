#!/usr/bin/env bash
set -e

ROOT_DIR=$(pwd)
OUT_DIR="$ROOT_DIR/phase4-inventory"
mkdir -p "$OUT_DIR"

echo "==== Phase 4 Inventory Script ===="
echo "Output will be in: $OUT_DIR"
echo ""

# Helper
run() {
  echo ">>> $1" | tee -a "$OUT_DIR/00-summary.txt"
  eval "$2" | tee -a "$OUT_DIR/$3" || echo "NOT FOUND" | tee -a "$OUT_DIR/$3"
  echo "" | tee -a "$OUT_DIR/00-summary.txt"
}

echo "Starting scan at $(date)" > "$OUT_DIR/00-summary.txt"

# 1-5: Runtime Wiring + Registry
run "1. ContractRouteRegistry" \
  "grep -RIn 'class ContractRouteRegistry\|interface ContractRouteRegistry' ." \
  "01-ContractRouteRegistry.txt"

run "2. RuntimeRegistry" \
  "grep -RIn 'class RuntimeRegistry\|interface RuntimeRegistry' ." \
  "02-RuntimeRegistry.txt"

run "3. ModuleIsolationGuard" \
  "grep -RIn 'class ModuleIsolationGuard\|interface ModuleIsolationGuard' ." \
  "03-ModuleIsolationGuard.txt"

run "4. RuntimeServices implementation" \
  "grep -RIn 'class.*implements RuntimeServices\|RuntimeServicesImpl' ." \
  "04-RuntimeServicesImpl.txt"

run "5. HandlerRegistry wiring" \
  "grep -RIn 'class HandlerRegistry\|registerHandler\|HandlerRegistry' ." \
  "05-HandlerRegistry.txt"

# 6-10: Compiler IR + Schema Models
run "6. ModuleIR" \
  "grep -RIn 'class ModuleIR\|record ModuleIR' compiler-cli/src" \
  "06-ModuleIR.txt"

run "7. Entity schema model" \
  "grep -RIn 'class EntitySchema\|record EntitySchema' compiler-cli/src" \
  "07-EntitySchema.txt"

run "8. Event schema model" \
  "grep -RIn 'class EventSchema\|record EventSchema' compiler-cli/src" \
  "08-EventSchema.txt"

run "9. API schema model" \
  "grep -RIn 'class ApiSchema\|record ApiSchema\|class RouteSchema' compiler-cli/src" \
  "09-ApiSchema.txt"

run "10. Workflow schema model" \
  "grep -RIn 'class WorkflowSchema\|record WorkflowSchema' compiler-cli/src" \
  "10-WorkflowSchema.txt"

# 11-13: Examples + Packaging + Bootstrap
run "11. .dbdsl examples" \
  "find . -type f -name '*.dbdsl'" \
  "11-dbdsl-examples.txt"

run "12. PackageContent" \
  "grep -RIn 'class PackageContent\|record PackageContent' ." \
  "12-PackageContent.txt"

run "13. Plugin/Package Loading Bootstrap" \
  "grep -RIn 'class RuntimeLauncher\|PluginLoader.loadPlugins' runtime-*/src" \
  "13-Bootstrap.txt"

# BONUS: dbpkg structure
if [ -f "execution/acme/tenant-acme-v1.0.0.dbpkg" ]; then
  echo ">>> 14. dbpkg structure" | tee -a "$OUT_DIR/00-summary.txt"
  unzip -l "execution/acme/tenant-acme-v1.0.0.dbpkg" > "$OUT_DIR/14-dbpkg-structure.txt" 2>&1 || echo "unzip failed" > "$OUT_DIR/14-dbpkg-structure.txt"
  
  echo ">>> 15. manifest.json from dbpkg" | tee -a "$OUT_DIR/00-summary.txt"
  unzip -p "execution/acme/tenant-acme-v1.0.0.dbpkg" "bootstrap/plugins/manifest.json" > "$OUT_DIR/15-manifest.json" 2>&1 || echo "{}" > "$OUT_DIR/15-manifest.json"
else
  echo ">>> 14-15. dbpkg not found, skipping" | tee -a "$OUT_DIR/00-summary.txt"
fi

echo ""
echo "==== DONE ===="
echo "Open: $OUT_DIR/00-summary.txt for quick overview"
echo "Open individual files in $OUT_DIR/ for details"
