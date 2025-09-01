package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.scheduler.Scheduled;


@ApplicationScoped
public class SftpBean {

    @Inject
    SftpService sftpService;

    // Runs every day at midnight
//    @Scheduled(cron = "0 0 0 * * ?")
//    void runSftpUpload() {
//        System.out.println("Running SFTP upload");
//        sftpService.uploadFile();
//    }

//    @Scheduled(every = "10s")
//    void runSftpDownload(){
//        System.out.println("Running SFTP download");
//        sftpService.downloadAllZips();
//    }
}
