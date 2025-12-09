# 🚀 Razorpay Complete Setup Guide

## Step 1: Add Dependency
**File: `app/build.gradle`**
```gradle
dependencies {
    // Add this line
    implementation 'com.razorpay:checkout:1.6.26'
    
    // Your existing dependencies...
}
```

## Step 2: Replace Test Key
**File: `PaymentActivity.java` (Line 120)**
```java
// Replace this line:
checkout.setKeyID("rzp_test_XXXXXXXXXXXXXXXXX");

// With your actual key:
checkout.setKeyID("YOUR_COPIED_TEST_KEY_HERE");
```

## Step 3: Test Cards (FREE Testing)

### Credit/Debit Cards:
- **Card Number**: 4111 1111 1111 1111
- **CVV**: 123
- **Expiry**: 12/25 (any future date)
- **Name**: Any name

### UPI Testing:
- **UPI ID**: success@razorpay (always success)
- **UPI ID**: failure@razorpay (always fails)

### NetBanking:
- Select any bank
- Use test credentials provided

## Step 4: Go Live (When Ready)
1. Complete KYC in Razorpay dashboard
2. Switch to Live Mode
3. Replace test key with live key
4. Start accepting real payments!

## 💰 Charges:
- **Testing**: FREE (unlimited)
- **Live**: 2% + GST (only on successful payments)
- **Setup**: FREE
- **Monthly**: FREE

## 🎯 Test Flow:
1. Click "Pay Now - Razorpay"
2. Select payment method
3. Use test credentials
4. Payment will succeed
5. Course will be enrolled automatically

**Ready to test! 🎉**