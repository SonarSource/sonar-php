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
package org.sonar.plugins.php.api.tree;

import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassFieldDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.UseDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.UseDeclarationsTree;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.TraitUseStatementTree;
import org.sonar.plugins.php.api.tree.statement.UseStatementTree;
import org.sonar.sslr.grammar.GrammarRuleKey;

import com.google.common.annotations.Beta;
import com.sonar.sslr.api.AstNodeType;

/**
 * Common interface for all nodes in an abstract syntax tree.
 */
@Beta
public interface Tree {

  boolean is(Kind... kind);

  public enum Kind implements AstNodeType, GrammarRuleKey {

    /**
     * {@link ClassTree}
     */
    CLASS_DECLARATION(ClassTree.class),

    /**
     * {@link ClassTree}
     */
    INTERFACE_DECLARATION(ClassTree.class),

     /**
     * {@link ClassTree}
     */
    TRAIT_DECLARATION(ClassTree.class),

    /**
     * {@link MethodDeclarationTree}
     */
    METHOD_DECLARATION(MethodDeclarationTree.class),

    /**
     * {@link FunctionDeclarationTree}
     */
    FUNCTION_DECLARATION(FunctionDeclarationTree.class),

    /**
     * {@link ClassFieldDeclarationTree}
     */
    CLASS_FIELD_DECLARATION(ClassFieldDeclarationTree.class),

    /**
     * {@link ClassFieldDeclarationTree}
     */
    CLASS_CONSTANT_FIELD_DECLARATION(ClassFieldDeclarationTree.class),

    /**
     * {@link VariableDeclarationTree}
     */
    VARIABLE_DECLARATION(VariableDeclarationTree.class),

    /**
     * {@link org.sonar.plugins.php.api.tree.statement.UseStatementTree}
     */
    USE_DECLARATIONS(UseStatementTree.class),

    /**
     * {@link UseDeclarationTree}
     */
    USE_DECLARATION(UseDeclarationTree.class),

    /**
     * {@link TraitUseStatementTree}
     */
    TRAIT_USE_STATEMENT(TraitUseStatementTree.class),

    /**
     * {@link BlockTree}
     */
    BLOCK(BlockTree.class),

    /**
     * {@link SyntaxToken}
     */
    TOKEN(SyntaxToken.class);


    final Class<? extends Tree> associatedInterface;

    private Kind(Class<? extends Tree> associatedInterface) {
      this.associatedInterface = associatedInterface;
    }

    public Class<? extends Tree> getAssociatedInterface() {
      return associatedInterface;
    }
  }

}
