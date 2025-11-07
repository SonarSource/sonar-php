/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.php.checks.utils.type;

import com.sonar.sslr.api.typed.ActionParser;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.tree.impl.VariableIdentifierTreeImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;

import static org.assertj.core.api.Assertions.assertThat;

class TreeValuesTest {

  static final ActionParser<Tree> PARSER = PHPParserBuilder.createParser();

  @Test
  void lookup() {
    CompilationUnitTree unit = (CompilationUnitTree) PARSER.parse("<?php f(); $a = 1; $b =& $a; $a = 2; $a += 3; $a = ((4));");
    ExpressionTree f = expression(unit, 0);
    ExpressionTree a = ((AssignmentExpressionTree) expression(unit, 1)).variable();
    ExpressionTree b = ((AssignmentExpressionTree) expression(unit, 2)).variable();
    ExpressionTree unknownVariable = new VariableIdentifierTreeImpl(
      new InternalSyntaxToken(1, 1, "$x", Collections.emptyList(), 1, false));

    SymbolTable symbolTable = SymbolTableImpl.create(unit);

    TreeValues values = TreeValues.of(a, symbolTable);
    assertThat(asString(values)).isEqualTo("$a");
    assertThat(asString(values.lookupPossibleValues(a))).isEqualTo("$a, 1, 2, 4");
    assertThat(asString(values.lookupPossibleValues(b))).isEqualTo("$b, $a");
    assertThat(asString(values.lookupPossibleValues(f))).isEqualTo("f()");
    assertThat(asString(values.lookupPossibleValues(unknownVariable))).isEqualTo("$x");
  }

  @Test
  void lookupForeach() {
    CompilationUnitTree unit = (CompilationUnitTree) PARSER.parse("<?php $a = 1; foreach($arr as $a) { };");
    ExpressionTree a = ((AssignmentExpressionTree) expression(unit, 0)).variable();
    ForEachStatementTree forEachStatement = (ForEachStatementTree) unit.script().statements().get(1);
    SymbolTable symbolTable = SymbolTableImpl.create(unit);

    TreeValues values = TreeValues.of(a, symbolTable);
    assertThat(asString(values)).isEqualTo("$a");
    assertThat(asString(values.lookupPossibleValues(forEachStatement.value()))).isEqualTo("$a, 1, $a");
    assertThat(asString(values.lookupPossibleValues(forEachStatement.expression()))).isEqualTo("$arr");
  }

  static ExpressionTree expression(CompilationUnitTree unit, int index) {
    return ((ExpressionStatementTree) unit.script().statements().get(index)).expression();
  }

  static String asString(TreeValues values) {
    return values.values.stream().map(Objects::toString).collect(Collectors.joining(", "));
  }

}
