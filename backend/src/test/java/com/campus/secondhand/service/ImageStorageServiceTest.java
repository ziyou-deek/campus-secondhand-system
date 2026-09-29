package com.campus.secondhand.service;

import com.campus.secondhand.common.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;

class ImageStorageServiceTest {
    @TempDir Path tempDir;

    @Test
    void storesValidImageUnderRandomName() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", output);
        byte[] png = output.toByteArray();
        ImageStorageService service = new ImageStorageService(tempDir.toString());
        String url = service.save(new MockMultipartFile("file", "portrait.png", "image/png", png), "avatars");
        assertTrue(url.matches("/uploads/avatars/[0-9a-f-]+\\.png"));
        assertArrayEquals(png, Files.readAllBytes(tempDir.resolve(url.substring("/uploads/".length()))));
    }

    @Test
    void rejectsNonImageEvenWithPngExtension() {
        ImageStorageService service = new ImageStorageService(tempDir.toString());
        MockMultipartFile fake = new MockMultipartFile("file", "fake.png", "image/png", "<script>alert(1)</script>".getBytes());
        assertThrows(BusinessException.class, () -> service.save(fake, "products"));
    }
}
