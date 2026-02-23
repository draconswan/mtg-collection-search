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
        if (!response.ok) {
            return;
        }

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
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
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
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
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
        const type = input.dataset.type;
        const quantity = parseInt(input.value, 10);

        const response = await fetch(`/api/v1/decks/${deckId}/update-quantity`, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `cardId=${encodeURIComponent(cardId)}&quantity=${encodeURIComponent(quantity)}`
        });

        if (response.ok) {
            // Visual feedback
            input.classList.add("border-success");
            setTimeout(() => input.classList.remove("border-success"), 600);

            // Keep label metadata in sync
            const label = document.querySelector(`label[data-card-id="${cardId}"]`);
            if (label) {
                label.dataset.quantity = quantity;
            }

            // Keep hidden form field in sync so Save Deck submits correct data
            const hidden = document.getElementById(`hidden_qty_${index}`);
            if (hidden) {
                hidden.value = quantity;
            }

            // Update totals (group totals and deck total)
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

        rows.forEach(row => {
            const type = row.dataset.type;
            const qtyInput = row.querySelector(".quantity-input");
            const qty = parseInt(qtyInput.value, 10) || 0;

            total += qty;
            typeTotals[type] = (typeTotals[type] || 0) + qty;
        });

        // Update per-type totals
        for (const [type, qty] of Object.entries(typeTotals)) {
            const el = document.getElementById(`typeTotal_${type}`);
            if (el) {
                el.textContent = qty;
            }
        }

        // Update overall total
        const totalEl = document.getElementById("totalQuantity");
        if (totalEl) {
            totalEl.textContent = "Total Cards: " + total;
        }
    }

    // -----------------------------
    // Send to Checklist
    // -----------------------------
    function sendUncheckedCards() {
        const unchecked = Array.from(
            document.querySelectorAll('#saveDeckForm .form-check-input')
        )
        .filter(input => !input.checked)
        .map(input => {
            const label = document.querySelector(`label[for="${input.id}"]`);
            const qty = label.dataset.quantity;
            const name = label.dataset.name;
            return `${qty} ${name}`;
        });

        if (unchecked.length === 0) {
            alert("There are no unchecked cards to send.");
            return;
        }

        const payload = unchecked.join("\n");

        document.getElementById('cardNames').value = payload;
        document.getElementById('sendForm').submit();
    }

    // -----------------------------
    // Copy to clipboard
    // -----------------------------
    function copyUncheckedCards() {
        const unchecked = Array.from(
            document.querySelectorAll('#saveDeckForm .form-check-input')
        )
        .filter(input => !input.checked)
        .map(input => {
            const label = document.querySelector(`label[for="${input.id}"]`);
            const qty = label.dataset.quantity;
            const name = label.dataset.name;
            return `${qty} ${name}`;
        });

        if (unchecked.length === 0) {
            showToast("No unchecked cards to copy.", "danger");
            return;
        }

        const text = unchecked.join("\n");

        navigator.clipboard.writeText(text)
            .then(() => {
                showToast("Unchecked cards copied to clipboard.", "success");
            })
            .catch(err => {
                console.error("Clipboard copy failed:", err);
                showToast("Could not copy to clipboard.", "danger");
            });
    }

    function showToast(message, type = "success") {
        const toastEl = document.getElementById("copyToast");

        // Update message + color
        toastEl.querySelector(".toast-body").textContent = message;
        toastEl.className = `toast text-bg-${type} border-0`;

        const toast = bootstrap.Toast.getOrCreateInstance(toastEl, {
            delay: 3000,   // auto-close after 3 seconds
            autohide: true
        });

        toast.show();
    }

    // -----------------------------
    // Init: Bind all events
    // -----------------------------
    function init() {
        // Delete buttons
        document.querySelectorAll(".delete-card-btn").forEach(btn => {
            btn.addEventListener("click", () => deleteCard(btn));
        });

        // Quantity inputs
        document.querySelectorAll(".quantity-input").forEach(input => {
            input.addEventListener("input", () => updateQuantity(input));
        });

        // Search input
        const searchInput = document.getElementById("cardSearch");
        const resultsBox = document.getElementById("cardSearchResults");

        searchInput.addEventListener("input", () => {
            const query = searchInput.value.trim();
            selectedCardId = null;

            if (searchTimeout) clearTimeout(searchTimeout);
            if (query.length < 2) {
                resultsBox.style.display = "none";
                return;
            }

            searchTimeout = setTimeout(() => searchCards(query), 250);
        });

        // Close search results on outside click
        document.addEventListener("click", (e) => {
            if (!resultsBox.contains(e.target) && e.target !== searchInput) {
                resultsBox.style.display = "none";
            }
        });

        // Buttons
        document.getElementById("addSelectedCardBtn")?.addEventListener("click", addSelectedCard);
        document.getElementById("exportUncheckedBtn")?.addEventListener("click", exportUncheckedCards);
        document.getElementById("clearUncheckedBtn")?.addEventListener("click", clearUncheckedCards);
        document.getElementById("sendUncheckedBtn")?.addEventListener("click", sendUncheckedCards);
        document.getElementById("copyUncheckedBtn")?.addEventListener("click", copyUncheckedCards);
    }

    return { init };
})();

// Initialize when DOM is ready
document.addEventListener("DOMContentLoaded", DeckUI.init);