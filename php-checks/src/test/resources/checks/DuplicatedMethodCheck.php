<?php

function fun1($a, $c) {
}
function fun2($a, $c) {
}
function fun3($a, $c) {
//       ^^^^> {{original implementation}}
  echo $a.$c;
  echo $a.$c;
}
function fun4($a, $c) { // Noncompliant {{Update this method so that its implementation is not identical to "fun3" on line 7.}}
//       ^^^^
  echo $a.$c;
  echo $a.$c;
}
function fun5($a, $c) {
  echo $a;
}

class A {
    public $var = 'a default value';

    public function displayVar() {
        echo $this->var;
    }
}

class B {
    public $var = 'a default value';

    public function displayVar() {
    //              ^^^^^^^^^^>
        echo $this->var;
        echo $this->var;
    }

    public function displayVar2() { // Noncompliant {{Update this method so that its implementation is not identical to "displayVar" on line 32.}}
    //              ^^^^^^^^^^^
        echo $this->var;
        echo $this->var;
    }

    abstract function toto();
    function nesting() {
       $this->profile = new class {
                   public function displayVar() {
                       echo $this->var;
                       echo $this->var;
                   }
                   public function displayVar2() { // Noncompliant {{Update this method so that its implementation is not identical to "displayVar" on line 47.}}
                       echo $this->var;
                       echo $this->var;
                   }
                   public function displayVar3() { // Noncompliant {{Update this method so that its implementation is not identical to "displayVar" on line 47.}}
                      echo $this->var;
                      echo $this->var;
                   }
                };
    }

}
class C {
   private $x;
   private $y;

 public function __call($method, $args)
    {
        throw new \LogicException('Unexpected method call');
    }
    public static function __callStatic($method, $args)
    {
        throw new \LogicException('Unexpected method call');
    }

    public function getXprop() {
       return $x;
    }
    public function getYprop() { // Noncompliant
       return $x;
    }
    public function setXprop($x) {
       $this->x = $x;
    }
    public function setYprop($x) { // Noncompliant
       $this->x = $x;
    }
    public function isXprop() {
       return $x == 0;
    }
    public function isYprop() { // Noncompliant
       return $x == 0;
    }

    public function emptyMethod() {
    }
    public function isEmptyMethod() {
    }
}
