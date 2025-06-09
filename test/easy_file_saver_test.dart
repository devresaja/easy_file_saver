import 'package:flutter_test/flutter_test.dart';
import 'package:easy_file_saver/easy_file_saver.dart';
import 'package:easy_file_saver/easy_file_saver_platform_interface.dart';
import 'package:easy_file_saver/easy_file_saver_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockEasyFileSaverPlatform
    with MockPlatformInterfaceMixin
    implements EasyFileSaverPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final EasyFileSaverPlatform initialPlatform = EasyFileSaverPlatform.instance;

  test('$MethodChannelEasyFileSaver is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelEasyFileSaver>());
  });

  test('getPlatformVersion', () async {
    EasyFileSaver easyFileSaverPlugin = EasyFileSaver();
    MockEasyFileSaverPlatform fakePlatform = MockEasyFileSaverPlatform();
    EasyFileSaverPlatform.instance = fakePlatform;

    expect(await easyFileSaverPlugin.getPlatformVersion(), '42');
  });
}
