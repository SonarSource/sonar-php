import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Akram Ben Aissi
 * 
 */
public class PHPCodeSnifferRulesExtractor {

  private static final int SNIFF_DESCRIPTION_POSITION = 1;
  private static final int SNIFF_LEVEL_POSITION = 2;
  private static final int SNIFF_NAME_POSITION = 3;
  private static final String $PHPCS_FILE_ADD_ERROR_OR_WARNING = "\\s*(.*)->add(Warning|Error)\\([^,]*, [^,]*, ([^,]*)(,[^,]*)*\\);";
  private static final String $ERROR_DELIMITOR = "\\s*\\$error\\s*=(.*);";
  private static final String ROOT_PATH = "D:\\tmp\\phpcs\\CodeSniffer\\Standards\\";

  private static final String EXTENSION = "Sniff.php";
  private static final String SNIFFS_DIRECTORY = ".Sniffs";

  /**
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    String l = "      $phpcsFile->addWarning($error, $stackPtr, 'NotAllowed');";
    Pattern p = Pattern.compile($PHPCS_FILE_ADD_ERROR_OR_WARNING);
    Matcher matcher = p.matcher(l);
    matcher.groupCount();
    matcher.matches();
    // matcher.group(0);
    matcher.group(1);
    matcher.group(2);
    matcher.group(3);
    // matcher.group(4);
    // matcher.group(5);

    String l2 = "       $phpcsFile->addError($error, $stackPtr, 'NotAllowedWarning', $data);";
    Matcher matcher2 = p.matcher(l);
    matcher2 = p.matcher(l2);
    matcher.groupCount();
    matcher2.matches();
    // matcher.group(0);
    matcher2.group(1);
    matcher2.group(2);
    matcher2.group(3);
    // matcher2.group(4);
    // matcher2.group(5);
    List<File> files = getSniffFiles(ROOT_PATH);
    List<String> sniffs = extractSniffNames(files);
    writeSniffsToFile("d:/tmp/all-phpcs-rules.csv", sniffs);

  }

  /**
   * @param path
   * @param sniffs
   * @throws IOException
   */
  static void writeSniffsToFile(String path, List<String> sniffs) throws IOException {
    FileOutputStream stream = new FileOutputStream(path);
    OutputStreamWriter streamWriter = new OutputStreamWriter(stream);
    BufferedWriter writer = new BufferedWriter(streamWriter);
    for (String sniff : sniffs) {
      writer.write(sniff);
    }
    writer.close();
    stream.close();
  }

  /**
   * @param files
   * @return
   * @throws IOException
   */
  static List<String> extractSniffNames(List<File> files) throws IOException {
    List<String> sniffs = new ArrayList<String>();
    String header = "key,priority,category,name,configKey,description\n";
    sniffs.add(header);

    Pattern descriptionPattern = Pattern.compile($ERROR_DELIMITOR);
    Pattern addErrorPattern = Pattern.compile($PHPCS_FILE_ADD_ERROR_OR_WARNING);

    for (File file : files) {
      FileInputStream inputStream = new FileInputStream(file);
      InputStreamReader streamReader = new InputStreamReader(inputStream);
      BufferedReader reader = new BufferedReader(streamReader);
      String prefix = file.getAbsolutePath().replace(ROOT_PATH, "").replace(EXTENSION, "");
      prefix = prefix.replaceAll("\\\\", ".").replace(SNIFFS_DIRECTORY, "");
      String line = reader.readLine();
      while (line != null) {
        Matcher descriptionMatcher = descriptionPattern.matcher(line);
        if (descriptionMatcher.matches()) {
          String description = clean(descriptionMatcher.group(SNIFF_DESCRIPTION_POSITION));
          // On recherche la prochaine occurence de addError ou addWarning
          Matcher sniffMatcher = addErrorPattern.matcher(line);
          String key = "";
          while ((line = reader.readLine()) != null && !sniffMatcher.matches()) {
            sniffMatcher = addErrorPattern.matcher(line);
          }
          if (line != null) {
            key = clean(sniffMatcher.group(SNIFF_NAME_POSITION));
          }
          String name = getName(prefix);
          // key,priority,category,name,configKey,description
          StringBuilder sniff = new StringBuilder(prefix).append(".").append(key).append(",");
          String level = "MAJOR";
          sniff.append(level).append(",");
          sniff.append("Maintainability").append(",");
          sniff.append(name).append(key).append(",");
          sniff.append("rulesets/").append(name).append(",");
          if ("".equals(description)) {
            sniff.append("No description available");
          }
          sniff.append(description).append("\n");
          sniffs.add(sniff.toString());
        }
        // Sin on est pas arrivé à la fin du fichier on recommence.
        if (line != null) {
          line = reader.readLine();
        }
      }
    }
    return sniffs;
  }

  /**
   * @param prefix
   * @return
   */
  private static String getName(String prefix) {
    String result = prefix.trim();
    return result.substring(result.lastIndexOf(".") + 1);
  }

  private static String clean(String s) {
    String result = s.trim();
    return result.substring(1, result.length() - 1);
  }

  /**
   * @param ROOT_PATH
   * @return a recursively list of all directories contained in path
   */
  static List<File> getSniffFiles(String rootPath) {
    return getSniffFiles(rootPath, true);
  }

  /**
   * @param ROOT_PATH
   * @return a recursively list of all directories contained in path
   */
  private static List<File> getSniffFiles(String rootPath, boolean start) {
    List<File> files = new ArrayList<File>();
    File root = new File(rootPath);
    if ( !start && !root.isDirectory()) {
      throw new RuntimeException("Path " + rootPath + " is not a directory");
    }
    for (File child : root.listFiles()) {
      if (child.isDirectory()) {
        List<File> sniffFiles = directoryContainsOnlySniffs(child);
        if (sniffFiles != null) {
          files.addAll(sniffFiles);
        } else {
          files.addAll(getSniffFiles(child.getAbsolutePath(), false));
        }
      }
    }
    return files;
  }

  /**
   * @param child
   * @return
   */
  private static List<File> directoryContainsOnlySniffs(File child) {
    List<File> files = new ArrayList<File>();
    for (File file : child.listFiles()) {
      if (file.isDirectory() || !file.getName().endsWith(EXTENSION)) {
        return null;
      }
      files.add(file);
    }
    return files;
  }

}
