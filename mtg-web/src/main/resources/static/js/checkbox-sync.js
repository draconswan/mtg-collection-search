function initCheckboxSync() {
    const ownedBoxes = document.querySelectorAll("input.owned[data-name]");
    const notFoundBoxes = document.querySelectorAll("input.not-found[data-name]");

    // Sync all "Owned" checkboxes with the same card name
    ownedBoxes.forEach(box => {
        box.addEventListener("change", function () {
            const name = this.dataset.name;

            ownedBoxes.forEach(other => {
                if (other !== box && other.dataset.name === name) {
                    other.checked = box.checked;
                }
            });

            if (box.checked) {
                notFoundBoxes.forEach(other => {
                    if (other.dataset.name === name) {
                        other.checked = false;
                    }
                });
            }

            if (typeof filterMissing === "function") {
                filterMissing()
            }
        });
    });

    // "Not Found" is mutually exclusive with its row's "Owned" only
    notFoundBoxes.forEach(box => {
        box.addEventListener("change", function () {
            const name = this.dataset.name;
            const row = this.closest("tr");
            if (!row) return;

            const safeName = CSS.escape(name);
            const owned = row.querySelector('input.owned[data-name="${safeName}"]');
            if (box.checked && owned) {
                owned.checked = false;
            }

            if (typeof filterMissing === "function") {
                filterMissing()
            }
        });
    });
}

initCheckboxSync();