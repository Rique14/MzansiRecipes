# Mzansi Recipes App - Part 2
## Group Members
- Princely Makhwara – ST10263265
- Aime Ndumuhire – ST10255663
- Fortunate Majere- ST10231459
- Sabelo Sibiya - ST10327016
- Enrique Arendse – ST10302006
## Video Link
## Link to Repository 

​
 Mzansi Recipes Logo <img width="1024" height="1024" alt="ic_pot_icon" src="https://github.com/user-attachments/assets/d529af05-3864-4d6d-9544-d8ea9ec75543" />

​
 ## 1. Introduction
​
 Mzansi Recipes is a mobile application dedicated to celebrating the rich and diverse culinary heritage of South Africa. It provides a platform for users to discover, share and enjoy authentic local recipes. The app is designed to be a community-driven space where food enthusiasts can connect, learn, and preserve the vibrant food culture of the nation.
​
 ## 2. Features
​
 - **User Authentication**: Secure user registration and login with encrypted password storage.
 - **Recipe Discovery**: Browse a wide variety of recipes from around the world using **The MealDB API**.
 - **User Settings**: Users can manage their profile and app settings.
 - **Community Engagement**: Share your own recipes and interacting with other users recipes by liking their recipes.
 - **Shopping List**: Easily create a shopping list, by adding recipe ingredients to your shoppinfg list.
 - **Offline Access**: Access your favorite recipes and shopping lists even without an internet connection.
​
 ## 3. Backend and API
​
 ### 3.1. Firebase
 The app uses **Firebase** as its backend for:
 - **Authentication**: Securely managing user accounts, including login and registration, with encrypted passwords.
 - **Cloud Firestore**: Storing user-generated data such as saved recipes, user profiles and community contributions.
​
 ### 3.2. The MealDB API
 For recipe content, the app is connected to **The MealDB**, a comprehensive database of recipes from around the world, accessed via RapidAPI. This allows users to discover a vast collection of dishes and cooking instructions.

 ## 4. Design Considerations
​
 The app is built with a focus on a clean, intuitive, and user-friendly interface. Key design principles include:
​
 - **Simplicity**: A minimalist design that makes it easy for users to navigate and find what they need.
 - **Consistency**: A consistent design language across all screens and components.
 - **Readability**: Clear and legible typography to ensure a pleasant reading experience.
 - **Visual Appeal**: High-quality images and a visually appealing layout to showcase the delicious food.
​
 ## 5. Tech Stack and Architecture
​
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

 ### 6.1. Continuous Integration (CI)
​
 We have a CI pipeline that runs on every push and pull request to the `main` branch. This pipeline performs the following tasks:
​
 - **Build the App**: Compiles the code and ensures that the app builds successfully.
 - **Run Tests**: Executes unit and instrumentation tests to ensure the correctness of the code.
​
 This helps us to maintain a high level of code quality and to catch any potential issues early in the development process.
​
 ### 6.2. Continuous Deployment (CD)
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
 5. Create a pull request to the `main` branch of the main repository.

## 8. Logs in Project
<img width="1920" height="1017" alt="Screenshot (324)" src="https://github.com/user-attachments/assets/d5cab443-e2ad-4e21-8537-32fd082a5e48" />

​<img width="1920" height="1080" alt="Screenshot (328)" src="https://github.com/user-attachments/assets/3f29b32c-bf60-4a5a-b08c-cad2781dc74a" />

<img width="1920" height="1080" alt="Screenshot (327)" src="https://github.com/user-attachments/assets/0364d52e-b3b3-4827-8f19-4e6e486dca48" />

 ## 9. License
​
 This project is licensed under the MIT License. See the `LICENSE` file for more information.
