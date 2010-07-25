date
cd ..
cd java
path_log_archive="`date '+../temp/log-%Y-%m-%d-archive'`"
echo run $path_log_archive
ant archive -l $path_log_archive
path_log_discover="`date '+../temp/log-%Y-%m-%d-discover'`"
echo run $path_log_discover
ant discover -l $path_log_discover
cd ..
cd config
./update.sh
date
