package run

import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class ScriptRunner {

    fun executeOpenISBT(address: String, version: String, workerPort: String) {
        thread(start = true, name = version) {
            ("sh run-openISBT.sh -a $address -v $version -w $workerPort").runCommand(File("scripts/run_openISBT"))
        }
    }

    fun prepareOpenISBT(build: Boolean, address: String, version: String) {
        var buildComm = ""
        if (build) buildComm = "-b "
        ("sh prepare-openISBT.sh $buildComm-a $address -v $version").runCommand(File("scripts/run_openISBT"))

    }

    fun bootSUT() {
        ("sh boot-sockshop.sh").runCommand(File("scripts"))
    }


    fun bootSockshop() {

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