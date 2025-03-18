package id.erela.surveyproduct.helpers

import android.content.Context
import androidx.core.content.edit
import id.erela.surveyproduct.objects.UsersSuper

class UserDataHelper(private val context: Context) {
    private val keyId = "key.id"
    private val keyFullName = "key.full_name"
    private val keyEmail = "key.email"
    private val keyUsername = "key.username"
    private val keyPhotoProfile = "key.photo_profile"
    private val keyCode = "key.code"
    private val keyTypeId = "key.type_id"
    private val keyType = "key.type"
    private val keyTeamId = "key.team_id"
    private val keyTeam = "key.team"
    private val keyBranchId = "key.branch_id"
    private val keyBranch = "key.branch"
    private val keyCreatedAt = "key.created_at"
    private val keyUpdatedAt = "key.updated_at"

    fun storeData(
        id: Int?,
        fullName: String?,
        email: String?,
        username: String?,
        photoProfile: String?,
        code: String?,
        typeId: Int?,
        type: String?,
        teamId: Int?,
        team: String?,
        branchId: Int?,
        branch: String?,
        createdAt: String?,
        updatedAt: String?
    ) {
        SharedPreferencesHelper.getSharedPreferences(context).edit {
            also {
                it.apply {
                    putInt(keyId, id ?: 0)
                    putString(keyFullName, fullName)
                    putString(keyEmail, email)
                    putString(keyUsername, username)
                    putString(keyPhotoProfile, photoProfile)
                    putString(keyCode, code)
                    putInt(keyTypeId, typeId ?: 0)
                    putString(keyType, type)
                    putInt(keyTeamId, teamId ?: 0)
                    putString(keyTeam, team)
                    putInt(keyBranchId, branchId ?: 0)
                    putString(keyBranch, branch)
                    putString(keyCreatedAt, createdAt)
                    putString(keyUpdatedAt, updatedAt)
                }
            }
        }
    }

    fun getData(): UsersSuper {
        SharedPreferencesHelper.getSharedPreferences(context).also {
            with(it) {
                return UsersSuper(
                    getInt(keyTypeId, 0),
                    getString(keyType, ""),
                    getString(keyUsername, ""),
                    getString(keyPhotoProfile, ""),
                    getString(keyCode, ""),
                    getString(keyCreatedAt, ""),
                    getString(keyTeam, ""),
                    getInt(keyBranchId, 0),
                    getInt(keyTeamId, 0),
                    getString(keyUpdatedAt, ""),
                    getString(keyEmail, ""),
                    getString(keyFullName, ""),
                    getInt(keyId, 0),
                    getString(keyBranch, "")
                )
            }
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