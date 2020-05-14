package org.sonar.php.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S4143")
public class OverwrittenArrayElementCheck extends PHPVisitorCheck {

}
