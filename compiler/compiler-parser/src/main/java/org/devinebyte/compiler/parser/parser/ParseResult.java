package org.devinebyte.compiler.parser.parser;

import org.devinebyte.compiler.parser.ast.ProgramNode;

public record ParseResult(

        ProgramNode program,

        boolean success

) {}
