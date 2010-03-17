package swarchive;


import java.net.URI;

import sw4j.util.DataQname;
import sw4j.util.Sw4jException;
import sw4j.util.ToolHash;
import sw4j.util.ToolSafe;
import sw4j.util.ToolURI;

public class DataUriUrl {
	
	
	String uri ;
	String url ;
//	String filename_url ;
	String hash_url ;
	String norm_url ;
	String rel_path_url; 

	public static DataUriUrl create(String szUri) throws Sw4jException{
		DataUriUrl data = new DataUriUrl();
		data.uri= szUri;
		data.url = DataQname.extractNamespaceUrl(szUri);
		if (null==data.url)
			data.url=data.uri;
//		data.filename_url = filenameUrl(data.url);
		data.hash_url = hashUrl(data.url);
		data.norm_url = normUrl(data.url);
		data.rel_path_url = data.hash_url.substring(0,2);
				
		return data;
	}
	
	public String getHostUrl(){
		try {
			URI ret = ToolURI.extractHostUrl(ToolURI.string2uri(url));
			if (null!=ret && !ToolSafe.isEmpty(ret.getHost()))
				return ret.toString();
		} catch (Sw4jException e) {
			e.printStackTrace();
		}
		return null;
	}


	public static String hashUrl(String szUrl){
		return ToolHash.hash_mbox_sum_sha1(szUrl);
	}
	
	public static String normUrl(String szUrl) throws Sw4jException{
		return ToolURI.encodeURIString(szUrl);
	}
/*	
	public static String filenameUrl(String szUrl) throws Sw4jException{
		if (szUrl.endsWith("/"))
			return normUrl(szUrl);
		
		int index =szUrl.lastIndexOf("/");
		if (index>0)
			return szUrl.substring(index+1);
		else
			return normUrl(szUrl);
	}
*/

	
}
