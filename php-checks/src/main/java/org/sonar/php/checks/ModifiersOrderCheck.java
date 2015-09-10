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
package org.sonar.php.checks;

import com.google.common.collect.ImmutableList;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.List;

@Rule(
  key = "S1124",
  name = "Modifiers should be declared in the correct order",
  priority = Priority.MINOR,
  tags = {Tags.CONVENTION, Tags.PSR2})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("2min")
public class ModifiersOrderCheck extends SquidCheck<LexerlessGrammar> {

  private static final AstNodeType[] EXPECTED_ORDER = {
    PHPKeyword.FINAL,
    PHPKeyword.ABSTRACT,
    PHPKeyword.PUBLIC,
    PHPKeyword.PROTECTED,
    PHPKeyword.PRIVATE,
    PHPKeyword.STATIC};

  @Override
  public void init() {
    subscribeTo(PHPGrammar.MEMBER_MODIFIER);
  }

  @Override
  public void visitNode(AstNode node) {
    if (isFirstModifer(node)) {
      List<AstNode> modifiers = getModifiers(node);

      if (isBadlyOrdered(modifiers)) {
        getContext().createLineViolation(this, "Reorder the modifiers to comply with the PSR2 standard.", node);
      }
    }
  }

  private static boolean isFirstModifer(AstNode node) {
    return node.getPreviousSibling() == null;
  }

  private static List<AstNode> getModifiers(AstNode node) {
    ImmutableList.Builder<AstNode> builder = ImmutableList.builder();
    builder.add(node);

    for (AstNode nextSibling = node.getNextSibling(); nextSibling != null && nextSibling.is(PHPGrammar.MEMBER_MODIFIER); nextSibling = nextSibling.getNextSibling()) {
      builder.add(nextSibling);
    }

    return builder.build();
  }

  private static boolean isBadlyOrdered(List<AstNode> modifiers) {
    int i = 0;

    for (AstNode modifier : modifiers) {
      for (; i < EXPECTED_ORDER.length && !modifier.getFirstChild().is(EXPECTED_ORDER[i]); i++) {
        // We're just interested in the final value of 'i'
      }
    }

    return i == EXPECTED_ORDER.length;
  }


}
