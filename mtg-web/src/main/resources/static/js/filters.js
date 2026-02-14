(function () {
    function updateSetGroupVisibility() {
        const setGroups = document.querySelectorAll(".set-group");
        setGroups.forEach(group => {
            const visibleRows = group.querySelectorAll("tbody tr:not([style*='display: none'])");
            group.style.display = visibleRows.length > 0 ? "" : "none";
        });
    }

    function filterCardsByTypeInternal() {
        const checkedTypes = Array.from(
            document.querySelectorAll("#filters .typeCheckbox[type='checkbox']:checked")
        ).map(cb => cb.value.toUpperCase());

        const rows = document.querySelectorAll("table tbody tr");
        rows.forEach(row => {
            const typesAttr = row.dataset.types || "";
            const types = typesAttr.toUpperCase().split(",").filter(Boolean);

            if (checkedTypes.length === 0) {
                row.style.display = "none";
                return;
            }

            const isVisible = types.some(type => checkedTypes.includes(type));
            if (row.getAttribute("data-missing-filter") === "hidden") {
                row.style.display = "none";
            } else {
                row.style.display = isVisible ? "" : "none";
            }
        });

        updateSetGroupVisibility();
    }

    function filterMissingInternal() {
        const missingOnly = document.getElementById("missingOnly")?.checked ?? false;
        const rows = document.querySelectorAll("table tbody tr");

        rows.forEach(row => {
            const owned = row.querySelector("input.owned")?.checked ?? false;
            const notFound = row.querySelector("input.not-found")?.checked ?? false;

            if (!missingOnly) {
                row.removeAttribute("data-missing-filter");
            } else {
                const shouldShow = !owned && !notFound;
                row.setAttribute("data-missing-filter", shouldShow ? "show" : "hidden");
            }
        });

        filterCardsByTypeInternal();
    }

    // Expose globals for inline HTML handlers
    window.filterCardsByType = function () {
        filterCardsByTypeInternal();
    };

    window.filterMissing = function () {
        filterMissingInternal();
    };

    // Initialize per-set collapse/expand buttons
    function initSetToggles() {
        const buttons = document.querySelectorAll(".toggle-set");
        buttons.forEach(btn => {
            btn.addEventListener("click", () => {
                const setGroup = btn.closest(".set-group");
                if (!setGroup) return;

                const table = setGroup.querySelector("table");
                if (!table) return;

                table.classList.toggle("collapsed");
            });
        });
    }

    // Initial run
    initSetToggles();
    filterCardsByTypeInternal();
})();