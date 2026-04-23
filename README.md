# PopOps

<p align="center">
  <a href="https://jitpack.io/#cocodestudio/PopOps"><img src="https://jitpack.io/v/cocodestudio/PopOps.svg" alt="JitPack" /></a>
  <a href="https://android-arsenal.com/api?level=26"><img src="https://img.shields.io/badge/API-26%2B-brightgreen.svg" alt="API Level" /></a>
  <a href="https://github.com/cocodestudio/PoOps/blob/main/LICENSE"><img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License" /></a>
  <a href="https://github.com/cocodestudio/PopOps/releases"><img src="https://img.shields.io/github/v/release/cocodestudio/PoOps" alt="Latest Release" /></a>
  <img src="https://img.shields.io/badge/Language-Java-orange.svg" alt="Java" />
</p>

PopOps is a powerful, remote in-app messaging and popup management SDK designed for Android developers. It allows you to communicate with your users in real-time by triggering targeted messages and popups remotely without requiring app updates.

## Features

- **Remote Messaging:** Deploy and manage in-app messages from a centralized dashboard.
- **Targeted Delivery:** Reach specific user segments using topic-based and version-based targeting.
- **Flexible Management:** Full control over popup lifecycle and presentation.
- **Lightweight Integration:** Designed for minimal impact on app performance.

## Installation

Add the following dependency to your `build.gradle` file:

```gradle
dependencies {
    implementation "com.github.cocodestudio:PopOps:1.0.0"
}
```

Add JitPack to your root `build.gradle`:
```gradle
allprojects {
    repositories {
        // your existing repositories ...
        maven { url 'https://jitpack.io' }
    }
}
```
If you are using `settings.gradle` (newer projects):
```gradle
dependencyResolutionManagement {
		repositories {
			// your existing repositories ...
			maven { url 'https://jitpack.io' }
		}
	}
```

## Getting Started

### Initialization
Initialize the SDK in your `Activity` class using your unique `Project ID`:
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Initialize PopOps with your Project ID
    PopOps.init(this, "YOUR_PROJECT_ID");
    
    // Optional: Subscribe to a specific topic to receive targeted messages
    PopOps.subscribeToTopic("any");
}
```

### Clean Up
To ensure proper resource management, shut down the SDK when the activity is destroyed:
```java
@Override
protected void onDestroy() {
    PopOps.shutdown();
    super.onDestroy();
}
```

## Advanced Usage
### Topic-Based Targeting
You can segment your audience by subscribing them to specific interest groups or topics. Use ```PopOps.subscribeToTopic(String topic)``` to enable this.
### Version-Based Targeting
You can segment your audience by targeting specific version of your app.

## Thing to Remember
Add internet permission to your manifest : `<uses-permission android:name="android.permission.INTERNET"/>`