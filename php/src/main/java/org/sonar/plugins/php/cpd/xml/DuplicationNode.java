/**
 * 
 */
package org.sonar.plugins.php.cpd.xml;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Model the <duplication> tag inside the pmd-cpd XML file.
 * 
 * @author akram
 * 
 */
@XStreamAlias("duplication")
public class DuplicationNode {

  /**
   * The number of duplicated lines.
   * 
   */
  @XStreamAlias("lines")
  @XStreamAsAttribute
  private Double lines;

  /**
   * The number of duplicated tokens.
   * 
   */
  @XStreamAlias("tokens")
  @XStreamAsAttribute
  private Double tokens;

  /** The files containing duplication */
  @XStreamImplicit
  @XStreamAlias("file")
  private List<FileNode> files;

  /**
   * The duplicated code fragment.
   */
  @XStreamAlias("codefragment")
  String codeFragment;

  /**
   * @return the files
   */
  public List<FileNode> getFiles() {
    return files;
  }

  /**
   * @return the lines
   */
  public Double getLines() {
    return lines;
  }

  /**
   * @return the tokens
   */
  public Double getTokens() {
    return tokens;
  }

  /**
   * @return the codeFragment
   */
  public String getCodeFragment() {
    return codeFragment;
  }

}
