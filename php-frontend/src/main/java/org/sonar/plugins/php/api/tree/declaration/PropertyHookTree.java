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
package org.sonar.plugins.php.api.tree.declaration;

import javax.annotation.Nullable;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

/**
 * <a href="https://wiki.php.net/rfc/property-hooks">Property Hooks</a>
 *
 * @since 3.39
 */
public interface PropertyHookTree extends Tree, HasAttributes {

  /**
   * Modifier of the property hook, may only be <code>final</code>.
   * @return the modifier
   */
  SyntaxToken modifierToken();

  /**
   * The double arrow token if arrow function like syntax is used.
   * @return the double arrow token
   */
  @Nullable
  SyntaxToken doubleArrowToken();

  /**
   * The reference token if the property hook is by reference.
   * @return the reference token
   */
  @Nullable
  SyntaxToken referenceToken();

  /**
   * The name of the property hook, can either be <code>get</code> or <code>set</code>.
   * @return the name of the property hook
   */
  NameIdentifierTree name();

  /**
   * The parameters of the property hook.
   * @return the parameters
   */
  @Nullable
  ParameterListTree parameters();

  /**
   * Either {@link PHPPunctuator#SEMICOLON ;} or {@link Kind#BLOCK block}
   * @return the body of the property hook
   */
  Tree body();

}
