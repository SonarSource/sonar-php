<?php
    function a(){ // NOK
    }

    $imavar = "";
    function a2(){ // NOK
    }

    /**
     * Comment
     */
    function b(){ // OK
    }

    //Comment
    function c(){ // OK
    }

    /** Comment */
    function d(){ // OK
    }

    /*
    comment
    */
    function e(){ // OK
    }

    class F { // NOK
    }

    $imavar = "";
    class F2 { // NOK
    }

    /**
     * Comment
     */
    class G { // OK
    }

    // Comment
    class H { // OK
    }

    /* Comment */
    Class I { // OK
    }

    /* Comment */
    Class I2 { // OK

        /* is correcty commented */
        function imFine(){} // OK

        function imNotOk(){} // NOK

        //does stuff
        function imOkHere(){} // OK

    }

    $imavar = "" // OK
?>