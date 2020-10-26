# {ANDROID} TO-DO unnamed first version

## 💀 Bugs 💀
- [ ] Be 100% sure that the **Companion Object** technique for **updating the adapter** of the RecyclerView on an activity, outside it, will always work perfectly.

## Primary
- [ ] Implement/improve missing help dialogs.
- [ ] Make demo video for the app.
- [ ] `AgendaLanscapeFragment`:
  - [ ] Fix bug:  initialized several times, being extremely unoptimal.
  - [ ] Improve image exporting: before export, resize the target view to be wider (1000 dp?), making the resulting image dimensions (almost) constant for all devices.
- [ ] Implement a crash reporting system. Use [ACRA library](https://github.com/ACRA/acra).
- [ ] Paralelly, allow the user to rate/review the app (star number and optional comment), using in the backend a Google Forms.

## Optional changes
- [ ] Refactor: replace "agenda" with "schedule".
- [X] Refactor: replace "take" with "inscribe".