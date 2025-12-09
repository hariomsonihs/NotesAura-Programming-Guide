# Testing Notification System

## How to Test

### 1. **Setup FCM Token**
- Open mobile app and login
- FCM token will be automatically saved to user document
- User will be subscribed to "all_users" topic

### 2. **Send Test Notification**
- Go to web admin panel → Notifications
- Click "Send Notification"
- Fill details:
  - Title: "Test Notification"
  - Message: "This is a test message"
  - Recipients: "All Users" or select specific user
- Click Send

### 3. **Check Results**
- **Mobile App**: Check notification icon for badge
- **Mobile App**: Go to side menu → Notifications
- **Push Notification**: Should appear on device notification bar

## Troubleshooting

### If notifications not appearing:

1. **Check FCM Token**:
   ```javascript
   // In Firebase Console → Firestore → users collection
   // Each user document should have "fcmToken" field
   ```

2. **Check Collections**:
   - `admin_notifications`: Admin sent notifications
   - `app_notifications`: User-specific notifications for mobile app

3. **Check Cloud Functions**:
   - Deploy functions: `firebase deploy --only functions`
   - Check logs: `firebase functions:log`

4. **Test Push Notification**:
   - Use Firebase Console → Cloud Messaging
   - Send test message to "all_users" topic

## Expected Flow

1. **Admin sends notification** → `admin_notifications` collection
2. **Cloud Function triggers** → Creates `app_notifications` for users
3. **Cloud Function sends FCM** → Push notification to devices
4. **Mobile app receives** → Shows badge and in-app notification
5. **User opens app** → Sees notifications in notification center

## Collections Structure

### admin_notifications
```json
{
  "title": "Test Notification",
  "message": "Test message",
  "type": "custom",
  "recipientType": "all",
  "sentAt": "timestamp",
  "sentBy": "admin@example.com"
}
```

### app_notifications
```json
{
  "title": "Test Notification", 
  "message": "Test message",
  "type": "custom",
  "userId": "user_uid",
  "timestamp": "timestamp",
  "isRead": false
}
```