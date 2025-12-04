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
package org.sonar.php.tree.impl.declaration;

import java.util.Iterator;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.collections.IteratorUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.TraitAliasTree;
import org.sonar.plugins.php.api.tree.statement.TraitMethodReferenceTree;
import org.sonar.plugins.php.api.visitors.VisitorCheck;

public class TraitAliasTreeImpl extends PHPTree implements TraitAliasTree {

  private static final Kind KIND = Kind.TRAIT_ALIAS;

  private final TraitMethodReferenceTree methodReference;
  private final InternalSyntaxToken asToken;
  private final SyntaxToken modifier;
  private final NameIdentifierTree alias;
  private final InternalSyntaxToken eosToken;

  public TraitAliasTreeImpl(
    TraitMethodReferenceTree methodReference,
    InternalSyntaxToken asToken,
    @Nullable SyntaxToken modifier,
    @Nullable NameIdentifierTree alias,
    InternalSyntaxToken eos) {
    this.methodReference = methodReference;
    this.asToken = asToken;
    this.modifier = modifier;
    this.alias = alias;
    this.eosToken = eos;
  }

  @Override
  public TraitMethodReferenceTree methodReference() {
    return methodReference;
  }

  @Override
  public SyntaxToken asToken() {
    return asToken;
  }

  @Nullable
  @Override
  public SyntaxToken modifierToken() {
    return modifier;
  }

  @Nullable
  @Override
  public NameIdentifierTree alias() {
    return alias;
  }

  @Override
  public SyntaxToken eosToken() {
    return eosToken;
  }

  @Override
  public Kind getKind() {
    return KIND;
  }

  @Override
  public Iterator<Tree> childrenIterator() {
    return IteratorUtils.iteratorOf(methodReference, asToken, modifier, alias, eosToken);
  }

  @Override
  public void accept(VisitorCheck visitor) {
    visitor.visitTraitAlias(this);
  }

}
