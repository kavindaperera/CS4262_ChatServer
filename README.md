

## A Chat Server is executed as follows:
```cmd
java -jar ChatServer.jar -i serverid -f servers_conf
```

```cmd
java -jar ChatServer.jar -i s1 -f /home/ec2-user/servers_conf.txt
```
## Connect to a AWS EC2 instance

- Open an SSH client.
- Locate your private key file. The key used to launch this instance is `chatserver-test.pem`
- Run this command, if necessary, to ensure your key is not publicly viewable.
```
chmod 400 chatserver-test.pem
```
- Connect to your instance using its Public DNS:
```
 ec2-3-134-109-203.us-east-2.compute.amazonaws.com
```

Example:
```
 ssh -i "chatserver-test.pem" ec2-user@ec2-3-134-109-203.us-east-2.compute.amazonaws.com
```

Public IPv4 address : `3.134.109.203`
