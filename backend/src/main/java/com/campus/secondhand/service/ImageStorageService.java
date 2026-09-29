package com.campus.secondhand.service;

import com.campus.secondhand.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageStorageService {
    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final long MAX_PIXELS = 20_000_000L;
    private static final Set<String> KINDS = Set.of("products", "avatars");
    private final Path root;

    public ImageStorageService(@Value("${app.upload-dir:./uploads}") String uploadDir) {
        root = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public String save(MultipartFile file, String kind) {
        if (!KINDS.contains(kind)) throw new BusinessException("图片用途无效");
        if (file == null || file.isEmpty() || file.getSize() > MAX_BYTES) {
            throw new BusinessException("请选择不超过 5 MB 的图片");
        }
        String format;
        try (InputStream input = file.getInputStream(); ImageInputStream imageInput = ImageIO.createImageInputStream(input)) {
            if (imageInput == null) throw new BusinessException("图片格式不支持，请使用 JPG 或 PNG");
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) throw new BusinessException("图片格式不支持，请使用 JPG 或 PNG");
            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInput);
                format = reader.getFormatName().toLowerCase();
                if (!format.equals("jpeg") && !format.equals("png")) {
                    throw new BusinessException("图片格式不支持，请使用 JPG 或 PNG");
                }
                if ((long) reader.getWidth(0) * reader.getHeight(0) > MAX_PIXELS) {
                    throw new BusinessException("图片尺寸过大");
                }
                // 实际解码，避免伪装成图片的文件进入公开目录。
                if (reader.read(0) == null) throw new BusinessException("图片文件损坏");
            } finally {
                reader.dispose();
            }
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            throw new BusinessException("图片文件损坏或读取失败");
        }
        String extension = format.equals("jpeg") ? ".jpg" : ".png";
        String name = UUID.randomUUID() + extension;
        Path directory = root.resolve(kind);
        Path destination = directory.resolve(name);
        try {
            Files.createDirectories(directory);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, destination);
            }
        } catch (IOException e) {
            throw new BusinessException("图片保存失败");
        }
        return "/uploads/" + kind + "/" + name;
    }

    public Path getRoot() { return root; }
}
