// Simple in-memory cache for mana symbol <img> elements
const manaSymbolCache = new Map();

/**
 * Returns an <img> element for a mana symbol, using cache if available.
 */
function getManaSymbolImg(symbol) {
    const clean = symbol.toUpperCase().replace(/\//g, "-");
    const url = `https://svgs.scryfall.io/card-symbols/${clean}.svg`;

    if (manaSymbolCache.has(clean)) {
        return manaSymbolCache.get(clean).cloneNode(true);
    }

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
 * Parses strings like:
 *   "{1}{W}{W}//{W} / W"
 *
 * Into:
 *   costA: ["{1}","{W}","{W}"]
 *   costB: ["{W}"]
 *   identity: ["{W}"]
 *
 * Supports ALL Scryfall symbol types.
 */
function parseCastingCostAndIdentity(raw) {
    if (!raw) return {costA: [], costB: [], identity: []};

    raw = raw.normalize("NFKC");

    // Split identity: "COSTA//COSTB / IDENTITY"
    const [left, identityRaw] = raw.split(" / ");

    // Split costs: "COSTA//COSTB"
    const [costAPart, costBPart] = left.split("//");

    // Extract braced symbols for each cost
    const costA = costAPart.match(/\{[^}]+\}/g) || [];
    const costB = costBPart ? (costBPart.match(/\{[^}]+\}/g) || []) : [];

    // Identity: braced + bare letters
    let identity = [];
    if (identityRaw) {
        const braced = identityRaw.match(/\{[^}]+\}/g) || [];

        // Remove braced symbols before scanning for bare letters
        const remainder = identityRaw.replace(/\{[^}]+\}/g, " ");

        // Bare identity letters (WUBRG)
        const bare = remainder.match(/\b[WUBRG]\b/g) || [];

        identity = [
            ...braced,
            ...bare.map(c => `{${c}}`)
        ];
    }

    return {costA, costB, identity};
}

/**
 * Renders mana symbols for costA, costB, and identity.
 * Preserves // and / separators.
 */
function renderManaSymbols() {
    const cells = document.querySelectorAll(".color-col");

    cells.forEach(cell => {
        const raw = cell.textContent.trim();
        cell.textContent = "";

        if (!raw) return;

        const {costA, costB, identity} = parseCastingCostAndIdentity(raw);

        // Render cost A
        costA.forEach(sym => {
            const clean = sym.replace(/[{}]/g, "");
            cell.appendChild(getManaSymbolImg(clean));
        });

        // Render cost B (Prepared/Adventure)
        if (costB.length > 0) {
            const sep = document.createElement("span");
            sep.textContent = " // ";
            cell.appendChild(sep);

            costB.forEach(sym => {
                const clean = sym.replace(/[{}]/g, "");
                cell.appendChild(getManaSymbolImg(clean));
            });
        }

        // Render identity
        if (identity.length > 0) {
            const sep = document.createElement("span");
            sep.textContent = " / ";
            cell.appendChild(sep);

            identity.forEach(sym => {
                const clean = sym.replace(/[{}]/g, "");
                cell.appendChild(getManaSymbolImg(clean));
            });
        }
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
