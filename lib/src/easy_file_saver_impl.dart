import 'dart:io';
import 'package:easy_file_saver/src/easy_file_save_directory.dart';
import 'package:easy_file_saver/src/service/file_service.dart';
import 'package:flutter/services.dart';
import 'package:mime/mime.dart' show lookupMimeType;

class EasyFileSaver {
  static const _channel = MethodChannel('easy_file_saver');

  /// Saves a file to the specified directory with the given filename.
  ///
  /// Returns the expected file path if applicable, otherwise null.
  ///
  /// Notes:
  /// - `fileName` must include the file extension (e.g., "document.pdf").
  static Future<String?> save({
    required String fileName,
    required Uint8List bytes,
    required EasyFileSaveDirectory directory,
    Function()? onPermissionDenied,
  }) async {
    final filePath = await getFilePath(
      fileName: fileName,
      directory: directory,
      onPermissionDenied: onPermissionDenied,
    );

    switch (directory) {
      case EasyFileSaveDirectory.cache:
        await _saveToAppInternalStorage(filePath, fileName, bytes);
        break;

      case EasyFileSaveDirectory.internal:
        await _saveToAppInternalStorage(filePath, fileName, bytes);
        break;

      case EasyFileSaveDirectory.external:
        await _invokeMethodChannel('saveFileToExternal', fileName, bytes);
        break;

      case EasyFileSaveDirectory.download:
        await _invokeMethodChannel('saveFileToDownloads', fileName, bytes);
        break;

      case EasyFileSaveDirectory.saf:
        await _invokeMethodChannel('saveFileToSaf', fileName, bytes);
        break;
    }

    return filePath;
  }

  /// Retrieves the file path for the specified file name and directory.
  ///
  /// Returns the expected file path if applicable, otherwise null.
  ///
  /// Notes:
  /// - `fileName` must include the file extension (e.g., "document.pdf").
  static Future<String?> getFilePath({
    required String fileName,
    required EasyFileSaveDirectory directory,
    Function()? onPermissionDenied,
  }) async {
    if (directory == EasyFileSaveDirectory.saf) return null;

    await FileService.requestPermission(
      onPermissionDenied: () {
        onPermissionDenied?.call();
        return;
      },
    );

    final dir = await FileService.getDirectory(directory);
    return '${dir.path}/$fileName';
  }

  static Future<void> _saveToAppInternalStorage(
    String? filePath,
    String fileName,
    Uint8List bytes,
  ) async {
    if (filePath == null) {
      throw Exception('Internal directory file path is null');
    }

    final file = File(filePath);
    await file.create(recursive: true);
    await file.writeAsBytes(bytes);
  }

  static Future<void> _invokeMethodChannel(
    String method,
    String fileName,
    Uint8List bytes,
  ) async {
    final mimeType = lookupMimeType(fileName);
    final result = await _channel.invokeMethod<String>(method, {
      'fileName': fileName,
      'mimeType': mimeType,
      'bytes': bytes,
    });

    if (result == null) throw Exception('Failed to save file using $method');
  }
}
