# Microappbench

Proof of concept. This project has 4 modules: ControllerTool, BenchmarkTool, MonitorRetrieverTool and AnalyzerTool.

## Requirements
Tool built in Kotlin. Requires Java 8, Gradle, Python 3 for the front-end UI, a Google Cloud account, Google SDK tools installed, a Google Cloud SQL instance created and Google Cloud SQL proxy.

## Compilation

To build the benchmark tool:

`gradle buildBenchmarkTool`

To build the monitoring retriever tool:

`gradle buildMonitorRetrieverTool`

To build the analyzer tool:

`gradle buildAnalyzerTool`

To build the controller tool:

`gradle buildControllerTool`

## Run

To run the complete tool we can execute the controller tool:

`java -jar build/libs/controllerTool-1.0-SNAPSHOT-all.jar [-c] [-v]`

where -c wipes the db data and -v enables verbose mode.

---

In case the modules need to be run separately, these can be executed.

- `java -jar build/libs/benchmarkTool-1.0-SNAPSHOT-all.jar [-b] [-v]` where -b builds the openISBT tool (needs to be enabled at least once), and -v enables verbose mode.

- `java -jar build/libs/monitorRetrieverTool-1.0-SNAPSHOT-all.jar [-c]` where -c wipes data from the existing db.

- `java -jar build/libs/analyzerTool-1.0-SNAPSHOT-all.jar [-o FILENAME] [-v] [-f]` where -o defines the name of the results file, -v enables verbose mode and -f enables front end UI graphs.

## Configuration

Microappbench configuration can be seen under config.json.
OpenAPI specification can be seen under scripts/run_openISBT/sockshop-orders-template.json.
OpenISBT configuration can be seen under scripts/run_openISBT/config/experiment-orders.json.


