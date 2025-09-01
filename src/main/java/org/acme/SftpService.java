package org.acme;

import com.jcraft.jsch.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Vector;


@ApplicationScoped
public class SftpService {

    @Inject
    @ConfigProperty(name = "cts.host")
    String host;

    @Inject
    @ConfigProperty(name = "cts.user")
    String user;

    @Inject
    @ConfigProperty(name = "ssh.key")
    String privateKey;

    @Inject
    @ConfigProperty(name = "cts.upload.path")
    String ctsUploadPath;

    @Inject
    @ConfigProperty(name = "cts.download.path")
    String ctsDownloadPath;

    @Inject
    @ConfigProperty(name = "local.upload.path")
    String localUploadPath;

    @Inject
    @ConfigProperty(name = "local.download.path")
    String localDownloadPath;

    @Inject
    @ConfigProperty(name = "cts.port")
    int port;

    @Inject
    @ConfigProperty(name = "known.hosts")
    String knownHosts;

    @Inject
    @ConfigProperty(name = "local.sent.path")
    String sentPath;


    /**
     *  Transfers a file between two remote SFTP servers by downloading it
     *  from the source server and then uploading it to the destination server.
     * <p>The method first downloads the file from <b>Server A</b> to a temporary
     * local path, then uploads it to <b>Server B</b>. After the transfer is complete,
     * the temporary file is deleted.</p>
     *
     * <h3>Process Flow:</h3>
     * <ol>
     *   <li>Download file from source server (Server A) → temporary local file</li>
     *   <li>Upload temporary local file → destination server (Server B)</li>
     *   <li>Delete the temporary local file</li>
     * </ol>
     *
     * <h3>Usage Example:</h3>
     * <pre>{@code
     * boolean success = transferFile(
     *     "source.example.com", "userA", "passwordA", "/remote/source/file.txt",
     *     "destination.example.com", "userB", "passwordB", "/remote/dest/file.txt"
     * );
     *
     * if (success) {
     *     System.out.println("File transferred successfully!");
     * } else {
     *     System.err.println("File transfer failed.");
     * }
     * }</pre>
     *
     * @param serverAHost   hostname or IP address of the source SFTP server
     * @param userA  username for the source server
     * @param passA  password for the source server
     * @param remotePathA   remote path of the file to download from the source server
     * @param serverBHost   hostname or IP address of the destination SFTP server
     * @param userB    username for the destination server
     * @param passB     password for the destination server
     * @param remotePathB  remote path (including filename) where the file will be uploaded
     *  *                      on the destination server
     * @return {@code true} if the file was successfully transferred from Server A
     *         to Server B, {@code false} otherwise
     *
     * @throws Exception if any error occurs during download, upload, or cleanup
     */
    public boolean transferFile(String serverAHost, String userA, String passA, String remotePathA,
                                String serverBHost, String userB, String passB, String remotePathB) {
        String tempLocalPath = "/tmp/temp_transfer_file";

        try {
            // Step 1: Download from Server A
            boolean downloaded = downloadFileFromServer(serverAHost, userA, passA, remotePathA, tempLocalPath);
            if (!downloaded) return false;

            // Step 2: Upload to Server B
            boolean uploaded = uploadFileToServer(serverBHost, userB, passB, tempLocalPath, remotePathB);
            if (!uploaded) return false;

            // Clean up temp file
            new File(tempLocalPath).delete();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Downloads a file from a remote SFTP server using Username and Password.
     *
     * <p>
     *     This method establishes an SFTP connection to the specified server using a username and password,
     *     then downloads the provided file from the remote directory.
     * </p>
     *
     * <h3>Usage Example:</h3>
     * <pre>{@code
     * downloadFileFromServer(
     *  "sftp.example.com",
     *  "sftp-user",
     *  "password",
     *  "/remote/path/file.txt
     *  "/local/path/file.txt",
     *  );
     * }</pre>
     * @param host  Remote server hostname or IP address
     * @param user   SFTP username to log in with
     * @param password   SFTP user's password
     * @param localPath  Absolute path to the local path
     * @param remotePath  Absolute path (including filename) where the file should be downloaded
     *                   from on the remote server
     * @throws JSchException    if the SSH connection or authentication fails
     * @throws SftpException    if the download fails (e.g., invalid path, no permissions)
     * @throws FileNotFoundException if the remote file does not exist
     * @throws IOException  if there is an error reading the file
     */
    private boolean downloadFileFromServer(String host, String user, String password, String remotePath, String localPath) {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect();

            FileOutputStream fos = new FileOutputStream(localPath);
            sftp.get(remotePath, fos);
            fos.close();

            sftp.disconnect();
            session.disconnect();

            System.out.println("✅ File downloaded from Server A.");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Download failed: " + e.getMessage());
            return false;
        }
    }


    /**
     * Uploads a file to a remote SFTP server using Username and Password.
     *
     * <p>
     *     This method establishes an SFTP connection to the specified server using a username and password,
     *     then uploads the provided file to the remote directory.
     * </p>
     *
     * <h3>Usage Example:</h3>
     * <pre>{@code
     * uploadFileToServer(
     *  "sftp.example.com",
     *  "sftp-user",
     *  "password",
     *  "/local/path/file.txt",
     *  "/remote/path/file.txt
     *  );
     * }</pre>
     * @param host  Remote server hostname or IP address
     * @param user   SFTP username to log in with
     * @param password   SFTP user's password
     * @param localPath  Absolute path to the local file to upload
     * @param remotePath  Absolute path (including filename) where the file should be uploaded
     *                   on the remote server
     * @throws JSchException    if the SSH connection or authentication fails
     * @throws SftpException    if the upload fails (e.g., invalid path, no permissions)
     * @throws FileNotFoundException if the local file does not exist
     * @throws IOException  if there is an error reading the local file
     */
    private boolean uploadFileToServer(String host, String user, String password, String localPath, String remotePath) {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 4022);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect();

            FileInputStream fis = new FileInputStream(localPath);
            sftp.put(fis, remotePath);
            fis.close();

            sftp.disconnect();
            session.disconnect();

            System.out.println("✅ File uploaded to Server B.");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Upload failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Establishes and returns an authenticated SFTP channel using SSH key authentication.
     *
     * <p>This method initializes JSch, loads the private key for authentication,
     * sets known hosts for server fingerprint verification, and establishes
     * an SFTP channel to the configured server. The caller is responsible
     * for closing the channel and the associated session after use.</p>
     *
     * <h3>Authentication Flow:</h3>
     * <ol>
     *   <li>Load the private SSH key from the configured path</li>
     *   <li>Load known hosts file for server fingerprint verification</li>
     *   <li>Create and connect an SSH session with the specified user, host, and port</li>
     *   <li>Open and connect an SFTP channel from the session</li>
     * </ol>
     *
     * <h3>Usage Example:</h3>
     * <pre>{@code
     * ChannelSftp sftp = setupSftpChannel();
     * try {
     *     sftp.put("/local/path/file.txt", "/remote/path/file.txt");
     * } finally {
     *     sftp.disconnect();
     *     sftp.getSession().disconnect();
     * }
     * }</pre>
     *
     * @return an authenticated {@link ChannelSftp} object connected to the server
     *
     * @throws JSchException if authentication or channel setup fails
     *
     * @implNote In production, ensure {@code knownHosts} points to a valid known_hosts file
     *           for host fingerprint verification. For testing, strict host key checking
     *           can be disabled using:
     *           <pre>{@code session.setConfig("StrictHostKeyChecking", "no"); }</pre>
     */
    private ChannelSftp setupSftpChannel() throws JSchException {
        JSch jsch = new JSch();

        System.out.println("Authentication started-------------------------");

        // Load private key
        jsch.addIdentity(privateKey);
        System.out.println("Authentication ended-------------------------");

        // Note:Important in production
        // Load known hosts for server fingerprint verification
        jsch.setKnownHosts(knownHosts);
        System.out.println("Server Authentication ended-------------------------");

        // Create session
        Session session = jsch.getSession(user, host, port);
        //Note: Only needed in test
        // session.setConfig("StrictHostKeyChecking", "no");
        session.setTimeout(60000);
        session.connect();

        System.out.println("Session created-------------------------");

        ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
        sftp.connect();
        System.out.println("SFTP Channel opened-------------------------");
        return sftp;
    }

    /**
     * Uploads all files in a specified local path to a remote SFTP server using SSH key authentication.
     *
     * <p>This method establishes an SFTP connection to the specified server
     * using a private SSH key, then uploads the provided local files to the
     * given remote directory. The connection is closed automatically once
     * the transfer is complete.</p>
     * @throws JSchException           if the SSH connection or authentication fails
     * @throws SftpException           if the upload fails (e.g., invalid path, no permission)
     * @throws FileNotFoundException   if the local file does not exist
     * @throws IOException             if there is an error reading the local file
     */
    public boolean uploadFile() {
        System.out.println("privateKey:----------------- " + privateKey);
        System.out.println("ctsUploadPath:---------------" + ctsUploadPath);
        System.out.println("localUploadPath:--------------" + localUploadPath);
        System.out.println("knownHosts:-------------------" + knownHosts);
        System.out.println("host:-------------------------" + host);
        System.out.println("port:-------------------------" + port);
        System.out.println("user:-------------------------" + user);
        System.out.println("ctsDownloadPath:---------------" + ctsDownloadPath);
        System.out.println("localDownloadPath:---------------" + localDownloadPath);

        try {

            ChannelSftp sftp = setupSftpChannel();

            System.out.println("File creation started-------------------------");
            File localDir = new File(localUploadPath);
            // list all files in local directory
            File[] files = localDir.listFiles();
            if(files == null ||  files.length == 0) {
                System.out.println("No files present in" + localUploadPath +"-------------------------");
                return false;
            }
            System.out.println("files:---------------------" + Arrays.toString(files));

            final int  BUFFER_SIZE = 1024 * 1024;
            // Iterate files that end with .zip
            for(File file : files) {
                String fileName = file.getName();
                if(fileName.toLowerCase().endsWith(".zip")) {
                    // For each file upload the file to remote location plus file initials e.g MX
                    String countryDir = ctsUploadPath + "/" + fileName.substring(0,2);
                    try {
                        sftp.cd(countryDir);
                    }catch(SftpException e) {
                        e.printStackTrace();
                    }
                    System.out.println("remotePath:----------------------------" + countryDir);
                    String localPath = localUploadPath + fileName;
                    System.out.println("localPath:-----------------------------" + localPath);
                    try(BufferedInputStream bis = new BufferedInputStream( new FileInputStream(localPath), BUFFER_SIZE)){
                        sftp.put(bis, fileName, new SftpProgressMonitor() {
                            private long transferred = 0;
                            private long fileSize = file.length();

                            @Override
                            public void init(int op, String src, String dest, long max) {
                                System.out.printf("Started upload: %s (%d bytes)%n", src, fileSize);
                            }

                            @Override
                            public boolean count(long bytes) {
                                transferred += bytes;
                                if (transferred % (1024 * 1024) < bytes) { // log every ~1 MB
                                    System.out.printf("Progress: %.2f%%%n", (transferred * 100.0) / fileSize);
                                }
                                return true;
                            }

                            @Override
                            public void end() {
                                System.out.println("Upload complete:----------------- " + fileName);
                                try {
                                    Path source = Paths.get(localPath);
                                    Path destination = Paths.get(sentPath, fileName);
                                    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, ChannelSftp.OVERWRITE);
                    }
                }
            }

            System.out.println("file uploaded successfully-------------------------");


            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Downloads all zip files from a remote SFTP Server using SSH Key Authentication.
     *
     * <p>This method establishes an SFTP connection to the specified server
     * using a private SSH key, then downloads all zip files from the remote path
     * and saves them to the given local directory. The connection is closed automatically
     * once the transfer is complete
     * </p>
     *
     * @throws JSchException  if the SSH connection or authentication fails
     * @throws SftpException   if the download fails (e.g., file not found, no permission
     * @throws FileNotFoundException   if the local path is invalid or cannot be written to
     * @throws IOException   if there is an error writing the file locally
     */
    public boolean downloadAllZips() {
        System.out.println("ctsDownloadPath:---------------" + ctsDownloadPath);
        System.out.println("localDownloadPath:---------------" + localDownloadPath);;

        try {
            ChannelSftp sftp = setupSftpChannel();

            // Make sure local directory exists
            File localDir = new File(localDownloadPath);
            if (!localDir.exists()) {
                localDir.mkdirs();
                System.out.println("making directories:-----------------------------");
            }

            // List remote files in the directory
            Vector<ChannelSftp.LsEntry> files = sftp.ls(ctsDownloadPath);
            System.out.println("listing files directories:-----------------------------");
            final int BUFFER_SIZE = 1024 * 1024;

            System.out.println("files-------------------------------------------: " + files);

            for (ChannelSftp.LsEntry entry : files) {
                String fileName = entry.getFilename();

                // Skip current and parent directories
                if (fileName.equals(".") || fileName.equals("..")) {
                    continue;
                }

                // Only process .zip files
                if (fileName.toLowerCase().endsWith(".zip")) {
                    String remoteFile = ctsDownloadPath + fileName;
                    String localFile = localDownloadPath + File.separator + fileName;

                    System.out.println("Downloading: " + remoteFile);
//                    sftp.get(remoteFile, localFile);
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(localFile), BUFFER_SIZE)) {
                        sftp.get(remoteFile, bos); // downloads the file into the buffered stream
                    }
                }
            }

            System.out.println("All .zip files downloaded successfully-------------------------");

            sftp.disconnect();
            sftp.getSession().disconnect();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
