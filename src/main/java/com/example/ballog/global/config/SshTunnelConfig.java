package com.example.ballog.global.config;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import jakarta.annotation.PreDestroy;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Component
@ConfigurationProperties(prefix = "ssh")
@Validated
@Setter
public class SshTunnelConfig {
    @Value("${ec2.remote_jump_host}")
    private String remoteJumpHost;
    @Value("${ec2.ssh_port}")
    private int sshPort;
    @Value("${ec2.user}")
    private String user;
    @Value("${ec2.private_key_path}")
    private String privateKeyPath;
    @Value("${ec2.database_endpoint}")
    private String databaseEndpoint;
    @Value("${ec2.database_port}")
    private int databasePort;


    private Session session;

    @PreDestroy
    public void destroy() {
        if (session.isConnected()) {
            session.disconnect();
        }
    }

    public Integer buildSshConnection() {
        Integer forwardPort = null;

        try {
            log.info("Connecting to SSH with {}@{}:{} using privateKey at {}", user, remoteJumpHost,
                    sshPort, privateKeyPath);
            JSch jsch = new JSch();

            jsch.addIdentity(privateKeyPath);
            session = jsch.getSession(user, remoteJumpHost, sshPort);
            session.setConfig("StrictHostKeyChecking", "no");

            log.info("Starting SSH session connection...");
            session.connect();
            log.info("SSH session connected");

            forwardPort = session.setPortForwardingL(0, databaseEndpoint, databasePort);
            log.info("Port forwarding created on local port {} to remote port {}", forwardPort,
                    databasePort);
        } catch (JSchException e) {
            log.error(e.getMessage());
            this.destroy();
            throw new RuntimeException(e);
        }
        return forwardPort;
    }
}
