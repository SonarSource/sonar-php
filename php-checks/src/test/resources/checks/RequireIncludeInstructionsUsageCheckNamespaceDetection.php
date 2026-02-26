<?php

// Including a file WITH a namespace — should flag
include "./requireIncludeUsage/WithNamespace.php"; // Noncompliant {{Replace "include" with namespace import mechanism through the "use" keyword.}}
require "./requireIncludeUsage/WithNamespace.php"; // Noncompliant {{Replace "require" with namespace import mechanism through the "use" keyword.}}
require_once "./requireIncludeUsage/WithNamespace.php"; // Noncompliant {{Replace "require_once" with namespace import mechanism through the "use" keyword.}}
include_once "./requireIncludeUsage/WithNamespace.php"; // Noncompliant {{Replace "include_once" with namespace import mechanism through the "use" keyword.}}

// Including a file WITHOUT a namespace — should NOT flag
include "./requireIncludeUsage/WithoutNamespace.php"; // Compliant
require "./requireIncludeUsage/WithoutNamespace.php"; // Compliant
require_once "./requireIncludeUsage/WithoutNamespace.php"; // Compliant
include_once "./requireIncludeUsage/WithoutNamespace.php"; // Compliant

// Non-existent / unknown file — not in symbol index → no issue
include "./requireIncludeUsage/NonExistent.php"; // Compliant

// Dynamic path (not a string literal) — cannot resolve → no issue
include $someVar; // Compliant
include __DIR__ . '/somefile.php'; // Compliant

// Return value used — compliant regardless (existing behaviour)
$config = require "./requireIncludeUsage/WithNamespace.php"; // Compliant
