const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

// Process FCM notification queue
exports.processFCMQueue = functions.firestore
    .document('fcm_queue/{queueId}')
    .onCreate(async (snap, context) => {
        const data = snap.data();
        
        if (data.processed) {
            return null;
        }
        
        try {
            const message = {
                token: data.token,
                notification: data.payload.notification,
                data: data.payload.data,
                android: {
                    priority: 'high',
                    notification: {
                        sound: 'default',
                        channelId: 'notesaura_notifications'
                    }
                }
            };
            
            const response = await admin.messaging().send(message);
            console.log('Successfully sent message:', response);
            
            // Mark as processed
            await snap.ref.update({ processed: true, response: response });
            
        } catch (error) {
            console.error('Error sending message:', error);
            await snap.ref.update({ processed: true, error: error.message });
        }
        
        return null;
    });

// Send notification when new course is added
exports.sendCourseNotification = functions.firestore
    .document('courses/{courseId}')
    .onCreate(async (snap, context) => {
        const courseData = snap.data();
        const courseId = context.params.courseId;
        
        try {
            // Get all users with FCM tokens
            const usersSnapshot = await admin.firestore().collection('users').get();
            const notifications = [];
            
            usersSnapshot.forEach(userDoc => {
                const userData = userDoc.data();
                if (userData.fcmToken) {
                    // Add to app_notifications collection
                    notifications.push(
                        admin.firestore().collection('app_notifications').add({
                            userId: userDoc.id,
                            title: 'New Course Available!',
                            message: `Check out our new course: ${courseData.title}`,
                            type: 'course',
                            targetId: courseId,
                            timestamp: admin.firestore.FieldValue.serverTimestamp(),
                            isRead: false
                        })
                    );
                    
                    // Queue FCM notification
                    notifications.push(
                        admin.firestore().collection('fcm_queue').add({
                            token: userData.fcmToken,
                            payload: {
                                notification: {
                                    title: 'New Course Available!',
                                    body: `Check out our new course: ${courseData.title}`
                                },
                                data: {
                                    type: 'course',
                                    targetId: courseId,
                                    title: 'New Course Available!',
                                    message: `Check out our new course: ${courseData.title}`
                                }
                            },
                            timestamp: Date.now(),
                            processed: false
                        })
                    );
                }
            });
            
            await Promise.all(notifications);
            console.log('Course notifications sent for:', courseData.title);
            
        } catch (error) {
            console.error('Error sending course notifications:', error);
        }
        
        return null;
    });

// Send notification for rating replies
exports.sendRatingReplyNotification = functions.firestore
    .document('course_ratings/{ratingId}')
    .onUpdate(async (change, context) => {
        const beforeData = change.before.data();
        const afterData = change.after.data();
        
        // Check if adminReplies array was updated
        const beforeReplies = beforeData.adminReplies || [];
        const afterReplies = afterData.adminReplies || [];
        
        if (afterReplies.length > beforeReplies.length) {
            // New reply added
            const newReply = afterReplies[afterReplies.length - 1];
            const userId = afterData.userId;
            
            try {
                // Get user's FCM token
                const userDoc = await admin.firestore().collection('users').doc(userId).get();
                const userData = userDoc.data();
                
                if (userData && userData.fcmToken) {
                    const title = 'Admin Reply to Your Rating';
                    const message = `Admin replied to your comment: "${(afterData.comment || 'your rating').substring(0, 40)}..." - ${newReply.message.substring(0, 30)}...`;
                    
                    // Add to app_notifications
                    await admin.firestore().collection('app_notifications').add({
                        userId: userId,
                        title: title,
                        message: message,
                        type: 'rating_reply',
                        targetId: context.params.ratingId,
                        timestamp: admin.firestore.FieldValue.serverTimestamp(),
                        isRead: false,
                        data: {
                            ratingId: context.params.ratingId,
                            replyText: newReply.message,
                            originalComment: afterData.comment,
                            courseId: afterData.courseId
                        }
                    });
                    
                    // Queue FCM notification
                    await admin.firestore().collection('fcm_queue').add({
                        token: userData.fcmToken,
                        payload: {
                            notification: {
                                title: title,
                                body: message
                            },
                            data: {
                                type: 'rating_reply',
                                targetId: context.params.ratingId,
                                title: title,
                                message: message
                            }
                        },
                        timestamp: Date.now(),
                        processed: false
                    });
                    
                    console.log('Rating reply notification sent to user:', userId);
                }
            } catch (error) {
                console.error('Error sending rating reply notification:', error);
            }
        }
        
        return null;
    });