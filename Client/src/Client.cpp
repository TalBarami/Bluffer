#include <stdlib.h>
#include <boost/locale.hpp>
#include "../include/connectionHandler.h"
#include <iostream>
#include <boost/thread.hpp>
#include <boost/date_time.hpp>

//ConnectionHandler connectionHandler("127.0.0.1",2500);

void readerFunc(ConnectionHandler* connectionHandler,bool* endLoop){
	 while (!*endLoop) {
	        std::string answer;
	        if (!connectionHandler->getLine(answer)) {
	            std::cout << "Disconnected. Exiting...\n" << std::endl;
	            break;
	        }
	        std::cout << answer << std::endl;
		if (answer.substr(0,22) == "Press any key to exit.") {
	            //std::cout << "Exiting...\n" << std::endl;
		    *endLoop=true;
	            break;
	        }
	}
}

void writerFunc(ConnectionHandler* connectionHandler,bool* endLoop){
	while (!*endLoop) {
	        const short bufsize = 1024;
	        char buf[bufsize];
	        std::cin.getline(buf, bufsize);
	        std::string line(buf);
	        if (!connectionHandler->sendLine(line)) {
	            std::cout << "Disconnected. Exiting...\n" << std::endl;
	            break;
	        }
	}
}

int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    unsigned short port = atoi(argv[2]);
    
    bool endLoop=false;
    
    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

    boost::thread readerThread(readerFunc,&connectionHandler,&endLoop);
    boost::thread writerThread(writerFunc,&connectionHandler,&endLoop);
    readerThread.join();
    writerThread.join();
    
    std::cout<< "THREADS TERMINATED" << std::endl;
    return 0;

    /*
    std::cout << "This one is listener! \n";
    while (1) {

        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
        std::string line(buf);
        if (!connectionHandler.sendLine(line)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }

    	std::cout << "3333333333333333333333333" << std::endl;
        // We can use one of three options to read data from the server:
        // 1. Read a fixed number of characters
        // 2. Read a line (up to the newline character using the getline() buffered reader
        // 3. Read up to the null character
        std::string answer;
        // Get back an answer: by using the expected number of bytes (len bytes + newline delimiter)
        // We could also use: connectionHandler.getline(answer) and then get the answer without the newline char at the end
        if (!connectionHandler.getLine(answer)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
        std::cout << "44444444444444444444" << std::endl;
		// A C string must end with a 0 char delimiter.  When we filled the answer buffer from the socket
		// we filled up to the \n char - we must make sure now that a 0 char is also present. So we truncate last character.
        //answer.resize(answer.length()-1);
        std::cout << "Reply: " << answer << std::endl;
        if (answer == "bye") {
            std::cout << "Exiting...\n" << std::endl;
            break;
        }*/
}

