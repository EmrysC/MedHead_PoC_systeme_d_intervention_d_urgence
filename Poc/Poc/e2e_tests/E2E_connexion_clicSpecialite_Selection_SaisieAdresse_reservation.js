const puppeteer = require('puppeteer');

(async () => {
  console.log("Demarrage du scenario de reservation complet");

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

  const BASE_URL = 'http://app:8080';

  const page = await browser.newPage();
  let confirmationMessage = "";

  page.on('dialog', async dialog => {
    confirmationMessage = dialog.message();
    console.log('DIALOGUE DETECTE:', confirmationMessage);
    await dialog.accept();
  });

  page.on('console', msg => console.log('LOG NAVIGATEUR:', msg.text()));
  page.on('pageerror', err => console.error('ERREUR NAVIGATEUR:', err.message));

  try {
    // --- ETAPE 1 : CONNEXION ---
    await page.goto('${BASE_URL}/api/login', { waitUntil: 'networkidle2', timeout: 30000 });
    await page.waitForSelector('input[type="email"]');
    await page.type('input[type="email"]', 'utilisateur1@compte.com');
    await page.type('input[type="password"]', 'MotDePasseSecret&1');
    await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle2' }),
      page.click('button[type="submit"]')
    ]);
    console.log("1. Connexion reussie.");

    // --- ETAPE 2 : SELECTION SPECIALITE ---
    await page.waitForSelector('.accordion-button');
    await page.click('.accordion-button');
    const selectBtn = '.btn-select';
    await page.waitForSelector(selectBtn, { visible: true });
    await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle2' }),
      page.click(selectBtn)
    ]);
    console.log("2. Specialite selectionnee.");

    // --- ETAPE 3 : RECHERCHE ---
    await page.waitForSelector('#adresse-input', { visible: true });
    await page.type('#adresse-input', 'paris', { delay: 50 });
    await page.keyboard.press('Enter');
    await page.waitForSelector('.hospital-card', { timeout: 10000 });
    console.log("3. Resultats de recherche affiches.");

    // --- ETAPE 4 : RESERVATION DYNAMIQUE ---
    // Ajout d'un .trim() pour nettoyer le nom de l'hôpital
    const hospitalName = await page.$eval('.hospital-card h5', el => el.textContent.trim());
    console.log("4. Tentative de reservation pour : " + hospitalName);

    await page.click('.hospital-card .btn-success');

    // --- ETAPE 5 : VERIFICATION ---
    await new Promise(resolve => setTimeout(resolve, 2000));

    if (confirmationMessage.toLowerCase().includes("succès")) {
      console.log("SUCCES FINAL : Reservation effectuee chez " + hospitalName);
    } else {
      console.log("AVERTISSEMENT : Message de confirmation non detecte");
    }

  } catch (error) {
    console.error("ECHEC DU TEST :", error.message);

    // On vérifie que la page est encore active avant de prendre le screenshot
    if (page && !page.isClosed()) {
      try {
        // Utilise un nom différent par fichier pour ne pas les écraser
        await page.screenshot({ path: 'erreur_debug_e2e1.png' });
        console.log("Screenshot sauvegardé.");
      } catch (e) {
        console.error("Impossible de capturer l'écran (session fermée)");
      }
    }
  } finally {
    if (browser) await browser.close();
    console.log("Navigateur fermé.");
  }
})();