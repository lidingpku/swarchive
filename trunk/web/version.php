<?php


/********************************************************************************
 Section 1. general information
*********************************************************************************/

/*
author: Li Ding (http://www.cs.rpi.edu/~dingl)
created: July 25, 2010
modified: July 25, 2010

MIT License

Copyright (c) 2010 -2010

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

*/



/********************************************************************************
 Section 2.  Readme
*********************************************************************************/
/*
1. Installation

software stack
* php


2. Change Log

2010-07-25, version 0.1 (Li)
* created 

*/


/********************************************************************************
 Section 3  Source code - Configuration
*********************************************************************************/

define("ME_NAME", "Semantic Web Archive RSS");
define("ME_VERSION", "2010-07-25");
define("ME_AUTHOR", "Li Ding");
define("ME_CREATED", "2009-07-25");

define("G_ROOT_DATA", "/work/swarchive/svn/data");
define("G_HISTORY", "history");

define("G_ROOT_WEB", "http://sam.tw.rpi.edu/swarchive");
define("G_WEB_RSS", G_ROOT_WEB."/rss");
define("G_WEB_VERSION", G_ROOT_WEB."/version");



define("INPUT_VERSION", "version");



/********************************************************************************
 Section 4  Source code - main function
*********************************************************************************/
$params_input= array();
$params_input[INPUT_VERSION] = get_param(INPUT_VERSION);
$params_input[INPUT_DATE] = substr($params_input[INPUT_VERSION],0,10);
$params_input[INPUT_URI] = substr($params_input[INPUT_VERSION],11);


//print_r($params_input);

$url = ToolUri::uri2url($params_input[INPUT_URI]);
$url_encode= urlencode($url);

$file_version = sprintf("%s/%s/%s",get_dir_history($url), $params_input[INPUT_DATE], $url_encode);

//echo $file_version;
echo file_get_contents($file_version);



function get_url_encoded($url){
	return urlencode($url);
}

function get_dir_history($url){
	$url_hash = sha1($url);
	$url_encode = get_url_encoded($url);
	
	return sprintf("%s/%s/%s/%s", G_ROOT_DATA, G_HISTORY, substr($url_hash,0,2), $url_encode );
}


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