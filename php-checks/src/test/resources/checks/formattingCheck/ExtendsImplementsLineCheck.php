<?php

class KO                     // Noncompliant {{Move "extends" keyword to the same line as the declaration of its class name, "KO".}}
//    ^^
    extends Another
{

}

class KO                     // Noncompliant {{Move "implements" keyword to the same line as the declaration of its class name, "KO".}}
//    ^^
    implements Anotherable
{

}

class KO                     // Noncompliant {{Move "extends" and "implements" keywords to the same line as the declaration of its class name, "KO".}}
//    ^^
    extends Another
    implements Anotherable
{

}

class OK extends Another implements Anotherable
{

}
