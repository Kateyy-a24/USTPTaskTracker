# USTP-CDO Task Tracker (JavaFX + FXML/Scene Builder + MySQL/XAMPP)

## 1. Database (XAMPP)
1. Open **XAMPP Control Panel** -> Start **Apache** and **MySQL**.
2. Open http://localhost/phpmyadmin -> **Import** -> choose `schema.sql` -> Go.
3. Default login:  **username `student` / password `12345678`**

(If your MySQL root has a password, change it in `src/main/java/com/ustp/tracker/Db.java`.)

## 2. Open in IntelliJ
1. Install **JDK 17 or 21** (File > Project Structure > SDK).
2. File > Open > select the `USTPTaskTracker` folder (the one with `pom.xml`) > "Open as Project".
3. Wait for Maven to download the dependencies (JavaFX + MySQL driver; needs internet the first time).
4. Run: right-click `Launcher.java` > Run  (or Maven panel > Plugins > javafx > `javafx:run`).

## 2b. Forgot Password / Forgot Username (Gmail OTP)
**One-time setup by the system owner (students do nothing):**
1. Use ONE Gmail account as the system sender and turn on **2-Step Verification**.
2. Create an **App Password** at https://myaccount.google.com/apppasswords.
3. Open `mail.properties` (project folder) and fill in `gmail.user` and `gmail.app.password`. Save. Done.
   (Never share this file. Alternative: env vars `USTP_GMAIL_USER` / `USTP_GMAIL_APP_PASSWORD`.)

**How students use it:**
- Login page -> **Forgot password?** -> enter username or email -> a 6-digit code is sent to the email saved in
  that account (shown masked, e.g. ka***@gmail.com) -> enter code -> set new password.
- Login page -> **Forgot username?** -> enter registered email -> code -> the username is shown.
- The code only ever goes to the email registered in the account. Code is valid 5 minutes, 5 tries, resend after 60s.
- Students can also log in with their email instead of username.
Maven will download the `jakarta.mail` dependency (reload Maven in IntelliJ).

## 2c. Deadline reminders (alarm + notification)
While the app is open and you are logged in, it checks your **Pending** tasks every minute:
- **Overdue / Due today**: red alert + alarm ringtone when you log in, repeated every 60 minutes until the task is Completed.
- **Due tomorrow / in 2-3 days**: one reminder per login.
Each alert shows a pop-up, plays the ringtone, and sends a Windows notification (bottom-right tray).
Click **Alarm On/Off** (top-right) to mute or unmute the ringtone. To test: add a task with a deadline of today or tomorrow.

## 2d. Deadline reminder EMAILS
Uses the same system Gmail from `mail.properties` and sends to the email **registered in each account**.
- Pending tasks due in 3 days, 2 days, tomorrow, today -> 1 email per task per day.
- Overdue tasks -> 1 email per day, for up to 3 days late. All tasks are combined in ONE email.
- Sent emails are remembered in the `reminder_log` table (created automatically), so there are no duplicates.
- Turn on/off with the **Email On/Off** button (top-right). Turning it ON sends a confirmation email.
- While the app is open, emails are checked every minute (and right after login).
- **When the app is closed:** run `com.ustp.tracker.ReminderJob` once a day (IntelliJ: right-click the class -> Run).
  It emails every student who has tasks needing attention. To automate it, create a Windows Task Scheduler task
  that runs it daily (or just run it each morning from IntelliJ).

## 2e. Code structure (Object-Oriented design)
| Concept | Where it is used |
|---|---|
| **Encapsulation** | All fields are private with getters/setters: `User`, `Person`, `Session`, `OtpService`, repositories. `Task` is an immutable `record`. |
| **Inheritance** | `Person` -> `User` (model). `JdbcRepository` -> `JdbcUserRepository`, `JdbcTaskRepository`, `JdbcReminderLogRepository` (data). `BaseController` -> every screen controller. |
| **Polymorphism** | (1) **Overriding:** `User` overrides `Person.getDisplayName()`, `equals()`, `hashCode()`, `toString()`. (2) **Interface polymorphism:** `ReminderService` calls every `ReminderChannel` (`ScreenReminderChannel`, `EmailReminderChannel`) with the same `remind()` call, and each behaves differently. (3) **Enum with overridden methods:** `DueStage` (OVERDUE, TODAY, TOMORROW, SOON) - each constant overrides `label()`, `color()`, `repeats()`, so there are no if/else chains. (4) **Overloading:** `Mailer.send(String,...)` / `send(Person,...)`, `AlarmSound.play()` / `play(int)`. (5) Parts are swappable: `Mailer`, repositories, `PasswordHasher`. |
| **Abstraction** | Interfaces: `UserRepository`, `TaskRepository`, `ReminderLogRepository`, `Mailer`, `PasswordHasher`, `ReminderChannel`. Abstract classes: `Person`, `JdbcRepository`, `BaseController`. |

`AppContext` creates all objects once and connects them (each part only knows the interfaces of the others).
Packages: `model`, `dao` (repositories), `security`, `mail`, `service`, `reminder`, `controller`.

## 3. Scene Builder
Install Scene Builder (gluonhq.com/products/scene-builder). In IntelliJ: right-click any
`.fxml` file in `src/main/resources/fxml` > **Open in SceneBuilder**.

## 4. Your images (optional but matches the prototype)
Put these in `src/main/resources/images/`:
- `logo.png`   - USTP logo (round logo on login + sidebar)
- `campus.jpg` - "I love USTP" campus photo for the left side of the login
Without them the app still runs (shows a text logo + dark gradient).
Profile pictures are copied to `C:\Users\<you>\USTPTaskTracker\avatars\`.

## Features
Login + Sign up (validation, duplicate check), show/hide password -> Dashboard (live counts),
Add / Edit / Delete task, Update status, View tasks with search + filter, Upcoming deadlines,
Search & Filter by subject/status, Profile (edit info + upload photo), Logout.
