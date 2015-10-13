<?php

class KO                     // NOK {{Move "extends" keyword to the same line as the declaration of its class name, "KO".}}
    extends Another
{

}

class KO                     // NOK {{Move "implements" keyword to the same line as the declaration of its class name, "KO".}}
    implements Anotherable
{

}

class KO                     // NOK {{Move "extends" and "implements" keywords to the same line as the declaration of its class name, "KO".}}
    extends Another
    implements Anotherable
{

}

class OK extends Another implements Anotherable
{

}
