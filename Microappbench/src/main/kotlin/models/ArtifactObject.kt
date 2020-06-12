package models

data class ArtifactObject (val name : String,
                           val path: String,
                           val parentList: ArrayList<ArtifactObject> = ArrayList(),
                           val childList: ArrayList<ArtifactObject> = ArrayList(),
                           var performanceIssue: Boolean = false){


                             }