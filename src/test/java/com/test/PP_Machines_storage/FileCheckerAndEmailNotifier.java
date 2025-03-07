package com.test.PP_Machines_storage;

import com.jcraft.jsch.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import com.jcraft.jsch.Session;


public class FileCheckerAndEmailNotifier {
    private static final int PORT = 22;
    private static final String USER = "appUser";
    private static final String PASSWORD = "Brain@123";

    private static final Map<String, String> MACHINES = new HashMap<>();

    static {
        MACHINES.put("pp1.humanbrain.in", "/store/nvmestorage/postImageProcessor");
        MACHINES.put("pp2.humanbrain.in", "/mnt/local/nvmestorage/postImageProcessor");
        MACHINES.put("pp3.humanbrain.in", "/mnt/local/nvmestorage/postImageProcessor");
        MACHINES.put("pp4.humanbrain.in", "/mnt/local/nvmestorage/postImageProcessor");
        MACHINES.put("pp5.humanbrain.in", "/mnt/local/nvmestorage/postImageProcessor");
        MACHINES.put("pp7.humanbrain.in", "/mnt/local/nvmestorage/postImageProcessor");
        MACHINES.put("qd4.humanbrain.in", "/mnt/local/nvme2/postImageProcessor");
    }

    public static void main(String[] args) {
        StringBuilder emailContent = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String todayDate = sdf.format(new Date());

        for (Map.Entry<String, String> entry : MACHINES.entrySet()) {
            String machine = entry.getKey();
            String directory = entry.getValue();

            List<String> todayFiles = new ArrayList<>();
            List<String> pendingFiles = new ArrayList<>();

            checkFiles(machine, directory, todayDate, todayFiles, pendingFiles);

            emailContent.append("================================== ").append(machine).append(" ==================================\n");

            if (!todayFiles.isEmpty()) {
                emailContent.append("📂 **Today's Files:**\n");
                emailContent.append("-----------------------------------------------------------------------------------------------------------\n");
                for (String file : todayFiles) {
                    emailContent.append(file).append("\n");
                }
            } else {
                emailContent.append("📅 No new files found for today.\n");
            }

            if (!pendingFiles.isEmpty()) {
                emailContent.append("\n⚠ **Pending Files from Previous Days:**\n");
                emailContent.append("------------------------------------------------------------------------------------------------------------------------------\n");
                for (String file : pendingFiles) {
                    emailContent.append(file).append("\n");
                }
            } else {
                emailContent.append("\n🎉 **No pending files remaining!**\n");
            }

            emailContent.append("\n================================== ***DONE*** ==================================\n\n");
        }

        // Send email only if there are relevant files to report
        sendEmailAlert(emailContent.toString());
    }

    private static void checkFiles(String machine, String directory, String todayDate, List<String> todayFiles, List<String> pendingFiles) {
        JSch jsch = new JSch();

        try {
            Session session = jsch.getSession(USER, machine, PORT);
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
                if (!line.startsWith("total")) { // Ignore total size line
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 6) {
                        String fileDate = parts[5]; // Extract date part

                        if (fileDate.equals(todayDate)) {
                            todayFiles.add(line);
                        } else {
                            pendingFiles.add(line);
                        }
                    }
                }
            }

            channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
            System.err.println("Error connecting to " + machine + ": " + e.getMessage());
        }
    }

    private static void sendEmailAlert(String messageBody) {
        String to = "divya.d@htic.iitm.ac.in";  // Recipient email
        String from = "gayathri@htic.iitm.ac.in";  // Sender email (unchanged as per your request)
        String host = "smtp.gmail.com";

        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        javax.mail.Session session = javax.mail.Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("gayathri@htic.iitm.ac.in", "Gayu@0918"); // Authentication details remain the same
            }
        });

        session.setDebug(true);
        
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("📢 **Current Machine Status Report** 📢");
            message.setText("================================== MACHINE STATUS ==================================\n\n"
                    + "This is Automation Message For Warning"
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
