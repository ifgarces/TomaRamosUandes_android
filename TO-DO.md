# TO-DO list

## 💀 Bugs 💀
- [X] Fix strange things happening when there is a non-evaluation event that doesn't have any day of the agenda assigned. (e.g. Ecuaciones Diferenciales, sección 2, 2021-10).

## Primary
- [ ] Be 100% sure that the **Companion Object** technique for **updating the adapter** of the RecyclerView on an activity, outside it, will always work perfectly.
- [ ] Make demo video for the app.
- [ ] `AgendaLanscapeFragment`:
  - [ ] Fix bug:  initialized several times, being extremely nonoptimal.
  - [ ] Improve image exporting: before export, resize the target view to be wider (1000 dp maybe), making the resulting image dimensions (almost) constant for all devices.
- [ ] Implement a crash reporting system. Use [ACRA library](https://github.com/ACRA/acra).
- [ ] Allow the user to rate/review the app (star number and optional comment), using Google Forms at the backend.