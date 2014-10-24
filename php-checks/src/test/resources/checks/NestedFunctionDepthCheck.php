<?php

function f1() {
  function f2() {
    function f3() {
      function f4() {       // NOK
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
