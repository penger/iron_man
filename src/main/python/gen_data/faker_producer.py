from faker import Faker
from time import sleep
from json import dumps
from kafka import KafkaProducer
import json


class Person:
    def __init__(self, name, address, province, phone_number, ssn, birthday, email):
        self.name = name
        self.address = address
        self.province = province
        self.phone_number = phone_number
        self.ssn = ssn
        self.birthday = birthday
        self.email = email

    def __str__(self):
        return f'name is {self.name} address is {self.address} province is {self.province} phone_number is {self.phone_number} snn is {self.ssn} birthday is {self.birthday} email is {self.email}'


my_producer = KafkaProducer(
    bootstrap_servers=['cassini:29092'],
    value_serializer=lambda x: dumps(x).encode('utf-8')
)

faker = Faker(locale="zh_CN")

for i in range(500000):
    my_data = {'num': i}
    p = Person(faker.name(), faker.address(), faker.province(), faker.phone_number(), faker.ssn(), faker.ssn()[6:14],
               faker.email())
    my_data = json.dumps(p.__dict__,ensure_ascii=False)
    print(my_data)
    my_producer.send(topic='test4', value=my_data)
    # sleep(2)
