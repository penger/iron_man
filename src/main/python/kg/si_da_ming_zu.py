import pandas as pd
import mysql.connector
from py2neo import Graph, Node, Subgraph, NodeMatcher, Relationship, RelationshipMatcher
import os

file_path = 'E:\知识图谱数据'
file_name = 'triples.csv'

url = "http://192.168.1.123"
username = 'neo4j'
password = 'happy_cassini'

node_type = 'person'

graph = Graph(url, auth=(username, password), name='neo4j')


def select_or_create(item):
    header = NodeMatcher(graph).match(node_type, name=item['head']).first()
    if header is None:
        print(item['head'])
        graph.create(Node(node_type, name=str(item['head'])))
    tailer = NodeMatcher(graph).match(node_type, name=item['tail']).first()
    if tailer is None:
        graph.create(Node(node_type, name=str(item['tail'])))
    return header, tailer


nodes = []
rels = []

for innerpath in os.listdir(file_path):
    real_file = os.path.join(file_path, innerpath, file_name)
    df = pd.read_csv(real_file)
    for i in range(len(df)):
        row = df.iloc[i]
        # 创建节点
        head, tail = select_or_create(row)
        # 创建关系
        print(row['label'])
        rels.append(Relationship(head, "acted", tail, name="xxxx"))

print(len(nodes))
print(len(rels))

for i in range(len(nodes)):
    graph.create(nodes[i])

for j in range(len(rels)):
    graph.create(rels[j])
