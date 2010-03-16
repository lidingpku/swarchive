package swarchive;

import org.junit.Test;


public class JobArchiveTest {
	
	@Test
	public void test_load(){
		String [] address = new String[]{			
			"http://github.com/lucmoreau/OpenProvenanceModel/raw/master/elmo/src/main/resources/opm.owl",
			"http://www.data.gov/data_gov_catalog.csv",
			"http://xmlns.com/foaf/0.1/",
		};

		JobArchive swa = new JobArchive(new DataConfig());
		for (int i=0; i< address.length; i++){
			String szUrl  = address[i];
			swa.archive(szUrl, false);
		}
	}
	
}
