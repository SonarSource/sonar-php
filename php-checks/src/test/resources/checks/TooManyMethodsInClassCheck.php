<?php

use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;

  interface I {             // Noncompliant {{Class "I" has 3 methods, which is greater than 2 authorized and is not an Entity of a Database. Split it into smaller classes.}}
//^^^^^^^^^

  public function f1();

  public function f2();

  public function f3();
}

  class C1 {       // Noncompliant
//^^^^^

  public function f1() {
  }

  public function f2() {
  }

  private function f3();
}

class C1 {       // OK

  private $i;

  public function f1() {
  }

  public function f2();
}

class TestClass {       // OK

  public function test_f1();

  public function f2();

  public function f3();
}

$x = new class {       // Noncompliant
//       ^^^^^

  public function f1() {  }

  public function f2() {  }

  public function f3() {  }

  private function f4();
};

class ClassDB {         // OK
  public function get1() {  }

  public function get2() {  }

  public function get3() {  }

  public function set1() {  }
}

  class ClassDB2 {     // Noncompliant
//^^^^^

  public function get1() {  }

    public function ko() {  }

    public function Target3() {  }

    public function set1() {  }
}


  #[ORM\Entity]
  class AnnotationClass {   // OK

    public function f1() {  }

    public function f2() {  }

    public function f3() {  }

    public function f4() {  }
 }

  #[ORM\Entity,Foo\Bar]
  class AnnotationClass {   // OK

    public function f1() {  }

    public function f2() {  }

    public function f3() {  }

    public function f4() {  }
  }

  #[Entity(repositoryClass: DocumentRepository::class)]
  class AnnotationArgsClass{ //OK

      public function f1() {  }

      public function f2() {  }

      public function f3() {  }

      public function f4() {  }
  }
