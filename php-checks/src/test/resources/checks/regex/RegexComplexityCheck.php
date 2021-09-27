<?php

function character_classes($input) {
  preg_match('/[a][b][c][d][e][f][g][h][i][j][k][l][m][n][o][p][q][r][s][t]/', $input);
  // Noncompliant@+1 {{Simplify this regular expression to reduce its complexity from 21 to the 20 allowed.}}
  preg_match('/[a][b][c][d][e][f][g][h][i][j][k][l][m][n][o][p][q][r][s][t][u]/', $input);
}

function disjunction($input) {
  // Noncompliant@+1 {{Simplify this regular expression to reduce its complexity from 21 to the 20 allowed.}}
  preg_match('/(a|(b|(c|(d|(e|(f|(gh)))))))/'); // 1+2+3+4+5+6=21
  // Noncompliant@+1 {{Simplify this regular expression to reduce its complexity from 21 to the 20 allowed.}}
  preg_match('/(a|(b|(c|(d|(e|(((f|(gh)))))))))/'); // 1+2+3+4+5+6=21
  // Noncompliant@+1 {{Simplify this regular expression to reduce its complexity from 23 to the 20 allowed.}}
  preg_match('/(a|(b|(c|(d|(e|(f|g|h|i))))))/'); // 1+2+3+4+5+8=23
  // Noncompliant@+1 {{Simplify this regular expression to reduce its complexity from 28 to the 20 allowed.}}
  preg_match('/(a|(b|(c|(d|(e|(f|(g|(hi))))))))/'); // 1+2+3+4+5+6+7=28
}

function repetition($input) {
  // Noncompliant@+1 {{Simplify this regular expression to reduce its complexity from 21 to the 20 allowed.}}
  preg_match('/(a(b(c(d(ef+)+)+)+)+)+/'); // 6+5+4+3+2+1=21
  // Noncompliant@+1 {{Simplify this regular expression to reduce its complexity from 21 to the 20 allowed.}}
  preg_match('/(a(b(c(d(ef*)*)*)*)*)*/'); // 6+5+4+3+2+1=21
}

function nonCapturingGroup($input) {
  preg_match('/(?:a(?:b(?:c(?:d(?:e(?:f))))))/'); // 0
  // Noncompliant@+1 {{Simplify this regular expression to reduce its complexity from 21 to the 20 allowed.}}
  preg_match('/(?i:a(?i:b(?i:c(?i:d(?i:e(?i:f))))))/'); // 1+2+3+4+5+6=21
  // Noncompliant@+1 {{Simplify this regular expression to reduce its complexity from 21 to the 20 allowed.}}
  preg_match('/(?i:a(?i:b(?i:c(?i:d(?i:e((?i)f))))))/'); // 1+2+3+4+5+6=21
  // Noncompliant@+1 {{Simplify this regular expression to reduce its complexity from 21 to the 20 allowed.}}
  preg_match('/(?-i:a(?-i:b(?-i:c(?-i:d(?-i:e(?-i:f))))))/'); // 1+2+3+4+5+6=21
}

function backReference($input) {
  // Noncompliant@+1 {{Simplify this regular expression to reduce its complexity from 21 to the 20 allowed.}}
  preg_match('/(abc)(a|(b|(c|(d|(e|(f|gh))))))/'); // 1+2+3+4+5+6=21
  // Noncompliant@+1 {{Simplify this regular expression to reduce its complexity from 22 to the 20 allowed.}}
  preg_match('/(abc)(a|(b|(c|(d|(e|(f|\\1))))))/'); // 1+2+3+4+5+6+1=22
}

function lookAround($input) {
  // Noncompliant@+1 {{Simplify this regular expression to reduce its complexity from 21 to the 20 allowed.}}
  preg_match('/(a|(b|(c|(d|(e|(f(?!g)))))))/'); // 1+2+3+4+5+6=21
  // Noncompliant@+1 {{Simplify this regular expression to reduce its complexity from 21 to the 20 allowed.}}
  preg_match('/(a|(b|(c|(d|(e(?!(f|g)))))))/'); // 1+2+3+4+5+6=21
}
