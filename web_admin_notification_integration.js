// Web Admin Panel Notification Integration
// Add this JavaScript code to your web admin panel

// Firebase configuration (use your project config)
const firebaseConfig = {
    // Your Firebase config here
};

// Initialize Firebase (if not already done)
// firebase.initializeApp(firebaseConfig);
// const db = firebase.firestore();

// Notification sender function
async function sendNotificationToApp(title, message, type, targetId = null) {
    try {
        console.log('Sending notification:', { title, message, type, targetId });
        
        // 1. Save notification to database for all users
        const users = await db.collection('users').get();
        const batch = db.batch();
        
        users.forEach(userDoc => {
            const notificationRef = db.collection('app_notifications').doc();
            batch.set(notificationRef, {
                userId: userDoc.id,
                title: title,
                message: message,
                type: type,
                targetId: targetId,
                timestamp: Date.now(),
                isRead: false,
                platform: 'android'
            });
        });
        
        await batch.commit();
        console.log('Notifications saved to database');
        
        // 2. Send FCM notification (you'll need to implement server-side FCM)
        await sendFCMNotification(title, message, type, targetId);
        
        // Show success message
        showNotificationSuccess('Notification sent successfully!');
        
    } catch (error) {
        console.error('Error sending notification:', error);
        showNotificationError('Failed to send notification: ' + error.message);
    }
}

// FCM notification sender (requires server-side implementation)
async function sendFCMNotification(title, message, type, targetId) {
    const payload = {
        notification: {
            title: title,
            body: message
        },
        data: {
            title: title,
            message: message,
            type: type,
            targetId: targetId || '',
            priority: 'high'
        },
        topic: 'all_users'
    };
    
    // Send to your server endpoint that handles FCM
    try {
        const response = await fetch('/send-fcm-notification', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(payload)
        });
        
        if (response.ok) {
            console.log('FCM notification sent successfully');
        } else {
            console.error('Failed to send FCM notification');
        }
    } catch (error) {
        console.error('FCM send error:', error);
    }
}

// Course operations
function onCourseAdded(courseId, courseName) {
    sendNotificationToApp(
        '🚀 New Course Added!',
        `Check out the new course: ${courseName}`,
        'course',
        courseId
    );
}

function onCourseUpdated(courseId, courseName) {
    sendNotificationToApp(
        '📝 Course Updated!',
        `${courseName} has been updated with new content`,
        'course',
        courseId
    );
}

function onExerciseAdded(courseId, courseName) {
    sendNotificationToApp(
        '📋 New Exercises Added!',
        `New exercises added to ${courseName}`,
        'exercise',
        courseId
    );
}

// Ebook operations
function onEbookAdded(ebookId, ebookName) {
    sendNotificationToApp(
        '📚 New Ebook Added!',
        `New ebook available: ${ebookName}`,
        'ebook',
        ebookId
    );
}

function onEbookUpdated(ebookId, ebookName) {
    sendNotificationToApp(
        '📖 Ebook Updated!',
        `${ebookName} has been updated`,
        'ebook',
        ebookId
    );
}

// Interview operations
function onInterviewQuestionsAdded(categoryId, categoryName) {
    sendNotificationToApp(
        '🎯 New Interview Questions!',
        `Fresh questions added for ${categoryName}`,
        'interview',
        categoryId
    );
}

// Practice operations
function onPracticeAdded(practiceId, practiceName) {
    sendNotificationToApp(
        '💻 New Practice Added!',
        `New practice exercises: ${practiceName}`,
        'practice',
        practiceId
    );
}

// Custom notification
function sendCustomNotification(title, message) {
    sendNotificationToApp(title, message, 'custom', null);
}

// UI Helper functions
function showNotificationSuccess(message) {
    // Show success toast/alert
    alert('✅ ' + message);
}

function showNotificationError(message) {
    // Show error toast/alert
    alert('❌ ' + message);
}

// Example usage in your admin panel forms:
/*
// When course form is submitted:
document.getElementById('courseForm').addEventListener('submit', function(e) {
    // ... your existing course save logic ...
    
    // After successful save:
    if (isNewCourse) {
        onCourseAdded(courseId, courseName);
    } else {
        onCourseUpdated(courseId, courseName);
    }
});

// When ebook form is submitted:
document.getElementById('ebookForm').addEventListener('submit', function(e) {
    // ... your existing ebook save logic ...
    
    // After successful save:
    if (isNewEbook) {
        onEbookAdded(ebookId, ebookName);
    } else {
        onEbookUpdated(ebookId, ebookName);
    }
});
*/