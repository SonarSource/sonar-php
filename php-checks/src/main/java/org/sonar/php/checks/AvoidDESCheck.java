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
package org.sonar.php.checks;

import com.google.common.collect.ImmutableSet;
import java.util.Locale;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.NameIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.CheckUtils.trimQuotes;

@Rule(key = "S2278")
public class AvoidDESCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Use the recommended AES (Advanced Encryption Standard) instead.";

  private static final Set<String> MCRYPT_CIPHERS = ImmutableSet.of("MCRYPT_3DES", "MCRYPT_DES", "MCRYPT_DES_COMPAT",
    "MCRYPT_TRIPLEDES");
  private static final String OPENSSL_DES = "des-ede3";

  @Override
  public void visitNameIdentifier(NameIdentifierTree tree) {
    if (MCRYPT_CIPHERS.contains(tree.text())) {
      context().newIssue(this, tree, MESSAGE);
    }
    super.visitNameIdentifier(tree);
  }

  @Override
  public void visitLiteral(LiteralTree tree) {
    String literal = trimQuotes(tree.value().toLowerCase(Locale.ROOT));
    if (literal.startsWith(OPENSSL_DES)) {
      context().newIssue(this, tree, MESSAGE);
    }
    super.visitLiteral(tree);
  }

}
