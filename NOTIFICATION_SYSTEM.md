# Notification Management System

## Overview
Complete notification management system for NotesAura admin panel with push notification support.

## Features

### 🔔 Custom Notifications
- Send notifications to all users or specific users
- Rich text messages with custom icons
- Action URLs for deep linking
- Real-time delivery via Firebase Cloud Messaging

### 📊 Notification History
- View all sent notifications with full details
- Filter by notification type (Custom, Course, E-book, Interview)
- Track delivery status and recipient information
- Detailed notification analytics

### 🎯 Targeting Options
- **All Users**: Send to everyone with the app installed
- **Specific User**: Target individual users by selection

### 📱 Mobile Integration
- Automatic badge indicators for unread notifications
- In-app notification center with swipe refresh
- Clear all functionality
- Proper navigation handling when notifications are clicked

## Usage

### Sending Custom Notifications
1. Go to **Notifications** in the admin panel
2. Click **Send Notification**
3. Fill in the notification details:
   - **Title**: Short, descriptive title
   - **Message**: Detailed message (max 500 characters)
   - **Recipients**: Choose "All Users" or "Specific User"
   - **Icon**: Optional custom icon URL
   - **Action URL**: Optional URL to open when clicked
4. Click **Send Notification**

### Viewing Notification History
- All sent notifications are displayed in a table
- Use the filter dropdown to view specific types
- Click the eye icon to view full notification details
- Refresh button to reload the latest data

## Technical Implementation

### Database Collections
- `admin_notifications`: Stores admin-sent notifications with metadata
- `app_notifications`: Stores notifications for mobile app display
- `users`: Contains FCM tokens for push notification delivery

### Firebase Cloud Functions
- `sendAdminNotification`: Automatically sends push notifications when admin creates them
- Handles both topic-based (all users) and token-based (specific user) messaging

### Mobile App Integration
- `NotificationService`: Handles incoming FCM messages
- `NotificationsActivity`: Displays notification history with UI controls
- Badge system in `MainActivity` for unread notification indicators

## Configuration

### FCM Setup
1. Ensure Firebase Cloud Messaging is enabled in your project
2. Update the server key in `notifications.js` (line 185)
3. Deploy the Cloud Functions to handle automatic push notifications

### Security Rules
Add these Firestore security rules:
```javascript
// Admin notifications (admin write, admin read)
match /admin_notifications/{notificationId} {
  allow read, write: if request.auth != null && 
    get(/databases/$(database)/documents/admins/$(request.auth.uid)).data.role == 'admin';
}

// App notifications (system write, user read)
match /app_notifications/{notificationId} {
  allow read: if request.auth != null && resource.data.userId == request.auth.uid;
  allow write: if request.auth != null;
}
```

## Best Practices

### Message Guidelines
- Keep titles under 50 characters for better display
- Messages should be clear and actionable
- Use appropriate icons that match your app's design
- Test action URLs before sending

### Targeting
- Use "All Users" for important announcements
- Use "Specific User" for personalized messages
- Consider user preferences and notification frequency

### Monitoring
- Regularly check notification history for delivery status
- Monitor user engagement with action URLs
- Track notification open rates through analytics

## Troubleshooting

### Common Issues
1. **Notifications not delivering**: Check FCM server key configuration
2. **Badge not updating**: Ensure `updateNotificationBadge()` is called after data changes
3. **Action URLs not working**: Verify URL format and app deep link handling

### Debug Steps
1. Check Firebase Console for FCM delivery logs
2. Verify user FCM tokens are being stored correctly
3. Test with a single user before sending to all users
4. Check device notification permissions

## Future Enhancements
- Scheduled notifications
- Rich media support (images, videos)
- Notification templates
- A/B testing for notification content
- Advanced analytics and reporting