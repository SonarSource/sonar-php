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

    /**
     */
    function b2() { // OK
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
    function e() { // NOK
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

    /**
     */
    class G2 { // OK
    }

    // Comment
    class H { // NOK
    }

    /* Comment */
    Class I { // NOK
    }

    /** Comment */
    Class I2 { // OK

        /* is incorrecty commented */
        function imFine() {} // NOK

        function imNotOk() {} // NOK

        //does stuff
        function imNotOk() {} // NOK

    }

    $imavar = ""; // OK

    // strage comment */
    function z() { // NOK
    }
?>