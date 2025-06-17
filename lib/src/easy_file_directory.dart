/// Represents the target directory where a file should be saved.
///
/// This enum provides different storage destinations based on the Android platform:
/// - [internal]: App-specific internal storage (private, sandboxed).
/// - [external]: Public media storage using `MediaStore`, scoped to app.
/// - [download]: Public downloads folder using `MediaStore`.
/// - [saf]: Flexible user-selected directory via Storage Access Framework (SAF).
///
/// Notes:
/// - Only [internal] guarantees a returnable file path.
/// - [saf]` does **not** support returning file path directly.
enum EasyFileDirectory {
  /// Android: App-specific internal storage (sandboxed, not visible to user).
  internal,

  /// Android 11 and above: `/storage/emulated/0/Android/media/<package>/<appName>`
  /// Android 10 and below: `/storage/emulated/0/<appName>`
  external,

  /// Android: `/storage/emulated/0/Download/<appName>`
  download,

  /// Android: Storage Access Framework (flexible user-picked directory).
  saf,
}
