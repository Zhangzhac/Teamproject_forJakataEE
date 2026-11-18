(function () {
    'use strict';

    var key = 'jspchatroom.theme';
    var names = ['default', 'coder', 'union'];

    function apply(value) {
        var theme = names.includes(value) ? value : 'default';
        document.documentElement.dataset.theme = theme;
        return theme;
    }

    try {
        apply(localStorage.getItem(key));
    } catch (error) {
        apply('default');
    }

    document.addEventListener('DOMContentLoaded', function () {
        var picker = document.getElementById('chatTheme');
        if (picker) {
            picker.value = document.documentElement.dataset.theme;
            picker.disabled = false;
            picker.addEventListener('change', function () {
                var theme = apply(picker.value);
                try { localStorage.setItem(key, theme); } catch (error) {}
            });
        }

        window.addEventListener('storage', function (event) {
            if (event.storageArea !== localStorage || (event.key !== key && event.key !== null)) return;
            var theme = apply(event.newValue);
            if (picker) picker.value = theme;
        });
    });
})();
