IMPORTANT!!	Good day, I know we were supposed to host our API but we kept coming across problems and we just couldn't get it right. So as a request,
we pushed our API project to github with this link: https://github.com/MichaelIsaacs-ST10143125/Links_API_Final ,

all you have to do is run the API Project in VISUAL STUDIO CODE with "node server.js" i know it's not ideal but atleast we now 
have an API (in Part 2, we did not have an API) and we apologise for the inconvenience,
but our app does work as you'll see in the video, we apologise again for the inconvenience,  thank you.

Links — Android Chat Application

A modern Android chat application built using Kotlin, Node.js, and MySQL, featuring real-time messaging, biometric authentication, offline mode, notifications, profile customization, and multi-language support.

Overview

Links is a lightweight, mobile chat application that allows users to register, sign in, send and receive messages, and manage personal profiles. The app integrates modern Android features such as Fingerprint/FaceID login, real-time push notifications, and offline messaging, making communication reliable even with unstable connectivity.

The backend is powered by a Node.js REST API with a MySQL database, while the Android frontend uses MVVM, Retrofit, and Jetpack components.

Core features

1. User Authentication

Users can register and sign in using email and password.

Passwords are securely hashed before storage.

Authentication is handled through the backend using Express.js.

2. Biometric Authentication (Fingerprint / Face Unlock)

Returning users can log in instantly using biometrics.

Securely stores user credentials locally (encrypted).

Automatically logs the user in when biometric verification succeeds.

3. Real-Time Notifications (FCM)

The app uses Firebase Cloud Messaging to deliver instant push notifications.

Users get notified when new messages are received.

FCM tokens are automatically generated and synced with the backend.

4. Real-Time Chat System

Users can search for other users and start new conversations.

Messages are loaded from the server and displayed instantly.

Messages sent appear immediately in the UI (optimistic UI).

Fully integrated with backend chats and messages models.

5. Offline Mode (Send Messages Without Internet)

If the user sends a message while offline:

The message is stored locally.

A “Saved Offline” message is shown.

When the device reconnects:

All unsent messages are automatically filtered and sent to the server.

This ensures the user never loses messages due to poor connectivity.

6. Multi-Language Support

Users can change the app language from the Settings screen.

Supported languages include:

- English

- IsiXhosa

- Afrikaans

Language changes apply instantly across all screens.

7️.	Dark Mode Toggle

Users can switch between Light Mode and Dark Mode inside Settings.

The app remembers the theme even after restarting.

8.	Editable User Profile + Bio

Users can add or update a bio using a popup form.

The bio is saved:

- in the backend database

- in the user model

- and updated in the Settings screen immediately

Helps personalize profiles and enhance user identity.

Tech Stack
Frontend (Android)

Kotlin

- MVP + LiveData + ViewModel (MVVM)

- Retrofit (API communication)

- Firebase Messaging

- SharedPreferences

- Material Design components

Backend

- Node.js

- Express.js REST API

- MySQL database

- Bcrypt for password hashing

- JSON Web Tokens (JWT) for secure auth

How the App Works (User Flow)

1.	User opens the app

- If a biometric login is available, they can log in instantly.

- Otherwise, they sign in using email + password.

2. Home and Chat

- User selects a person from search or notifications.

- Messages load in real time.

- Messages sent offline are queued and sent later.

3.	Settings

- User can toggle dark mode

- Change language

- Update their bio

- Log out

4. Notifications

- When someone sends a message, FCM pushes a notification even if the app is closed.

Conclusion

Our Links app is a full-featured messaging platform built with modern Android technologies and a robust Node.js backend. It was designed for speed, reliability, and user convenience, offering functionality commonly found in professional messaging applications.
