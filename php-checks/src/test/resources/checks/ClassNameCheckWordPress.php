<?php

use WP\Core\Plugin;


class myClass {  // NOK {{Rename class "myClass" to match the regular expression ^[A-Z][a-zA-Z0-9]*$.}}
//    ^^^^^^^
    
    public function get_user_data() {  // OK - WordPress uses snake_case for functions
        return array();
    }
    
    public function process_user_123() {  // OK - numbers are allowed in snake_case
        return true;
    }
}

class WP_Custom_Widget_Helper { // OK - WordPress uses PascalCase with underscores for classes
    
    public function init_widget() {  // OK
        return null;
    }
    
    public function get_widget_options() {  // OK
        return array();
    }
    
    public function save_widget_data_2() {  // OK - numbers allowed
        return false;
    }
}

class Migration_20231105_add_user_meta { // OK - WordPress migration naming pattern
    
    public function up() {  // OK
        // Migration logic
    }
    
    public function down() {  // OK
        // Rollback logic
    }
    
    public function create_table_v2() {  // OK - numbers in function names
        return true;
    }
}
