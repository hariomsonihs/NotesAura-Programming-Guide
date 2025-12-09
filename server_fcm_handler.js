// Server-side FCM Handler (Node.js/Express)
// Add this to your web admin panel backend

const admin = require('firebase-admin');

// Initialize Firebase Admin SDK
const serviceAccount = require('./path/to/your/service-account-key.json');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: 'https://your-project-id.firebaseio.com'
});

// FCM notification endpoint
app.post('/send-fcm-notification', async (req, res) => {
    try {
        const { notification, data, topic } = req.body;
        
        console.log('Sending FCM notification:', { notification, data, topic });
        
        const message = {
            notification: {
                title: notification.title,
                body: notification.body
            },
            data: {
                title: data.title,
                message: data.message,
                type: data.type,
                targetId: data.targetId || '',
                priority: data.priority || 'high'
            },
            topic: topic || 'all_users',
            android: {
                priority: 'high',
                notification: {
                    channelId: 'notesaura_notifications',
                    priority: 'max',
                    defaultSound: true,
                    defaultVibrateTimings: true
                }
            }
        };
        
        const response = await admin.messaging().send(message);
        console.log('FCM notification sent successfully:', response);
        
        res.json({ success: true, messageId: response });
        
    } catch (error) {
        console.error('Error sending FCM notification:', error);
        res.status(500).json({ success: false, error: error.message });
    }
});

// Alternative: Send to specific user
app.post('/send-fcm-to-user', async (req, res) => {
    try {
        const { userId, notification, data } = req.body;
        
        // Get user's FCM token from Firestore
        const userDoc = await admin.firestore().collection('users').doc(userId).get();
        const fcmToken = userDoc.data()?.fcmToken;
        
        if (!fcmToken) {
            return res.status(400).json({ success: false, error: 'User FCM token not found' });
        }
        
        const message = {
            notification: notification,
            data: data,
            token: fcmToken,
            android: {
                priority: 'high',
                notification: {
                    channelId: 'notesaura_notifications',
                    priority: 'max'
                }
            }
        };
        
        const response = await admin.messaging().send(message);
        res.json({ success: true, messageId: response });
        
    } catch (error) {
        console.error('Error sending FCM to user:', error);
        res.status(500).json({ success: false, error: error.message });
    }
});

// Bulk notification sender
app.post('/send-bulk-notifications', async (req, res) => {
    try {
        const { title, message, type, targetId } = req.body;
        
        // Get all users with FCM tokens
        const usersSnapshot = await admin.firestore()
            .collection('users')
            .where('fcmToken', '!=', null)
            .get();
        
        const tokens = [];
        const batch = admin.firestore().batch();
        
        usersSnapshot.forEach(doc => {
            const userData = doc.data();
            if (userData.fcmToken) {
                tokens.push(userData.fcmToken);
            }
            
            // Save notification to database
            const notificationRef = admin.firestore().collection('app_notifications').doc();
            batch.set(notificationRef, {
                userId: doc.id,
                title: title,
                message: message,
                type: type,
                targetId: targetId,
                timestamp: Date.now(),
                isRead: false,
                platform: 'android'
            });
        });
        
        // Save all notifications to database
        await batch.commit();
        
        // Send FCM to all tokens
        if (tokens.length > 0) {
            const fcmMessage = {
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
                tokens: tokens,
                android: {
                    priority: 'high',
                    notification: {
                        channelId: 'notesaura_notifications',
                        priority: 'max'
                    }
                }
            };
            
            const response = await admin.messaging().sendMulticast(fcmMessage);
            console.log('Bulk FCM sent:', response.successCount, 'success,', response.failureCount, 'failed');
        }
        
        res.json({ 
            success: true, 
            notificationsSaved: usersSnapshot.size,
            fcmSent: tokens.length 
        });
        
    } catch (error) {
        console.error('Error sending bulk notifications:', error);
        res.status(500).json({ success: false, error: error.message });
    }
});

module.exports = { admin };