<?php

$x = new class1();

  new class1();               // Noncompliant {{Either remove this useless object instantiation of class "class1" or use it}}
//^^^^^^^^^^^^

new class2;               // Noncompliant {{Either remove this useless object instantiation of class "class2" or use it}}

new class1().method1();

$x;

class1();

function isValidDate1($varToTest) {
    try {
        new DateTime($varToTest); // Compliant, as it is the only statement in the try catch
    } catch (Exception $exception) {
        return false;
    }
    return true;
}

function isValidDate2($varToTest) {
    try {
        $x = 1;
        new DateTime($varToTest); // Noncompliant
    } catch (Exception $exception) {
        return false;
    }
    return true;
}

function isValidDate3($varToTest) {
    try {
        new DateTime($varToTest); // Noncompliant
        $x = 1;
    } catch (Exception $exception) {
        return false;
    }
    return true;
}

function isValidDate4($varToTest) {
    try {
    } catch (Exception $exception) {
        return false;
    }
    return true;
}

if($x) {
  new DateTime($varToTest); // Noncompliant
}

if($x) {
  $x = 1;
  new DateTime($varToTest); // Noncompliant
}
