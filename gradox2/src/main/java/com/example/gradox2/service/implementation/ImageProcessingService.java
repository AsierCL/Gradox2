package com.example.gradox2.service.implementation;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import com.example.gradox2.service.exceptions.InternalServerErrorException;
import com.example.gradox2.service.exceptions.InvalidFileOperation;

@Service
public class ImageProcessingService {

    private static final int PROFILE_PICTURE_SIZE = 256;
    private static final String WEBP_FORMAT = "webp";

    public byte[] processProfilePicture(byte[] sourceBytes) {
        try {
            BufferedImage source = ImageIO.read(new ByteArrayInputStream(sourceBytes));
            if (source == null) {
                throw new InvalidFileOperation("No se pudo procesar la imagen");
            }

            BufferedImage cropped = centerCropToSquare(source);
            BufferedImage resized = resize(cropped, PROFILE_PICTURE_SIZE, PROFILE_PICTURE_SIZE);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            boolean written = ImageIO.write(resized, WEBP_FORMAT, output);
            if (!written) {
                throw new InternalServerErrorException("No se pudo codificar la imagen en WebP");
            }
            return output.toByteArray();
        } catch (IOException e) {
            throw new InvalidFileOperation("No se pudo procesar la imagen", e);
        }
    }

    private BufferedImage centerCropToSquare(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int side = Math.min(width, height);
        int x = (width - side) / 2;
        int y = (height - side) / 2;
        return source.getSubimage(x, y, side, side);
    }

    private BufferedImage resize(BufferedImage source, int targetWidth, int targetHeight) {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resized;
    }
}