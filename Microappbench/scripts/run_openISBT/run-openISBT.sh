#!/bin/bash
address="http://localhost:80"
version="v1"
worker_port="8000"
service="orders"

for arg in "$@"
do
    case $arg in
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

java -jar jars/openISBTWorker-1.0-SNAPSHOT.jar $worker_port &
worker_pid=$!

sleep 3

mkdir results
java -jar jars/runner-1.0-SNAPSHOT-all.jar -o -r results/results-$service-$version.json -w config/workload-$service-$version.json -e http://$address -t 5 -u localhost:$worker_port

kill -KILL $worker_pid

exit 0