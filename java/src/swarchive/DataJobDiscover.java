package swarchive;


public class DataJobDiscover extends DataJob{
	
	@Override
	public String[] getFields() {
		final String [] fields= new String[]{
				JOB_CNT_LENGTH ,
				JOB_CNT_TRIPLE ,
				JOB_DUPLICATED ,
				JOB_REQUIRERDF ,
				JOB_SHA1SUM ,
				JOB_TS_HISTORY ,
				JOB_TS_JOB ,
				JOB_TS_MODIFIED ,
				JOB_URI,
				JOB_URL,
		};
		return fields;
	}
	

}
