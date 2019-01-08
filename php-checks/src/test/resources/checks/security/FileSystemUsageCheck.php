<?php

function handle_file($filename, $directory, $group, $data, $mode, $flags, $use_include_path, $pattern, $recursive, $context)
{
    file_put_contents($filename, $data, $flags); // Noncompliant {{Make sure this file handling is safe here.}}
    copy($filename, $filename); // Noncompliant
    copy("mypath1", "mypath2"); // Noncompliant
    tmpfile(); // Noncompliant
    parse_ini_file($filename); // Noncompliant

    // The following calls will raise an issue if and only if the $filename or $directory is not hardcoded
    move_uploaded_file("mypath1", "mypath2"); // Compliant
    move_uploaded_file($filename, $filename); // Noncompliant
    move_uploaded_file("mypath1", $filename); // Noncompliant
    move_uploaded_file($filename, "mypath2"); // Noncompliant
    rmdir("mypath");           // Compliant
    rmdir($directory);         // Noncompliant
    unlink("mypath");          // Compliant
    unlink($filename);         // Noncompliant
    delete("mypath");          // Compliant
    delete($filename);         // Noncompliant
    chgrp("mypath", "admin");   // Compliant
    chgrp($filename, "admin"); // Noncompliant
    lchgrp("mypath", $group);  // Compliant
    lchgrp($filename, $group); // Noncompliant
    chmod("mypath", $mode);    // Compliant
    chmod($filename, $mode);   // Noncompliant
    chown("mypath", $data);    // Compliant
    chown($filename, $data);   // Noncompliant
    lchown("mypath", $data);   // Compliant
    lchown($filename, $data);  // Noncompliant

    // false-positive, it will be fixed by SONARPHP-885, API limitation about determining if $hardcoded_value is a constant value
    $hardcoded_value = "mypath";
    delete($hardcoded_value); // Noncompliant

    // The following functions can also be used to perform network requests (http, socket, ftp, etc...)
    // in some case they won't raise issues, see below.
    file_get_contents($filename, $use_include_path); // Noncompliant
    file($filename, $flags); // Noncompliant
    fopen($filename, $mode, $use_include_path); // Noncompliant
    readfile($filename, $use_include_path); // Noncompliant

    // No issue is raised if the path does not start with 'file://' but contains '://'
    // Note: following wrapper are ignored: 'compress.zlib://', 'compress.bzip2://', 'zip://',
    // 'glob://', 'rar://', 'ogg://'
    file_get_contents("http://example.com", $use_include_path); // Compliant
    file("http://example.com", $flags); // Compliant
    fopen("http://example.com", $mode, $use_include_path); // Compliant
    readfile("http://example.com", $use_include_path); // Compliant
    readfile("file://file1", $use_include_path); // Noncompliant
    readfile("compress.zlib://file1", $use_include_path); // Noncompliant
    readfile("compress.bzip2://file1", $use_include_path); // Noncompliant
    readfile("zip://file1", $use_include_path); // Noncompliant
    readfile("glob://file1", $use_include_path); // Noncompliant
    readfile("rar://file1", $use_include_path); // Noncompliant
    readfile("ogg://file1", $use_include_path); // Noncompliant
    readfile("file1", $use_include_path); // Noncompliant
    readfile("rar://ogg://http://example.com", $use_include_path); // Compliant

    // No issue is created if a context is given as there is a high chance that it is not a filesystem access.
    // Note that this will create some false negatives with "zip" contexts.
    file_get_contents($filename, $use_include_path, $context); // Compliant
    file($filename, $flags, $context); // Compliant
    fopen($filename, $mode, $use_include_path, $context); // Compliant
    readfile($filename, $use_include_path, $context); // Compliant

    // coverage
    readfile();
    $unknown($filename, $use_include_path, $context);
    unknown($filename, $use_include_path, $context);
}
