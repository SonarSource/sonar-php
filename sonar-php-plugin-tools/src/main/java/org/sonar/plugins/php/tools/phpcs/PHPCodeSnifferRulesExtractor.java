package org.sonar.plugins.php.tools.phpcs;

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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

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
  private static final String RULES_CSV_FILE = "all-phpcs-rules.csv";

  private static final String EXTENSION = "Sniff.php";
  private static final String SNIFFS_DIRECTORY = ".Sniffs";

  private String outputDir;
  private String inputDir;

  public PHPCodeSnifferRulesExtractor(String inputDir, String outputDir) {
    this.inputDir = new File(inputDir).getAbsolutePath() + File.separator;
    this.outputDir = outputDir;
  }

  public File extractSniffs() throws IOException {
    File outputFolder = new File(outputDir);
    if (outputFolder.exists()) {
      FileUtils.deleteDirectory(outputFolder);
    }
    outputFolder.mkdirs();

    List<File> files = getSniffFiles(inputDir);
    List<String> sniffs = extractSniffNames(files);
    writeSniffsToFile(outputDir + RULES_CSV_FILE, sniffs);

    return new File(outputDir + RULES_CSV_FILE);
  }

  /**
   * @param path
   * @param sniffs
   * @throws IOException
   */
  private void writeSniffsToFile(String path, List<String> sniffs) throws IOException {
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
  private List<String> extractSniffNames(List<File> files) throws IOException {
    List<String> sniffs = new ArrayList<String>();
    String header = "key,priority,name,configKey,description\n";
    sniffs.add(header);

    Pattern descriptionPattern = Pattern.compile($ERROR_DELIMITOR);
    Pattern addErrorPattern = Pattern.compile($PHPCS_FILE_ADD_ERROR_OR_WARNING);

    for (File file : files) {
      FileInputStream inputStream = new FileInputStream(file);
      InputStreamReader streamReader = new InputStreamReader(inputStream);
      BufferedReader reader = new BufferedReader(streamReader);
      String prefix = file.getAbsolutePath().replace(inputDir, "").replace(EXTENSION, "");
      prefix = prefix.replaceAll("\\\\", ".").replaceAll("/", ".").replace(SNIFFS_DIRECTORY, "");
      String line = reader.readLine();
      String description = null;
      while (line != null) {
        Matcher descriptionMatcher = descriptionPattern.matcher(line);
        Matcher sniffMatcher = addErrorPattern.matcher(line);
        if (descriptionMatcher.matches()) {
          description = clean(descriptionMatcher.group(SNIFF_DESCRIPTION_POSITION));
        } else if (sniffMatcher.matches() && description != null) {
          String key = clean(sniffMatcher.group(SNIFF_NAME_POSITION));
          if ( !StringUtils.isBlank(key) && Character.isUpperCase(key.charAt(0))) {
            String name = getName(prefix);
            // key,priority,category,name,configKey,description
            StringBuilder sniff = new StringBuilder(prefix).append(".").append(key).append(",");
            String level = "MAJOR";
            sniff.append(level).append(",");
            sniff.append(name).append(key).append(",");
            sniff.append("rulesets/").append(name).append(",");
            if ("".equals(description)) {
              sniff.append("No description available");
            }
            sniff.append(description).append("\n");
            sniffs.add(sniff.toString());
          }
          description = null;
        }

        line = reader.readLine();
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
  private List<File> getSniffFiles(String rootPath) {
    return getSniffFiles(rootPath, true);
  }

  /**
   * @param ROOT_PATH
   * @return a recursively list of all directories contained in path
   */
  private List<File> getSniffFiles(String rootPath, boolean start) {
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
  private List<File> directoryContainsOnlySniffs(File child) {
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
