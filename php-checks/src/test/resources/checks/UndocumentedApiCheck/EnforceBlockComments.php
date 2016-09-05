<?php

    function a() { // NOK
    }

    $imavar = ""; // OK
    function a2() { // NOK
    }

    /**
     * Comment
     */
    function b() { // OK
    }

    //Comment
    function c() { // NOK
    }

    /** Comment */
    function d() { // OK
    }

    /*
    comment
    */
    function e() { // OK
    }

    class F { // NOK
    }

    $imavar = ""; // OK
    class F2 { // NOK
    }

    /**
     * Comment
     */
    class G { // OK
    }

    // Comment
    class H { // NOK
    }

    /* Comment */
    Class I { // OK
    }

    /* Comment */
    Class I2 { // OK

        /* is correcty commented */
        function imFine() {} // OK

        function imNotOk() {} // NOK

        //does stuff
        function imNotOk() {} // NOK

    }

    $imavar = ""; // OK
?>