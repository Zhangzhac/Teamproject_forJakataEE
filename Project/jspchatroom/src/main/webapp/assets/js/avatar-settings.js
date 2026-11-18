(function () {
    'use strict';

    var form = document.getElementById('avatarForm');
    var input = document.getElementById('avatarFile');
    var preview = document.getElementById('avatarPreview');
    var initial = document.getElementById('avatarInitial');
    var error = document.getElementById('avatarPreviewError');
    var save = form.querySelector('button[type="submit"]');
    var originalSrc = preview.getAttribute('src');
    var prepared = null;
    var previewUrl = null;
    var selection = 0;
    var size = 200;
    var maxBytes = 5 * 1024 * 1024;
    var maxPixels = 24000000;

    function resetPreview() {
        prepared = null;
        if (previewUrl) URL.revokeObjectURL(previewUrl);
        previewUrl = null;
        if (originalSrc) preview.src = originalSrc;
        else preview.removeAttribute('src');
        preview.hidden = !originalSrc;
        initial.hidden = Boolean(originalSrc);
        error.hidden = true;
        save.disabled = true;
    }

    function showError(message) {
        error.textContent = message;
        error.hidden = false;
    }

    resetPreview();
    input.addEventListener('change', async function () {
        var current = ++selection;
        resetPreview();
        var file = input.files[0];
        if (!file) return;
        if (file.size > maxBytes) {
            showError(form.dataset.tooLarge);
            return;
        }
        if (file.type && !['image/jpeg', 'image/png'].includes(file.type)) {
            showError(form.dataset.invalid);
            return;
        }

        var source = URL.createObjectURL(file);
        try {
            var image = new Image();
            image.src = source;
            await image.decode();
            if (current !== selection) return;
            if (image.naturalWidth * image.naturalHeight > maxPixels) {
                showError(form.dataset.dimensions);
                return;
            }

            // Use the same square for the round preview and the uploaded image.
            var crop = Math.min(image.naturalWidth, image.naturalHeight);
            var canvas = document.createElement('canvas');
            canvas.width = canvas.height = size;
            var draw = canvas.getContext('2d');
            draw.fillStyle = '#fff';
            draw.fillRect(0, 0, size, size);
            draw.drawImage(image, Math.floor((image.naturalWidth - crop) / 2),
                Math.floor((image.naturalHeight - crop) / 2), crop, crop, 0, 0, size, size);
            var square = await new Promise(function (resolve) { canvas.toBlob(resolve, 'image/png'); });
            if (current !== selection) return;
            if (!square) throw new Error('Preview could not be created');

            prepared = square;
            previewUrl = URL.createObjectURL(square);
            preview.src = previewUrl;
            preview.hidden = false;
            initial.hidden = true;
            save.disabled = false;
        } catch (e) {
            if (current === selection) showError(form.dataset.invalid);
        } finally {
            URL.revokeObjectURL(source);
        }
    });

    form.addEventListener('submit', function (event) {
        if (!prepared) event.preventDefault();
    });
    form.addEventListener('formdata', function (event) {
        if (prepared) event.formData.set('avatar', prepared, 'avatar.png');
    });
})();
