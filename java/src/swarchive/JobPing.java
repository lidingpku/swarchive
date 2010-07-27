package swarchive;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.Date;

import org.apache.log4j.Logger;

import sw4j.rdf.load.AgentModelLoader;
import sw4j.rdf.util.ToolJena;
import sw4j.task.load.TaskLoad;
import sw4j.util.DataLRUCache;
import sw4j.util.DataSmartMap;
import sw4j.util.Sw4jException;
import sw4j.util.Sw4jMessage;
import sw4j.util.ToolHash;
import sw4j.util.ToolIO;
import sw4j.util.ToolSafe;

public class JobPing {
	private DataConfig config = null;
	private DataSkipPattern skip = null;
	
	public static void main(String[] args){
		
		try {
			
			DataConfig config = new DataConfig();

			//load configuration file
			if (args.length>0)
				config = DataConfig.load(args[0]);
			
			JobPing agent = new JobPing(config);
			
			agent.process_job(config.getFileLogJob(),true);

			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 		
	}
	
	public JobPing(DataConfig config){
		this.config = config;
		this.skip = DataSkipPattern.create(config);
	}
	
	
	

	/**
	 * process a job file, each line corresponding to a crawl
	 * 
	 * @param f_jobfile		- the file containing job description
	 * @param requireRDF	- if the target document must be an valid RDF document
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void process_job(File f_jobfile, boolean requireRDF) throws FileNotFoundException, IOException{
		System.out.println("processing ... "+ f_jobfile.getAbsolutePath());
		BufferedReader reader = new BufferedReader(new FileReader(f_jobfile));
		
		
		String line=null;
		while (null!=(line=reader.readLine())){
			DataJobArchive job = new DataJobArchive();

			//parse job description
			if (!job.initFromCsv(line))
				continue;
						
			//now process the job
			archive(job.getAsString(DataJobArchive.JOB_URI), requireRDF);
			
			
		}

	}

	DataLRUCache<String> visited = new DataLRUCache<String>(1000);

	/**
	 * archive a single URL
	 * 
	 * @param szUri
	 * @param requireRDF
	 */
	
	public void archive(String szUri, boolean requireRDF){
		//section 1: pre-process: validate/parse URI
		DataLodUri uu = null;
		try{
			uu = DataLodUri.create(szUri);
		} catch (Sw4jException e) {
			getLogger().info("error: "+ e.getMessage());
			return;
		}
		
		//section 2: pre-process: skip based on configuration
		try{			
			//1. avoid access the same URL
			if (visited.contains(uu.url)){
				visited.add(uu.url);
				throw new Sw4jException(Sw4jMessage.STATE_INFO, "[skip url: visited in this job] " + uu.url);				
			}

		} catch (Sw4jException e) {
			getLogger().info("error: "+ e.getMessage());
			return;
		}
		HttpURLConnection conn = ToolIO.openHttpConnection(uu.url, true);
		
		if (conn!=null){
			System.out.println(uu.url);
			conn.disconnect();
		}
			
	
	}
	
	public Logger getLogger(){
		return Logger.getLogger(this.getClass());
	}
	
}
