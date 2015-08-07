#!/bin/bash

#echo "Scale down clock-speed. TODO: adjust it for each hardware"
#sudo cpufreq-set -c 0 -f 2200Mhz
#sudo cpufreq-set -c 1 -f 2200Mhz
#sudo cpufreq-set -c 2 -f 2200Mhz
#sudo cpufreq-set -c 3 -f 2200Mhz

#echo "Turn off Turbo Boost"
#sudo wrmsr -p0 0x1a0 0x4000850089
#sudo wrmsr -p1 0x1a0 0x4000850089
#sudo wrmsr -p2 0x1a0 0x4000850089
#sudo wrmsr -p3 0x1a0 0x4000850089

#VARIABLES
# i in for loop is always batchSize

warmIter=20
iter=20
forks=10

## do it for 1 and 2 threads
for t in 1 2
do

	## really small tests
	for i in {1..150..1}
	do
		ant perf-tests -Dargs="-gc=true -rf json -rff ./jmh_tests/thread${t}_batch_${i}.json -t ${t} -f ${forks} -wi ${warmIter} -i ${iter} -bs ${i}"
	done
	
	
	##### TESTS FOR MEASURING SINGLE EXECUTIONS
	for i in {50..1500..50}
	do
		initSize=200
		ant perf-tests -Dargs="-gc=true -rf json -rff ./jmh_tests/thread${t}_batch_${i}_initial_${initSize}.json -t ${t} -f ${forks} -wi ${warmIter} -i ${iter} -bs ${i} -p initialSize=${initSize}"
	done
	
	for i in {100..2000..100}
	do
		initSize=5000
		ant perf-tests -Dargs="-gc=true -rf json -rff ./jmh_tests/thread${t}_batch_${i}_initial_${initSize}.json -t ${t} -f ${forks} -wi ${warmIter} -i ${iter} -bs ${i} -p initialSize=${initSize}"
	done
	
	for i in {250..5000..250}
	do
		initSize=10000
		ant perf-tests -Dargs="-gc=true -rf json -rff ./jmh_tests/thread${t}_batch_${i}_initial_${initSize}.json -t ${t} -f ${forks} -wi ${warmIter} -i ${iter} -bs ${i} -p initialSize=${initSize}"
	done
done

## future tests
# ant perf-tests -Dargs=".*Concurrent.* -p initialSize=1000,1000000"


#echo "reset Clockspeed"

#echo "enable Turbo Boost"
#sudo wrmsr -p0 0x1a0 0x850089
#sudo wrmsr -p1 0x1a0 0x850089
#sudo wrmsr -p2 0x1a0 0x850089
#sudo wrmsr -p3 0x1a0 0x850089
