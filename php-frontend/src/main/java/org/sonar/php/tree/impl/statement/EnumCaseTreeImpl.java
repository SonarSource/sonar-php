/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.tree.impl.statement;

import java.util.Iterator;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.EnumCaseTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class EnumCaseTreeImpl extends PHPTree implements EnumCaseTree {

  private final SyntaxToken caseToken;
  private final NameIdentifierTree name;
  private final SyntaxToken eosToken;

  public EnumCaseTreeImpl(SyntaxToken caseToken, NameIdentifierTree name, SyntaxToken eosToken) {
    this.caseToken = caseToken;
    this.name = name;
    this.eosToken = eosToken;
  }

  @Override
  public SyntaxToken caseToken() {
    return caseToken;
  }

  @Override
  public NameIdentifierTree name() {
    return name;
  }

  @Override
  public SyntaxToken eosToken() {
    return eosToken;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.iteratorOf(caseToken, name, eosToken);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitEnumCase(this);
  }

  @Override
  public Kind getKind() {
    return Kind.ENUM_CASE;
  }
}