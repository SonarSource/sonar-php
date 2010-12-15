import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Akram Ben Aissi
 * 
 */

public class GenerateSonarRulesAndProfile {

  private static final String INPUT_FOLDER = "D:\\tmp\\phpcs\\CodeSniffer\\Standards\\";

  private static final String OUTPUT_FOLDER = "d:/tmp/";
  private static final String RULES_CSV_FILE = OUTPUT_FOLDER + "all-phpcs-rules.csv";
  private static final String SONAR_PROFILE_XML = OUTPUT_FOLDER + "php-profile-with-cs.xml";
  private static final String SONAR_RULES_XML = OUTPUT_FOLDER + "all-phpcs-rules.xml";

  /**
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    List<File> files = PHPCodeSnifferRulesExtractor.getSniffFiles(INPUT_FOLDER);
    List<String> sniffs = PHPCodeSnifferRulesExtractor.extractSniffNames(files);
    PHPCodeSnifferRulesExtractor.writeSniffsToFile(RULES_CSV_FILE, sniffs);
    CSVRulesToSonarRules.generateSonarRulesXml(RULES_CSV_FILE, SONAR_RULES_XML);
    CSVRulesToSonarProfile.generateSonarProfileXml(RULES_CSV_FILE, SONAR_PROFILE_XML);
  }
}
