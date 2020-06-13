cd ../../openISBT/openISBTBackend
gradle buildMatchingTool
gradle buildWorkloadGenerator
gradle buildRunTool
#gradle buildBoxPlotTool

cp build/libs/matchingTool-1.0-SNAPSHOT-all.jar ../../scripts/run_openISBT/
cp build/libs/wlgenerator-1.0-SNAPSHOT-all.jar ../../scripts/run_openISBT/
cp build/libs/runner-1.0-SNAPSHOT-all.jar ../../scripts/run_openISBT/

cd ../openISBTWorker
gradle clean build
cp build/libs/openISBTWorker-1.0-SNAPSHOT.jar ../../scripts/run_openISBT/

cd ../fakerServer
node server.js &
fakeServer_pid=$!

cd ../../scripts/run_openISBT
java -jar openISBTWorker-1.0-SNAPSHOT.jar 8000 &
worker_pid=$!

java -jar matchingTool-1.0-SNAPSHOT-all.jar -o -s sockshop-order.json -d experiment-order.json -m mapping-order.json

java -jar wlgenerator-1.0-SNAPSHOT-all.jar -o -m mapping-order.json -w workload-order.json

kill -KILL $fakeServer_pid

java -jar runner-1.0-SNAPSHOT-all.jar -o -r results-order.json -w workload-order.json -e http://35.205.233.151 -t 5 -u localhost:8000

kill -KILL $worker_pid