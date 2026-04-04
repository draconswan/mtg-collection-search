const DeckUI = (() => {
    let searchTimeout = null;
    let selectedCardId = null;

    // -----------------------------
    // Utility: Debounce
    // -----------------------------
    const debounce = (fn, delay) => {
        let timer = null;
        return (...args) => {
            clearTimeout(timer);
            timer = setTimeout(() => fn(...args), delay);
        };
    };

    // -----------------------------
    // Search Cards
    // -----------------------------
    async function searchCards(query) {
        const resultsBox = document.getElementById("cardSearchResults");

        const response = await fetch(`/api/v1/cards/search?cardName=${encodeURIComponent(query)}`);
        if (!response.ok) return;

        const cards = await response.json();
        resultsBox.innerHTML = "";

        if (cards.length === 0) {
            resultsBox.style.display = "none";
            return;
        }

        cards.forEach(card => {
            const item = document.createElement("button");
            item.type = "button";
            item.className = "list-group-item list-group-item-action";
            item.textContent = card.displayName;

            item.addEventListener("click", () => {
                document.getElementById("cardSearch").value = card.displayName;
                selectedCardId = card.id;
                resultsBox.style.display = "none";
            });

            resultsBox.appendChild(item);
        });

        resultsBox.style.display = "block";
    }

    // -----------------------------
    // Add Selected Card
    // -----------------------------
    async function addSelectedCard() {
        const deckId = document.querySelector("input[name='deckId']").value;
        const msg = document.getElementById("addCardMessage");

        if (!selectedCardId) {
            msg.textContent = "Select a card from the list.";
            return;
        }

        const response = await fetch(`/api/v1/decks/${deckId}/add-card`, {
            method: "POST",
            headers: {"Content-Type": "application/x-www-form-urlencoded"},
            body: `cardId=${encodeURIComponent(selectedCardId)}`
        });

        if (response.ok) {
            msg.textContent = "Card added! Reloading…";
            setTimeout(() => location.reload(), 600);
        } else {
            msg.textContent = "Failed to add card.";
        }
    }

    // -----------------------------
    // Delete Card
    // -----------------------------
    async function deleteCard(btn) {
        const cardId = btn.dataset.cardId;
        const deckId = btn.dataset.deckId;

        const response = await fetch(`/api/v1/decks/${deckId}/remove-card`, {
            method: "POST",
            headers: {"Content-Type": "application/x-www-form-urlencoded"},
            body: `cardId=${encodeURIComponent(cardId)}`
        });

        if (response.ok) {
            setTimeout(() => location.reload(), 600);
        } else {
            alert("Failed to delete card.");
        }
    }

    // -----------------------------
    // Update Quantity (debounced)
    // -----------------------------
    const updateQuantity = debounce(async (input) => {
        const cardId = input.dataset.cardId;
        const deckId = input.dataset.deckId;
        const index = input.dataset.index;
        const quantity = parseInt(input.value, 10);

        const response = await fetch(`/api/v1/decks/${deckId}/update-quantity`, {
            method: "POST",
            headers: {"Content-Type": "application/x-www-form-urlencoded"},
            body: `cardId=${encodeURIComponent(cardId)}&quantity=${encodeURIComponent(quantity)}`
        });

        if (response.ok) {
            input.classList.add("border-success");
            setTimeout(() => input.classList.remove("border-success"), 600);

            const label = document.querySelector(`label[data-card-id="${cardId}"]`);
            if (label) label.dataset.quantity = quantity;

            const hidden = document.getElementById(`hidden_qty_${index}`);
            if (hidden) hidden.value = quantity;

            updateTotals();
        } else {
            input.classList.add("border-danger");
            setTimeout(() => input.classList.remove("border-danger"), 1200);
        }
    }, 400);

    function updateTotals() {
        const rows = document.querySelectorAll(".card-row");

        const typeTotals = {};
        let total = 0;
        let proxyCount = 0;

        rows.forEach(row => {
            const type = row.dataset.type;
            const qtyInput = row.querySelector(".quantity-input");
            const qty = parseInt(qtyInput.value, 10) || 0;

            total += qty;
            typeTotals[type] = (typeTotals[type] || 0) + qty;

            const cb = row.querySelector(".tri-check");
            if (cb && cb.dataset.proxy === "true") {
                proxyCount += qty;
            }
        });

        for (const [type, qty] of Object.entries(typeTotals)) {
            const el = document.getElementById(`typeTotal_${type}`);
            if (el) el.textContent = qty;
        }

        const totalEl = document.getElementById("totalQuantity");
        if (totalEl) totalEl.textContent = "Total Cards: " + total;

        const proxyEl = document.getElementById("proxyCount");
        if (proxyEl) {
            proxyEl.textContent = proxyCount > 0
                ? `(${proxyCount} proxied)`
                : "";
        }
    }

    // -----------------------------
    // Send to Checklist
    // -----------------------------
    function sendUncheckedCards() {
        const unchecked = Array.from(
            document.querySelectorAll('#saveDeckForm .tri-check')
        )
            .filter(cb => cb.dataset.checked !== "true")
            .map(cb => {
                const label = document.querySelector(`label[for="${cb.id}"]`);
                return `${label.dataset.quantity} ${label.dataset.name}`;
            });

        if (unchecked.length === 0) {
            alert("There are no unchecked cards to send.");
            return;
        }

        document.getElementById('cardNames').value = unchecked.join("\n");
        document.getElementById('sendForm').submit();
    }

    // -----------------------------
    // Copy to clipboard
    // -----------------------------
    function copyUncheckedCards() {
        const unchecked = Array.from(
            document.querySelectorAll('#saveDeckForm .tri-check')
        )
            .filter(cb => cb.dataset.checked !== "true")
            .map(cb => {
                const label = document.querySelector(`label[for="${cb.id}"]`);
                return `${label.dataset.quantity} ${label.dataset.name}`;
            });

        if (unchecked.length === 0) {
            showToast("No unchecked cards to copy.", "danger");
            return;
        }

        navigator.clipboard.writeText(unchecked.join("\n"))
            .then(() => showToast("Unchecked cards copied to clipboard.", "success"))
            .catch(() => showToast("Could not copy to clipboard.", "danger"));
    }

    function showToast(message, type = "success") {
        const toastEl = document.getElementById("copyToast");
        toastEl.querySelector(".toast-body").textContent = message;
        toastEl.className = `toast text-bg-${type} border-0`;

        bootstrap.Toast.getOrCreateInstance(toastEl, {
            delay: 3000,
            autohide: true
        }).show();
    }

    // -----------------------------
    // Bulk Actions (Tri-State Aware)
    // -----------------------------
    function applyTriState(checkedValue, proxyValue) {
        document.querySelectorAll(".tri-check").forEach(cb => {
            const row = cb.closest(".card-row");
            const proxyInput = row.querySelector(".proxy-hidden");
            const visual = cb.nextElementSibling;

            cb.dataset.checked = checkedValue;
            cb.dataset.proxy = proxyValue;

            proxyInput.value = proxyValue;
            cb.checked = checkedValue === "true";

            if (proxyValue === "true") {
                visual.textContent = "P";
                visual.style.background = "#ffe08a";
            } else if (checkedValue === "true") {
                visual.textContent = "✓";
                visual.style.background = "#c8f7c5";
            } else {
                visual.textContent = "";
                visual.style.background = "white";
            }
        });
    }

    // -----------------------------
    // Init: Bind all events
    // -----------------------------
    function init() {
        document.querySelectorAll(".delete-card-btn").forEach(btn => {
            btn.addEventListener("click", () => deleteCard(btn));
        });

        document.querySelectorAll(".quantity-input").forEach(input => {
            input.addEventListener("input", () => updateQuantity(input));
        });

        const searchInput = document.getElementById("cardSearch");
        const resultsBox = document.getElementById("cardSearchResults");

        searchInput?.addEventListener("input", () => {
            const query = searchInput.value.trim();
            selectedCardId = null;

            if (searchTimeout) clearTimeout(searchTimeout);
            if (query.length < 2) {
                resultsBox.style.display = "none";
                return;
            }

            searchTimeout = setTimeout(() => searchCards(query), 250);
        });

        document.addEventListener("click", (e) => {
            if (!resultsBox.contains(e.target) && e.target !== searchInput) {
                resultsBox.style.display = "none";
            }
        });

        document.getElementById("addSelectedCardBtn")?.addEventListener("click", addSelectedCard);
        document.getElementById("sendUncheckedBtn")?.addEventListener("click", sendUncheckedCards);
        document.getElementById("copyUncheckedBtn")?.addEventListener("click", copyUncheckedCards);

        // Bulk actions
        document.getElementById("checkAllBtn")?.addEventListener("click", () => {
            applyTriState("true", "false");
        });

        document.getElementById("uncheckAllBtn")?.addEventListener("click", () => {
            applyTriState("false", "false");
        });
    }

    return {init};
})();

document.addEventListener("DOMContentLoaded", () => {
    DeckUI.init();
    TriStateCheckbox.init();
});