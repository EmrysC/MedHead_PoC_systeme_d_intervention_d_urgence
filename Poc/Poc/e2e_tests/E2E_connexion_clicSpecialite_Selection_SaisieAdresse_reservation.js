const puppeteer = require('puppeteer');

(async () => {
  console.log("Démarrage du scénario de réservation complet");

  // Configuration optimisée pour Docker/Jenkins
  const browser = await puppeteer.launch({
    args: [
      '--no-sandbox',
      '--disable-setuid-sandbox',
      '--disable-dev-shm-usage',
      '--ignore-certificate-errors',
      '--allow-insecure-localhost',
      '--disable-web-security',
      '--proxy-server="direct://"',
      '--proxy-bypass-list=*'
    ],
    headless: "new"
  });

  const BASE_URL = 'http://medhead_backend:8080'; // Utilise le container_name Docker
  const page = await browser.newPage();
  let confirmationMessage = "";

  // Gestionnaire de dialogues (Alertes navigateur)
  page.on('dialog', async dialog => {
    confirmationMessage = dialog.message();
    console.log('DIALOGUE DÉTECTÉ:', confirmationMessage);
    await dialog.accept();
  });

  // Logs et erreurs console du navigateur
  page.on('console', msg => console.log('LOG NAVIGATEUR:', msg.text()));
  page.on('pageerror', err => console.error('ERREUR NAVIGATEUR:', err.message));

  try {
    // --- ÉTAPE 1 : CONNEXION ---
    await page.goto(`${BASE_URL}/api/login`, { waitUntil: 'networkidle2', timeout: 30000 });
    await page.waitForSelector('input[type="email"]');
    await page.type('input[type="email"]', 'utilisateur1@compte.com');
    await page.type('input[type="password"]', 'MotDePasseSecret&1');

    await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle2' }),
      page.click('button[type="submit"]')
    ]);
    console.log("1. Connexion réussie.");

    // --- ÉTAPE 2 : SÉLECTION SPÉCIALITÉ ---
    await page.waitForSelector('.accordion-button');
    await page.click('.accordion-button');

    const selectBtn = '.btn-select';
    await page.waitForSelector(selectBtn, { visible: true });

    await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle2' }),
      page.click(selectBtn)
    ]);
    console.log("2. Spécialité sélectionnée.");

    // --- ÉTAPE 3 : RECHERCHE ---
    await page.waitForSelector('#adresse-input', { visible: true });
    await page.type('#adresse-input', 'paris', { delay: 50 });
    await page.keyboard.press('Enter');

    await page.waitForSelector('.hospital-card', { timeout: 10000 });
    console.log("3. Résultats de recherche affichés.");

    // --- ÉTAPE 4 : RÉSERVATION ---
    const hospitalName = await page.$eval('.hospital-card h5', el => el.textContent.trim());
    console.log(`4. Tentative de réservation pour : ${hospitalName}`);

    await page.click('.hospital-card .btn-success');

    // --- ÉTAPE 5 : VÉRIFICATION ---
    // Attente du traitement asynchrone et de l'alerte
    await new Promise(resolve => setTimeout(resolve, 2000));

    if (confirmationMessage.toLowerCase().includes("succès")) {
      console.log(`SUCCÈS FINAL : Réservation effectuée chez ${hospitalName}`);
      await page.screenshot({ path: '/tmp/output/reservation_reussie.png' }); // Sauvegarde à la racine pour Jenkins
    } else {
      console.log("AVERTISSEMENT : Message de confirmation non détecté");
      await page.screenshot({ path: '/tmp/output/alerte_inattendue.png' });
    }

  } catch (error) {
    console.error("ECHEC DU TEST :", error.message);

    // Capture d'écran de débuggage
    if (page && !page.isClosed()) {
      try {
        // Sauvegarde directe à la racine du conteneur 
        await page.screenshot({ path: '/tmp/output/erreur_debug_saisie_adresse.png' });
        console.log("Screenshot d'erreur sauvegardé.");
      } catch (e) {
        console.error("Impossible de capturer l'écran (page fermée)");
      }
    }
  } finally {
    if (browser) await browser.close();
    console.log("Test terminé, navigateur fermé.");
  }
})();