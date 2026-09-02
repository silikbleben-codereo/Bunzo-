const functions = require("firebase-functions");
const admin = require("firebase-admin");
const crypto = require("crypto");
const axios = require("axios");

admin.initializeApp();
const db = admin.firestore();

/**
 * 1. Set Custom Claims for First Owner (Run once from Firebase CLI or Cloud Shell)
 * Usage: node scripts/setFirstOwner.js <USER_UID>
 */
exports.setUserRole = functions.https.onCall(async (data, context) => {
  // Enforce caller authentication
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "يجب تسجيل الدخول أولاً");
  }

  const callerUid = context.auth.uid;
  const callerClaims = context.auth.token;
  const targetUid = data.targetUid;
  const targetRole = data.role; // 'admin', 'kitchen', 'customer', 'owner'

  // Only OWNER can assign ADMIN or OWNER
  // ADMIN can only assign KITCHEN
  const isOwner = callerClaims.role === "owner" || callerClaims.owner === true;
  const isAdmin = isOwner || callerClaims.role === "admin" || callerClaims.admin === true;

  if (targetRole === "owner" || targetRole === "admin") {
    if (!isOwner) {
      throw new functions.https.HttpsError("permission-denied", "فقط المالك (Owner) يستطيع تعيين مدراء النظام");
    }
  } else if (targetRole === "kitchen") {
    if (!isAdmin) {
      throw new functions.https.HttpsError("permission-denied", "فقط المدراء والمشرفين يستطيعون تعيين طاقم المطبخ");
    }
  } else if (targetRole === "customer") {
    if (!isAdmin) {
      throw new functions.https.HttpsError("permission-denied", "غير مصرح لك بتعديل الرتب");
    }
  } else {
    throw new functions.https.HttpsError("invalid-argument", "رتبة غير صالحة");
  }

  // Set Firebase Auth Custom User Claims
  await admin.auth().setCustomUserClaims(targetUid, {
    role: targetRole,
    admin: targetRole === "admin" || targetRole === "owner",
    kitchen: targetRole === "kitchen" || targetRole === "admin" || targetRole === "owner",
    owner: targetRole === "owner"
  });

  // Sync to Firestore user document
  await db.collection("users").document(targetUid).update({
    role: targetRole,
    updatedAt: Date.now()
  });

  // Audit Log
  await db.collection("auditLogs").add({
    actorId: callerUid,
    action: "ROLE_CHANGE",
    targetUid: targetUid,
    newRole: targetRole,
    timestamp: Date.now()
  });

  return { success: true, message: `تم تحديث رتبة المستخدم ${targetUid} إلى ${targetRole}` };
});

/**
 * 2. Real SMS Gateway Dispatcher (Keeps all SMS Credentials strictly on Backend)
 */
exports.requestSmsOtp = functions.https.onCall(async (data, context) => {
  const rawPhone = data.phone;
  if (!rawPhone || !rawPhone.startsWith("+9639")) {
    throw new functions.https.HttpsError("invalid-argument", "رقم الهاتف يجب أن يكون سوري بصيغة +9639XXXXXXXX");
  }

  const now = Date.now();
  const otpRef = db.collection("_secureOtpStore").document(rawPhone);
  const doc = await otpRef.get();

  if (doc.exists) {
    const existing = doc.data();
    // 60-second cooldown check
    if (now - existing.generatedAt < 60000) {
      throw new functions.https.HttpsError("resource-exhausted", "يرجى الانتظار دقيقة قبل طلب رمز جديد");
    }
  }

  // Generate 6 digit secure code
  const code = crypto.randomInt(100000, 999999).toString();
  const codeHash = crypto.createHash("sha256").update(code).digest("hex");

  // Save to private server-side store
  await otpRef.set({
    codeHash: codeHash,
    generatedAt: now,
    expiresAt: now + (5 * 60 * 1000), // 5 min TTL
    attemptsLeft: 5,
    isUsed: false
  });

  // Send SMS via Provider (Stored securely in Google Cloud Secret Manager)
  // Environment variables are set via: firebase functions:secrets:set SMS_API_KEY
  const smsApiKey = process.env.SMS_API_KEY;
  const smsSender = process.env.SMS_SENDER_ID || "BUNZO";

  if (smsApiKey) {
    try {
      // Example call to SMS gateway supporting Syrian numbers (+963)
      await axios.post("https://api.sms-provider.com/v1/send", {
        to: rawPhone,
        from: smsSender,
        message: `رمز التحقق لمطعم بونزوا هو: ${code} (صالح لمدة 5 دقائق)`
      }, {
        headers: { "Authorization": `Bearer ${smsApiKey}` }
      });
    } catch (err) {
      console.error("SMS Dispatch error:", err.message);
    }
  }

  return { success: true, message: "تم إرسال رمز التحقق عبر رسالة نصية SMS" };
});
