sort -iu job_ontology_todo.csv job_ontology.csv > temp;
path="`date '+../log/%Y/%Y-%m-%d-log-job_ontology.csv'`"
cp job_ontology.csv $path
rm job_ontology_todo.csv
mv temp job_ontology.csv
