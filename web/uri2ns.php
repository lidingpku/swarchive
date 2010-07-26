<?php
define("INPUT_URI", "uri");
require_once("inc/ToolUri.php");

$params_all= array();
$params_all[INPUT_URI] = urldecode(get_param(INPUT_URI));

echo ToolUri::get_namespace($params_all[INPUT_URI]);


/********************************************************************************
 Section 5  Source code - functions
*********************************************************************************/

////////////////////////////////////////
// functions - process HTTP request
////////////////////////////////////////

// get a the value of a key (mix)
function get_param($key, $default=false){
        if (is_array($key)){
                foreach ($key as $onekey){
                        $ret = get_param($onekey);
                        if ($ret)
                                return $ret;
                }
        }else{  
               
                if ($_GET)
                        if (array_key_exists($key,$_GET))
                                return $_GET[$key];
                if ($_POST)
                        if (array_key_exists($key,$_POST))
                                return $_POST[$key];    
        }
       
        return $default;
}


?>