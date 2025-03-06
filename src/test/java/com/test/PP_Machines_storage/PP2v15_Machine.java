package com.test.PP_Machines_storage;

import java.io.InputStream;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.testng.annotations.Test;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;

public class PP2v15_Machine {
    
     @Test
        public void testStorageDetails() {
            JSch jsch = new JSch();
            com.jcraft.jsch.Session session = null;
            try {
                String user = "hbp";
                String host = "pp2.humanbrain.in";
                String password = "Health#123";
                int port = 22;
                session = jsch.getSession(user, host, port);
                session.setPassword(password);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();
                Channel channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand("df -h /dev/sda  ");
                channel.setInputStream(null);
                ((ChannelExec) channel).setErrStream(System.err);
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
                        ee.printStackTrace();
                    }
                }

                String[] lines = output.toString().split("\n");
                System.out.println("+------------------------------------+------+-------+-------+--------+----------------------+");
                System.out.println("|       Filesystem                   | Size | Used  | Avail |  Use%  | Mounted on           |");
                System.out.println("+------------------------------------+------+-------+-------+--------+----------------------+");

                StringBuilder emailContent = new StringBuilder();
                boolean sendEmail = false;
                for (int i = 1; i < lines.length; i++) {
                    String[] parts = lines[i].trim().split("\\s+");
                    System.out.printf("| %-34s | %4s | %5s | %5s | %6s | %-20s |\n", parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
                    System.out.println("+------------------------------------+------+-------+-------+--------+----------------------+");

                    int usePercent = Integer.parseInt(parts[4].replace("%", ""));
                    if (usePercent > 70) {
                        sendEmail = true;
                        if (parts[0].equals("/dev/sda")) {
                            emailContent.append("pp2v15.humanbrain.in  - D2_nvmeShare used storage is exceeding 70%\n");
                        }
                    }
                }
                if (sendEmail) {
                    sendEmailAlert(emailContent.toString());
                }
                channel.disconnect();
                session.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Test encountered an exception: " + e.getMessage());
            }
        }

        private void sendEmailAlert(String messageBody) {
            String to = "gayathrigayu0918@gmail.com";
            String from = "gayathri@htic.iitm.ac.in";
            String host = "smtp.gmail.com";
            Properties properties = System.getProperties();
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", "465");
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.auth", "true");
            javax.mail.Session session = javax.mail.Session.getInstance(properties, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("gayathri@htic.iitm.ac.in", "Gayu@0918");
                }
            });
            session.setDebug(true);
            try {
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(from));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
                message.setSubject("PP2v15.humanbrain.in - Storage Alert");
                message.setText("This is an automatically generated email, \n\nSTORAGE ALERT!!!\n" + messageBody);
                System.out.println("sending...");
                Transport.send(message);
                System.out.println("Sent message successfully....");
            } catch (MessagingException mex) {
                mex.printStackTrace();
            }
        }
    }
