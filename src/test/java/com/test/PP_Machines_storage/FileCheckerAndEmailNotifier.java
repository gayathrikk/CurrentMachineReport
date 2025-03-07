package com.test.PP_Machines_storage;

import com.jcraft.jsch.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class FileCheckerAndEmailNotifier {
    private static final int PORT = 22;
    private static final String USER = "hbp";
    private static final String PASSWORD = "Health#123";
    private static final Map<String, String> MACHINES = new HashMap<>();

    static {
        MACHINES.put("pp5.humanbrain.in", "/mnt/local/nvmestorage/postImageProcessor");
    }

    public static void main(String[] args) throws JSchException {
        StringBuilder emailContent = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String todayDate = sdf.format(new Date());
        boolean sendEmail = false;

        for (Map.Entry<String, String> entry : MACHINES.entrySet()) {
            String machine = entry.getKey();
            String directory = entry.getValue();

            List<String> todayFiles = new ArrayList<>();
            List<String> pendingFiles = new ArrayList<>();
            checkFiles(machine, directory, todayDate, todayFiles, pendingFiles);

            emailContent.append("================================== ").append(machine).append(" ==================================\n");

            if (!todayFiles.isEmpty()) {
                emailContent.append("\n📂 Today's Files:\n");
                emailContent.append("------------------------------------------\n");
                for (String file : todayFiles) {
                    emailContent.append(file).append("\n");
                }
                sendEmail = true;
            } else {
                emailContent.append("📅 No new files found for today.\n");
            }

            if (!pendingFiles.isEmpty()) {
                emailContent.append("\n⚠ Pending Files from Previous Days:\n");
                emailContent.append("-----------------------------------------------------------\n");
                for (String file : pendingFiles) {
                    emailContent.append(file).append("\n");
                }
                sendEmail = true;
            } else {
                emailContent.append("\n🎉 No pending files remaining!\n");
            }
            emailContent.append("\n================================== ***DONE*** ==================================\n\n");
        }

        if (sendEmail) {
            sendEmailAlert(emailContent.toString());
        }
    }

    private static void checkFiles(String machine, String directory, String todayDate, List<String> todayFiles, List<String> pendingFiles) throws JSchException {
        JSch jsch = new JSch();
        com.jcraft.jsch.Session session = jsch.getSession(USER, machine, PORT);

        try {
            session = jsch.getSession(USER, machine, PORT);
            session.setPassword(PASSWORD);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(5000);

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("ls -lh --time-style=long-iso " + directory);
            channel.setInputStream(null);
            InputStream in = channel.getInputStream();
            channel.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("total")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 6) {
                        String fileDate = parts[5];
                        if (fileDate.equals(todayDate)) {
                            todayFiles.add(line);
                        } else {
                            pendingFiles.add(line);
                        }
                    }
                }
            }
            channel.disconnect();
        } catch (Exception e) {
            System.err.println("Error connecting to " + machine + ": " + e.getMessage());
        } finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private static void sendEmailAlert(String messageBody) {
        String to = "annotation.divya@gmail.com";
        String from = "gayathri@htic.iitm.ac.in";
        String host = "smtp.gmail.com";

        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        javax.mail.Session session = javax.mail.Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("gayathri@htic.iitm.ac.in", "Gayu@0918");
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("📢 Machine Status Report");
            message.setText("================================== MACHINE STATUS ==================================\n\n"
                    + "This is an automated message:\n\n"
                    + messageBody
                    + "\n\n================================== ***DONE*** ==================================");

            System.out.println("Sending...");
            Transport.send(message);
            System.out.println("✅ Sent message successfully.");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}
