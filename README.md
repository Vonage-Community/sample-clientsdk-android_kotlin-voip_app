# VoiceSampleApp

VoiceSampleApp is an Android application that showcases how to integrate the Vonage Client SDK for Voice into your Android app. With this app, you can make voice calls using the Vonage Voice API.

## Getting Started

1. Clone the repository
2. Install Android Studio
3. Open the project in Android Studio
4. Sync your project with Gradle
5. Build and run the app on an Android device or emulator.

**Note:** In order to build the app, the minimum Android SDK version required is `26` and the minimum version for both `com.android.application` and `com.android.library` plugins is `7.3.0`.

## Usage

Before using the app, you will need a valid Vonage API token. To add this token to the app, simply add a property named `MY_VONAGE_API_TOKEN` at the bottom of your `local.properties` file:

```
MY_VONAGE_API_TOKEN=<YOUR_API_TOKEN>
```

You can either store your default application token there or leave it empty and paste it at runtime.

To make a voice call using this app, log in, type the name of the user you want to call, or enter a phone number using the dialer. Press the call button, and the app will connect to the Vonage Voice API to initiate the call.

## License

This project is licensed under the MIT License - see the `LICENSE` file for details.

