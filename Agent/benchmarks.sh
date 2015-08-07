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

ant perf-tests -Dargs='-gc=true -rff ./jmh_tests/thread1_batch_50000.csv -t 1 -bs 50000'
ant perf-tests -Dargs='-gc=true -rff ./jmh_tests/thread2_batch_50000.csv -t 2 -bs 50000'
#ant perf-tests -Dargs='-gc=true -rff ./jmh_tests/thread3_batch_50000.csv -t 3 -bs 50000'
#ant perf-tests -Dargs='-gc=true -rff ./jmh_tests/thread4_batch_50000.csv -t 4 -bs 50000'

ant perf-tests -Dargs='-gc=true -rff ./jmh_tests/thread1_batch_75000.csv -t 1 -bs 75000'
ant perf-tests -Dargs='-gc=true -rff ./jmh_tests/thread2_batch_75000.csv -t 2 -bs 75000'
#ant perf-tests -Dargs='-gc=true -rff ./jmh_tests/thread3_batch_50000.csv -t 3 -bs 75000'
#ant perf-tests -Dargs='-gc=true -rff ./jmh_tests/thread4_batch_50000.csv -t 4 -bs 75000'

ant perf-tests -Dargs='-gc=true -rff ./jmh_tests/thread1_batch_100000.csv -t 1 -bs 100000'
ant perf-tests -Dargs='-gc=true -rff ./jmh_tests/thread2_batch_100000.csv -t 2 -bs 100000'
#ant perf-tests -Dargs='-gc=true -rff ./jmh_tests/thread3_batch_100000.csv -t 3 -bs 100000'
#ant perf-tests -Dargs='-gc=true -rff ./jmh_tests/thread4_batch_100000.csv -t 4 -bs 100000'

ant perf-tests -Dargs='-gc=true -rff ./jmh_tests/thread1_batch_125000.csv -t 1 -bs 125000'
ant perf-tests -Dargs='-gc=true -rff ./jmh_tests/thread2_batch_125000.csv -t 2 -bs 125000'
#ant perf-tests -Dargs='-gc=true -rff ./jmh_tests/thread3_batch_100000.csv -t 3 -bs 125000'
#ant perf-tests -Dargs='-gc=true -rff ./jmh_tests/thread4_batch_100000.csv -t 4 -bs 125000'

ant perf-tests -Dargs='-gc=true -rff ./jmh_tests/thread1_batch_150000.csv -t 1 -bs 150000'
ant perf-tests -Dargs='-gc=true -rff ./jmh_tests/thread2_batch_150000.csv -t 2 -bs 150000'
#ant perf-tests -Dargs='-gc=true -rff ./jmh_tests/thread3_batch_150000.csv -t 3 -bs 150000'
#ant perf-tests -Dargs='-gc=true -rff ./jmh_tests/thread4_batch_150000.csv -t 4 -bs 150000'

#echo "reset Clockspeed"

#echo "enable Turbo Boost"
#sudo wrmsr -p0 0x1a0 0x850089
#sudo wrmsr -p1 0x1a0 0x850089
#sudo wrmsr -p2 0x1a0 0x850089
#sudo wrmsr -p3 0x1a0 0x850089