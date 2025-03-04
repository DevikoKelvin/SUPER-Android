package id.erela.surveyproduct.objects

data class UserData (
    val id: Int,
    val name: String,
    val username: String,
    val phone: String,
    val photoProfile: String,
    val privilege: Int
)