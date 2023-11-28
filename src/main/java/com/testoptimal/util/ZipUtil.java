package com.testoptimal.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipUtil {
	private static Logger logger = LoggerFactory.getLogger(ZipUtil.class);
    private static final int BUFFER_SIZE = 4096;
    
	/**
	 * zip a folder and all of its sub folders.
	 * @param dir2zip
	 * @param zos
	 * @param rootPath
	 * @param printFolderName
	 * @param ignoreFileName
	 */
	public static void zipDir(String dir2zip, ZipOutputStream zos, String rootPath, String includeRegExp_p) {
		try {
			File file = new File(dir2zip);
	        zipDirectory(file, null, zos, includeRegExp_p);
		    zos.flush();
		}
		catch (Exception e) {
			logger.error ("Error zipping directory " + dir2zip + ": " + e.getMessage());
		}
	}

	/**
	 * unzip into an existing folder
	 * @param zip
	 * @param extractTo
	 * @return
	 * @throws IOException
	 */
	public static final String unzipDir(File zip, File extractTo, String startingFolder)
			throws Exception {
		StringBuffer retBuf = new StringBuffer();
		try ( ZipFile archive = new ZipFile(zip);) {
			Enumeration<?> e = archive.entries();
			int startIdx = startingFolder.length();
			while (e.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) e.nextElement();
				String fName = entry.getName();
				if (!fName.startsWith(startingFolder) || fName.equals(startingFolder)) continue;
				fName = fName.substring(startIdx);
				File file = new File(extractTo, fName);
				if (entry.isDirectory()) {
					if (!file.exists()) {
						retBuf.append("creating dir " + fName + "\n");
						file.mkdirs();
					}
					continue;
				} 
				if (file.exists()) {
					if (!fName.endsWith(".jar")) file.delete();
					retBuf.append("replacing file " + fName + "\n");
				}
				else {
					if (!file.getParentFile().exists()) {
						file.getParentFile().mkdirs();
					}
					retBuf.append("creating new file " + fName + "\n");
				}
				
				try (
					InputStream in = archive.getInputStream(entry);
					BufferedOutputStream out = new BufferedOutputStream (new FileOutputStream(file));) {
					byte[] buffer = new byte[8192];
					int read;
	
					while (-1 != (read = in.read(buffer))) {
						out.write(buffer, 0, read);
					}
				}
//				in.close();
//				out.close();
			}
		}
		catch (Exception e) {
			logger.error("error openning zip file from archive object retrieved from db", e);
			throw new Exception (e.getMessage());
		}
		
		return retBuf.toString();
	}

	
	
    /**
     * Adds a directory to the current zip output stream
     * @param folder the directory to be  added
     * @param parentFolder the path of parent directory
     * @param zos the current zip output stream
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void zipDirectory(File folder, String parentFolder,
            ZipOutputStream zos, String includeRegExp_p) throws FileNotFoundException, IOException {
        for (File file : folder.listFiles()) {
        	String folderPath = parentFolder==null?file.getName(): parentFolder + "/" + file.getName();
            if (file.isDirectory()) {
                zipDirectory(file, folderPath, zos, includeRegExp_p);
                continue;
            }
            if (!file.getName().matches(includeRegExp_p)) {
            	continue;
            }
            zos.putNextEntry(new ZipEntry(folderPath));
            try (BufferedInputStream bis = new BufferedInputStream(
	                    new FileInputStream(file)); ) {
	            byte[] bytesIn = new byte[BUFFER_SIZE];
	            int read = 0;
	            while ((read = bis.read(bytesIn)) != -1) {
	                zos.write(bytesIn, 0, read);
	            }
	            zos.closeEntry();
            }
        }
    }
}
