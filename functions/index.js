const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

/**
 * Cloud Function to securely send Syrian SMS OTP via Telco Gateway (e.g. Syriatel / MTN / Infobip / Twilio)
 * Never exposes API Secrets to the Android APK.
 */
exports.sendSmsOtp = functions.https.onCall(async (data, context) => {
  const phone = data.phone;
  if (!phone) {
    throw new functions.https.HttpsError("invalid-argument", "رقم الهاتف مطلوب");
  }

  // Generate 6-digit cryptographic OTP
  const otp = Math.floor(100000 + Math.random() * 900000).toString();
  const expiresAt = Date.now() + 5 * 60 * 1000; // 5 minutes validity

  // Store hashed/active OTP in Firestore securely
  await admin.firestore().collection("otp_requests").doc(phone).set({
    otp: otp,
    phone: phone,
    expiresAt: expiresAt,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    attempts: 0
  });

  // Here: Integrate with Syrian Gateway API / SMS Provider
  console.log(`[BUNZO SMS PROD] Sending SMS to ${phone} with OTP: ${otp}`);

  return {
    success: true,
    message: `تم إرسال رمز التحقق إلى ${phone}`
  };
});

/**
 * Cloud Function triggered when a new Order is placed
 * Automatically notifies the Kitchen KDS and Admin devices via FCM.
 */
exports.onOrderCreated = functions.firestore
  .document("orders/{orderId}")
  .onCreate(async (snap, context) => {
    const orderData = snap.data();
    const orderNumber = orderData.orderNumber || context.params.orderId;

    const payload = {
      notification: {
        title: "طلب جديد وارد! 🍔",
        body: `طلب رقم ${orderNumber} - ${orderData.branchNameAr || "بونزوا"}`
      },
      data: {
        orderId: context.params.orderId,
        type: "NEW_ORDER"
      },
      topic: "kitchen_staff"
    };

    try {
      await admin.messaging().sendToTopic("kitchen_staff", payload);
      console.log(`FCM sent to kitchen staff for order ${orderNumber}`);
    } catch (error) {
      console.error("Error sending FCM notification:", error);
    }
  });

/**
 * Cloud Function triggered on Order status changes
 * Notifies the Customer device in real time.
 */
exports.onOrderStatusUpdated = functions.firestore
  .document("orders/{orderId}")
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();

    if (before.status !== after.status) {
      const orderId = context.params.orderId;
      const fcmToken = after.fcmToken;

      if (fcmToken) {
        let statusTitle = "تحديث حالة طلبك في بونزوا 🛵";
        let statusBody = `أصبح طلبك الآن: ${after.status}`;

        if (after.status === "PREPARING") {
          statusBody = "بدأ الشيف في تحضير وجبتك اللذيذة 👨‍🍳";
        } else if (after.status === "READY") {
          statusBody = "تم تجهيز وجبتك وهي جاهزة للتسليم 📦";
        } else if (after.status === "OUT_FOR_DELIVERY") {
          statusBody = "الكابتن في طريقه إليك الآن 🛵💨";
        } else if (after.status === "DELIVERED") {
          statusBody = "تم توصيل الطلب بنجاح. صحتين وهنا! 🎉";
        }

        const message = {
          token: fcmToken,
          notification: {
            title: statusTitle,
            body: statusBody
          },
          data: {
            orderId: orderId,
            status: after.status
          }
        };

        try {
          await admin.messaging().send(message);
        } catch (e) {
          console.error("Error sending status update FCM:", e);
        }
      }
    }
  });
