# 💳 Payment Testing Guide

## 🚨 **Razorpay Test Mode Issues:**

### **Problem:**
- Razorpay test mode shows fake UPI ID: `upi@razorpay`
- Real UPI apps don't recognize this fake ID
- Payment fails but app shows success

### **Solution:**

## ✅ **Working Test Methods:**

### **1. Test Credit/Debit Cards (RECOMMENDED):**
```
Card Number: 4111 1111 1111 1111
CVV: 123
Expiry: 12/25
Name: Test User
Result: Always SUCCESS
```

### **2. Test NetBanking:**
```
Select: Any Bank (HDFC, SBI, etc.)
Username: test
Password: test
Result: Always SUCCESS
```

### **3. Test Wallets:**
```
Select: Paytm/PhonePe Wallet
Amount: Auto-filled
Result: Always SUCCESS
```

### **4. Demo Payment Button:**
```
Click: "✅ Test Payment (Always Success)"
Result: Instant SUCCESS + Course Enrollment
```

## 🎯 **Testing Steps:**

1. **Click "Pay Now - Razorpay"**
2. **Select "Cards" (NOT UPI)**
3. **Enter test card details**
4. **Click Pay**
5. **Payment SUCCESS**
6. **Course Enrolled**

## 💡 **For Real Payments:**

### **Live Mode Setup:**
1. Complete KYC in Razorpay
2. Add real bank account
3. Switch to Live Mode
4. Replace test key with live key
5. Enable UPI for real payments

### **Current Status:**
- **Test Mode**: Cards/NetBanking work perfectly
- **UPI**: Disabled in test mode (fake ID issue)
- **Demo Button**: Always works for testing

## 🚀 **Recommendation:**
**Use Test Cards for realistic payment testing!**

Card payments work exactly like real payments in test mode.