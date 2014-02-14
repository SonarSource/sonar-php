<?php


if ($a) {
} elseif ($a) {
} elseif ($a) {  // NOK
}

if ($a):
elseif ($a):     // NOK
endif;

if ($a) {        // OK
} elseif ($a) {
} else {
}

if ($a) {        // OK
}

if ($a) {        // OK
} else {
}

if ($a) {        // OK
} else {
  if ($a) {
  }
}
