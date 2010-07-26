package swarchive;


public class DataJobDiscover extends DataJob{
	
	@Override
	public String[] getFields() {
		final String [] fields= new String[]{
				JOB_CHANGE_CONTENT_CACHED,
				JOB_CHANGE_CONTENT_CHANGED,
				JOB_CHANGE_ONLINE_BEFORE,
				JOB_CHANGE_ONLINE_NOW,
				JOB_CHANGE_TYPE,
				
				JOB_CNT_LENGTH ,
				JOB_CNT_TRIPLE ,
				JOB_REQUIRERDF ,
				JOB_SHA1SUM ,
//				JOB_TS_HISTORY ,
				JOB_TS_JOB ,
				JOB_TS_MODIFIED ,
				JOB_URI,
				JOB_URL,
		};
		return fields;
	}
	
	public boolean is_content_changed(){
		String temp = this.getAsString(JOB_CHANGE_CONTENT_CHANGED);
		return temp.equals("true");
	}

}
