sort -iu job_ontology_todo.csv job_ontology.csv > temp;
rm job_ontology.csv
rm job_ontology_todo.csv
mv temp job_ontology.csv
