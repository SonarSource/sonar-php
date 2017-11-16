<?php
$str = "";
if(empty((($str = "foo")))) { // Noncompliant
}
echo $plop = "plop"; // Noncompliant {{Extract the assignment of "$plop" from this expression.}}
         //^
echo $plop =& $str; // Noncompliant
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
