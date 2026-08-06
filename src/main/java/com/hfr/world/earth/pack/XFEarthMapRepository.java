package com.hfr.world.earth.pack;

import com.hfr.config.XFConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class XFEarthMapRepository {

 public static final String CLEAN = "xf-earth-16k-clean";
 public static final String POPULATED = "xf-earth-16k-populated";

 private final File root;
 private final File cache;

 public XFEarthMapRepository(File minecraftDirectory) {
  if (XFConfig.earthLocalMapDirectory == null
          || XFConfig.earthLocalMapDirectory.trim().isEmpty()) {

   this.root = new File(
           minecraftDirectory,
           "xenofactions/earthmaps"
   );
  } else {
   this.root = new File(XFConfig.earthLocalMapDirectory);
  }

  if (XFConfig.earthBundledCacheDirectory == null
          || XFConfig.earthBundledCacheDirectory.trim().isEmpty()) {

   this.cache = new File(this.root, "cache");
  } else {
   this.cache = new File(XFConfig.earthBundledCacheDirectory);
  }
 }

 public List<XFEarthMapPack> discover() {
  List<XFEarthMapPack> packs = new ArrayList<XFEarthMapPack>();

  System.out.println(
          "[XF Earth] Searching Earth map directory: "
                  + root.getAbsolutePath()
  );

  addBundled(
          packs,
          CLEAN,
          "earth-16k-clean.xfmap"
  );

  addBundled(
          packs,
          POPULATED,
          "earth-16k-populated.xfmap"
  );

  if (!root.exists()) {
   System.out.println(
           "[XF Earth] Earth map directory does not exist. Creating: "
                   + root.getAbsolutePath()
   );

   if (!root.mkdirs() && !root.isDirectory()) {
    System.err.println(
            "[XF Earth] Could not create Earth map directory: "
                    + root.getAbsolutePath()
    );

    return packs;
   }
  }

  File[] files = root.listFiles();

  if (files == null) {
   System.err.println(
           "[XF Earth] Could not list Earth map directory: "
                   + root.getAbsolutePath()
   );

   return packs;
  }

  Arrays.sort(
          files,
          new Comparator<File>() {

           @Override
           public int compare(File first, File second) {
            return first.getName().compareToIgnoreCase(
                    second.getName()
            );
           }
          }
  );

  for (File file : files) {
   if (!file.isFile()) {
    continue;
   }

   if (!file.getName().toLowerCase().endsWith(".xfmap")) {
    continue;
   }

   try {
    System.out.println(
            "[XF Earth] Verifying local map pack: "
                    + file.getAbsolutePath()
    );

    XFEarthMapManifest manifest =
            XFEarthMapVerifier.verify(file);

    String archiveHash =
            XFEarthMapVerifier.sha256(file);

    XFEarthMapSource source =
            new LocalEarthMapSource(
                    file,
                    XFEarthMapSource.Provider.EXTERNAL_INSTALLED
            );

    packs.add(
            new XFEarthMapPack(
                    manifest,
                    source,
                    archiveHash
            )
    );

    System.out.println(
            "[XF Earth] Accepted local map pack: "
                    + manifest.id
                    + " from "
                    + file.getAbsolutePath()
    );
   } catch (Exception exception) {
    System.err.println(
            "[XF Earth] Rejected local map pack: "
                    + file.getAbsolutePath()
    );

    exception.printStackTrace();
   }
  }

  System.out.println(
          "[XF Earth] Discovered "
                  + packs.size()
                  + " usable Earth map pack(s)."
  );

  return packs;
 }

 private void addBundled(
         List<XFEarthMapPack> packs,
         String expectedId,
         String resourceName
 ) {
  String resourcePath =
          "assets/hfr/earthmaps/bundled/" + resourceName;

  BundledEarthMapSource source =
          new BundledEarthMapSource(resourcePath);

  if (!source.exists()) {
   System.out.println(
           "[XF Earth] Bundled map is not present: "
                   + resourcePath
   );

   return;
  }

  try {
   File cachedArchive =
           cacheResource(expectedId, source);

   XFEarthMapManifest manifest =
           XFEarthMapVerifier.verify(cachedArchive);

   if (!expectedId.equals(manifest.id)) {
    throw new IOException(
            "Bundled map ID mismatch. Expected "
                    + expectedId
                    + " but found "
                    + manifest.id
    );
   }

   packs.add(
           new XFEarthMapPack(
                   manifest,
                   source,
                   XFEarthMapVerifier.sha256(cachedArchive)
           )
   );

   System.out.println(
           "[XF Earth] Accepted bundled map pack: "
                   + manifest.id
   );
  } catch (Exception exception) {
   System.err.println(
           "[XF Earth] Rejected bundled map pack: "
                   + resourcePath
   );

   exception.printStackTrace();
  }
 }

 public XFEarthMapPack find(String id) {
  for (XFEarthMapPack pack : discover()) {
   if (pack.manifest.id.equals(id)) {
    return pack;
   }
  }

  System.err.println(
          "[XF Earth] No usable map pack was found with ID: "
                  + id
  );

  return null;
 }

 public File materialize(XFEarthMapPack pack)
         throws IOException {

  if (pack.source instanceof LocalEarthMapSource) {
   LocalEarthMapSource localSource =
           (LocalEarthMapSource) pack.source;

   return localSource.getFile();
  }

  if (pack.source instanceof BundledEarthMapSource) {
   BundledEarthMapSource bundledSource =
           (BundledEarthMapSource) pack.source;

   return cacheResource(
           pack.manifest.id,
           bundledSource
   );
  }

  throw new IOException(
          "Unsupported Earth map source type: "
                  + pack.source.getClass().getName()
  );
 }

 private File cacheResource(
         String id,
         BundledEarthMapSource source
 ) throws IOException {

  if (!cache.exists()
          && !cache.mkdirs()
          && !cache.isDirectory()) {

   throw new IOException(
           "Could not create Earth map cache directory: "
                   + cache.getAbsolutePath()
   );
  }

  File temporaryFile =
          File.createTempFile(
                  id,
                  ".xfmap.tmp",
                  cache
          );

  try {
   copy(
           source.open(),
           new FileOutputStream(temporaryFile)
   );

   String hash =
           XFEarthMapVerifier.sha256(temporaryFile);

   File targetFile =
           new File(
                   cache,
                   id + "-" + hash + ".xfmap"
           );

   if (targetFile.isFile()) {
    String existingHash =
            XFEarthMapVerifier.sha256(targetFile);

    if (hash.equals(existingHash)) {
     return targetFile;
    }

    if (!targetFile.delete()) {
     throw new IOException(
             "Could not replace invalid cached Earth map: "
                     + targetFile.getAbsolutePath()
     );
    }
   }

   if (!temporaryFile.renameTo(targetFile)) {
    copy(
            new FileInputStream(temporaryFile),
            new FileOutputStream(targetFile)
    );
   }

   return targetFile;
  } finally {
   if (temporaryFile.exists()
           && !temporaryFile.delete()) {

    temporaryFile.deleteOnExit();
   }
  }
 }

 private static void copy(
         InputStream input,
         OutputStream output
 ) throws IOException {

  try {
   byte[] buffer = new byte[65536];

   int amountRead;

   while ((amountRead = input.read(buffer)) != -1) {
    output.write(
            buffer,
            0,
            amountRead
    );
   }

   output.flush();
  } finally {
   try {
    input.close();
   } finally {
    output.close();
   }
  }
 }
}