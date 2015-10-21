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
package org.sonar.php.tree.visitors;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.sonar.sslr.api.typed.ActionParser;
import org.junit.Test;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

import java.io.File;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class PHPSubscriptionCheckTest {

  @Test
  public void test() {
    ActionParser<Tree> parser = PHPParserBuilder.createParser(Charsets.UTF_8);
    File file = new File("src/test/resources/visitors/test.php");
    CompilationUnitTree tree = (CompilationUnitTree) parser.parse(file);

    TestSubscription testVisitor = new TestSubscription();
    testVisitor.analyze(file, tree);

    assertThat(testVisitor.classCounter).isEqualTo(1);
    assertThat(testVisitor.namespaceNameCounter).isEqualTo(2);
    assertThat(testVisitor.varIdentifierCounter).isEqualTo(2);
  }


  private class TestSubscription extends PHPSubscriptionCheck {
    int classCounter = 0;
    int namespaceNameCounter = 0;
    int varIdentifierCounter = 0;


    @Override
    public List<Tree.Kind> nodesToVisit() {
      return ImmutableList.of(
        Tree.Kind.CLASS_DECLARATION,
        Tree.Kind.NAMESPACE_NAME,
        Tree.Kind.VARIABLE_IDENTIFIER);
    }

    @Override
    public void visitNode(Tree tree) {
      switch (tree.getKind()) {
        case CLASS_DECLARATION:
          classCounter++;
          break;

        case NAMESPACE_NAME:
          namespaceNameCounter++;
          break;

        case VARIABLE_IDENTIFIER:
          varIdentifierCounter++;
          break;

        default:
          break;
      }
    }
  }

}
