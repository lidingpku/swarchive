package swarchive;

import java.util.StringTokenizer;

import sw4j.util.DataSmartMap;
import sw4j.util.ToolSafe;

public class DataJobArchive extends DataSmartMap{
	public static final String JOB_URI ="uri";

	/**
	 * parse a line to get job structure
	 * @param line
	 * @return
	 */
	public static DataJobArchive create(String line){
		//skip empty or comment line
		line = line.trim();
		if (ToolSafe.isEmpty(line)|| line.startsWith("#"))
			return null;
	
		//parse job
		DataJobArchive  job = new DataJobArchive();
		StringTokenizer st = new StringTokenizer(line,",");
		job.put(JOB_URI,st.nextToken().trim());

		return job;
	}
}
