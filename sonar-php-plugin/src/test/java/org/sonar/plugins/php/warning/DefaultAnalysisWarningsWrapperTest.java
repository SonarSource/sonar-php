package org.sonar.plugins.php.warning;

import org.junit.Test;
import org.sonar.api.notifications.AnalysisWarnings;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class DefaultAnalysisWarningsWrapperTest {


  @Test
  public void addWarning() {
    AnalysisWarnings analysisWarnings = spy(AnalysisWarnings.class);
    AnalysisWarningsWrapper analysisWarningsWrapper = new DefaultAnalysisWarningsWrapper(analysisWarnings);
    analysisWarningsWrapper.addWarning("Test");

    verify(analysisWarnings).addUnique("Test");
  }

}
