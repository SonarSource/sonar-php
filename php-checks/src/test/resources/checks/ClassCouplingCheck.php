<?php

class Foo {                           // NOK - depends on 11 classes

    /**
     * @var T1
     */
     public $a1;                      // coupled to T1

    /**
     * @var T2
     */
     protected $a2;                   // coupled to T2

    /**
     * @var T3
     */
     private $a3;                     // coupled to T3

    /**
     * @var Boolean
     */
     private $a3;

    /**
     * @param T5
     * @param T6
     *
     * @return T4
     */
     public function  f(T5 $a, $b) {  // coupled to T4, T5, T6
       $result = new T7();            // coupled to T7
       $localVar = new T8();          // coupled to T8

       return $result;
     }

    /**
     * @throws T9
     * @param T10
     *
     * @return T11|null
     */
     public function g(T10 $p) {      // coupled to T9, T10, T11
       if ($p->a) {
         throw new T10();
       }
       return new T11();
     }

     /**
      * @param T11[]
      */
     public function h($p) {
      return doSomething($p);
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
