package com.example.kscommon.utils;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

public class JschUtil {

    private static final Logger logger = LoggerFactory.getLogger(JschUtil.class);

    JSch jSch = new JSch();

    // session对象
    Session session;

    // JAVA与主机的连接通道
    Channel channel;

    // sftp通道
    ChannelSftp chSftp;

    // 主机ip
    String host = "47.99.113.58";

    // 主机端口号
    int port = 22;

    // 主机账号
    String username = "root";

    // 主机密码
    String password = "asd1245+asd";

    /**
     * 检测是否可以和主机通信
     */
    public boolean connect() throws JSchException {
        boolean result = session.isConnected();
        close();
        return result;
    }

    /**
     * 关闭连接
     */
    public void close() {

        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }

        if (session != null && session.isConnected()) {
            session.disconnect();
        }

        if (chSftp != null && chSftp.isConnected()) {
            chSftp.quit();
        }

    }

    /**
     * 执行shell命令
     */
    public String execCommand(String command) {
        // 存放执行命令结果
        StringBuilder result = new StringBuilder();
        int exitStatus = 0;
        try {
            getConnectedSession();
            channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            channel.setInputStream(null);
            // 错误信息输出流，用于输出错误的信息，当exitstatus<0的时候
            ((ChannelExec) channel).setErrStream(System.err);

            // 执行命令，等待执行结果
            channel.connect();

            // 获取命令执行结果
            InputStream in = channel.getInputStream();

            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    result.append(new String(tmp, 0, i));
                }
                // 从channel获取全部信息之后，channel会自动关闭
                if (channel.isClosed()) {
                    if (in.available() > 0) {
                        continue;
                    }
                    exitStatus = channel.getExitStatus();
                    break;
                }
            }


        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (JSchException e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }

        logger.info("退出码为：" + exitStatus);
        return result.toString();
    }

    /**
     * 文件上传至主机
     */
    public String upload(MultipartFile file, String dir, String today, boolean randomName) {
        String baseUrl = "https://www.guetzjb.cn/assets_other/" + today;
        String fileName;
        if (randomName) {
            fileName = UUID.randomUUID().toString() + Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));
        } else {
            fileName = file.getOriginalFilename();
        }
        try {
            // 打开SFTP通道
            getConnectedSession();
            chSftp = (ChannelSftp) session.openChannel("sftp");

            chSftp.connect();

            // 设置编码格式
            chSftp.setFilenameEncoding("UTF-8");

            dir += today;
            if (!enterPath(dir)) {
                chSftp.mkdir(dir);
                enterPath(dir);
            }
            chSftp.put(file.getInputStream(), dir + "/" + fileName);
            logger.info("文件上传成功");
        } catch (JSchException | SftpException e) {
            logger.warn(e.getMessage());
            return "";
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }
        return baseUrl + "/" + fileName;
    }

    public String upload(InputStream stream, String dir, String filePath, String fileName) {
        String baseUrl = "https://www.guetzjb.cn/assets_other" + filePath;
        try {
            // 打开SFTP通道
            getConnectedSession();
            chSftp = (ChannelSftp) session.openChannel("sftp");
            chSftp.connect();
            // 设置编码格式
            chSftp.setFilenameEncoding("UTF-8");
            dir += filePath;
            if (!enterPath(dir)) {
                chSftp.mkdir(dir);
                enterPath(dir);
            }
            chSftp.put(stream, dir + "/" + fileName);
            logger.info("文件上传成功");
        } catch (JSchException | SftpException e) {
            logger.warn(e.getMessage());
            return "";
        } finally {
            close();
        }
        return baseUrl + "/" + fileName;
    }

    private boolean enterPath(String dir) {
        try {
            chSftp.cd(dir);
        } catch (SftpException e) {
            return false;
        }
        return true;
    }

    /**
     * 将主机文件下载至本地
     *
     * @param directory    下载到本地的位置
     * @param downloadFile 下载文件在虚拟机的位置
     */
    public void download(String directory, String downloadFile) {

        try {
            getConnectedSession();
            // 打开SFTP通道
            chSftp = (ChannelSftp) session.openChannel("sftp");

            // 建立SFTP通道的连接
            chSftp.connect();

            // 设置编码格式
            chSftp.setFilenameEncoding("UTF-8");

            chSftp.get(directory, downloadFile);

            logger.info("文件下载成功");
        } catch (JSchException | SftpException e) {
            logger.warn(e.getMessage());
        } finally {
            close();
        }
    }

    private void getConnectedSession() throws JSchException {
        // 根据主机账号、ip、端口获取一个Session对象
        session = jSch.getSession(username, host, port);

        // 存放主机密码
        session.setPassword(password);

        // 首次连接，去掉公钥确认
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        // 超时连接时间为3秒
        session.setTimeout(3000);
        session.connect();
    }

    public void mkdir(String filePath, String dirName) {
        try {
            getConnectedSession();
            chSftp = (ChannelSftp) session.openChannel("sftp");
            chSftp.connect();
            // 设置编码格式
            chSftp.setFilenameEncoding("UTF-8");
            filePath += "/" + dirName;
            if (!enterPath(filePath)) {
                chSftp.mkdir(filePath);
            }
        } catch (JSchException e) {
            throw new RuntimeException(e);
        } catch (SftpException e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }
    }
}

