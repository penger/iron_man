import pandas as pd
import mysql.connector
from py2neo import Graph, Node, Subgraph, NodeMatcher, Relationship, RelationshipMatcher

cnx = mysql.connector.connect(user="cassini", password="9ijn)OKM",
                              host="192.168.1.123", database="etl",
                              auth_plugin='mysql_native_password')
cursor = cnx.cursor()

df = pd.read_sql("select * from person ", cnx)

print(df.head())

url = "http://192.168.1.123"
username = 'neo4j'
password = 'happy_cassini'

node_type = 'person'

graph = Graph(url, auth=(username, password), name='neo4j')


def select_or_create(item):
    node = NodeMatcher(graph).match(node_type, name=item['name']).first()
    if node is None:
        graph.create(Node(node_type, **item))
    return node


nodes = []

for i in range(len(df)):
    row = df.iloc[i]
    select_or_create(row)

print(len(nodes))

for i in range(len(nodes)):
    graph.create(nodes[i])

cnx.close()
cursor.close()
