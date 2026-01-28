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
  // On ignore les erreurs 404 connues
  page.on('pageerror', err => { });

  try {
    // --- ÉTAPE 0 : NAVIGATION (Statut 404 attendu mais affichage OK) ---
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
    console.log("Connexion effectuée, redirection en cours...");

    // --- CORRECTION CRITIQUE : STABILISATION ---
    // On attend explicitement que la barre de recherche du Dashboard soit là.
    // Cela confirme que nous sommes sur la page d'accueil (HTTP 200) et plus sur le Login (404).
    await page.waitForSelector('.search-input');

    // --- ÉTAPE 2 : ACTIVATION GPS ---
    // Maintenant que la page est stable, on active le GPS
    const context = browser.defaultBrowserContext();
    await context.overridePermissions(BASE_URL, ['geolocation']);
    await page.setGeolocation({ latitude: 48.8566, longitude: 2.3522 });
    console.log("Permissions GPS accordées sur le Dashboard stable.");

    // --- ÉTAPE 3 : RECHERCHE 'me' ---
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

    // --- ÉTAPE 4 : CLIC GPS ---
    const gpsBtn = 'button.btn-outline-primary';
    await page.waitForSelector(gpsBtn);

    // Petite pause de sécurité pour que le JS du bouton soit prêt
    await new Promise(r => setTimeout(r, 1000));

    console.log("Clic sur le bouton GPS...");
    await page.click(gpsBtn);

    await page.waitForSelector('.hospital-card', { timeout: 15000 });
    console.log("Résultats GPS affichés.");

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