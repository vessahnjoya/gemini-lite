# Project Report

Author: Louis Nathan Vessah Njoya Tchuente

Email: <v.vessahnjoyatchuente@student.maastrichtuniversity.nl>

Student ID number: i6371413

## Gemini Lite Client Program

(Insert user documentation for your program here. Include command-line usage instructions.)

### Bonus enhancements

(If you attempt any bonus enhancements, document them in this section.)

## Gemini Lite Server Program

(Insert user documentation for your program here. Include command-line usage instructions.)

### Bonus enhancements

(If you attempt any bonus enhancements, document them in this section.)

## Gemini Lite Proxy Program

(Insert user documentation for your program here. Include command-line usage instructions.)

### Bonus enhancements

(If you attempt any bonus enhancements, document them in this section.)

## Test Cases

(Include at least 3 of each kind of test case here, formatted as instructed in the project manual.)

### Client test cases

### Server test cases

### Proxy test cases

## Alternative DNS, Bakeoff and Wireshark outputs

### Lab4

#### Step 2

- Running `dig -t ns lab-kale` to retrieve all domain name server (ns) records associated with the domain lab-kale.

![alt text](report-images/step2-part1.png)

- Running `dig ns1.lab-kale` to retrieve the domain's ip address.

![alt text](report-images/step2-part2.png)

- Querying the IP address to retrieve all available dns records associated with the subdomain `hate-ai.lab-kale`

![alt text](report-images/step3-part3.png)

-

#### Step 5

Name: Berke, ID: i63

### Lab5

### Lab 5: Packet capture of joining a network

1- What are the source and destination ethernet addresses for the request packet?

`source: 5e:05:18:37:73:a2,
destination: ff:ff:ff:ff:ff:ff`

2- What are the IP source and destination addresses in the request packet?

`source: 0.0.0.0, destination: 255.255.255.255`

3- What are the UDP source and destination port numbers in the request packet?

`source: 68, destination: 67`

4- What is the value of the DHCP “Your (client) IP address” field in the request packet?

`IP: 0.0.0.0`

5- Does the request contain an Option 50 (Requested IP Address)? If so, what is the IP address being requested?

`Requested IP: 10.2.2.169`

6- What are the source and destination ethernet addresses for the ACK packet?

`source: 2c:cf:67:32:7f:67, destination: 5e:05:18:37:73:a2`

7- What are the IP source and destination addresses in the ACK packet?

`source: 10.2.0.1, destination: 10.2.2.169`

8- What are the UDP source and destination port numbers in the ACK packet?

`source: 67, destination: 68`

9- What is the value of the DHCP “Your (client) IP address” field in the ACK packet?

`IP: 10.2.2.169`

10- What are the values of the following DHCP options in the ACK packet?

`Option (1) Subnet Mask: 255.255.252.0, length: 4`

`Option (3) Router: 10.2.0.3, length: 4`

`Option (6) Domain Name Server: 10.2.0.1, length: 4`

11- Look back at Lab 2. Use the appropriate command (ip route, netstat -rn, or route print) to print out your laptop’s forwarding table. Satisfy yourself you can find the Subnet Mask and Router from the DHCP ACK packet in your machine’s forwarding table. Copy and paste the table into your report.

`unfortunately I did not answer these questions during the lab hence cannot have the subnet mask inside the route table as I am not connected to the kale network anymore`


## Reflection on Gemini Lite

(Paragraph or short essay-style answers to the questions)
