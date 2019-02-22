# SSL Encryption
As the TeamSpeak 3 query connection does not support SSL connections, any connection made through an unsecured network will be vulnerable to sniffing and man-in-the-middle attacks.  
To prevent this, the JeakBot framework has the ability to establish SSL tcp connections allowing the use of a tcp reverse-proxy.  
This way, the main connection would be secured and SSL termination can be performed on the target machine where the TS3 server is running.  
  
## Configuration


### Nginx (SSL termination proxy)
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

### JeakBot
In the bot configuration file, set ``ssl`` to ``true``:
```json
{
  "host": "your-domain.com",
  "port": 9988,
  "ssl": true
}
```

If the connection fails with issues regarding the chain of trust, Java may be lacking the required root certificates.  
Java retrieves additional root certificates from [TrustStores](https://docs.oracle.com/cd/E19509-01/820-3503/ggffo/index.html).
Administrators can drop their trusted root CAs in PEM format into this directory and run ``./create_keystore.sh``.
This will generate a keystore with the CAs as trusted certificates.  
Afterwards, edit your JeakFramework startup command to include the following JVM options.  
```
-Djavax.net.ssl.trustStore=../utils/trusted_roots.jks
-Djavax.net.ssl.keyStorePassword=jeakbot
```  
Additional information can also be viewed by enabling SSL debugging in Java (``-Djavax.net.debug=ssl``).