/**
 * 
 */
package org.sonar.plugins.checkstyle;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sonar.api.rules.RulePriority;

/**
 * @author akram
 * 
 */
public class CheckstyleRulePriorityMapperTest {

  /**
   * Test method for {@link org.sonar.plugins.checkstyle.CheckstyleRulePriorityMapper#from(java.lang.String)}.
   */
  @Test
  public void testFrom() {
    CheckstyleRulePriorityMapper mapper = new CheckstyleRulePriorityMapper();
    assertEquals(RulePriority.BLOCKER, mapper.from("error"));
    assertEquals(RulePriority.MAJOR, mapper.from("warning"));
    assertEquals(RulePriority.INFO, mapper.from("info"));
    assertEquals(null, mapper.from(null));
    assertEquals(null, mapper.from("RANDOM"));

  }

  /**
   * Test method for {@link org.sonar.plugins.checkstyle.CheckstyleRulePriorityMapper#to(org.sonar.api.rules.RulePriority)}.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testTo() {
    CheckstyleRulePriorityMapper mapper = new CheckstyleRulePriorityMapper();
    assertEquals("info", mapper.to(RulePriority.INFO));
    assertEquals("info", mapper.to(RulePriority.MINOR));
    assertEquals("warning", mapper.to(RulePriority.MAJOR));
    assertEquals("error", mapper.to(RulePriority.BLOCKER));
    assertEquals("error", mapper.to(RulePriority.CRITICAL));
    assertEquals(null, mapper.to(null));
  }
}
