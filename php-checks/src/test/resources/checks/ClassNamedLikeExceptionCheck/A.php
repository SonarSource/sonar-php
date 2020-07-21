<?php

class NotReallyAnException extends B { // Noncompliant {{Rename this class to remove "Exception" or correct its inheritance.}}
}

class ARealException extends C {
}
