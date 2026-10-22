/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks;

import java.util.HashSet;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.TestFileExcludedCheck;
import org.sonar.php.symbols.Symbols;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonarsource.analyzer.commons.appsec.IpAddressCandidateExtractor;
import org.sonarsource.analyzer.commons.appsec.IpAddressClassifier;

import static org.sonar.php.checks.utils.CheckUtils.skipParenthesis;
import static org.sonar.php.checks.utils.CheckUtils.trimQuotes;

@Rule(key = "S1313")
public class HardCodedIpAddressCheck extends PHPVisitorCheck implements TestFileExcludedCheck {

  private static final String MESSAGE = "Make sure using this hardcoded IP address is safe here.";

  private final Set<LiteralTree> candidates = new HashSet<>();

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    candidates.clear();
    super.visitCompilationUnit(tree);
    if (!candidates.isEmpty()) {
      new VersionComparisonFilter(candidates).analyze(context());
    }
    if (!candidates.isEmpty()) {
      new ClassNameSuffixFilter(candidates).analyze(context());
    }
    for (LiteralTree candidate : candidates) {
      context().newIssue(this, candidate, MESSAGE);
    }
  }

  @Override
  public void visitLiteral(LiteralTree tree) {
    if (tree.is(Tree.Kind.REGULAR_STRING_LITERAL)
      && IpAddressCandidateExtractor.extract(trimQuotes(tree.value()))
        .flatMap(IpAddressClassifier::parse)
        .map(parsed -> !parsed.isAllowedToBeHardcoded()).orElse(false)) {
      candidates.add(tree);
    }
    super.visitLiteral(tree);
  }

  private static boolean isBuiltinFunction(FunctionCallTree call, QualifiedName expectedName) {
    if (!(call.callee() instanceof NamespaceNameTree)) {
      return false;
    }
    var symbol = Symbols.get(call);
    // Builtins have no declaration in the function index; known symbols represent user-defined functions.
    boolean isBuiltin = symbol.isUnknownSymbol();
    return isBuiltin && expectedName.equals(symbol.qualifiedName());
  }

  // Removes string literals passed directly to builtin version_compare.
  private static class VersionComparisonFilter extends PHPVisitorCheck {
    private static final QualifiedName VERSION_COMPARE = QualifiedName.qualifiedName("version_compare");

    private final Set<LiteralTree> candidates;

    private VersionComparisonFilter(Set<LiteralTree> candidates) {
      this.candidates = candidates;
    }

    @Override
    public void visitFunctionCall(FunctionCallTree tree) {
      if (isBuiltinFunction(tree, VERSION_COMPARE)) {
        for (CallArgumentTree argument : tree.callArguments()) {
          if (skipParenthesis(argument.value()) instanceof LiteralTree literal) {
            candidates.remove(literal);
          }
        }
      }
      super.visitFunctionCall(tree);
    }

  }

  // Removes string literals appended directly to builtin get_class results.
  private static class ClassNameSuffixFilter extends PHPVisitorCheck {
    private static final QualifiedName GET_CLASS = QualifiedName.qualifiedName("get_class");

    private final Set<LiteralTree> candidates;

    private ClassNameSuffixFilter(Set<LiteralTree> candidates) {
      this.candidates = candidates;
    }

    @Override
    public void visitBinaryExpression(BinaryExpressionTree tree) {
      if (tree.is(Tree.Kind.CONCATENATION)
        && skipParenthesis(tree.leftOperand()) instanceof FunctionCallTree call
        && isBuiltinFunction(call, GET_CLASS)
        && skipParenthesis(tree.rightOperand()) instanceof LiteralTree suffix) {
        candidates.remove(suffix);
      }
      super.visitBinaryExpression(tree);
    }

  }

}
