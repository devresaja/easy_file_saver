/// Represents the target directory where a file should be saved.
///
/// This enum provides different directory destinations:
/// - [cache]: Temporary cache direcotory.
/// - [internal]: App-specific internal directory (private, sandboxed).
/// - [external]: Public media directory via `MediaStore`, scoped to app.
/// - [download]: Public downloads directory via `MediaStore`.
/// - [saf]: Flexible user-selected directory via `Storage Access Framework (SAF)`.
///
/// Notes:
/// - [saf] does **not** support returning a file path directly.
enum EasyFileDirectory {
  /// Android: Temporary cache directory.
  cache,

  /// Android: App-specific internal directory (sandboxed, not visible to user).
  internal,

  /// Android 11+: `/storage/emulated/0/Android/media/<package>/<appName>`
  /// Android 10-: `/storage/emulated/0/<appName>`
  external,

  /// Android: `/storage/emulated/0/Download/<appName>`
  download,

  /// Android: Storage Access Framework (user-selected directory).
  saf,
}
