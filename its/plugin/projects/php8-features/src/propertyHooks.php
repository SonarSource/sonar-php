<?php

class Test {
    public int $prop {
        get { return $this->prop * 2; }
        set { $this->prop = $value * 2; }
    }

    // Edge-case where recursion happens via isset().
    public int $prop2 {
        get { return isset($this->prop2); }
        set { }
    }

    public int $prop3 {
            get {
                return $this->prop3 + 1;
            }
            set {
                $this->prop3 = $value - 1;
            }
        }
        public $prop4 = 1 {
            #[Attr]
            &get => $this->prop4;
            final set($value) => $value - 1;
        }
        abstract public $prop5 {
            get;
            set;
        }
        // TODO: Force multiline for hooks?
        public function __construct(
            public $foo {
                get => 42;
                set => 123;
            },
            public $bar
        ) {}
}

$test = new Test;
$test->prop = 10;
var_dump($test->prop);
var_dump(isset($test->prop));
var_dump(isset($test->prop2));
var_dump($test);
