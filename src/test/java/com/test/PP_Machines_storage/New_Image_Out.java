package com.test.PP_Machines_storage;

import com.jcraft.jsch.*;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.*;

public class New_Image_Out {

    @Test
    public void testStorageDetails() {
        String host = "ap7v1.humanbrain.in";
        int port = 22;
        String user = "hbp";
        String password = "hbp";

        try {
            // Establish SSH connection
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            // Print and format command output
            String commandOutput = printCommandOutput(session, "cd /lustre/data/store10PB/repos1/iitlab/humanbrain/analytics && ls -1");
            System.out.println("Formatted command output:");
            System.out.println(formatCommandOutput(commandOutput));

            // Retrieve the list of directories
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter the biosample:");
            String biosample = scanner.nextLine();
            Set<String> providedFormats = getRemoteDirectoryListing(session, "/lustre/data/store10PB/repos1/iitlab/humanbrain/analytics/" + biosample + "/NISL");

            // Expected formats
            Set<String> expectedFormats = new HashSet<>(Arrays.asList(
                    "compressed.jp2", "dirInfo.txt", "downsampled.tif", "lossless.jp2",
                    "macro.jpg", "thumbnail.jpg", "thumbnail_original.jpg",
                    "thumbnail_small.jpg", "label.jpg"
            ));

            // Extract section numbers and group provided formats by section
            Map<String, Set<String>> sectionFormatsMap = new HashMap<>();
            for (String format : providedFormats) {
                int sectionStart = format.indexOf("SE_") + 3;
                int sectionEnd = format.indexOf('_', sectionStart);
                if (sectionEnd == -1) {
                    sectionEnd = format.indexOf('-', sectionStart);
                }
                if (sectionStart != -1 && sectionEnd != -1) {
                    String sectionNumber = format.substring(sectionStart, sectionEnd);
                    sectionFormatsMap.putIfAbsent(sectionNumber, new HashSet<String>()); // Ensure HashSet<String>
                    for (String expected : expectedFormats) {
                        if (format.endsWith(expected)) {
                            sectionFormatsMap.get(sectionNumber).add(expected);
                            break;
                        }
                    }
                }
            }

            // Check for missing formats in each section
            for (Map.Entry<String, Set<String>> entry : sectionFormatsMap.entrySet()) {
                String sectionNumber = entry.getKey();
                Set<String> providedSuffixes = entry.getValue();
                Set<String> missingFormats = new HashSet<>(expectedFormats);
                missingFormats.removeAll(providedSuffixes);
                System.out.println("Section " + sectionNumber + " missing formats: " + missingFormats);
            }

            session.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Test encountered an exception: " + e.getMessage());
        }
    }

    private String printCommandOutput(Session session, String command) throws Exception {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        InputStream in = channel.getInputStream();
        channel.connect();

        StringBuilder output = new StringBuilder();
        byte[] tmp = new byte[1024];
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
            Thread.sleep(1000);
        }

        channel.disconnect();

        return output.toString();
    }

    private String formatCommandOutput(String commandOutput) {
        String[] lines = commandOutput.split("\n");
        List<String> items = new ArrayList<>();
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                items.add(line.trim());
            }
        }

        int numColumns = 15;
        StringBuilder formattedOutput = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            formattedOutput.append(String.format("%-8s", items.get(i)));
            if ((i + 1) % numColumns == 0) {
                formattedOutput.append("\n");
            }
        }

        return formattedOutput.toString();
    }

    private Set<String> getRemoteDirectoryListing(Session session, String directory) throws Exception {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand("ls -1 " + directory);
        InputStream in = channel.getInputStream();
        channel.connect();

        StringBuilder output = new StringBuilder();
        byte[] tmp = new byte[1024];
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
            Thread.sleep(1000);
        }

        channel.disconnect();

        String[] lines = output.toString().split("\n");
        Set<String> items = new HashSet<>();
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                items.add(line.trim());
            }
        }

        return items;
    }
}
