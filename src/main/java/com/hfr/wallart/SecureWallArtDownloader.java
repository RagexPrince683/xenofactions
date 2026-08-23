package com.hfr.wallart;

import com.hfr.clowder.flag.CustomFlagService;
import com.hfr.config.XFConfig;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

/** Server-only, bounded HTTPS fetch and image processing pipeline. */
final class SecureWallArtDownloader {
  private static final int MAX_HTML_BYTES = 512 * 1024;
  private static final String USER_AGENT = "Mozilla/5.0";

  static byte[] downloadAndProcess(String text, int blocksWide, int blocksHigh)
      throws IOException {
    if (text == null || text.length() == 0 ||
        text.length() > WallArtConstants.MAX_URL_BYTES) {
      throw new DownloadException("validate", "URL must be 1-2048 characters.",
                                  null, -1);
    }
    if (!WallArtConstants.validSize(blocksWide, blocksHigh)) {
      throw new DownloadException("validate", "Invalid Wall Art resolution.",
                                  null, -1);
    }

    URL sourceUrl = validate(text, false);
    FetchResponse response =
        fetch(sourceUrl, 0, XFConfig.wallArtMaxSourceBytes, true);
    if (isHtml(response.contentType, response.bytes)) {
      URL resolvedUrl = resolveImageUrl(response.finalUrl, response.bytes);
      response = fetch(validate(resolvedUrl.toExternalForm(), true), 0,
                       XFConfig.wallArtMaxSourceBytes, false);
    } else if (isClearlyUnsupported(response.contentType)) {
      throw new DownloadException("content-type",
                                  "The URL returned unsupported content (" +
                                      cleanContentType(response.contentType) +
                                      ").",
                                  response.finalUrl, -1);
    }

    int targetWidth = blocksWide * WallArtConstants.PIXELS_PER_BLOCK;
    int targetHeight = blocksHigh * WallArtConstants.PIXELS_PER_BLOCK;
    BufferedImage decoded = decode(response.bytes, targetWidth, targetHeight);
    BufferedImage resized = null;
    try {
      resized = resize(decoded, targetWidth, targetHeight);
    } finally {
      decoded.flush();
    }
    try {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      if (!ImageIO.write(resized, "png", output)) {
        throw new DownloadException("encode",
                                    "Could not encode the processed image.",
                                    response.finalUrl, -1);
      }
      byte[] png = output.toByteArray();
      if (png.length > WallArtConstants.MAX_PROCESSED_IMAGE_BYTES) {
        throw new DownloadException(
            "encode",
            "Processed Wall Art image exceeds the 2 MiB storage limit.",
            response.finalUrl, -1);
      }
      return png;
    } finally {
      if (resized != null)
        resized.flush();
    }
  }

  private static URL validate(String text, boolean resolved)
      throws IOException {
    try {
      URI uri = new URI(text);
      if (uri.getUserInfo() != null ||
          !"https".equalsIgnoreCase(uri.getScheme()) ||
          uri.getHost() == null) {
        throw new DownloadException("validate", "Invalid HTTPS image URL.",
                                    null, -1);
      }
      String host = normalizeHost(uri.getHost());
      if (!XFConfig.wallArtAllowedHostSet.contains(host)) {
        String prefix = resolved ? "Resolved image host " : "Host ";
        throw new DownloadException(
            "host-policy", prefix + host + " is not allowed by this server.",
            uri.toURL(), -1);
      }
      InetAddress[] addresses = InetAddress.getAllByName(host);
      if (addresses.length == 0) {
        throw new DownloadException(
            "dns", "Could not resolve image host " + host + ".", uri.toURL(),
            -1);
      }
      for (InetAddress address : addresses) {
        if (CustomFlagService.isUnsafeAddress(address)) {
          throw new DownloadException(
              "ssrf", "Image host " + host + " resolves to an unsafe address.",
              uri.toURL(), -1);
        }
      }
      return uri.toURL();
    } catch (DownloadException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new DownloadException("validate", "Invalid HTTPS image URL.", null,
                                  -1);
    }
  }

  private static FetchResponse fetch(URL requested, int redirects, int limit,
                                     boolean pageAllowed) throws IOException {
    URL safe = validate(requested.toExternalForm(), false);
    HttpURLConnection connection = (HttpURLConnection)safe.openConnection();
    if (!(connection instanceof HttpsURLConnection)) {
      throw new DownloadException("validate", "Invalid HTTPS image URL.", safe,
                                  -1);
    }
    connection.setInstanceFollowRedirects(false);
    connection.setConnectTimeout(XFConfig.wallArtDownloadTimeoutMs);
    connection.setReadTimeout(XFConfig.wallArtDownloadTimeoutMs);
    connection.setRequestProperty("User-Agent", USER_AGENT);
    connection.setRequestProperty(
        "Accept",
        pageAllowed
            ? "image/png,image/jpeg,image/*;q=0.9,text/html;q=0.8,*/*;q=0.1"
            : "image/png,image/jpeg,image/*;q=0.9,application/" +
              "octet-stream;q=0.5,*/*;q=0.1");
    try {
      int status = connection.getResponseCode();
      if (status >= 300 && status < 400) {
        if (redirects >= XFConfig.wallArtMaxRedirects) {
          throw new DownloadException("redirect", "Too many image redirects.",
                                      safe, status);
        }
        String location = connection.getHeaderField("Location");
        if (location == null) {
          throw new DownloadException(
              "redirect",
              "Image host returned a redirect without a destination.", safe,
              status);
        }
        URL destination = new URL(safe, location);
        validate(destination.toExternalForm(), false);
        return fetch(destination, redirects + 1, limit, pageAllowed);
      }
      if (status < 200 || status >= 300) {
        throw new DownloadException(
            "http", "Image host returned HTTP " + status + ".", safe, status);
      }
      String contentType = connection.getContentType();
      int effectiveLimit = isHtml(contentType, null) ? MAX_HTML_BYTES : limit;
      long length = connection.getContentLengthLong();
      if (length > effectiveLimit)
        throw tooLarge(effectiveLimit, safe);
      return new FetchResponse(
          safe, contentType,
          read(connection.getInputStream(), effectiveLimit, safe));
    } catch (SocketTimeoutException exception) {
      throw new DownloadException("timeout", "Image download timed out.", safe,
                                  -1);
    } finally {
      connection.disconnect();
    }
  }

  private static URL resolveImageUrl(final URL pageUrl, byte[] html)
      throws IOException {
    final HtmlImageCandidates candidates = new HtmlImageCandidates();
    try {
      ParserDelegator parser = new ParserDelegator();
      parser.parse(
          new InputStreamReader(new ByteArrayInputStream(html), "UTF-8"),
          new HTMLEditorKit.ParserCallback() {
            @Override
            public void handleSimpleTag(
                HTML.Tag tag, MutableAttributeSet attributes, int position) {
              candidates.inspect(tag, attributes);
            }
            @Override
            public void handleStartTag(
                HTML.Tag tag, MutableAttributeSet attributes, int position) {
              candidates.inspect(tag, attributes);
            }
          },
          true);
      String candidate = candidates.best();
      if (candidate == null) {
        throw new DownloadException("html-resolver",
                                    "The page did not contain a usable image.",
                                    pageUrl, -1);
      }
      return new URL(pageUrl, candidate);
    } catch (DownloadException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new DownloadException("html-resolver",
                                  "The page did not contain a usable image.",
                                  pageUrl, -1);
    }
  }

  private static final class HtmlImageCandidates {
    private String secureOpenGraph;
    private String openGraph;
    private String firstImage;

    void inspect(HTML.Tag tag, MutableAttributeSet attributes) {
      if (tag == HTML.Tag.META) {
        String property =
            attribute(attributes, HTML.Attribute.NAME, "property");
        String content =
            attribute(attributes, HTML.Attribute.CONTENT, "content");
        if ("og:image:secure_url".equalsIgnoreCase(property) && usable(content))
          secureOpenGraph = content;
        else if ("og:image".equalsIgnoreCase(property) && usable(content))
          openGraph = content;
      } else if (tag == HTML.Tag.IMG && firstImage == null) {
        String source = attribute(attributes, HTML.Attribute.SRC, "src");
        if (usable(source))
          firstImage = source;
      }
    }

    String best() {
      if (secureOpenGraph != null)
        return secureOpenGraph;
      if (openGraph != null)
        return openGraph;
      return firstImage;
    }

    private static boolean usable(String value) {
      if (value == null || value.trim().length() == 0)
        return false;
      String lower = value.trim().toLowerCase(Locale.ROOT);
      return !lower.startsWith("data:") && !lower.startsWith("file:") &&
          !lower.startsWith("ftp:") && !lower.startsWith("http:");
    }

    private static String attribute(MutableAttributeSet attributes,
                                    HTML.Attribute standard, String literal) {
      Object value = attributes.getAttribute(standard);
      if (value == null)
        value = attributes.getAttribute(literal);
      return value == null ? null : value.toString().trim();
    }
  }

  private static byte[] read(InputStream input, int max, URL url)
      throws IOException {
    try {
      ByteArrayOutputStream output =
          new ByteArrayOutputStream(Math.min(max, 32768));
      byte[] buffer = new byte[8192];
      int total = 0;
      int count;
      while ((count = input.read(buffer)) >= 0) {
        if (count == 0)
          continue;
        total += count;
        if (total > max)
          throw tooLarge(max, url);
        output.write(buffer, 0, count);
      }
      return output.toByteArray();
    } finally {
      input.close();
    }
  }

  private static DownloadException tooLarge(int max, URL url) {
    return new DownloadException("download",
                                 "Image exceeds the " + (max / (1024 * 1024)) +
                                     " MiB source download limit.",
                                 url, -1);
  }

  private static BufferedImage decode(byte[] bytes, int targetWidth,
                                      int targetHeight) throws IOException {
    ImageInputStream stream =
        ImageIO.createImageInputStream(new ByteArrayInputStream(bytes));
    if (stream == null)
      throw new DownloadException("decode", "Could not decode the image.", null,
                                  -1);
    ImageReader reader = null;
    try {
      Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
      if (!readers.hasNext())
        throw new DownloadException(
            "decode", "Unsupported image format. Use PNG or JPEG.", null, -1);
      reader = readers.next();
      String format = reader.getFormatName();
      if (!"png".equalsIgnoreCase(format) &&
          !"jpeg".equalsIgnoreCase(format) &&
          !"jpg".equalsIgnoreCase(format)) {
        throw new DownloadException(
            "decode", "Unsupported image format. Use PNG or JPEG.", null, -1);
      }
      reader.setInput(stream, true, true);
      int width = reader.getWidth(0);
      int height = reader.getHeight(0);
      if (width < 1 || height < 1)
        throw new DownloadException("decode", "Could not decode the image.",
                                    null, -1);
      if (width > XFConfig.wallArtMaxSourceDimension ||
          height > XFConfig.wallArtMaxSourceDimension ||
          (long)width * height > XFConfig.wallArtMaxSourcePixels) {
        throw new DownloadException("dimensions",
                                    "Image dimensions exceed the safety limit.",
                                    null, -1);
      }
      int subsampling =
          Math.max(1, (int)Math.floor(Math.max((double)width / targetWidth,
                                               (double)height / targetHeight)));
      ImageReadParam parameters = reader.getDefaultReadParam();
      if (subsampling > 1)
        parameters.setSourceSubsampling(subsampling, subsampling, 0, 0);
      BufferedImage image = reader.read(0, parameters);
      if (image == null)
        throw new DownloadException("decode", "Could not decode the image.",
                                    null, -1);
      return image;
    } catch (DownloadException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new DownloadException("decode", "Could not decode the image.", null,
                                  -1);
    } finally {
      if (reader != null)
        reader.dispose();
      stream.close();
    }
  }

  private static BufferedImage resize(BufferedImage input, int width,
                                      int height) throws IOException {
    try {
      BufferedImage output =
          new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      Graphics2D graphics = output.createGraphics();
      try {
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                  RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                                  RenderingHints.VALUE_RENDER_QUALITY);
        double scale = Math.min((double)width / input.getWidth(),
                                (double)height / input.getHeight());
        int scaledWidth =
            Math.max(1, (int)Math.round(input.getWidth() * scale));
        int scaledHeight =
            Math.max(1, (int)Math.round(input.getHeight() * scale));
        graphics.drawImage(input, (width - scaledWidth) / 2,
                           (height - scaledHeight) / 2, scaledWidth,
                           scaledHeight, null);
      } finally {
        graphics.dispose();
      }
      return output;
    } catch (RuntimeException exception) {
      throw new DownloadException("resize", "Could not resize the image.", null,
                                  -1);
    }
  }

  private static boolean isHtml(String contentType, byte[] bytes) {
    if (contentType != null &&
        contentType.toLowerCase(Locale.ROOT).startsWith("text/html"))
      return true;
    if (bytes == null)
      return false;
    String prefix;
    try {
      prefix = new String(bytes, 0, Math.min(bytes.length, 256), "US-ASCII")
                   .trim()
                   .toLowerCase(Locale.ROOT);
    } catch (Exception impossible) {
      return false;
    }
    return prefix.startsWith("<!doctype html") || prefix.startsWith("<html");
  }

  private static boolean isClearlyUnsupported(String contentType) {
    if (contentType == null)
      return false;
    String lower = contentType.toLowerCase(Locale.ROOT);
    return lower.startsWith("application/json") ||
        lower.startsWith("text/plain") || lower.startsWith("application/xml") ||
        lower.startsWith("application/xhtml");
  }

  private static String cleanContentType(String value) {
    if (value == null)
      return "unknown type";
    int separator = value.indexOf(';');
    return separator < 0 ? value : value.substring(0, separator);
  }

  static String normalizeHost(String host) {
    String normalized = host == null ? "" : host.toLowerCase(Locale.ROOT);
    while (normalized.endsWith("."))
      normalized = normalized.substring(0, normalized.length() - 1);
    return normalized;
  }

  static String sanitize(URL url) {
    if (url == null)
      return "<unknown>";
    String path = url.getPath();
    return url.getProtocol() + "://" + url.getHost() +
        (path == null ? "" : path) +
        (url.getQuery() == null ? "" : "?<query omitted>");
  }

  static final class DownloadException extends IOException {
    final String stage;
    final URL url;
    final int httpStatus;

    DownloadException(String stage, String message, URL url, int httpStatus) {
      super(message);
      this.stage = stage;
      this.url = url;
      this.httpStatus = httpStatus;
    }
  }

  private static final class FetchResponse {
    final URL finalUrl;
    final String contentType;
    final byte[] bytes;

    FetchResponse(URL finalUrl, String contentType, byte[] bytes) {
      this.finalUrl = finalUrl;
      this.contentType = contentType;
      this.bytes = bytes;
    }
  }

  private SecureWallArtDownloader() {}
}
