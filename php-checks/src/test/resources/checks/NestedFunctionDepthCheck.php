<?php

function f1() {
  function f2() {
    function f3() {
      function f4() {       // NOK [[secondary=-0,-1,-2,-3]] {{Refactor this code to not nest functions more than 3 levels deep.}}
//    ^^^^^^^^
      }
    }
  }
}

class A {

  private function f1() {
    function f2() {
      function f3() {
        function f4() {     // NOK
        }
      }
    }
  }

}

function f1() {
  function f2() {
    function f3() {         // OK
    }
  }
}
