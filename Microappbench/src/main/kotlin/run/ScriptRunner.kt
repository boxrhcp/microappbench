package run

import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class ScriptRunner {

    fun executeOpenISBT(build: Boolean, address: String, version: String, workerPort: String) {
        var buildComm: String = ""
        if (build) buildComm = "-b "
        thread (start = true, name = version){
            ("sh run-openISBT.sh $buildComm-a $address -v $version -w $workerPort").runCommand(File("../scripts/run_openISBT"))
        }
    }

    fun bootSockshop(){

    }

    private fun String.runCommand(workingDir: File) {
        ProcessBuilder(*split(" ").toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor(60, TimeUnit.MINUTES)
    }

}