package run

import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class ScriptRunner(private val verbose: Boolean) {

    fun executeOpenISBT(address: String, service: String, version: String, workerPort: String) {
        thread(start = true, name = version) {
            ("sh run-openISBT.sh -a $address -s $service -v $version -w $workerPort").runCommand(File("scripts/run_openISBT"))
        }
    }

    fun prepareOpenISBT(build: Boolean, address: String, service: String, version: String) {
        var buildComm = ""
        if (build) buildComm = "-b "
        ("sh prepare-openISBT.sh $buildComm-a $address -s $service -v $version").runCommand(File("scripts/run_openISBT"))

    }

    fun bootSUT(): String {
        val result = ("sh boot-sockshop.sh").runCommand(File("scripts"))
        return result.split("\n").last()
        //return ("sh boot-sockshop.sh").runCommand(File("scripts"))
    }

    fun executeBenchmarkRunner(ip: String, verbose: Boolean) {
        var vArg = ""
        if (verbose) vArg = "-v "
        ("java -jar build/libs/benchmarkTool-1.0-SNAPSHOT-all.jar $vArg-i $ip").runCommand(File("."))
    }

    fun executeMonitorRetriever(clean: Boolean) {
        var cleanComm = ""
        if (clean) cleanComm = "-c"
        ("java -jar build/libs/monitorRetrieverTool-1.0-SNAPSHOT-all.jar $cleanComm").runCommand(File("."))
    }

    fun executeAnalyzer() {
        ("java -jar build/libs/analyzerTool-1.0-SNAPSHOT-all.jar").runCommand(File("."))
    }


    private fun String.runCommand(workingDir: File): String {
        var redirect = ProcessBuilder.Redirect.PIPE
        if (verbose) {
            redirect = ProcessBuilder.Redirect.INHERIT
        }
        val proc = ProcessBuilder(*split(" ").toTypedArray())
            .directory(workingDir)
            .redirectOutput(redirect)
            .redirectError(redirect)
            .start()

        proc.waitFor(60, TimeUnit.MINUTES)

        return proc.inputStream.bufferedReader().readText()
    }

}