<?php

function compute($a, $a, $c) {      // NOK
}

function compute($a, $a, $c, $a) {  // NOK
}

function compute($a, $b, $c) {      // OK
}
