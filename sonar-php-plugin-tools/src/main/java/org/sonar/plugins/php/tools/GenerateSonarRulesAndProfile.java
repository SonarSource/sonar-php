package org.sonar.plugins.php.tools;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.sonar.plugins.php.tools.export.CSVRulesToSonarProfile;
import org.sonar.plugins.php.tools.export.CSVRulesToSonarRules;
import org.sonar.plugins.php.tools.phpcs.PHPCodeSnifferRulesExtractor;

/**
 * Class that "tries" to generate the Sonar repository and profile files from PHP CodeSNiffer source files.
 * 
 * IMPORTANT NOTE: to generate the list of rules and the profile files, this class parses the source code of PHP CodeSniffer to find the
 * different sniffs. This is far from being perfect: it heavily depends on the way the sniffs are developed in PHP, which may change over
 * time.
 * 
 * TODO: see with the creator of PHP CodeSniffer if it's possible to create a listing of those rules.
 * 
 * 
 * @author Akram Ben Aissi
 * @author Fabrice Bellingard
 */

public class GenerateSonarRulesAndProfile {

  private static final String INPUT_FOLDER = "/tmp/PHP_CodeSniffer-1.3.1/CodeSniffer/Standards";
  private static final String OUTPUT_FOLDER = "target/generated-files/";

  private static final String SONAR_PROFILE_XML = OUTPUT_FOLDER + "php-profile-with-cs.xml";
  private static final String SONAR_RULES_XML = OUTPUT_FOLDER + "all-phpcs-rules.xml";

  public static void main(String[] args) throws IOException {
    PHPCodeSnifferRulesExtractor extractor = new PHPCodeSnifferRulesExtractor(INPUT_FOLDER, OUTPUT_FOLDER);
    File extractedSniffs = extractor.extractSniffs();

    CSVRulesToSonarRules.generateSonarRulesXml(extractedSniffs.getAbsolutePath(), SONAR_RULES_XML);
    CSVRulesToSonarProfile.generateSonarProfileXml(extractedSniffs.getAbsolutePath(), SONAR_PROFILE_XML);

    FileUtils.deleteQuietly(extractedSniffs);
    System.out.println("SUCCESS: rules and profile generated in " + new File(OUTPUT_FOLDER).getAbsolutePath());
  }
}
