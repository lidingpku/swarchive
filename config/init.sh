path1="`date '+../data/log/'`"
mkdir $path1
path2="`date '+../data/log/%Y/'`"
mkdir $path2
path_job="`date '+../data/log/%Y/%Y-%m-%d-archive-job.csv'`"
cp seed/job_ontology.csv $path_job
