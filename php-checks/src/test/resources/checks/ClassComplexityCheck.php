<?php

class KO {
  function f() // +1
  {
    switch (foo)
    {
      case 1: // +1
      case 2: // +1
      default:
      ;
    }

    if (true) { // +1
      return 1; // +1
    }
    return 1;
  }

  function f() // +1
  {
  }
}

class OK {

  public function f() // +1
  {
    switch (foo)
    {
      case 1: // +1
      case 2: // +1
      default:
      ;
    }
  }

  public function ok() {
  }
}

class OK {
}
