package swarchive;


public class DataJobArchive extends DataJob{
	@Override
	public String[] getFields() {
		final String [] fields= new String[]{
				JOB_URI,
		};
		return fields;
	}
}
