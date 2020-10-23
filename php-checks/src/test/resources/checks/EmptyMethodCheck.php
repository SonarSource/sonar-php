<?php

class TestClass {
  public function method1() { } // Noncompliant {{Add a nested comment explaining why this method is empty, throw an Exception or complete the implementation.}}

  public function method2() {
    echo 1;
  } // Compliant

  public function method3() {
    // Comment
  } // Compliant

  public function method4() {
    # Comment
  } // Compliant

  public function method5() {
    /* Comment */
  } // Compliant

  public function method6() {
    /** Comment */
  } // Compliant

  public function method7() { /** Comment */ } // Compliant

  public function method8() { /**/ } // Noncompliant

  public function method9() {throw new Exception();} // Compliant

  public function method10() {
      /*
          Larger comment
      */

  } // Compliant

  // Comment
  public function method11() {} // Compliant

  // Comment
  function method11() {} // Compliant
}

abstract class AbstractClass {
  public function defaultMethod1() { } // Compliant

  abstract function abstractMethod1();
}

final class FinalClass {
  public function defaultMethod1() { } // Noncompliant
}

trait AnonymousClassInTrait {
    public function test() {
        return new class() {
            public function foo() {} // Noncompliant
        };
    }
}

interface TestInterface {
  public function interfaceMethod(); // Compliant
}

function function1() { } // Noncompliant {{Add a nested comment explaining why this function is empty, throw an Exception or complete the implementation.}}
function function2() { echo 1; } // Compliant
function function3() { throw new Exception(); } // Compliant
function function4() { /* TODO */ } // Compliant

$function1 = function() { }; // Compliant Handled by Rule S108 - EmptyNestedBlockCheck

// Comment
function function5() {} // Compliant

# Comment
function function6() {} // Compliant

/* Comment */
function function7() {} // Compliant

/**
 * Comment
 */
function function8() {} // Compliant

/**
 * Co
 */
function function9() {} // Noncompliant

/*
 * CommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommententtCommentCommentCommentCommentCommentCommentCommentCommentent
 * CommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommenenttCommentCommentCommentCommentCommentCommentCommentCommentent
 * CommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommenenttCommentCommentCommentCommentCommentCommentCommentCommentent
 * CommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommenenttCommentCommentCommentCommentCommentCommentCommentCommentent
 * CommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommententtCommentCommentCommentCommentCommentCommentCommentCommentent
 * CommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommenenttCommentCommentCommentCommentCommentCommentCommentCommentent
 * CommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommentCommententtCommentCommentCommentCommentCommentCommentCommentCommentent
 */
function function10() {} // Compliant - is related to https://jira.sonarsource.com/browse/SONARPHP-1022

class Php8Class
{
  public function __construct(private $a) {} // Compliant

  public function __construct($a, private $b) {} // Compliant

  public function __construct($a) {} // Noncompliant
}
