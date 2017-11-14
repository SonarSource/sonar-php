<?php
  function func($gender, $is_married) {
    $gender == "MALE" ? "Mr. " : function () use($is_married) { return $is_married ? "Mrs. " : "Miss "; };


    $gender == "MALE" ? "Mr. " : ($is_married ? "Mrs. " : "Miss ");  // Noncompliant
    //                ^>          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    $gender == "MALE" ?
    //                ^> {{Parent ternary operator}}
       ($is_married ? "Mrs. " : "Mrs. ") :
    //  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^> {{Other nested ternary}}
       ($is_married ? "Mrs. " : "Miss ");  // Noncompliant
    //  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ {{Extract this nested ternary operation into an independent statement.}}
    $is_married ? "Mrs. " : "Miss ";
    $is_married ? : "Miss ";

    echo ($gender ? (new class { function someMethod($foo) { return $foo ? "foo" : "bar"; } })->someMethod("foo") : "false");
  }
