<?php

use WP_CLI\Utils; // Simulate importing a WordPress helper namespace

class c1 {
    public function register_custom_option(): void { }

    public function log_adminCount(): void { } // Noncompliant {{Rename function "log_adminCount" to match the regular expression ^[a-z][a-z0-9_]*$.}}
//                  ^^^^^^^^^^^^^^
}

class c2 { // Compliant
    public function run_installation(): void{ }

    protected function create_demo_post(): int { }
}
