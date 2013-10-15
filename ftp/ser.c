#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <strings.h>
#include <netinet/in.h>
#include <string.h>

/* Compilation: gcc ser.c -o ser -lsocket -lnsl */
/* Running: ./ser <port number> */

void error(const char *message) {
	perror(message);
	exit(1);
}

int main(int argc, char **argv) {
        int listenfd, connfd, clilen, n;
        struct sockaddr_in servaddr, cliaddr;
        char buff[256];
	int portno;
	
	if (argc < 2) {
		error("No port provided\n");
	}

	// gets port number
	portno = atoi(argv[1]);

        /* Create a TCP socket */
        listenfd = socket(AF_INET, SOCK_STREAM, 0);
	if (listenfd < 0) {
		error("Error opening socket");
	}

	/* Initialize server's address and well-known port */
	bzero(&servaddr, sizeof(servaddr));
        servaddr.sin_family      = AF_INET;
        servaddr.sin_addr.s_addr = htonl(INADDR_ANY);
        servaddr.sin_port        = htons(portno);   /* daytime server */

	/* Bind server’s address and port to the socket */
        if (bind(listenfd, (struct sockaddr *) &servaddr, sizeof(servaddr)) 
< 0)
		error("Error on binding");
		       
	/* Convert socket to a listening socket – max 100 pending clients*/
	listen(listenfd, 100); 
   

	for ( ; ; ) {
		/* Wait for client connections and accept them */
		clilen = sizeof(cliaddr);
        	connfd = accept(listenfd, (struct sockaddr *)&cliaddr, 
&clilen);
		if (connfd < 0) {
			error("Error on accept");
		}

		bzero(buff, 256);
		n = read(connfd, buff, 255);
		if (n < 0) error("Error reading from socket");
	
		char *pch;
		pch = strtok(buff, " ");
		if (strcmp(pch, "cd") == 0) {
			pch = strtok(NULL, " ");
			pch[strlen(pch) - 1] = 0;
			if (chdir(pch) < 0) {
				error("Could not change directory");
			}
		}
		pch[strlen(pch) - 1] = 0;
		if(strcmp(pch, "ls") == 0) system(buff);
		
		if(strcmp(pch, "pwd") == 0) system(buff);
		
		if(strcmp(pch, "bye") == 0) break;
   	}
	close(connfd);
	close(listenfd);
}

