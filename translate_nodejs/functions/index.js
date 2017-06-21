'use strict';

// [START all]
// [START import]
// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');
var querystring = require('querystring');
var http = require('http');
var fs = require('fs');

// The Firebase Admin SDK to access the Firebase Realtime Database. 
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
// [END import]

const Translate = require('@google-cloud/translate');
const translate = Translate();


// [START addMessage]
// Take the text parameter passed to this HTTP endpoint and insert it into the
// Realtime Database under the path /messages/:pushId/original
// [START addMessageTrigger]
exports.addMessage = functions.https.onRequest((req, res) => {
    // [END addMessageTrigger]
    // Grab the text parameter.
    const original = req.query.text;
    // [START adminSdkPush]
    // Push the new message into the Realtime Database using the Firebase Admin SDK.
    admin.database().ref('/messages').push({
        original: original
    }).then(snapshot => {
        // Redirect with 303 SEE OTHER to the URL of the pushed object in the Firebase console.
        res.redirect(303, snapshot.ref);
    });
    // [END adminSdkPush]
});
// [END addMessage]
var ct = 0;

// [START makeUppercase]
// Listens for new messages added to /messages/:pushId/original and creates an
// uppercase version of the message to /messages/:pushId/uppercase
// [START makeUppercaseTrigger]
exports.makeUppercase = functions.database.ref('/9958814273/msg/{pushId}/original')
    .onWrite(event => {
        // [END makeUppercaseTrigger]
        // [START makeUppercaseBody]
        // Grab the current value of what was written to the Realtime Database.
        const original = event.data.val();
        console.log('Uppercasing', event.params.pushId, original);
        const uppercase = original.toUpperCase();
        // Setting an "uppercase" sibling in the Realtime Database returns a Promise.

        var text = original;
        var target = 'hi';
        //        var target = event.data.ref.parent.parent.child();
        translate.translate(text, target)
            .then((results) => {
                let translations = results[0];
                translations = Array.isArray(translations) ? translations : [translations];

                console.log('Translations:');
                console.log(translations);
                console.log('data:');
                var ans = "";
                translations.forEach((translation, i) => {
                    console.log(`${text[i]} => (${target}) ${translation}`);
                    ans += translation;
                });
                //                var dtm = +new Date;
                ct += 1;
                event.data.ref.parent.child('translate').child(ct).set(ans);
            })
            .catch((err) => {
                console.error('ERROR:not afd', err);
            });

        event.data.ref.parent.child('original').remove();
        /*return*/

        //        event.data.ref.remove();
        return event.data.ref.parent.child('uppercase').set(uppercase);
        // [END makeUppercaseBody]
    });
// [END makeUppercase]
// [END all]
//console.log('hello');


exports.makeUppercase1 = functions.database.ref('/9891127886/msg/{pushId}/original')
    .onWrite(event => {
        // [END makeUppercaseTrigger]
        // [START makeUppercaseBody]
        // Grab the current value of what was written to the Realtime Database.
        const original = event.data.val();
        console.log('Uppercasing', event.params.pushId, original);
        const uppercase = original.toUpperCase();
        // Setting an "uppercase" sibling in the Realtime Database returns a Promise.

        var text = original;
        var target = 'en';
        //        var target = event.data.ref.parent.parent.child();
        translate.translate(text, target)
            .then((results) => {
                let translations = results[0];
                translations = Array.isArray(translations) ? translations : [translations];

                console.log('Translations:');
                console.log(translations);
                console.log('data:');
                var ans = "";
                translations.forEach((translation, i) => {
                    console.log(`${text[i]} => (${target}) ${translation}`);
                    ans += translation;
                });
                //                var dtm = +new Date;
                ct += 1;
                event.data.ref.parent.child('translate').child(ct).set(ans);
            })
            .catch((err) => {
                console.error('ERROR:not afd', err);
            });

        event.data.ref.parent.child('original').remove();
        /*return*/

        //        event.data.ref.remove();
        return event.data.ref.parent.child('uppercase').set(uppercase);
        // [END makeUppercaseBody]
    });
// [END makeUppercase]
// [END all]
//console.log('hello');
