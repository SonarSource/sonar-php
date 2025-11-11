package org.sonar.php.checks.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class PhpFileUtils {

  private static final Pattern IMPORTS_KEY_PATTERN = Pattern.compile(
    "return\\s+\\[.*(['\\\"]imports['\\\"]\\s*=>|\"imports\"\\s*=>).*\\];", Pattern.DOTALL);

  public static boolean isImportmapPhp(String stringFilePath) {
    Path filePath = Path.of(stringFilePath);
    if (filePath == null || !Files.isRegularFile(filePath)) {
      return false;
    }
    String bla = filePath.getFileName().toString();
    if (!filePath.getFileName().toString().equals("importmap.php")) {

      return false;
    }
    return true;
//    try {
//      String content = Files.readString(filePath);
//      return IMPORTS_KEY_PATTERN.matcher(content).find();
//    } catch (IOException e) {
//      return false;
//    }
  }
}

