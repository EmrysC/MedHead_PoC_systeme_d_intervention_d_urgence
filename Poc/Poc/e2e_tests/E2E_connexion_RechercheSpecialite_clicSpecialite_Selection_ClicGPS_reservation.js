const puppeteer = require('puppeteer');

(async () => {
  console.log("Demarrage du scenario : Recherche 'me' + Adult Mental Illness + GPS");

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

  // Configuration de la geolocalisation
  const context = browser.defaultBrowserContext();
  await context.overridePermissions('http://medhead_backend:8080', ['geolocation']);
  await page.setGeolocation({ latitude: 48.8566, longitude: 2.3522 });

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
    await page.goto('http://app:8080/api/login', { waitUntil: 'networkidle2' });
    await page.waitForSelector('input[type="email"]');
    await page.type('input[type="email"]', 'utilisateur1@compte.com');
    await page.type('input[type="password"]', 'MotDePasseSecret&1');

    await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle2' }),
      page.click('button[type="submit"]')
    ]);
    console.log("1. Connexion reussie.");

    // --- ETAPE 2 : RECHERCHE 'me' ET SELECTION ---
    await page.waitForSelector('.search-input');
    console.log("2. Saisie de 'me' dans la recherche...");
    await page.type('.search-input', 'me', { delay: 100 });

    await page.waitForSelector('.spec-list-item');

    console.log("3. Selection de 'ADULT MENTAL ILLNESS'...");
    await page.evaluate(() => {
      const items = Array.from(document.querySelectorAll('.spec-list-item'));
      const target = items.find(item => item.textContent.includes('ADULT MENTAL ILLNESS'));
      if (target) {
        const btn = target.querySelector('.btn-select');
        btn.click();
      } else {
        throw new Error("Specialite 'ADULT MENTAL ILLNESS' introuvable");
      }
    });

    await page.waitForNavigation({ waitUntil: 'networkidle2' });
    console.log("4. Redirection vers la recherche d'hopital.");

    // --- ETAPE 3 : CLIC GPS ---
    const gpsBtn = 'button.btn-outline-primary';
    await page.waitForSelector(gpsBtn);
    console.log("5. Clic sur le bouton GPS...");
    await page.click(gpsBtn);

    await page.waitForSelector('.hospital-card', { timeout: 15000 });
    console.log("6. Resultats affiches via GPS.");

    // --- ETAPE 4 : RESERVATION ---
    const firstReserveBtn = '.hospital-card .btn-success';
    await page.waitForSelector(firstReserveBtn);

    const hospitalName = await page.$eval('.hospital-card h5', el => el.textContent);
    console.log("7. Tentative de reservation chez : " + hospitalName.trim());

    await page.click(firstReserveBtn);

    // --- ETAPE 5 : VERIFICATION ---
    await new Promise(resolve => setTimeout(resolve, 2000));

    if (confirmationMessage.includes("succès")) {
      console.log("SUCCES FINAL : Reservation terminee avec succes.");
    } else {
      console.log("AVERTISSEMENT : La confirmation n'a pas ete detectee.");
    }

    await page.screenshot({ path: 'test_me_gps_reussi.png' });

  } catch (error) {
    console.error("ECHEC DU TEST :", error.message);
    await page.screenshot({ path: 'erreur_test_me.png' });
  } finally {
    await browser.close();
    console.log("Test termine, navigateur ferme.");
  }
})();