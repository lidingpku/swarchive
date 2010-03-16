package swarchive;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;

import sw4j.rdf.load.AgentModelLoader;
import sw4j.rdf.util.AgentModelStat;
import sw4j.util.Sw4jException;
import sw4j.util.ToolIO;
import sw4j.util.ToolString;

import com.hp.hpl.jena.rdf.model.Model;

public class JobDiscover {
	private DataConfig config = new DataConfig();

	public static void main(String[] args){
		
		try {
			
			DataConfig config = new DataConfig();
			//load config
			if (args.length>0)
				config = DataConfig.load(args[0]);
			
			JobDiscover disc = new JobDiscover(config);
			disc.run();
			
			JobArchive archive= new JobArchive(config);
			archive.run_ontology_todo();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Sw4jException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 		
	}
	
	public JobDiscover(DataConfig config){
		this.config = config;
	}
	
	
	private void run() throws IOException {
		//get starting date
		Date date = config.getDiscDate();

		//process today's log
		File f_log = config.getFileLog(date);
		process_one_log(f_log);
		
		//update config, move to the next date
		config.setDiscDate(DataConfig.incrementDate(date));
		config.store();
	}
	
	private void process_one_log(File f_log) throws IOException {
		System.out.println("processing ... "+ f_log.getAbsolutePath());
		BufferedReader reader = new BufferedReader(new FileReader(f_log));
		String line=null;
		
		//skip the first line which contains header
		reader.readLine();
		
		while (null!=(line=reader.readLine())){
			DataJobDiscover job = new DataJobDiscover();
			
			if (!job.initFromCsv(line))
				continue;
			
			//now process the job
			try {
				process_one_url(job.getAsString(DataJob.JOB_URL), new Date(Long.parseLong(job.getAsString(DataJob.JOB_TS_HISTORY))));
			} catch (NumberFormatException e) {
				getLogger().warn(e.getMessage());
			} catch (Sw4jException e) {
				getLogger().warn(e.getMessage());
			}
		}
		
	}
	

	private void process_one_url(String szUrl, Date date_history) throws Sw4jException {
		//parse url
		DataUriUrl uu = DataUriUrl.create(szUrl);
		
		// get cached document
		File f_cached = config.getFileCurrent(uu);
		if (null!= date_history){
			f_cached = config.getFileHistory(uu, date_history);
		}
		
		// parse RDF
		AgentModelLoader loader = new AgentModelLoader(f_cached.getAbsolutePath(), uu.url,null);
		Model m = loader.getModelData();
		
		if (null==m){
			getLogger().warn("no triples for "+ szUrl+" in file "+ f_cached.getAbsolutePath());
			return;			
		}
		
		//generate stat
		AgentModelStat stat = new AgentModelStat();
		stat.traverse(m);
		
		
		//append observed ontologies to todo list of ontology
		File f_ontology_todo = config.getFileJobOntologyTodo();
		String content=String.format("# ontology found for URL %s in FILE %s\n ", uu.url , f_cached.getAbsolutePath());
		content +=ToolString.printCollectionToString(stat.listOntologies());
		ToolIO.pipeStringToFile(content, f_ontology_todo, false, true);
	}
	
	public Logger getLogger(){
		return Logger.getLogger(this.getClass());
	}

}