# *Simulador de toma de ramos Uandes - Android*

**END-OF-LIFE, GO TO THE NEW VERSION OF THIS PROJECT [tomaramos.app](https://tomaramos.app)**

Android application for automating weekly schedule construction and other features when inscribing Uandes' courses.

Developed in Android Studio (Kotlin).

Author: Ignacio F. Garcés.

Universidad de los Andes.

## Features

- Automatic construction of student's week schedule.
- Exporting week schedule as image to the device's gallery.
- Detection of conflicts between events when inscribing courses.
- Exportation of evaluations of inscribed courses to the device's calendar of choice.
- Persistance of inscribed courses in storage, so they will reappear when reopening the app.
- Dark mode.
- **Only courses from the engineering curriculum map are available**, as I have no access to other schools's courses, sadly.

## Project dependencies

- Android SDK 26 (8.0 Oreo) or higher.

## App permissions

- Internet: for accessing the latest catalog and checking for updates (via Firebase Firestore remote database).
- Storage (write): for storing the week schedule as image for the inscribed courses of the user.
- Calendar (read and write): for exporting evaluations to a calendar from the device, chosen by the user.

## Logging

There's not too much logging, so all of it is labelled `LOGF` (for easily accessing it with Android Studio's logcat), except Firebase-related output (see [`FirebaseMaster.kt`](./TomaRamosUandes/app/src/main/java/com/ifgarces/tomaramosuandes/networking/FirebaseMaster.kt)), which is labelled `FirebaseMaster`, just as that object's name. For more details about logging for this particular project, see [`Logf.kt`](./TomaRamosUandes/app/src/main/java/com/ifgarces/tomaramosuandes/utils/Logf.kt).

## Code style

The Kotlin code style for this project is slightly different than the default, as you can see in the files (spacing is changed). To load this code style in Android Studio for integration in code completion and formatting, go to *Editor > Code Style > Kotlin* in the IDE settings, and select *Import scheme...*, then select the file [`kotlin_code_style.xml`](./kotlin_code_style.xml).
