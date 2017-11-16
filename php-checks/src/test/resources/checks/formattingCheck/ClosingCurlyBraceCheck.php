<?php
if ($a) {
}
  elseif ($b) { // Noncompliant {{Move this "elseif" to the same line as the previous closing curly brace.}}
//^^^^^^
}
elseif ($b) { // Noncompliant
}
