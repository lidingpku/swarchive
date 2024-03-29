package swarchive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import sw4j.util.Sw4jException;
import sw4j.util.ToolIO;
import sw4j.util.ToolSafe;

public class DataConfig extends Properties{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String G_FILENAME_DEFAULT = "swarchive.conf";
	public static final String G_DIR_HOME_DEFAULT = ".";

	public static final String G_FILENAME_CHANGELOG = "changelog.v3.csv";
	public static final String G_FILENAME_STATUS_ONLINE = "online.status";

	public static final String G_PATH_PATTERN_DATE = "yyyy-MM-dd";

	public static final String G_FILENAME_SUFFIX_LOG_PING_ONLINE = "-ping-online.csv";
	public static final String G_FILENAME_SUFFIX_LOG_JOB = "-archive-job.csv";
	public static final String G_FILENAME_SUFFIX_LOG_LOG = "-archive-log.csv";
	public static final String G_FILENAME_SUFFIX_LOG_DISC_ONTO = "-discover-ontology.csv";
	public static final String G_FILENAME_SUFFIX_LOG_DISC_ONLINE = "-discover-online.csv";
	public static final String G_FILENAME_SUFFIX_INDEX_RSS = "-rss.csv";

	public static final String G_DATA= "data";
	public static final String G_CONFIG= "config";
	public static final String G_SEED = "seed";
	public static final String G_LOG = "log";
	public static final String G_INDEX = "index";
	public static final String G_RSS = "rss";
	public static final String G_CURRENT = "current";
	public static final String G_HISTORY = "history";

	public static final String CONFIG_DIR_HOME = "dir_home";
	public static final String CONFIG_FILE_SKIP_PATTERN= "file_skip_pattern";
	public static final String CONFIG_NEW_URL_ONLY= "new_url_only";
	
	//provide either a file or a date 
	public static final String CONFIG_FILE_JOB= "file_job";
	public static final String CONFIG_DATE_JOB = "date_job";	
	
	public File f_conf = new File(G_FILENAME_DEFAULT);

	public DataConfig(){
		put(CONFIG_DIR_HOME, G_DIR_HOME_DEFAULT);
		put(CONFIG_FILE_SKIP_PATTERN, "skip_pattern.txt");
		put(CONFIG_NEW_URL_ONLY, false);
	}

	
	public static DataConfig load(String config_filename) throws FileNotFoundException, IOException{	
		DataConfig properties = new DataConfig();

		if (ToolSafe.isEmpty(config_filename))
			return properties;
		
		properties.f_conf = new File(config_filename);
		
		System.out.println("loading and processing ... "+ properties.f_conf.getAbsolutePath());
		properties.load(new FileInputStream(properties.f_conf));
		
		return properties;
	}

	public boolean store(){
		if (!f_conf.canWrite()){
			getLogger().warn("cannot write to "+ f_conf.getAbsolutePath());
			return false;
		}
		
		try {
			this.store(ToolIO.prepareFileOutputStream(f_conf,false, false), "");
			return true;
		} catch (IOException e) {
			getLogger().warn(e.getMessage());
			return false;
		} catch (Sw4jException e) {
			getLogger().warn(e.getMessage());
			return false;
		}
		
	}
	
	
	/** 
	 * format date string with default date as today
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public static String formatDate(Date date, String format){
	    SimpleDateFormat formatter = new SimpleDateFormat(format);
	    if (null==date)
	    	date = new Date();
		return formatter.format(date);
	}

	/**
	 * format a file path
	 * 
	 * @param paths
	 * @param filename
	 * @return
	 */
	public static String formatFileLocation(List<String> paths, String filename){
		String ret = "";
		for (String path : paths){
			ret += path+"/";
		}
		
		if (!ToolSafe.isEmpty(filename))
			ret +=filename;
		
		return ret;
	}
	
	public static Date incrementDate(Date date){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, 1);  // number of days to add
		return c.getTime();  // dt is now the new date
	}
	
//	
	
	public List<String> createPathsData(){
		ArrayList<String> paths= new ArrayList<String>();
		paths.add(getProperty(CONFIG_DIR_HOME, G_DIR_HOME_DEFAULT));
		paths.add(G_DATA);
		
		return paths;
	}
	
	public List<String> createPathsConfig(){
		ArrayList<String> paths= new ArrayList<String>();
		paths.add(getProperty(CONFIG_DIR_HOME, G_DIR_HOME_DEFAULT));
		paths.add(G_CONFIG);
		
		return paths;
	}

	


	public File getFileSkipPattern(){
		if (ToolSafe.isEmpty( this.getProperty(CONFIG_FILE_SKIP_PATTERN)))
			return null;
		
		List<String> paths = createPathsConfig();
		paths.add(G_SEED);
		String filename = formatFileLocation(paths, this.getProperty(CONFIG_FILE_SKIP_PATTERN));
		return new File(filename);
	}

	public File getFileLogJob(){
		String filename = this.getProperty(CONFIG_FILE_JOB);
		if (ToolSafe.isEmpty(filename)){
			return getFileLogJob( getDateJob() );
		}
		return new File(filename);
	}

	public  File getFileLogJob(Date date){
		return getFileLog(date, G_FILENAME_SUFFIX_LOG_JOB);
	}

	public  File getFileLogLog(Date date){
		return getFileLog(date, G_FILENAME_SUFFIX_LOG_LOG);
	}


	public  File getFileLogDiscoverOntology(Date date){
		return getFileLog(date, G_FILENAME_SUFFIX_LOG_DISC_ONTO);
	}
	
	public  File getFileLogDiscoverOnline(Date date){
		return getFileLog(date, G_FILENAME_SUFFIX_LOG_DISC_ONLINE);
	}

	public  File getFileLogPingOnline(Date date){
		return getFileLog(date, G_FILENAME_SUFFIX_LOG_PING_ONLINE);
	}

	private File getFileLog(Date date, String filename_suffix){
		List<String> paths = createPathsData();
		paths.add(G_LOG);
		String filename = formatFileLocation(paths, String.format("%s%s",formatDate(date, "yyyy/"+G_PATH_PATTERN_DATE),filename_suffix));
		return new File(filename);		
	}

	public  File getFileIndexRss(Date date){
		String filename_suffix = G_FILENAME_SUFFIX_INDEX_RSS;
		List<String> paths = createPathsData();
		paths.add(G_INDEX);
		paths.add(G_RSS);
		String filename = formatFileLocation(paths, String.format("%s%s",formatDate( date, "yyyy/"+G_PATH_PATTERN_DATE),filename_suffix));
		return new File(filename);		
	}

	public File getFileCurrent(DataLodUri uu){
		List<String> paths = createPathsData();
		paths.add(G_CURRENT);
		paths.add(uu.rel_path_url);
		String filename = formatFileLocation(paths, uu.norm_url);
		return new File(filename);
	}

	public File getFileCurrentRdf(DataLodUri uu){
		List<String> paths = createPathsData();
		paths.add(G_CURRENT);
		paths.add(uu.rel_path_url);
		String filename = formatFileLocation(paths, uu.norm_url+".rdf");
		return new File(filename);
	}

	public File getDirHistory(DataLodUri uu){
		List<String> paths = createPathsData();
		paths.add(G_HISTORY);
		paths.add(uu.rel_path_url);
		paths.add(uu.norm_url);
		String filename = formatFileLocation(paths, "");
		return new File(filename);
	}

	
	public File getFileHistory(DataLodUri uu, Date date){
		List<String> paths = createPathsData();
		paths.add(G_HISTORY);
		paths.add(uu.rel_path_url);
		paths.add(uu.norm_url);
		paths.add(formatDate(date, G_PATH_PATTERN_DATE) );
		String filename = formatFileLocation(paths, uu.norm_url);
		return new File(filename);
	}

	public  File getFileHistoryLog(DataLodUri uu){
		List<String> paths = createPathsData();
		paths.add(G_HISTORY);
		paths.add(uu.rel_path_url);
		paths.add(uu.norm_url);
		String filename = formatFileLocation(paths, G_FILENAME_CHANGELOG);
		return new File(filename);
	}

	public  File getFileHistoryStatusOnline(DataLodUri uu){
		List<String> paths = createPathsData();
		paths.add(G_HISTORY);
		paths.add(uu.rel_path_url);
		paths.add(uu.norm_url);
		String filename = formatFileLocation(paths, G_FILENAME_STATUS_ONLINE);
		return new File(filename);
	}
	
	public void setDateJob(Date date){
		put(CONFIG_DATE_JOB, formatDate(date, G_PATH_PATTERN_DATE) );
	}
	
	public Date getDateJob(){
		Date date = new Date();
		String szDate = this.getProperty(CONFIG_DATE_JOB);
		if (!ToolSafe.isEmpty(szDate))
			try {
				date = new SimpleDateFormat(G_PATH_PATTERN_DATE).parse(szDate);
			} catch (ParseException e) {
				getLogger().warn("bad date format in configuration file: " + CONFIG_DATE_JOB+"="+szDate);
				e.printStackTrace();
			}
		return date;
	}
	
	public Logger getLogger(){
		return Logger.getLogger(this.getClass());
	}
	
	public boolean checkNewUrlOnly(){
		return this.containsKey(CONFIG_NEW_URL_ONLY) && this.get(CONFIG_NEW_URL_ONLY).equals("true");
	}
}
