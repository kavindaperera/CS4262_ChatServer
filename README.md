

## A Chat Server is executed as follows:
```cmd
java -jar ChatServer.jar -i serverid -f servers_conf
```

```cmd
java -jar ChatServer.jar -i s1 -f /home/ec2-user/servers_conf.txt
```
## Connect to an AWS EC2 instance

- Open an SSH client.
- Locate your private key file. 
- The key used to launch `Ohio` is `chatserver-test.pem`
- The key used to launch `Mumbai` is `chatserver-instance-2.pem`
- Run this command, if necessary, to ensure your key is not publicly viewable.
```
chmod 400 chatserver-test.pem
```
```
chmod 400 chatserver-instance-2.pem
```
- Connect to your instance using its Public DNS:
```
 ec2-3-134-109-203.us-east-2.compute.amazonaws.com
```
```
 ec2-13-235-128-78.ap-south-1.compute.amazonaws.com
```

Example:
```
 ssh -i "chatserver-test.pem" ec2-user@ec2-3-134-109-203.us-east-2.compute.amazonaws.com
```
```
 ssh -i "chatserver-instance-2.pem" ec2-user@ec2-13-235-128-78.ap-south-1.compute.amazonaws.com
```

Public IPv4 address `Ohio` : `3.134.109.203`

Public IPv4 address `Mumbai` : `13.235.128.78`

```
s1	3.134.109.203	4445	5555
s2	3.134.109.203	4446	5556
s3	13.235.128.78	4447	5557
s4	13.235.128.78	4448	5558
```