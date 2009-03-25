<?php

if (class_exists('PHP_CodeSniffer_Standards_CodingStandard', true) === false) {
    throw new PHP_CodeSniffer_Exception('Class PHP_CodeSniffer_Standards_CodingStandard not found');
}

class PHP_CodeSniffer_Standards_$(PROFILE)_$(PROFILE)CodingStandard extends PHP_CodeSniffer_Standards_CodingStandard{
    public function getIncludedSniffs()
    {
        return array(
            $(RULES)
               );

    }
}

?>