# python 环境安装
```
conda create --name=iron_man python=3.8

activate iron_man
    
python -m pip install -i https://mirrors.cloud.tencent.com/pypi/simple mysql-connector --proxy http://localhost:10080

pymysql=1.0.3

生成 requirements.txt

conda list -e > requirements.txt

python -m pip install -i https://mirrors.cloud.tencent.com/pypi/simple kafka-python --proxy http://localhost:10080
python -m pip install -i https://mirrors.cloud.tencent.com/pypi/simple flask --proxy http://localhost:10080
python -m pip install -i https://mirrors.cloud.tencent.com/pypi/simple elasticsearch7 --proxy http://localhost:10080
python -m pip install -i https://mirrors.cloud.tencent.com/pypi/simple sqlalchemy --proxy http://localhost:10080
python -m pip install -i https://mirrors.cloud.tencent.com/pypi/simple toollib --proxy http://localhost:10080


```

# java 环境
```
java 环境为 jdk17
```

# docker 启动命令
```
docker run \
--restart always \
--privileged=true \
--name happy_neo4j \
-p 7687:7687 \
-p 7474:7474 \
-v /opt/neo4j/docker_dir/conf:/var/lib/neo4j/conf \
-v /opt/neo4j/docker_dir/import:/var/lib/neo4j/import \
-v /opt/neo4j/docker_dir/logs:/logs \
-v /opt/neo4j/docker_dir/data:/data \
-e NEO4J_AUTH=neo4j/happy_cassini \
-d neo4j:5.2.0-community

或者：

docker run \
--restart always \
--privileged=true \
--net="piesat" \
--name neo4j-master \
--hostname neo4j-master \
--ip 172.168.10.40 \
-p 41040:7687 \
-p 41041:7474 \
-v /data/docker/neo4j/neo4j-master/conf:/var/lib/neo4j/conf \
-v /data/docker/neo4j/neo4j-master/import:/var/lib/neo4j/import \
-v /data/docker/neo4j/neo4j-master/logs:/logs \
-v /data/docker/neo4j/neo4j-master/data:/data \
-e NEO4J_AUTH=neo4j/xxx \
-d neo4j:5.2.0-community

```

# 安装docker-compose 及启动 kafka
```sql
https://www.baeldung.com/ops/kafka-docker-setup
    
version: '2'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - 22181:2181
  
  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    ports:
      - 29092:29092
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:29092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1    
```