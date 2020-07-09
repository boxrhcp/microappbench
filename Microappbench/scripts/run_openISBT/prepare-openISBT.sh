#!/bin/bash
flag_build=0
address="http://localhost:80"
version="v1"
service="order"

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
        -s|--service)
        service="$2"
        shift # Remove argument name from processing
        shift # Remove argument value from processing
        ;;
        -v|--version)
        version="$2"
        shift # Remove argument name from processing
        shift # Remove argument value from processing
        ;;
    esac
done

if [ $flag_build -eq 1 ]; then
    echo "building openISBT"
    cd ../../../openISBT/openISBTBackend
    gradle buildMatchingTool
    gradle buildWorkloadGenerator
    gradle buildRunTool
    #gradle buildBoxPlotTool

    mkdir ../../Microappbench/scripts/run_openISBT/jars
    cp build/libs/matchingTool-1.0-SNAPSHOT-all.jar ../../Microappbench/scripts/run_openISBT/jars/
    cp build/libs/wlgenerator-1.0-SNAPSHOT-all.jar ../../Microappbench/scripts/run_openISBT/jars/
    cp build/libs/runner-1.0-SNAPSHOT-all.jar ../../Microappbench/scripts/run_openISBT/jars/

    cd ../openISBTWorker
    gradle clean build
    cp build/libs/openISBTWorker-1.0-SNAPSHOT.jar ../../Microappbench/scripts/run_openISBT/jars/

    cd ../fakerServer
    npm init -y
    npm install json-schema-faker faker --save
else
    echo "ignoring openISBT build"
    cd ../../../openISBT/fakerServer
fi

#do it in out of the script or check if already running
node server.js 9080 &
fakeServer_pid=$!

cd ../../Microappbench/scripts/run_openISBT

cp sockshop-$service-template.json config/sockshop-$service-$version.json
sed -i '' -e "s/ip.to.benchmark/$address/g" config/sockshop-$service-$version.json
sed -i '' -e "s/version.to.benchmark/$version/g" config/sockshop-$service-$version.json

java -jar jars/matchingTool-1.0-SNAPSHOT-all.jar -o -s config/sockshop-$service-$version.json -d config/experiment-$service.json -m config/mapping-$service-$version.json

java -jar jars/wlgenerator-1.0-SNAPSHOT-all.jar -o -m config/mapping-$service-$version.json -w config/workload-$service-$version.json

kill -KILL $fakeServer_pid