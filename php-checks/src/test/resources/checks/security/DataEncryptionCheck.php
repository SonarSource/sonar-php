<?php

function myEncrypt($cipher, $key, $data, $mode, $iv, $options, $padding, $infile, $outfile, $recipcerts, $headers, $nonce, $ad, $pub_key_ids, $env_keys) {
    mcrypt_ecb ($cipher, $key, $data, $mode); // Noncompliant {{Make sure that encrypting data is safe here.}}
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    MCRYPT_ECB($cipher, $key, $data, $mode); // Noncompliant
    mcrypt_cfb($cipher, $key, $data, $mode, $iv); // Noncompliant
    mcrypt_cbc($cipher, $key, $data, $mode, $iv); // Noncompliant
    mcrypt_encrypt($cipher, $key, $data, $mode); // Noncompliant

    openssl_encrypt($data, $cipher, $key, $options, $iv); // Noncompliant
    openssl_public_encrypt($data, $crypted, $key, $padding); // Noncompliant
    openssl_pkcs7_encrypt($infile, $outfile, $recipcerts, $headers); // Noncompliant
    openssl_seal($data, $sealed_data, $env_keys, $pub_key_ids); // Noncompliant

    sodium_crypto_aead_aes256gcm_encrypt ($data, $ad, $nonce, $key); // Noncompliant
    sodium_crypto_aead_chacha20poly1305_encrypt ($data, $ad, $nonce, $key); // Noncompliant
    sodium_crypto_aead_chacha20poly1305_ietf_encrypt ($data, $ad, $nonce, $key); // Noncompliant
    sodium_crypto_aead_xchacha20poly1305_ietf_encrypt ($data, $ad, $nonce, $key); // Noncompliant
    sodium_crypto_box_seal ($data, $key); // Noncompliant
    sodium_crypto_box ($data, $nonce, $key); // Noncompliant
    sodium_crypto_secretbox ($data, $nonce, $key); // Noncompliant
    sodium_crypto_stream_xor ($data, $nonce, $key); // Noncompliant
}

use Cake\Utility\Security as CakeSecurity;

function myCakeEncrypt($key, $data, $engine)
{
    CakeSecurity::encrypt($data, $key); // Noncompliant
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    \Cake\Utility\Security::engine($engine); // Noncompliant
    \cake\utility\security::ENGINE($engine); // Noncompliant
    \Cake\Utility\Security::class; // OK
    \Not\Cake\Utility\Security::encrypt($data, $key); // OK
    \Cake\Utility\Security::encrypt["abc"](); // OK
}

function drupalEncrypt() {
    $encrypted_text = encrypt('some string to encrypt'); // Noncompliant
}

use Joomla\Crypt\CipherInterface;

abstract class MyCipher1 implements CipherInterface // Noncompliant
//                                  ^^^^^^^^^^^^^^^
{}

abstract class MyCipher2 implements cipherinterface // Noncompliant
{}

abstract class MyCipher3 implements NotACipherInterface1 // Ok
{}

abstract class MyCipher4 implements NotACipherInterface1, CipherInterface, NotACipherInterface2 // Noncompliant
{}

function joomlaEncrypt() {
    new \Joomla\Crypt\Cipher_Sodium(); // Noncompliant
    new Joomla\Crypt\Cipher_Simple(); // Noncompliant
    new Joomla\Crypt\Cipher_Rijndael256(); // Noncompliant
    new Joomla\Crypt\Cipher_Crypto(); // Noncompliant
    new Joomla\Crypt\Cipher_Blowfish(); // Noncompliant
    new Joomla\Crypt\Cipher_3DES(); // Noncompliant
}

use Defuse\Crypto\Crypto;
use Defuse\Crypto\File;

function mypPhpEncryption($data, $key, $password, $inputFilename, $outputFilename, $inputHandle, $outputHandle) {
    Crypto::encrypt($data, $key); // Noncompliant
    crypto::ENCRYPT($data, $key); // Noncompliant
    Crypto::encryptWithPassword($data, $password); // Noncompliant
    File::encryptFile($inputFilename, $outputFilename, $key); // Noncompliant
    File::encryptFileWithPassword($inputFilename, $outputFilename, $password); // Noncompliant
    File::encryptResource($inputHandle, $outputHandle, $key); // Noncompliant
    File::encryptResourceWithPassword($inputHandle, $outputHandle, $password); // Noncompliant
}

function myphpseclib($mode) {
    new phpseclib\Crypt\RSA(); // Noncompliant
    new phpseclib\Crypt\AES(); // Noncompliant
    new phpseclib\Crypt\Rijndael(); // Noncompliant
    new phpseclib\Crypt\Twofish(); // Noncompliant
    new phpseclib\Crypt\Blowfish(); // Noncompliant
    new phpseclib\Crypt\RC4(); // Noncompliant
    new phpseclib\Crypt\RC2(); // Noncompliant
    new phpseclib\Crypt\TripleDES(); // Noncompliant
    new phpseclib\Crypt\DES(); // Noncompliant

    new phpseclib\Crypt\AES($mode); // Noncompliant
    new phpseclib\Crypt\Rijndael($mode); // Noncompliant
    new phpseclib\Crypt\TripleDES($mode); // Noncompliant
    new phpseclib\Crypt\DES($mode); // Noncompliant
}

function mySodiumCompatEncrypt($data, $ad, $nonce, $key) {
    ParagonIE_Sodium_Compat::crypto_aead_chacha20poly1305_ietf_encrypt($data, $ad, $nonce, $key); // Noncompliant
    ParagonIE_Sodium_Compat::crypto_aead_xchacha20poly1305_ietf_encrypt($data, $ad, $nonce, $key); // Noncompliant
    ParagonIE_Sodium_Compat::crypto_aead_chacha20poly1305_encrypt($data, $ad, $nonce, $key); // Noncompliant

    ParagonIE_Sodium_Compat::crypto_aead_aes256gcm_encrypt($data, $ad, $nonce, $key); // Noncompliant

    ParagonIE_Sodium_Compat::crypto_box($data, $nonce, $key); // Noncompliant
    ParagonIE_Sodium_Compat::crypto_secretbox($data, $nonce, $key); // Noncompliant
    ParagonIE_Sodium_Compat::crypto_box_seal($data, $key); // Noncompliant
    ParagonIE_Sodium_Compat::crypto_secretbox_xchacha20poly1305($data, $nonce, $key); // Noncompliant
}

use Zend\Crypt\FileCipher;
use Zend\Crypt\PublicKey\DiffieHellman;
use Zend\Crypt\PublicKey\Rsa;
use Zend\Crypt\Hybrid;
use Zend\Crypt\BlockCipher;

function myZendEncrypt($key, $data, $prime, $options, $generator, $lib)
{
    new FileCipher; // Noncompliant

    new DiffieHellman($prime, $generator, $key); // Noncompliant

    new Rsa; // Noncompliant
    $rsa = Rsa::factory([ // Noncompliant
        'public_key'    => 'public_key.pub',
        'private_key'   => 'private_key.pem',
        'pass_phrase'   => 'mypassphrase',
        'binary_output' => false,
    ]);
    $rsa->encrypt($data); // No issue raised here. The configuration of the Rsa object is the line to review.

    $hybrid = new Hybrid(); // Noncompliant

    new BlockCipher(); // Noncompliant
    BlockCipher::factory($lib, $options); // Noncompliant
}

use Craft;

// This is similar to Yii as it used by CraftCMS
function craftEncrypt($data, $key, $password) {
    Craft::$app->security->encryptByKey($data, $key); // Noncompliant
    Craft::$app->getSecurity()->encryptByKey($data, $key); // Noncompliant
    Craft::$app->security->encryptByPassword($data, $password); // Noncompliant
    Craft::$app->getSecurity()->encryptByPassword($data, $password); // Noncompliant
    Craft::$app->getSecurity()->encryptByPassword["abc"](); // OK
}

use Yii;

// Similar to CraftCMS as it uses Yii
function YiiEncrypt($data, $key, $password) {
    Yii::$app->security->encryptByKey($data, $key); // Noncompliant
    Yii::$app->getSecurity()->encryptByKey($data, $key); // Noncompliant
    Yii::$app->security->encryptByPassword($data, $password); // Noncompliant
    Yii::$app->getSecurity()->encryptByPassword($data, $password); // Noncompliant
}

class EncryptionController extends CI_Controller
{
    public $isAuthorized = 3;

    public function __construct()
    {
        parent::__construct();
        $this->load->library('encryption');
    }

    public function index()
    {
        $this->encryption->create_key(16); // Noncompliant
        $this->encryption->initialize( // Noncompliant
            array(
                'cipher' => 'aes-256',
                'mode' => 'ctr',
                'key' => 'the key',
            )
        );
        $this->encryption->initialize["foo"](); // OK
        $this->not_encryption->initialize(); // OK
        $this->encryption->encrypt("mysecretdata"); // Noncompliant
    }
}

new class() extends CI_Controller { }; // Ok
new class() extends CI_Controller {
    public function index()
    {
        $this->encryption->create_key(16); // Noncompliant
    }
};
new class() extends Not_CI_Controller {
    public function index()
    {
        $this->encryption->create_key(16); // OK - not extending CI_Controller
    }
};
new class() implements CipherInterface { }; // Noncompliant

use Illuminate\Support\Facades\Crypt;
use Illuminate\Support as CryptAlias;

function myLaravelEncrypt($data)
{
    Crypt::encryptString($data); // Noncompliant
    \Illuminate\Support\Facades\Crypt::encrypt($data); // Noncompliant
    CryptAlias\Facades\Crypt::encryptString($data); // Noncompliant
    // encrypt using the Laravel "encrypt" helper
    encrypt($data); // Noncompliant
    ABC::encryptString($data); // Ok - not from framework
}
