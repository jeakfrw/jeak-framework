# SSL Encryption
As the TeamSpeak 3 query connection does not support SSL connections, any connection made through an unsecured network will be vulnerable to sniffing and man-in-the-middle attacks.  
To prevent this, the JeakBot framework has the ability to establish SSL tcp connections allowing the use of a tcp reverse-proxy.  
This way, the main connection would be secured and SSL termination can be performed on the target machine where the TS3 server is running.  
  
## Configuration

```nginx
stream {
  server {
    listen <public-ip>:9987 ssl;
    ssl_certificate /etc/letsencrypt/live/your-domain/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain/privkey.pem;
    proxy_ssl off;
    proxy_pass 127.0.0.1:9987
  }
}
```
Please note: In this configuration, the local port is equivalent to the remote port, because it is assumed that the listening IPs are statically configured.  
If you want to use wildcard IPs for the server configurations (``0.0.0.0``, ``::1``), make sure to change one of the ports.  
