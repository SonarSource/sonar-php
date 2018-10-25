# Sonar-PHP Control Flow Graph

----
## Specification

A CFG is a graph of [basic blocks](https://en.wikipedia.org/wiki/Basic_block) (from now on, we will use the term 'block').

### Blocks

#### Block attributes

* successors - set of blocks that are executed after
* predecessors - set of blocks that are executed before
* syntactic successor
  - an imaginary successor which exists only for jump blocks (BREAK, CONTINUE, RETURN, GOTO, THROW)
  - it is the "normal" successor in case the block would not have a jump statement in the end
  - for non-jump blocks, it is null
* elements
  - list of trees (ASTs) inside the block
  - **Important** : the block does not necessarily end with a terminator
    - it could have a terminator if it contains a RETURN inside the list of trees
    - there are no artificial terminators

#### Block types

Different implementations of blocks:

* Simple block - one successor
* Branching block
  - has 2 successors - one TRUE successor and one FALSE successor
  - contains the branching tree - e.g. IfStatementTree
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

The TRY statement is modelled as a block with multiple successors
- each CATCH block represents a successor
- the FINALLY block represents a successor

----
## Tests

The best documentation is Test Automation, see [ControlFlowGraphTest](php-frontend/src/test/java/org/sonar/php/cfg/ControlFlowGraphTest.java).
