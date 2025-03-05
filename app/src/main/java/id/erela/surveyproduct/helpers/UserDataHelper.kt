package id.erela.surveyproduct.helpers

import android.content.Context
import androidx.core.content.edit
import id.erela.surveyproduct.objects.Users

class UserDataHelper(private val context: Context) {
    private val keyId = "key.id"
    private val keyFullName = "key.full_name"
    private val keyEmail = "key.email"
    private val keyUsername = "key.username"
    private val keyCode = "key.code"
    private val keyType = "key.type"
    private val keyTeam = "key.team"
    private val keyBranch = "key.branch"

    fun storeData(
        id: Int,
        fullName: String?,
        email: String?,
        username: String?,
        code: String?,
        type: String?,
        team: String?,
        branch: String?
    ) {
        SharedPreferencesHelper.getSharedPreferences(context).edit {
            also {
                it.apply {
                    putInt(keyId, id)
                    putString(keyFullName, fullName)
                    putString(keyEmail, email)
                    putString(keyUsername, username)
                    putString(keyCode, code)
                    putString(keyType, type)
                    putString(keyTeam, team)
                    putString(keyBranch, branch)
                }
            }
        }
    }

    fun getData(): Users {
        SharedPreferencesHelper.getSharedPreferences(context).also {
            return Users(
                it.getString(keyTeam, ""),
                it.getString(keyType, ""),
                it.getInt(keyId, 0),
                it.getString(keyFullName, ""),
                it.getString(keyEmail, ""),
                it.getString(keyCode, ""),
                it.getString(keyBranch, ""),
                it.getString(keyUsername, "")
            )
        }
    }

    fun isUserDataStored(): Boolean {
        SharedPreferencesHelper.getSharedPreferences(context).also {
            return it.contains(keyId)
        }
    }

    fun purgeUserData() {
        SharedPreferencesHelper.getSharedPreferences(context).edit { clear() }
    }
}