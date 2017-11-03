<?php

class A {
  function f() {
    $x = ((42  // NOK [[secondary=+2]] {{Remove these useless parentheses.}}
//        ^
             )
              );
  }
}