package com.test.PP_Machines_storage;

import com.jcraft.jsch.*;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Image_out {

    @Test
    public void testStorageDetails() {
        String host = "ap7v1.humanbrain.in";
        int port = 22;
        String user = "hbp";
        String password = "hbp";

        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("cd /lustre/data/store10PB/repos1/iitlab/humanbrain/analytics && ls -1");
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
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String commandOutput = output.toString();
            System.out.println("Command Output:");
            System.out.println(formatOutput(commandOutput));

            channel.disconnect();
            session.disconnect();

            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter the name of the biosample to navigate into:");
            String biosample = scanner.nextLine();

            executeCommandInDirectory(host, port, user, password, biosample);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Test encountered an exception: " + e.getMessage());
        }
    }

    private String formatOutput(String commandOutput) {
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

    private void executeCommandInDirectory(String host, int port, String user, String password, String dirName) {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            String command = "cd /lustre/data/store10PB/repos1/iitlab/humanbrain/analytics/" + dirName + "/NISL && ls";
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
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Contents of directory " + dirName + "/NISL:");
            System.out.println(output.toString());

            channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Test encountered an exception: " + e.getMessage());
        }
    }
}
