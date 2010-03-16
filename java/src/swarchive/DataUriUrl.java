package swarchive;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import sw4j.util.DataQname;
import sw4j.util.Sw4jException;
import sw4j.util.ToolHash;
import sw4j.util.ToolSafe;
import sw4j.util.ToolURI;

public class DataUriUrl {
	
	DataConfig config=null;
	
	String uri ;
	String url ;
//	String filename_url ;
	String hash_url ;
	String norm_url ;
	String rel_path_url; 

	public static DataUriUrl create(String szUri, DataConfig config) throws Sw4jException{
		DataUriUrl data = new DataUriUrl();
		data.config = config;
		data.url= szUri;
		data.url = DataQname.extractNamespaceUrl(szUri);
//		data.filename_url = filenameUrl(data.url);
		data.hash_url = hashUrl(data.url);
		data.norm_url = normUrl(data.url);
		data.rel_path_url = data.hash_url.substring(0,2);
				
		return data;
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
	public File getFileCurrent(){
		return new File(String.format("%s/%s/%s",config.getDirData(DataConfig.G_CURRENT),rel_path_url, norm_url));
	}
	
	
	public File getFileHistory(Date date){
		if (ToolSafe.isEmpty(date)){
		    date = new Date();
		}
	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String path_date = formatter.format(date);
		return new File(String.format("%s/%s/%s/%s/%s",config.getDirData(DataConfig.G_HISTORY), rel_path_url, norm_url, path_date ,norm_url));
	}
	
}
