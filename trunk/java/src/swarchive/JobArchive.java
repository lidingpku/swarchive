package swarchive;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

public class JobArchive {
	private DataConfig config = null;
	private DataSkipPattern skip = null;
	
	public static void main(String[] args){
		
		try {
			
			DataConfig config = new DataConfig();

			//load configuration file
			if (args.length>0)
				config = DataConfig.load(args[0]);
			
			JobArchive agent = new JobArchive(config);
			
			agent.process_job(config.getFileJob(),true);

			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 		
	}
	
	public JobArchive(DataConfig config){
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

		DataSmartMap log = new DataSmartMap();
		log.put(DataJob.JOB_URI, szUri);
		log.put(DataJob.JOB_REQUIRERDF, requireRDF);
		log.put(DataJob.JOB_URL, "");
//		log.put("filename", "");
		log.put(DataJob.JOB_CNT_LENGTH, "");
		log.put(DataJob.JOB_TS_JOB, System.currentTimeMillis());
		log.put(DataJob.JOB_TS_MODIFIED, "");
		log.put(DataJob.JOB_TS_HISTORY, "");
		log.put(DataJob.JOB_SHA1SUM, "");
		log.put(DataJob.JOB_CNT_TRIPLE, 0);
		log.put(DataJob.JOB_DUPLICATED, false);
		
		//section 1: pre-process: validate/parse URI
		DataLodUri uu = null;
		File file_current =null;
		try{
			uu = DataLodUri.create(szUri);

			//record cleared URL
			log.put(DataJob.JOB_URL, uu.url);
//			log.put("filename", uu.filename_url);

			file_current = config.getFileCurrent(uu);
			
		} catch (Sw4jException e) {
			getLogger().info("error: "+ e.getMessage());

			//write to daily log
			ToolMyUtil.writeCsv(this.config.getFileLogLog(null), log);
			return;
		}
		
		//section 2: pre-process: skip based on configuration
		try{			
			//1. avoid access the same URL
			if (visited.contains(uu.url)){
				visited.add(uu.url);
				throw new Sw4jException(Sw4jMessage.STATE_INFO, "[skip url: visited in this job] " + uu.url);				
			}

			//2. check skip pattern file
			if (skip.testSkip(uu.url)){
				throw new Sw4jException(Sw4jMessage.STATE_INFO, "[skip url: pattern filter] " + uu.url);
			}
			
			//3. check if we only handle new URL
			if (config.checkNewUrlOnly() && file_current.exists()){
				throw new Sw4jException(Sw4jMessage.STATE_INFO, "[skip url: new url only] " + uu.url);
			}
		} catch (Sw4jException e) {
			getLogger().info("error: "+ e.getMessage());
			
			//write to daily log
			ToolMyUtil.writeCsv(this.config.getFileLogLog(null), log);
			return;
		}

		
		//section 3: download/validate URL
		boolean bChanged =false;
		try {
			//download file
			AgentModelLoader loader= new AgentModelLoader(uu.url);

			if (!loader.getLoad().isLoadSucceed()){
				
				//if the site is not accessible, skip it
				if (loader.getLoad().getState()==TaskLoad.STATE_OUTPUT_FAILED_CONNECTION_CANNOT_OPEN){
					String host_url = uu.getHostUrl();
					if (!ToolSafe.isEmpty(host_url))
						skip.add(String.format("%s.+",host_url.replace(".", "\\.")), true);
				}
				
				throw new Sw4jException(Sw4jMessage.STATE_INFO, "load failed: " + loader.getLoad().getReport().toCSVrow());
			}

			//check for change
			String content = loader.getLoad().getContent();
			log.put(DataJob.JOB_CNT_LENGTH, content.length());
			log.put(DataJob.JOB_SHA1SUM, ToolHash.hash_mbox_sum_sha1(content));

			if (file_current.exists()){
				bChanged = (file_current.length()==content.getBytes("UTF-8").length);
				
				if (bChanged){
					log.put(DataJob.JOB_DUPLICATED, true);
					throw new Sw4jException(Sw4jMessage.STATE_INFO, "duplicate content (by file length)." );
				}
			}
			
			//continue process if the file has been changed
			
			//check RDF
			if (requireRDF){
				if (null== loader.getModelData()){
					throw new Sw4jException(Sw4jMessage.STATE_INFO, "need RDF." );
				}					
				log.put(DataJob.JOB_CNT_TRIPLE, loader.getModelData().size());
			}

			//record modification timestamp
			log.put(DataJob.JOB_TS_MODIFIED, loader.getLoad().getLastmodified());

			//save file to appropriate folder
			
			// get history date
			Date date = new Date();
			if (loader.getLoad().getLastmodified()>0)
				date = new Date(loader.getLoad().getLastmodified());
			log.put(DataJob.JOB_TS_HISTORY, date.getTime());

			// archive downloaded file (new version)
			ToolIO.pipeStringToFile(loader.getLoad().getContent(), config.getFileHistory(uu,date));

			// update change log of the file
			ToolMyUtil.writeCsv(this.config.getFileHistoryLog(uu, date), log);
			
			// replace the current file with the new version
			ToolIO.pipeStringToFile(loader.getLoad().getContent(), file_current);
			
			// replace the current rdf with the parse result 
			ToolJena.printModelToFile(loader.getModelData(), config.getFileCurrentRdf(uu));
			
		} catch (Sw4jException e) {
			getLogger().info("error: "+ e.getMessage());
		} catch (UnsupportedEncodingException e) {
			getLogger().info("error: "+ e.getMessage());
		}
		
		//write to daily log
		ToolMyUtil.writeCsv(this.config.getFileLogLog(null), log);
	}
	
	public Logger getLogger(){
		return Logger.getLogger(this.getClass());
	}
}
