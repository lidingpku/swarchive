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
			
			agent.process_job(config.getFileLogJob(),true);

			
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
		log.put(DataJob.JOB_CHANGE_TYPE, "");
		log.put(DataJob.JOB_CHANGE_ONLINE_BEFORE, "");
		log.put(DataJob.JOB_CHANGE_ONLINE_NOW, "");
		log.put(DataJob.JOB_CHANGE_CONTENT_CACHED, "");
		log.put(DataJob.JOB_CHANGE_CONTENT_CHANGED, "");
		
		log.put(DataJob.JOB_CNT_LENGTH, "");
		log.put(DataJob.JOB_TS_JOB, System.currentTimeMillis());
		log.put(DataJob.JOB_TS_MODIFIED, "");
		log.put(DataJob.JOB_SHA1SUM, "");
		log.put(DataJob.JOB_CNT_TRIPLE, 0);
		
		//section 1: pre-process: validate/parse URI
		DataLodUri uu = null;
		try{
			uu = DataLodUri.create(szUri);

			//record cleared URL
			log.put(DataJob.JOB_URL, uu.url);
//			log.put("filename", uu.filename_url);

		} catch (Sw4jException e) {
			getLogger().info("error: "+ e.getMessage());

			//write to daily log
			ToolMyUtil.writeCsv(this.config.getFileLogLog(null), log);
			return;
		}
		
		File file_current = config.getFileCurrent(uu);
		boolean bCached = file_current.exists();
		log.put(DataJob.JOB_CHANGE_CONTENT_CACHED, bCached);

		boolean bOnline = config.getFileHistoryStatusOnline(uu).exists();
		log.put(DataJob.JOB_CHANGE_ONLINE_BEFORE, bOnline);

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

		//download file
		AgentModelLoader loader= new AgentModelLoader(uu.url);
		boolean  bLoaded = loader.getLoad().isLoadSucceed();
		log.put(DataJob.JOB_CHANGE_ONLINE_NOW, bLoaded);

		// get modification date
		Date dateModify = new Date();
		if (bLoaded && loader.getLoad().getLastmodified()>0)
			dateModify = new Date(loader.getLoad().getLastmodified());
		log.put(DataJob.JOB_TS_MODIFIED, dateModify.getTime());

		boolean bChanged = false;
		try {
			if (!bLoaded){
				//if the site is not accessible, skip it
				if (loader.getLoad().getState()==TaskLoad.STATE_OUTPUT_FAILED_CONNECTION_CANNOT_OPEN){
					String host_url = uu.getHostUrl();
					if (!ToolSafe.isEmpty(host_url))
						skip.add(String.format("%s.+",host_url.replace(".", "\\.")), true);
				}
				
				save_change_type( log, uu, dateModify);
				
				throw new Sw4jException(Sw4jMessage.STATE_INFO, "load failed: " + loader.getLoad().getReport().toCSVrow());
			}

			String content = loader.getLoad().getContent();
			log.put(DataJob.JOB_CNT_LENGTH, content.length());
			log.put(DataJob.JOB_SHA1SUM, ToolHash.hash_mbox_sum_sha1(content));
			
			//check for change
			if (bCached){
				bChanged = (file_current.length() != content.getBytes("UTF-8").length);
			}else{
				bChanged = true;
			}
			log.put(DataJob.JOB_CHANGE_CONTENT_CHANGED, bChanged);
			
			save_change_type(log, uu, dateModify);										

			// skip unchanged data
			if (bCached && !bChanged){
				throw new Sw4jException(Sw4jMessage.STATE_INFO, "duplicate content (by file length)." );
			}
			
			//continue process if the file has been changed
			
			if (null != loader.getModelData()){
				log.put(DataJob.JOB_CNT_TRIPLE, loader.getModelData().size());
			}
			
			//check RDF
			if (requireRDF){
				if (null== loader.getModelData()){
					throw new Sw4jException(Sw4jMessage.STATE_INFO, "need RDF." );
				}					
			}
			


			//save file to appropriate folder
			

			// archive downloaded file (new version)
			ToolIO.pipeStringToFile(loader.getLoad().getContent(), config.getFileHistory(uu,dateModify));

			// update change log of the file
			ToolMyUtil.writeCsv(this.config.getFileHistoryLog(uu), log);
			
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
	
	public void save_change_type(DataSmartMap log, DataLodUri uu, Date date){

		get_change_type(log);

		boolean bOnlineNow= log.getAsString(DataJob.JOB_CHANGE_ONLINE_NOW).equals("true");

		//write RSS - which files were changed on that date;
		if (bOnlineNow){
			ToolMyUtil.writeCsv(this.config.getFileIndexRss(date), log);
		}
		
		//write status file
		if (bOnlineNow){		
			ToolMyUtil.writeCsv(this.config.getFileHistoryStatusOnline(uu), log);
		}else{
			this.config.getFileHistoryStatusOnline(uu).delete();			
		}
	}
	
	/*
cached	online		loaded	changed		type
T	X		T	T		update
F	X		T	X		new
T	T		F	X		offline
T	F		F	X		skip
F	X		F	X		skip
T	T		T	F		same
T	F		T	F		online
	 
	 */
		
	public static String get_change_type(DataSmartMap log){
		boolean bCached = log.getAsString(DataJob.JOB_CHANGE_CONTENT_CACHED).equals("true");
		boolean bChanged= log.getAsString(DataJob.JOB_CHANGE_CONTENT_CHANGED).equals("true");
		boolean bOnlineBefore = log.getAsString(DataJob.JOB_CHANGE_ONLINE_BEFORE).equals("true");
		boolean bOnlineNow= log.getAsString(DataJob.JOB_CHANGE_ONLINE_NOW).equals("true");

		//set change type
		String change_type ="";
		if (bCached && bOnlineNow && bChanged){
			change_type= DataJob.VALUE_CHANGE_TYPE_UPDATE;
		}else if (!bCached && bOnlineNow){
			change_type= DataJob.VALUE_CHANGE_TYPE_NEW;
		}else if (bCached &&  bOnlineBefore && !bOnlineNow){
			change_type= DataJob.VALUE_CHANGE_TYPE_OFFLINE;
		}else if (bCached && !bOnlineBefore && !bOnlineNow ){
			change_type= DataJob.VALUE_CHANGE_TYPE_SKIP;
		}else if (!bCached && !bOnlineNow ){
			change_type= DataJob.VALUE_CHANGE_TYPE_SKIP;
		}else if (bCached && bOnlineBefore && bOnlineNow && !bChanged){
			change_type= DataJob.VALUE_CHANGE_TYPE_SAME;
		}else if (bCached && !bOnlineBefore && bOnlineNow && !bChanged){
			change_type= DataJob.VALUE_CHANGE_TYPE_ONLINE;
		}
		log.put(DataJob.JOB_CHANGE_TYPE, change_type);
		
		return change_type;
	}
}
