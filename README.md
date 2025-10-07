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
<<img width="600" height="600" alt="Screenshot 2025-10-07 142911" src="https://github.com/user-attachments/assets/5b350e52-5b22-4bd4-af1d-8b0c3b22385f" />




 ## 10. License
​This project is licensed under the MIT License. See the `LICENSE` file for more information.
