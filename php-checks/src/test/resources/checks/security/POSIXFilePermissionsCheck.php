<?php

namespace N1;

$foo = "0007";
chmod("foo", 0777);       // Noncompliant
chmod("foo", $foo);       // Noncompliant
chmod("foo", 750);        // Noncompliant
chmod("foo", "750");      // Noncompliant
chmod("foo", "0750");     // Noncompliant
Chmod("foo", "0750");     // Noncompliant
\Chmod("foo", "0750");    // Noncompliant
chmod(permissions:0777, filename:"foo");       // Noncompliant

chmod("foo", 0750);       // Compliant
chmod("foo", 0);          // Compliant
chmod("foo", "0120");     // Compliant
chmod("foo", $mode);      // Compliant (unknown)
chmod(750);               // Compliant
chmod(0777, "foo");       // Compliant

$bar = 0750;
umask(0000);              // Noncompliant
umask(0750);              // Noncompliant
umask($bar);              // Noncompliant
umask(0023);              // Noncompliant
Umask(0023);              // Noncompliant
Umask(mask:0023);         // Noncompliant

umask("0023");            // Compliant
umask(0777);              // Compliant
umask(0007);              // Compliant
umask($mask);             // Compliant (unknown)
umask();                  // Compliant

namespace Symfony;

use Symfony\Component\Filesystem\Filesystem;

$foo = 0007;
$fs = new Filesystem();
$fs->chmod("foo", 0777, 0000);   // Noncompliant
$fs->chmod("foo", 0777);         // Noncompliant
$fs->chmod("foo", $foo);         // Noncompliant
$fs->chmod("foo", "0750");       // Noncompliant
$fs->chmod(mode:"0750", "foo");                // Noncompliant
$fs->chmod(umask:0, mode:"0750", "foo");       // Noncompliant

$fs->chmod("foo", 0750);         // Compliant
$fs->chmod("foo", 0);            // Compliant
$fs->chmod("foo", "0120");       // Compliant
$fs->chmod("foo", 0000, 0777);   // Compliant
$fs->chmod("foo", 0777, 0777);   // Compliant
$fs->foo("foo", 0777);           // Compliant
$fs->chmod(0777);                // Compliant
$fs->foo(0777, 0000, "foo");     // Compliant
