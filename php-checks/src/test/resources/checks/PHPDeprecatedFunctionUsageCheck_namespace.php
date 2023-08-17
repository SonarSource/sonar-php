<?php
namespace mynamespace;

function split() {
  echo "split";
}

split();  // OK, function in current namespace
\split()  // Noncompliant
?>
