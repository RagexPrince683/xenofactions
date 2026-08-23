package com.hfr.wallart;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.net.ssl.HttpsURLConnection;

import com.hfr.clowder.flag.CustomFlagService;
import com.hfr.config.XFConfig;

/** Server-only HTTPS fetch/decode pipeline. No Minecraft world state is touched here. */
final class SecureWallArtDownloader {
    static byte[] downloadAndProcess(String text, int blocksWide, int blocksHigh) throws Exception {
        if(text == null || text.length() == 0 || text.length() > WallArtConstants.MAX_URL_BYTES)
            throw new IOException("URL must be 1-2048 characters.");
        if(!WallArtConstants.validSize(blocksWide, blocksHigh))
            throw new IOException("Invalid Wall Art resolution.");

        int targetWidth = blocksWide * WallArtConstants.PIXELS_PER_BLOCK;
        int targetHeight = blocksHigh * WallArtConstants.PIXELS_PER_BLOCK;
        byte[] source = download(validate(text), 0);
        BufferedImage input = decode(source, targetWidth, targetHeight);
        source = null;
        BufferedImage output = null;
        try {
            output = resize(input, targetWidth, targetHeight);
        } finally {
            input.flush();
        }

        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            if(!ImageIO.write(output, "png", bytes))
                throw new IOException("Could not resize image.");
            byte[] png = bytes.toByteArray();
            if(png.length > WallArtConstants.MAX_PROCESSED_IMAGE_BYTES)
                throw new IOException("Processed Wall Art image exceeds the storage limit.");
            return png;
        } finally {
            output.flush();
        }
    }

    private static URL validate(String text) throws Exception {
        URI uri = new URI(text);
        if(uri.getUserInfo() != null || !"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null)
            throw new IOException("Invalid HTTPS image URL.");
        String host = normalizeHost(uri.getHost());
        if(!XFConfig.customFlagAllowedHostSet.contains(host))
            throw new IOException("Unsupported image host.");
        InetAddress[] addresses = InetAddress.getAllByName(host);
        if(addresses.length == 0)
            throw new IOException("Unsafe image host.");
        for(InetAddress address : addresses)
            if(CustomFlagService.isUnsafeAddress(address))
                throw new IOException("Unsafe image host.");
        return uri.toURL();
    }

    private static String normalizeHost(String host) {
        String normalized = host.toLowerCase(Locale.ROOT);
        while(normalized.endsWith("."))
            normalized = normalized.substring(0, normalized.length() - 1);
        return normalized;
    }

    private static byte[] download(URL url, int redirects) throws Exception {
        URL safe = validate(url.toString());
        HttpURLConnection connection = (HttpURLConnection)safe.openConnection();
        if(!(connection instanceof HttpsURLConnection))
            throw new IOException("Invalid HTTPS image URL.");
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(Math.min(10000, XFConfig.customFlagTimeoutMs));
        connection.setReadTimeout(Math.min(10000, XFConfig.customFlagTimeoutMs));
        connection.setRequestProperty("User-Agent", "XenofactionsWallArt/1.0");
        connection.setRequestProperty("Accept", "image/png,image/jpeg,image/*;q=0.8,*/*;q=0.1");
        try {
            int code = connection.getResponseCode();
            if(code >= 300 && code < 400) {
                if(redirects >= Math.min(3, XFConfig.customFlagMaxRedirects))
                    throw new IOException("Too many image redirects.");
                String location = connection.getHeaderField("Location");
                if(location == null)
                    throw new IOException("Invalid image redirect.");
                // download() validates HTTPS, the whitelist, DNS and every resolved address again.
                return download(new URL(safe, location), redirects + 1);
            }
            if(code < 200 || code >= 300)
                throw new IOException("Image host returned HTTP " + code + ".");

            String contentType = connection.getContentType();
            if(isClearlyNotImage(contentType))
                throw new IOException("The URL did not return an image. Use the direct image link.");
            long length = connection.getContentLengthLong();
            if(length > WallArtConstants.MAX_SOURCE_DOWNLOAD_BYTES)
                throw new IOException("Image download exceeds the Wall Art source limit.");
            return read(connection.getInputStream(), WallArtConstants.MAX_SOURCE_DOWNLOAD_BYTES);
        } catch(SocketTimeoutException e) {
            throw new IOException("Image download timed out.");
        } finally {
            connection.disconnect();
        }
    }

    private static boolean isClearlyNotImage(String contentType) {
        if(contentType == null)
            return false;
        String lower = contentType.toLowerCase(Locale.ROOT);
        return lower.startsWith("text/") || lower.startsWith("application/json")
            || lower.startsWith("application/xml") || lower.startsWith("application/xhtml");
    }

    private static byte[] read(InputStream input, int max) throws IOException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int total = 0;
            int read;
            while((read = input.read(buffer)) >= 0) {
                if(read == 0)
                    continue;
                total += read;
                if(total > max)
                    throw new IOException("Image download exceeds the Wall Art source limit.");
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        } finally {
            input.close();
        }
    }

    private static BufferedImage decode(byte[] bytes, int targetWidth, int targetHeight) throws IOException {
        ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes));
        if(stream == null)
            throw new IOException("Could not decode image.");
        ImageReader reader = null;
        try {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if(!readers.hasNext()) {
                if(looksLikeHtml(bytes))
                    throw new IOException("The URL did not return an image. Use the direct image link.");
                throw new IOException("Unsupported image format. Use PNG or JPEG.");
            }
            reader = readers.next();
            String format = reader.getFormatName();
            if(!"png".equalsIgnoreCase(format) && !"jpeg".equalsIgnoreCase(format) && !"jpg".equalsIgnoreCase(format))
                throw new IOException("Unsupported image format. Use PNG or JPEG.");
            reader.setInput(stream, true, true);
            int width = reader.getWidth(0);
            int height = reader.getHeight(0);
            if(width < 1 || height < 1)
                throw new IOException("Could not decode image.");
            if(width > WallArtConstants.MAX_SOURCE_DIMENSION || height > WallArtConstants.MAX_SOURCE_DIMENSION
                || (long)width * (long)height > WallArtConstants.MAX_SOURCE_PIXELS)
                throw new IOException("Image dimensions exceed the Wall Art safety limit.");

            double reduction = Math.max((double)width / targetWidth, (double)height / targetHeight);
            int subsampling = Math.max(1, (int)Math.floor(reduction));
            ImageReadParam param = reader.getDefaultReadParam();
            if(subsampling > 1)
                param.setSourceSubsampling(subsampling, subsampling, 0, 0);
            BufferedImage image = reader.read(0, param);
            if(image == null)
                throw new IOException("Could not decode image.");
            return image;
        } catch(IOException e) {
            throw e;
        } catch(RuntimeException e) {
            throw new IOException("Could not decode image.");
        } finally {
            if(reader != null)
                reader.dispose();
            stream.close();
        }
    }

    private static boolean looksLikeHtml(byte[] bytes) {
        int length = Math.min(bytes.length, 256);
        String prefix;
        try {
            prefix = new String(bytes, 0, length, "US-ASCII").trim().toLowerCase(Locale.ROOT);
        } catch(java.io.UnsupportedEncodingException impossible) {
            return false;
        }
        return prefix.startsWith("<!doctype html") || prefix.startsWith("<html");
    }

    private static BufferedImage resize(BufferedImage input, int targetWidth, int targetHeight) throws IOException {
        try {
            BufferedImage output = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = output.createGraphics();
            try {
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                double scale = Math.min((double)targetWidth / input.getWidth(), (double)targetHeight / input.getHeight());
                int width = Math.max(1, (int)Math.round(input.getWidth() * scale));
                int height = Math.max(1, (int)Math.round(input.getHeight() * scale));
                graphics.drawImage(input, (targetWidth - width) / 2, (targetHeight - height) / 2, width, height, null);
            } finally {
                graphics.dispose();
            }
            return output;
        } catch(RuntimeException e) {
            throw new IOException("Could not resize image.");
        }
    }

    private SecureWallArtDownloader() { }
}
