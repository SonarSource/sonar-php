package org.sonar.plugins.php.cpd.xml;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * The root node on the Pmd-cpd XML file.
 * 
 * @author akram
 * 
 */
@XStreamAlias("pmd-cpd")
public class PmdCpdNode {

  /** The duplication contained in the file */
  @XStreamImplicit
  @XStreamAlias("duplication")
  private List<DuplicationNode> duplications;

  /**
   * @return the duplications
   */
  public List<DuplicationNode> getDuplications() {
    return duplications;
  }

}
