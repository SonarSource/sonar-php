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
package org.sonar.plugins.php;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.profiles.XMLProfileParser;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FakeProfileParser extends XMLProfileParser {

  public FakeProfileParser() {
    super(ruleFinder());
  }

  private static RuleFinder ruleFinder() {
    return when(mock(RuleFinder.class).findByKey(anyString(), anyString())).thenAnswer(new Answer<Rule>() {
      public Rule answer(InvocationOnMock invocation) {
        Object[] arguments = invocation.getArguments();
        return Rule.create((String) arguments[0], (String) arguments[1], (String) arguments[1]);
      }
    }).getMock();
  }

}
