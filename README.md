# Typing Overlay

A small Android social-comfort experiment inspired by the communication anxiety many young people feel in modern messaging apps.

Sometimes, when a message is not answered immediately, people begin to overthink: *Did I say something wrong? Are they ignoring me? Should I send another message?* This project explores a simple emotional-design idea: adding a familiar "typing..." style overlay to a chat screen, so the waiting moment feels less empty and less stressful.

The app displays a floating typing indicator bubble on top of other apps. It does not read messages, modify any chat app, or detect whether another person is actually typing. It is a lightweight, user-controlled visual overlay meant for personal comfort, UI experimentation, and discussion around digital social anxiety.

## Core Idea

The main goal of this project is not just to build a floating Android widget.

The deeper idea is to respond to a common emotional experience in online communication: the anxiety caused by delayed replies. By placing a small "the other person is typing..." style indicator on the screen, the interface creates a sense of presence and expectation, making the waiting experience feel softer and less uncertain.

This project treats a tiny UI element as an emotional buffer between people and the pressure of instant messaging.

## Features

- Floating typing indicator overlay
- Three-dot animated typing effect
- Draggable overlay bubble
- Start and stop controls from the main screen
- Foreground service support to keep the overlay running
- Runtime overlay permission flow
- Android 13+ notification permission handling
- Android 14+ `specialUse` foreground service declaration
- Minimal programmatic Android UI with no extra layout files

## Screens / Behavior

The app opens with two simple controls:

- **Show Overlay**: requests the "Display over other apps" permission if needed, then starts the floating bubble.
- **Hide Overlay**: stops the overlay service and removes the bubble.

Once enabled, the overlay appears on top of the current screen. Users can drag it to a different position. A persistent foreground notification is shown while the overlay is active, and tapping the notification stops the overlay.

## Tech Stack

- Kotlin
- Android SDK
- Programmatic Android UI
- `WindowManager` overlay window
- Foreground `Service`
- `ObjectAnimator` for dot animation

## Project Structure

```text
app/src/main/
├── AndroidManifest.xml
└── java/com/example/typingoverlay/
    ├── MainActivity.kt
    └── TypingOverlayService.kt
```

## Main Components

### `MainActivity.kt`

`MainActivity` builds a simple interface in code. It checks whether the app has permission to draw over other apps, opens the Android overlay permission settings page when needed, requests notification permission on Android 13+, and starts or stops `TypingOverlayService`.

### `TypingOverlayService.kt`

`TypingOverlayService` creates the floating overlay using `WindowManager`. It builds the rounded typing bubble, animates the three dots with staggered movement and opacity changes, allows the bubble to be dragged, and runs as a foreground service with a persistent notification.

### `AndroidManifest.xml`

The manifest declares the permissions required for overlay windows, foreground services, special-use foreground services, and Android 13+ notifications. It also registers the main launcher activity and the overlay service.

## Permissions

This app uses the following Android permissions:

```xml
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

These permissions are required because the app draws a floating view above other applications and keeps that view active through a foreground service.

## How to Run

1. Open the project in Android Studio.
2. Make sure the package name is consistent with:

   ```kotlin
   package com.example.typingoverlay
   ```

3. Build and install the app on an Android device or emulator.
4. Open the app.
5. Tap **Show Overlay**.
6. Grant the "Display over other apps" permission when prompted.
7. Return to the app and tap **Show Overlay** again.
8. Drag the bubble anywhere on the screen.
9. Tap **Hide Overlay** or tap the foreground notification to stop it.

## Android Version Notes

- Android 6.0+ requires explicit overlay permission for `SYSTEM_ALERT_WINDOW`.
- Android 8.0+ requires foreground services to use notification channels.
- Android 13+ requires the `POST_NOTIFICATIONS` runtime permission.
- Android 14+ requires a declared foreground service type. This project uses `specialUse` with a subtype description in the manifest.

## Design Notes

The overlay is intentionally small, soft, and familiar. The white rounded bubble and animated gray dots borrow from common chat-interface patterns, but the project does not attempt to access private conversations or imitate real message states.

The design is meant to raise a question:

> Can a tiny interface cue reduce the emotional pressure created by waiting for a reply?

## Limitations

- The project currently focuses on the core overlay implementation.
- The UI is created programmatically rather than with XML layouts or Jetpack Compose.
- The overlay is a visual indicator only.
- It does not read messages, detect actual typing status, send messages, or interact with third-party chat apps.
- It should not be presented as a real typing status from another person.

## Possible Improvements

- Add customization options for bubble size, color, position, and animation speed.
- Save the last overlay position with `SharedPreferences`.
- Add optional text beside the dots, such as "Typing...".
- Add a Compose-based settings screen.
- Add screenshots or a demo GIF to the README.
- Add a short research note about social anxiety, delayed replies, and interface design.
- Package the project as a complete Gradle Android Studio project.

## Ethical Use

This project should be used as a personal comfort tool, a design prototype, or an educational Android overlay experiment.

It should not be used to deceive others, impersonate someone, fake another person's activity, or manipulate a conversation. The overlay is a personal visual layer controlled by the user, not a real status indicator from any messaging platform.

## License

No license has been selected yet. Add a license such as MIT if you want others to use, modify, or distribute this project.
