default: compile

setup:
	module load gcc-8.2
	ulimit -s 16384

compile: setup
    g++ -std=c++11 $(file).cpp -o $(file)

clean:
    -rm -f $(file)
