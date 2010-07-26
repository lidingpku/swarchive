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
define("G_WEB_RSS", G_ROOT_WEB."/rss.php?uri=");
define("G_WEB_VERSION", G_ROOT_WEB."/version.php?version=");



define("INPUT_URI", "uri");


/********************************************************************************
 Section 4  Source code - main function
*********************************************************************************/
$params_all= array();
$params_all[INPUT_URI] = urldecode(get_param(INPUT_URI));

if (empty($params_all[INPUT_URI])){
?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
  <title>Semantic Web Archive - RSS Service</title>
  <link href="http://data-gov.tw.rpi.edu/2009/data-gov.css"  rel="stylesheet" type="text/css" />
</head>

<body>
<h3>Semantic Web Archive - RSS Service (0.1)</h3>

<fieldset>
<legend>Refresh a URI</legend>

<form Method="GET" Action="rss.php" >
 URL:<input name="uri" size="100" value=""  />  <br/>
 <input value="query" type="submit" /><br />
</form>
</fieldset>
</body>
</html>
<?php
	exit(0);
}


//print_r($params_input);

$url = uri2url($params_all[INPUT_URI]);
$url_encode= urlencode($url);



$params = array();
$params["title"]="changelog for $url";
$params["link"]= sprintf("%s%s", G_WEB_RSS, $url_encode);
print_rss_header("",$params);


$dir = get_dir_history($url);
//echo $dir;

 //list directory
 if($handle = opendir($dir))
 {
    while($file = readdir($handle))
    {
        clearstatcache();
	 $new_dir = sprintf("%s/%s",$dir,$file);
	 $new_file = sprintf("%s/%s/%s",$dir,$file, $url_encode);
        if(!is_file($new_dir)){
	  
	    if (strcmp($file, "." )==0){
	    }else if (strcmp($file, ".." )==0){
           }else {
		$params = array();
		$params["title"]="version $file";
		$params["description"]= sprintf("%s (bytes)", filesize($new_file));
		$params["link"]=sprintf("%s%s-%s", G_WEB_VERSION, $file, $url_encode);
	
		print_rss_item($file,$params);
	    }
	 }
    }
    closedir($handle);
 } 

print_rss_footer();



function uri2url($uri){
	return $uri;
}

function get_url_encoded($url){
	return urlencode($url);
}

function get_dir_history($url){
	$url_hash = sha1($url);
	$url_encode = get_url_encoded($url);
	
	return sprintf("%s/%s/%s/%s", G_ROOT_DATA, G_HISTORY, substr($url_hash,0,2), $url_encode );
}

function print_rss_header($uri, $params){
echo "<?xml version=\"1.0\"?>\n";
?>
<rdf:RDF
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns="http://purl.org/rss/1.0/"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#">
  <channel rdf:about="<?php echo $uri; ?>">
<?php
    foreach ($params as $key => $value){
	$text = sprintf("    <%s>%s</%s>\n", $key, $value, $key);
	echo $text;
    }
?>
  </channel>
<?php


}

function print_rss_item($uri, $params){
?>
  <item rdf:ID="<?php echo $uri; ?>">
<?php
    foreach ($params as $key => $value){
	$text = sprintf("    <%s>%s</%s>\n", $key, $value, $key);
	echo $text;
    }
?>
  </item>
<?php
}

function print_rss_footer(){
?>
</rdf:RDF>
<?php
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