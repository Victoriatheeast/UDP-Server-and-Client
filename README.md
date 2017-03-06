## UDP-Server-and-Client ##

### File List ###

* PingServer.java
* PingClient.java

### Compile ### 

* javac *.java

### Run ###

* java PingServer --port=56789 --loss_rate=0.0 --avg_delay=100
* java PingClient --server_ip=127.0.0.1 --server_port=56789 \
--count=10 --period=1000 --timeout=60000
* java PingServer --port=56789 --loss_rate=0.0 --avg_delay=100
* java PingClient --server_ip=127.0.0.1 --server_port=56789 \
--count=5 --period=1000 --timeout=60000
* java PingServer --port=56789 --loss_rate=0.0 --avg_delay=100
* java PingClient --server_ip=127.0.0.1 --server_port=56789 \
--count=10 --period=2000 --timeout=60000
* Large Delay: java PingServer --port=56789 --loss_rate=0.0 --avg_delay=5000
* java PingClient --server_ip=127.0.0.1 --server_port=56789 \
--count=10 --period=1000 --timeout=60000
* Loss: java PingServer --port=56789 --loss_rate=0.75 --avg_delay=100
* java PingClient --server_ip=127.0.0.1 --server_port=56789 \
--count=10 --period=1000 --timeout=10000
* Short Timeout: java PingServer --port=56789 --loss_rate=0.0 --avg_delay=10000
* java PingClient --server_ip=127.0.0.1 --server_port=56789 \
--count=10 --period=1000 --timeout=2000
* Long Timeout: java PingServer --port=56789 --loss_rate=0.0 --avg_delay=2000
* java PingClient --server_ip=127.0.0.1 --server_port=56789 \
--count=10 --period=1000 --timeout=10000
* No Server: java PingClient --server_ip=127.0.0.1 --server_port=56789 \
--count=10 --period=1000 --timeout=100
