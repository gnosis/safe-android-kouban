package io.gnosis.kouban.core.di

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri
import io.gnosis.kouban.core.BuildConfig
import io.gnosis.kouban.data.di.DataContentProvider

abstract class CoreContentProvider : ContentProvider() {

    override fun attachInfo(context: Context?, providerInfo: ProviderInfo?) {
        if (providerInfo == null) {
            throw NullPointerException("YourLibraryInitProvider ProviderInfo cannot be null.")
        }
        // So if the authorities equal the library internal ones, the developer forgot to set his applicationId
        if ("${BuildConfig.LIBRARY_PACKAGE_NAME}.${DataContentProvider::class.java.canonicalName}" == providerInfo.authority) {
            throw IllegalStateException(
                "Incorrect provider authority in manifest. Most likely due to a "
                        + "missing applicationId variable in application\'s build.gradle."
            )
        }
        super.attachInfo(context, providerInfo)
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        return null
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun getType(uri: Uri): String? {
        return null
    }
}
