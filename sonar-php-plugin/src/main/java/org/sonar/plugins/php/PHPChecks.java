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
package org.sonar.plugins.php;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PHPCustomRuleRepository;

/**
 * Wrapper around Checks Object to ease the manipulation of the different PHP rule repositories.
 */
public class PHPChecks {

  private final CheckFactory checkFactory;
  private Set<Checks<PHPCheck>> checksByRepository = new HashSet<>();

  private PHPChecks(CheckFactory checkFactory) {
    this.checkFactory = checkFactory;
  }

  public static PHPChecks createPHPCheck(CheckFactory checkFactory) {
    return new PHPChecks(checkFactory);
  }

  public PHPChecks addChecks(String repositoryKey, Iterable<Class<?>> checkClass) {
    checksByRepository.add(checkFactory
      .<PHPCheck>create(repositoryKey)
      .addAnnotatedChecks(checkClass));

    return this;
  }

  public PHPChecks addCustomChecks(@Nullable PHPCustomRuleRepository[] customRuleRepositories) {
    if (customRuleRepositories != null) {

      for (PHPCustomRuleRepository ruleRepository : customRuleRepositories) {
        addChecks(ruleRepository.repositoryKey(), new ArrayList<>(ruleRepository.checkClasses()));
      }
    }

    return this;
  }

  public List<PHPCheck> all() {
    List<PHPCheck> allVisitors = new ArrayList<>();

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
