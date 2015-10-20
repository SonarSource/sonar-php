<p>
html is not considered as lines of code
</p>

<?php /* opening tag is not considered as a line of code */
/*
 * File Header
 */

/*
Multi-line comment
*/
class A {
  //
  // Inline comment
  //
  public function f() {
    // NOSONAR comment
  }
}
"string one line";
"string
multiline";
/* closing tag is not considered as a line of code */ ?>

<p>
html is not considered as lines of code
</p>
