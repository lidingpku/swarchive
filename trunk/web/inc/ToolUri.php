<?php
class ToolUri{
  public static function get_url($uri){
	$namespace = ToolUri::get_namespace($uri);

	$index = strpos( $namespace , "#");
	if ($index !== FALSE ) {
		$namespace = substr($namespace, 0, strlen($namespace) );
	}

	return $namespace;
  }

  public static function get_namespace($uri){
	$index = ToolUri::split_uri($uri);
	if ($index < strlen($uri)){
		$namespace= substr($uri,0, $index);
	}else{
		$namespace = $uri;
	}
	return $namespace;
  }

  public static function split_uri ($uri){
	$well_known_ns = array(
		"http://sws.geonames.org/",	// there URIs are ugly   http://sws.geonames.org/1283416/
		"http://rdf.freebase.com/ns/", //freebase
		"http://data.nytimes.com/",  // http://data.nytimes.com/34102657707806421181
		"http://sw.cyc.com/concept/", 
		"http://ontology.dumontierlab.com/",
		"http://purl.uniprot.org/core/",
		"http://rdf.insee.fr/geo/",
		"http://web.resource.org/cc/",
		"http://www.w3.org/2006/03/wn/wn20/schema/",

		"http://xmlns.com/foaf/0.1/",
		"http://xmlns.com/wot/0.1/",
		"http://xmlns.com/wordnet/1.6/",

		"http://purl.org/dc/elements/1.1/",
		"http://purl.org/dc/terms/",
		"http://purl.org/rss/1.0/",
		"http://purl.org/dc/dcmitype/",
		"http://purl.org/vocab/bio/0.1/",

		"http://dbpedia.org/class/yago/",
		"http://dbpedia.org/ontology/",
		"http://dbpedia.org/resource/", //dbpedia

		"http://sw.opencyc.org/concept/",
		"http://wiki.infowiss.net/Spezial:URIResolver/Kategorie-3A"
	);

	$well_known_special_pattern = array(
		"http://umbel.org/.*"=>"/",
		"http://sw.nokia.com/.*"=>"/",
		"http://wiki.infowiss.net/.*"=>"/",
		"http://www.rdfabout.com/.*"=>"/",
		"http://sw.opencyc.org/.*"=>"/",
		"http://xmlns.com/.*"=>"/",
		"http://dbpedia.org/.*"=>"/",
		"http://purl.org/.*"=>"/",
//		"http://data-gov.tw.rpi.edu/.*"=>"/",
		".*Category-3A.*"=>"Category-3A",
		".*Kategorie-3A.*"=>"Kategorie-3A",
		".*:URIResolver/.*"=>"/",
		"http://bio2rdf.org/.*"=>":",
		".*/resource/.*"=>"/",
		".*/class/.*"=>"/",
		".*/things/.*"=>"/"
	);

	$index = strpos( $uri, "#");
	if ($index === 0) {
		return 1;
	} else if ($index === FALSE ) {

		foreach ($well_known_ns as $ns){
			if (strncmp($uri, $ns, strlen($ns))==0){
				return strlen($ns);
			}
		}
		

		foreach ($well_known_special_pattern as $pattern => $locator){
			if (preg_match($pattern, $uri)){
				$index= strrpos( $uri, $locator);
				if ($index>0 && $index < strlen($uri))
					return $index+ strlen($locator);
			}
		}
		return strlen($uri);
	} else {
		return $index+1;
	}


   }
}	  

?>