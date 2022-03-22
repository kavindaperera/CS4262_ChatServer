

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
- The key used to launch `Singapore` is `chatserver-instance-3.pem`
- Run this command, if necessary, to ensure your key is not publicly viewable.
```
chmod 400 chatserver-test.pem
```

- Connect to your instance using its Public DNS:

Ohio
```
ec2-3-142-91-69.us-east-2.compute.amazonaws.com
```

Mumbai
```
ec2-65-1-84-110.ap-south-1.compute.amazonaws.com
```

Singapore
```
ec2-13-213-32-97.ap-southeast-1.compute.amazonaws.com
```

### Example:

Ohio
```
ssh -i "chatserver-test.pem" ec2-user@ec2-3-142-91-69.us-east-2.compute.amazonaws.com
```
Mumbai
```
ssh -i "chatserver-instance-2.pem" ec2-user@ec2-65-1-84-110.ap-south-1.compute.amazonaws.com
```
Singapore
```
ssh -i "chatserver-instance-3.pem" ec2-user@ec2-13-213-32-97.ap-southeast-1.compute.amazonaws.com
```

Public IPv4 address `Ohio` : `3.142.91.69`

Public IPv4 address `Mumbai` : `65.1.84.110`

Public IPv4 address `Singapore` : `13.213.32.97`

```
s1	3.142.91.69     4445	5555
s2	3.142.91.69     4446	5556
s3	65.1.84.110     4447	5557
s4	65.1.84.110     4448	5558
s5	13.213.32.97	4449	5559
s6	13.213.32.97	4450	5560	
```