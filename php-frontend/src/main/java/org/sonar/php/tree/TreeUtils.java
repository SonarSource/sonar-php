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
package org.sonar.php.tree;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.*;
import org.sonar.plugins.php.api.tree.declaration.AttributeGroupTree;
import org.sonar.plugins.php.api.tree.declaration.AttributeTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;

import static java.util.Arrays.asList;
import static org.sonar.php.utils.collections.ListUtils.getLast;

public class TreeUtils {

  private TreeUtils() {
    // utility class - do not instantiate
  }

  public static boolean isDescendant(Tree tree, Tree potentialParent) {
    Tree parent = tree;
    while (parent != null && !potentialParent.equals(parent)) {
      parent = parent.getParent();
    }
    return potentialParent.equals(parent);
  }

  @CheckForNull
  public static Tree findAncestorWithKind(Tree tree, Collection<Tree.Kind> kinds) {
    Tree parent = tree;
    while (parent != null && !kinds.contains(parent.getKind())) {
      parent = parent.getParent();
    }
    return parent;
  }

  @CheckForNull
  public static Tree findAncestorWithKind(Tree tree, Tree.Kind... kinds) {
    return findAncestorWithKind(tree, asList(kinds));
  }

  public static Stream<Tree> descendants(@Nullable Tree root) {
    if (root == null || ((PHPTree) root).isLeaf()) {
      return Stream.empty();
    }
    Spliterator<Tree> spliterator = Spliterators.spliteratorUnknownSize(((PHPTree) root).childrenIterator(), Spliterator.ORDERED);
    Stream<Tree> stream = StreamSupport.stream(spliterator, false);
    return stream.flatMap(tree -> Stream.concat(Stream.of(tree), descendants(tree)));
  }

  public static <T extends Tree> Stream<T> descendants(@Nullable Tree root, Class<T> clazz) {
    return descendants(root)
      .filter(clazz::isInstance)
      .map(clazz::cast);
  }

  public static Optional<Tree> firstDescendant(@Nullable Tree root, Predicate<Tree> predicate) {
    return descendants(root).filter(predicate).findFirst();
  }

  public static <T extends Tree> Optional<T> firstDescendant(Tree root, Class<T> clazz) {
    return (Optional<T>) firstDescendant(root, clazz::isInstance);
  }

  /**
   * Check if a method has an annotation (PHPDoc comment) or attribute (PHP 8+ syntax).
   * The check is case-insensitive for both formats.
   */
  public static boolean hasAnnotationOrAttribute(MethodDeclarationTree tree, String name) {
    // Check PHPDoc annotations
    if (TreeUtils.hasAnnotation(tree, name)) {
      return true;
    }

    // Check PHP 8+ attributes
    String lowerCaseName = name.toLowerCase(Locale.ROOT);
    for (AttributeGroupTree attributeGroup : tree.attributeGroups()) {
      for (AttributeTree attribute : attributeGroup.attributes()) {
        String attributeName = attribute.name().fullyQualifiedName();
        // Extract the simple name from the fully qualified name
        String simpleName = attributeName.substring(attributeName.lastIndexOf('\\') + 1);
        if (simpleName.toLowerCase(Locale.ROOT).equals(lowerCaseName)) {
          return true;
        }
      }
    }

    return false;
  }

  public static boolean hasAnnotation(Tree declaration, String annotation) {
    if (!annotation.startsWith("@")) {
      annotation = "@" + annotation;
    }

    List<SyntaxTrivia> trivias = ((PHPTree) declaration).getFirstToken().trivias();

    if (!trivias.isEmpty()) {
      return getLast(trivias).text().toLowerCase(Locale.ROOT).contains(annotation.toLowerCase(Locale.ROOT));
    }

    return false;
  }

  // Helper method: Gets function name from FunctionCallTree (replaces CheckUtils.functionName)
  @Nullable
  public static String functionName(FunctionCallTree functionCall) {
    ExpressionTree callee = functionCall.callee();
    if (callee.is(Tree.Kind.CLASS_MEMBER_ACCESS) || callee.is(Tree.Kind.OBJECT_MEMBER_ACCESS)) {
      return nameOf(((MemberAccessTree) callee).member());
    }
    return nameOf(callee);
  }

  // Helper method: Gets name from a tree (replaces CheckUtils.nameOf)
  @Nullable
  public static String nameOf(Tree tree) {
    if (tree.is(Tree.Kind.NAMESPACE_NAME)) {
      return ((NamespaceNameTree) tree).qualifiedName();
    } else if (tree.is(Tree.Kind.NAME_IDENTIFIER)) {
      return ((NameIdentifierTree) tree).text();
    } else if (tree.is(Tree.Kind.CLASS_MEMBER_ACCESS) || tree.is(Tree.Kind.OBJECT_MEMBER_ACCESS)) {
      MemberAccessTree memberAccess = (MemberAccessTree) tree;
      String className = nameOf(memberAccess.object());
      String memberName = nameOf(memberAccess.member());
      if (className != null && memberName != null) {
        return className + "::" + memberName;
      }
    } else if (tree.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
      VariableIdentifierTree variableIdentifier = (VariableIdentifierTree) tree;
      if ("$this".equals(variableIdentifier.text())) {
        ClassDeclarationTree classDeclaration = (ClassDeclarationTree) TreeUtils.findAncestorWithKind(tree,
          EnumSet.of(Tree.Kind.CLASS_DECLARATION, Tree.Kind.TRAIT_DECLARATION));
        if (classDeclaration != null) {
          return nameOf(classDeclaration.name());
        }
      }
    }
    return null;
  }

  // Helper method: Gets argument from FunctionCallTree (replaces CheckUtils.argument)
  public static Optional<CallArgumentTree> argument(FunctionCallTree call, String name, int position) {
    SeparatedList<CallArgumentTree> callArguments = call.callArguments();

    CallArgumentTree argument = callArguments.stream()
      .filter(a -> a.name() != null)
      .filter(a -> a.name().text().equalsIgnoreCase(name))
      .findFirst()
      .orElse(null);

    if (argument != null) {
      return Optional.of(argument);
    }

    if (callArguments.size() >= position + 1 && callArguments.get(position).name() == null) {
      return Optional.of(callArguments.get(position));
    }

    return Optional.empty();
  }

  public static String trimQuotes(LiteralTree literalTree) {
    if (literalTree.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
      String value = literalTree.value();
      return value.substring(1, value.length() - 1);
    }
    throw new IllegalArgumentException("Cannot trim quotes from non-string literal");
  }
}
