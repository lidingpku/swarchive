path_ls="`date '+../data/log/%Y/'`"
path_today_disc_o="`date '+../data/log/%Y/%Y-%m-%d-discover-ontology.csv'`"
path_today_job="`date '+../data/log/%Y/%Y-%m-%d-archive-job.csv'`"
path_tomorrow_job="`date --date=tomorrow '+../data/log/%Y/%Y-%m-%d-archive-job.csv'`"
#echo $path_today_disc_o
#echo $path_today_job
#echo $path_tomorrow_job
path1="`date '+../data/log/'`"
mkdir $path1
path2="`date --date=tomorrow '+../data/log/%Y/'`"
mkdir $path2
cat $path_today_disc_o $path_today_job > temp_job
#sed -i '/^\#/d' temp_job
sed -i 's/\#.*//' temp_job
sed -i '/^file/d' temp_job
sed -i '/^$/d' temp_job
sort -iu temp_job > $path_tomorrow_job
rm temp_job
ls -lr $path_ls
