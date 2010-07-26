path1="`date '+../data/log/'`"
mkdir $path1
path2="`date '+../data/log/%Y/'`"
mkdir $path2
path_job="`date '+../data/log/%Y/%Y-%m-%d-archive-job.csv'`"
cat seed/job_ontology.csv seed/job_datagov.csv > $path_job
