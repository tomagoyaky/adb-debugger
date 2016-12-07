package com.tomagoyaky.local.debug;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

	private static final int BUFFER = 102400; // 100k

	@SuppressWarnings("unused")
	private static void dirChecker(String dir) {
		File f = new File(dir);
		if (!f.isDirectory()) {
			f.mkdirs();
		}
	}

	public static void deleteDir(String file) {
		deleteFile(new File(file));
	}

	/**
	 * @param file
	 */
	public static boolean deleteFile(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				deleteFile(files[i]);
			}
		}
		// file.delete();
		return delFileInternal(file);
	}

	public static boolean deleteFile(String path) {

		return delFileInternal(new File(path));
	}

	private static boolean delFileInternal(File file) {
		if (file == null)
			return false;
		if (!file.exists() || (file.isDirectory() && file.length() != 0)) {
			return false;
		}
		int retry = 3;
		boolean ret = file.delete();
		while (!ret) {
			if (retry == 0) {
				break;
			}
			ret = file.delete();
			retry--;
		}
		return ret;
	}

	public static void extraAll(String zipFilePath, String unZipDir) {

		File source = new File(zipFilePath);
		if (source.exists()) {
			ZipInputStream zis = null;
			BufferedOutputStream bos = null;
			try {
				zis = new ZipInputStream(new FileInputStream(source));
				ZipEntry entry = null;
				while ((entry = zis.getNextEntry()) != null) {
					File target = new File(unZipDir, entry.getName());
					if(entry.isDirectory()){
						target.mkdirs();
						continue;
					} else{
						if(!target.getParentFile().exists()){
							target.getParentFile().mkdirs();
						}
					}
					// 写入文件
					bos = new BufferedOutputStream(new FileOutputStream(target));
					int read = 0;
					byte[] buffer = new byte[BUFFER];
					while ((read = zis.read(buffer, 0, buffer.length)) != -1) {
						bos.write(buffer, 0, read);
					}
					bos.flush();
				}
				zis.closeEntry();
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				IOUtils.closeQuietly(zis, bos);
			}
		}
	}
	/**
	 * 提取文件
	 * @param file zip文件
	 * @param entryFiles zip中文件相对路径
	 * @param recFiles 提取输出文件路径
	 */
	public static void extraZipEntry(File file, String[] entryFiles, String[] recFiles) throws IOException {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(file);
			byte[] buff = creatBuffBytes();
			int len;
			final int size = entryFiles.length;
			for (int i = 0; i < size; i++) {
				final ZipEntry entry = zipFile.getEntry(entryFiles[i]);
				InputStream stream = null;
				FileOutputStream fos = null;
				try {
					stream = zipFile.getInputStream(entry);
					fos = new FileOutputStream(recFiles[i]);
					while ((len = stream.read(buff)) > 0) {
						fos.write(buff, 0, len);
					}
				} catch (IOException e) {
					throw e;
				} finally {
					IOUtils.closeQuietly(fos, stream);
				}
				// Logger.Logger.debug("[Zip:Extra] " + entryFiles[i] + " (->) " +
				// recFiles[i]);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			IOUtils.closeQuietly(zipFile);
		}
	}

	/**
	 * 替换文件
	 * 
	 * @param zipFile
	 *            zip 文件
	 * @param srcFiles
	 *            zip 中文件相对路径
	 * @param newFiles
	 *            对应要替换的文件
	 * @throws IOException
	 */
	public static void replaceZipEntry(File zipFile, String[] srcFiles, String[] newFiles) throws IOException {
		File tempFile = File.createTempFile(zipFile.getName(), null);
		tempFile.delete();
		tempFile.deleteOnExit();

		FileUtils.copyFile(zipFile, tempFile);
		FileInputStream fis = null;
		FileOutputStream fos = null;
		ZipInputStream zin = null;
		ZipOutputStream zout = null;

		try {
			fis = new FileInputStream(tempFile);
			fos = new FileOutputStream(zipFile);
			zin = new ZipInputStream(fis);
			zout = new ZipOutputStream(fos);
			ZipEntry entry = zin.getNextEntry();

			final int size = (srcFiles == null ? 0 : srcFiles.length);
			int dels = 0;
			byte[] buff = creatBuffBytes();
			int len;
			while (entry != null) {
				String name = entry.getName();
				if (size != 0 && dels != size) {
					int rp = -1;
					for (int i = 0; i < size; i++) {
						if (srcFiles[i].equals(name)) {
							rp = i;
							// Logger.Logger.debug("[Zip:Replace] " + zipFile.getName()
							// + " (\\) " + name);
							break;
						}
					}
					if (rp >= 0) {
						FileInputStream fis2 = null;
						try {
							zout.putNextEntry(new ZipEntry(name));
							fis2 = new FileInputStream(newFiles[rp]);
							while ((len = fis2.read(buff)) > 0) {
								zout.write(buff, 0, len);
							}
							zout.closeEntry();
						} catch (IOException e) {
							e.printStackTrace();
							throw e;
						} finally {
							IOUtils.closeQuietly(fis2);
						}
						dels++;
						entry = zin.getNextEntry();
						continue;
					}
				}

				zout.putNextEntry(new ZipEntry(name));
				while ((len = zin.read(buff)) > 0) {
					zout.write(buff, 0, len);
				}
				zout.closeEntry();
				entry = zin.getNextEntry();
			}
			zout.finish();
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			IOUtils.closeQuietly(zin, zout, fis, fos);
			tempFile.delete();
		}
	}

	/**
	 * 删除内部子文件
	 * 
	 * @param zipFile
	 *            zip 文件
	 * @param files
	 *            需要删除的文件 相对路径
	 * @throws IOException
	 */
	public static void deleteZipEntry(File zipFile, String[] files) throws IOException {
		File tempFile = File.createTempFile(zipFile.getName(), null);
		tempFile.delete();
		tempFile.deleteOnExit();

		FileUtils.copyFile(zipFile, tempFile);

		FileInputStream fis = null;
		FileOutputStream fos = null;
		ZipInputStream zin = null;
		ZipOutputStream zout = null;

		try {
			fis = new FileInputStream(tempFile);
			fos = new FileOutputStream(zipFile);
			zin = new ZipInputStream(fis);
			zout = new ZipOutputStream(fos);
			ZipEntry entry = zin.getNextEntry();
			final int delSize = (files == null ? 0 : files.length);
			int dels = 0;

			byte[] buff = creatBuffBytes();
			int len;
			while (entry != null) {
				String name = entry.getName();
				if (delSize != 0 && dels != delSize) {
					boolean toBeDeleted = false;
					for (String file : files) {
						if (file.equals(name)) {
							toBeDeleted = true;
							// Logger.Logger.debug("[Zip:Delete] " + zipFile.getName() +
							// " (-) " + file);
							break;
						}
					}
					if (toBeDeleted) {
						entry = zin.getNextEntry();
						dels++;
						continue;
					}
				}
				zout.putNextEntry(new ZipEntry(name));
				while ((len = zin.read(buff)) > 0) {
					zout.write(buff, 0, len);
				}
				zout.closeEntry();
				entry = zin.getNextEntry();
			}
			zout.finish();
		} catch (IOException e) {
			throw e;
		} finally {
			IOUtils.closeQuietly(zin, zout, fis, fos);
			tempFile.delete();
		}

	}

	/**
	 * 删除内部子文件
	 * 
	 * @param zipFile
	 *            zip 文件
	 * @param files
	 *            需要删除的文件 相对路径
	 * @throws IOException
	 */
	public static void deleteZipEntry2(File zipFile, String[] files) throws IOException {
		File tempFile = File.createTempFile(zipFile.getName(), null);
		tempFile.delete();
		tempFile.deleteOnExit();

		FileUtils.copyFile(zipFile, tempFile);

		FileInputStream fis = null;
		FileOutputStream fos = null;
		ZipInputStream zin = null;
		ZipOutputStream zout = null;

		try {
			fis = new FileInputStream(tempFile);
			fos = new FileOutputStream(zipFile);
			zin = new ZipInputStream(fis);
			zout = new ZipOutputStream(fos);
			ZipEntry entry = zin.getNextEntry();
			byte[] buf = creatBuffBytes();
			while (entry != null) {
				String name = entry.getName();
				boolean toBeDeleted = false;
				for (String file : files) {
					if (file.equals(name)) {
						toBeDeleted = true;
						// Logger.Logger.debug("[Zip:Delete] " + zipFile.getName() + "
						// (-) " + file);
						break;
					}
				}
				if (!toBeDeleted) {
					zout.putNextEntry(new ZipEntry(name));
					int len;
					while ((len = zin.read(buf)) > 0) {
						zout.write(buf, 0, len);
					}
					zout.closeEntry();
				}
				entry = zin.getNextEntry();
			}
			zout.finish();
		} catch (IOException e) {
			throw e;
		} finally {
			IOUtils.closeQuietly(zin, zout, fis, fos);
			tempFile.delete();
		}

	}

	/**
	 * 删除某个文件夹
	 * 
	 * @param zipFile
	 * @param dir
	 * @throws IOException
	 */
	public static void deleteEntryDir(File zipFile, String dir) throws IOException {
		File tempFile = File.createTempFile(zipFile.getName(), null);
		tempFile.delete();
		tempFile.deleteOnExit();

		FileUtils.copyFile(zipFile, tempFile);

		ZipInputStream zin = null;
		ZipOutputStream zout = null;

		try {
			zin = new ZipInputStream(new FileInputStream(tempFile));
			zout = new ZipOutputStream(new FileOutputStream(zipFile));
			ZipEntry entry = zin.getNextEntry();

			byte[] buff = creatBuffBytes();
			String name = null;
			while (entry != null) {
				name = entry.getName();
				if (name.startsWith(dir)) {
					entry = zin.getNextEntry();
					continue;
				}
				zout.putNextEntry(new ZipEntry(name));
				int len;
				while ((len = zin.read(buff)) > 0) {
					zout.write(buff, 0, len);
				}
				zout.closeEntry();
				entry = zin.getNextEntry();
			}
			zin.close();
			zout.finish();
			// Logger.Logger.debug("[Zip:Delete] " + zipFile.getName() + " (-) " + dir);
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			IOUtils.closeQuietly(zin, zout);
			tempFile.delete();
		}
	}

	/**
	 * 添加文件到zip 包
	 * 
	 * @param zipFile
	 *            目标zip 文件
	 * @param entryName
	 *            添加的entry文件名
	 * @param mapFile
	 *            entry映射的外部文件路径
	 * @throws IOException
	 */
	public static void addEntryItem(File zipFile, String entryName, String mapFile) throws Exception {

		File tempFile = File.createTempFile(zipFile.getName(), null);
		ZipInputStream zin = null;
		ZipOutputStream zout = null;

		try {
			zin = new ZipInputStream(new FileInputStream(zipFile));
			zout = new ZipOutputStream(new FileOutputStream(tempFile));
			ZipEntry entry = zin.getNextEntry();

			byte[] buff = creatBuffBytes();
			int len;
			while (entry != null) {
				zout.putNextEntry(new ZipEntry(entry.getName()));
				while ((len = zin.read(buff)) > 0) {
					zout.write(buff, 0, len);
				}
				entry = zin.getNextEntry();
			}
			FileInputStream fis2 = null;
			try {
				zout.putNextEntry(new ZipEntry(entryName));
				fis2 = new FileInputStream(mapFile);
				while ((len = fis2.read(buff)) > 0) {
					zout.write(buff, 0, len);
				}
			} catch (IOException e) {
				throw e;
			} finally {
				IOUtils.closeQuietly(fis2);
			}
			zout.finish();
		} catch (Exception e) {
			throw e;
		} finally {
			IOUtils.closeQuietly(zin, zout);
			FileUtils.copyFile(tempFile, zipFile);
			tempFile.delete();
		}
	}

	/**
	 * 添加多个文件到zip 包
	 * 
	 * @param zipFile
	 *            目标zip 文件
	 * @param entryNames
	 *            添加的entry文件名
	 * @param mapFiles
	 *            entry映射的外部文件路径
	 * @throws Exception
	 */
	public static void addEntrys(File zipFile, String[] entryNames, String[] mapFiles) throws Exception {

		if (mapFiles.length != entryNames.length) {
			// Logger.Logger.error("params's number not match. entryNames:" +
			// entryNames.length + ", mapFiles:" + mapFiles.length);
			return;
		}
		for (int i = 0; i < mapFiles.length; i++) {
			addEntryItem(zipFile, entryNames[i], mapFiles[i]);
		}
	}

	private static byte[] creatBuffBytes() {
		return new byte[4096];
	}

}