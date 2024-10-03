package org.dreamcodesoft;

import com.jcraft.jsch.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

public class SftpConnection {
    private Properties properties;
    private Session session;
    private ChannelSftp channelSftp;

    SftpConnection(){
        try {
            this.properties = loadProperties("sftp.properties");
            this.channelSftp = this.connect();
        } catch (IOException e){
            System.out.println("No se ha podido cargar las propiedades: " + e);
        }

    }

    private Properties loadProperties(String path) throws IOException {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(path)) {
            if (input == null) {
                throw new FileNotFoundException("archivo sftp.properties no encontrado: " + path);
            }
            props.load(input);
        }
        return props;
    }

    private ChannelSftp connect() {
        try {
            String host         = properties.getProperty("sftp.host");
            String username     = properties.getProperty("sftp.username");
            String password     = properties.getProperty("sftp.password");
            int port            = Integer.parseInt(properties.getProperty("sftp.port"));

            JSch jsch = new JSch();
            this.session = jsch.getSession(username, host, port);
            this.session.setPassword(password);
            this.session.setConfig("StrictHostKeyChecking", "no");
            this.session.connect();

            ChannelSftp channelSftp = (ChannelSftp) this.session.openChannel("sftp");
            channelSftp.connect();
            return channelSftp;
        } catch (JSchException e){
            System.out.println("No se ha podido conectar al servidor: "+ e);
        }
        return null;
    }

    public List<String> downloadFiles(String localDir, String remoteDir) throws SftpException, NullPointerException {

        List<String> downloadedFiles = new ArrayList<>();
        Vector<ChannelSftp.LsEntry> list = this.connect().ls(remoteDir);

        for (ChannelSftp.LsEntry entry : list) {
            if (!entry.getAttrs().isDir() && entry.getFilename().endsWith(".xlsx")) {
                String remoteFile = remoteDir + "/" + entry.getFilename();
                String localFile = localDir + "/" + entry.getFilename();
                channelSftp.get(remoteFile, localFile);
                downloadedFiles.add(localFile);
            }
        }

        this.channelSftp.disconnect();
        this.session.disconnect();

        return downloadedFiles;
    }

    public String getRemoteDir(){
        return this.properties.getProperty("sftp.remoteDir");
    }

    public String getLocalDir (){
        return this.properties.getProperty("sftp.localDir");
    }
}
