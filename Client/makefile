# All Targets
all: bluffer

# Tool invocations
# Executable "hello" depends on the files hello.o and run.o.
bluffer: bin/connectionHandler.o bin/Client.o 
	@echo 'Building target: hello'
	@echo 'Invoking: C++ Linker'
	g++ -o bin/bluffer bin/connectionHandler.o bin/Client.o -lboost_system -lboost_locale -lboost_thread
	@echo 'Finished building target: hello'
	@echo ' '

# Depends on the source and header files
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ -g -Wall -c -Linclude -o bin/connectionHandler.o src/connectionHandler.cpp
# Depends on the source and header files 
bin/Client.o: src/Client.cpp
	g++ -g -Wall -c -Linclude -o bin/Client.o src/Client.cpp

	
#Clean the build directory
clean: 
	rm -f bin/*