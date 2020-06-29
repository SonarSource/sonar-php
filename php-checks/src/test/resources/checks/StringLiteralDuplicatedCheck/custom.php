<?php

echo "aa()";    // Noncompliant {{Define a constant instead of duplicating this literal "aa()" 5 times.}}
echo "aa()";
echo "aa()";
echo "aa()";
echo "aa()";

echo "bbbb()";  // Noncompliant {{Define a constant instead of duplicating this literal "bbbb()" 4 times.}}
echo "bbbb()";
echo "bbbb()";
echo "bbbb()";
