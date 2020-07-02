<?php

echo "aa()";    // Noncompliant {{Define a constant instead of duplicating this literal "aa()" 5 times.}}
echo "aa()";
echo "aa()";
echo "aa()";
echo "aa()";

echo "a()";    // Compliant
echo "a()";
echo "a()";
echo "a()";
echo "a()";
