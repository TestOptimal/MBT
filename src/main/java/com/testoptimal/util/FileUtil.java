package com.testoptimal.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.util.Strings;
import org.springframework.util.FileSystemUtils;

import com.testoptimal.server.config.Config;
import com.testoptimal.util.misc.SysLogger;

public class FileUtil {

	public static long DayMillis = 24 * 60 * 60 * 1000;
	public static long WeekMillis = DayMillis * 7;
	public static long MonthMillis = DayMillis * 31;
	public static long YearMillis = DayMillis * 365;
	private static String encoding = "UTF-8";
	
	public static String getEncoding () {
		return encoding;
	}
	public static void setEncoding(String encoding_p) {
		if (StringUtil.isEmpty(encoding_p)) {
//			encoding = null;
		}
		else encoding = encoding_p;
	}
	
	public static boolean renameFile(String fromFile_p, String toFilePath_p) throws Exception {
		// File (or directory) with old name
		File file = new File(fromFile_p);

		// File (or directory) with new name
		File file2 = new File(toFilePath_p);
		
		if (file2.exists() && (toFilePath_p.endsWith(".deleted"))) {
			FileUtil.recursiveDelete(file2);
		}

		// Rename file (or directory)
		boolean success = file.renameTo(file2);
		return success;

	}

	/**
	 * deletes a file.
	 * 
	 */
	public static boolean deleteOneFile(String workingPath_p, String fileName_p) {
		return deleteOneFile(workingPath_p + fileName_p);
	}

	public static boolean deleteOneFile(String filePath_p) {
		File aFile = new File(filePath_p);
		if (aFile.exists()) {
			if (aFile.isDirectory()) {
				try {
					FileSystemUtils.deleteRecursively(aFile);
				}
				catch (Exception e) {
					return false;
				}
				return true;
			}
			else return aFile.delete();
		}
		else return true;
	}

	public static void recursiveDelete(File file) throws IOException {
		if (!file.exists()) return;
		if (file.isDirectory()) {
			// directory is empty, then delete it
			if (file.list()==null || file.list().length == 0) {
				file.delete();
			} else {
				// list all the directory contents
				String files[] = file.list();
				for (String temp: files) {
					// construct the file structure
					File fileDelete = new File(file, temp);
					// recursive delete
					recursiveDelete(fileDelete);
				}

				// check the directory again, if empty then delete it
				if (file.list().length == 0) {
					file.delete();
				}
			}
		} else {
			// if file, then delete it
			file.delete();
		}
	}


	public static void deleteAllFilesInFolder (String folderPath_p) throws IOException {
		File folder = new File(folderPath_p);
		if (!folder.isDirectory()) return;
	
		// directory is empty, then delete it
		if (folder.list()==null || folder.list().length == 0) return;
		// list all the directory contents
		String files[] = folder.list();
		for (String temp: files) {
			// construct the file structure
			File fileDelete = new File(folder, temp);
			if (fileDelete.isDirectory()) {
				recursiveDelete(fileDelete);
			}
			else fileDelete.delete();
		}
	}

	public static void deleteFilesByRegExpr (String folderPath_p, String regExpr_p) throws IOException {
		File folder = new File(folderPath_p);
		if (!folder.isDirectory()) return;
	
		// directory is empty, then delete it
		if (folder.list()==null || folder.list().length == 0) return;
		// list all the directory contents
		String files[] = folder.list();
		Pattern pattern = Pattern.compile(regExpr_p);
		for (String temp: files) {
			Matcher matcher = pattern.matcher(temp);
			if (matcher.find()) {
				try {
					File fileDelete = new File(folder, temp);
					fileDelete.delete();
				}
				catch (Exception e) {
					SysLogger.logWarn("Unable to delete " + temp);
				}
	        }
		}
	}


	public static boolean exists(String filePath_p) {
		File aFile = new File(filePath_p);
		return aFile.exists();

	}

	public static String concatFilePath(String rootPath_p, String ... filePathPieces_p) {
		String ret = rootPath_p;
		for (String pathPiece: filePathPieces_p) {
			if (!Strings.isBlank(pathPiece)) {
				ret = concatFilePath(ret, pathPiece);
			}
		}
		return ret;
	}

	public static String concatFilePath(String folder_p, String fileName_p) {
		if (folder_p == null)
			folder_p = "";
		else
			folder_p = folder_p.trim();
		if (folder_p.length() > 0) {
			char lastChar = folder_p.charAt(folder_p.length() - 1);
			if (lastChar != '/' && lastChar != '\\') {
				folder_p = folder_p + File.separator;
			}
		}
		if (fileName_p == null)
			fileName_p = "";
		String ret = folder_p + fileName_p;
		ret = resolveRelativeFilePath(ret);
		return ret;
	}

	
	public static String resolveRelativeFilePath (String filePath_p) {
		if (File.separator.equals("/")) {
			filePath_p = filePath_p.replace("\\", File.separator);
		}
		else {
			filePath_p = filePath_p.replace("/", File.separator);
		}
	
		while (true) {
			int idx = filePath_p.indexOf(".." + File.separator);
			if (idx<0) return filePath_p;
			
			String piece1 = filePath_p.substring(0, idx-1);
			int idx2 = piece1.lastIndexOf(File.separator);
			if (idx2<0) return filePath_p;
			filePath_p = piece1.substring(0,idx2) + File.separator + filePath_p.substring(idx + 3);
		}
	}
	
	public static boolean createFolder(String folderPath_p) {
		File aFile = new File(folderPath_p);
		boolean ret = aFile.mkdir();
		return ret;
	}

	/**
	 * writes the post upload file content to a file with the name specified.
	 * 
	 * @param fileName_p
	 * @param contentBuf_p
	 * @throws Exception
	 */
	public static void writeToFile(String fileName_p, StringBuffer contentBuf_p)
			throws Exception {
		writeToFile(fileName_p, contentBuf_p.toString());
	}
	public static void writeToFile(String fileName_p, String contentBuf_p)
			throws Exception {
		try (
			FileOutputStream outS = new FileOutputStream(fileName_p);) {
			if (encoding==null) {
				outS.write(contentBuf_p.getBytes());
			}
			else {
				outS.write(contentBuf_p.getBytes(encoding));
			}
		}
		return;
	}

	/**
	 * reads a file and return the file content in StringBuffer. Assuming the
	 * file content is text.
	 * 
	 * @param filePath_p
	 * @return
	 * @throws Exception
	 */
	public static StringBuffer readFile(String filePath_p) throws Exception {
		StringBuffer retBuf = new StringBuffer();
		try (
				DataInputStream in = new DataInputStream(new FileInputStream(filePath_p));
				BufferedReader br = encoding==null?
					new BufferedReader(new InputStreamReader(in)):
					new BufferedReader(new InputStreamReader(in, encoding));
			) {
			String line = br.readLine();
			while (line != null) {
				retBuf.append(line).append("\n");
				line = br.readLine();
			}
		} 
		return retBuf;
	}

	public static void appendFile(String filePath_p, String content_p) throws Exception {
		appendFile (filePath_p, content_p, false);
	}
	
	public static void appendFile (String filePath_p, String content_p, boolean createIfNotExist_p) throws Exception {
		File aFile = new File(filePath_p);
		if (!aFile.exists()) {
			if (createIfNotExist_p) {
				writeToFile(filePath_p, "");
			}
			else {
				throw new Exception ("File not found: " + filePath_p);
			}
		}
		FileOutputStream appendedFile = new FileOutputStream (filePath_p, true);
		try {
			if (encoding==null) {
				appendedFile.write("\n".getBytes());
				appendedFile.write(content_p.getBytes());
			}
			else {
				appendedFile.write("\n".getBytes(encoding));
				appendedFile.write(content_p.getBytes(encoding));
			}
		}
		finally {
			appendedFile.close();
		}
	}
	
	/**
	 * reads a file into array list line by line.
	 * 
	 * @param filePath_p
	 * @return
	 * @throws Exception
	 */
	public static List<String> readFileIntoArray(String filePath_p, boolean removeComments_p)
			throws Exception {
		List<String> retList = new java.util.ArrayList<String>();
		if (!FileUtil.exists(filePath_p)) return retList;
		FileInputStream fstream = new FileInputStream(filePath_p);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br;
		if (encoding==null) {
			br = new BufferedReader(new InputStreamReader(in));
		}
		else {
			br = new BufferedReader(new InputStreamReader(in, encoding));
		}
		try {
			String line = br.readLine();
			while (line != null) {
				if (!line.startsWith("##") && !line.startsWith("//") || !removeComments_p) {
					retList.add(line);
				}
				line = br.readLine();
			}
		} finally {
			if (br != null)
				br.close();
		}

		return retList;

	}

	/**
	 * returns a list of names of submodels.
	 */
	public static List<String> getModelList(File srchFolder_p, boolean subModelOnly_p) {
		java.util.ArrayList<String> retList = new java.util.ArrayList<String>();

		// TODO: need to search nested folder to find all subfolders.
		// File folderReader = new File(Config.getModelRoot(), "");
		String[] fileList = srchFolder_p.list();
		// boolean hideDemoApp =
		// Util.isTrue(Config.getProperty("hideDemoApps"));
		for (int i = 0; fileList != null && i < fileList.length; i++) {
			if (fileList[i].startsWith(".") || fileList[i].startsWith("_DELETED_")) {
				continue;
			}
			File tempFile = new File(srchFolder_p, fileList[i]);
			if (ModelFile.isModelFolder(tempFile)) {
				if (subModelOnly_p && !(fileList[i].startsWith("_sub_"))) {
					continue;
				}
				// if (hideDemoApp && fileList[i].startsWith("DEMO_")) continue;
				File fileReader = new File(srchFolder_p, fileList[i]);
				if (!fileReader.isDirectory())
					continue;
				retList.add(fileList[i]);
			} 
			else {
				if (tempFile.isDirectory() && !tempFile.getName().equalsIgnoreCase("_template")) {
					retList.addAll(getModelList(tempFile, subModelOnly_p));
				}
			}
		}
		return retList;
	}


	public static boolean copyFile(String srFile, String dtFile)
			throws Exception {
		OutputStream out = null;
		InputStream in = null;
		try {
			File f1 = new File(srFile);
			if (!f1.exists())
				return false;

			File f2 = new File(dtFile);
			in = new FileInputStream(f1);

			// For Append the file.
			// OutputStream out = new FileOutputStream(f2,true);

			// For Overwrite the file.
			out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			return true;
		} finally {
			if (out != null)
				out.close();
			if (in != null)
				in.close();
		}
	}

	/**
	 * purges files that match the pattern and only keep the versions of the
	 * matched files per keepCriteria_p passed in.
	 * 
	 * @param folderPath_p
	 * @param filePattern_p
	 * @param keepCriteria_p
	 */
	public static void purgeFiles(String folderPath_p, String filePattern_p,
			String keepCriteria_p) {
		String logPth = Config.getLogPath();
		File folderReader = new File(logPth, "");
		FilenameFilter filter = new MscriptLogFileFilter(filePattern_p);
		String[] fileList = folderReader.list(filter);
		int verLimit = Integer.MAX_VALUE;
		if (StringUtil.isEmpty(keepCriteria_p))
			keepCriteria_p = "5";
		long keepMillis = 0;
		try {
			verLimit = Integer.parseInt(keepCriteria_p);
		} catch (Exception e) {
			int numField = Integer.parseInt(keepCriteria_p.substring(0,
					keepCriteria_p.length() - 1));
			try {
				switch (keepCriteria_p.charAt(keepCriteria_p.length() - 1)) {
				case 'D':
				case 'd':
					keepMillis -= numField * DayMillis;
					break;
				case 'W':
				case 'w':
					keepMillis -= numField * WeekMillis;
					break;
				case 'M':
				case 'm':
					keepMillis -= numField * MonthMillis;
					break;
				case 'Y':
				case 'y':
					keepMillis -= numField * YearMillis;
					break;
				}
			} catch (Exception e2) {
				keepMillis = 0;
			}
		}

		Map sortedList = new TreeMap();
		for (int i = 0; fileList != null && i < fileList.length; i++) {
			File fileReader = new File(folderReader, fileList[i]);
			long modMillis = fileReader.lastModified();
			if (modMillis < keepMillis)
				continue; // skip
			sortedList.put(new Long(modMillis), fileList[i]);
		}

		java.util.Iterator it = sortedList.entrySet().iterator();
		int deleteNum = sortedList.size() - verLimit;
		while (it.hasNext() && deleteNum > 0) {
			java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
			deleteOneFile(folderPath_p, (String) entry.getValue());
			deleteNum--;
		}
	}

	/**
	 * reads the file and return its content in byte[],
	 * 
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public static byte[] readFileToByteArray(String filePath) throws Exception {
		File file = new File(filePath);
		try ( InputStream is = new FileInputStream(file);) {
			long length = file.length();
			byte[] fileBytes = new byte[(int) length];
			int offset = 0;
			int numRead = 0;
			while (offset < fileBytes.length
					&& (numRead = is.read(fileBytes, offset, fileBytes.length
							- offset)) >= 0) {
				offset += numRead;
			}
			if (offset < fileBytes.length) {
				throw new IOException("Could not read file " + file.getName());
			}
			is.close();
			return fileBytes;
		}
	}

	public static String extractRelativePath(String rootPath_p, String path_p) {
		if (rootPath_p.equals(path_p + File.separator)) return "";
		if (rootPath_p.length()>=path_p.length()) return "";
		return path_p.substring(rootPath_p.length());
	}

	public static int fileDiff(String filePath1_p, String filePath2_p)
			throws Exception {
		boolean br1End = false;
		boolean br2End = false;
		BufferedReader br1 = null;
		BufferedReader br2 = null;
		if (FileUtil.exists(filePath1_p)) {
			br1 = new BufferedReader(new FileReader(filePath1_p));
		} else {
			br1End = true;
		}
		if (FileUtil.exists(filePath1_p)) {
			br2 = new BufferedReader(new FileReader(filePath2_p));
		} else {
			br2End = true;
		}
		int bufSize = 500;
		List<String> buf1 = new java.util.ArrayList<String>(bufSize);
		List<String> buf2 = new java.util.ArrayList<String>(bufSize);
		int diffCount = 0;
		String temp;
		while (true) {
			if (!br1End) {
				if (buf1.size() < bufSize) {
					temp = br1.readLine();
					if (temp == null)
						br1End = true;
					else
						buf1.add(temp);
				}
			}
			if (!br2End) {
				if (buf2.size() < bufSize) {
					temp = br2.readLine();
					if (temp == null)
						br2End = true;
					else
						buf2.add(temp);
				}
			}

			if (buf1.size() < bufSize && !br1End || buf2.size() < bufSize
					&& !br2End) {
				continue;
			}

			if (buf1.isEmpty()) {
				diffCount += buf2.size();
				break;
			}

			if (buf2.isEmpty()) {
				diffCount += buf1.size();
				break;
			}

			String t1 = buf1.get(0);
			int foundI = -1;
			for (int i = 0; i < buf2.size(); i++) {
				if (t1.equals(buf2.get(i))) {
					foundI = i;
					break;
				}
			}

			if (foundI < 0) { // no match found
				buf1.remove(0);
				diffCount++;
			} else {
				buf1.remove(0);
				for (int i = foundI; i >= 0; i--) {
					buf2.remove(i);
				}
				diffCount += foundI;
			}
		}
		return diffCount;
	}

	/**
	 * return null if file is not found after all attempts.
	 * 
	 * @param modelName_p
	 * @param filePath_p
	 * @param searchPref_p
	 * @return
	 * @throws Exception
	 */
	public static File findFile(String modelFolderPath_p, String filePath_p,
			String searchPref_p) throws Exception {
		if (StringUtil.isEmpty(searchPref_p))
			searchPref_p = "DRM"; // data, report then model folders
		File file = new File(filePath_p);
		if (file.exists())
			return file;

		String folderPath;
		for (int i = 0; i < searchPref_p.length(); i++) {
			switch (searchPref_p.charAt(i)) {
				case 'D':
					folderPath = FileUtil.concatFilePath(modelFolderPath_p, "dataset");
					file = new File(FileUtil.concatFilePath(folderPath, filePath_p));
					if (file.exists())
						return file;
					break;
				case 'R':
					folderPath = FileUtil.concatFilePath(modelFolderPath_p, "report");
					file = new File(FileUtil.concatFilePath(folderPath, filePath_p));
					if (file.exists())
						return file;
					break;
				case 'M':
					folderPath = modelFolderPath_p;
					file = new File(FileUtil.concatFilePath(folderPath, filePath_p));
					if (file.exists())
						return file;
					break;
				case 'S':
					folderPath = FileUtil.concatFilePath(modelFolderPath_p, "snapscreen");
					file = new File(FileUtil.concatFilePath(folderPath, filePath_p));
					if (file.exists())
						return file;
					break;
			}
		}
		return null;
	}

	/**
	 * returns true if sourceString_p matches the searchString_p. First char in
	 * searchString_p can contains ~ for regexp, = for exact match (case
	 * insensitive) or none of these char for contains match (case insensitive)
	 * 
	 * @param sourceString_p
	 * @param searchString_p
	 * @return
	 */
	private static boolean isMatch(String sourceString_p, String searchString_p) {
		if (searchString_p.startsWith("~")) {
			Pattern pattern = Pattern.compile(searchString_p.substring(1));
			Matcher m = pattern.matcher(sourceString_p);
			return m.matches();
		} else if (searchString_p.startsWith("=")) {
			return (sourceString_p
					.equalsIgnoreCase(searchString_p.substring(1)));
		} else {
			return sourceString_p.toUpperCase().contains(
					searchString_p.toUpperCase());
		}
	}

	public static List<File> searchFile(File folder_p, String searchString_p,
			boolean returnFirstFound_p) {
		List<File> retList = new java.util.ArrayList<File>();
		if (!folder_p.exists()) return retList;
		
		String[] fileList = folder_p.list();

		// two passes, first model folders only, then subfolders
		for (String file : fileList) {
			if (file.startsWith("_DELETED_") || file.startsWith("."))
				continue;
			File aFile = new File(folder_p, file);
			if (isMatch(file, searchString_p)) {
				retList.add(aFile);
			}
		}

		if (returnFirstFound_p && !retList.isEmpty()) {
			return retList;
		}

		for (String file : fileList) {
			if (file.equals(".svn"))
				continue;
			File aFile = new File(folder_p, file);
			if (aFile.isFile())
				continue;
			if (ModelFile.isModelFolder(aFile))
				continue;
			retList.addAll(searchFile(aFile, searchString_p, returnFirstFound_p));

			if (returnFirstFound_p && !retList.isEmpty()) {
				return retList;
			}
		}

		return retList;
	}

	public static List<File> searchModel(File folder_p, String searchString_p,
			boolean returnFirstFound_p) {
		List<File> retList = new java.util.ArrayList<File>();
		if (!folder_p.exists()) return retList;
		
		String[] fileList = folder_p.list();

		// two passes, first model folders only, then subfolders
		for (String file : fileList) {
			if (file.startsWith("_DELETED_") || file.startsWith("."))
				continue;
			File aFile = new File(folder_p, file);
			if (!ModelFile.isModelFolder(aFile)) {
				continue;
			}
			if (isMatch(file, searchString_p)) {
				retList.add(aFile);
			}
		}

		if (returnFirstFound_p && !retList.isEmpty()) {
			return retList;
		}

		for (String file : fileList) {
			if (file.equals(".svn"))
				continue;
			File aFile = new File(folder_p, file);
			if (aFile.isFile())
				continue;
			if (ModelFile.isModelFolder(aFile))
				continue;
			retList.addAll(searchModel(aFile, searchString_p, returnFirstFound_p));

			if (returnFirstFound_p && !retList.isEmpty()) {
				return retList;
			}
		}

		return retList;
	}

	public static File findFirst(File folder_p, String fileName_p) {
		String[] fileList = folder_p.list();
		
		List<File> folderList = new java.util.ArrayList<>();
		for (String file : fileList) {
			File aFile = new File(folder_p, file);
			if (file.equals(fileName_p)) {
				return aFile;
			}
			if (aFile.isDirectory()) {
				folderList.add(aFile);
			}
		}

		for (File aFile: folderList) {
			File bFile = findFirst (aFile, fileName_p);
			if (bFile!=null) {
				return bFile;
			}
		}
		return null;
	}

	/**
	 * copies file/folder from source to destination.
	 * 
	 * @param srcPath
	 * @param dstPath
	 * @param recursive_p
	 * @return number of files copied
	 * @throws Exception
	 */
	public static int copyFolder(String fromPath, String toPath,
			boolean overrideExistingFile_p) throws Exception {
		File srcPath = new File(fromPath);
		if (!srcPath.exists())
			throw new Exception("folder.notexist");
		File dstPath = new File(toPath);
		int fileCount = 0;
		if (srcPath.isDirectory()) {
			if (!dstPath.exists()) {
				dstPath.mkdir();
			}

			String files[] = srcPath.list();
			for (int i = 0; i < files.length; i++) {
				fileCount += copyFolder(FileUtil.concatFilePath(
						srcPath.getAbsolutePath(), files[i]),
						FileUtil.concatFilePath(dstPath.getAbsolutePath(),
								files[i]), overrideExistingFile_p);
			}
		} else {
			if (dstPath.exists() && !overrideExistingFile_p)
				return fileCount;

			if (srcPath.exists()) {
				InputStream in = new FileInputStream(srcPath);
				OutputStream out = new FileOutputStream(dstPath);
				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}

				in.close();
				out.close();
				fileCount++;
			}
		}

		return fileCount;
	}

	/**
	 * moves file/folder from source to destination.
	 * 
	 * @param srcPath
	 * @param dstPath
	 * @return true if moved successful
	 * @throws Exception
	 */
	public static boolean moveFolder(String fromPath, String toPath)
			throws Exception {
		File srcPath = new File(fromPath);
		if (!srcPath.exists())
			throw new Exception("folder.notexist");
		File dstPath = new File(toPath);
		if (dstPath.exists())
			throw new Exception("folder.alreadyexist");

		return (srcPath.renameTo(dstPath));
	}
	
	public static String getFileExtension(String filePath_p) {
		int idx = filePath_p.lastIndexOf(".");
		if (idx < 0) return "";
		else return filePath_p.substring(idx+1);
	}
	
	public static String readFromStream(InputStream is_p) throws IOException {
	    int ch;
	    StringBuilder sb = new StringBuilder();
	    while((ch = is_p.read())!= -1)
	        sb.append((char)ch);
	    return sb.toString();
	}
	
	public static File [] getFileList (String folderPath_p) throws Exception {
		File folderReader = new File(folderPath_p, "");
		if (!folderReader.exists()) {
			throw new Exception ("Folder does not exist: " + folderReader.getAbsolutePath());
		}
		
		return folderReader.listFiles();
	}
	/**
	 * returns the path to the model folder absolute path.
	 * @param modelName_p
	 * @return null if not found
	 */
	public static String findModelFolder(String modelName_p) {
		if (!modelName_p.endsWith(".fsm")) {
			modelName_p = modelName_p + ".fsm";
		}
		String mRoot = Config.getModelRoot();
		List<File> fileList = FileUtil.searchModel( new File(mRoot), "=" + modelName_p, true);
		if (fileList.isEmpty()) return null;
		return fileList.get(0).getAbsolutePath() + File.separator;
	}

	/**
	 * relative model path
	 * @param modelName_p
	 * @return
	 */
	public static String findModelPath(String modelName_p) {
		if (!modelName_p.endsWith(".fsm")) {
			modelName_p = modelName_p + ".fsm";
		}
		File rootFile = new File(Config.getModelRoot());
		File file = FileUtil.findFirst( rootFile, modelName_p);
		String ret = null;
		if (file!=null) {
			ret = file.getAbsolutePath().substring((int) rootFile.getAbsolutePath().length()+1);
			ret = ret.substring(0, ret.length()-modelName_p.length());
			ret = ret.replace("\\", "/");
		}
		return ret;
	}
	
	public static void sanctionFilePath (String filePath_p) throws Exception {
		if (filePath_p.contains("..")) throw new Exception ("Invalid file path: " + filePath_p);
	}
	
	public static String cleanseFileName (String fName_p) {
		return fName_p.replaceAll("[\\\\/:*?\"<>|]", "");
	}
	
}
