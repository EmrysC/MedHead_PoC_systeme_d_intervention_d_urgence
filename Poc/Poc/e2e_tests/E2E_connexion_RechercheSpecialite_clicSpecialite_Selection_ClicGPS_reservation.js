const puppeteer = require('puppeteer');

(async () => {
  console.log("Démarrage du scénario : Recherche 'me' + Adult Mental Illness + GPS (Mode Mocking)");

  const browser = await puppeteer.launch({
    args: [
      '--no-sandbox',
      '--disable-setuid-sandbox',
      '--disable-dev-shm-usage',
      '--ignore-certificate-errors',
      '--allow-insecure-localhost',
      '--disable-web-security'
    ],
    headless: "new"
  });

  const BASE_URL = 'http://host.docker.internal:8080';
  const page = await browser.newPage();
  let confirmationMessage = "";

  page.on('dialog', async dialog => {
    confirmationMessage = dialog.message();
    console.log('DIALOGUE DÉTECTÉ:', confirmationMessage);
    await dialog.accept();
  });

  page.on('console', msg => console.log('LOG NAVIGATEUR:', msg.text()));
  // On ignore les erreurs techniques de la page (404, etc.)
  page.on('pageerror', err => { });

  try {
    // --- ÉTAPE 0 : NAVIGATION ---
    console.log(`Navigation vers ${BASE_URL}/api/login ...`);
    await page.goto(`${BASE_URL}/api/login`, { waitUntil: 'domcontentloaded', timeout: 30000 });

    // --- ÉTAPE 1 : CONNEXION ---
    await page.waitForSelector('input[type="email"]');
    await page.type('input[type="email"]', 'utilisateur1@compte.com');
    await page.type('input[type="password"]', 'MotDePasseSecret&1');

    console.log("Validation du formulaire...");
    await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle2' }),
      page.click('button[type="submit"]')
    ]);
    console.log("Redirection vers le Dashboard détectée.");

    // --- STABILISATION ---
    await page.waitForSelector('.search-input');
    // Petite pause pour s'assurer que VueJS est prêt
    await new Promise(r => setTimeout(r, 1000));

    // --- ÉTAPE 2 : RECHERCHE 'me' ---
    console.log("Saisie de 'me'...");
    await page.type('.search-input', 'me', { delay: 100 });

    await page.waitForSelector('.spec-list-item');

    console.log("Sélection de 'ADULT MENTAL ILLNESS'...");
    await page.evaluate(() => {
      const items = Array.from(document.querySelectorAll('.spec-list-item'));
      const target = items.find(item => item.textContent.includes('ADULT MENTAL ILLNESS'));
      if (target) {
        target.querySelector('.btn-select').click();
      } else {
        throw new Error("Spécialité introuvable");
      }
    });

    await page.waitForNavigation({ waitUntil: 'networkidle2' });
    console.log("Redirection vers la liste des hôpitaux.");

    // --- ÉTAPE 3 : Mock GPS  ---
    console.log("Injection du Mock GPS (Bypassing permissions)...");

    await page.evaluate(() => {
      // On remplace la fonction officielle par la nôtre
      window.navigator.geolocation.getCurrentPosition = (successCallback, errorCallback) => {
        console.log("L'application a demandé le GPS, on envoie les fausses coordonnées.");
        successCallback({
          coords: {
            latitude: 48.8566,
            longitude: 2.3522,
            accuracy: 10,
            altitude: null,
            altitudeAccuracy: null,
            heading: null,
            speed: null
          },
          timestamp: Date.now()
        });
      };
    });

    // --- ÉTAPE 4 : CLIC GPS ---
    const gpsBtn = 'button.btn-outline-primary';
    await page.waitForSelector(gpsBtn);

    console.log("Clic sur le bouton GPS...");
    await page.click(gpsBtn);

    // On attend les résultats 
    await page.waitForSelector('.hospital-card', { timeout: 15000 });
    console.log("Résultats GPS affichés.");

    // --- ÉTAPE 5 : RÉSERVATION ---
    const firstReserveBtn = '.hospital-card .btn-success';
    await page.waitForSelector(firstReserveBtn);
    const hospitalName = await page.$eval('.hospital-card h5', el => el.textContent.trim());
    console.log(`Tentative de réservation pour : ${hospitalName}`);

    await page.click(firstReserveBtn);

    // --- ÉTAPE 6 : VÉRIFICATION ---
    await new Promise(resolve => setTimeout(resolve, 2000));

    if (confirmationMessage.toLowerCase().includes("succès")) {
      console.log(`SUCCÈS FINAL : Réservation effectuée.`);
      await page.screenshot({ path: '/tmp/output/test_me_gps_reussi.png' });
    } else {
      console.log("AVERTISSEMENT : Confirmation non détectée.");
      await page.screenshot({ path: '/tmp/output/alerte_manquante.png' });
    }

  } catch (error) {
    console.error("ECHEC DU TEST :", error.message);
    if (page && !page.isClosed()) {
      try { await page.screenshot({ path: '/tmp/output/erreur_debug_gps.png' }); } catch (e) { }
    }
    process.exit(1);
  } finally {
    if (browser) await browser.close();
    console.log("Fin du test.");
  }
})();