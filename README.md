# Mzansi Recipes App - Part 2
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
<img width="700" height="700" alt="ic_pot_icon" src="https://github.com/user-attachments/assets/d529af05-3864-4d6d-9544-d8ea9ec75543" />

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
    <td><img src="https://github.com/user-attachments/assets/6740e9a9-d215-433f-9c5e-6ef8b836d6ce" alt="Screenshot (336)" width="100%"/></td>
    <td><img src="https://github.com/user-attachments/assets/d154de22-5110-43b0-bdae-ca9d7101b49f" alt="Screenshot (337)" width="100%"/></td>
  </tr>
  <tr>
    <td colspan="2" align="center">
      <img src="https://github.com/user-attachments/assets/9d9eb35d-653f-4fdc-8cad-0b8fbaff3d15" alt="Screenshot (338)" width="50%"/>
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

<img width="1920" height="1080" alt="Screenshot (350)" src="https://github.com/user-attachments/assets/f11fc522-d22c-4c9f-ba69-d3813ea4c6f6" />

### Login Screen
The login screen is the secure gateway for returning users to access their personal accounts and saved content. Its core functionality is to authenticate a user's identity, typically through an email or username and a password. To enhance user convenience, modern login screens often include options for social media logins, allowing for quick access through existing accounts like Google. Additionally, a crucial feature is the "forgot password" link, which enables users to reset their password if they have forgotten it, ensuring they can regain access to their account without creating a new one.

<img width="1920" height="1080" alt="Screenshot (340)" src="https://github.com/user-attachments/assets/028bcdc4-3358-4289-8cdb-dbfc48572e69" />


### Register Screen
The registration, or sign-up, screen is designed for new users to create a personal account within the application. The primary function is to collect essential user information, which typically includes a name, email address, and a password for the new account. The goal is to make the sign-up process as quick and straightforward as possible to encourage user retention from the very beginning.

<img width="1920" height="1080" alt="Screenshot (341)" src="https://github.com/user-attachments/assets/3f414b41-2079-4644-b48d-dfbc747f6cf7" />


### Home Screen
The home screen is the central hub of the application, where users begin their journey and discover content. This screen is designed for easy navigation, providing clear pathways to the app's main features like recipe categories, the search function, and trending recipes. By presenting dynamic and interesting content, the home screen encourages users to explore the app further.

<img width="1920" height="1080" alt="Screenshot (342)" src="https://github.com/user-attachments/assets/146920bf-e77f-4f84-942d-437e4d9a36eb" />


### Recipe Detail Screen
The recipe detail screen provides all the necessary information for a user to successfully prepare a specific dish. It prominently displays the recipe's name, an appealing image, and a list of all required ingredients with their quantities. This screen offers clear, step-by-step cooking instructions. Additional functionalities often include the ability to save recipes to the saved recipes screen and to add ingredients to the shopping list.

<img width="1920" height="1080" alt="Screenshot (345)" src="https://github.com/user-attachments/assets/c82d22a6-b9dd-48d4-8ee0-27a3205b4268" />

### Community Screen 
The community section transforms the app from a solo experience into an interactive social platform. Its functionality revolves around enabling users to connect and engage with one another over their shared interest in cooking. This is achieved through features that allow users to share their own recipes, post photos of their culinary creations. Furthermore, users can interact by liking recipes, which helps to build a vibrant and supportive community of food lovers.

<img width="1920" height="1080" alt="Screenshot (343)" src="https://github.com/user-attachments/assets/37aa28c7-1b20-4c8c-b7bb-2effa82673a2" />

<img width="1920" height="1080" alt="Screenshot (344)" src="https://github.com/user-attachments/assets/9a8a7bfa-f914-4f32-bf2d-f665e0b1768d" />


### Shpping Screen
The shopping list feature is a practical tool designed to simplify grocery shopping for the user. Its core functionality is to allow users to add ingredients from any recipe to a digital list with a single tap. User can tick off the ingredients as they shop and later clear it when they done.

<img width="1920" height="1080" alt="Screenshot (346)" src="https://github.com/user-attachments/assets/31f77514-e94e-4c21-84f8-239c16191c2d" />

### Settings Screen
The settings screen is where users can customize their app experience to their personal preferences. This section allows users to view their profile, saved recipes inside profile, turning notifications on or off, change theme to light or dark, change the language to their preference and to safely logout.

<img width="1920" height="1080" alt="Screenshot (347)" src="https://github.com/user-attachments/assets/399bbc78-dd03-46fe-ae68-abedc57d2740" />

### Profile Screen
The user can see their full name and email. They can also view their saved recipes.

<img width="1920" height="1080" alt="Screenshot (348)" src="https://github.com/user-attachments/assets/10a91b62-72a1-43e1-b19e-f2552670f231" />


### Saved Recipes Screen
Users saved recipes are saved to the saved recipes screen for when they want to cook an specific recipe or also just for later view.

<img width="1920" height="1080" alt="Screenshot (349)" src="https://github.com/user-attachments/assets/5236676b-d78e-4950-ad9b-f8c0007fb10a" />



 ## 10. License
​This project is licensed under the MIT License. See the `LICENSE` file for more information.
