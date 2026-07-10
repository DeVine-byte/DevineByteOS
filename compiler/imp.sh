
set -e

echo "=== Rewriting deprecated imports ==="

find . -path "*/src/test/java/*.java" | while read -r file; do

# CompileCommand moved
sed -i \
's/import org\.devinebyte\.compiler\.cli\.CompileCommand;/import org.devinebyte.compiler.cli.commands.CompileCommand;/g' \
"$file"

# SourceWriter moved
sed -i \
's/import org\.devinebyte\.compiler\.generator\.engine\.SourceWriter;/import org.devinebyte.compiler.generator.writer.SourceWriter;/g' \
"$file"

# JavaGenerator package
sed -i \
's/import org\.devinebyte\.compiler\.generator\.java\.JavaGenerator;/import org.devinebyte.compiler.generator.java.JavaGenerator;/g' \
"$file"

# Diagnostics package
sed -i \
's/import org\.devinebyte\.compiler\.Diagnostic;/import org.devinebyte.compiler.api.diagnostics.Diagnostic;/g' \
"$file"

sed -i \
's/import org\.devinebyte\.compiler\.DiagnosticSeverity;/import org.devinebyte.compiler.api.diagnostics.DiagnosticSeverity;/g' \
"$file"

# CompilationResult rename
sed -i \
's/import org\.devinebyte\.compiler\.api\.CompilerResult;/import org.devinebyte.compiler.api.CompilationResult;/g' \
"$file"

# CompilerRequest removal
sed -i \
's/import org\.devinebyte\.compiler\.api\.request\.CompilerRequest;/import org.devinebyte.compiler.api.Request;/g' \
"$file"

done

echo "=== Removing imports for deleted subsystems ==="

find . -path "*/src/test/java/*.java" | while read -r file; do

sed -i '/import org\.devinebyte\.compiler\.lexing\.Lexer;/d' "$file"
sed -i '/import org\.devinebyte\.compiler\.parser\.Parser;/d' "$file"
sed -i '/import org\.devinebyte\.compiler\.semantic\.SemanticAnalyzer;/d' "$file"

done

echo "=== Replacing deleted class names ==="

find . -path "*/src/test/java/*.java" -exec sed -i \
-e 's/\bCompilerResult\b/CompilationResult/g' \
-e 's/\bCompilerRequest\b/Request/g' \
{} +

echo "=== Replacing obsolete constructors ==="

find . -path "*/src/test/java/*.java" -exec sed -i \
-e 's/new BlueprintValidator()/mock(BlueprintValidator.class)/g' \
{} +

echo "=== Adding Mockito import where needed ==="

find . -path "*/src/test/java/*.java" | while read -r file; do

if grep -q "mock(BlueprintValidator.class)" "$file"; then
    grep -q "import static org.mockito.Mockito.mock;" "$file" \
    || sed -i '/^import/a import static org.mockito.Mockito.mock;' "$file"
fi

done

echo "=== Finished ==="
