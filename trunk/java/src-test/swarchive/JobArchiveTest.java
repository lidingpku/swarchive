package swarchive;

import org.junit.Test;


public class JobArchiveTest {
	
	@Test
	public void test_hash(){
		String url ="http://www.w3.org/2000/01/rdf-schema";
		System.out.println(DataLodUri.hashUrl(url));
	}
	
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
