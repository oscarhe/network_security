#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <strings.h>
#include <netinet/in.h>
#include <netdb.h>
#include <unistd.h>

/* Compilation line: gcc cli.c -o cli -lsocket -lnsl */
/* Running: ./cli <IP Adress> (128.226.6.39 for Bingsuns) */

void error(const char *message) {
	perror(message);
	exit(1);
}

int main(int argc, char **argv) {
        int  sockfd, n, portno, byeFlag = 0; 
        char recvline[256];
        struct sockaddr_in servaddr;
	struct hostent *server;

	if(argc!=3){
		error("Not enough arguments"); 
	}
	
	//get port number
	portno = atoi(argv[2]);

        server = gethostbyname(argv[1]);

        /* Specify server’s IP address and port */
        bzero(&servaddr, sizeof(servaddr));
        servaddr.sin_family = AF_INET;
	bcopy((char *)server -> h_addr, (char *)&servaddr.sin_addr.s_addr, 
server -> h_length);
        servaddr.sin_port = htons(portno); /* daytime server port */

	/*
        if (inet_pton(AF_INET, argv[1], &servaddr.sin_addr) <= 0) {
			 perror("inet_pton"); exit(3);
        }
	*/
	system("clear");

for(;;) {
	printf("ftp > ");
	bzero(recvline, 256);
	fgets(recvline, 255, stdin);
	char cdStr[256];
	strcpy(cdStr, recvline);
	char *pch;
	pch = strtok(recvline, " ");

	if (strcmp(pch, "lcd") == 0) {
		pch = strtok(NULL, " ");
		pch[strlen(pch) - 1] = 0;
		chdir(pch);
	}
	pch[strlen(pch) - 1] = 0;
	if (strcmp(pch, "lls") == 0) {
		system("ls");
	}
	if (strcmp(pch, "lpwd") == 0) {
		system("pwd");
	}
	if (strcmp(pch, "ls") == 0 || strcmp(pch, "pwd") == 0 || strcmp(pch, 
"bye") == 0 || strcmp(pch, "c") == 0) {
		if (strcmp(pch, "bye") == 0) {
			byeFlag = 1;
		}
        	sockfd = socket(AF_INET,SOCK_STREAM, 0);
        	if (sockfd < 0) error("Error opening socket");        	

		if (connect(sockfd,  (struct sockaddr *) 
&servaddr,sizeof(servaddr))
< 0 ) error("Error connecting");

		n = write(sockfd, cdStr, strlen(cdStr));
		if (n < 0) error("Error writing to socket");
		
		close(sockfd);
		
	}
	if (byeFlag == 1) break;
}

} 

