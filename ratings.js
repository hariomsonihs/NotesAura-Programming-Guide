// Firebase configuration
const firebaseConfig = {
    apiKey: "AIzaSyBkWGQHXyTaWJaOaih_wkQZhKJYJxJKKjE",
    authDomain: "notesaura-b8c5b.firebaseapp.com",
    projectId: "notesaura-b8c5b",
    storageBucket: "notesaura-b8c5b.appspot.com",
    messagingSenderId: "123456789",
    appId: "1:123456789:web:abcdef123456"
};

firebase.initializeApp(firebaseConfig);
const db = firebase.firestore();

let allRatings = [];
let currentTab = 'all';

// Load ratings on page load
document.addEventListener('DOMContentLoaded', function() {
    loadRatings();
    loadCourses();
});

async function loadRatings() {
    try {
        const snapshot = await db.collection('course_ratings').orderBy('timestamp', 'desc').get();
        allRatings = [];
        
        snapshot.forEach(doc => {
            allRatings.push({
                id: doc.id,
                ...doc.data()
            });
        });
        
        displayRatings();
    } catch (error) {
        console.error('Error loading ratings:', error);
    }
}

async function loadCourses() {
    try {
        const snapshot = await db.collection('courses').get();
        const courseFilter = document.getElementById('courseFilter');
        
        snapshot.forEach(doc => {
            const option = document.createElement('option');
            option.value = doc.id;
            option.textContent = doc.data().title;
            courseFilter.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading courses:', error);
    }
}

function showTab(tab) {
    currentTab = tab;
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    event.target.classList.add('active');
    displayRatings();
}

function displayRatings() {
    const container = document.getElementById('ratingsContainer');
    let filteredRatings = [...allRatings];
    
    if (currentTab === 'course') {
        filteredRatings = groupByCourse(filteredRatings);
    } else if (currentTab === 'user') {
        filteredRatings = groupByUser(filteredRatings);
    }
    
    container.innerHTML = '';
    
    if (currentTab === 'all') {
        filteredRatings.forEach(rating => {
            container.appendChild(createRatingCard(rating));
        });
    } else {
        Object.keys(filteredRatings).forEach(key => {
            const groupDiv = document.createElement('div');
            groupDiv.innerHTML = `<h3 style="margin: 20px 0 10px 0; color: #333;">${key}</h3>`;
            container.appendChild(groupDiv);
            
            filteredRatings[key].forEach(rating => {
                container.appendChild(createRatingCard(rating));
            });
        });
    }
}

function groupByCourse(ratings) {
    return ratings.reduce((groups, rating) => {
        const courseId = rating.courseId || 'Unknown Course';
        if (!groups[courseId]) groups[courseId] = [];
        groups[courseId].push(rating);
        return groups;
    }, {});
}

function groupByUser(ratings) {
    return ratings.reduce((groups, rating) => {
        const userName = rating.userName || 'Anonymous';
        if (!groups[userName]) groups[userName] = [];
        groups[userName].push(rating);
        return groups;
    }, {});
}

function createRatingCard(rating) {
    const card = document.createElement('div');
    card.className = 'rating-card';
    
    const stars = '★'.repeat(rating.rating) + '☆'.repeat(5 - rating.rating);
    const date = new Date(rating.timestamp).toLocaleDateString();
    
    card.innerHTML = `
        <div class="rating-header">
            <div>
                <strong>${rating.userName || 'Anonymous'}</strong>
                <div class="stars">${stars}</div>
            </div>
            <div class="rating-meta">
                Course: ${rating.courseId}<br>
                Date: ${date}
            </div>
        </div>
        
        ${rating.comment ? `<div class="rating-comment">"${rating.comment}"</div>` : ''}
        
        <div class="reply-section">
            ${rating.adminReply ? `
                <div class="existing-reply">
                    <strong>Admin Reply:</strong><br>
                    ${rating.adminReply}
                </div>
            ` : `
                <textarea class="reply-input" placeholder="Write your reply..." id="reply-${rating.id}"></textarea>
                <button class="reply-btn" onclick="sendReply('${rating.id}', '${rating.userId}')">Send Reply</button>
            `}
        </div>
    `;
    
    return card;
}

async function sendReply(ratingId, userId) {
    const replyText = document.getElementById(`reply-${ratingId}`).value.trim();
    
    if (!replyText) {
        alert('Please enter a reply message');
        return;
    }
    
    try {
        // Update rating with admin reply
        await db.collection('course_ratings').doc(ratingId).update({
            adminReply: replyText,
            replyTimestamp: Date.now()
        });
        
        // Send notification to user
        await sendNotificationToUser(userId, replyText, ratingId);
        
        alert('Reply sent successfully!');
        loadRatings(); // Refresh the display
        
    } catch (error) {
        console.error('Error sending reply:', error);
        alert('Error sending reply');
    }
}

async function sendNotificationToUser(userId, replyText, ratingId) {
    const notificationData = {
        userId: userId,
        title: 'Admin Reply to Your Rating',
        message: 'Admin has replied to your course rating',
        type: 'rating_reply',
        data: {
            ratingId: ratingId,
            replyText: replyText
        },
        timestamp: Date.now(),
        read: false
    };
    
    try {
        await db.collection('notifications').add(notificationData);
        console.log('Notification sent to user:', userId);
    } catch (error) {
        console.error('Error sending notification:', error);
    }
}

function applyFilters() {
    const courseFilter = document.getElementById('courseFilter').value;
    const ratingFilter = document.getElementById('ratingFilter').value;
    
    let filtered = [...allRatings];
    
    if (courseFilter) {
        filtered = filtered.filter(rating => rating.courseId === courseFilter);
    }
    
    if (ratingFilter) {
        filtered = filtered.filter(rating => rating.rating == ratingFilter);
    }
    
    allRatings = filtered;
    displayRatings();
}