// triStateCheckbox.js
// Reusable tri-state checkbox module for MTG deck management
// Visuals: [ ] = none, [✓] = owned, [P] = proxy
// Rule: proxy requires checked=true

const TriStateCheckbox = (() => {

    function init() {
        document.querySelectorAll(".tri-check").forEach(cb => {

            const row = cb.closest(".card-row");
            const proxyInput = row?.querySelector(".proxy-hidden");

            if (!proxyInput) {
                console.warn("TriStateCheckbox: Missing .proxy-hidden input for", cb);
                return;
            }

            // Replace the native checkbox with a styled container
            cb.style.display = "none";

            // Create visual element
            const visual = document.createElement("span");
            visual.className = "tri-check-visual";
            visual.style.cursor = "pointer";
            visual.style.display = "inline-block";
            visual.style.width = "22px";
            visual.style.height = "22px";
            visual.style.border = "1px solid #666";
            visual.style.borderRadius = "4px";
            visual.style.fontSize = "14px";
            visual.style.fontWeight = "bold";
            visual.style.lineHeight = "20px";
            visual.style.textAlign = "center";
            visual.style.userSelect = "none";

            cb.insertAdjacentElement("afterend", visual);

            const updateVisual = () => {
                const checked = cb.dataset.checked === "true";
                const proxy = cb.dataset.proxy === "true";

                if (proxy) {
                    visual.textContent = "P";
                    visual.style.background = "#ffe08a"; // light yellow
                } else if (checked) {
                    visual.textContent = "✓";
                    visual.style.background = "#c8f7c5"; // light green
                } else {
                    visual.textContent = "";
                    visual.style.background = "white";
                }
            };

            updateVisual();

            visual.addEventListener("click", () => {
                let checked = cb.dataset.checked === "true";
                let proxy = cb.dataset.proxy === "true";

                // Cycle: none → owned → proxy → none
                if (!checked && !proxy) {
                    checked = true;
                    proxy = false;
                } else if (checked && !proxy) {
                    checked = true;
                    proxy = true;
                } else {
                    checked = false;
                    proxy = false;
                }

                cb.dataset.checked = checked;
                cb.dataset.proxy = proxy;

                // Update hidden proxy field
                proxyInput.value = proxy;

                // Update real checkbox for form submission
                cb.checked = checked;

                updateVisual();
            });
        });
    }

    return {init};
})();