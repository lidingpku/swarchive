package swarchive;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeSet;

import sw4j.util.Sw4jException;
import sw4j.util.ToolIO;
import sw4j.util.ToolSafe;

public class DataSkipPattern {

	TreeSet<String> patterns = new TreeSet<String>();
	DataConfig config = new DataConfig();
	
	
	public static DataSkipPattern create(DataConfig config){
		DataSkipPattern ret = new DataSkipPattern();
		
		if (null!=config){
			ret.config = config;

			File f_skip_pattern = config.getFileSkipPattern();
			if (!ToolSafe.isEmpty(f_skip_pattern)){
				//load every line as a pattern
				ret.load(f_skip_pattern);
			}
		}
		
		return ret;
	}
	
	public boolean testSkip(String szUrl){
		for (String pattern : patterns){
			if (szUrl.matches(pattern))
				return true;
		}
		return false;
	}
	
	public void add(String pattern, boolean bSave){
		
		patterns.add(pattern);

		if (bSave){
			File f_skip_pattern = config.getFileSkipPattern();
			try {
				ToolIO.pipeStringToFile(pattern+"\n", f_skip_pattern, false, true);
			} catch (Sw4jException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void load(File f){
		System.out.println("processing ... "+ f.getAbsolutePath());
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(f));
			String line=null;
			while (null!=(line=reader.readLine())){
				//skip empty or comment line
				line = line.trim();
				if (ToolSafe.isEmpty(line)|| line.startsWith("#"))
					return ;
				
				patterns.add(line);
			}			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
}
