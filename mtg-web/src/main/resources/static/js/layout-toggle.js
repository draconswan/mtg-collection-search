(function () {
    function toggleLayoutInternal() {
        document.body.classList.toggle("single-column");
    }

    // Expose global for button onclick
    window.toggleLayout = toggleLayoutInternal;
})();