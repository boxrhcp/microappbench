 #!/bin/bash
flag_build=0
address="http://localhost:80"

for arg in "$@"
do
    case $arg in
        -b|--build)
        flag_build=1
        shift # Remove --initialize from processing
        ;;
        -a|--address)
        address="$2"
        shift # Remove argument name from processing
        shift # Remove argument value from processing
        ;;
    esac
done

if [ $flag_build -eq 1 ]; then
    echo "building openISBT"
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
    gradle build
else
    echo "ignoring openISBT build"
    cd ../../openISBT/fakerServer
fi

node server.js &
fakeServer_pid=$!

cd ../../scripts/run_openISBT
java -jar openISBTWorker-1.0-SNAPSHOT.jar 8000 &
worker_pid=$!

java -jar matchingTool-1.0-SNAPSHOT-all.jar -o -s sockshop-order.json -d experiment-order.json -m mapping-order.json

java -jar wlgenerator-1.0-SNAPSHOT-all.jar -o -m mapping-order.json -w workload-order.json

kill -KILL $fakeServer_pid

java -jar runner-1.0-SNAPSHOT-all.jar -o -r results-order.json -w workload-order.json -e $address -t 5 -u localhost:8000

kill -KILL $worker_pid