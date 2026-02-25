[![Development branch](https://github.com/NiallScott/MyBusEdinburgh/actions/workflows/push-and-pull-request.yml/badge.svg?branch=main)](https://github.com/NiallScott/MyBusEdinburgh/actions/workflows/push-and-pull-request.yml)

# My Bus Edinburgh

This is the open source project for the "My Bus Edinburgh" Android application.

My Bus Edinburgh is an application for the Android platform of mobile devices. It allows users to
check the next departures of bus services at most bus stops in the Edinburgh and Lothian regions.
Users can save and maintain a list of favourite stops, view bus stops on a Google Maps view,
set alerts and show the nearest stops to their location.

As this is the source code hosting service for this project, these pages most likely will not
interest you unless you're a developer. To find out more about the application, please visit
[the website](https://www.rivernile.org.uk/bustracker/) or find out the latest news by visiting the
[Bluesky feed](https://bsky.app/profile/mybusedinburgh.bsky.social).

This project utilises the Edinburgh Bus Tracker service provided by the City of Edinburgh Council,
found at https://www.edinburghtraveltracker.com/. This project started its life as the dissertation
project for the author's BSc (Hons) Computer Science final year dissertation at Heriot-Watt
University, Edinburgh, in 2009.

## Obtaining

The application is available to download in the Google Play Store under the name "My Bus Edinburgh".
The package name is `uk.org.rivernile.edinburghbustracker.android`. Here is a direct link to the app
in the Google Play Store:
https://play.google.com/store/apps/details?id=uk.org.rivernile.edinburghbustracker.android

## Latest Version

The latest stable version of the application is 3.4.

The minimum supported version of Android is Android 9.0 Pie (API level 28).

## Developers

Developers are welcome to clone or fork this repository. The `main` branch contains the
bleeding-edge code of the development workstream. Releases are tagged and can be found
[here](https://github.com/NiallScott/MyBusEdinburgh/releases).

### Building

This project is a standard Android project, not requiring anything special to build it. The project
will use either the latest stable or at least a very recent stable version of the Android tooling at
any point in time. You should be able to open and build the project on any up-to-date version of
Android Studio.

To see an example of how the project is built, refer to the GitHub Actions workflow file for the
latest guidance: [push-and-pull-request.yml](./.github/workflows/push-and-pull-request.yml).

#### google-services.json

The app will not build unless include a `google-services.json` file for the variant you wish to
build. This exists so that the app can report crashes back to Firebase Crashlytics. Follow the
instructions [here](https://firebase.google.com/docs/android/setup) to include the file in your
project.

Alternatively, disable the Google Services Gradle plugin for this project and remove all usages of
Crashlytics.

#### Required properties

You need to specify some properties to Gradle for the project to build. This can either be in your
`~/.gradle/gradle.properties` file or passed to Gradle as command line parameters. Never check these
properties in to source control.

This is the list of required properties for a debug build with their type;

- `mybus.edinburgh.apiKey` - String. This is the API key used to talk to the backend server for the
  private API. API keys won't be handed out for this, so either supply a dummy key which won't work
  against the real service, or replicate the service yourself.
- `mybus.edinburgh.bustracker.apiKey` - String. This is the API key to talk to the Edinburgh bus
  tracker service. Keys can be obtained from https://www.edinburghtraveltracker.com/.
- `mybus.edinburgh.debug.mapsKey` - String. This is the Google Maps for Android SDK API key. See
  https://developers.google.com/maps/documentation/android-sdk/get-api-key.
- `mybus.keystore.debug.file` - String. A file path pointing towards the keystore file to sign the
  app in debug mode.
- `mybus.keystore.debug.storePassword` - String. The keystore password.
- `mybus.keystore.debug.keyAlias` - String. The keystore key alias.
- `mybus.keystore.debug.keyPassword` - String. The keystore key password.

#### Build

Once your `google-services.json` file(s) are in place and the above properties have been defined,
you can now build the app. This can either be done within Android Studio or on the command-line;

```shell
./gradlew assembleDebug
```

### Contribution policy

This project is the author's main portfolio project. As such, external contributions will normally
be rejected. This project is open source in the sense that development is performed in the open.

### Security policy

The security policy can be found [here](SECURITY.md).

## Donations
The application is made available free of charge to everyone and the source code is freely
available. Of course, as Android runs on hundreds of different devices then lots of testing needs to
be done. It is impossible to test every device but donations go a long way to be able to buy test
gear and cover any other costs of the project (hosting, initial Android Market sign up fee).

If you would like to make a donation, please get it touch. Any amount will be gratefully
appreciated.

Thanks to previous donors;

- Connexionz UK
