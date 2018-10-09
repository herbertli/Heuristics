set permissions: "chmod 755 comp.sh"
set gcc and compile: ". comp.sh"
run: "./main --host=XXX.XXX.XXX.XXX --port=XXXX [--first]"

note that "--first" is optional while --host and --port are not.
