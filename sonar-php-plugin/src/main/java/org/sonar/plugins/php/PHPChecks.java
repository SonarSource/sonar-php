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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.php.api.visitors.PHPCustomRuleRepository;
import org.sonar.plugins.php.api.visitors.PHPCheck;

/**
 * Wrapper around Checks Object to ease the manipulation of the different PHP rule repositories.
 */
public class PHPChecks {

  private final CheckFactory checkFactory;
  private Set<Checks<PHPCheck>> checksByRepository = Sets.newHashSet();

  private PHPChecks(CheckFactory checkFactory) {
    this.checkFactory = checkFactory;
  }

  public static PHPChecks createPHPCheck(CheckFactory checkFactory) {
    return new PHPChecks(checkFactory);
  }

  public PHPChecks addChecks(String repositoryKey, Iterable<Class> checkClass) {
    checksByRepository.add(checkFactory
      .<PHPCheck>create(repositoryKey)
      .addAnnotatedChecks(checkClass));

    return this;
  }

  public PHPChecks addCustomChecks(@Nullable PHPCustomRuleRepository[] customRuleRepositories) {
    if (customRuleRepositories != null) {

      for (PHPCustomRuleRepository ruleRepository : customRuleRepositories) {
        addChecks(ruleRepository.repositoryKey(), Lists.newArrayList(ruleRepository.checkClasses()));
      }
    }

    return this;
  }

  public List<PHPCheck> all() {
    List<PHPCheck> allVisitors = Lists.newArrayList();

    for (Checks<PHPCheck> checks : checksByRepository) {
      allVisitors.addAll(checks.all());
    }

    return allVisitors;
  }

  @Nullable
  public RuleKey ruleKeyFor(PHPCheck check) {
    RuleKey ruleKey;

    for (Checks<PHPCheck> checks : checksByRepository) {
      ruleKey = checks.ruleKey(check);

      if (ruleKey != null) {
        return ruleKey;
      }
    }
    return null;
  }
}
