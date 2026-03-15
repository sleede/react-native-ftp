# GEMINI.md - React Native FTP (rn-ftp)

## Project Overview
`rn-ftp` is a React Native library that provides a bridge for FTP (File Transfer Protocol) operations on both Android and iOS. It allows developers to connect to FTP servers, list directory contents, and download files directly within a React Native application.

## Key Technologies
- **React Native:** Bridge for cross-platform mobile development.
- **Java (Android):** Uses `org.apache.commons.net.ftp.FTPClient` for FTP operations.
- **Objective-C (iOS):** Uses a custom `FTPKit` library (built on `ftplib`) for FTP operations.
- **Apache Commons Net:** The underlying library for Android's FTP implementation.

## Project Structure
- `index.js`: The JavaScript entry point that exports the `RNFtp` native module.
- `android/`: Contains the Android native module implementation (`RNFtpModule.java`).
- `ios/`: Contains the iOS native module implementation (`RNFtp.m`, `RNFtp.h`) and the embedded `FTPKit` and `ftplib` libraries.
- `RNFtp.podspec`: The CocoaPods specification for the iOS library.
- `package.json`: Project metadata and dependencies.

## API Reference (JavaScript)

### `connect(config: Object): Promise<boolean>`
Connects to an FTP server.
- `config.hostname`: The server address (e.g., `192.168.1.1` or `ftp.example.com:21`).
- `config.username`: (Optional) Defaults to `anonymous`.
- `config.password`: (Optional) Defaults to `anonymous@`.
- `config.timeout`: (Optional) Connection timeout in milliseconds.
- `config.systemType`: (Optional, Android only) Manually specify the FTP server's system type to resolve list parsing errors (e.g., `'UNIX'`, `'VMS'`, `'WINDOWS'`). Useful if the server reports an unusual type (like `Win32NT`) but uses a different listing format.

### `list(path: string): Promise<Array<Object>>`
Lists the contents of a directory.
- `path`: The remote directory path.
- Returns an array of file/directory objects:
  - `name`: File or directory name.
  - `type`: `0` for file, `1` for directory (on Android; iOS returns objects for both).
  - `size`: File size in bytes.
  - `modifiedDate`: ISO 8601 formatted date string.

### `downloadFile(remoteFile: string, localFile: string): Promise<boolean>`
Downloads a file from the remote server to a local path.
- `remoteFile`: The path to the file on the FTP server.
- `localFile`: The local destination path on the device.

### `disconnect(): Promise<boolean>`
Closes the FTP connection.

## Building and Running

### Prerequisites
- Node.js and npm/yarn.
- Android Studio and Android SDK (for Android).
- Xcode and CocoaPods (for iOS).

### Installation
```bash
npm install rn-ftp --save
```

### Android
The library is included in the project's `android/build.gradle`. Ensure you have the `commons-net` dependency if not automatically resolved.

### iOS
Run `pod install` in your project's `ios/` directory to link the library via CocoaPods.

## Development Conventions
- **Native Bridges:** Changes to functionality must be implemented in both `RNFtpModule.java` (Android) and `RNFtp.m` (iOS) to maintain parity.
- **Error Handling:** Native methods should reject promises with an "ERROR" code and a descriptive message.
- **Threading:** 
  - Android: Native methods are executed in separate threads to avoid blocking the JS thread.
  - iOS: Native methods use a serial dispatch queue (`FNFtp`).

