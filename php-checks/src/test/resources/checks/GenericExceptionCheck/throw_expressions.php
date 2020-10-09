<?php

function ko($p) {
  $p || throw new Exception(); // Noncompliant
}

function ok($p) {
  $p || throw new InvalidArgumentException();
}
