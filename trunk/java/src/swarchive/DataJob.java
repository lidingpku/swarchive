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
	public static final String JOB_DUPLICATED ="duplicated";
	public static final String JOB_TS_HISTORY ="ts_history";
	public static final String JOB_TS_JOB ="ts_job";
	public static final String JOB_TS_MODIFIED ="ts_modified";
	public static final String JOB_REQUIRERDF ="requireRDF";

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
