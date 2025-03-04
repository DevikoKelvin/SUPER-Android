package id.erela.surveyproduct.helpers

import android.content.Context
import id.erela.surveyproduct.objects.UserData

class UserDataHelper(private val context: Context) {
    private val keyId = "key.id"
    private val keyName = "key.name"
    private val keyUsername = "key.username"
    private val keyPhone = "key.phone"
    private val keyPhotoProfile = "key.photo_profile"
    private val keyPrivilege = "key.privilege"

    fun storeData(
        id: Int,
        name: String,
        username: String,
        phone: String?,
        photoProfile: String?,
        privilege: Int
    ) {
        SharedPreferencesHelper.getSharedPreferences(context).edit().also {
            it.apply {
                putInt(keyId, id)
                putString(keyName, name)
                putString(keyUsername, username)
                putString(keyPhone, phone)
                putString(keyPhotoProfile, photoProfile)
                putInt(keyPrivilege, privilege)
            }
        }.apply()
    }

    fun getData(): UserData {
        SharedPreferencesHelper.getSharedPreferences(context).also {
            return UserData(
                id = it.getInt(keyId, 0),
                name = it.getString(keyName, "") ?: "",
                username = it.getString(keyUsername, "") ?: "",
                phone = it.getString(keyPhone, "") ?: "",
                photoProfile = it.getString(keyPhotoProfile, "") ?: "",
                privilege = it.getInt(keyPrivilege, 0)
            )
        }
    }

    fun isUserDataStored(): Boolean {
        SharedPreferencesHelper.getSharedPreferences(context).also {
            return it.contains(keyId)
        }
    }

    fun purgeUserData() {
        SharedPreferencesHelper.getSharedPreferences(context).edit().clear().apply()
    }
}