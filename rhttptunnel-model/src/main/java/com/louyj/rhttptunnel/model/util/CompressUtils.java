package com.louyj.rhttptunnel.model.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * Create at 2020年7月3日
 *
 * @author Louyj
 *
 */
public class CompressUtils {

	public static void unZipFile(File source, File destination) throws FileNotFoundException, IOException {
		try (ZipArchiveInputStream archive = new ZipArchiveInputStream(
				new BufferedInputStream(new FileInputStream(source)))) {
			ZipArchiveEntry entry;
			while ((entry = archive.getNextZipEntry()) != null) {
				File file = new File(destination, entry.getName());
				file.getParentFile().mkdirs();
				IOUtils.copy(archive, new FileOutputStream(file));
			}
		}
	}

	public static void zipFolder(File source, File destination) throws IOException, ArchiveException {
		Collection<File> fileList = FileUtils.listFiles(source, null, true);
		OutputStream archiveStream = new FileOutputStream(destination);
		ArchiveOutputStream archive = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP,
				archiveStream);
		for (File file : fileList) {
			String entryName = getEntryName(source, file);
			ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
			archive.putArchiveEntry(entry);
			BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
			IOUtils.copy(input, archive);
			input.close();
			archive.closeArchiveEntry();
		}
		archive.finish();
		archiveStream.close();
	}

	private static String getEntryName(File source, File file) throws IOException {
		int index = source.getAbsolutePath().length() + 1;
		String path = file.getCanonicalPath();
		return path.substring(index);
	}

}
