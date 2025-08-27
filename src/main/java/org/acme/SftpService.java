package org.acme;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import jakarta.enterprise.context.ApplicationScoped;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;


@ApplicationScoped
public class SftpService {
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

    public boolean uploadFile(UploadRequest request) {
        try {

            JSch jsch = new JSch();

            System.out.println("Authentication started-------------------------");

            // Load private key
            jsch.addIdentity(request.privateKey);

            // Note:Important in production
            // Load known hosts for server fingerprint verification
            jsch.setKnownHosts(request.knownHosts);

            // Create session
            Session session = jsch.getSession(request.user, request.host, request.port);
            //Note: Only needed in test
           // session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            System.out.println("Session created-------------------------");

            ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect();

            try(FileInputStream fis = new FileInputStream(request.localFile)){
                String fileName = Paths.get(request.localFile).getFileName().toString();
                sftp.put(fis, request.remotePath + fileName);
            }

            System.out.println("file uploaded successfully-------------------------");

            sftp.disconnect();
            session.disconnect();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean downloadAllZips(DownloadRequest request) {
        try {
            JSch jsch = new JSch();

            System.out.println("Authentication started-------------------------");

            // Load private key
            jsch.addIdentity(request.privateKey);

            // Load known_hosts for server fingerprint verification
//            Note: Needed in production
            jsch.setKnownHosts(request.knownHosts);
            // Create session
            Session session = jsch.getSession(request.user, request.host, request.port);
//            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            System.out.println("Session created-------------------------");

            ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect();

            // Make sure local directory exists
            File localDir = new File(request.localDir);
            if (!localDir.exists()) {
                localDir.mkdirs();
            }

            // List remote files in the directory
            Vector<ChannelSftp.LsEntry> files = sftp.ls(request.remotePath);

            System.out.println("files-------------------------------------------: " + files);

            for (ChannelSftp.LsEntry entry : files) {
                String fileName = entry.getFilename();

                // Skip current and parent directories
                if (fileName.equals(".") || fileName.equals("..")) {
                    continue;
                }

                // Only process .zip files
                if (fileName.toLowerCase().endsWith(".zip")) {
                    String remoteFile = request.remotePath + fileName;
                    String localFile = request.localDir + File.separator + fileName;

                    System.out.println("Downloading: " + remoteFile);
                    sftp.get(remoteFile, localFile);
                }
            }

            System.out.println("All .zip files downloaded successfully-------------------------");

            sftp.disconnect();
            session.disconnect();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean upload( UploadRequest request) {
        SSHClient ssh = new SSHClient();
        try{
            System.out.println("Session started--------------------------------");
            ssh.addHostKeyVerifier(new PromiscuousVerifier());

            ssh.connect(request.host, request.port);
            System.out.println("Connection started--------------------------------");

            KeyProvider keys = ssh.loadKeys(request.privateKey, null, null);

            ssh.authPublickey(request.user, keys);

            System.out.println("private key "+ request.privateKey +" ----------------------------");

            SFTPClient sftpClient = ssh.newSFTPClient();

            try {
                sftpClient.put(request.localFile, request.remotePath);

                System.out.println("File uploaded successfully -------------------------------");
            } finally {
                sftpClient.close();
            }
            ssh.disconnect();
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return  false;
        }
    }
}
