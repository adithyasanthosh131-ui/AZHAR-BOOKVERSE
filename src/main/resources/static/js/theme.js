// AZHAR BOOKVERSE - Dark / Light Theme

(function () {
    const savedTheme = localStorage.getItem("azhar-theme") || "light";

    // Apply saved theme immediately
    document.documentElement.setAttribute("data-theme", savedTheme);

    function createThemeButton() {
        if (document.getElementById("azharThemeToggle")) {
            return;
        }

        const button = document.createElement("button");

        button.id = "azharThemeToggle";
        button.type = "button";
        button.setAttribute("aria-label", "Switch colour theme");
        button.title = "Switch Dark / Light Mode";

        function updateButton() {
            const currentTheme =
                document.documentElement.getAttribute("data-theme");

            if (currentTheme === "dark") {
                button.innerHTML = "☀️";
                button.title = "Switch to Light Mode";
            } else {
                button.innerHTML = "🌙";
                button.title = "Switch to Dark Mode";
            }
        }

        button.addEventListener("click", function () {
            const currentTheme =
                document.documentElement.getAttribute("data-theme");

            const newTheme =
                currentTheme === "dark" ? "light" : "dark";

            document.documentElement.setAttribute("data-theme", newTheme);
            localStorage.setItem("azhar-theme", newTheme);

            updateButton();
        });

        document.body.appendChild(button);
        updateButton();
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", createThemeButton);
    } else {
        createThemeButton();
    }
})();