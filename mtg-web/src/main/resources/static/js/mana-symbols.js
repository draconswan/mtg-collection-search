/**
 * Converts a Scryfall mana cost string into HTML with mana symbol images.
 * Handles hybrid, phyrexian, generic, snow, variable, and standard symbols.
 *
 * Example input: "{2}{W/U}{B}{G/P}"
 */
function renderManaSymbols() {
    const cells = document.querySelectorAll("td.color-col");

    cells.forEach(cell => {
        const raw = cell.textContent.trim();
        cell.textContent = "";

        if (!raw) {
            return;
        }

        // Extract symbols like {W}, {2}, {W/U}, {G/P}, {X}, etc.
        const symbols = raw.match(/\{[^}]+\}/g);

        if (!symbols) {
            cell.textContent = raw;
            return;
        }

        symbols.forEach(sym => {
            const clean = sym.replace(/[{}]/g, "").toUpperCase().replace(/\//g, "");

            // Scryfall canonical symbol URL
            const url = `https://svgs.scryfall.io/card-symbols/${clean}.svg`;

            const img = document.createElement("img");
            img.src = url;
            img.alt = clean;
            img.width = 20;
            img.height = 20;
            img.style.marginRight = "3px";
            img.style.verticalAlign = "middle";

            cell.appendChild(img);
        });
    });
}

document.addEventListener("DOMContentLoaded", renderManaSymbols);