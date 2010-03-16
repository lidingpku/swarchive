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
import sw4j.util.DataLRUCache;
import sw4j.util.DataSmartMap;
import sw4j.util.Sw4jException;
import sw4j.util.Sw4jMessage;
import sw4j.util.ToolHash;
import sw4j.util.ToolIO;

public class JobArchive {
	private DataConfig config = null;
	
	public static void main(String[] args){
		
		try {
			
			DataConfig config = new DataConfig();
			//load config
			if (args.length>0)
				config = DataConfig.load(args[0]);
			
			JobArchive agent = new JobArchive(config);
			agent.run_ontology();
			
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
	}
	
	
	/**
	 * process user defined ontology seeds
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void run_ontology() throws FileNotFoundException, IOException{
		//1. process ontology job
		process_job(config.getFileJobOntology(),true);
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
		
		DataLRUCache<String> visited = new DataLRUCache<String>(1000);
		
		String line=null;
		while (null!=(line=reader.readLine())){
			DataJobArchive job = new DataJobArchive();

			//parse job description
			if (!job.initFromCsv(line))
				continue;
			
			//avoid duplicated job
			if (visited.contains(line))
				continue;
			visited.add(line);
			
			//now process the job
			archive(job.getAsString(DataJobArchive.JOB_URI), requireRDF);
			
			
		}

	}
	
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
		//process URL
		try {
			DataUriUrl uu = DataUriUrl.create(szUri);

			log.put(DataJob.JOB_URL, uu.url);
//			log.put("filename", uu.filename_url);

			//download file
			AgentModelLoader loader= new AgentModelLoader(uu.url);

			if (!loader.getLoad().isLoadSucceed()){
				throw new Sw4jException(Sw4jMessage.STATE_INFO, "load failed: " + loader.getLoad().getReport().toCSVrow());
			}

			//check duplication
			String content = loader.getLoad().getContent();
			log.put(DataJob.JOB_CNT_LENGTH, content.length());
			log.put(DataJob.JOB_SHA1SUM, ToolHash.hash_mbox_sum_sha1(content));

			File file_current = config.getFileCurrent(uu);
			if (file_current.exists()){
				if (file_current.length()==content.getBytes("UTF-8").length){
					log.put(DataJob.JOB_DUPLICATED, true);
					throw new Sw4jException(Sw4jMessage.STATE_INFO, "duplicate content (by file length)." );
				}
			}
			
			//check RDF
			if (requireRDF){
				if (null== loader.getModelData()){
					throw new Sw4jException(Sw4jMessage.STATE_INFO, "need RDF." );
				}					
				log.put(DataJob.JOB_CNT_TRIPLE, loader.getModelData().size());
			}
			
			log.put(DataJob.JOB_TS_MODIFIED, loader.getLoad().getLastmodified());

			//save file to appropriate folder
			
			//save history
			Date date = new Date();
			if (loader.getLoad().getLastmodified()>0)
				date = new Date(loader.getLoad().getLastmodified());
			log.put(DataJob.JOB_TS_HISTORY, date.getTime());

			File file_history = config.getFileHistory(uu,date);
			ToolIO.pipeStringToFile(loader.getLoad().getContent(), file_history);

			//save current
			ToolIO.pipeStringToFile(loader.getLoad().getContent(), file_current);
			
			
		} catch (Sw4jException e) {
			getLogger().info("error: "+ e.getMessage());
		} catch (UnsupportedEncodingException e) {
			getLogger().info("error: "+ e.getMessage());
		}
		
		//generate log
		try {
			File file_log = this.config.getFileLog(null);
			if (!file_log.exists()){
				ToolIO.pipeStringToFile(log.toCSVheader()+"\n",file_log);
			}

			ToolIO.pipeStringToFile(log.toCSVrow()+"\n",file_log,false,true);
		} catch (Sw4jException e) {
			getLogger().info("error: "+ e.getMessage());
		}
	}
	
	public Logger getLogger(){
		return Logger.getLogger(this.getClass());
	}
}
