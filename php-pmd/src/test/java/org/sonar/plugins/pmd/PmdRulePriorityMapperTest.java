/**
 * 
 */
package org.sonar.plugins.pmd;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sonar.api.rules.RulePriority;

/**
 * @author akram
 * 
 */
public class PmdRulePriorityMapperTest {

  /**
   * Test method for {@link org.sonar.plugins.checkstyle.CheckstyleRulePriorityMapper#from(java.lang.String)}.
   */
  @Test
  public void testFrom() {
    PmdRulePriorityMapper mapper = new PmdRulePriorityMapper();
    assertEquals(RulePriority.BLOCKER, mapper.from("1"));
    assertEquals(RulePriority.CRITICAL, mapper.from("2"));
    assertEquals(RulePriority.MAJOR, mapper.from("3"));
    assertEquals(RulePriority.MINOR, mapper.from("4"));
    assertEquals(RulePriority.INFO, mapper.from("5"));
    assertEquals(RulePriority.MAJOR, mapper.from(null));
    assertEquals(RulePriority.MAJOR, mapper.from(""));
  }

  /**
   * Test method for {@link org.sonar.plugins.checkstyle.CheckstyleRulePriorityMapper#to(org.sonar.api.rules.RulePriority)}.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testTo() {
    PmdRulePriorityMapper mapper = new PmdRulePriorityMapper();
    assertEquals("1", mapper.to(RulePriority.BLOCKER));
    assertEquals("2", mapper.to(RulePriority.CRITICAL));
    assertEquals("3", mapper.to(RulePriority.MAJOR));
    assertEquals("4", mapper.to(RulePriority.MINOR));
    assertEquals("5", mapper.to(RulePriority.INFO));
    assertEquals(null, mapper.to(null));
  }
}
