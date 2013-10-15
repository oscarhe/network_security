all: ftpserv ftpcli

ftpserv		: ser.o
		gcc ser.o -o ftpserv -lsocket -lnsl

ser.o 		: ser.c
		gcc -c ser.c

ftpcli		: cli.o
		gcc cli.o -o ftpcli -lsocket -lnsl

cli.o		: cli.c
		gcc -c cli.c
