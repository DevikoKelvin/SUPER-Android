package id.erela.surveyproduct.helpers

import com.google.firebase.crashlytics.FirebaseCrashlytics

object Generic {
    fun crashReport(throwable: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }
}