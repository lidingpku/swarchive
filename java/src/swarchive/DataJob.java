package swarchive;

import java.util.StringTokenizer;

import sw4j.util.DataSmartMap;
import sw4j.util.ToolSafe;

public abstract class DataJob extends DataSmartMap{
	public static final String JOB_SHA1SUM ="sha1sum";
	public static final String JOB_URI ="uri";
	public static final String JOB_URL ="url";
	public static final String JOB_CNT_LENGTH ="cnt_length";
	public static final String JOB_CNT_TRIPLE ="cnt_triple";

	public static final String JOB_TS_JOB ="ts_job";			// must, 
	public static final String JOB_TS_MODIFIED ="ts_modified";	// if downloaded
//	public static final String JOB_TS_HISTORY ="ts_history";	// if downloaed and cached

	public static final String JOB_REQUIRERDF ="requireRDF";
	
	public static final String JOB_CHANGE_TYPE ="change_type";
	public static final String JOB_CHANGE_ONLINE_BEFORE ="change_online_before";	//previously status: online
	public static final String JOB_CHANGE_ONLINE_NOW ="change_online_now";	//current status: online
	public static final String JOB_CHANGE_CONTENT_CACHED ="change_content_cached";	//old version exists
	public static final String JOB_CHANGE_CONTENT_CHANGED ="change_content_changed";	// old version exists, plus the new version is different from the old version, a new version has been cached

	
	public static final String VALUE_CHANGE_TYPE_NEW= "new";	
	public static final String VALUE_CHANGE_TYPE_UPDATE= "update";
	public static final String VALUE_CHANGE_TYPE_SAME = "same";
	public static final String VALUE_CHANGE_TYPE_ONLINE= "online";
	public static final String VALUE_CHANGE_TYPE_OFFLINE= "offline";
	public static final String VALUE_CHANGE_TYPE_SKIP= "skip";
	
	abstract public String[] getFields();

	
	/**
	 * parse a csv line to get job data
	 * @param line
	 * @return
	 */
	public boolean initFromCsv(String line){
		//skip empty or comment line
		line = line.trim();
		if (ToolSafe.isEmpty(line)|| line.startsWith("#"))
			return false;

		//parse lines
		try{
			StringTokenizer st = new StringTokenizer(line,",");
			for (String field :getFields()){
				String value = st.nextToken();
				value = value.trim();
				if (value.startsWith("\"")&&value.endsWith("\""))
					value = value.substring(1,value.length()-1);
				value = value.trim();
				this.put(field, value);
			}
			return true;
		}catch (Exception e){
			getLogger().warn(e.getMessage());
			return false;
		}
	}
}
