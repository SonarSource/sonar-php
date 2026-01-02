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
package org.sonar.php.checks;

import java.util.List;
import java.util.Locale;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.tree.symbols.HasMethodSymbol;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.AttributeGroupTree;
import org.sonar.plugins.php.api.tree.declaration.AttributeTree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AnonymousClassTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = TooManyMethodsInClassCheck.KEY)
public class TooManyMethodsInClassCheck extends PHPVisitorCheck {

  public static final String KEY = "S1448";
  private static final String MESSAGE = "Class \"%s\" has %s methods, which is greater than %s authorized. Split it into smaller classes.";
  private static final String MESSAGE_ANONYMOUS_CLASS = "This anonymous class has %s methods, which is greater than %s authorized. Split it into smaller classes.";

  private static final int DEFAULT_THRESHOLD = 20;
  private static final boolean DEFAULT_NON_PUBLIC = true;

  @RuleProperty(
    key = "maximumMethodThreshold",
    defaultValue = "" + DEFAULT_THRESHOLD)
  public int maximumMethodThreshold = DEFAULT_THRESHOLD;

  @RuleProperty(
    key = "countNonpublicMethods",
    defaultValue = "" + DEFAULT_NON_PUBLIC,
    type = "BOOLEAN")
  public boolean countNonpublicMethods = DEFAULT_NON_PUBLIC;

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    super.visitClassDeclaration(tree);
    if (tree.is(Kind.CLASS_DECLARATION, Kind.INTERFACE_DECLARATION)) {
      checkClass(tree);
    }
  }

  @Override
  public void visitAnonymousClass(AnonymousClassTree tree) {
    super.visitAnonymousClass(tree);
    checkClass(tree);
  }

  private void checkClass(ClassTree tree) {
    int nbMethod = getNumberOfMethods(tree);

    if (nbMethod > maximumMethodThreshold && classIsDBEntity(tree)) {
      String message;
      if (tree.is(Kind.ANONYMOUS_CLASS)) {
        message = String.format(MESSAGE_ANONYMOUS_CLASS, nbMethod, maximumMethodThreshold);
      } else {
        message = String.format(MESSAGE, ((ClassDeclarationTree) tree).name().text(), nbMethod, maximumMethodThreshold);
      }
      context().newIssue(this, tree.classToken(), message);
    }
  }

  private int getNumberOfMethods(ClassTree tree) {
    int nbMethod = 0;

    for (ClassMemberTree classMember : tree.members()) {
      if (classMember.is(Kind.METHOD_DECLARATION) && !isExcluded((MethodDeclarationTree) classMember)) {
        nbMethod++;
      }
    }

    return nbMethod;
  }

  private static boolean classIsDBEntity(ClassTree tree) {
    return !classHasOnlyGettersAndSetters(tree) && !classHasEntityAttribute(tree);
  }

  private static boolean classHasOnlyGettersAndSetters(ClassTree tree) {
    for (ClassMemberTree classMember : tree.members()) {
      if (classMember.is(Kind.METHOD_DECLARATION)) {
        String methodName = ((MethodDeclarationTree) classMember).name().text().toLowerCase(Locale.ROOT);
        if (!methodName.startsWith("get") && !methodName.startsWith("set")) {
          return false;
        }
      }
    }
    return true;
  }

  private static boolean classHasEntityAttribute(ClassTree tree) {
    List<AttributeGroupTree> attributes = tree.attributeGroups();

    if (!attributes.isEmpty()) {
      for (AttributeGroupTree attribute : attributes) {
        for (AttributeTree attributeTree : attribute.attributes()) {
          String finalName = attributeTree.name().fullyQualifiedName();
          if ("ORM\\Entity".equals(finalName) || "Entity".equals(finalName)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Return true if method is private, protected or is a test method.
   */
  private boolean isExcluded(MethodDeclarationTree tree) {
    if (((HasMethodSymbol) tree).symbol().isTestMethod().isTrue()) {
      return true;
    }

    if (!countNonpublicMethods) {
      for (SyntaxToken modifierToken : tree.modifiers()) {
        String modifier = modifierToken.text();
        if (PHPKeyword.PROTECTED.getValue().equals(modifier) || PHPKeyword.PRIVATE.getValue().equals(modifier)) {
          return true;
        }
      }
    }
    return false;
  }

}
