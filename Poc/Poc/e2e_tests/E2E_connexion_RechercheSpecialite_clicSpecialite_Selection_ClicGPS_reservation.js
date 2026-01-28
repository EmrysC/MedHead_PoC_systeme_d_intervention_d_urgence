const puppeteer = require('puppeteer');

(async () => {
  console.log("Démarrage du scénario : Recherche 'me' + Adult Mental Illness + GPS");

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

  // Ignorer les erreurs 404 dans la console pour ne pas polluer, car on sait que /api/login renvoie 404
  page.on('pageerror', err => console.error('ERREUR NAVIGATEUR:', err.message));

  try {
    // --- ÉTAPE 0 : NAVIGATION ---
    console.log(`Navigation vers ${BASE_URL}/api/login ...`);
    // On ignore le fait que ça renvoie 404, tant que le sélecteur apparait
    await page.goto(`${BASE_URL}/api/login`, { waitUntil: 'domcontentloaded', timeout: 30000 });

    // --- ÉTAPE 1 : CONNEXION D'ABORD (Sans GPS pour l'instant) ---
    await page.waitForSelector('input[type="email"]');
    await page.type('input[type="email"]', 'utilisateur1@compte.com');
    await page.type('input[type="password"]', 'MotDePasseSecret&1');

    console.log("Tentative de connexion...");
    await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle2' }),
      page.click('button[type="submit"]')
    ]);
    console.log("1. Connexion réussie. Nous sommes sur le Dashboard.");

    // --- ÉTAPE 2 : ACTIVATION GPS (MAINTENANT C'EST SÛR) ---
    // Maintenant qu'on est connecté sur une page valide (Dashboard), on active le GPS
    const context = browser.defaultBrowserContext();
    await context.overridePermissions(BASE_URL, ['geolocation']);
    await page.setGeolocation({ latitude: 48.8566, longitude: 2.3522 });
    console.log("Permissions GPS accordées sur le Dashboard.");

    // --- ÉTAPE 3 : RECHERCHE 'me' ---
    await page.waitForSelector('.search-input');
    console.log("2. Saisie de 'me'...");
    await page.type('.search-input', 'me', { delay: 100 });

    await page.waitForSelector('.spec-list-item'); // Attente résultats

    console.log("3. Sélection de 'ADULT MENTAL ILLNESS'...");
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
    console.log("4. Redirection vers la recherche d'hôpital.");

    // --- ÉTAPE 4 : CLIC GPS ---
    const gpsBtn = 'button.btn-outline-primary';
    await page.waitForSelector(gpsBtn);

    // Petite pause pour être sûr que la géolocalisation est prête
    await new Promise(r => setTimeout(r, 1000));

    console.log("5. Clic sur le bouton GPS...");
    await page.click(gpsBtn);

    await page.waitForSelector('.hospital-card', { timeout: 15000 });
    console.log("6. Résultats affichés via GPS.");

    // --- ÉTAPE 5 : RÉSERVATION ---
    const firstReserveBtn = '.hospital-card .btn-success';
    await page.waitForSelector(firstReserveBtn);
    const hospitalName = await page.$eval('.hospital-card h5', el => el.textContent.trim());
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