package id.resdev.easy_file_saver

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodChannel

class SafFileSaver {

    private var activity: Activity? = null
    private var pendingResult: MethodChannel.Result? = null

    private var _bytesToSave: ByteArray? = null
    private var _mimeTypeToSave: String? = null
    private var _fileNameToSave: String? = null

    private val REQUEST_CODE_SAVE_FILE = 43

    fun setActivity(activity: Activity?) {
        this.activity = activity
    }

    private fun clearPendingState() {
        _bytesToSave = null
        _mimeTypeToSave = null
        _fileNameToSave = null
        pendingResult = null
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode != REQUEST_CODE_SAVE_FILE) {
            return false
        }

        if (resultCode != Activity.RESULT_OK || data == null) {
            pendingResult?.error("SAF_SAVE_CANCELLED", "SAF file picking cancelled", null)
            clearPendingState()
            return true
        }

        val uri: Uri? = data.data
        if (uri == null) {
            pendingResult?.error("SAF_SAVE_FAILED", "No URI selected", null)
            clearPendingState()
            return true
        }

        if (_bytesToSave == null) {
            pendingResult?.error("SAF_SAVE_FAILED", "Data to save not found after SAF pick", null)
            clearPendingState()
            return true
        }

        try {
            activity?.contentResolver?.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(_bytesToSave)
            }
            pendingResult?.success(uri.toString())
        } catch (e: Exception) {
            Log.e("EasyFileSaver", "SAF_SAVE_FAILED: ${e.message}", e)
            pendingResult?.error("SAF_SAVE_FAILED", e.message, null)
        }

        clearPendingState()
        return true
    }

    fun saveFile(result: MethodChannel.Result, bytes: ByteArray, mimeType: String, fileName: String) {
        if (activity == null) {
            result.error("UNAVAILABLE", "Activity is not attached", null)
            return
        }

        this.pendingResult = result
        this._bytesToSave = bytes
        this._mimeTypeToSave = mimeType
        this._fileNameToSave = fileName

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        activity?.startActivityForResult(intent, REQUEST_CODE_SAVE_FILE)
    }
} 