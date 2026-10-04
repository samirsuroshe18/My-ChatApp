# 📱 My ChatApp

A modern Android chat application providing a seamless messaging experience with features like Google sign-in, one-to-one chats, and profile management — similar to WhatsApp but built from scratch with Firebase.

## 🎥 Demo

<p align="center">
  <a href="https://www.youtube.com/watch?v=ScsVUOd-dFw" target="_blank">
    <img src="https://img.shields.io/badge/Watch%20on%20YouTube-red?logo=youtube&logoColor=white&style=for-the-badge" alt="Watch on YouTube"/>
  </a>
</p>

## 📸 Screenshots

<p align="center">
  <img width="188" alt="login" src="https://github.com/user-attachments/assets/ad921577-82da-430b-ad94-f204bb229e00" />
  <img width="188" alt="register" src="https://github.com/user-attachments/assets/82bfdc49-fe8c-46cd-ae34-6dc35873a9bc" />
  <img width="188" alt="google-signin" src="https://github.com/user-attachments/assets/499a3f1d-bcaf-49f9-9da7-83029bd60350" />
  <img width="188" alt="home" src="https://github.com/user-attachments/assets/6d20b89f-96f6-4e65-b951-c205827aefd1" />
  <img width="188" alt="chat" src="https://github.com/user-attachments/assets/0d98fe22-bb56-4e24-a723-2b7b631e4231" />
  <img width="188" alt="profile" src="https://github.com/user-attachments/assets/3cfd1b2c-f7f1-4a8f-bef0-71047154626d" />
</p>

## 📥 Download

<p align="center">
  <img width="80" height="80" alt="chat_app_icon" src="https://github.com/user-attachments/assets/95440a12-b545-440f-8696-6dc3696f9b76" />
  <br/><br/>
  <a href="https://github.com/samirsuroshe18/My-ChatApp/releases/latest">
    <img src="https://img.shields.io/badge/Download%20APK-blue?style=for-the-badge&logo=android" alt="Download APK"/>
  </a>
</p>

## 🚀 Features
- 🔑 Google sign-in authentication
- 📧 Email sign-up with a verification link  
- 💬 Private one-to-one chats
- 👤 Customizable user profiles 
- ✍️ Real-time typing indicators  
- 📩 Read receipts (Read/Unread messages)
- 🟢 Online status and last seen
- 🔢 Unread message counts and an unread chats filter
- 🗑️ Delete a message, clear a chat or delete a conversation  
- 🔔 Push notifications (via FCM)  
- ⚡ Real-time data sync (Firebase Realtime Database)

## 🛠️ Tech Stack
- **Language:** Java  
- **UI:** XML layouts  
- **Backend:** Firebase (Auth, Realtime Database, Storage, Cloud Messaging)
- **Notifications:** a small Node.js server, [pushNotification](https://github.com/samirsuroshe18/pushNotification)  
- **IDE:** Android Studio  
- **Design Tools:** Figma, Eraser.io (data modeling)  

## 📲 Installation
1. Download the APK from the [Releases](https://github.com/samirsuroshe18/My-ChatApp/releases/latest).  
2. Enable **installation from unknown sources** on your device.  
3. Tap the APK file to install it.  
4. Create an account or sign in with Google to start chatting.  

## ⚙️ For Developers (Setup Guide)
1. Clone this repo
   ```bash
   git clone https://github.com/samirsuroshe18/My-ChatApp.git
   ```
2. Open the project in Android Studio.
3. Create a Firebase project, add an Android app with the package name
   `com.example.mychatapp`, and put its `google-services.json` in the `app/` folder.
4. Add the SHA-1 fingerprint of your signing key to that Android app in the
   Firebase console. Google sign-in fails without it.
5. Enable Firebase Authentication with the **Email/Password** and **Google** providers.
6. Set up the Firebase Realtime Database and Storage.
7. Sync Gradle and run on an emulator or a device.

Push notifications go through a small server that lives in a separate repository:
[pushNotification](https://github.com/samirsuroshe18/pushNotification).
The server's address is `SERVER_URL` in
`app/src/main/java/com/example/mychatapp/utils/NotificationSender.java`;
change it there to use a server of your own.

## 📬 Contact
👨‍💻 Developer: Samir Suroshe  
📧 Email: [sameersuroshe50@gmail.com](mailto:sameersuroshe50@gmail.com)  
🔗 LinkedIn: [samir suroshe](https://www.linkedin.com/in/samir-suroshe)  

Your feedback and contributions are always welcome!

## License

[MIT](LICENSE)
