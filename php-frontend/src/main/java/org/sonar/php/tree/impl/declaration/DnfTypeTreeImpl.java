/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.DeclaredTypeTree;
import org.sonar.plugins.php.api.tree.declaration.DnfTypeTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class DnfTypeTreeImpl extends PHPTree implements DnfTypeTree {

  private final SeparatedList<DeclaredTypeTree> types;

  public DnfTypeTreeImpl(SeparatedList<DeclaredTypeTree> types) {
    this.types = types;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return types.elementsAndSeparators();
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitDnfType(this);
  }

  @Override
  public Kind getKind() {
    return Kind.DNF_TYPE;
  }

  @Override
  public SeparatedList<DeclaredTypeTree> types() {
    return types;
  }
}
