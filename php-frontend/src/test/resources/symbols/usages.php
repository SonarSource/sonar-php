<?php

$a = 1;
$b = 1;

echo $a;             // use global $a

$f = function () use ($a, &$b) {
    echo $a;         // use local $a
    echo $b;         // use global $b
};

function g() {
    global $a;
    echo $a;         // use global $a
}

function h($a) {
    echo $a;         // use parameter $a
}

function j() {
    $a = 1;
    echo $a;         // use local $a
}

$f();
h(j());

$array = [1, 2, 3];
echo $array[0];      // use global $array

class A {

    public $field;

    public function method() {
        $field = 1;
        echo $field;
        echo $this->field;
        $this->method();

        foo(A::class);
        $A = 1;
        return $A;
    }
}


// compound variable
$compoundVar = "str";
${$compoundVar} = 1;    // use of $compoundVar
${compoundVar} = 1;     // use of $compoundVar

$var = "str";
echo "${var}";          // use of $var
$compactArray = compact("var", "unknown"); // use of $var
$compactArray = \compact("var", "unknown"); // use of $var

new A();
new A;

$heredocVar = 1;
echo <<<EOF
  This is first usage of \$heredocVar: $heredocVar
  This is second usage: {$heredocVar}
EOF;
echo <<<'EOF'
  This is nowdoc. So this is not usage $heredocVar.
  And this {$heredocVar}
EOF;

class SomeClass {}
interface SomeInterface {}
trait SomeTrait {}
$fooBar = "fooBar";

new class($fooBar, 10) extends SomeClass implements SomeInterface {
    private $qux;
    private $num;

    public function __construct($string, $num)
    {
        $this->qux = $string;
        $this->num = $num;
        $this->num = "x";
    }

    use SomeTrait;
};

new class() { }; // no parameter, super-class or super-interface
