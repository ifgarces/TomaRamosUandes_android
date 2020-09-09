# {ANDROID} TO-DO unnamed first version


!! FIX USERSTATS NOT HAVING PRIMARY KEY FOR ROOM DATABASE !!
!! and may replace that method with SharedPreferences: https://stackoverflow.com/questions/7217578/check-if-application-is-on-its-first-run !!

- [X] ⚠ Fix bug: conflict not being checked when taking ramos.
- [X] Recolorize credits sum `TextView` when maximum is exceeded.
- [ ] Manage to export events to the user's calendar.
- [ ] Make missing help dialogs.
- [ ] Make demo video for the app.
- [ ] `AgendaLanscapeFragment`:
  - [ ] Fix bug:  initialized several times, being extremely unoptimal.
  - [ ] Improve image exporting: before export, resize the target view to be wider (1000 dp?), making the resulting image dimensions (almost) constant for all devices.
- [ ] Implement a crash reporting system. Use [this](https://github.com/ACRA/acra).
- [ ] Somehow, allow the user to easily and anonymously report an error and, if so, manage to get device data (model, manufacterer and Android version). Probably using Google Forms, in the frontend (`WebView`) or the backend (more complex).
  - [ ] Paralelly, allow the user to rate/review the app (star number and optional comment).

## Optional changes
- [ ] Refactor: replace "agenda" by "schedule".