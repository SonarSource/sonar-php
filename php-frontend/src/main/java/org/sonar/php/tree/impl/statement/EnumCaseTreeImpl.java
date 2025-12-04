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
package org.sonar.php.tree.impl.statement;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeGroupTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.EnumCaseTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class EnumCaseTreeImpl extends PHPTree implements EnumCaseTree {

  private final List<AttributeGroupTree> attributeGroupTrees;
  private final SyntaxToken caseToken;
  private final NameIdentifierTree name;
  private final SyntaxToken equalToken;
  private final ExpressionTree value;
  private final SyntaxToken eosToken;

  public EnumCaseTreeImpl(List<AttributeGroupTree> attributeGroups, SyntaxToken caseToken, NameIdentifierTree name,
    @Nullable SyntaxToken equalToken, @Nullable ExpressionTree value, SyntaxToken eosToken) {
    this.attributeGroupTrees = attributeGroups;
    this.caseToken = caseToken;
    this.name = name;
    this.equalToken = equalToken;
    this.value = value;
    this.eosToken = eosToken;
  }

  @Override
  public List<AttributeGroupTree> attributeGroups() {
    return attributeGroupTrees;
  }

  @Override
  public SyntaxToken caseToken() {
    return caseToken;
  }

  @Override
  public NameIdentifierTree name() {
    return name;
  }

  @Nullable
  @Override
  public SyntaxToken equalToken() {
    return equalToken;
  }

  @Nullable
  @Override
  public ExpressionTree value() {
    return value;
  }

  @Override
  public SyntaxToken eosToken() {
    return eosToken;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.concat(attributeGroupTrees.iterator(), IteratorUtils.iteratorOf(caseToken, name, equalToken, value, eosToken));
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
