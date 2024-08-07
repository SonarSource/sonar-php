<?php

not_follow_by_whitespaces();

// Noncompliant@+1 {{Remove the useless trailing whitespaces at the end of this line.}}
follow_by_whitespaces();    
//                      ^^^^

// next line contains only spaces and is ok

// Non compliant test cases with optimized characters
// Noncompliant@+1
if (true) {  
//         ^^
// Noncompliant@+1
}  

// Compliant test cases with optimized characters
if (true) {
}

// Non compliant test cases with non-optimized character
// Noncompliant@+1
$var = 5 +  
//        ^^
3;

// Compliant test cases with non-optimized characters
$var = 5 +
3;