<?php

if ($a) {
} elseif ($a) {
} elseif ($a) {      // Noncompliant {{Add the missing "else" clause.}}
//^^^^^^
}

if ($a):
  elseif ($a):         // Noncompliant
//^^^^^^
endif;

if ($a) {
} else if ($a) {     // Noncompliant
//^^^^^^^
}

if ($a) {
} elseif ($a) {
} else if ($a) {     // Noncompliant
}

if ($a) {
} else if ($a) {
} elseif ($a) {      // Noncompliant
}

if (true) {
 } else if (true) {
 } else if (true) {  // Noncompliant
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

if ($a) {
  break;
} elseif ($b) {
  break;
}

if ($a) {
  break;
} else if ($b) {
  break;
}

if ($a) {
  throw new Exception();
} else if ($b) {
  throw new Exception();
}

if ($a) {
  return;
} else if ($b) {
  return;
}

if ($a) {
  continue;
} else if ($b) {
  continue;
}


if ($a) {
  break;
} else if ($b) {} // Noncompliant

if ($a) {
  break;
} elseif ($b) {} // Noncompliant

if ($a) {
} else if ($b) { // Noncompliant
  break;
}

if ($a) {
  if ($c) {
    return;
  } else {
    return;
  }
} else if ($b) {
  return;
}

if ($a) {
  return;
} else if ($b) {
  if ($c) {
    return;
  } else {
    return;
  }
}

if ($a) {
  return;
} else if ($b) {
  if ($c) {} else {}
  return;
}

if ($a) {
  return;
} else if ($b) { // Noncompliant
  if ($c) {
    return;
  } elseif ($d) {
    return;
  }
  if ($e) {
    return;
  } else {
    if ($g) { } else {
      return;
    }
  }
}

if ($a) {
  return;
} elseif ($b) { // Noncompliant
  if ($c) {
    return;
  } else if ($d) {
    return;
  }
}

if ($a) {
  return;
} else if ($b) { // Noncompliant
  if ($c) {
    return;
  } else {}
}
