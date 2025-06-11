package id.resdev.easy_file_saver

import android.app.Activity
import android.content.Context
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.Result
import id.resdev.easy_file_saver.MediaStoreSaver
import java.io.File
import android.util.Log
import android.content.Intent
import android.net.Uri


class EasyFileSaverPlugin : FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware {
  private lateinit var channel: MethodChannel
  private lateinit var context: Context
  private var safFileSaver: SafFileSaver = SafFileSaver()

  override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    context = binding.applicationContext
    channel = MethodChannel(binding.binaryMessenger, "easy_file_saver")
    channel.setMethodCallHandler(this)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    safFileSaver.setActivity(binding.activity)
    binding.addActivityResultListener(safFileSaver::handleActivityResult)
  }

  override fun onDetachedFromActivityForConfigChanges() {
    safFileSaver.setActivity(null)
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    safFileSaver.setActivity(binding.activity)
  }

  override fun onDetachedFromActivity() {
    safFileSaver.setActivity(null)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    when (call.method) {
      "saveFileToExternal" -> {
          val bytes = call.argument<ByteArray>("bytes")
          val mimeType = call.argument<String>("mimeType")
          val fileName = call.argument<String>("fileName")

          if (bytes == null || mimeType == null || fileName == null) {
              result.error("INVALID_ARGUMENTS", "Missing required arguments", null)
              return
          }

          try {
              val uri = MediaStoreSaver.saveToExternal(context, fileName, mimeType, bytes)
              result.success(uri)
          } catch (e: Exception) {
              Log.e("EasyFileSaver", "SAVE_FAILED: ${e.message}", e)

              result.error("SAVE_FAILED", e.message, null)
          }
      }

      "saveFileToDownloads" -> {
          val bytes = call.argument<ByteArray>("bytes")
          val mimeType = call.argument<String>("mimeType")
          val fileName = call.argument<String>("fileName")

          if (bytes == null || mimeType == null || fileName == null) {
              result.error("INVALID_ARGUMENTS", "Missing required arguments", null)
              return
          }

          try {
              val uri = MediaStoreSaver.saveToDownloads(context, fileName, mimeType, bytes)
              result.success(uri)
          } catch (e: Exception) {
              Log.e("EasyFileSaver", "SAVE_FAILED: ${e.message}", e)

              result.error("SAVE_FAILED", e.message, null)
          }
      }

      "saveFileToSaf" -> {
          val bytes = call.argument<ByteArray>("bytes")
          val mimeType = call.argument<String>("mimeType")
          val fileName = call.argument<String>("fileName")

          if (bytes == null || mimeType == null || fileName == null) {
              result.error("INVALID_ARGUMENTS", "Missing required arguments", null)
              return
          }
          safFileSaver.saveFile(result, bytes, mimeType, fileName)
      }
      else -> result.notImplemented()
    }
  }
}