# {ANDROID} TO-DO unnamed first version

## ⚠ Possible bugs ⚠
- [ ] Be 100% sure that the **Companion Object** technique for **updating the adapter** of the RecyclerView on an activity, outside it, will always work perfectly.

## Primary
- [ ] `CatalogActivity`: improve search algorithm (logical && instead of ||, I guess).
- [ ] Add fast scroll for the catalog `RecyclerView`.
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
- [X] Refactor: replace "take" by "inscribe".