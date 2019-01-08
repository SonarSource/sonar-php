/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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
package org.sonar.php.checks.utils.namespace;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.tree.statement.NamespaceStatementTree;
import org.sonar.plugins.php.api.tree.statement.UseClauseTree;
import org.sonar.plugins.php.api.tree.statement.UseStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

public class NamespaceAwareVisitor extends PHPVisitorCheck {

  private QualifiedName currentNamespace;
  private Map<String, QualifiedName> aliases;

  @Override
  public void init() {
    super.init();
    currentNamespace = null;
    aliases = new HashMap<>();
  }

  @Override
  public void visitNamespaceStatement(NamespaceStatementTree tree) {
    aliases.clear();
    currentNamespace = tree.namespaceName() != null ? QualifiedName.create(tree.namespaceName()) : null;
    super.visitNamespaceStatement(tree);

    boolean isBracketedNamespace = tree.openCurlyBrace() != null;
    if (isBracketedNamespace) {
      currentNamespace = null;
      aliases.clear();
    }
  }

  @Override
  public void visitUseStatement(UseStatementTree tree) {
    super.visitUseStatement(tree);
    QualifiedName namespacePrefix = getPrefix(tree);

    tree.clauses().forEach(useClauseTree -> {
      String alias = getAliasName(useClauseTree);
      QualifiedName originalName = getOriginalFullyQualifiedName(namespacePrefix, useClauseTree);
      aliases.put(alias, originalName);
    });
  }

  /**
   * @return the fully qualified name depending on current context (current namespace and aliases defined)
   */
  protected QualifiedName getFullyQualifiedName(NamespaceNameTree name) {
    QualifiedName qualifiedName = QualifiedName.create(name);
    if (name.isFullyQualified()) {
      return qualifiedName;
    } else {
      QualifiedName originalName = aliases.get(qualifiedName.firstPart());
      if (originalName != null) {
        return qualifiedName.withOriginalName(originalName);
      } else if (currentNamespace == null) {
        // No alias and in global namespace
        return qualifiedName;
      } else {
        return QualifiedName.create(currentNamespace, qualifiedName);
      }
    }
  }

  private static QualifiedName getOriginalFullyQualifiedName(@Nullable QualifiedName namespacePrefix, UseClauseTree useClauseTree) {
    QualifiedName originalName = QualifiedName.create(useClauseTree.namespaceName());
    if (namespacePrefix != null) {
      originalName = QualifiedName.create(namespacePrefix, originalName);
    }
    return originalName;
  }

  @Nullable
  private static QualifiedName getPrefix(UseStatementTree useStatementTree) {
    QualifiedName namespacePrefix;
    if (useStatementTree.prefix() != null) {
      namespacePrefix = QualifiedName.create(useStatementTree.prefix());
    } else {
      namespacePrefix = null;
    }
    return namespacePrefix;
  }

  private static String getAliasName(UseClauseTree useClauseTree) {
    NameIdentifierTree aliasNameTree = useClauseTree.alias();
    if (aliasNameTree != null) {
      return aliasNameTree.text();
    } else {
      return useClauseTree.namespaceName().name().text();
    }
  }

}
