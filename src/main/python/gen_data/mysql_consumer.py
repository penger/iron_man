from kafka import KafkaConsumer, consumer
from time import sleep
import json

# 初始化数据库

import sqlalchemy
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy import Column, Integer, String

from sqlalchemy.orm import sessionmaker
from toollib.guid import SnowFlake

engine = create_engine("mysql+pymysql://cassini:9ijn)OKM@192.168.1.123/etl",
                       echo=True)  # echo是否打印运行过程

Base = declarative_base()  # 生成orm基类


class Person(Base):
    __tablename__ = 'person'
    id = Column(String(32), primary_key=True)
    name = Column(String(64))
    address = Column(String(64))
    province = Column(String(64))
    phone_number = Column(String(64))
    ssn = Column(String(64))
    birthday = Column(String(64))
    email = Column(String(64))

    def __str__(self):
        return f'name is {self.name} address is {self.address} province is {self.province} phone_number is {self.phone_number} snn is {self.ssn} birthday is {self.birthday} email is {self.email}'


Base.metadata.create_all(engine)  # 创建表结构

Session_class = sessionmaker(bind=engine)  # 创建与数据库的会话session class ,注意,这里返回给session的是个class,不是实例
Session = Session_class()  # 生成session实例
snow = SnowFlake()


class MessageConsumer:
    broker = ""
    topic = ""
    group_id = ""
    logger = None

    def __init__(self, broker, topic, group_id):
        self.broker = broker
        self.topic = topic
        self.group_id = group_id

    def activate_listener(self):
        my_consumer = KafkaConsumer(bootstrap_servers=self.broker,
                                    group_id='my-group',
                                    consumer_timeout_ms=60000,
                                    auto_offset_reset='latest',
                                    enable_auto_commit=False,
                                    value_deserializer=lambda m: json.loads(m.decode('ascii')))

        my_consumer.subscribe(self.topic)
        print("consumer is listening....")
        try:
            for message in my_consumer:
                print("received message = ", message)
                p_id = snow.gen_uid()
                print(p_id)
                p_json = json.loads(message.value);
                person = Person(name=p_json['name'], address=p_json['address'], province=p_json['province'],
                                ssn=p_json['ssn'], phone_number=p_json['phone_number'], birthday=p_json['birthday'],
                                email=p_json['email'], id=p_id)
                # 写入数据到 mysql并提交
                Session.add(person)
                Session.commit()
                # 生成你要创建的数据对象
                # committing message manually after reading from the topic
                my_consumer.commit()
        except KeyboardInterrupt:
            print("Aborted by user...")
        finally:
            my_consumer.close()


# Running multiple consumers
broker = 'cassini:29092'
topic = 'test4'
group_id = 'consume1'

consumer1 = MessageConsumer(broker, topic, group_id)
consumer1.activate_listener()

# consumer2 = MessageConsumer(broker,topic,group_id)
# consumer2.activate_listener()
