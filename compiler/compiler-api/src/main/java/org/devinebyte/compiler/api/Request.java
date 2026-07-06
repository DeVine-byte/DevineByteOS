package org.devinebyte.compiler.api;
import java.io.File;
public record Request(File sourceFile, File outputDirectory, CompilerContext context, boolean incremental) {}
