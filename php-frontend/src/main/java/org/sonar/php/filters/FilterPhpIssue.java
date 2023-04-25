package org.sonar.php.filters;

public interface FilterPhpIssue {

  boolean accept(String fileName, String ruleName, int line);
}
