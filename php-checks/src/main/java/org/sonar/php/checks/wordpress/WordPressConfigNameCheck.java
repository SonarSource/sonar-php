/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.php.checks.wordpress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = "S6349")
public class WordPressConfigNameCheck extends WordPressConfigVisitor {

  private static final String MESSAGE = "Unknown WordPress option \"%s\". Did you mean \"%s\"?";

  private static final List<String> KNOWN_OPTIONS = Arrays.asList(
    "ADMIN_COOKIE_PATH",
    "ALTERNATE_WP_CRON",
    "AUTH_KEY",
    "AUTH_SALT",
    "AUTOMATIC_UPDATER_DISABLED",
    "AUTOSAVE_INTERVAL",
    "CONCATENATE_SCRIPTS",
    "COOKIE_DOMAIN",
    "COOKIEPATH",
    "CUSTOM_USER_META_TABLE",
    "CUSTOM_USER_TABLE",
    "DB_CHARSET",
    "DB_COLLATE",
    "DB_HOST",
    "DB_NAME",
    "DB_PASSWORD",
    "DB_USER",
    "DISABLE_WP_CRON",
    "DISALLOW_FILE_EDIT",
    "DISALLOW_FILE_MODS",
    "DO_NOT_UPGRADE_GLOBAL_TABLES",
    "EMPTY_TRASH_DAYS",
    "FORCE_SSL_ADMIN",
    "FS_CHMOD_DIR",
    "FS_CHMOD_FILE",
    "FS_METHOD",
    "FTP_BASE",
    "FTP_CONTENT_DIR",
    "FTP_HOST",
    "FTP_PASS",
    "FTP_PLUGIN_DIR",
    "FTP_PRIKEY",
    "FTP_PUBKEY",
    "FTP_SSL",
    "FTP_USER",
    "IMAGE_EDIT_OVERWRITE",
    "LOGGED_IN_KEY",
    "LOGGED_IN_SALT",
    "NOBLOGREDIRECT",
    "NONCE_KEY",
    "NONCE_SALT",
    "PLUGINDIR",
    "PLUGINS_COOKIE_PATH",
    "SAVEQUERIES",
    "SCRIPT_DEBUG",
    "SECURE_AUTH_KEY",
    "SECURE_AUTH_SALT",
    "SITECOOKIEPATH",
    "STYLESHEETPATH",
    "TEMPLATEPATH",
    "UPLOADS",
    "WP_ACCESSIBLE_HOSTS",
    "WP_ALLOW_MULTISITE",
    "WP_ALLOW_REPAIR",
    "WP_AUTO_UPDATE_CORE",
    "WP_CACHE",
    "WP_CONTENT_DIR",
    "WP_CONTENT_URL",
    "WP_CRON_LOCK_TIMEOUT",
    "WP_DEBUG",
    "WP_DEBUG_DISPLAY",
    "WP_DEBUG_LOG",
    "WP_DISABLE_FATAL_ERROR_HANDLER",
    "WP_ENVIRONMENT_TYPE",
    "WP_HOME",
    "WP_HTTP_BLOCK_EXTERNAL",
    "WPLANG",
    "WP_LANG_DIR",
    "WP_MAX_MEMORY_LIMIT",
    "WP_MEMORY_LIMIT",
    "WP_PLUGIN_DIR",
    "WP_PLUGIN_URL",
    "WP_POST_REVISIONS",
    "WP_SITEURL");

  @RuleProperty(
    key = "customOptions",
    description = "Comma separated list of custom options")
  String customOptions = "";

  List<String> knownAndCustomOptions;

  @Override
  public void init() {
    knownAndCustomOptions = new ArrayList<>(KNOWN_OPTIONS);
    Stream.of(customOptions.split(","))
      .map(String::trim)
      .filter(s -> !s.isEmpty())
      .forEach(knownAndCustomOptions::add);
    super.init();
  }

  @Override
  void visitConfigDeclaration(FunctionCallTree config) {
    configKeyString(config).ifPresent(key -> checkConfig(config, key));
  }

  private void checkConfig(FunctionCallTree configTree, String configKey) {
    if (knownAndCustomOptions.contains(configKey)) {
      return;
    }

    for (String knownOption : knownAndCustomOptions) {
      if (knownOption.equalsIgnoreCase(configKey) || levenshteinDistance(configKey, knownOption) == 1) {
        newIssue(configTree, String.format(MESSAGE, configKey, knownOption));
        return;
      }
    }
  }

  private static int levenshteinDistance(String from, String to) {
    int[][] dp = new int[from.length() + 1][to.length() + 1];

    for (int i = 0; i <= from.length(); i++) {
      for (int j = 0; j <= to.length(); j++) {
        if (i == 0) {
          dp[i][j] = j;
        } else if (j == 0) {
          dp[i][j] = i;
        } else {
          dp[i][j] = min(dp[i - 1][j - 1] + costOfSubstitution(from.charAt(i - 1), to.charAt(j - 1)),
            dp[i - 1][j] + 1,
            dp[i][j - 1] + 1);
        }
      }
    }

    return dp[from.length()][to.length()];
  }

  private static int costOfSubstitution(char a, char b) {
    return a == b ? 0 : 1;
  }

  private static int min(int... numbers) {
    return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
  }
}
