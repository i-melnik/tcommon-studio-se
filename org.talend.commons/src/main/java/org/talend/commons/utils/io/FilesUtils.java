// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.commons.utils.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.i18n.internal.Messages;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 * 
 */
public final class FilesUtils {

    private FilesUtils() {
    }

    public static final String[] SVN_FOLDER_NAMES = new String[] { ".svn", "_svn" }; //$NON-NLS-1$  //$NON-NLS-2$

    private static final String MIGRATION_FILE_EXT = ".mig"; //$NON-NLS-1$

    public static boolean isSVNFolder(String name) {
        if (name != null) {
            name = name.toLowerCase();
            for (int i = 0; i < SVN_FOLDER_NAMES.length; i++) {
                if (SVN_FOLDER_NAMES[i].equals(name) || name.endsWith(SVN_FOLDER_NAMES[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isSVNFolder(File file) {
        if (file != null) {
            return isSVNFolder(file.getName());
        }
        return false;
    }

    public static boolean isSVNFolder(IResource resource) {
        if (resource != null) {
            return isSVNFolder(resource.getName());
        }
        return false;
    }

    /**
     * @param source
     * @param target
     * @param emptyTargetBeforeCopy
     * @param sourceFolderFilter
     * @param sourceFileFilter
     * @param copyFolder
     * @param synchronize if set to true, synchronize source and target, remove non existing folders/files.
     * @param monitorWrap
     * @throws IOException
     */
    public static void copyFolder(File source, File target, boolean emptyTargetBeforeCopy, final FileFilter sourceFolderFilter,
            final FileFilter sourceFileFilter, boolean copyFolder, boolean synchronize, IProgressMonitor... monitorWrap)
            throws IOException {
        copyFolder(source, target, target.getAbsolutePath(), emptyTargetBeforeCopy, sourceFolderFilter, sourceFileFilter,
                copyFolder, synchronize, monitorWrap);
    }

    /**
     * DOC nrousseau Comment method "copyFolder".
     * 
     * @param source
     * @param target
     * @param emptyTargetBeforeCopy
     * @param sourceFolderFilter
     * @param sourceFileFilter
     * @param copyFolder
     * @param monitorWrap
     * @throws IOException
     */
    public static void copyFolder(File source, File target, boolean emptyTargetBeforeCopy, final FileFilter sourceFolderFilter,
            final FileFilter sourceFileFilter, boolean copyFolder, IProgressMonitor... monitorWrap) throws IOException {
        copyFolder(source, target, target.getAbsolutePath(), emptyTargetBeforeCopy, sourceFolderFilter, sourceFileFilter,
                copyFolder, false, monitorWrap);
    }

    private static void copyFolder(File source, File target, String targetBaseFolder, boolean emptyTargetBeforeCopy,
            final FileFilter sourceFolderFilter, final FileFilter sourceFileFilter, boolean copyFolder, boolean synchronize,
            IProgressMonitor... monitorWrap) throws IOException {
        // cf bug 14658
        boolean needAvoidCopyItself = false;
        if (targetBaseFolder.equals(source.getAbsolutePath())) {
            needAvoidCopyItself = true;
        }
        IProgressMonitor monitor = null;
        if (monitorWrap != null && monitorWrap.length == 1) {
            monitor = monitorWrap[0];
        }

        if (!target.exists()) {
            target.mkdirs();
        }

        if (emptyTargetBeforeCopy) {
            emptyFolder(target);
        }

        FileFilter folderFilter = new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.isDirectory() && (sourceFolderFilter == null || sourceFolderFilter.accept(pathname));
            }

        };
        FileFilter fileFilter = new FileFilter() {

            public boolean accept(File pathname) {
                return !pathname.isDirectory() && (sourceFileFilter == null || sourceFileFilter.accept(pathname));
            }

        };

        if (!copyFolder || (copyFolder && !needAvoidCopyItself)) {
            Map<String, File> foldersToDelete = new HashMap<String, File>();
            if (synchronize) {
                for (File file : target.listFiles()) {
                    if (file.isDirectory()) {
                        foldersToDelete.put(file.getName(), file);
                    }
                }
            }
            for (File current : source.listFiles(folderFilter)) {
                if (foldersToDelete.keySet().contains(current.getName())) {
                    foldersToDelete.remove(current.getName());
                }
                if (monitor != null && monitor.isCanceled()) {
                    throw new OperationCanceledException(Messages.getString("FilesUtils.operationCanceled")); //$NON-NLS-1$
                }
                if (copyFolder) {
                    File newFolder = new File(target, current.getName());
                    newFolder.mkdir();
                    copyFolder(current, newFolder, targetBaseFolder, emptyTargetBeforeCopy, sourceFolderFilter, sourceFileFilter,
                            copyFolder, synchronize);
                } else {
                    copyFolder(current, target, targetBaseFolder, emptyTargetBeforeCopy, sourceFolderFilter, sourceFileFilter,
                            copyFolder, synchronize);
                }
            }
            if (synchronize) {
                for (File file : foldersToDelete.values()) {
                    removeFolder(file, true);
                }
            }
        }

        Map<String, File> filesToDelete = new HashMap<String, File>();
        if (synchronize) {
            for (File file : target.listFiles()) {
                if (!file.isDirectory()) {
                    filesToDelete.put(file.getName(), file);
                }
            }
        }
        for (File current : source.listFiles(fileFilter)) {
            if (filesToDelete.keySet().contains(current.getName())) {
                filesToDelete.remove(current.getName());
            }

            if (monitor != null && monitor.isCanceled()) {
                throw new OperationCanceledException(""); //$NON-NLS-1$
            }
            File out = new File(target, current.getName());
            copyFile(current, out);
        }
        if (synchronize) {
            for (File file : filesToDelete.values()) {
                file.delete();
            }
        }

    }

    private static void emptyFolder(File toEmpty) {
        final File[] listFiles = toEmpty.listFiles(getExcludeSystemFilesFilter());
        for (File current : listFiles) {
            if (current.isDirectory()) {
                emptyFolder(current);
            }
            current.delete();
        }
    }

    public static void copyFile(File source, File target) throws IOException {
        // Need to recopy the file in one of these cases:
        // 1. target doesn't exists (never copied)
        // 2. if the target exists, compare their sizes, once defferent, for the copy.
        // 2. target exists but source has been modified recently(not used right now)

        if (!target.exists() || source.length() != target.length() || source.lastModified() > target.lastModified()) {
            copyFile(new FileInputStream(source), target);
        }
    }

    public static void removeFile(File target) {
        if (target.exists() && target.isFile()) {
            target.delete();
        }
    }

    public static void copyFile(InputStream source, File target) throws IOException {
        FileOutputStream fos = null;
        try {
            if (!target.getParentFile().exists()) {
                target.getParentFile().mkdirs();
            }

            fos = new FileOutputStream(target);
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = source.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
        } finally {
            try {
                source.close();
            } catch (Exception e) {
            }
            try {
                fos.close();
            } catch (Exception e) {
            }
        }

    }

    public static void replaceInFile(String regex, String fileName, String replacement) throws IOException {
        InputStream in = new FileInputStream(fileName);
        StringBuffer buffer = new StringBuffer();
        try {
            InputStreamReader inR = new InputStreamReader(in);
            BufferedReader buf = new BufferedReader(inR);
            String line;
            while ((line = buf.readLine()) != null) {
                buffer.append(line.replaceAll(regex, replacement)).append("\n"); //$NON-NLS-1$
            }
        } finally {
            in.close();
        }

        OutputStream os = new FileOutputStream(fileName);
        os.write(buffer.toString().getBytes());
        os.close();
    }

    public static List<URL> getFilesFromFolder(Bundle bundle, String path, String extension) {
        // List<URL> toReturn = new ArrayList<URL>();
        //
        // Enumeration entryPaths = bundle.getEntryPaths(path);
        // for (Enumeration enumer = entryPaths; enumer.hasMoreElements();) {
        // String fileName = (String) enumer.nextElement();
        // if (fileName.endsWith(extension)) {
        // URL url = bundle.getEntry(fileName);
        // try {
        // toReturn.add(FileLocator.toFileURL(url));
        // } catch (IOException e) {
        // CommonExceptionHandler.process(e);
        // }
        // }
        // }
        // return toReturn;

        return getFilesFromFolder(bundle, path, extension, true, false);
    }

    public static List<URL> getFilesFromFolder(Bundle bundle, String path, String extension, boolean absoluteURL, boolean nested) {
        List<URL> toReturn = new ArrayList<URL>();

        Enumeration entryPaths = bundle.getEntryPaths(path);
        if (entryPaths == null) {
            return toReturn;
        }
        for (Enumeration enumer = entryPaths; enumer.hasMoreElements();) {
            String fileName = (String) enumer.nextElement();
            if (fileName.endsWith(extension)) {
                URL url = bundle.getEntry(fileName);
                if (absoluteURL) {
                    try {
                        toReturn.add(FileLocator.toFileURL(url));
                    } catch (IOException e) {
                        CommonExceptionHandler.process(e);
                    }
                } else {
                    toReturn.add(url);
                }
            } else {
                if (nested) {
                    List<URL> subResult = getFilesFromFolder(bundle, fileName, extension, absoluteURL, nested);
                    toReturn.addAll(subResult);
                }
            }
        }
        return toReturn;
    }

    public static FileFilter getExcludeSystemFilesFilter() {
        FileFilter filter = new FileFilter() {

            public boolean accept(File pathname) {
                return !isSVNFolder(pathname) && !pathname.toString().endsWith(".dummy"); //$NON-NLS-1$ //$NON-NLS-2$
            }

        };
        return filter;
    }

    public static FileFilter getAcceptJARFilesFilter() {
        FileFilter filter = new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.toString().toLowerCase().endsWith(".jar") || pathname.toString().toLowerCase().endsWith(".zip");//$NON-NLS-1$ //$NON-NLS-2$
            }

        };
        return filter;
    }

    public static FileFilter getAcceptModuleFilesFilter() {
        FileFilter filter = new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.toString().toLowerCase().endsWith(".jar") || pathname.toString().toLowerCase().endsWith(".zip") || pathname.toString().toLowerCase().endsWith(".properties");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }

        };
        return filter;
    }

    public static String[] getAcceptJARFilesSuffix() {
        return new String[] { "*.jar;*.properties;*.zip;*.dll;*.so" };//$NON-NLS-1$
    }

    public static FileFilter getAcceptPMFilesFilter() {
        FileFilter filter = new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.toString().endsWith(".pm"); //$NON-NLS-1$
            }

        };
        return filter;
    }

    /**
     * Load in a list all lines of the given file.
     * 
     * @throws IOException
     */
    public static List<String> getContentLines(String filePath) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
        List<String> lines = new ArrayList<String>();
        try {
            String line;
            while ((line = in.readLine()) != null) {
                lines.add(line);
            }
        } finally {
            in.close();
        }
        return lines;
    }

    /**
     * .
     * 
     * @param path
     * @param pathIsFilePath if true the given path has a filename at last segment so this segment is not processed
     * @throws IOException
     */
    public static void createFoldersIfNotExists(String path, boolean pathIsFilePath) throws IOException {
        Path completePath = new Path(path);
        IPath pathFolder = null;
        if (pathIsFilePath) {
            pathFolder = completePath.removeLastSegments(1);
        } else {
            pathFolder = completePath;
        }

        File folder = new File(pathFolder.toOSString());
        if (!folder.exists()) {

            int size = pathFolder.segmentCount();
            for (int i = 0; i < size; i++) {
                folder = new File(pathFolder.uptoSegment(i + 1).toOSString());
                if (!folder.exists()) {
                    folder.mkdir();
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            // createFoldersIfNotExists("c:\\test\\test1/test2", false);
            // createFoldersIfNotExists("c:\\test10\\test11/test20/test.pl", true);
            unzip("d:/tFileOutputPDF.zip", "d:/temp"); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * DOC amaumont Comment method "removeDirectory".
     * 
     * @param b
     */
    public static boolean removeFolder(String pathFolder, boolean recursiveRemove) {
        File folder = new File(pathFolder);
        if (folder.isDirectory()) {
            return removeFolder(folder, recursiveRemove);
        }
        return false;
    }

    /**
     * DOC amaumont Comment method "removeFolder".
     * 
     * @param current
     * @param removeRecursivly
     */
    public static boolean removeFolder(File folder, boolean removeRecursivly) {
        if (removeRecursivly) {
            for (File current : folder.listFiles()) {
                if (current.isDirectory()) {
                    removeFolder(current, true);
                } else {
                    current.delete();
                }
            }
        }
        return folder.delete();
    }

    public static void removeEmptyFolder(File folder) {
        if (!folder.isDirectory()) {
            return;
        }
        File[] children = folder.listFiles();
        if (children == null) {
            folder.delete();
        } else {
            for (File current : children) {
                removeEmptyFolder(current);
            }
        }
        children = folder.listFiles();
        if (children == null || children.length == 0) {
            folder.delete();
        }
    }

    public static String extractPathFolderFromFilePath(String filePath) {
        Path completePath = new Path(filePath);
        return completePath.removeLastSegments(1).toOSString();
    }

    /**
     * Unzip the component file to the user folder.
     * 
     * @param zipFile The component zip file
     * @param targetFolder The user folder
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static void unzip(String zipFile, String targetFolder) throws Exception {
        Exception exception = null;
        ZipFile zip = new ZipFile(zipFile);
        byte[] buf = new byte[8192];

        try {
            Enumeration<ZipEntry> enumeration = (Enumeration<ZipEntry>) zip.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry entry = enumeration.nextElement();

                File file = new File(targetFolder, entry.getName());

                if (entry.isDirectory()) {
                    if (!file.exists()) {
                        file.mkdir();
                    }
                } else {

                    InputStream zin = zip.getInputStream(entry);
                    OutputStream fout = new FileOutputStream(file);
                    // check if parent folder exists
                    File dir = file.getParentFile();
                    if (dir.isDirectory() && !dir.exists()) {
                        dir.mkdirs();
                    }

                    try {
                        while (true) {
                            int bytesRead = zin.read(buf);
                            if (bytesRead == -1) { // end of file
                                break;
                            }
                            fout.write(buf, 0, bytesRead);

                        }
                        fout.flush();
                    } catch (Exception e) {
                        exception = e;
                        // stop looping
                        return;
                    } finally {
                        zin.close();
                        fout.close();
                    }
                }
            }
        } catch (Exception e) {
            exception = e;
        } finally {
            zip.close();

            if (exception != null) {
                // notify caller with exception
                throw exception;
            }
        }
    }

    /**
     * DOC sgandon Comment method "getAllFilesFromFolder".
     * 
     * @param sampleFolder
     * @param arrayList
     * @param filenameFilter
     */
    public static void getAllFilesFromFolder(File sampleFolder, ArrayList<File> fileList, FilenameFilter filenameFilter) {
        File[] folderFiles = sampleFolder.listFiles(filenameFilter);
        if (fileList != null && folderFiles != null) {
            Collections.addAll(fileList, folderFiles);
        }
        File[] allFolders = sampleFolder.listFiles(new FileFilter() {

            public boolean accept(File arg0) {
                return arg0.isDirectory();
            }
        });
        if (allFolders != null) {
            for (File folder : allFolders) {
                getAllFilesFromFolder(folder, fileList, filenameFilter);
            }
        }
    }

    /**
     * DOC according to the replace string map to migrate files of given folders from old content to new ones.
     * 
     * @param migFolder folder to migrate
     * @param acceptFileExtentionNames extention name of the files which should to be migrated
     * @param replaceStringMap the replace string map {key=oldString, value=newString}
     * @param log logger to record logs
     * @return true if success, false if get exceptions
     */
    public static boolean migrateFolder(File migFolder, final String[] acceptFileExtentionNames,
            Map<String, String> replaceStringMap, Logger log) {
        boolean result = true;

        ArrayList<File> fileList = new ArrayList<File>();
        getAllFilesFromFolder(migFolder, fileList, new FilenameFilter() {

            public boolean accept(File dir, String name) {
                for (String extName : acceptFileExtentionNames) {
                    if (name.endsWith(extName)) {
                        return true;
                    }
                }
                return false;
            }
        });
        log.info("-------------- Migrating " + fileList.size() + " files");

        int counter = 0;
        int errorCounter = 0;
        Throwable error = null;

        for (File sample : fileList) {
            log.info("-------------- Migrating (" + counter++ + ") : " + sample.getAbsolutePath());
            try {
                BufferedReader fileReader = new BufferedReader(new FileReader(sample));
                BufferedWriter fileWriter = new BufferedWriter(new FileWriter(new File(sample.getAbsolutePath()
                        + MIGRATION_FILE_EXT)));

                while (fileReader.ready()) {
                    String line = fileReader.readLine();
                    for (String key : replaceStringMap.keySet()) {
                        line = line.replaceAll(key, replaceStringMap.get(key));
                    }
                    fileWriter.append(line);
                    fileWriter.newLine();
                }

                fileWriter.flush();
                fileWriter.close();
            } catch (Exception e) {
                error = e;
                errorCounter++;
                log.error("!!!!!!!!!!!  Error transforming (" + sample.getAbsolutePath() + ")\n" + e.getMessage(), e);
            }
            log.info("-------------- Migration done of " + counter + " files"
                    + (errorCounter != 0 ? (",  there are " + errorCounter + " files in error.") : "."));
        }

        if (error != null) {
            result = false;
        } else {
            // remove original files and rename new ones to old ones
            for (File sample : fileList) {
                boolean isDeleted = sample.delete();
                log.info(sample.getAbsolutePath() + (isDeleted ? " is deleted." : " failed to delete."));
                boolean isrenamed = new File(sample.getAbsolutePath() + MIGRATION_FILE_EXT).renameTo(sample); //$NON-NLS-1$
                log.info(sample.getAbsolutePath() + MIGRATION_FILE_EXT + (isrenamed ? " is renamed." : " failed to rename."));
            }
        }

        return result;
    }
}
