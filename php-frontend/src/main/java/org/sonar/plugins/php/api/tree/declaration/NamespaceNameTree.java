/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.php.api.tree.declaration;

import com.google.common.annotations.Beta;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

import javax.annotation.Nullable;

@Beta
public interface NamespaceNameTree extends ExpressionTree {

  @Nullable
  SyntaxToken absoluteSeparator();

  SeparatedList<NameIdentifierTree> namespaces();

  IdentifierTree name();

  /**
   * Return the concatenation of:
   * <ul>
   * <li>absolute separator (if present)</li>
   * <li>namespaces with separators (if present)</li>
   * <li>name</li>
   * </ul>
   */
  String fullName();

  /**
   * Return the unqualified form of the namespaced name, means it will return
   * just the String value of the name.
   * <p>Example:
   * <pre>
   *  \Foo\Bar  => return "Bar"
   *  Foo\Bar   => return "Bar"
   *  Bar       => return "Bar"
   * </pre>
   */
  String unqualifiedName();

  /**
   * Return the qualified form of the namespaced name, with the
   * namespaces and separators if present, only the name otherwise.
   * <p>
   * <p>Example:
   * <pre>
   *  \Foo\Bar  => return "Foo\Bar"
   *  Foo\Bar   => return "Foo\Bar"
   *  Bar       => return "Bar"
   * </pre>
   */
  String qualifiedName();

  /**
   * Return the fully qualified form of the namespaced name, with the
   * namespaces and separators if present and the absolute separator if present.
   * <p>
   * <p>Example:
   * <pre>
   *  \Foo\Bar  => return "\Foo\Bar"
   *  Foo\Bar   => return "Foo\Bar"
   *  Bar       => return "Bar"
   * </pre>
   */
  String fullyQualifiedName();

  /**
   * Return true if the namespace name starts with a namespace separator.
   * <p>Example:
   * <pre>
   *  \Foo\Bar
   *  \AnotherFoo
   * </pre>
   */
  boolean isFullyQualified();

  /**
   * Return true if the namespace name has namespace separator.
   * <p>Example:
   * <pre>
   *  Foo\Bar
   * </pre>
   */
  boolean hasQualifiers();

}
