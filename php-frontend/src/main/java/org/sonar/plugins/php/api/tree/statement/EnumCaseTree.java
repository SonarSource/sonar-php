/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.php.api.tree.statement;

import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.HasAttributes;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * <a href="https://www.php.net/manual/en/language.enumerations.php">Enumeration cases</a>
 * <pre>
 *   case {@link #name()};
 *   case {@link #name()} = {@link #value()};
 * </pre>
 */
public interface EnumCaseTree extends ClassMemberTree, HasAttributes {

  SyntaxToken caseToken();

  NameIdentifierTree name();

  @Nullable
  SyntaxToken equalToken();

  @Nullable
  ExpressionTree value();

  SyntaxToken eosToken();
}
