<?php

  class Foo {       // Noncompliant {{Split this class into smaller and more specialized ones to reduce its dependencies on other classes from 15 to the maximum authorized 10 or less.}}
//^^^^^
    use MyTrait;

//  ^[sl=+2;el=+4;ec=7]< {{Dependency on T1.}}
    /**
     * @var T1
     */
     public $a1;                      // coupled to T1

//  ^[sl=+2;el=+4;ec=7]< {{Dependency on T2.}}
    /**
     * @var T2
     */
     protected $a2;                   // coupled to T2

//   ^[sl=+2;el=+2;ec=19]< {{Dependency on T3.}}
     /** @var T3 */
     private $a3;                     // coupled to T3

    /**
     * @var Boolean
     */
     private $a3;

//  ^[sl=+2;el=+4;ec=7]< {{Dependency on T4.}}
    /**
     * @var T4
     */
     const A4 = 1;                     // coupled to T4

//  ^[sl=+2;el=+7;ec=7]< {{Dependency on T5, T7.}}
    /**
     * @param T6 $a
     * @param T7
     *
     * @return T5
     */
     public function  f(T6 $a, $b) {  // coupled to T5, T6, T7
//                      ^^< {{Dependency on T6.}}
       $result = new T8();            // coupled to T8
//                   ^^< {{Dependency on T8.}}
       $localVar = new T9();          // coupled to T9
//                     ^^< {{Dependency on T9.}}
       $withoutParentheses = new T16; // coupled to T16
//                               ^^^< {{Dependency on T16.}}

       return $result;
     }

//
    /**
     * @throws T10
     * @param T11
     *
     * @return T12|null
     */
     public function g(T11 $p) {      // coupled to T10, T11, T12
//                     ^^^< {{Dependency on T11.}}
       if ($p->a) {
         throw new T10();
//                 ^^^< {{Dependency on T10.}}
       }
       return new T11();
     }

//   ^[sl=+2;el=+4;ec=8]< {{Dependency on T12.}}
     /**
      * @param T12[]
      */
     public function h($p) {
      return doSomething($p);
     }

     /**
      * @return \T13
      */
      public function i() {
       return new namespace\T13 ();    // coupled to T13
//                ^^^^^^^^^^^^^< {{Dependency on \T13.}}
      }

     /**
      * No doc
      */
      public function k(T15 $p) {
//                      ^^^< {{Dependency on T15.}}
        $class = "T14";
        return new $class();          // Not supported
      }
}

class Bar {       // OK - depends on 2 classes

  /**
   * @var T1
   */
   public $a1;    // coupled to T1

  /**
   * @var T2
   */
   public $a2;    // coupled to T2

  /**
   * @param array
   *
   * @return string
   */
   public function f(array $p = array()) {
     return $p->toString();
   }
}

$x = new class {       // Noncompliant
//       ^^^^^
  function foo() {
    new T1();
    new T2();
    new T3();
    new T4();
    new T5();
    new T6();
    new T7();
    new T8();
    new T9();
    new T10();
    new T11();
  }
};

new T8();

enum EnumTestClass {
  case B;
  case C;

  function foo() {
    new T1();
    new T2();
    new T3();
    new T4();
    new T5();
    new T6();
    new T7();
    new T8();
    new T9();
    new T10();
    new T11();
  }
}
