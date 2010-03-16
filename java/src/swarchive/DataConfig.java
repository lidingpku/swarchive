package swarchive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import sw4j.util.ToolSafe;

public class DataConfig extends Properties{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String G_FILENAME = "swarchive.conf";
	public static final String G_DATA= "data";
	public static final String G_SEED = "seed";
	public static final String G_LOG = "log";
	public static final String G_CURRENT = "current";
	public static final String G_HISTORY = "history";

	public static final String CONFIG_DIR_HOME = "dir_home";
	public static final String CONFIG_JOB_ONTOLOGY = "job_ontology";
	
	
	
	public static DataConfig create(String config_filename) throws FileNotFoundException, IOException{	
		
		if (null==config_filename){
			return create();
		}else{
			DataConfig properties = new DataConfig();
			File f_conf = getFileConfig(config_filename);
			System.out.println("loading ... "+ f_conf.getAbsolutePath());

			properties.load(new FileInputStream(f_conf));						
			return properties;		
		}
		
	}

	
	public static DataConfig create(){
		DataConfig properties = new DataConfig();
		properties.put(CONFIG_DIR_HOME, ".");
		properties.put(CONFIG_JOB_ONTOLOGY, "job_ontology.csv");
		return properties;							
	}
	
	public String getDirHome(){
		String ret =this.getProperty(CONFIG_DIR_HOME);
		if (ToolSafe.isEmpty(ret))
			return ".";
		else
			return ret;
	}
	
	public static File getFileConfig(String filename){
		if (ToolSafe.isEmpty(filename)){
			return new File(G_FILENAME);
		}else{
			return new File(filename);			
		}
	}
	
	public File getFileJobOntology(){
		String filename = String.format("%s/%s", getDirData(G_SEED), this.getProperty(CONFIG_JOB_ONTOLOGY));
		return  new File(filename);
	}
	
	public  File getFileLog(){
	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/yyyy-MM-dd");
		String path_date = formatter.format(new Date());
		return new File(String.format("%s/%s-log.csv",getDirData(G_LOG),path_date));
	}


	public File getDirData(String szSub){
		if (ToolSafe.isEmpty(szSub))
			return new File(String.format("%s/%s",getDirHome(), G_DATA));
		else
			return new File(String.format("%s/%s/%s",getDirHome(), G_DATA, szSub));
		
	}

}
