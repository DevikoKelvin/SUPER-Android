package id.erela.surveyproduct.helpers

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesHelper {
    fun getSharedPreferences(context: Context): SharedPreferences = context.getSharedPreferences(
        "SUPER", Context.MODE_PRIVATE
    )
}