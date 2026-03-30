// Simple in-memory cache for mana symbol <img> elements
const manaSymbolCache = new Map();

/**
 * Returns an <img> element for a mana symbol, using cache if available.
 */
function getManaSymbolImg(symbol) {
    const clean = symbol.toUpperCase().replace(/\//g, "");
    const url = `https://svgs.scryfall.io/card-symbols/${clean}.svg`;

    // If cached, return a cloned node (so each cell gets its own <img>)
    if (manaSymbolCache.has(clean)) {
        return manaSymbolCache.get(clean).cloneNode(true);
    }

    // Otherwise create it, cache it, and return it
    const img = document.createElement("img");
    img.src = url;
    img.alt = clean;
    img.width = 20;
    img.height = 20;
    img.style.marginRight = "3px";
    img.style.verticalAlign = "middle";

    manaSymbolCache.set(clean, img);

    return img.cloneNode(true);
}

/**
 * Converts Scryfall mana cost strings like "{2}{W/U}{B}{G/P}" into images.
 */
function renderManaSymbols() {
    const cells = document.querySelectorAll(".color-col");

    cells.forEach(cell => {
        const raw = cell.textContent.trim();
        cell.textContent = "";

        if (!raw) {
            return;
        }

        const symbols = raw.match(/\{[^}]+\}/g);
        if (!symbols) {
            cell.textContent = raw;
            return;
        }

        symbols.forEach(sym => {
            const clean = sym.replace(/[{}]/g, "");
            cell.appendChild(getManaSymbolImg(clean));
        });
    });
}

/**
 * Renders deck color identity symbols from <span data-mana="W"> etc.
 */
function renderDeckColorSymbols() {
    const elements = document.querySelectorAll("[data-mana]");

    elements.forEach(el => {
        const symbol = el.dataset.mana;
        el.textContent = "";
        el.appendChild(getManaSymbolImg(symbol));
    });
}

document.addEventListener("DOMContentLoaded", () => {
    renderManaSymbols();
    renderDeckColorSymbols();
});