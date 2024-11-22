/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.plugins.php.api.tree.expression;

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * <a href="http://php.net/manual/en/language.oop5.anonymous.php">Anonymous class</a>
 *
 * <pre>
 *   class
 * </pre>
 */
public interface AnonymousClassTree extends ExpressionTree, ClassTree {
  boolean isReadOnly();

  @Override
  SyntaxToken classToken();

  @Nullable
  SyntaxToken openParenthesisToken();

  /**
   * @deprecated since 3.11 . Use {@link #callArguments()} instead.
   */
  @Deprecated
  SeparatedList<ExpressionTree> arguments();

  SeparatedList<CallArgumentTree> callArguments();

  @Nullable
  SyntaxToken closeParenthesisToken();

  @Override
  @Nullable
  SyntaxToken extendsToken();

  @Override
  @Nullable
  NamespaceNameTree superClass();

  @Override
  @Nullable
  SyntaxToken implementsToken();

  @Override
  SeparatedList<NamespaceNameTree> superInterfaces();

  @Override
  SyntaxToken openCurlyBraceToken();

  @Override
  List<ClassMemberTree> members();

  @Override
  SyntaxToken closeCurlyBraceToken();

}
