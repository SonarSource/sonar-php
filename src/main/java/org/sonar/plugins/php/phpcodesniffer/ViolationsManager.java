package org.sonar.plugins.php.phpcodesniffer;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import org.sonar.commons.resources.Resource;
import org.sonar.commons.rules.Rule;
import org.sonar.commons.rules.RuleFailureLevel;
import org.sonar.commons.rules.RuleFailureParam;
import static org.sonar.plugins.api.maven.MavenCollectorUtils.parseNumber;
import org.sonar.plugins.api.maven.ProjectContext;
import org.sonar.plugins.api.rules.RulesManager;

import java.text.ParseException;

public class ViolationsManager {

  private ProjectContext context;
  private RulesManager rulesManager;
  private String pluginKey;

  public ViolationsManager(ProjectContext context, RulesManager rulesManager, String pluginKey) {
    this.context = context;
    this.rulesManager = rulesManager;
    this.pluginKey = pluginKey;
  }

  public void createViolation(Resource file, String level, String line, String key, String message) {
    Rule rule = rulesManager.getPluginRule(pluginKey, key);
    RuleFailureLevel ruleFailureLevel = levelForViolation(level);
    if (rule != null && file != null) {
      context.addViolation(file, rule, message, ruleFailureLevel, getLineNumberParam(line));
    }
  }

  private RuleFailureLevel levelForViolation(String level) {
    RuleFailureLevel ruleFailureLevel = RuleFailureLevel.INFO;
    if ("error".equals(level)) {
      ruleFailureLevel = RuleFailureLevel.ERROR;
    } else if ("warning".equals(level)) {
      ruleFailureLevel = RuleFailureLevel.WARNING;
    }
    return ruleFailureLevel;
  }

  private RuleFailureParam getLineNumberParam(String line) {
    RuleFailureParam lineParam = null;
    try {
      if (isNotBlank(line)) {
        return new RuleFailureParam("line", parseNumber(line), null);
      }
    } catch (ParseException e) {
      return null;
    }
    return lineParam;
  }

}
