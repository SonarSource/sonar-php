<?php
$str = "";
if(($str = "foo")) {
}elseif(($str = "foo")) {}
if($toto && $str = "foo") {} // Noncompliant

isNull($str = "foo");
if(empty(((toto && $str = "foo")))) { // Noncompliant
}
echo "toto".$plop = "plop"; // Noncompliant {{Extract the assignment of "$plop" from this expression.}}
                //^
echo "toto".$plop =& $str; // Noncompliant
                //^
$plop = "plopure";
for($i = 0; $i < $seriesCount; ++$i) {
}
do{
} while ($c = $c->parent);
while ($c = $c->parent) {
}
for ($pos = 0; ; $pos += 128) {
}
foreach ($i = array(1, 2, 3, 4) as &$value) { // Noncompliant
}
switch ($i = $toto) { // Noncompliant
    case "apple":
        echo "i est une pomme";
        break;
    case "bar":
        echo "i est une barre";
        break;
    case "cake":
        echo "i est un gateau";
        break;
}

function foo() {
    return 42;
}

$f = foo() or die();
