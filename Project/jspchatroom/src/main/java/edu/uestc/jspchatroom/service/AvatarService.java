package edu.uestc.jspchatroom.service;

import edu.uestc.jspchatroom.dao.AvatarDao;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class AvatarService {
    public static final int MAX_UPLOAD_BYTES = 5 * 1024 * 1024;
    private static final long MAX_PIXELS = 24_000_000;
    private static final int SIZE = 200;

    private final AvatarDao avatars = new AvatarDao();

    public AvatarDao.Avatar find(int userId) {
        return avatars.find(userId);
    }

    public Map<Integer, String> versions(Set<Integer> userIds) {
        return avatars.findVersions(userIds);
    }

    public void save(int userId, InputStream upload) throws IOException {
        avatars.save(userId, UUID.randomUUID().toString(), compress(upload));
    }

    private byte[] compress(InputStream upload) throws IOException {
        BufferedImage square = readSquare(upload);
        BufferedImage avatar = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
        var graphics = avatar.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, SIZE, SIZE);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawImage(square, 0, 0, SIZE, SIZE, null);
        } finally {
            graphics.dispose();
            square.flush();
        }

        var writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        var bytes = new ByteArrayOutputStream();
        try (var output = new MemoryCacheImageOutputStream(bytes)) {
            writer.setOutput(output);
            var params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(.84f);
            writer.write(null, new IIOImage(avatar, null, null), params);
            output.flush();
        } finally {
            writer.dispose();
            avatar.flush();
        }
        return bytes.toByteArray();
    }

    private BufferedImage readSquare(InputStream upload) throws IOException {
        try (var input = new MemoryCacheImageInputStream(upload)) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new InvalidAvatar("avatar.invalid");

            var reader = readers.next();
            try {
                if (!Set.of("JPEG", "png").contains(reader.getFormatName())) {
                    throw new InvalidAvatar("avatar.invalid");
                }
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if ((long) width * height > MAX_PIXELS) throw new InvalidAvatar("avatar.dimensions");
                int side = Math.min(width, height);
                var params = reader.getDefaultReadParam();
                params.setSourceRegion(new Rectangle((width - side) / 2, (height - side) / 2, side, side));
                // Decode at most about twice the final size along each edge.
                int step = Math.max(1, side / (2 * SIZE));
                params.setSourceSubsampling(step, step, 0, 0);
                return reader.read(0, params);
            } catch (IIOException | IllegalArgumentException e) {
                throw new InvalidAvatar("avatar.invalid");
            } finally {
                reader.dispose();
            }
        }
    }

    public static final class InvalidAvatar extends IOException {
        public InvalidAvatar(String messageKey) {
            super(messageKey);
        }
    }
}
