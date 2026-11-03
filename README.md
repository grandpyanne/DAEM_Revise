# DAEM Revise

An offline Android revision app for the DAEM Year 2 KNEC units, built from your own past papers, notes and textbooks.

## What's inside (version 19)

| Unit | Topics | MCQs | Past-paper questions | Flashcards | Textbook links |
| --- | --- | --- | --- | --- | --- |
| Aircraft Mechanical Technology I | 14 | 55 | 159 | 66 | 32 |
| Electronics & Control Systems | 13 | 52 | 68 | 76 | 5 |
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
- **Topics tagged Covered, Safety or Later** to match this term's teaching. "This term" filter on each unit.
- **Notes tab**: short exam-focused revision notes, plus where the full lecturer PDF is on your PC.
- **Books tab**: the textbook chapters for the topic. Tap **Read** and the book opens inside the app at the exact page.
- **Textbooks** (new in v9, built in): 7 books ship inside the app — Bansal, Khurmi, Bird & Ross, H.K. Dass, Kermode, the BA SC17 propeller notes and EASA Module 17. The reader has zoom (− / +) and Go to page. Nothing to download or set up.
- **Home-screen widget** (new in v6): your 🔥 streak, today's minutes against the 60-minute goal, your study score, and Kapi the capybara — awake once you've studied today, asleep if not. Tap it to open the app. Add it from the "Add Kapi to your home screen" card, or long-press your home screen → Widgets → DAEM Revise.
- **Quizzes** (spaced repetition new in v19): MCQs per topic or a 10-question unit quiz. Options are shuffled; every answer shows a full explanation. Missed questions come back first next time, and ones you get right return after a growing gap (1 day, 3 days, about a week…). The quiz tiles show how many are due.
- **Papers tab**: every KNEC question on the topic with its sitting, plus a "Practised" tick.
- **Formulas** and **Facts** tabs.
- **Flashcards with spaced repetition** (new in v19, like Anki / SM-2): tap Show answer, then rate yourself Again / Hard / Good / Easy. Each button shows when the card comes back (<1m, 1d, 4d…). Due cards come first, then up to 15 new cards a session; gaps grow 1 → 3 → 8 → 20 days → months as you keep remembering. "Practise all cards anyway" reviews without changing the schedule.
- **Search**: find any question by keyword or year, e.g. `torsion 2024`.
- Everything is saved on the phone and works with no internet.

### Study score

Out of 100: up to 50 for time (full at 60 minutes a day), up to 30 for today's quiz accuracy (15 if no quiz yet), and up to 20 for the streak (full at 7 days). A day counts toward the streak after 5 minutes of study.

## Sharing the app with friends

1. In Android Studio: **Build › Build App Bundle(s) / APK(s) › Build APK(s)**.
2. When it finishes, click **locate** in the pop-up. Send `DAEM-Revise-v19.apk` on WhatsApp (the file is named after the version automatically).
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
  assets/books/*.pdf         The 7 bundled textbooks, trimmed to your course outlines (see KEEP in tools/books.py)
  java/com/grandpyanne/daemrevise/
    DaemApp.kt               Runs first: crash recorder, on-demand WorkManager for the widget
    MainActivity.kt          Starts the app and switches between screens
    data/Content.kt          Reads content.json into courses, units, topics, books
    data/Progress.kt         Study time, streak, quiz scores, known cards
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
```

## Adding or editing content

1. Edit the question lists in `tools/*_questions.txt`, the notes and MCQs in the `tools/*_content.py` files, or the textbook pages in `tools/books.py`.
2. Run `python tools/build_content.py` from the project folder.
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
