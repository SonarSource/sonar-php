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
package org.sonar.php.tree.impl.declaration;

import java.util.Iterator;
import java.util.List;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.PropertyHookListTree;
import org.sonar.plugins.php.api.tree.declaration.PropertyHookTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class PropertyHookListTreeImpl extends PHPTree implements PropertyHookListTree {

  private static final Kind KIND = Kind.PROPERTY_HOOK_LIST;

  private final InternalSyntaxToken openCurlyBrace;
  private final List<PropertyHookTree> hooks;
  private final InternalSyntaxToken closeCurlyBrace;

  public PropertyHookListTreeImpl(
    InternalSyntaxToken openCurlyBrace, List<PropertyHookTree> hooks, InternalSyntaxToken closeCurlyBrace) {
    this.openCurlyBrace = openCurlyBrace;
    this.hooks = hooks;
    this.closeCurlyBrace = closeCurlyBrace;
  }

  @Override
  public List<PropertyHookTree> hooks() {
    return hooks;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(
      IteratorUtils.iteratorOf(openCurlyBrace),
      hooks.iterator(),
      IteratorUtils.iteratorOf(closeCurlyBrace));
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitPropertyHookList(this);
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public InternalSyntaxToken openCurlyBrace() {
    return openCurlyBrace;
  }

  @Override
  public InternalSyntaxToken closeCurlyBrace() {
    return closeCurlyBrace;
  }
}
