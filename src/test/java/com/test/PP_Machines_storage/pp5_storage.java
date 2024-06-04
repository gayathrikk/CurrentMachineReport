package com.test.PP_Machines_storage;

import com.jcraft.jsch.*;

import java.io.*;

import org.testng.Assert;
import org.testng.annotations.Test;

public class pp5_storage {
    @Test
    public void testStorageDetails() {
        // Set up SSH connection
        JSch jsch = new JSch();
        Session session = null;
        try {
            // Replace these with your SSH server details
            String user = "hbp";
            String host = "pp5.humanbrain.in";
            String password = "Health#123";
            int port = 22;

            // Establish SSH session
            session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            // Execute command on SSH server
            Channel channel = session.openChannel("exec");
            // Command to retrieve storage details for /dev/mapper devices and /dev/sdb3
            ((ChannelExec) channel).setCommand("df -h /dev/mapper/vg--store1-lv_store1 /dev/sdb3");
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(System.err);

            // Get output
            InputStream in = channel.getInputStream();
            channel.connect();
            byte[] tmp = new byte[1024];
            StringBuilder output = new StringBuilder();
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    output.append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if (in.available() > 0) continue;
                    System.out.println("Exit status: " + channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                }
            }

            // Parse and format output as table
            String[] lines = output.toString().split("\n");
            System.out.println("+------------------------------------+------+-------+-------+--------+---------------------+");
            System.out.println("|       Filesystem                   | Size | Used  | Avail |  Use%  | Mounted on          |");
            System.out.println("+------------------------------------+------+-------+-------+--------+---------------------+");
            
            boolean testFailed = false; // Flag to check if the test should fail
            
            for (int i = 1; i < lines.length; i++) {
                String[] parts = lines[i].trim().split("\\s+");
                System.out.printf("| %-34s | %4s | %5s | %5s | %5s | %-19s |\n", parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);

                // Check if the Use% exceeds 50%
                int usePercentage = Integer.parseInt(parts[4].replace("%", ""));
                if (usePercentage > 20) {
                    testFailed = true;
                }
            }
            System.out.println("+------------------------------------+------+-------+-------+--------+---------------------+");

            channel.disconnect();
            session.disconnect();

            Assert.assertFalse(testFailed, "Test failed because one or more filesystems have Use% above 20%");

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test encountered an exception: " + e.getMessage());
        }
    }
}