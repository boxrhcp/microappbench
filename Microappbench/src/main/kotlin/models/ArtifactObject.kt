package models

data class ArtifactObject (val name : String,
                           val path: String,
                           val parentList: ArrayList<ArtifactObject> = ArrayList<ArtifactObject>(),
                           val childList: ArrayList<ArtifactObject> = ArrayList<ArtifactObject>(),
                           var performanceIssue: Boolean = false){


                             }