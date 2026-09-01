#!/usr/bin/env bash

set -u

ROOT="$(pwd)"
OUT="$ROOT/PHASE4_CRM_CONTEXT.txt"

echo "Collecting Phase 3 contracts..."
echo "Repository: $ROOT"
echo

: > "$OUT"

write_header() {
    {
        echo
        echo "================================================================"
        echo "$1"
        echo "================================================================"
        echo
    } >> "$OUT"
}

find_and_append() {
    local label="$1"
    shift

    write_header "$label"

    local found=0

    for pattern in "$@"; do
        while IFS= read -r -d '' file; do
            found=1

            {
                echo
                echo "----- FILE: ${file#"$ROOT"/} -----"
                echo
                cat "$file"
                echo
            } >> "$OUT"

        done < <(
            find "$ROOT" \
                -type f \
                -name "$pattern" \
                -not -path '*/.git/*' \
                -not -path '*/build/*' \
                -not -path '*/target/*' \
                -print0
        )
    done

    if [ "$found" -eq 0 ]; then
        echo "[NOT FOUND]" >> "$OUT"
    fi
}

write_header "DEVINEBYTE OS — PHASE 4 CRM CONTEXT"

{
    echo "Generated: $(date)"
    echo "Repository: $ROOT"
    echo
    echo "This file contains the Phase 3 contracts requested before CRM implementation."
    echo "The collection script is read-only."
} >> "$OUT"


# ================================================================
# 1. ContractRouteRegistry
# ================================================================

find_and_append \
    "1. CONTRACT ROUTE REGISTRY" \
    "ContractRouteRegistry.java"


# ================================================================
# 2. RuntimeRegistry
# ================================================================

find_and_append \
    "2. RUNTIME REGISTRY" \
    "RuntimeRegistry.java"


# ================================================================
# 3. ModuleIsolationGuard
# ================================================================

find_and_append \
    "3. MODULE ISOLATION GUARD" \
    "ModuleIsolationGuard.java"


# ================================================================
# 4. RuntimeServices implementation
# ================================================================

find_and_append \
    "4. RUNTIME SERVICES" \
    "RuntimeServices.java" \
    "*RuntimeServicesImpl.java" \
    "*RuntimeService*.java"


# ================================================================
# 5. HandlerRegistry + Event registration/wiring
# ================================================================

find_and_append \
    "5. EVENT HANDLER REGISTRATION AND WIRING" \
    "HandlerRegistry.java" \
    "*EventHandler*.java" \
    "*EventDispatcher*.java" \
    "*EventBus*.java"


# ================================================================
# 6. ModuleIR
# ================================================================

find_and_append \
    "6. MODULE IR" \
    "ModuleIR.java"


# ================================================================
# 7. Entity schema models
# ================================================================

find_and_append \
    "7. ENTITY SCHEMA MODELS" \
    "*EntitySchema*.java" \
    "*Entity*.java"


# ================================================================
# 8. Event schema models
# ================================================================

find_and_append \
    "8. EVENT SCHEMA MODELS" \
    "*EventSchema*.java" \
    "*DomainEvent*.java" \
    "*EventMetadata*.java"


# ================================================================
# 9. API schema models
# ================================================================

find_and_append \
    "9. API SCHEMA MODELS" \
    "*APISchema*.java" \
    "*ApiSchema*.java" \
    "*Route*.java" \
    "*Endpoint*.java"


# ================================================================
# 10. Workflow schema models
# ================================================================

find_and_append \
    "10. WORKFLOW SCHEMA MODELS" \
    "*WorkflowSchema*.java" \
    "*WorkflowDefinition*.java" \
    "*ExecutableStateMachine*.java"


# ================================================================
# 11. Working DSL examples
# ================================================================

write_header "11. DBDSL EXAMPLES"

DSL_FOUND=0

while IFS= read -r -d '' file; do
    DSL_FOUND=1

    {
        echo
        echo "----- FILE: ${file#"$ROOT"/} -----"
        echo
        cat "$file"
        echo
    } >> "$OUT"

done < <(
    find "$ROOT" \
        -type f \
        \( \
            -name "*.dbdsl" \
            -o -name "*.dbos" \
            -o -name "*.bp" \
        \) \
        -not -path '*/.git/*' \
        -not -path '*/build/*' \
        -not -path '*/target/*' \
        -print0
)

if [ "$DSL_FOUND" -eq 0 ]; then
    echo "[NO DSL FILES FOUND]" >> "$OUT"

    {
        echo
        echo "Searching source/test files for DSL examples..."
        echo
    } >> "$OUT"

    grep -RniE \
        --exclude-dir=.git \
        --exclude-dir=build \
        --exclude-dir=target \
        --exclude='*.class' \
        --exclude='*.jar' \
        -E 'module[[:space:]]+[A-Za-z_]|entity[[:space:]]+[A-Za-z_]|event[[:space:]]+[A-Za-z_]|workflow[[:space:]]+[A-Za-z_]' \
        "$ROOT" 2>/dev/null >> "$OUT" || true
fi


# ================================================================
# 12. PackageContent
# ================================================================

find_and_append \
    "12. PACKAGE CONTENT" \
    "PackageContent.java"


# ================================================================
# 13. Plugin/package loading bootstrap
# ================================================================

find_and_append \
    "13. PLUGIN AND PACKAGE LOADING BOOTSTRAP" \
    "PluginLoader.java" \
    "DbpkgExtractor.java" \
    "*Bootstrap*.java" \
    "*PackageLoader*.java" \
    "*RuntimeBootstrap*.java"


# ================================================================
# Additional high-value context
# ================================================================

find_and_append \
    "ADDITIONAL: RUNTIME PLUGIN CONTRACT" \
    "RuntimePlugin.java" \
    "PluginContext.java" \
    "PluginDescriptor.java" \
    "PluginManifest.java"

find_and_append \
    "ADDITIONAL: RUNTIME SDK" \
    "RuntimeClient.java" \
    "EntityClient.java" \
    "WorkflowClient.java" \
    "RuntimeContext.java"

find_and_append \
    "ADDITIONAL: EVENT MODEL" \
    "StoredEvent.java" \
    "EventStore.java"

find_and_append \
    "ADDITIONAL: RUNTIME API SERVER" \
    "RuntimeApiServer.java"

find_and_append \
    "ADDITIONAL: PACKAGE BUILDER" \
    "PackageBuilder.java"

find_and_append \
    "ADDITIONAL: COMPILER MODELS REFERENCED BY RUNTIME" \
    "ModuleIR.java" \
    "State.java" \
    "Transition.java"


# ================================================================
# Summary
# ================================================================

{
    echo
    echo "================================================================"
    echo "COLLECTION COMPLETE"
    echo "================================================================"
    echo
    echo "Output:"
    echo "$OUT"
    echo
    echo "Size:"
    wc -l "$OUT"
} >> "$OUT"

echo
echo "================================================"
echo "Collection complete."
echo "================================================"
echo
echo "Output file:"
echo "$OUT"
echo
echo "Lines:"
wc -l "$OUT"
echo
echo "You can now send me:"
echo
echo "PHASE4_CRM_CONTEXT.txt"
echo
