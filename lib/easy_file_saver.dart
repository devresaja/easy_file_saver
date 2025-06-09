
import 'easy_file_saver_platform_interface.dart';

class EasyFileSaver {
  Future<String?> getPlatformVersion() {
    return EasyFileSaverPlatform.instance.getPlatformVersion();
  }
}
