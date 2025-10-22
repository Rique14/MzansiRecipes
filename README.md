# Mzansi Recipes App - POE
## Table of Contents
 [Group Members](#group-members)
 [ Video Link](#video-link)
 [Link to Repository](#link-to-repository)
 [Mzansi Recipes Logo](#mzansi-recipes-logo)
1. [Introduction](#1-introduction)
2. [Features](#2-features)
3. [Backend and API](#3-backend-and-api)
4. [Design Considerations](#4-design-considerations)
5. [Tech Stack and Architecture](#5-tech-stack-and-architecture)
6. [GitHub and GitHub Actions](#6-github-and-github-actions)
7. [How to Contribute](#7-how-to-contribute)
8. [Use of Logs in Project](#8-use-of-logs-in-project)
9. [App Functionality ](#9-app-functionality)
10. [Release Notes – Final POE](#10-release-notes--mzansi-recipes-final-poe)
11. [License](#11-license)

## Group Members
- Princely Makhwara – ST10263265
- Aime Ndumuhire – ST10255663
- Fortunate Majere- ST10231459
- Sabelo Sibiya - ST10327016
- Enrique Arendse – ST10302006

## Video Link
https://youtu.be/nLh78x2sff4 

## Link to Repository 
https://github.com/Rique14/MzansiRecipes 

## Mzansi Recipes Logo 
<img width="400" height="400" alt="ic_pot_icon" src="https://github.com/user-attachments/assets/d529af05-3864-4d6d-9544-d8ea9ec75543" />

​
 ## 1. Introduction
 Mzansi Recipes is a mobile application dedicated to celebrating the rich and diverse culinary heritage of South Africa. It provides a platform for users to discover, share and enjoy authentic local recipes. The app is designed to be a community-driven space where food enthusiasts can connect, learn and preserve the vibrant food culture of the nation.
​
 ## 2. Features
 - **User Authentication**: Secure user registration and login with encrypted password storage.
 - **Recipe Discovery**: Browse a wide variety of recipes from around the world using **The MealDB API**.
 - **User Settings**: Users can manage their profile and app settings.
 - **Community Engagement**: Share your own recipes and interacting with other users recipes by liking their recipes.
 - **Shopping List**: Easily create a shopping list, by adding recipe ingredients to your shoppinfg list.
 - **Offline Access**: Access your favorite recipes and shopping lists even without an internet connection.
​
 ## 3. Backend and API
 ### 3.1. Firebase
 The app uses **Firebase** as its backend for:
 - **Authentication**: Securely managing user accounts, including login and registration, with encrypted passwords.
 - **Cloud Firestore**: Storing user-generated data such as saved recipes, user profiles and community contributions.
 - <img width="600" height="600" alt="Screenshot 2025-10-07 141535" src="https://github.com/user-attachments/assets/bd5af3a9-5a29-47a6-92b3-6d7fdeff610a" />

​
 ### 3.2. The MealDB API Integration
The application sources its recipe content by connecting to The MealDB, a public REST API with a comprehensive database of dishes. This integration is essential for providing users with a vast and varied collection of recipes. We use the Retrofit library to manage all network requests, enabling the app to search, filter and retrieve detailed recipe information. To ensure a smooth user experience and provide offline access, the RecipeRepository class caches the data fetched from the API into a local Room database. This strategy improves performance by reducing network calls and allows the app to remain functional even without an internet connection.

 ## 4. Design Considerations
 The app is built with a focus on a clean, intuitive, and user-friendly interface. Key design principles include:
​
 - **Simplicity**: A minimalist design that makes it easy for users to navigate and find what they need.
 - **Consistency**: A consistent design language across all screens and components.
 - **Readability**: Clear and legible typography to ensure a pleasant reading experience.
 - **Visual Appeal**: High-quality images and a visually appealing layout to showcase the delicious food.
​
 ## 5. Tech Stack and Architecture
 The Mzansi Recipes app is built using the latest Android development technologies:
​
 - **Kotlin**: The primary programming language for building robust and modern Android apps.
 - **Jetpack Compose**: A modern declarative UI toolkit for building native Android UI.
 - **ViewModel**: For managing UI-related data in a lifecycle-conscious way.
 - **Coroutines**: For managing background threads with simplified code and improved performance.
 - **Dagger Hilt**: For dependency injection to simplify the management of dependencies.
 - **Room**: For local database storage to enable offline access.
 - **Retrofit**: For making network requests to The MealDB API.
​
 - The app follows the MVVM (Model-View-ViewModel) architecture to separate the business logic from the UI, making the codebase more modular, testable and maintainable.
​
 ## 6. GitHub and GitHub Actions
​
 The project is hosted on GitHub and we use GitHub Actions to automate our development workflow.

 ### 6.1. Github Action
<table>
  <tr>
    <td><<img src="https://github.com/user-attachments/assets/97d6cc51-0906-4dac-9aca-148a362a36a0"  width="100%"  alt="Screenshot (336)"/></td>
    <td><<img src="https://github.com/user-attachments/assets/99765953-5c16-4eb6-9654-bea85b92e237"  width="100%" alt="Screenshot (337)" /></td>
  </tr>
  <tr>
    <td colspan="2" align="center">
      <<img src="https://github.com/user-attachments/assets/248fc0b2-6a1c-4214-a280-ff1fe6bc6b92" width="50%" alt="Screenshot (338)"  />
    </td>
  </tr>
</table>


 ### 6.2. Continuous Integration (CI)
​
 We have a CI pipeline that runs on every push and pull request to the `master` branch. This pipeline performs the following tasks:
​
 - **Build the App**: Compiles the code and ensures that the app builds successfully.
 - **Run Tests**: Executes unit and instrumentation tests to ensure the correctness of the code.
​
 This helps us to maintain a high level of code quality and to catch any potential issues early in the development process.
​
 ### 6.3. Continuous Deployment (CD)
​
 We are in the process of setting up a CD pipeline that will automatically deploy the app to the Google Play Store whenever a new version is released.
​
 ## 7. How to Contribute
​
 We welcome contributions from the community! If you would like to contribute to the project, please follow these steps:
​
 1. Fork the repository.
 2. Create a new branch for your feature or bug fix.
 3. Make your changes and commit them with a descriptive commit message.
 4. Push your changes to your forked repository.
 5. Create a pull request to the `master` branch of the main repository.

## 8. Use of Logs in Project

<table>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/c88d2193-d00a-406b-bddd-2fea4b40c037" alt="Screenshot (328)" width="100%"/></td>
    <td><img src="https://github.com/user-attachments/assets/23b7c998-1d22-4690-9f6a-d3752f35fafd" alt="Screenshot (324)" width="100%"/></td>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/939b0e0e-8f0c-40f4-bdfb-f338e459be48" alt="Screenshot (321)" width="100%"/></td>
    <td><img src="https://github.com/user-attachments/assets/d84884ac-9cce-48fb-ab30-7d94d4cde857" alt="Screenshot (329)" width="100%"/></td>
  </tr>
  <tr>
    <td colspan="2" align="center">
      <img src="https://github.com/user-attachments/assets/e34b7f99-6631-483f-8399-b94cd50e7c20" alt="Screenshot (327)" width="50%"/>
    </td>
  </tr>
</table>


 ## 9. App Functionality 
 ### Splash Screen
The splash screen serves as the initial visual introduction to the application the moment it is launched. Its primary function is to display branding elements like the app's logo, name, or a unique graphic while the main application is loading in the background. This provides immediate feedback to the user that the app is starting, creating a smoother and more professional user experience than a blank screen would. Essentially, it acts as a welcome mat, setting the tone for the application and making the load time feel shorter and more engaging for the user.

<img width="1920" height="1024" alt="Screenshot (350)" src="https://github.com/user-attachments/assets/48b23c8b-2a48-4164-9263-1aa5e9f536f9" />

### Login Screen
The login screen is the secure gateway for returning users to access their personal accounts and saved content. Its core functionality is to authenticate a user's identity, typically through an email or username and a password. To enhance user convenience, modern login screens often include options for social media logins, allowing for quick access through existing accounts like Google. Additionally, a crucial feature is the "forgot password" link, which enables users to reset their password if they have forgotten it, ensuring they can regain access to their account without creating a new one.

<img width="1920" height="1011" alt="Screenshot (340)" src="https://github.com/user-attachments/assets/1583de4d-96ae-4004-9293-a30f40d657c2" />


### Register Screen
The registration, or sign-up, screen is designed for new users to create a personal account within the application. The primary function is to collect essential user information, which typically includes a name, email address, and a password for the new account. The goal is to make the sign-up process as quick and straightforward as possible to encourage user retention from the very beginning.

<img width="1920" height="1028" alt="Screenshot (341)" src="https://github.com/user-attachments/assets/3b40e8bf-9f31-4a5f-bfca-742cf66bcfeb" />


### Home Screen
The home screen is the central hub of the application, where users begin their journey and discover content. This screen is designed for easy navigation, providing clear pathways to the app's main features like recipe categories, the search function, and trending recipes. By presenting dynamic and interesting content, the home screen encourages users to explore the app further.

<img width="1920" height="1011" alt="Screenshot (342)" src="https://github.com/user-attachments/assets/50c301f6-6834-45cc-8920-4177900ea966" />


### Recipe Detail Screen
The recipe detail screen provides all the necessary information for a user to successfully prepare a specific dish. It prominently displays the recipe's name, an appealing image, and a list of all required ingredients with their quantities. This screen offers clear, step-by-step cooking instructions. Additional functionalities often include the ability to save recipes to the saved recipes screen and to add ingredients to the shopping list.

<img width="1920" height="1017" alt="Screenshot (345)" src="https://github.com/user-attachments/assets/a0e9de50-d849-484f-9da5-4528e93bf398" />


### Community Screen 
The community section transforms the app from a solo experience into an interactive social platform. Its functionality revolves around enabling users to connect and engage with one another over their shared interest in cooking. This is achieved through features that allow users to share their own recipes, post photos of their culinary creations. Furthermore, users can interact by liking recipes, which helps to build a vibrant and supportive community of food lovers.

<img width="1920" height="1021" alt="Screenshot (343)" src="https://github.com/user-attachments/assets/94e53723-4a90-47a4-8dc7-5a32e9d9cebd" />

<img width="1920" height="1007" alt="Screenshot (344)" src="https://github.com/user-attachments/assets/0d0818d1-4839-4c37-9c2f-2c02d2bf30b8" />


### Shopping Screen
The shopping list feature is a practical tool designed to simplify grocery shopping for the user. Its core functionality is to allow users to add ingredients from any recipe to a digital list with a single tap. User can tick off the ingredients as they shop and later clear it when they done.

<img width="1920" height="1017" alt="Screenshot (346)" src="https://github.com/user-attachments/assets/3f81b01d-4377-423f-816f-f7f538716bb7" />

### Settings Screen
The settings screen is where users can customize their app experience to their personal preferences. This section allows users to view their profile, saved recipes inside profile, turning notifications on or off, change theme to light or dark, change the language to their preference and to safely logout.

<img width="1920" height="1007" alt="Screenshot (347)" src="https://github.com/user-attachments/assets/9cbd09d6-202d-456c-971a-663bf64943e5" />


### Profile Screen
The user can see their full name and email. They can also view their saved recipes.

<img width="1920" height="1007" alt="Screenshot (348)" src="https://github.com/user-attachments/assets/6f49ceb1-bd1f-4ec6-8bac-233bd970f9fe" />


### Saved Recipes Screen
Users saved recipes are saved to the saved recipes screen for when they want to cook an specific recipe or also just for later view.

<img width="1920" height="1017" alt="Screenshot (349)" src="https://github.com/user-attachments/assets/dbbd4af6-9699-44f2-85ab-42898eda10d7" />

 # 10. Release Notes – Mzansi Recipes Final POE
 ### Overview

This update marks the final Proof of Evidence (POE) release of Mzansi Recipes, introducing innovative features that significantly enhance the app's functionality, usability and resilience.

 ## 10.1 New Features 

- Single Sign-On (SSO): Enables users to log in seamlessly using an existing Google account for quicker access.
- Multi-language Support: The app now supports multiple South African languages, making it more accessible to a diverse audience.
- Offline-First Experience: Mzansi Recipes now works seamlessly even without an internet connection. You can browse recipes you've previously viewed and manage your saved collection offline. All changes are automatically synced with the cloud once you reconnect, thanks to a robust background synchronization mechanism.
- Community and Social Sharing: A new "Community" section has been added where users can share their own culinary creations. You can now view posts from other users and get inspired by the broader Mzansi Recipes family.
- Save Your Favorites: You can now save your favorite recipes with a single tap. All saved recipes are available in a dedicated "Saved Recipes" screen for quick and easy access, even when you're offline.
- Complete User Authentication: The app now features a full authentication system, allowing users to register for a new account, log in and securely reset their passwords if needed.
- 
 ## 10.2 Other Enhancements

- Modern, Reactive UI: The entire user interface has been built using Jetpack Compose, offering a smooth, modern, and reactive experience.
- Optimized Data Handling: We've integrated the MealDB API for a vast collection of recipes and optimized data flow using Kotlin Coroutines and Flow.
- Improved Navigation: The app's navigation has been streamlined, making it more intuitive to move between recipe browsing, your saved collection, and the new community section.

## 10.3 App running on mobile phone
<img src="https://github.com/user-attachments/assets/47c90b77-dd1e-49dc-b12c-fb7596d2fb1c" alt="App running on phone" width="300" height="600" />

### 10.3.1 Splash Screen
<img src="https://github.com/user-attachments/assets/ee8bdaca-fdcc-4f47-80ad-fa935475af70" alt="Splash Screen" width="300" height="600" />


### 10.3.2 Login Screen
<img src="https://github.com/user-attachments/assets/79986e8c-70c3-4af9-a227-1e62db2a1f36" alt="Login Screen" width="300" height="600" />

### 10.3.3 Register Screen
<img src="https://github.com/user-attachments/assets/7801d77e-3283-4a74-8d5d-13e99201121d" alt="Register Screen" width="300" height="600" />

### 10.3.4 Home Screen
<img src= "https://github.com/user-attachments/assets/88f4b2cf-b6ad-433e-9d3a-59f310d8f095" alt="Home Screen" width="300" height="600"  />

### 10.3.5 Recipe Detail Screen
<img src="https://github.com/user-attachments/assets/8a71dd79-7989-440c-a112-737bbb12aefd" alt="Recipe Detail Screen" width="300"  height="600" />

### 10.3.6 Community Screen
<img src="https://github.com/user-attachments/assets/2f5c7c39-76e2-4a44-8038-512233c5b533" alt="Community Screen"  width="300" height="600" /> <img src="https://github.com/user-attachments/assets/4f5cf9ef-9b74-4be4-a4c0-664990bb0fbd" alt="Community Screen" width="300"  height="600"/>

### 10.3.7 Shopping Screen
<img src="https://github.com/user-attachments/assets/64fb2a6a-1bf2-46bf-93dc-9ad73e2da8a9"  alt="Shopping Screen" width="300" height="600" />

### 10.3.8 Settings Screen
<img src="https://github.com/user-attachments/assets/9c1fb867-f5ee-4253-a518-0c111ecc74d1" alt="Settings Screen" width="300" height="600" />

### 10.3.9 Saved Recipe Screen
<img src="https://github.com/user-attachments/assets/4019bbef-b79c-462d-9a23-691e0c91b847" alt="Saved Recipe Screen" width="300" height="600" />

### 10.3.10 Profile Screen
<img src="https://github.com/user-attachments/assets/b216d0ce-3df3-4fb1-a0c5-c3268c8c2f0a" alt="Profile Screen" width="300" height="600" />  <img src="https://github.com/user-attachments/assets/4cf12f8d-7ef4-4b19-aecb-fd1d9c424a61" alt="Profile Screen" width="300" height="600" />

### 10.3.11 Push Notification
<img src="https://github.com/user-attachments/assets/dd509093-2f36-47b4-93f9-897956339457" alt="Shopping Screen" width="300" height="600" />

### 10.3.12 Signed APK
<img width="1919" height="1018" alt="Screenshot 2025-10-21 162832" src="https://github.com/user-attachments/assets/9ee78c14-b4c8-4a8c-8d29-c55bc0ebbc32" />

### 10.3.13 Ready for upload on google playstore
<img width="1910" height="1020" alt="Screenshot 2025-10-21 195243" src="https://github.com/user-attachments/assets/272af2d1-ffd8-4ea3-bc45-b2772cd0c0ce" />


 ## 11. License
​This project is licensed under the MIT License. See the `LICENSE` file for more information.
