# Sonar-PHP Control Flow Graph

----
## Specification

A CFG is a graph of [basic blocks](https://en.wikipedia.org/wiki/Basic_block) (from now on, we will use the term 'block').

### Blocks

A block has the following attributes:

* successors - blocks that are executed after
* predecessors - blocks that are executed before
* syntactic successor - if no jump is applied, the block that immediately follows
* elements - the AST Trees that are inside the block

Types of blocks:

* Simple block - one successor
* Branching block
  - has 2 successors - one TRUE successor and one FALSE successor
  - contains the branching tree
* End block - no successors

### Statements

Statements that are considered for building blocks:

* TRY, THROW, RETURN, BREAK, CONTINUE, GOTO, LABEL
* DO_WHILE, WHILE, FOR, FOREACH, IF, SWITCH

Every other statement will be added as element to the containing block (including ternary expressions).

In other words, we do not explore the expressions inside the above statements when building the CFG, we just add them to the list of elements.

### Relevant implementation details

The CFG gets built in [ControlFlowGraphBuilder](php-frontend/src/main/java/org/sonar/php/cfg/ControlFlowGraphBuilder.java)

The FOR statement creates multiple blocks:

- one for initialization of variables (before body)
- one for the body of the for
- one for update (following the body)
- one for the condition

The SWITCH statement is modelled as multiple if-elseif blocks.

The TRY statement is as a multi-successor block - the TRY block has as successors the CATCH and FINALLY blocks.

----
## Tests

The best documentation is Test Automation, see [ControlFlowGraphTest](php-frontend/src/test/java/org/sonar/php/cfg/ControlFlowGraphTest.java).
