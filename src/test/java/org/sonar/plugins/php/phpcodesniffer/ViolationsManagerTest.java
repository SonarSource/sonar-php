package org.sonar.plugins.php.phpcodesniffer;

import org.junit.Test;
import static org.mockito.Mockito.*;
import org.sonar.commons.resources.Resource;
import org.sonar.commons.rules.Rule;
import org.sonar.commons.rules.RuleFailureLevel;
import org.sonar.commons.rules.RuleFailureParam;
import org.sonar.plugins.api.maven.ProjectContext;
import org.sonar.plugins.api.rules.RulesManager;

public class ViolationsManagerTest {

  @Test
  public void shouldCreateViolations() {
    ProjectContext context = mock(ProjectContext.class);
    RulesManager rulesManager = mock(RulesManager.class);
    Resource file = mock(Resource.class);
    Rule rule = mock(Rule.class);

    String pluginKey = "test";
    String ruleKey = "aRuleKey";
    String message = "a rule message description";
    when(rulesManager.getPluginRule(pluginKey, ruleKey)).thenReturn(rule);
    ViolationsManager violationsManager = new ViolationsManager(context, rulesManager, pluginKey);

    violationsManager.createViolation(file, "error", "10", ruleKey, message);
    verify(context).addViolation(eq(file), eq(rule), eq(message), eq(RuleFailureLevel.ERROR), any(RuleFailureParam.class));

    violationsManager.createViolation(file, "warning", "3", ruleKey, message);
    verify(context).addViolation(eq(file), eq(rule), eq(message), eq(RuleFailureLevel.WARNING), any(RuleFailureParam.class));  }

}
