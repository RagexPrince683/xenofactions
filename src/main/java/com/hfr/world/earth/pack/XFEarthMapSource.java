package com.hfr.world.earth.pack;
import java.io.*;
public interface XFEarthMapSource {
 enum Provider { BUNDLED_RESOURCE, LOCAL_FILE, EXTERNAL_INSTALLED }
 Provider getProvider(); String getDescription(); InputStream open() throws IOException;
}
