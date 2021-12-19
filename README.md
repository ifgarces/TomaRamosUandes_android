# *Simulador de toma de ramos Uandes - Android*

Android application for inscribing Uandes' courses.

Developed in Android Studio (Kotlin).
Author: Ignacio F. Garcés.
Universidad de los Andes.

## Features

- Automatic construction of week schedule.
- Exporting week schedule as image to the device's gallery.
- Detection of conflict between events when inscribing courses.
- Exportation of evaluations of inscribed courses to the device's calendar of choice.
- Persistance of inscribed courses in storage, so they will reappear when reopening the app.
- **Only courses from the engineering curriculum map are available**, as I have no access to other schools's courses.

## Project dependencies

- Android SDK 26 (8.0 Oreo) or higher.

## App permissions

- Internet: for accessing the latest catalog and checking for updates (via Firebase Firestore remote database).
- Storage (write): for storing the week schedule as image for the inscribed courses of the user.
- Calendar (read and write): for exporting evaluations to a calendar from the device, chosen by the user.

## Logging

There's not too much logging, so all of it is labelled `_DEBUGLOG_` (for easily accessing it with Android Studio's logcat), except Firebase-related output (see [`FirebaseMaster.kt`](./TomaRamosUandes/app/src/main/java/com/ifgarces/tomaramosuandes/networking/FirebaseMaster.kt)), which is labelled `FirebaseMaster`, just as that object's name.
