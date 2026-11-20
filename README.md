# DAEM Revise

An offline Android revision app for the DAEM Year 2 KNEC units, built from your own past papers, notes and textbooks.

## What's inside (version 19)

| Unit | Topics | MCQs | Past-paper questions | Flashcards | Textbook links |
| --- | --- | --- | --- | --- | --- |
| Aircraft Mechanical Technology I | 14 | 91 | 173 | 66 | 32 |
| Electronics & Control Systems | 13 | 102 | 68 | 76 | 5 |
| Engineering Mathematics II | 8 | 61 | 67 | 43 | 24 |
| Aircraft Propeller Systems | 5 | 31 | 25 | 29 | 17 |
| Airfield Safety II & Research Methods | 7 | 40 | 21 | 41 | 6 |
| Airframe Systems I | 6 | 41 | 30 | 39 | 6 |
| Theory of Flight | 5 | 37 | 35 | 37 | 11 |
| Business Plan | 5 | 31 | 15 | 24 | – |

- **Welcome tour** (new in v7): four short pages from Kapi on first launch, with an optional name for the greeting. Replay it with the ⓘ button on the home screen.
- **Send feedback** (new in v7): at the bottom of the home screen. Opens WhatsApp (or any app) with a ready-made message that includes the app version and phone model. Put your number in `data/Feedback.kt` (`WHATSAPP_NUMBER`) so it goes straight to your chat.
- **Crash reporter** (new in v8): if the app ever closes by itself, the next launch shows what went wrong with a **Send report** button.
- **New app icon** (new in v7): the open book with a gold plane.
- **Study score card** on the home screen: today's study time, streak, quiz accuracy, a score out of 100 and a 7-day chart. Time counts while the app is open.
- **Every topic in one list** per unit (new in v20), in course order (Mech Tech follows the syllabus; Electronics and Maths follow the lecturers' order; set in `TOPIC_ORDER` in `tools/build_content.py`). The old Covered / Safety / Later tags and the "This term" filter are gone. A topic's page shows how many KNEC exams asked it ("Asked 8 times").
- **Notes for beginners** (new in v20, Electronics and Mech Tech so far): each topic opens with "In plain words", then the key ideas with a labelled diagram beside the idea it explains, a worked example and exam traps. All 13 Electronics and all 14 Mech Tech topics now have notes. New in Electronics: op-amps (checked against Omae's analog notes and c.systems.pdf), combinational and sequential logic, ADC/DAC and displays, stability and time response. New in Mech Tech: thin cylinders, balancing, gear trains (incl. epicyclic), clutches and belt drives, following Nganga's notes. The Feb 2026 Mech Tech mock is in the Papers tab.
- **Notes tab**: short exam-focused revision notes, plus where the full lecturer PDF is on your PC.
- **Books tab**: the textbook chapters for the topic. Tap **Read** and the book opens inside the app at the exact page.
- **Textbooks** (new in v9, built in): 7 books ship inside the app — Bansal, Khurmi, Bird & Ross, H.K. Dass, Kermode, the BA SC17 propeller notes and EASA Module 17. The reader has zoom (− / +) and Go to page. Nothing to download or set up.
- **Home-screen widget** (new in v6): your 🔥 streak, today's minutes against the 60-minute goal, your study score, and Kapi the capybara — awake once you've studied today, asleep if not. Tap it to open the app. Add it from the "Add Kapi to your home screen" card, or long-press your home screen → Widgets → DAEM Revise.
- **Quizzes** (spaced repetition new in v19): MCQs per topic or a 10-question unit quiz. Options are shuffled; every answer shows a full explanation. Missed questions come back first next time, and ones you get right return after a growing gap (1 day, 3 days, about a week…). The quiz tiles show how many are due.
- **Kapi's Park** (new in v20): a map from the entrance up to Kapi's hot spring. Each unit is an area (Workshop Yard, Circuit Forest, Maths Meadow, Propeller Pier, Safety Station, The Hangar, Runway Hill, Market Square) and each topic a stop with 3 stars: notes read ("Mark as read" at the end of the notes), quiz 70%+, and half the flashcards learned. Areas bloom at 25/50/75/100%, a finished area gives Kapi a keepsake, and every visit celebrates the stars earned since the last one. The glowing "Next stop" shows where to study next.
- **Quick 5** (new in v20): five MCQs from all your units in one tap, for spare moments. Missed and due questions come first.
- **Kapi's games** (new in v20): three games, each with a short how-to the first time.
  - **Kapi Blocks** (restyled in v20): drag shapes onto an 8×8 board, clear rows and columns, streaks and a Perfect clear bonus. Mostly 4-block shapes; singles are rare. Rounded glossy blocks, each shape in its own colour (same family as Kapi Jewels), on a night-navy board. The board theme changes every 600 points and after great moves: Midnight, Jade, Ember, Ocean, Walnut, Dusk. Rows about to clear glow gold. One "new shapes" rescue a game.
  - **Kapi Jewels** (was Kapi Crush; restyled in v20): swap cut gemstones on a night-navy board to line up 3 or more. Each colour has its own shape (round sapphire, emerald cut, hexagonal ruby, lozenge topaz, trillion amethyst). 4 in a line makes a Beam jewel (clears its row/column), an L or T makes a Burst (3×3), 5 makes a Prism (clears every jewel of one colour). Swap two power-ups together for a combo. Levels with collect goals and ★ ratings; beat one and the next follows in the same game. One +5 moves rescue a game.
  - **Kapi Mahjong** (classic tiles in v20): tap free tiles to send them up to a 4-slot tray; two the same clear. If the tray fills with no match, the game is over. Classic faces drawn by the app: circles 1–9, bamboo 1–9 and the red 中 and green 發. A new layout each level (Little Pyramid, Twin Towers, Kapi's Hill, The Runway…), and the next level follows in the same game. Undo, 3 hints, 2 shuffles, and one rescue that sends the tray back.
  - **The games screen** (new in v20): an evening-lounge backdrop, Kapi under a spotlight, and three cards with a small picture of each game, its level, best score and a gold Play button.
  - Each game has **one free try a day**. After that, games are earned by studying: one for every 27 minutes of study, or for a Quick 5 with 4 or more right (at most 2 a day from Quick 5s). At most 5 games a day, shared between the three. Time spent playing does not count as study time. The tour only hints at the 27 ("Kapi's lucky number").
- **Note diagrams** (new in v20, always shown): 27 labelled diagrams drawn from scratch (nothing copied from textbooks), placed inside the notes with `FIG("id")`: logic gates, half adder, 4-to-1 MUX, clocked SR flip-flop, Johnson counter, binary-weighted DAC, successive-approximation ADC, energy bands, power-supply blocks, BJT and diode symbols, inverting op-amp, closed-loop block diagram, Bode margins, step response, test signals, stress-strain curve, SFD/BMD, thin cylinder stresses, balancing polygon, simple/compound and epicyclic gear trains, plate and cone clutches, open and crossed belts, the four forces, aircraft axes and the blade element.
- **Blueprint sketches** (new in v20, developer preview, `Progress.DEV_TOPIC_ART`): every topic (except Business Plan) gets a blueprint-style line drawing — a grid-paper strip at the top of its page and a faint copy behind its quiz and flashcards. Set the switch to `false` before a Play Store build to hide them.
- **Papers tab**: every KNEC question on the topic with its sitting, plus a "Practised" tick. Questions that need a diagram (circuits, beams, gear trains…) show it, cropped from the paper (new in v20). Tap a diagram to zoom.
- **Formulas** and **Facts** tabs.
- **Flashcards with spaced repetition** (new in v19, like Anki / SM-2): tap Show answer, then rate yourself Again / Hard / Good / Easy. Each button shows when the card comes back (<1m, 1d, 4d…). Due cards come first, then up to 15 new cards a session; gaps grow 1 → 3 → 8 → 20 days → months as you keep remembering. "Practise all cards anyway" reviews without changing the schedule.
- **Search**: find any question by keyword or year, e.g. `torsion 2024`.
- Everything is saved on the phone and works with no internet.

### Study score

Out of 100: up to 50 for time (full at 60 minutes a day), up to 30 for today's quiz accuracy (15 if no quiz yet), and up to 20 for the streak (full at 7 days). A day counts toward the streak after 5 minutes of study.

## Web version (for iPhones)

The `docs/` folder is a web version of the app for friends with iPhones (or any phone or laptop):
notes with all the diagrams, past papers with their figures, flashcards, quizzes (with the same
spaced repetition), Quick 5, search, stars and the exam countdown. Progress is saved on each phone.
Kapi's games, Kapi's Park, the PDF books and the widget stay Android-only.

- **Hosting:** GitHub Pages, free. In the repo on github.com: Settings → Pages → "Deploy from a branch" → branch `main`, folder `/docs` → Save. The address is then `https://<your-username>.github.io/<repo-name>/`.
- **On an iPhone:** open the address in Safari → Share → Add to Home Screen. It opens full screen like an app and works offline after the first visit.
- **Updating it:** after `python tools/build_content.py`, run `python tools/build_web.py`, then commit and push. Phones pick up the new version the next time they open it with internet.
- It reads the same `content.json` as the Android app, the diagrams from `tools/sketches.py` and the exam timetable from `data/Exams.kt`, so there is nothing else to keep in step. Don't edit `docs/data`, `docs/figures` or `docs/img` by hand; `build_web.py` rewrites them.

## Sharing the app with friends

1. In Android Studio: **Build › Build App Bundle(s) / APK(s) › Build APK(s)**.
2. When it finishes, click **locate** in the pop-up. Send `DAEM-Revise-v20.apk` on WhatsApp (the file is named after the version automatically).
3. Your friend taps the file, allows installing from WhatsApp/Files when asked, then taps **Install**.

## Updating from an older version

1. Close the project in Android Studio (**File → Close Project**).
2. Replace the project folder with the new one and open the inner `DAEMRevise` folder again.

Progress saved on your phone carries over, because the app ID is unchanged.

## Open and run it (first time)

1. Unzip `DAEMRevise.zip` into `C:\Users\<you>\AndroidStudioProjects\`.
2. Open Android Studio → **File → Open** → choose the `DAEMRevise` folder → **OK**.
3. If asked "Trust project?", choose **Trust Project**.
4. Wait for **Gradle sync** to finish (bottom status bar). The first time it downloads Gradle 8.9 and the Android libraries, which needs internet and can take 5–15 minutes.
5. If Android Studio offers to install **Android SDK Platform 35** or other missing parts, accept.
6. Pick a device in the top toolbar: your phone (USB debugging on) or an emulator from **Device Manager**.
7. Press the green **Run ▶** button.

If sync fails, copy the first red error line and send it to Claude.

## Project layout

```
app/src/main/
  assets/content.json        All study content (generated, see below)
  assets/figures/*.png      Diagrams cropped from the KNEC papers (linked with ;; fig: in *_questions.txt)
  assets/books/*.pdf         The 7 bundled textbooks, trimmed to your course outlines (see KEEP in tools/books.py)
  java/com/grandpyanne/daemrevise/
    DaemApp.kt               Runs first: crash recorder, on-demand WorkManager for the widget
    MainActivity.kt          Starts the app and switches between screens
    data/Content.kt          Reads content.json into courses, units, topics, books
    data/Progress.kt         Study time, streak, quiz scores, known cards
    data/Games.kt            Rules of Kapi Blocks, Kapi Jewels (CrushGame) and Kapi Mahjong (no drawing, so they can be tested)
    data/Settings.kt         Welcome-tour flag and the student's name
    data/CrashLog.kt         Saves the error if the app crashes (shown by ui/CrashScreen.kt)
    data/Feedback.kt         Send feedback button (set WHATSAPP_NUMBER here)
    data/BookFiles.kt        Gets a bundled textbook ready for the reader
    ui/HomeScreen.kt         Greeting, study score card, textbooks, list of units
    ui/Widgets.kt            Study score card, progress rings, status tags
    ui/UnitScreen.kt         Topics in a unit, this-term filter, unit quiz
    ui/TopicScreen.kt        Notes, papers, formulas, facts and books for one topic
    ui/WelcomeScreen.kt      First-run tour
    ui/BooksUi.kt            Books tab and Textbooks list
    ui/ReaderScreen.kt       Built-in PDF reader (zoom, go to page)
    ui/Figures.kt            Past-paper diagrams under questions, tap to zoom
    ui/Games.kt              Kapi's games hub, unlock rules on screen, shared game pieces
    ui/GameBlocks.kt, GameCrush.kt, GameMahjong.kt   The three games' screens
    ui/ParkScreen.kt         Kapi's Park map, stars, blooms and keepsakes
    ui/TopicArt.kt           Blueprint sketches, note diagrams and their drawing engine
    ui/TopicSketches.kt      GENERATED by tools/sketches.py: the sketch and diagram drawings
    ui/QuizScreen.kt         MCQ quiz with explanations
    ui/FlashcardScreen.kt    Flashcard practice
    ui/SearchScreen.kt       Search across all questions
    ui/Theme.kt, Components.kt  Colours and shared pieces
    widget/StudyWidget.kt    Home-screen widget (Jetpack Glance)
  res/drawable/mascot_*.xml  Kapi the capybara, happy and sleepy
tools/
  build_content.py           Builds content.json (also holds Mech Tech & Electronics formulas/cards)
  study_content.py           Notes, MCQs, topic status and PDF pointers (merges the unit files)
  *_content.py              Notes, formulas, cards and MCQs for each other unit
  books.py                   The textbooks and which chapter/page goes with each topic
  *_questions.txt            Past-paper questions by topic
  sketches.py                The topic sketches and note diagrams; run it to regenerate ui/TopicSketches.kt
  build_web.py               Refreshes the web version in docs/ from the app's content
  electronics_content.py     Notes and MCQs for the Electronics topics added in v20
  mechtech_content.py        Notes and MCQs for the Mech Tech topics added in v20
```

## Adding or editing content

1. Edit the question lists in `tools/*_questions.txt` (add `;; fig: name.png` to attach a diagram from `assets/figures`), the notes and MCQs in the `tools/*_content.py` files, or the textbook pages in `tools/books.py`.
2. Run `python tools/build_content.py` from the project folder. For drawings, edit `tools/sketches.py` and run `python tools/sketches.py`.
3. Press **Run ▶** again in Android Studio.

## Notes

- Textbook page numbers are the page the in-app reader shows (not the printed page number), checked against the PDFs. The EASA Module 17 PDF was stored upside down; the bundled copy is turned the right way up.
- The textbooks are trimmed to the chapters in your course outlines (Dass, Bansal, Khurmi, Bird, Kermode), about 81 MB in total. Only the book you opened last is kept unpacked on the phone. The full books are still in your DAEM folders.
- The textbooks make the app about 90 MB. They are for your own study: remove or license them before any wider release.
- A few typos from the source question bank were corrected (for example "150 kW" in a compound-strut question is "150 kN").
- Electronics questions marked "KNEC paper" came from a scanned paper whose year was not legible.
- KNEC papers, lecturer notes and textbooks are for your own study. Ask permission before sharing the app more widely.

---

DAEM Revise was designed and built by Grandpyanne™.
