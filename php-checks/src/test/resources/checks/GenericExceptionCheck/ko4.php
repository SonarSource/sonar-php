<?php

namespace Package {

  class ClassName {

      function test2() {
          throw new \Exception();  // NOK {{Define and throw a dedicated exception instead of using a generic one.}}
//                  ^^^^^^^^^^
      }
  }

}

namespace {

  class ClassName {

      function test2() {
          throw new Exception();   // NOK
      }
  }
}
