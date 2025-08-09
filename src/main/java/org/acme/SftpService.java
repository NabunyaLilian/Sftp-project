package org.acme;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


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
            Session session = jsch.getSession(user, host, 22);
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
}
