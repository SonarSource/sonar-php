<?php

if ($a) {
} elseif ($a) {
} elseif ($a) {      // NOK
}

if ($a):
elseif ($a):         // NOK
endif;

if ($a) {
} else if ($a) {     // NOK
}

if ($a) {
} elseif ($a) {
} else if ($a) {     // NOK
}

if ($a) {
} else if ($a) {
} elseif ($a) {      // NOK
}

if (true) {
 } else if (true) {
 } else if (true) {  // NOK
 }

if (true) {
 } else if (true) {
 } else if (true) {
 } elseif (true) {
 } else {            // OK
 }

if ($a) {            // OK
} elseif ($a) {
} else {
}

if ($a) {            // OK
}

if ($a) {            // OK
} else {
}

if ($a) {            // OK
} else {
  if ($a) {
  }
}
