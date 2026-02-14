/**
 * Fetches the Scryfall set icon for a given set code.
 * Uses localStorage with a 24-hour expiration.
 */
async function fetchSetIcon(setCode) {
    if (!setCode) {
        return null;
    }
    const code = setCode.toLowerCase().trim();
    const cacheKey = "setIcon_" + code;
    const EXPIRATION_MS = 24 * 60 * 60 * 1000; // 24 hours

    // 1. Check cache
    const cachedRaw = localStorage.getItem(cacheKey);
    if (cachedRaw) {
        try {
            const cached = JSON.parse(cachedRaw);
            if (Date.now() - cached.timestamp < EXPIRATION_MS) {
                return cached.icon; // still valid
            } else {
                localStorage.removeItem(cacheKey); // expired
            }
        } catch {
            localStorage.removeItem(cacheKey); // corrupted
        }
    }

    // 2. Fetch from Scryfall
    const url = `https://api.scryfall.com/sets/${code}`;

    try {
        const response = await fetch(url);
        if (!response.ok) {
            return null;
        }

        const data = await response.json();
        const iconUrl = data.icon_svg_uri || null;

        // 3. Store with timestamp
        if (iconUrl) {
            localStorage.setItem(
                cacheKey,
                JSON.stringify({
                    icon: iconUrl,
                    timestamp: Date.now()
                })
            );
        }

        return iconUrl;
    } catch (err) {
        console.error("Error fetching set icon:", err);
        return null;
    }
}

/**
 * Preloads all unique set icons found on the page.
 * Returns a map: { "setcode": "iconUrl", ... }
 */
async function preloadSetIcons() {
    const elements = document.querySelectorAll("[data-set-code]");
    const codes = new Set();

    // Collect unique set codes
    elements.forEach(el => {
        const code = el.getAttribute("data-set-code");
        if (code) codes.add(code.toLowerCase());
    });

    // Fetch all icons in parallel
    const results = await Promise.all(
        Array.from(codes).map(async code => {
            const icon = await fetchSetIcon(code);
            return [code, icon];
        })
    );

    // Convert to map
    return Object.fromEntries(results);
}

/**
 * Renders icons into the table after preloading.
 */
async function loadAllSetIcons() {
    const iconMap = await preloadSetIcons();
    const elements = document.querySelectorAll("[data-set-code]");

    elements.forEach(el => {
        const code = el.getAttribute("data-set-code").toLowerCase();
        const iconUrl = iconMap[code];

        if (iconUrl) {
            el.innerHTML = `<img src="${iconUrl}" alt="${code} set icon" height="20">`;
        } else {
            el.textContent = code.toUpperCase(); // fallback
        }
    });
}

document.addEventListener("DOMContentLoaded", loadAllSetIcons);