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
import sw4j.util.DataSmartMap;
import sw4j.util.Sw4jException;
import sw4j.util.Sw4jMessage;
import sw4j.util.ToolHash;
import sw4j.util.ToolIO;

public class JobSwArchive {
	
	public static void main(String[] args){
		
		try {
			
			JobSwArchive agent = new JobSwArchive();
			
			//load config
			if (args.length>0)
				agent.config = DataConfig.create(args[0]);
			
			//process job
			agent.process_job(agent.config.getFileJobOntology(),true);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 		
	}

	DataConfig config = DataConfig.create();
	
	public void process_job(File f_jobfile, boolean requireRDF) throws FileNotFoundException, IOException{
		System.out.println("loading ... "+ f_jobfile.getAbsolutePath());
		BufferedReader reader = new BufferedReader(new FileReader(f_jobfile));
		String line=null;
		while (null!=(line=reader.readLine())){
			DataJobArchive job = DataJobArchive.create(line);
			
			if (null==job)
				continue;
			
			//now process the job
			
			archive(job.getAsString(DataJobArchive.JOB_URI), requireRDF);
		}

	}
	
	
	public void archive(String szUri, boolean requireRDF){
		Date date = new Date();

		DataSmartMap log = new DataSmartMap();
		log.put("uri", szUri);
		log.put("requireRDF", requireRDF);
		log.put("url", "");
//		log.put("filename", "");
		log.put("cnt_length", "");
		log.put("timestamp", System.currentTimeMillis());
		log.put("modified", "");
		log.put("sha1sum", "");
		log.put("cnt_triples", 0);
		log.put("duplicated", false);
		//process URL
		try {
			DataUriUrl uu = DataUriUrl.create(szUri, this.config);

			log.put("url", uu.url);
//			log.put("filename", uu.filename_url);

			//download file
			AgentModelLoader loader= new AgentModelLoader(uu.url);

			if (!loader.getLoad().isLoadSucceed()){
				throw new Sw4jException(Sw4jMessage.STATE_INFO, "load failed: " + loader.getLoad().getReport().toCSVrow());
			}

			//check duplication
			String content = loader.getLoad().getContent();
			log.put("cnt_length", content.length());
			log.put("sha1sum", ToolHash.hash_mbox_sum_sha1(content));

			File file_current = uu.getFileCurrent();
			if (file_current.exists()){
				if (file_current.length()==content.getBytes("UTF-8").length){
					log.put("duplicated", true);
					throw new Sw4jException(Sw4jMessage.STATE_INFO, "duplicate content (by file length)." );
				}
			}
			
			//check RDF
			if (requireRDF){
				if (null== loader.getModelData()){
					throw new Sw4jException(Sw4jMessage.STATE_INFO, "need RDF." );
				}					
				log.put("cnt_triples", loader.getModelData().size());
			}
			
			//save file to appropriate folder
			if (loader.getLoad().getLastmodified()>0)
				date = new Date(loader.getLoad().getLastmodified());
			
			log.put("modified", loader.getLoad().getLastmodified());

			File file_history = uu.getFileHistory(date);

			ToolIO.pipeStringToFile(loader.getLoad().getContent(), file_current);
			ToolIO.pipeStringToFile(loader.getLoad().getContent(), file_history);
			
			
		} catch (Sw4jException e) {
			getLogger().info("error: "+ e.getMessage());
		} catch (UnsupportedEncodingException e) {
			getLogger().info("error: "+ e.getMessage());
		}
		
		//generate log
		try {
			File file_log = this.config.getFileLog();
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
