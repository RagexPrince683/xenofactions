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
import java.net.URI;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.net.ssl.HttpsURLConnection;

import com.hfr.clowder.flag.CustomFlagService;
import com.hfr.config.XFConfig;

/** Server-only HTTPS fetch/decode pipeline. No Minecraft world state is touched here. */
final class SecureWallArtDownloader {
    static byte[] downloadAndProcess(String text, int blocksWide, int blocksHigh) throws Exception {
        if(text == null || text.length() == 0 || text.length() > WallArtConstants.MAX_URL_BYTES) throw new IOException("URL must be 1-2048 characters");
        byte[] source=download(validate(text),0); BufferedImage input=decode(source);
        int targetW=blocksWide*WallArtConstants.PIXELS_PER_BLOCK,targetH=blocksHigh*WallArtConstants.PIXELS_PER_BLOCK;
        BufferedImage output=new BufferedImage(targetW,targetH,BufferedImage.TYPE_INT_ARGB); Graphics2D g=output.createGraphics();
        try { g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR); double scale=Math.min((double)targetW/input.getWidth(),(double)targetH/input.getHeight());int w=Math.max(1,(int)Math.round(input.getWidth()*scale)),h=Math.max(1,(int)Math.round(input.getHeight()*scale));g.drawImage(input,(targetW-w)/2,(targetH-h)/2,w,h,null); } finally { g.dispose(); input.flush(); }
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); if(!ImageIO.write(output,"png",bytes))throw new IOException("PNG encoder unavailable"); output.flush();
        byte[] png=bytes.toByteArray(); if(png.length>WallArtConstants.MAX_IMAGE_BYTES)throw new IOException("processed image is too large"); return png;
    }
    private static URL validate(String text)throws Exception{URI uri=new URI(text);if(uri.getUserInfo()!=null||!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null)throw new IOException("invalid HTTPS URL");String host=uri.getHost().toLowerCase();if(host.endsWith("."))host=host.substring(0,host.length()-1);if(!XFConfig.customFlagAllowedHostSet.contains(host))throw new IOException("unsupported image host");InetAddress[] addresses=InetAddress.getAllByName(host);if(addresses.length==0)throw new IOException("unsafe host");for(InetAddress a:addresses)if(CustomFlagService.isUnsafeAddress(a))throw new IOException("unsafe host");return uri.toURL();}
    private static byte[] download(URL url,int redirects)throws Exception{URL safe=validate(url.toString());HttpURLConnection c=(HttpURLConnection)safe.openConnection();if(!(c instanceof HttpsURLConnection))throw new IOException("invalid HTTPS URL");c.setInstanceFollowRedirects(false);c.setConnectTimeout(Math.min(10000,XFConfig.customFlagTimeoutMs));c.setReadTimeout(Math.min(10000,XFConfig.customFlagTimeoutMs));c.setRequestProperty("User-Agent","XenofactionsWallArt/1.0");try{int code=c.getResponseCode();if(code>=300&&code<400){if(redirects>=Math.min(3,XFConfig.customFlagMaxRedirects))throw new IOException("too many redirects");String location=c.getHeaderField("Location");if(location==null)throw new IOException("invalid redirect");return download(new URL(safe,location),redirects+1);}if(code<200||code>=300)throw new IOException("image host returned HTTP "+code);int length=c.getContentLength();if(length>Math.min(WallArtConstants.MAX_IMAGE_BYTES,XFConfig.customFlagMaxFileSizeBytes))throw new IOException("image too large");return read(c.getInputStream(),Math.min(WallArtConstants.MAX_IMAGE_BYTES,XFConfig.customFlagMaxFileSizeBytes));}finally{c.disconnect();}}
    private static byte[] read(InputStream in,int max)throws IOException{try{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int total=0,n;while((n=in.read(b))>=0){total+=n;if(total>max)throw new IOException("image too large");out.write(b,0,n);}return out.toByteArray();}finally{in.close();}}
    private static BufferedImage decode(byte[] bytes)throws IOException{ImageInputStream stream=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes));if(stream==null)throw new IOException("unsupported image");ImageReader reader=null;try{Iterator<ImageReader> it=ImageIO.getImageReaders(stream);if(!it.hasNext())throw new IOException("unsupported image");reader=it.next();String format=reader.getFormatName();if(!"png".equalsIgnoreCase(format)&&!"jpeg".equalsIgnoreCase(format)&&!"jpg".equalsIgnoreCase(format))throw new IOException("only static PNG and JPEG images are supported");reader.setInput(stream,true,true);int w=reader.getWidth(0),h=reader.getHeight(0);int maxW=Math.min(2048,XFConfig.customFlagMaxWidth),maxH=Math.min(2048,XFConfig.customFlagMaxHeight);if(w<1||h<1||w>maxW||h>maxH||(long)w*h>(long)maxW*maxH)throw new IOException("source image dimensions are too large");BufferedImage image=reader.read(0);if(image==null)throw new IOException("malformed image");return image;}finally{if(reader!=null)reader.dispose();stream.close();}}
    private SecureWallArtDownloader() { }
}
