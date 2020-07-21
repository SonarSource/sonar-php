<?php

try {
    foo();
} catch (NoThrowable $e) { // Noncompliant
    echo "foo";
}

try {
    foo();
} catch (SomeThrowable $e) { // Compliant
    echo "foo";
}
