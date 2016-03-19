/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
package org.sonar.php.checks;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

@Rule(
        key = AliasFunctionUsageCheck.KEY,
        name = "Alias functions should not be used",
        priority = Priority.MAJOR,
        tags = {Tags.OBSOLETE})
@ActivatedByDefault
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.LANGUAGE_RELATED_PORTABILITY)
@SqaleConstantRemediation("5min")
public class AliasFunctionUsageCheck extends PHPVisitorCheck {

    public static final String KEY = "S2050";
    public static final String MESSAGE = "Remove this use of \"%s\"";

    private static final String[] ALIAS_FUNCTIONS = {
        "sizeof",
        "delete",
        "print",
        "is_null",
        "is_double",
        "is_integer",
        "is_long",
        "is_real",
        "create_function",
        "chop",
        "ini_alter",
        "join",
        "key_exists",
        "fputs",
        "is_writeable"
    };

    @Override
    public void visitFunctionCall(FunctionCallTree tree) {
        String callee = tree.callee().toString();

        if (this.isAliasFunction(callee)) {
            context().newIssue(this, String.format(MESSAGE, callee)).tree(tree);
        }

        super.visitFunctionCall(tree);
    }

    private boolean isAliasFunction(String callee) {
        for (String alias : ALIAS_FUNCTIONS) {
            if (alias.equalsIgnoreCase(callee)) {
                return true;
            }
        }

        return false;
    }
}
