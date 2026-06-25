package org.devinebyte.compiler.parser.parser;

import com.devinebyte.compiler.parser.ast.ProgramNode;

public record ParseResult(

        ProgramNode program,

        boolean success

) {}
