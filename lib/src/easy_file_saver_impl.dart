import 'dart:io';
import 'package:easy_file_saver/src/easy_file_directory.dart';
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
    required EasyFileDirectory directory,
    Function()? onPermissionDenied,
  }) async {
    final filePath = await getFilePath(
      fileName: fileName,
      directory: directory,
      onPermissionDenied: onPermissionDenied,
    );

    switch (directory) {
      case EasyFileDirectory.cache:
        await _saveToAppInternalStorage(filePath, fileName, bytes);
        break;

      case EasyFileDirectory.internal:
        await _saveToAppInternalStorage(filePath, fileName, bytes);
        break;

      case EasyFileDirectory.external:
        await _invokeMethodChannel('saveFileToExternal', fileName, bytes);
        break;

      case EasyFileDirectory.download:
        await _invokeMethodChannel('saveFileToDownloads', fileName, bytes);
        break;

      case EasyFileDirectory.saf:
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
    required EasyFileDirectory directory,
    Function()? onPermissionDenied,
  }) async {
    if (directory == EasyFileDirectory.saf) return null;

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
