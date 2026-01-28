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

  const page = await browser.newPage();
  const BASE_URL = 'http://host.docker.internal:8080';

  let confirmationMessage = "";

  // Gestionnaire de dialogues (alertes navigateur)
  page.on('dialog', async dialog => {
    confirmationMessage = dialog.message();
    console.log('DIALOGUE DÉTECTÉ:', confirmationMessage);
    await dialog.accept();
  });

  // Logs et erreurs console du navigateur
  page.on('console', msg => console.log('LOG NAVIGATEUR:', msg.text()));
  page.on('pageerror', err => console.error('ERREUR NAVIGATEUR:', err.message));

  try {
    // --- ÉTAPE 0 : NAVIGATION & PERMISSIONS (CORRIGÉ) ---

    // 1. On navigue d'abord vers la bonne page (HTML et non API)
    console.log(`Navigation vers ${BASE_URL}/api/login ...`);
    await page.goto(`${BASE_URL}/api/login`, { waitUntil: 'networkidle2', timeout: 30000 });

    // 2. On applique les permissions APRES la navigation (sinon crash ProtocolError)
    const context = browser.defaultBrowserContext();
    await context.overridePermissions(BASE_URL, ['geolocation']);
    await page.setGeolocation({ latitude: 48.8566, longitude: 2.3522 });
    console.log("Permissions GPS accordées.");

    // --- ÉTAPE 1 : CONNEXION ---
    await page.waitForSelector('input[type="email"]');
    await page.type('input[type="email"]', 'utilisateur1@compte.com');
    await page.type('input[type="password"]', 'MotDePasseSecret&1');

    await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle2' }),
      page.click('button[type="submit"]')
    ]);
    console.log("1. Connexion réussie.");

    // --- ÉTAPE 2 : RECHERCHE 'me' ET SÉLECTION (LOGIQUE INCHANGÉE) ---
    await page.waitForSelector('.search-input');
    console.log("2. Saisie de 'me' dans la recherche...");
    await page.type('.search-input', 'me', { delay: 100 });

    await page.waitForSelector('.spec-list-item');

    console.log("3. Sélection de 'ADULT MENTAL ILLNESS'...");
    await page.evaluate(() => {
      const items = Array.from(document.querySelectorAll('.spec-list-item'));
      const target = items.find(item => item.textContent.includes('ADULT MENTAL ILLNESS'));
      if (target) {
        const btn = target.querySelector('.btn-select');
        btn.click();
      } else {
        throw new Error("Spécialité 'ADULT MENTAL ILLNESS' introuvable");
      }
    });

    await page.waitForNavigation({ waitUntil: 'networkidle2' });
    console.log("4. Redirection vers la recherche d'hôpital.");

    // --- ÉTAPE 3 : CLIC GPS (LOGIQUE INCHANGÉE) ---
    const gpsBtn = 'button.btn-outline-primary';
    await page.waitForSelector(gpsBtn);
    console.log("5. Clic sur le bouton GPS...");
    await page.click(gpsBtn);

    await page.waitForSelector('.hospital-card', { timeout: 15000 });
    console.log("6. Résultats affichés via GPS.");

    // --- ÉTAPE 4 : RÉSERVATION (LOGIQUE INCHANGÉE) ---
    const firstReserveBtn = '.hospital-card .btn-success';
    await page.waitForSelector(firstReserveBtn);

    const hospitalName = await page.$eval('.hospital-card h5', el => el.textContent.trim());
    console.log(`7. Tentative de réservation chez : ${hospitalName}`);

    await page.click(firstReserveBtn);

    // --- ÉTAPE 5 : VÉRIFICATION (LOGIQUE INCHANGÉE) ---
    // Attente de l'alerte de succès
    await new Promise(resolve => setTimeout(resolve, 2000));

    if (confirmationMessage.toLowerCase().includes("succès")) {
      console.log("SUCCÈS FINAL : Réservation terminée avec succès.");
      await page.screenshot({ path: '/tmp/output/test_me_gps_reussi.png' }); // Sauvegarde à la racine pour Jenkins
    } else {
      console.log("AVERTISSEMENT : La confirmation n'a pas été détectée.");
    }

  } catch (error) {
    console.error("ÉCHEC DU TEST :", error.message);

    // Capture d'écran de débuggage
    if (page && !page.isClosed()) {
      try {
        // Sauvegarde à la racine pour être récupéré par archiveArtifacts
        await page.screenshot({ path: '/tmp/output/erreur_debug_saisie_gps.png' });
        console.log("Screenshot d'erreur sauvegardé.");
      } catch (e) {
        console.error("Impossible de capturer l'écran (page fermée)");
      }
    }
    // Indispensable pour que Jenkins marque le test en ROUGE
    process.exit(1);
  } finally {
    if (browser) await browser.close();
    console.log("Navigateur fermé, test terminé.");
  }
})();