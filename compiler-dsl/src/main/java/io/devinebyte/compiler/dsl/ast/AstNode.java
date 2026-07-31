package io.devinebyte.compiler.dsl.ast;

public sealed interface AstNode permits ModuleNode, EntityNode, EventNode, WorkflowNode, KpiNode {}
