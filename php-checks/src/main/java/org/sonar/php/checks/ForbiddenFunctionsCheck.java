/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * dev@sonar.codehaus.org
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

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import java.util.Arrays;
import java.util.List;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;

@Rule(
        key = "S666",
        priority = Priority.MAJOR)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority
        = Priority.MAJOR)
public class ForbiddenFunctionsCheck extends SquidCheck<Grammar> {

    static final String DEFAULT = "var_dump";

    @RuleProperty(
            key = "functions",
            type = "STRING",
            defaultValue = DEFAULT)
    String functions = DEFAULT;
    List<String> functionList;

    @Override
    public void init() {
        super.init();
        subscribeTo(PHPGrammar.STATEMENT);
        functionList = Arrays.asList(functions.split("[ ,]"));
    }

    @Override
    public void visitNode(AstNode astNode) {
        if (astNode.is(PHPGrammar.STATEMENT)) {
            for (String function : functionList) {
                if (function.equalsIgnoreCase(astNode.getTokenOriginalValue())) {
                    getContext().createLineViolation(this,
                            "Use of forbidden function {0}",
                            astNode, function);
                }
            }
        }
    }
}
