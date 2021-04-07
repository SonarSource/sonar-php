<?php

try {
  foo();
} catch (A $e) {
} catch (A1 $e) { // Noncompliant
}
