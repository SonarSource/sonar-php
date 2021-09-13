package org.sonar.php.regex.ast;

import javax.annotation.Nonnull;
import org.sonarsource.analyzer.commons.regex.ast.ActiveFlagsState;
import org.sonarsource.analyzer.commons.regex.ast.AutomatonState;
import org.sonarsource.analyzer.commons.regex.ast.FlagSet;

public class EndOfConditionalSubpatternsState extends ActiveFlagsState {
  private final ConditionalSubpatternsTree parent;

  public EndOfConditionalSubpatternsState(ConditionalSubpatternsTree parent, FlagSet activeFlags) {
    super(activeFlags);
    this.parent = parent;
  }

  @Nonnull
  @Override
  public AutomatonState continuation() {
    return parent.continuation();
  }

  @Nonnull
  @Override
  public TransitionType incomingTransitionType() {
    return TransitionType.EPSILON;
  }
}
