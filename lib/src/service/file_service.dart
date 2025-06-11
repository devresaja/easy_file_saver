// ignore_for_file: curly_braces_in_flow_control_structures
import 'dart:io';
import 'package:easy_file_saver/easy_file_directory.dart';
import 'package:package_info_plus/package_info_plus.dart';
import 'package:path_provider/path_provider.dart';
import 'package:device_info_plus/device_info_plus.dart';
import 'package:permission_handler/permission_handler.dart';

class FileService {
  static String? _appName;
  static String? _packageName;

  static Future<String> _getAppName() async {
    if (_appName != null) return _appName!;
    final info = await PackageInfo.fromPlatform();
    _appName = info.appName;
    return _appName!;
  }

  static Future<String> _getPackageName() async {
    if (_packageName != null) return _packageName!;
    final info = await PackageInfo.fromPlatform();
    _packageName = info.packageName;
    return _packageName!;
  }

  static Future<void> requestPermission({
    required Function() onPermissionDenied,
  }) async {
    if (Platform.isAndroid) {
      final androidVersion = await DeviceInfoPlugin().androidInfo.then(
        (info) => info.version.sdkInt,
      );

      if (androidVersion <= 29) {
        if (await Permission.storage.isGranted) return;

        final status = await Permission.storage.request();

        if (status.isDenied) {
          onPermissionDenied();
        }
      }
    }
  }

  static Future<Directory> getDirectory(EasyFileDirectory location) async {
    final Directory directory;
    final appName = await _getAppName();
    final packageName = await _getPackageName();

    switch (location) {
      case EasyFileDirectory.internal:
        directory = await getApplicationDocumentsDirectory();
        break;

      case EasyFileDirectory.external:
        final androidVersion = await DeviceInfoPlugin().androidInfo.then(
          (info) => info.version.sdkInt,
        );
        if (androidVersion <= 29) {
          directory = Directory('/storage/emulated/0/$appName');
        } else {
          directory = Directory(
            '/storage/emulated/0/Android/media/$packageName',
          );
        }
        break;

      case EasyFileDirectory.download:
        directory = Directory('/storage/emulated/0/Download/$appName');
        break;

      case EasyFileDirectory.saf:
        throw UnsupportedError(
          'EasyFileDirectory.saf does not support file path access.',
        );
    }

    return directory;
  }
}
