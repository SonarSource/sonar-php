/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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
package org.sonar.php.checks.security;

import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S4835")
public class SessionFixationCheck extends PHPVisitorCheck {

    private static final String MESSAGE_HARDCODED_SESSION_ID = "Session IDs must be unique and should not be hardcoded.";
    private static final String MESSAGE_USER_SUPPLIED_DATA = "Make sure the session ID being set here is cryptographically secure and is not user-supplied.";

    @Override
    public void visitFunctionCall(FunctionCallTree tree) {
        if (isSessionIdFunctionWithArguments(tree)) {
            if (hasHardcodedStringArgument(tree)) {
                createIssue(tree, MESSAGE_HARDCODED_SESSION_ID);
            } else {
                createIssue(tree, MESSAGE_USER_SUPPLIED_DATA);
            }
        }

        super.visitFunctionCall(tree);
    }

    private boolean isSessionIdFunctionWithArguments(FunctionCallTree tree) {
        String qualifiedName = ((NamespaceNameTree) tree.callee()).qualifiedName();
        return qualifiedName.equalsIgnoreCase("session_id") && hasArguments(tree);
    }

    private boolean hasHardcodedStringArgument(FunctionCallTree tree) {
        return tree.arguments().get(0).is(Tree.Kind.REGULAR_STRING_LITERAL);
    }

    private boolean hasArguments(FunctionCallTree tree) {
        return tree.arguments().size() >= 1;
    }

    private void createIssue(FunctionCallTree tree, String message) {
        context().newIssue(this, tree, message);
    }
}
