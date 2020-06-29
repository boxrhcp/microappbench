 #!/bin/bash
address="http://localhost:80"
version="v1"
worker_port="8000"
service="order"

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

java -jar openISBTWorker-1.0-SNAPSHOT.jar $worker_port &
worker_pid=$!

sleep 3

java -jar runner-1.0-SNAPSHOT-all.jar -o -r results-$service-$version.json -w workload-$service-$version.json -e http://$address -t 5 -u localhost:$worker_port

kill -KILL $worker_pid