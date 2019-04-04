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
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S4835")
public class SessionFixationCheck extends PHPVisitorCheck {

    private static final String MESSAGE_USER_SUPPLIED_SESSION_ID = "Make sure the session ID being set here is cryptographically secure and is not user-supplied.";

    @Override
    public void visitFunctionCall(FunctionCallTree tree) {
        if (isSessionIdFunctionWithArguments(tree)) {
            context().newIssue(this, tree, MESSAGE_USER_SUPPLIED_SESSION_ID);
        }

        super.visitFunctionCall(tree);
    }

    private boolean isSessionIdFunctionWithArguments(FunctionCallTree tree) {
        return isSessionIdFunction(tree) && hasArguments(tree);
    }

    private boolean isSessionIdFunction(FunctionCallTree tree) {
        String qualifiedName = ((NamespaceNameTree) tree.callee()).qualifiedName();
        return qualifiedName.equalsIgnoreCase("session_id");
    }

    private boolean hasArguments(FunctionCallTree tree) {
        return !tree.arguments().isEmpty();
    }
}
