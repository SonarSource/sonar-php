/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
package org.sonar.php.regex.ast;

import org.junit.Test;
import org.sonarsource.analyzer.commons.regex.ast.LookAroundTree;
import org.sonarsource.analyzer.commons.regex.ast.RegexTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.php.regex.RegexParserTestUtils.assertSuccessfulParse;
import static org.sonar.php.regex.RegexParserTestUtils.assertType;

public class ConditionalSubpatternsTreeTest {

  @Test
  public void conditionalSubpatternTree_with_reference() {
    assertConditionalSubpatterns("'/(?(1)a)/'", ReferenceConditionTree.class, false);
    assertConditionalSubpatterns("'/(?(2)a|b)/'", "2", true);
    assertConditionalSubpatterns("'/(?(+1)a)/'", "+1", false);
    assertConditionalSubpatterns("'/(?(+12)a)/'", "+12", false);
    assertConditionalSubpatterns("'/(?(-1)a)/'", "-1", false);
    assertConditionalSubpatterns("'/(?(<name>)a)/'", "<name>", false);
    assertConditionalSubpatterns("\"/(?('name')a)/\"", "'name'", false);
    assertConditionalSubpatterns("'/(?(R)a)/'", "R", false);
    assertConditionalSubpatterns("'/(?(R2)a)/'", "R2", false);
    assertConditionalSubpatterns("'/(?(R&name)a)/'", "R&name", false);
  }

  @Test
  public void conditionalSubpatternTree_with_look_around() {
    assertConditionalSubpatterns("'/(?(?=[^a-z])2)/'", LookAroundTree.class, false);
    assertConditionalSubpatterns("'/(?(?=[^a-z])2|3)/'", LookAroundTree.class, true);
    assertConditionalSubpatterns("'/(?(?<=[^a-z])2)/'", LookAroundTree.class, false);
    assertConditionalSubpatterns("'/(?(?![^a-z])2)/'", LookAroundTree.class, false);
    assertConditionalSubpatterns("'/(?(?<![^a-z])2)/'", LookAroundTree.class, false);
    assertConditionalSubpatterns("'/(?(?=[^a-z]*[a-z])\\d{2}-[a-z]{3}-\\d{2}|\\d{2}-\\d{2}-\\d{2} )/x'", LookAroundTree.class, true);
  }

  private ConditionalSubpatternsTree assertConditionalSubpatterns(String regex, boolean hasNoPattern) {
    RegexTree tree = assertSuccessfulParse(regex);
    ConditionalSubpatternsTree conditionalSubpattern = assertType(ConditionalSubpatternsTree.class, tree);
    assertThat(conditionalSubpattern.getYesPattern()).isNotNull();
    if (hasNoPattern) {
      assertThat(conditionalSubpattern.getPipe()).isNotNull();
      assertThat(conditionalSubpattern.getNoPattern()).isNotNull();
    } else {
      assertThat(conditionalSubpattern.getPipe()).isNull();
      assertThat(conditionalSubpattern.getNoPattern()).isNull();
    }
    return conditionalSubpattern;
  }

  private void assertConditionalSubpatterns(String regex, Class<?> conditionType, boolean hasNoPattern) {
    ConditionalSubpatternsTree conditionalSubpattern = assertConditionalSubpatterns(regex, hasNoPattern);
    assertType(conditionType, conditionalSubpattern.getCondition());
  }

  private void assertConditionalSubpatterns(String regex, String condition, boolean hasNoPattern) {
    ConditionalSubpatternsTree conditionalSubpattern = assertConditionalSubpatterns(regex, hasNoPattern);
    ReferenceConditionTree groupReferenceCondition = assertType(ReferenceConditionTree.class, conditionalSubpattern.getCondition());
    assertThat(groupReferenceCondition.getReference()).isEqualTo(condition);
  }
}
