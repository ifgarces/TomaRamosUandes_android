# TO-DO list

## ⚠ URGENT ⚠

## Primary
- [ ] Find the exact cause of the `NullPointerException` happening sometimes when trying to list user calendars in `CalendarHandler`.
- [ ] Be 100% sure that the **Companion Object** technique for **updating the adapter** of the RecyclerView on an activity, outside it, will always work perfectly.
- [ ] `AgendaLanscapeFragment`:
  - [ ] Fix light bug:  initialized several times, being extremely nonoptimal.
  - [ ] Improve image exporting: before export, resize the target view to be wider (1000 dp maybe), making the resulting image dimensions (almost) constant for all devices.
- [ ] Implement a crash reporting system. Use [ACRA library](https://github.com/ACRA/acra).
- [ ] Allow the user to rate/review the app (star number and optional comment), using Google Forms at the backend.
