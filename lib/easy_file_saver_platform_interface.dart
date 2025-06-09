import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'easy_file_saver_method_channel.dart';

abstract class EasyFileSaverPlatform extends PlatformInterface {
  /// Constructs a EasyFileSaverPlatform.
  EasyFileSaverPlatform() : super(token: _token);

  static final Object _token = Object();

  static EasyFileSaverPlatform _instance = MethodChannelEasyFileSaver();

  /// The default instance of [EasyFileSaverPlatform] to use.
  ///
  /// Defaults to [MethodChannelEasyFileSaver].
  static EasyFileSaverPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [EasyFileSaverPlatform] when
  /// they register themselves.
  static set instance(EasyFileSaverPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
