<?php

echo "bbbb()";  // Noncompliant {{Define a constant instead of duplicating this literal "bbbb()" 4 times.}}
echo "bbbb()";
echo "bbbb()";
echo "bbbb()";

echo "aaaa()";  // Compliant
echo "aaaa()";
echo "aaaa()";
