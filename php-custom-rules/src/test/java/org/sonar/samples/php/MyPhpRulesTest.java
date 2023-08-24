package org.sonar.samples.php;

import org.junit.Test;
import org.sonar.api.server.rule.RulesDefinition;

import static org.junit.Assert.assertEquals;

public class MyPhpRulesTest {

  @Test
  public void rules() {
    MyPhpRules rulesDefinition = new MyPhpRules();
    RulesDefinition.Context context = new RulesDefinition.Context();
    rulesDefinition.define(context);
    RulesDefinition.Repository repository = context.repository("custom");
    assertEquals(2, repository.rules().size());
  }
}
