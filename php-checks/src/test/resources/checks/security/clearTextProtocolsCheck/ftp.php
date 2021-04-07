<?php

ftp_connect('xxx'); // Noncompliant {{Using ftp_connect() is insecure. Use ftp_ssl_connect() instead}}
\ftp_connect('xxx'); // Noncompliant
ftp_connect('xxx', 1234); // Noncompliant
ftp_ssl_connect('xxx'); // Compliant
ftp_ssl_connect('xxx', 1234); // Compliant
