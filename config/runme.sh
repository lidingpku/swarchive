cd ..
cd java
ant archive
ant discover
cd ..
cd data/seed
./update.sh
svn commit -m "server update" 
cd ../..
