<?php

function compute($a, $a, $c) {          // NOK
}

function compute($a, $a, $c, $b, $b) {  // NOK
}

function compute($a, $b, $c) {          // OK
}
