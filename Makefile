default: compile

compile: SHELL:=/usr/local/bin/bash
compile:
	module load gcc-8.2
	ulimit -s 16384 && g++ -std=c++11 $(file).cpp -o $(file)
