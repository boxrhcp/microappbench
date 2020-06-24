 #!/bin/bash
flag_build=0
address="http://localhost:80"
version="v1"
worker_port="8000"
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
        -w|--worker)
        worker_port="$2"
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
    npm init -y
    npm install json-schema-faker faker --save
else
    echo "ignoring openISBT build"
    cd ../../openISBT/fakerServer
fi

#do it in out of the script or check if already running
node server.js 9080 &
fakeServer_pid=$!

cd ../../scripts/run_openISBT

cp ../sockshop-$service-template.json sockshop-$service-$version.json
sed -i '' -e "s/ip.to.benchmark/$address/g" sockshop-$service-$version.json
sed -i '' -e "s/version.to.benchmark/$version/g" sockshop-$service-$version.json

java -jar openISBTWorker-1.0-SNAPSHOT.jar $worker_port &
worker_pid=$!

java -jar matchingTool-1.0-SNAPSHOT-all.jar -o -s sockshop-$service-$version.json -d experiment-$service.json -m mapping-$service-$version.json

java -jar wlgenerator-1.0-SNAPSHOT-all.jar -o -m mapping-$service-$version.json -w workload-$service-$version.json

kill -KILL $fakeServer_pid

java -jar runner-1.0-SNAPSHOT-all.jar -o -r results-$service-$version.json -w workload-$service-$version.json -e http://$address -t 5 -u localhost:$worker_port

kill -KILL $worker_pid