package com.example.gradox2.service.implementation;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.ImageInputStream;

import org.springframework.stereotype.Service;

import com.example.gradox2.service.exceptions.InternalServerErrorException;
import com.example.gradox2.service.exceptions.InvalidFileOperation;
import com.luciad.imageio.webp.WebPImageReaderSpi;

import jakarta.annotation.PostConstruct;

@Service
public class ImageProcessingService {

    private static final int PROFILE_PICTURE_SIZE = 256;
    private static final String WEBP_FORMAT = "webp";
    private static final int MAX_SOURCE_DIMENSION = 8192;

    @PostConstruct
    void disableWebpDecoding() {
        IIORegistry.getDefaultInstance().deregisterServiceProvider(WebPImageReaderSpi.class);
    }

    public byte[] processProfilePicture(byte[] sourceBytes) {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(sourceBytes))) {
            BufferedImage source = decodeWithLimits(input);
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

    private BufferedImage decodeWithLimits(ImageInputStream input) throws IOException {
        Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
        if (!readers.hasNext()) {
            throw new InvalidFileOperation("No se pudo procesar la imagen");
        }
        ImageReader reader = readers.next();
        try {
            reader.setInput(input, true, true);
            int width = reader.getWidth(0);
            int height = reader.getHeight(0);
            if (width <= 0 || height <= 0 || width > MAX_SOURCE_DIMENSION || height > MAX_SOURCE_DIMENSION) {
                throw new InvalidFileOperation("Las dimensiones de la imagen superan el tamaño permitido");
            }
            BufferedImage source = reader.read(0);
            if (source == null) {
                throw new InvalidFileOperation("No se pudo procesar la imagen");
            }
            return source;
        } finally {
            reader.dispose();
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