(function () {
    const cache = {};
    const preview = document.getElementById("card-preview");
    if (!preview) {
        return;
    }

    function showPreview(imgUrl) {
        if (!imgUrl) return;

        if (cache[imgUrl]) {
            preview.innerHTML = cache[imgUrl];
            preview.classList.add("visible");
            return;
        }

        const img = new Image();
        img.src = imgUrl;
        img.alt = "Card image";
        img.style.maxWidth = "200px";

        img.onload = () => {
            const html = `<img src="${imgUrl}" alt="Card image" style="max-width:200px;">`;
            cache[imgUrl] = html;
            preview.innerHTML = html;
            preview.classList.add("visible");
        };
    }

    function hidePreview() {
        preview.classList.remove("visible");
    }

    // Clamp preview within viewport
    function positionPreview(clientX, clientY) {
        const offset = 20;
        const maxX = window.innerWidth - 220; // 200px image + padding
        const maxY = window.innerHeight - 300; // some safe height

        const x = Math.min(clientX + offset, maxX);
        const y = Math.min(clientY + offset, maxY);

        preview.style.left = x + "px";
        preview.style.top = y + "px";
    }

    // Attach events to card links
    const links = document.querySelectorAll(".card-link[data-img-url]");
    links.forEach(link => {
        link.addEventListener("mouseenter", () => {
            const imgUrl = link.getAttribute("data-img-url");
            showPreview(imgUrl);
        });

        link.addEventListener("mouseleave", () => {
            hidePreview();
        });
    });

    // Global mousemove only matters when preview is visible
    document.addEventListener("mousemove", e => {
        if (!preview.classList.contains("visible")) return;
        // If cursor is NOT over a card link, hide preview
        if (!e.target.closest(".card-link")) {
            hidePreview();
            return;
        }
        positionPreview(e.clientX, e.clientY);
    });
})();