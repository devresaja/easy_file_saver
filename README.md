# easy_file_saver

A Flutter plugin for easily saving files to various directories. This plugin handles permissions and different storage locations, including internal storage, external storage, downloads, and the Storage Access Framework (SAF).

## Features

-   Save `Uint8List` bytes to a file.
-   Supports multiple storage directories
    #### Android:
    -   `EasyFileDirectory.internal`: App-specific internal storage (private to the app).
    -   `EasyFileDirectory.external`: Public media storage (scoped to app).
    -   `EasyFileDirectory.download`: Public downloads folder.
    -   `EasyFileDirectory.saf`: User-selected directory via Storage Access Framework (SAF).

    #### IOS:
    - Not supported yet.

## Installation

Add `easy_file_saver` to your `pubspec.yaml` file:

```sh
flutter pub add easy_file_saver
```

## Configuration

### Android  
For Android versions 10 (SDK 29) and below, you need to add the `WRITE_EXTERNAL_STORAGE` permission to your `AndroidManifest.xml`.

Add the following to your `android/app/src/main/AndroidManifest.xml` inside the `<manifest>` tag:

```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="29" />
```

## Usage

### Import the package:

```dart
import 'package:easy_file_saver/easy_file_saver.dart';
```

### Saving a file

You can save a file using the `EasyFileSaver.save` method. This method requires the `fileName`, `bytes` (as `Uint8List`), and the `directory` (`EasyFileDirectory` enum) where the file should be saved.

```dart
import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:easy_file_saver/easy_file_saver.dart';

// Example of saving an image file
Future<void> saveImageFile(BuildContext context, Uint8List imageBytes) async {
  String fileName = "my_image.png"; // Include extension
  try {
    String? filePath = await EasyFileSaver.save(
      fileName: fileName,
      bytes: imageBytes,
      directory: EasyFileDirectory.external, // Example: save to external media
      onPermissionDenied: () {
        // Handle permission denied case
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Storage permission denied!')),
        );
      },
    );
    if (filePath != null) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Image saved to: $filePath')),
      );
    } else {
      // For SAF, filePath will be null as it doesn't return a direct path
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('File saved successfully (path not returned for SAF)')),
      );
    }
  } catch (e) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Failed to save image: $e')),
    );
  }
}

