package com.gamelaunch.frontend.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.DocumentsContract

object StorageUtils {

    /**
     * Converts a SAF tree URI returned by OpenDocumentTree into a real filesystem path.
     *
     * SAF tree doc IDs look like "primary:ROMs" (internal) or "1CE5-2B42:ROMs" (SD card).
     * The canonical filesystem equivalents are /storage/emulated/0/ROMs and /storage/1CE5-2B42/ROMs.
     */
    fun resolveTreeUriToPath(uri: Uri): String? = runCatching {
        val docId = DocumentsContract.getTreeDocumentId(uri)
        treeDocIdToPath(docId)
    }.getOrNull()

    /**
     * Normalises whatever got stored as the ROM root path.
     * Handles three broken formats we may have stored previously:
     *   - content://com.android.externalstorage.documents/tree/…   (raw SAF URI)
     *   - /tree/1CE5-2B42:ROMs                                     (uri.path of a SAF URI)
     *   - A real absolute path (nothing to do)
     */
    fun resolveStoredPath(rawPath: String): String {
        if (rawPath.startsWith("content://")) {
            return runCatching {
                val uri = Uri.parse(rawPath)
                val docId = DocumentsContract.getTreeDocumentId(uri)
                treeDocIdToPath(docId) ?: rawPath
            }.getOrDefault(rawPath)
        }
        if (rawPath.startsWith("/tree/")) {
            val docId = rawPath.removePrefix("/tree/")
            return treeDocIdToPath(docId) ?: rawPath
        }
        return rawPath
    }

    /** Lists all mounted storage volumes as (label, rootPath) pairs. */
    fun getStorageVolumes(context: Context): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val sm = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            sm.storageVolumes.forEach { vol ->
                if (!vol.isEmulated && !vol.isRemovable) {
                    // Primary internal storage
                    val path = Environment.getExternalStorageDirectory().absolutePath
                    result.add("Internal storage" to path)
                } else if (vol.isRemovable) {
                    // SD card — path only available on API 30+, fall back to /storage/<uuid>
                    val path: String? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        vol.directory?.absolutePath
                    } else {
                        vol.uuid?.let { "/storage/$it" }
                    }
                    if (path != null) {
                        result.add("SD card (${vol.uuid ?: "external"})" to path)
                    }
                }
            }
        }

        // Always ensure internal storage is in the list
        val internalPath = Environment.getExternalStorageDirectory().absolutePath
        if (result.none { it.second == internalPath }) {
            result.add(0, "Internal storage" to internalPath)
        }

        return result
    }

    private fun treeDocIdToPath(docId: String): String? {
        val colonIdx = docId.indexOf(':')
        if (colonIdx < 0) return null
        val volume = docId.substring(0, colonIdx)
        val subPath = docId.substring(colonIdx + 1)
        val base = if (volume.equals("primary", ignoreCase = true)) {
            Environment.getExternalStorageDirectory().absolutePath
        } else {
            "/storage/$volume"
        }
        return if (subPath.isEmpty()) base else "$base/$subPath"
    }
}
