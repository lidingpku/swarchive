package swarchive;

import java.io.File;

import sw4j.util.DataSmartMap;
import sw4j.util.Sw4jException;
import sw4j.util.ToolIO;

public class ToolMyUtil {
	public static void writeCsv(File f, DataSmartMap content) {
		if (!f.exists()){
			ToolIO.pipeStringToFile(content.toCSVheader()+"\n",f);
		}
		try {
			ToolIO.pipeStringToFile(content.toCSVrow()+"\n",f,false,true);
		} catch (Sw4jException e) {
			e.printStackTrace();
		}

	}
}