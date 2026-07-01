const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

/**
 * عندما يضيف المشرف إشعارًا جديدًا في /notifications/{id},
 * ترسل هذه الدالة FCM Push Notification إلى جميع أجهزة التطبيق
 * (المشتركين في topic "all") حتى لو التطبيق مقفول.
 */
exports.sendNotificationOnWrite = functions.database
    .ref('/notifications/{notificationId}')
    .onWrite(async (change, context) => {
        const data = change.after.val();
        if (!data) return; // مسح — لا داعي للإرسال

        const title = data.title || 'معمل أبو إدريس';
        const body = data.body || '';
        const reportId = data.report_id || null;

        const message = {
            topic: 'all',
            notification: { title, body },
            data: {}
        };

        if (reportId) {
            message.data.report_id = reportId;
        }

        try {
            await admin.messaging().send(message);
            console.log('FCM sent successfully:', context.params.notificationId);
        } catch (err) {
            console.error('FCM send error:', err);
        }
    });
