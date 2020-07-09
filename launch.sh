#!/bin/bash
abs_path=`pwd`
java -Djava.library.path="$abs_path/lib/native/windows/x64/" -jar target/videotag*.jar
