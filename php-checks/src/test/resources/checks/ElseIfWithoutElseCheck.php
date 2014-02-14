<?php


if ($a) {
} elseif ($a) {
} elseif ($a) {  // NOK
}

if ($a):
elseif ($a):     // NOK
endif;

if ($a) {
}

if ($a) {
} else {
}

if ($a) {
} else {
  if ($a) {
  }
}
