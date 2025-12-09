# 🚀 Razorpay Live Mode Setup Guide

## 📋 **Step 1: Razorpay Dashboard Changes**

### **A. Complete KYC:**
```
1. Login to Razorpay Dashboard
2. Go to Account & Settings > KYC Details
3. Upload required documents:
   - PAN Card
   - Aadhaar Card
   - Bank Account Proof
   - Business Registration (if applicable)
4. Wait for approval (1-2 days)
```

### **B. Switch to Live Mode:**
```
1. Dashboard top-right corner
2. Toggle from "Test Mode" to "Live Mode"
3. Generate Live API Keys
4. Copy Live Key ID (rzp_live_xxxxxxxxx)
```

## 🔧 **Step 2: Code Changes**

### **A. Replace API Key:**
```java
// PaymentActivity.java में बदलें:

// Test Key (हटाएं):
checkout.setKeyID("rzp_test_1DP5mmOlF5G5ag");

// Live Key (लगाएं):
checkout.setKeyID("rzp_live_YOUR_LIVE_KEY_HERE");
```

### **B. Enable All Payment Methods:**
```java
// UPI को वापस enable करें:
JSONObject method = new JSONObject();
method.put("netbanking", true);
method.put("card", true);
method.put("upi", true);        // ✅ Enable करें
method.put("wallet", true);
options.put("method", method);
```

### **C. Remove Test Button:**
```java
// Test payment button को hide करें या remove करें
Button testPaymentButton = findViewById(R.id.test_payment_button);
testPaymentButton.setVisibility(View.GONE); // Hide करें
```

## 💰 **Step 3: Business Setup**

### **A. Bank Account:**
```
1. Business bank account add करें
2. Account verification complete करें
3. Settlement schedule set करें (Daily/Weekly)
```

### **B. Webhook Setup (Optional):**
```
1. Dashboard > Settings > Webhooks
2. Add webhook URL: https://yourserver.com/webhook
3. Select events: payment.captured, payment.failed
4. This ensures automatic payment verification
```

## 🎯 **Step 4: Testing Live Payments**

### **A. Small Amount Test:**
```
1. ₹1 की payment test करें
2. Real UPI/Card use करें
3. Money actually deduct होगा
4. Settlement account में आएगा
```

### **B. Refund Test:**
```
1. Dashboard से refund initiate करें
2. Money वापस customer को जाएगा
3. Process verify करें
```

## 📱 **Step 5: App Updates**

### **A. Remove Debug Features:**
```java
// Debug logs remove करें
// Test buttons hide करें
// Error messages को user-friendly बनाएं
```

### **B. Add Production Features:**
```java
// Real customer support contact
// Proper error handling
// Receipt generation
// Order tracking
```

## 💳 **Live Mode Benefits:**

### **Real Payments:**
- ✅ All UPI apps work
- ✅ Real cards accepted
- ✅ All banks supported
- ✅ Wallets integrated

### **Real Money Flow:**
- Customer pays → Razorpay → Your account
- Automatic settlements
- Real transaction IDs
- Proper receipts

## 🔒 **Security Checklist:**

### **A. API Key Security:**
```
❌ Don't commit live keys to Git
❌ Don't share keys publicly
✅ Store in secure environment variables
✅ Use different keys for different environments
```

### **B. Webhook Security:**
```
✅ Verify webhook signatures
✅ Use HTTPS only
✅ Validate payment status from server
```

## 💰 **Cost Structure:**

### **Live Charges:**
- **Domestic Cards**: 2% + GST
- **UPI**: 0% (Free till certain limit)
- **NetBanking**: 2% + GST
- **Wallets**: 2% + GST
- **International**: 3% + GST

### **Settlement:**
- **T+1**: Next day settlement
- **Instant**: Available for extra fee
- **No setup fee**: Only transaction charges

## 🚀 **Go Live Checklist:**

```
☐ KYC completed and approved
☐ Live API keys generated
☐ Code updated with live keys
☐ Test button removed/hidden
☐ UPI re-enabled
☐ Small amount tested
☐ Bank account verified
☐ Webhook configured (optional)
☐ Customer support ready
☐ App published to Play Store
```

## 📞 **Support:**
- **Razorpay Support**: support@razorpay.com
- **Phone**: 080-68727374
- **Dashboard**: Live chat available

**Live mode में जाने के बाद real payments start हो जाएंगी!** 💰