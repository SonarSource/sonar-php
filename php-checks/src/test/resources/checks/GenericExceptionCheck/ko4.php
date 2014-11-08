<?php

namespace Package {

  class ClassName {

      function test2() {
          throw new \Exception();
      }
  }

}

namespace {

  class ClassName {

      function test2() {
          throw new Exception();
      }
  }
}
