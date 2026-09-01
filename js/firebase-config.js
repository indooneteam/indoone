(() => {
  const firebaseConfig = {
    apiKey: "AIzaSyCwp4d_vMD44UBK38WtVq7vF8CHT3QzA8c",
    authDomain: "indoone.firebaseapp.com",
    projectId: "indoone",
    storageBucket: "indoone.firebasestorage.app",
    messagingSenderId: "715011875230",
    appId: "1:715011875230:web:4de5306bb94a0388e0b68f",
    measurementId: "G-RE8HGJ64GR"
  };

  if (!window.firebase) {
    console.error('Indoone Firebase SDK is not loaded.');
    return;
  }

  const app = firebase.initializeApp(firebaseConfig);
  const auth = firebase.auth();

  window.IndooneFirebase = { app, auth, config: firebaseConfig };
})();
