/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.parser;

import org.sonar.sslr.internal.matchers.Matcher;
import org.sonar.sslr.internal.vm.Machine;
import org.sonar.sslr.internal.vm.NativeExpression;
import org.sonar.sslr.internal.vm.PatternExpression;
import org.sonar.sslr.internal.vm.StringExpression;

/**
 * This is a variant of {@link StringExpression} which does case-insensitive
 * checks to avoid more expensive regex checks that would otherwise be done
 * through {@link PatternExpression}.
 */
public class CaseInsensitiveStringExpression extends NativeExpression implements Matcher {

  private final String string;

  public CaseInsensitiveStringExpression(String string) {
    this.string = string;
  }

  @Override
  public void execute(Machine machine) {
    if (machine.length() < string.length()) {
      machine.backtrack();
      return;
    }
    for (var i = 0; i < string.length(); i++) {
      if (Character.toLowerCase(machine.charAt(i)) != Character.toLowerCase(string.charAt(i))) {
        machine.backtrack();
        return;
      }
    }
    machine.createLeafNode(this, string.length());
    machine.jump(1);
  }

  @Override
  public String toString() {
    return "String " + string;
  }

}
