# NotesAura Programming Guide

A comprehensive Android learning platform application built with Java and Firebase, featuring modern UI design, course management, and interactive learning content.

## Features

### 🔐 Authentication System
- Firebase Authentication integration
- Email/Password login and registration
- Password reset functionality
- Session management

### 🏠 Modern Home Screen
- App logo and branding
- Search functionality
- Horizontal scrolling categories
- Featured and recent courses
- Modern card-based design with gradients

### 📚 Course Management
- Detailed course pages
- HTML-based learning content
- Exercise tracking
- Progress monitoring
- Enrollment system

### 🧭 Navigation
- Bottom navigation (Home, Categories, Profile, Settings)
- Navigation drawer with additional options
- Smooth transitions and animations

### 👤 User Profile
- Personal information display
- Course progress tracking
- Achievement system
- Profile editing capabilities

### 🎨 Modern UI/UX
- Gradient backgrounds and buttons
- Card-based layouts with shadows
- Smooth animations and transitions
- Dark/Light mode support
- Material Design components

## Technical Stack

- **Language**: Java
- **Platform**: Android (API 24+)
- **Database**: Firebase Firestore
- **Authentication**: Firebase Auth
- **Storage**: Firebase Storage
- **UI Components**: Material Design Components
- **Image Loading**: Glide
- **Animations**: Lottie

## Project Structure

```
app/
├── src/main/
│   ├── java/com/hariomsonihs/notesaura/
│   │   ├── activities/          # Activity classes
│   │   ├── adapters/           # RecyclerView adapters
│   │   ├── fragments/          # Fragment classes
│   │   ├── models/             # Data model classes
│   │   ├── utils/              # Utility classes
│   │   └── interfaces/         # Callback interfaces
│   ├── res/
│   │   ├── layout/             # XML layouts
│   │   ├── drawable/           # Vector drawables and shapes
│   │   ├── values/             # Colors, strings, styles
│   │   └── menu/               # Navigation menus
│   └── assets/courses/         # HTML course content
```

## Setup Instructions

### 1. Prerequisites
- Android Studio Arctic Fox or later
- Java 11 or higher
- Firebase project setup

### 2. Firebase Setup
1. Create a new Firebase project at [Firebase Console](https://console.firebase.google.com)
2. Add an Android app with package name: `com.hariomsonihs.notesaura`
3. Download `google-services.json` and place it in the `app/` directory
4. Enable Authentication with Email/Password
5. Create Firestore database
6. Set up Firebase Storage

### 3. Clone and Build
```bash
git clone <repository-url>
cd NotesAuraProgrammingGuide
```

### 4. Open in Android Studio
1. Open Android Studio
2. Select "Open an existing project"
3. Navigate to the project directory
4. Wait for Gradle sync to complete

### 5. Run the Application
1. Connect an Android device or start an emulator
2. Click "Run" or press Shift+F10
3. The app will install and launch

## Course Content

The app includes sample HTML-based course content in the `assets/courses/` directory:

- **Programming**: Java, C, Python courses
- **Web Development**: HTML/CSS, JavaScript
- **App Development**: Android, Flutter
- **Data Science**: Machine Learning, Data Visualization
- **Cheat Sheets**: Quick reference guides

### Adding New Courses

1. Create HTML files in `assets/courses/[category]/[course_name]/`
2. Use the provided HTML template with embedded CSS
3. Update the course data in the respective fragments/activities

## Key Components

### Activities
- `SplashActivity`: App launch screen
- `AuthActivity`: Login/Registration
- `MainActivity`: Main container with navigation
- `CourseDetailActivity`: Course information and exercises
- `ExerciseActivity`: HTML content viewer

### Fragments
- `HomeFragment`: Dashboard with featured content
- `CategoriesFragment`: Course categories grid
- `ProfileFragment`: User profile and statistics
- `SettingsFragment`: App preferences

### Adapters
- `CourseAdapter`: Course cards display
- `CategoryAdapter`: Category items
- `ExerciseAdapter`: Exercise lists

## Customization

### Colors and Themes
Edit `res/values/colors.xml` and `res/values/styles.xml` to customize the app's appearance.

### Course Content
Add new HTML files to `assets/courses/` and update the data loading logic in fragments.

### Firebase Configuration
Update Firebase rules and collections as needed for your specific requirements.

## Dependencies

Key dependencies used in this project:

```gradle
// Firebase
implementation platform('com.google.firebase:firebase-bom:32.7.0')
implementation 'com.google.firebase:firebase-auth'
implementation 'com.google.firebase:firebase-firestore'
implementation 'com.google.firebase:firebase-storage'

// UI Components
implementation 'com.google.android.material:material:1.10.0'
implementation 'androidx.navigation:navigation-fragment:2.7.6'
implementation 'androidx.navigation:navigation-ui:2.7.6'

// Image Loading
implementation 'com.github.bumptech.glide:glide:4.16.0'
implementation 'de.hdodenhof:circleimageview:3.1.0'

// Animations
implementation 'com.airbnb.android:lottie:6.2.0'
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions, please open an issue in the GitHub repository.

---

**NotesAura Programming Guide** - Learn programming with style! 🚀