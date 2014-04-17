<?php

define ( 'THRIFT_SCHEME', 'http' );
define ( 'THRIFT_HOST', 'localhost' );
define ( 'THRIFT_PORT', 8080 );
define ( 'THRIFT_PATH', '/thriftee-examples-war' );
define ( 'THRIFT_PATH_SERVICES', THRIFT_PATH . '/services/endpoint' );
define ( 'THRIFT_PATH_CLIENT', THRIFT_PATH . '/clients/php' );
define ( 'THRIFT_SERVICES', serialize(array(
	'PresidentService' => 'org\thriftee\examples\presidents',
)));

use Thrift\Protocol\TBinaryProtocol;
use Thrift\Transport\TSocket;
use Thrift\Transport\THttpClient;
use Thrift\Transport\TBufferedTransport;
use Thrift\Exception\TException;
use Thrift\Protocol\TMultiplexedProtocol;

use Thrift\ClassLoader\ThriftClassLoader;

$thrift = null;
function _thrift_setup() {
    global $thrift;
    
    if (defined ( 'THRIFT_SETUP' )) {
        return;
    }
    
    $GEN_DIR = dirname ( __FILE__ ) . '/gen-php';
    
    if (! file_exists ( $GEN_DIR . '/.client_downloaded' ) ) {
        if (! file_exists ( $GEN_DIR )) {
            mkdir ( $GEN_DIR );
        }
        $it = new RecursiveIteratorIterator ( new RecursiveDirectoryIterator ( $GEN_DIR ), RecursiveIteratorIterator::CHILD_FIRST );
        foreach ( $it as $entry ) {
            if ($entry->isDir ()) {
                $basename = $entry->getBasename ();
                if ($entry->getBasename () != '.' && $entry->getBasename () != '..') {
                    echo 'Deleting dir: ' . $entry->getPathname () . "\n";
                    rmdir ( $entry->getPathname () );
                }
            } else {
                unlink ( $entry->getPathname () );
            }
        }
        $zip_file = $GEN_DIR . DIRECTORY_SEPARATOR . 'downloaded_client.zip';
        if ($file = fopen ( $zip_file, 'w' )) {
            $url = THRIFT_SCHEME . '://' . THRIFT_HOST . ':' . THRIFT_PORT . THRIFT_PATH_CLIENT . '?download=zip';
            echo "$url\n";
            $curl = curl_init ($url);
            curl_setopt ( $curl, CURLOPT_SSL_VERIFYPEER, false );
            curl_setopt ( $curl, CURLOPT_FILE, $file );
            curl_exec ( $curl );
            $info = curl_getinfo ( $curl );
            curl_close ( $curl );
            fclose ( $file );
            if ($info['http_code'] != 200) {
                die ( "Invalid response from $url: " . print_r($info, true) );
            }
            if (file_exists($zip_file)) {
                $zip = new ZipArchive ();
                if ($zip->open ( $zip_file ) ) {
                    if ($zip->extractTo ( $GEN_DIR )) {
                        file_put_contents ( $GEN_DIR . '/.client_downloaded', date ( 'c' ) );
                        unlink ( $zip_file );
                    } else {
                        die ( 'could not extract file to: ' . $GEN_DIR );
                    }
                } else {
                    die ( 'Could not open downloaded zip file: ' . $zip_file );
                }
            } else {
                die ( 'Zip file with client not found: ' . $zip_file );
            }
        } else {
            die ( 'could not open file for writing: ' . $zip_file );
        }
    }

    require_once $GEN_DIR . '/Thrift/ClassLoader/ThriftClassLoader.php';
    
    $loader = new ThriftClassLoader ();
    $loader->registerNamespace( 'Thrift', $GEN_DIR );
    $services = unserialize(THRIFT_SERVICES);
    foreach ( $services as $service => $namespace ) {
        echo "$namespace => $GEN_DIR\n";
        $loader->registerDefinition( $namespace, $GEN_DIR );
    }
    $loader->register();
    
    $socket = new THttpClient ( THRIFT_HOST, THRIFT_PORT, THRIFT_PATH_SERVICES, THRIFT_SCHEME );
    
    $transport = new TBufferedTransport ( $socket, 1024, 1024 );
    $protocol = new TBinaryProtocol ( $transport );
    
    $thrift = new \stdClass ();
    $thrift->_socket    =   $socket;
    $thrift->_transport =   $transport;
    $thrift->_protocol  =   $protocol;
    
    $clients = array();
    foreach ( $services as $service => $namespace ) {
        $className = '\\' . $namespace . '\\' . $service . 'Client';
        $multiplex = new TMultiplexedProtocol($protocol, $service);
        $clients[$service] = new $className($multiplex);
    }
    $thrift->clients = (object) $clients;
    
    $transport->open();

    define('THRIFT_SETUP', true);
}

_thrift_setup();

?>
