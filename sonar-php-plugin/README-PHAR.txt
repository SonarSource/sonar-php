* Concerning embedded PHAR *

The purpose of embedding PHAR for external tools is to avoid as far as possible extra configuration for users.
1) Install Sonar PHP plugin
2) Configure Sonar Runner
3) Should works!

Some projects provide PHAR with a bzip2 compression. The issue is that on some platform (Windows) a PHP extension has to be
enabled in php.ini to support bzip2. So this is at the opposite of a simplified usage.

In addition we are embedding PHAR in the plugin so we benefit from the JAR compression and we don't really need bzip2 compression. As a result PHAR
files embedded in the plugin are uncompressed versions when possible.
